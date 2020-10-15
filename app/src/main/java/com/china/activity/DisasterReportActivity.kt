package com.china.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import com.china.R
import com.china.adapter.DisasterReportAdapter
import com.china.common.CONST
import com.china.dto.DisasterReportDto
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
 * 灾情直报
 * @author shawn_sun
 */
class DisasterReportActivity : BaseActivity(), OnClickListener {

    private var mAdapter: DisasterReportAdapter? = null
    private val dataList: MutableList<DisasterReportDto> = ArrayList()

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
        refreshLayout.setOnRefreshListener { okHttpDisaster() }
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (title != null) {
            tvTitle!!.text = title
        }

        okHttpDisaster()

        val columnId = intent.getStringExtra(CONST.COLUMN_ID)
        CommonUtil.submitClickCount(columnId, title)
    }

    private fun initListView() {
        mAdapter = DisasterReportAdapter(this, dataList)
        listView.adapter = mAdapter
        listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = dataList[arg2]
            val intent = Intent(this, DisasterReportDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("data", dto)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    /**
     * 获取灾情直报
     */
    private fun okHttpDisaster() {
        Thread(Runnable {
            val url = "http://decision-admin.tianqi.cn/infomes/data/chinaweather/zqzb.html"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
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
                                if (!obj.isNull("response")) {
                                    dataList.clear()
                                    val `object` = obj.getJSONObject("response")
                                    if (!`object`.isNull("directList")) {
                                        val array = `object`.getJSONArray("directList")
                                        for (i in 0 until array.length()) {
                                            val itemObj = array.getJSONObject(i)
                                            val dto = DisasterReportDto()
                                            dto.vSendername = itemObj.getString("vSendername")
                                            dto.vCategory = itemObj.getString("vCategory")
                                            dto.vEdittime = itemObj.getString("vEdittime")
                                            dto.vGeneralLoss = itemObj.getString("vGeneralLoss")
                                            dto.vRzDpop = itemObj.getString("vRzDpop")
                                            dto.vEditor = itemObj.getString("vEditor")
                                            dto.vTaPhone = itemObj.getString("vTaPhone")
                                            dto.vSummary = itemObj.getString("vSummary")
                                            dto.vInfluenceDiscri = itemObj.getString("vInfluenceDiscri")
                                            dto.vStartTime = itemObj.getString("vStartTime")
                                            dto.vEndTime = itemObj.getString("vEndTime")
                                            dto.dRecordId = itemObj.getString("dRecordId")
                                            dataList.add(dto)
                                        }
                                        if (dataList.size > 0) {
                                            dataList.sortWith(Comparator { arg0, arg1 -> arg1.vEdittime.compareTo(arg0.vEdittime) })
                                            if (mAdapter != null) {
                                                mAdapter!!.notifyDataSetChanged()
                                            }
                                        }
                                    }
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
