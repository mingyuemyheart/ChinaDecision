package com.china.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import com.china.R
import com.china.adapter.FactRankAdapter
import com.china.common.CONST
import com.china.dto.StationMonitorDto
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_five_rain_rank.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 5天降水量统计
 */
class FiveRainRankActivity : BaseActivity(), OnClickListener{

    private var startTime: String? = null
    private var endTime: String? = null
    private var provinceName = ""
    private var areaName: String? = "全国"
    private var mAdapter: FactRankAdapter? = null
    private val dataList: MutableList<StationMonitorDto> = ArrayList()
    private val sdf3 = SimpleDateFormat("yyyyMMddHH", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyy年MM月dd日HH时", Locale.CHINA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_five_rain_rank)
        initWidget()
        initListView()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "五天降水量排行"
        ivMapSearch.setOnClickListener(this)
        ivMapSearch.visibility = View.VISIBLE
        ivShare.setOnClickListener(this)
        ivShare.visibility = View.VISIBLE
        endTime = sdf3.format(Date())
        startTime = sdf3.format(Date().time - 1000 * 60 * 60 * 24 * 5)
        tvArea.text = "全国"
        okHttpList()
    }

    private fun initListView() {
        mAdapter = FactRankAdapter(this, dataList)
        listView.adapter = mAdapter
        listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = dataList[arg2]
            val intent = Intent(this, FactRankDetailActivity::class.java)
            intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
            intent.putExtra("stationId", dto.stationId)
            intent.putExtra("interface", "newOneDay")
            startActivity(intent)
        }
    }

    private fun okHttpList() {
        showDialog()
        val url = String.format("http://data-66.cxwldata.cn/other/fivedayrain?starttime=%s&endtime=%s&province=%s&map=all&num=30", startTime, endTime, provinceName)
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
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
                                if (!obj.isNull("rainfallmax")) {
                                    dataList.clear()
                                    val itemArray = obj.getJSONArray("rainfallmax")
                                    var length = itemArray.length()
                                    if (length > 30) {
                                        length = 30
                                    }
                                    for (i in 0 until length) {
                                        val itemObj = itemArray.getJSONObject(i)
                                        val dto = StationMonitorDto()
                                        dto.provinceName = itemObj.getString("province")
                                        dto.name = itemObj.getString("city")
                                        dto.precipitation1h = itemObj.getString("rainfall")
                                        dto.value = dto.precipitation1h + getString(R.string.unit_mm)
                                        dto.stationId = itemObj.getString("stationid")
                                        dataList.add(dto)
                                    }
                                }
                                if (!obj.isNull("starttime")) {
                                    startTime = obj.getString("starttime")
                                }
                                if (!obj.isNull("endtime")) {
                                    endTime = obj.getString("endtime")
                                }
                                try {
                                    tvTime.text = sdf4.format(sdf3.parse(startTime)) + " - " + sdf4.format(sdf3.parse(endTime))
                                } catch (e: ParseException) {
                                    e.printStackTrace()
                                }
                                if (mAdapter != null) {
                                    mAdapter!!.notifyDataSetChanged()
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
            R.id.llBack -> finish()
            R.id.ivMapSearch -> {
                val intent = Intent(this, FiveRainRankSearchActivity::class.java)
                if (TextUtils.equals(provinceName, "")) {
                    intent.putExtra("provinceName", "全国")
                } else {
                    intent.putExtra("provinceName", provinceName)
                }
                intent.putExtra("areaName", areaName)
                startActivityForResult(intent, 1001)
            }
            R.id.ivShare -> {
                val bitmap1 = CommonUtil.captureView(llPrompt)
                val bitmap2 = CommonUtil.captureView(listView)
                val bitmap3 = CommonUtil.mergeBitmap(this@FiveRainRankActivity, bitmap1, bitmap2, false)
                CommonUtil.clearBitmap(bitmap1)
                CommonUtil.clearBitmap(bitmap2)
                val bitmap4 = BitmapFactory.decodeResource(resources, R.drawable.legend_share_portrait)
                val bitmap = CommonUtil.mergeBitmap(this@FiveRainRankActivity, bitmap3, bitmap4, false)
                CommonUtil.clearBitmap(bitmap3)
                CommonUtil.clearBitmap(bitmap4)
                CommonUtil.share(this@FiveRainRankActivity, bitmap)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1001 -> {
                    val bundle = data.extras
                    provinceName = bundle.getString("provinceName")
                    areaName = bundle.getString("areaName")
                    tvArea.text = provinceName
                    if (TextUtils.equals(provinceName, "全国")) {
                        provinceName = ""
                        areaName = "全国"
                    }
                    okHttpList()
                }
            }
        }
    }

}
