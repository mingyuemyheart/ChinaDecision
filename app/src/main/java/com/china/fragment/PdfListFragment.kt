package com.china.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import com.china.R
import com.china.activity.PDFActivity
import com.china.activity.Webview2Activity
import com.china.adapter.NewsAdapter
import com.china.common.CONST
import com.china.dto.NewsDto
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_pdf_list.*
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
 * pdf文档列表
 * @author shawn_sun
 */
class PdfListFragment : Fragment() {

    private var mAdapter: NewsAdapter? = null
    private val dataList: MutableList<NewsDto> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pdf_list, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        okHttpList()
    }

    private fun initWidget() {
        refresh()
    }

    /**
     * 初始化listview
     */
    private fun initListView() {
        mAdapter = NewsAdapter(activity, dataList)
        listView.adapter = mAdapter
        listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = dataList[arg2]
            val intent: Intent
            if (TextUtils.equals(dto.showType, CONST.PDF)) {
                intent = Intent(activity, PDFActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, dto.title)
                intent.putExtra(CONST.WEB_URL, dto.detailUrl)
                startActivity(intent)
            } else {
                intent = Intent(activity, Webview2Activity::class.java)
                intent.putExtra("data", dto)
                intent.putExtra(CONST.ACTIVITY_NAME, dto.title)
                intent.putExtra(CONST.WEB_URL, dto.detailUrl)
                startActivity(intent)
            }
        }
    }

    private fun okHttpList() {
        var url = arguments!!.getString(CONST.WEB_URL)
        if (TextUtils.isEmpty(url)) {
            refreshLayout!!.isRefreshing = false
            return
        }
        if (url.contains("newGetDecistionZXZB")) {
            url = url.replace("newGetDecistionZXZB", "newGetDecistionZXZB/new/1")
        }
        if (url.contains("num=20")) {
            url = url.replace("num=20", "num=100")
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
                    activity!!.runOnUiThread {
                        refreshLayout!!.isRefreshing = false
                        dataList.clear()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("l")) {
                                    val array = JSONArray(obj.getString("l"))
                                    for (i in 0 until array.length()) {
                                        val itemObj = array.getJSONObject(i)
                                        val dto = NewsDto()
                                        if (!itemObj.isNull("l1")) {
                                            dto.title = itemObj.getString("l1")
                                        }
                                        if (!itemObj.isNull("l2")) {
                                            dto.detailUrl = itemObj.getString("l2")
                                            if (dto.detailUrl.endsWith(".pdf") || dto.detailUrl.endsWith(".PDF")) {
                                                dto.showType = CONST.PDF
                                            }
                                        }
                                        if (!itemObj.isNull("l3")) {
                                            dto.time = itemObj.getString("l3")
                                        }
                                        if (!itemObj.isNull("l4")) {
                                            dto.imgUrl = itemObj.getString("l4")
                                        }
                                        if (!itemObj.isNull("l5")) {
                                            dto.flagUrl = itemObj.getString("l5")
                                        }
                                        dataList.add(dto)
                                    }
                                }
                                if (!obj.isNull("info")) {
                                    val array = obj.getJSONArray("info")
                                    for (i in 0 until array.length()) {
                                        val dto = NewsDto()
                                        val itemObj = array.getJSONObject(i)
                                        if (!itemObj.isNull("url")) {
                                            dto.detailUrl = itemObj.getString("url")
                                            if (dto.detailUrl.endsWith(".pdf") || dto.detailUrl.endsWith(".PDF")) {
                                                dto.showType = CONST.PDF
                                            }
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
                                }
                                if (dataList.size <= 0) {
                                    tvPrompt.visibility = View.VISIBLE
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

}
