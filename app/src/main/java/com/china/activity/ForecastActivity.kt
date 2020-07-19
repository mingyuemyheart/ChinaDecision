package com.china.activity;

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import com.china.R
import com.china.adapter.ShawnWeeklyForecastAdapter
import com.china.common.MyApplication
import com.china.dto.WeatherDto
import com.china.utils.AuthorityUtil
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import com.china.utils.WeatherUtil
import com.china.view.WeeklyView
import kotlinx.android.synthetic.main.activity_forecast.*
import kotlinx.android.synthetic.main.shawn_layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 天气预报
 */
class ForecastActivity : ShawnBaseActivity(), OnClickListener {

	private var mAdapter: ShawnWeeklyForecastAdapter? = null
	private val weeklyList: ArrayList<WeatherDto> = ArrayList()
	private val sdf1 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
	private val sdf2 = SimpleDateFormat("HH", Locale.CHINA)
	private val sdf3 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_forecast)
		checkAuthority()
	}

	private fun init() {
		initWidget()
		initListView()
	}

	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		showDialog()
		llBack.setOnClickListener(this)
		tvTitle.text = "天气详情"
		//解决scrollView嵌套listview，动态计算listview高度后，自动滚动到屏幕底部
		tvTime.isFocusable = true
		tvTime.isFocusableInTouchMode = true
		tvTime.requestFocus()
		ivSwitcher.setOnClickListener(this)
		ivShare.setOnClickListener(this)
		ivShare.visibility = View.VISIBLE
		refresh()
	}

	private fun refresh() {
		val areaName = intent.getStringExtra("cityName")
		if (!TextUtils.isEmpty(areaName)) {
			tvCity!!.text = areaName
		}
		val cityId = intent.getStringExtra("cityId")
		if (!TextUtils.isEmpty(cityId)) {
			getWeatherInfo(cityId)
		}
	}

	/**
	 * 获取天气数据
	 */
	private fun getWeatherInfo(cityId: String) {
		Thread(Runnable {
			val url = "https://videoshfcx.tianqi.cn/dav_tqwy/ty_weather/data/$cityId.html"
			OkHttpUtil.enqueue(Request.Builder().url(String.format(url, cityId)).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {}
				@Throws(IOException::class)
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						if (!TextUtils.isEmpty(result)) {
							try {
								val obj = JSONObject(result)
								//逐小时预报信息
								if (!obj.isNull("jh")) {
									val jh = obj.getJSONArray("jh")
									llContainer1!!.removeAllViews()
									for (i in 0 until jh.length()) {
										val itemObj = jh.getJSONObject(i)
										val hourlyCode = Integer.valueOf(itemObj.getString("ja"))
										val hourlyTemp = Integer.valueOf(itemObj.getString("jb"))
										val hourlyTime = itemObj.getString("jf")
										val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
										val view = inflater.inflate(R.layout.shawn_layout_hour_forecast, null)
										val tvHour = view.findViewById<TextView>(R.id.tvHour)
										val ivPhe = view.findViewById<ImageView>(R.id.ivPhe)
										val tvPhe = view.findViewById<TextView>(R.id.tvPhe)
										val tvTemp = view.findViewById<TextView>(R.id.tvTemp)
										try {
											val current = sdf2.format(sdf1.parse(hourlyTime)).toInt()
											tvHour.text = current.toString() + "时"
											if (current in 5..17) {
												ivPhe.setImageBitmap(WeatherUtil.getDayBitmap(this@ForecastActivity, hourlyCode))
											} else {
												ivPhe.setImageBitmap(WeatherUtil.getNightBitmap(this@ForecastActivity, hourlyCode))
											}
										} catch (e: ParseException) {
											e.printStackTrace()
										}
										tvPhe.text = getString(WeatherUtil.getWeatherId(hourlyCode))
										tvTemp.text = hourlyTemp.toString() + getString(R.string.unit_degree)
										llContainer1!!.addView(view)
									}
								}
								//15天预报
								if (!obj.isNull("f")) {
									weeklyList.clear()
									val f = obj.getJSONObject("f")
									val f0 = f.getString("f0")
									var foreDate: Long = 0
									var currentDate: Long = 0
									try {
										val fTime = sdf3.format(sdf1.parse(f0))
										foreDate = sdf3.parse(fTime).time
										currentDate = sdf3.parse(sdf3.format(Date())).time
									} catch (e: ParseException) {
										e.printStackTrace()
									}
									if (!f.isNull("f1")) {
										val f1 = f.getJSONArray("f1")
										for (i in 0 until f1.length()) {
											val dto = WeatherDto()
											val weeklyObj = f1.getJSONObject(i)
											//晚上
											dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"))
											dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))))
											dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"))
											dto.lowWindDir = Integer.valueOf(weeklyObj.getString("ff"))
											dto.lowWindForce = Integer.valueOf(weeklyObj.getString("fh"))
											//白天
											dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"))
											dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))))
											dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"))
											dto.highWindDir = Integer.valueOf(weeklyObj.getString("fe"))
											dto.highWindForce = Integer.valueOf(weeklyObj.getString("fg"))
											if (currentDate > foreDate) {
												when (i) {
													0 -> {
														dto.week = "昨天"
													}
													1 -> {
														dto.week = "今天"
														tvFactInfo.text = dto.highTemp.toString() + "℃" + "/" + dto.lowTemp + "℃" + "\n"
													}
													2 -> {
														dto.week = "明天"
													}
													else -> {
														dto.week = CommonUtil.getWeek(i - 1) //星期几
													}
												}
												dto.date = CommonUtil.getDate(f0, i) //日期
											} else {
												when (i) {
													0 -> {
														dto.week = "今天"
														tvFactInfo.text = dto.highTemp.toString() + "℃" + "/" + dto.lowTemp + "℃" + "\n"
													}
													1 -> {
														dto.week = "明天"
													}
													else -> {
														dto.week = CommonUtil.getWeek(i) //星期几
													}
												}
												dto.date = CommonUtil.getDate(f0, i) //日期
											}
											weeklyList.add(dto)
										}
									}
									//一周预报列表
									if (mAdapter != null) {
										mAdapter!!.notifyDataSetChanged()
									}
									//一周预报曲线
									val weeklyView = WeeklyView(this@ForecastActivity)
									weeklyView.setData(weeklyList, foreDate, currentDate)
									llContainer2.removeAllViews()
									llContainer2.addView(weeklyView, CommonUtil.widthPixels(this@ForecastActivity) * 2, CommonUtil.dip2px(this@ForecastActivity, 400f).toInt())
									//空气质量
									if (!obj.isNull("k")) {
										val k = obj.getJSONObject("k")
										if (!k.isNull("k3")) {
											val num = WeatherUtil.lastValue(k.getString("k3"))
											if (!TextUtils.isEmpty(num)) {
												tvFactInfo.text = tvFactInfo.text.toString() + "空气质量 " + WeatherUtil.getAqi(this@ForecastActivity, Integer.valueOf(num)) + " " + num + "\n"
											}
										}
									}
									//实况信息
									if (!obj.isNull("l")) {
										val l = obj.getJSONObject("l")
										if (!l.isNull("l7")) {
											val time = l.getString("l7")
											if (time != null) {
												tvTime.text = time + "发布"
											}
										}
										if (!l.isNull("l5")) {
											val weatherCode = WeatherUtil.lastValue(l.getString("l5"))
											val current = sdf2.format(Date()).toInt()
											if (current in 5..17) {
												ivPhenomenon!!.setImageBitmap(WeatherUtil.getDayBitmap(this@ForecastActivity, Integer.valueOf(weatherCode)))
											} else {
												ivPhenomenon!!.setImageBitmap(WeatherUtil.getNightBitmap(this@ForecastActivity, Integer.valueOf(weatherCode)))
											}
											tvPhe.text = getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode)))
										}
										if (!MyApplication.FACTENABLE) {
											if (!l.isNull("l1")) {
												tvTemp.text = WeatherUtil.lastValue(l.getString("l1"))
											}
											if (!l.isNull("l2")) {
												val humidity = WeatherUtil.lastValue(l.getString("l2"))
												tvFactInfo.text = tvFactInfo.text.toString() + "相对湿度 " + humidity + getString(R.string.unit_percent) + "\n"
											}
											if (!l.isNull("l4")) {
												val windDir = WeatherUtil.lastValue(l.getString("l4"))
												if (!l.isNull("l3")) {
													val windForce = WeatherUtil.lastValue(l.getString("l3"))
													if (!TextUtils.isEmpty(windDir) && !TextUtils.isEmpty(windForce)) {
														tvFactInfo.text = tvFactInfo.text.toString() + getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) +
																" " + WeatherUtil.getFactWindForce(Integer.valueOf(windForce)) + "\n"
													}
												}
											}
										} else {
											okHttpFact()
										}
									}
								}
							} catch (e: JSONException) {
								e.printStackTrace()
							}
						}
						scrollView!!.visibility = View.VISIBLE
						cancelDialog()
					}
				}
			})
		}).start()
	}

	private fun okHttpFact() {
		Thread(Runnable {
			val lat = intent.getDoubleExtra("lat", 0.0)
			val lng = intent.getDoubleExtra("lng", 0.0)
			val url = "https://music.data.cma.cn/lsb/api?elements=TEM,PRE,RHU,WINS,WIND,WEA&interfaceId=getSurfEleInLocationByTime&lat=$lat&lon=$lng&apikey=AxEkluey201exDxyBoxUeYSw&nsukey=IGzynTgkKQ1Hfa3iJTwv4lci%2F%2F13c%2FQm3p83hih8xiri%2Bc5bm0ia85VASrEHrZRsgj6nlBF1U6F3m5PDkUd6oPtd7itR8p%2BwpJi7yIE%2FVcBsCwya6rhj%2BP%2BhBPCCyrb%2BsyYZLhRk5pkL73jJKE%2Ff4O7PWGPRwVtgQAqgFQ1XEXROJp7qMek79o6%2BiukbiCuY"
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {}
				@Throws(IOException::class)
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						if (!TextUtils.isEmpty(result)) {
							try {
								val obj = JSONObject(result)
								if (!obj.isNull("DS")) {
									val array = obj.getJSONArray("DS")
									if (array.length() > 0) {
										val itemObj = array.getJSONObject(0)
										if (!itemObj.isNull("TEM")) {
											tvTemp.text = itemObj.getString("TEM")
										}
										if (!itemObj.isNull("RHU")) {
											val humidity = itemObj.getString("RHU")
											tvFactInfo.text = tvFactInfo.text.toString() + "相对湿度 " + humidity + getString(R.string.unit_percent) + "\n"
										}
										if (!itemObj.isNull("WIND")) {
											val windDir = itemObj.getString("WIND").toFloat().toInt()
											val windForce = itemObj.getString("WINS").toFloat().toInt()
											var dir = getString(WeatherUtil.getWindDirection(windDir))
											if (TextUtils.isEmpty(dir)) {
												dir = "无持续风向"
											}
											tvFactInfo.text = tvFactInfo.text.toString() + dir +
													" " + WeatherUtil.getFactWindForce(windForce) + "\n"
										}
									}
								}
							} catch (e: JSONException) {
								e.printStackTrace()
							}
						}
					}
				}
			})
		}).start()
	}

	/**
	 * 初始化listview
	 */
	private fun initListView() {
		mAdapter = ShawnWeeklyForecastAdapter(this, weeklyList)
		listView.adapter = mAdapter
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.llBack -> finish()
			R.id.ivSwitcher -> {
				if (listView!!.visibility == View.VISIBLE) {
					ivSwitcher.setImageResource(R.drawable.shawn_icon_switch_trend)
					listView!!.visibility = View.GONE
					llContainer2.visibility = View.VISIBLE
				} else {
					ivSwitcher.setImageResource(R.drawable.shawn_icon_switch_list)
					listView!!.visibility = View.VISIBLE
					llContainer2.visibility = View.GONE
				}
			}
			R.id.ivShare -> {
				val bitmap: Bitmap
				if (listView!!.visibility == View.VISIBLE) {
					val bitmap1 = CommonUtil.captureScrollView(scrollView)
					val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.shawn_legend_share_portrait)
					bitmap = CommonUtil.mergeBitmap(this@ForecastActivity, bitmap1, bitmap2, false)
					CommonUtil.clearBitmap(bitmap1)
					CommonUtil.clearBitmap(bitmap2)
				} else {
					val bitmap1 = CommonUtil.captureView(clLocation)
					val bitmap2 = CommonUtil.captureView(llHourly)
					val bitmap3 = CommonUtil.mergeBitmap(this@ForecastActivity, bitmap1, bitmap2, false)
					CommonUtil.clearBitmap(bitmap1)
					CommonUtil.clearBitmap(bitmap2)
					val bitmap4 = CommonUtil.captureView(llContainer1)
					val bitmap5 = CommonUtil.mergeBitmap(this@ForecastActivity, bitmap3, bitmap4, false)
					CommonUtil.clearBitmap(bitmap3)
					CommonUtil.clearBitmap(bitmap4)
					val bitmap6 = CommonUtil.captureView(clWeekly)
					val bitmap7 = CommonUtil.mergeBitmap(this@ForecastActivity, bitmap5, bitmap6, false)
					CommonUtil.clearBitmap(bitmap5)
					CommonUtil.clearBitmap(bitmap6)
					val bitmap8 = CommonUtil.captureView(llContainer2)
					val bitmap9 = CommonUtil.mergeBitmap(this@ForecastActivity, bitmap7, bitmap8, false)
					CommonUtil.clearBitmap(bitmap7)
					CommonUtil.clearBitmap(bitmap8)
					val bitmap10 = BitmapFactory.decodeResource(resources, R.drawable.shawn_legend_share_portrait)
					bitmap = CommonUtil.mergeBitmap(this@ForecastActivity, bitmap9, bitmap10, false)
					CommonUtil.clearBitmap(bitmap9)
					CommonUtil.clearBitmap(bitmap10)
				}
				CommonUtil.share(this@ForecastActivity, bitmap)
			}
		}
	}

	//需要申请的所有权限
	var allPermissions = arrayOf(
			Manifest.permission.CALL_PHONE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	)

	//拒绝的权限集合
	var deniedList: MutableList<String> = ArrayList()

	/**
	 * 申请定位权限
	 */
	private fun checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			init()
		} else {
			deniedList.clear()
			for (permission in allPermissions) {
				if (ContextCompat.checkSelfPermission(this, permission) !== PackageManager.PERMISSION_GRANTED) {
					deniedList.add(permission)
				}
			}
			if (deniedList.isEmpty()) { //所有权限都授予
				init()
			} else {
				val permissions = deniedList.toTypedArray() //将list转成数组
				ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_LOCATION)
			}
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			AuthorityUtil.AUTHOR_LOCATION -> if (grantResults.isNotEmpty()) {
				var isAllGranted = true //是否全部授权
				for (gResult in grantResults) {
					if (gResult != PackageManager.PERMISSION_GRANTED) {
						isAllGranted = false
						break
					}
				}
				if (isAllGranted) { //所有权限都授予
					init()
				} else { //只要有一个没有授权，就提示进入设置界面设置
					AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用电话权限、存储权限，是否前往设置？")
				}
			} else {
				for (permission in permissions) {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission!!)) {
						AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用电话权限、存储权限，是否前往设置？")
						break
					}
				}
			}
		}
	}

}