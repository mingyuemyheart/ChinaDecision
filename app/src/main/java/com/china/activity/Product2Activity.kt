package com.china.activity;

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.china.R
import com.china.adapter.ShawnProductAdapter
import com.china.common.CONST
import com.china.common.ColumnData
import com.china.common.MyApplication
import com.china.dto.NewsDto
import com.china.utils.CommonUtil
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
 * 农业气象等
 */
class Product2Activity : BaseActivity(), OnClickListener {
	
	private var mAdapter : ShawnProductAdapter? = null
	private val dataList : ArrayList<ColumnData> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_product)
		initWidget()
		initListView()
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
		val columnId = intent.getStringExtra(CONST.COLUMN_ID)
		CommonUtil.submitClickCount(columnId, title)
	}
	
	/**
	 * 初始化listview
	 */
	private fun initListView() {
		mAdapter = ShawnProductAdapter(this, dataList)
		gridView.adapter = mAdapter
		gridView.setOnItemClickListener { parent, view, position, id ->
			val dto = dataList[position]
			var intent : Intent? = null
			if (TextUtils.equals(dto.showType, CONST.NEWS)) {//天气资讯
				intent = Intent(this, WeatherInfoActivity::class.java)
				intent.putExtra(CONST.COLUMN_ID, dto.columnId)
				intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
				intent.putExtra(CONST.WEB_URL, dto.dataUrl)
				startActivity(intent)
			}else {
				if (!TextUtils.isEmpty(dto.dataUrl)) {
					if (dto.dataUrl.contains(".pdf") || dto.dataUrl.contains(".PDF")) {//pdf格式
						intent = Intent(this, PDFActivity::class.java)
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
						intent.putExtra(CONST.WEB_URL, dto.dataUrl)
						startActivity(intent)
					}else {//网页、图片
						intent = Intent(this, Webview2Activity::class.java)
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
