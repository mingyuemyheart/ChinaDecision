package com.china.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.AbsListView
import com.china.R
import com.china.adapter.DecisionNewsAdapter
import com.china.common.CONST
import com.china.dto.DisasterDto
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_titlebar_listview.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 决策专报、灾情专报、一周灾情总结
 * @author shawn_sun
 */
class DecisionNewsActivity : BaseActivity(), OnClickListener {
	
	private var mAdapter : DecisionNewsAdapter? = null
	private val dataList : ArrayList<DisasterDto> = ArrayList()
	private var page = 1

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_titlebar_listview)
		initRefreshLayout()
		initWidget()
		initListView()
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private fun initRefreshLayout() {
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
		refreshLayout.setProgressViewEndTarget(true, 300)
		refreshLayout.isRefreshing = true
		refreshLayout.setOnRefreshListener {
			refresh()
		}
	}
	
	private fun refresh() {
		page = 1
		okHttpList()
	}
	
	private fun initWidget() {
		llBack.setOnClickListener(this)

		val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
		if (title != null) {
			tvTitle.text = title
		}
		refresh()
		CommonUtil.submitClickCount(intent.getStringExtra(CONST.COLUMN_ID), title)
	}
	
	private fun initListView() {
		mAdapter = DecisionNewsAdapter(this, dataList)
		listView.adapter = mAdapter
		listView.setOnItemClickListener { parent, view, position, id ->
			val dto = dataList[position]
			val intent = Intent(this, PDFActivity::class.java)
			intent.putExtra(CONST.ACTIVITY_NAME, dto.title)
			intent.putExtra(CONST.WEB_URL, dto.url)
			intent.putExtra(CONST.IMG_URL, dto.imgUrl)
			intent.putExtra(CONST.DATA_TIME, dto.time)
			startActivity(intent)
		}
		listView.setOnScrollListener(object : AbsListView.OnScrollListener {
			override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
			}
			override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view!!.lastVisiblePosition == view.count - 1) {
					page++
					okHttpList()
				}
			}
		})
	}
	
	/**
	 * 获取列表数据
	 */
	private fun okHttpList() {
		Thread(Runnable {
			var url = intent.getStringExtra(CONST.WEB_URL)
			if (!TextUtils.isEmpty(url) && url.contains("num=20")) {
				url = url.replace("num=20", "num=100")
			}
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						refreshLayout.isRefreshing = false
						if (!TextUtils.isEmpty(result)) {
							try {
								val obj = JSONObject(result)
								if (!obj.isNull("info")) {
									dataList.clear()
									val array = obj.getJSONArray("info")
									for (i in 0 until array.length()) {
										val dto = DisasterDto()
										val itemObj = array.getJSONObject(i)
										if (!itemObj.isNull("url")) {
											dto.url = itemObj.getString("url")
										}
										if (!itemObj.isNull("time")) {
											dto.time = itemObj.getString("time")
										}
										if (!itemObj.isNull("title")) {
											dto.title = itemObj.getString("title")
										}
										if (!itemObj.isNull("image")) {
											dto.imgUrl = itemObj.getString("image")
										}
										dataList.add(dto)
									}
									if (mAdapter != null) {
										mAdapter!!.notifyDataSetChanged()
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
			R.id.llBack -> finish()
		}
	}

}
