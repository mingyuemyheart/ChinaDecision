package com.china.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import com.china.R
import com.china.adapter.NewsAdapter
import com.china.common.CONST
import com.china.dto.NewsDto
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_titlebar_listview.*
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
 * 天气资讯
 */
class WeatherInfoActivity : BaseActivity(), OnClickListener {

    private var mAdapter: NewsAdapter? = null
    private val dataList: MutableList<NewsDto> = ArrayList()
    private var dataUrl: String? = null
    private var showType: String? = null

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
        refreshLayout.setOnRefreshListener { refresh() }
    }

    private fun refresh() {
        dataList.clear()
        dataUrl = intent.getStringExtra(CONST.WEB_URL)
        okHttpList(dataUrl)
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        refresh()
        val columnId = intent.getStringExtra(CONST.COLUMN_ID)
        CommonUtil.submitClickCount(columnId, title)
    }

    /**
     * 初始化listview
     */
    private fun initListView() {
        mAdapter = NewsAdapter(this, dataList)
        listView.adapter = mAdapter
        listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = dataList[arg2]
            val intent: Intent
            if (TextUtils.equals(showType, CONST.PDF)) {
                intent = Intent(this, PDFActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, dto.title)
                intent.putExtra(CONST.WEB_URL, dto.detailUrl)
                startActivity(intent)
            } else {
                intent = Intent(this, Webview2Activity::class.java)
                intent.putExtra("data", dto)
                intent.putExtra(CONST.ACTIVITY_NAME, dto.title)
                intent.putExtra(CONST.WEB_URL, dto.detailUrl)
                startActivity(intent)
            }
        }
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.lastVisiblePosition == view.count - 1) {
                    okHttpList(dataUrl)
                }
            }
            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {}
        })
    }

    private fun okHttpList(url: String?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url!!).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        refreshLayout!!.isRefreshing = false
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("l")) {
                                    val array = JSONArray(obj.getString("l"))
                                    for (i in 0 until array.length()) {
                                        val itemObj = array.getJSONObject(i)
                                        val dto = NewsDto()
                                        dto.title = itemObj.getString("l1")
                                        dto.detailUrl = itemObj.getString("l2")
                                        dto.time = itemObj.getString("l3")
                                        dto.imgUrl = itemObj.getString("l4")
                                        if (!itemObj.isNull("l5")) {
                                            dto.flagUrl = itemObj.getString("l5")
                                        }
                                        dataList.add(dto)
                                    }
                                }
                                if (!obj.isNull("prev")) {
                                    dataUrl = obj.getString("prev")
                                }
                                if (!obj.isNull("type")) {
                                    showType = obj.getString("type")
                                }
                                if (mAdapter != null) {
                                    mAdapter!!.notifyDataSetChanged()
                                }
                            } catch (e1: JSONException) {
                                e1.printStackTrace()
                            }
                        }
                    }
                }
            })
        }).start()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
        }
    }

}
