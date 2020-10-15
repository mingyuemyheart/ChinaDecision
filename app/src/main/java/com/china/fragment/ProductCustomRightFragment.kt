package com.china.fragment;

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.china.R
import com.china.adapter.ProductOrderAdapter
import com.china.common.MyApplication
import com.china.dto.DisasterDto
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_product_custom_right.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 产品定制-提供资料
 */
class ProductCustomRightFragment : Fragment(), View.OnClickListener {

    private var timeDesc = false
    private var nameDesc = false
    private var mAdapter: ProductOrderAdapter? = null
    private val dataList: MutableList<DisasterDto> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product_custom_right, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
        initListView()
    }

    private fun initWidget() {
        ll1.setOnClickListener(this)
        ll2.setOnClickListener(this)
    }

    private fun initListView() {
        mAdapter = ProductOrderAdapter(activity, dataList)
        listView.adapter = mAdapter
        okHttpList()
    }

    private fun okHttpList() {
        Thread(Runnable {
            val url = "https://decision-admin.tianqi.cn/Home/work2019/decision_get_demand_doc?uid=${MyApplication.UID}"
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
                                        dataList.add(dto)
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ll1 -> {
                timeDesc = !timeDesc
                if (timeDesc) {
                    iv1!!.setImageResource(R.drawable.shawn_icon_rank_top)
                    dataList.sortWith(Comparator { o1, o2 -> o1.time.compareTo(o2.time) })
                } else {
                    iv1!!.setImageResource(R.drawable.shawn_icon_rank_bottom)
                    dataList.sortWith(Comparator { o1, o2 -> o2.time.compareTo(o1.time) })
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
            R.id.ll2 -> {
                nameDesc = !nameDesc
                if (nameDesc) {
                    iv2.setImageResource(R.drawable.shawn_icon_triangle_bottom)
                    dataList.sortWith(Comparator { o1, o2 -> o1.title.compareTo(o2.title) })
                } else {
                    iv2.setImageResource(R.drawable.shawn_icon_triangle_top)
                    dataList.sortWith(Comparator { o1, o2 -> o2.title.compareTo(o1.title) })
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

}
