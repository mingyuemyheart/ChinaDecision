package com.china.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.china.R
import com.china.adapter.ProductAdapter
import com.china.common.CONST
import com.china.common.ColumnData
import com.china.common.MyApplication
import com.china.dto.NewsDto
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_product.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 实况监测、天气预报、专业服务、灾情信息、天气会商
 */
class ProductActivity : BaseActivity(), OnClickListener {

	private var mAdapter : ProductAdapter? = null
	private val dataList : ArrayList<ColumnData> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_product)
		initWidget()
		initGridView()
	}

	private fun initWidget() {
		llBack.setOnClickListener(this)

		val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
		if (!TextUtils.isEmpty(title)) {
			tvTitle.text = title
		}

		dataList.clear()
		val dataUrl = intent.getStringExtra(CONST.WEB_URL)
		if (!TextUtils.isEmpty(dataUrl)) {
			showDialog()
			okHttpList(dataUrl)
		}else {
			val data : ColumnData = intent.extras.getParcelable("data")
			tvTitle.text = data.name
			val columnIds = MyApplication.getColumnIds(this)
			if (!TextUtils.isEmpty(columnIds)) {
				for (i in 0 until data.child.size) {
					val dto = data.child[i]
					if (columnIds.contains(dto.columnId)) {//已经有保存的栏目
						dataList.add(dto)
					}
				}
			}else {
				dataList.addAll(data.child)
			}
		}
	}
	
	private fun initGridView() {
		mAdapter = ProductAdapter(this, dataList)
		gridView.adapter = mAdapter
		gridView.setOnItemClickListener { parent, view, position, id ->
			val dto = dataList[position]
			var intent : Intent? = null
			if (TextUtils.equals(dto.showType, CONST.URL)) {//网页类
				intent = Intent(this, WebviewActivity::class.java)
				val data = NewsDto()
				data.title = dto.name
				data.detailUrl = dto.dataUrl
				data.imgUrl = dto.icon
				val bundle = Bundle()
				bundle.putParcelable("data", data)
				intent.putExtras(bundle)

				intent.putExtra(CONST.COLUMN_ID, dto.columnId)
				intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
				intent.putExtra(CONST.WEB_URL, dto.dataUrl)
				startActivity(intent)
			}else if (TextUtils.equals(dto.showType, CONST.PDF)) {
				intent = Intent(this, PDFActivity::class.java)
				intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
				intent.putExtra(CONST.WEB_URL, dto.dataUrl)
				startActivity(intent)
			}else if (TextUtils.equals(dto.showType, CONST.NEWS)) {
				intent = Intent(this, PdfTitleActivity::class.java)
				val list = ArrayList<ColumnData>()
				val cd = ColumnData()
				cd.columnId = dto.columnId
				cd.name = dto.name
				cd.dataUrl = dto.dataUrl
				list.add(cd)
				val bundle = Bundle()
				bundle.putParcelableArrayList("dataList", list)
				intent.putExtras(bundle)
				intent.putExtra(CONST.COLUMN_ID, dto.columnId)
				intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
				intent.putExtra(CONST.WEB_URL, dto.dataUrl)
				startActivity(intent)
			}else if (TextUtils.equals(dto.showType, CONST.PRODUCT)) {
				intent = Intent(this, ProductActivity::class.java)
				intent.putExtra(CONST.COLUMN_ID, dto.columnId)
				intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
				intent.putExtra(CONST.WEB_URL, dto.dataUrl)
				val bundle = Bundle()
				bundle.putParcelable("data", dto)
				intent.putExtras(bundle)
				startActivity(intent)
			}else if (TextUtils.equals(dto.showType, CONST.LOCAL)) {
				if (TextUtils.equals(dto.id, "101")) {//站点检测
					intent = Intent(this, FactActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "102")) {//中国大陆区域彩色云图
					intent = Intent(this, WebviewActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					intent.putExtra(CONST.WEB_URL, CONST.CLOUD_URL)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "103")) {//台风路径
					intent = Intent(this, TyhpoonActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "104")) {//天气统计
					intent = Intent(this, WeatherStaticsActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "105")) {//社会化观测
					intent = Intent(this, SocietyObserveActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "106")) {//空气污染
					intent = Intent(this, AirQualityActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "107")) {//视频会商
					intent = Intent(this, WeatherMeetingActivity::class.java)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "109")) {//天气图分析
					intent = Intent(this, WeatherChartActivity::class.java)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "110")) {//格点实况
					intent = Intent(this, PointFactActivity::class.java)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "111")) {//综合预报
					intent = Intent(this, ComForecastActivity::class.java)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "112")) {//强对流天气实况（新）
					intent = Intent(this, StreamFactActivity::class.java)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "113")) {//产品定制
					intent = Intent(this, ProductCustomActivity::class.java)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "114")) {//5天降水量统计
					intent = Intent(this, FiveRainActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				} else if (TextUtils.equals(dto.id, "115")) { //天气现象实况
					intent = Intent(this, WeatherFactActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "201")) {//城市天气预报
					intent = Intent(this, CityForecastActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				} else if (TextUtils.equals(dto.id, "205")) { //国外城市天气预报
					intent = Intent(this, NationForecastActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					intent.putExtra(CONST.WEB_URL, dto.dataUrl)
					startActivity(intent)
				} else if (TextUtils.equals(dto.id, "206")) { //环渤海天气预报
					intent = Intent(this, HbhForecastActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					intent.putExtra(CONST.WEB_URL, dto.dataUrl)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "202")) {//分钟级降水估测
					intent = Intent(this, MinuteFallActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "203")) {//等风来
					intent = Intent(this, WaitWindActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					intent.putExtra(CONST.WEB_URL, CONST.WAIT_WIND)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "204")) {//分钟降水与强对流
					intent = Intent(this, StrongStreamActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "207")) {//格点预报
					intent = Intent(this, PointForeActivity::class.java)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "301") || TextUtils.equals(dto.id, "6666")) {//灾情专报、逐日滚动监测
					intent = Intent(this, PdfTitleActivity::class.java)
					val list = ArrayList<ColumnData>()
					val cd = ColumnData()
					cd.columnId = dto.columnId
					cd.name = dto.name
					cd.dataUrl = dto.dataUrl
					list.add(cd)
					val bundle = Bundle()
					bundle.putParcelableArrayList("dataList", list)
					intent.putExtras(bundle)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					intent.putExtra(CONST.WEB_URL, dto.dataUrl)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "302")) {//灾情直报
					intent = Intent(this, DisasterReportActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				}else if (TextUtils.equals(dto.id, "601")) {//视频直播
					intent = Intent(this, WeatherMeetingActivity::class.java)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				} else if (TextUtils.equals(dto.id, "1205")) { //联播天气
					intent = Intent(this, BroadcastWeatherActivity::class.java)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					intent.putExtra(CONST.WEB_URL, dto.dataUrl)
					startActivity(intent)
				} else if (TextUtils.equals(dto.id, "1002")) { //预警信息
					intent = Intent(this, WarningActivity::class.java)
					intent.putExtra(CONST.COLUMN_ID, dto.columnId)
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
					startActivity(intent)
				} else if (TextUtils.equals(dto.id, "-1")) {
					Toast.makeText(this, "频道建设中", Toast.LENGTH_SHORT).show()
				}
			} else {
				if (!TextUtils.isEmpty(dto.dataUrl)) {
					if (dto.dataUrl.contains(".pdf") || dto.dataUrl.contains(".PDF")) {//pdf格式
						intent = Intent(this, PDFActivity::class.java)
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
						intent.putExtra(CONST.WEB_URL, dto.dataUrl)
						startActivity(intent)
					}else {//网页、图片
						intent = Intent(this, WebviewActivity::class.java)
						val data = NewsDto()
						data.title = dto.name
						data.detailUrl = dto.dataUrl
						data.imgUrl = dto.icon
						val bundle = Bundle()
						bundle.putParcelable("data", data)
						intent.putExtras(bundle)

						intent.putExtra(CONST.COLUMN_ID, dto.columnId)
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
						intent.putExtra(CONST.WEB_URL, dto.dataUrl)
						startActivity(intent)
					}
				}
			}
		}
	}

	private fun okHttpList(url : String) {
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
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
								val obj = JSONObject(result)
								if (!obj.isNull("l")) {
									val array = JSONArray(obj.getString("l"))
									for (i in 0 until array.length()) {
										val itemObj = array.getJSONObject(i)
										val dto = ColumnData()
										dto.name = itemObj.getString("l1")
										dto.dataUrl = itemObj.getString("l2")
										dto.icon = itemObj.getString("l4")
										dataList.add(dto)
									}
								}
								if (mAdapter != null) {
									mAdapter!!.notifyDataSetChanged()
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
			R.id.llBack -> finish()
		}
	}

}
