package com.china.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.china.R
import com.china.adapter.ProductOrderAdapter
import com.china.common.MyApplication
import com.china.dto.DisasterDto
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_product_custom_left.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 产品定制-需求单
 */
class ProductCustomLeftFragment : Fragment() {

    private var mAdapter: ProductOrderAdapter? = null
    private val dataList: MutableList<DisasterDto> = ArrayList()
    private val dataList1: MutableList<DisasterDto> = ArrayList()
    private val dataList2: MutableList<DisasterDto> = ArrayList()
    private val dataList3: MutableList<DisasterDto> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product_custom_left, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
        initListView()
    }

    private fun initWidget() {
        llContainer.removeAllViews()
        llContainer1.removeAllViews()
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = 1f
        val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params1.weight = 1f
        params1.setMargins(CommonUtil.dip2px(activity, 20f).toInt(), 0, CommonUtil.dip2px(activity, 20f).toInt(), 0)
        for (i in 0..2) {
            val tvName = TextView(activity)
            val name = when (i) {
                0 -> {
                    "全部"
                }
                1 -> {
                    "待审批"
                }
                else -> {
                    "已审批"
                }
            }
            tvName.text = name
            tvName.setPadding(0, 20, 0, 20)
            tvName.gravity = Gravity.CENTER
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            tvName.setTextColor(resources.getColor(R.color.text_color3))
            tvName.layoutParams = params
            tvName.tag = name
            llContainer.addView(tvName)
            val tvBar = TextView(activity)
            tvBar.gravity = Gravity.CENTER
            tvBar.layoutParams = params1
            if (i == 0) {
                tvBar.setBackgroundColor(resources.getColor(R.color.blue))
            } else {
                tvBar.setBackgroundColor(Color.TRANSPARENT)
            }
            llContainer1.addView(tvBar)
            tvName.setOnClickListener { v ->
                val name = v.tag.toString() + ""
                for (j in 0 until llContainer.childCount) {
                    val tvName = llContainer.getChildAt(j) as TextView
                    val tvBar = llContainer1.getChildAt(j) as TextView
                    if (TextUtils.equals(name, tvName.tag.toString() + "")) {
                        tvBar.setBackgroundColor(resources.getColor(R.color.blue))
                    } else {
                        tvBar.setBackgroundColor(Color.TRANSPARENT)
                    }
                }
                dataList.clear()
                when {
                    TextUtils.equals(name, "待审批") -> { //待审批
                        dataList.addAll(dataList2)
                    }
                    TextUtils.equals(name, "已审批") -> { //已审批
                        dataList.addAll(dataList3)
                    }
                    else -> { //全部
                        dataList.addAll(dataList1)
                    }
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    private fun initListView() {
        mAdapter = ProductOrderAdapter(activity, dataList)
        listView.adapter = mAdapter
        okHttpList()
    }

    fun okHttpList() {
        Thread(Runnable {
            val url = "https://decision-admin.tianqi.cn/Home/work2019/decision_get_demand?uid=" + MyApplication.UID
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        dataList.clear()
                        dataList1.clear()
                        dataList2.clear()
                        dataList3.clear()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("data")) {
                                    val array = obj.getJSONArray("data")
                                    for (i in 0 until array.length()) {
                                        val itemObj = array.getJSONObject(i)
                                        val dto = DisasterDto()
                                        if (!itemObj.isNull("department")) {
                                            dto.title = itemObj.getString("department")
                                        }
                                        if (!itemObj.isNull("usefor")) {
                                            dto.content = itemObj.getString("usefor")
                                        }
                                        if (!itemObj.isNull("add_time")) {
                                            dto.time = itemObj.getString("add_time")
                                        }
                                        if (!itemObj.isNull("status")) {
                                            dto.status = itemObj.getString("status")
                                        }
                                        dataList.add(dto)
                                        dataList1.add(dto)
                                        if (TextUtils.equals(dto.status, "0")) { //待审批
                                            dataList2.add(dto)
                                        } else { //已审批
                                            dataList3.add(dto)
                                        }
                                    }
                                    if (mAdapter != null) {
                                        mAdapter!!.notifyDataSetChanged()
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

}
