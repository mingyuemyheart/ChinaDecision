package com.china.activity;

import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode
import com.amap.api.location.AMapLocationListener
import com.china.R
import com.china.common.CONST
import com.china.common.ColumnData
import com.china.common.MyApplication
import com.china.dto.NewsDto
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 登录界面
 */
class LoginActivity : BaseActivity(), OnClickListener {
	
	private var lat  = "0"
	private var lng = "0"
	private var addr = ""
	private val dataList : ArrayList<ColumnData> = ArrayList()
	private val pdfList : ArrayList<NewsDto> = ArrayList()//pdf文档类

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)
		initWidget()
	}

	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		tvLogin.setOnClickListener(this)
		tvForgetPwd.paint.flags = Paint.UNDERLINE_TEXT_FLAG
		tvCommonLogin.setOnClickListener(this)
		tvCommonLogin.paint.flags = Paint.UNDERLINE_TEXT_FLAG
		tvCommonLogin.paint.isAntiAlias = true

		startLocation()
	}

	/**
	 * 开始定位
	 */
	private fun startLocation() {
		val mLocationOption = AMapLocationClientOption()//初始化定位参数
		val mLocationClient = AMapLocationClient(this)//初始化定位
        mLocationOption.locationMode = AMapLocationMode.Hight_Accuracy//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.isNeedAddress = true//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.isOnceLocation = true//设置是否只定位一次,默认为false
        mLocationOption.isMockEnable = false//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.interval = 2000//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption)//给定位客户端对象设置定位参数
		mLocationClient.setLocationListener {
			AMapLocationListener { aMapLocation ->
				if (aMapLocation != null && aMapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
					lat = aMapLocation.latitude.toString()
					lng = aMapLocation.longitude.toString()
					addr = aMapLocation.address
				}
			}
		}
        mLocationClient.startLocation()//启动定位
	}

	private fun doLogin() {
		if (checkInfo()) {
			okHttpLogin()
		}
	}
	
	/**
	 * 验证用户信息
	 */
	private fun checkInfo() : Boolean {
		if (TextUtils.isEmpty(etUserName.text.toString())) {
			Toast.makeText(this, getString(R.string.input_username), Toast.LENGTH_SHORT).show()
			return false
		}
		if (TextUtils.isEmpty(etPwd.text.toString())) {
			Toast.makeText(this, getString(R.string.input_password), Toast.LENGTH_SHORT).show()
			return false
		}
		return true
	}
	
	/**
	 * 登录
	 */
	private fun okHttpLogin() {
		showDialog()
		val url = "http://decision-admin.tianqi.cn/home/Work/login"
		val builder = FormBody.Builder()
		builder.add("username", etUserName.text.toString())
		builder.add("password", etPwd.text.toString())
		builder.add("appid", CONST.APPID)
		builder.add("device_id", CommonUtil.getUniqueId(this))
		builder.add("platform", "android")
		builder.add("os_version", android.os.Build.VERSION.RELEASE)
		builder.add("software_version", CommonUtil.getVersion(this))
		builder.add("mobile_type", android.os.Build.MODEL)
		builder.add("address", addr)
		builder.add("lat", lat)
		builder.add("lng", lng)
		val body = builder.build()
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						cancelDialog()
						if (!TextUtils.isEmpty(result)) {
							try {
								val obje = JSONObject(result)
								if (!obje.isNull("status")) {
									val status  = obje.getInt("status")
									if (status == 1) {//成功
										val array = obje.getJSONArray("column")
										dataList.clear()
										for (i in 0 until array.length()) {
											val obj = array.getJSONObject(i)
											val data = ColumnData()
											if (!obj.isNull("id")) {
												data.columnId = obj.getString("id")
											}
											if (!obj.isNull("localviewid")) {
												data.id = obj.getString("localviewid")
											}
											if (!obj.isNull("name")) {
												data.name = obj.getString("name")
											}
											if (!obj.isNull("icon")) {
												data.icon = obj.getString("icon")
											}
											if (!obj.isNull("desc")) {
												data.desc = obj.getString("desc")
											}
											if (!obj.isNull("showtype")) {
												data.showType = obj.getString("showtype")
											}
											if (!obj.isNull("dataurl")) {
												data.dataUrl = obj.getString("dataurl")
											}
											if (!obj.isNull("child")) {
												val childArray = obj.getJSONArray("child")
												for (j in 0 until childArray.length()) {
													val childObj = childArray.getJSONObject(j)
													val dto = ColumnData()
													dto.groupColumnId = data.columnId
													if (!childObj.isNull("id")) {
														dto.columnId = childObj.getString("id")
													}
													if (!childObj.isNull("localviewid")) {
														dto.id = childObj.getString("localviewid")
													}
													if (!childObj.isNull("name")) {
														dto.name = childObj.getString("name")
													}
													if (!childObj.isNull("desc")) {
														dto.desc = childObj.getString("desc")
													}
													if (!childObj.isNull("icon")) {
														dto.icon = childObj.getString("icon")
													}
													if (!childObj.isNull("showtype")) {
														dto.showType = childObj.getString("showtype")
													}
													if (!childObj.isNull("dataurl")) {
														dto.dataUrl = childObj.getString("dataurl")
													}
													if (!childObj.isNull("child")) {
														val childArray2 = childObj.getJSONArray("child")
														for (m in 0 until childArray2.length()) {
															val childObj2 = childArray2.getJSONObject(m)
															val d = ColumnData()
															if (!childObj2.isNull("id")) {
																d.columnId = childObj2.getString("id")
															}
															if (!childObj2.isNull("localviewid")) {
																d.id = childObj2.getString("localviewid")
															}
															if (!childObj2.isNull("name")) {
																d.name = childObj2.getString("name")
															}
															if (!childObj2.isNull("desc")) {
																d.desc = childObj2.getString("desc")
															}
															if (!childObj2.isNull("icon")) {
																d.icon = childObj2.getString("icon")
															}
															if (!childObj2.isNull("showtype")) {
																d.showType = childObj2.getString("showtype")
															}
															if (!childObj2.isNull("dataurl")) {
																d.dataUrl = childObj2.getString("dataurl")
															}
															dto.child.add(d)
														}
													}
													data.child.add(dto)
												}
											}
											dataList.add(data)
										}

										if (!obje.isNull("appinfo")) {
											val obj = obje.getJSONObject("appinfo")
											if (!obj.isNull("counturl")) {
												CONST.COUNTURL = obj.getString("counturl")
											}
											if (!obj.isNull("recommendurl")) {
												CONST.RECOMMENDURL = obj.getString("recommendurl")
											}
											if (!obj.isNull("news")) {
												pdfList.clear()
												val newsArray = obj.getJSONArray("news")
												for (i in 0 until newsArray.length()) {
													val itemObj = newsArray.getJSONObject(i)
													val dto = NewsDto()
													if (!itemObj.isNull("header")) {
														dto.header = "【"+itemObj.getString("header")+"】"
													}
													if (!itemObj.isNull("name")) {
														dto.title = dto.header+itemObj.getString("name")
													}
													if (!itemObj.isNull("url")) {
														dto.detailUrl = itemObj.getString("url")
													}
													if (!itemObj.isNull("time")) {
														dto.time = itemObj.getString("time")
													}
													if (!itemObj.isNull("flagImg")) {
														dto.imgUrl = itemObj.getString("flagImg")
													}
													pdfList.add(dto)
												}
											}
										}

										if (!obje.isNull("info")) {
											val obj = JSONObject(obje.getString("info"))
											MyApplication.UID = obj.getString("id")
											MyApplication.USERGROUP = obj.getString("usergroup")
											MyApplication.USERNAME = etUserName.text.toString()
											MyApplication.PASSWORD = etPwd.text.toString()
											MyApplication.saveUserInfo(this@LoginActivity)

											okHttpPushToken()

											val intent = Intent(this@LoginActivity, MainActivity::class.java)
											val bundle = Bundle()
											bundle.putParcelableArrayList("dataList", dataList as ArrayList<out Parcelable>)
											bundle.putParcelableArrayList("pdfList", pdfList as ArrayList<out Parcelable>)
											intent.putExtras(bundle)
											startActivity(intent)
											finish()
										}
									}else {
										//失败
										if (!obje.isNull("msg")) {
											val msg = obje.getString("msg")
											if (msg != null) {
												Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
											}
										}
									}
								}
							} catch (e : JSONException) {
								e.printStackTrace()
							}
						}
					}
				}
			})
		}).start()
	}

	override fun onClick(v: View?) {
		when(v!!.id) {
			R.id.tvLogin -> doLogin()
			R.id.tvCommonLogin -> {
				etUserName.setText("中国气象")
				etPwd.setText("121")
				etUserName.setSelection(etUserName.text.toString().length)
				etPwd.setSelection(etPwd.text.toString().length)
				doLogin()
			}
		}
	}

	private fun okHttpPushToken() {
		if (TextUtils.equals(MyApplication.USERGROUP, "17")) {//公众用户不推送
			return
		}
		val url = "http://decision-admin.tianqi.cn/Home/work2019/savePushToken_zgqx"
		val builder = FormBody.Builder()
		val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
		val serial = Build.SERIAL
		builder.add("uuid", androidId+serial)
		builder.add("uid", MyApplication.UID)
		builder.add("groupid", MyApplication.USERGROUP)
		builder.add("pushtoken", MyApplication.DEVICETOKEN)
		builder.add("platform", "android")
		builder.add("um_key", MyApplication.appKey)
		val body = builder.build()
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url).post(body).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					Log.e("result", result)
				}
			})
		}).start()
	}

}
