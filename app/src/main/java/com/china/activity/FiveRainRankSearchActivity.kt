package com.china.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.china.R
import com.china.dto.StationMonitorDto
import com.china.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_five_rain_rank_search.*
import kotlinx.android.synthetic.main.layout_title.*
import java.util.*

/**
 * 5天降水量统计-区域选择
 * @author shawn_sun
 */
class FiveRainRankSearchActivity : BaseActivity(), OnClickListener{

    private var provinceName = "全国"
    private var areaName = "全国"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_five_rain_rank_search)
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "选择区域"
        if (intent.hasExtra("provinceName")) {
            provinceName = intent.getStringExtra("provinceName")
        }
        if (intent.hasExtra("areaName")) {
            areaName = intent.getStringExtra("areaName")
        }
        setProvinceData()
    }

    /**
     * 设置区域及省份信息
     */
    private fun setProvinceData() {
        val dataList: MutableList<StationMonitorDto> = ArrayList()
        var dto = StationMonitorDto()
        dto.partition = "全国"
        dto.areaList.add("全国")
        dataList.add(dto)
        dto = StationMonitorDto()
        dto.partition = "华北"
        dto.areaList.add("北京")
        dto.areaList.add("天津")
        dto.areaList.add("河北")
        dto.areaList.add("山西")
        dto.areaList.add("内蒙古")
        dataList.add(dto)
        dto = StationMonitorDto()
        dto.partition = "华东"
        dto.areaList.add("上海")
        dto.areaList.add("山东")
        dto.areaList.add("江苏")
        dto.areaList.add("浙江")
        dto.areaList.add("江西")
        dto.areaList.add("安徽")
        dto.areaList.add("福建")
        dataList.add(dto)
        dto = StationMonitorDto()
        dto.partition = "华中"
        dto.areaList.add("湖北")
        dto.areaList.add("湖南")
        dto.areaList.add("河南")
        dataList.add(dto)
        dto = StationMonitorDto()
        dto.partition = "华南"
        dto.areaList.add("广东")
        dto.areaList.add("广西")
        dto.areaList.add("海南")
        dataList.add(dto)
        dto = StationMonitorDto()
        dto.partition = "东北"
        dto.areaList.add("黑龙江")
        dto.areaList.add("吉林")
        dto.areaList.add("辽宁")
        dataList.add(dto)
        dto = StationMonitorDto()
        dto.partition = "西北"
        dto.areaList.add("陕西")
        dto.areaList.add("甘肃")
        dto.areaList.add("宁夏")
        dto.areaList.add("新疆")
        dto.areaList.add("青海")
        dataList.add(dto)
        dto = StationMonitorDto()
        dto.partition = "西南"
        dto.areaList.add("重庆")
        dto.areaList.add("四川")
        dto.areaList.add("贵州")
        dto.areaList.add("云南")
        dto.areaList.add("西藏")
        dataList.add(dto)
        addProvinceView(dataList)
    }

    /**
     * 添加省份信息
     */
    private fun addProvinceView(dataList: List<StationMonitorDto>) {
        llContainer!!.removeAllViews()
        for (i in dataList.indices) {
            val dto = dataList[i]

            //整个区域
            val ll1 = LinearLayout(this)
            ll1.orientation = LinearLayout.HORIZONTAL
            ll1.gravity = Gravity.CENTER_VERTICAL

            //图片、区域名称
            val ll2 = LinearLayout(this)
            ll2.orientation = LinearLayout.VERTICAL
            ll2.gravity = Gravity.CENTER_HORIZONTAL
            ll2.setPadding(CommonUtil.dip2px(this, 10f).toInt(), CommonUtil.dip2px(this, 10f).toInt(), CommonUtil.dip2px(this, 10f).toInt(), CommonUtil.dip2px(this, 10f).toInt())
            //图片
            val ivMap = ImageView(this)
            if (TextUtils.equals(dto.partition, areaName)) {
                ivMap.setImageResource(setAreaImage(dto.partition, true))
            } else {
                ivMap.setImageResource(setAreaImage(dto.partition, false))
            }
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.width = CommonUtil.dip2px(this, 50f).toInt()
            params.height = CommonUtil.dip2px(this, 50f).toInt()
            ivMap.layoutParams = params
            ll2.addView(ivMap)
            //区域名称
            val tvArea = TextView(this)
            tvArea.gravity = Gravity.CENTER
            tvArea.setTextColor(Color.BLACK)
            tvArea.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            tvArea.text = dto.partition
            ll2.addView(tvArea)

            //间隔线
            val line = TextView(this)
            line.setBackgroundColor(resources.getColor(R.color.light_gray))
            line.width = CommonUtil.dip2px(this, 1f).toInt()
            line.height = CommonUtil.dip2px(this, 60f).toInt()

            //省份名称部分
            val ll3 = LinearLayout(this)
            ll3.orientation = LinearLayout.VERTICAL
            ll3.setPadding(CommonUtil.dip2px(this, 10f).toInt(), CommonUtil.dip2px(this, 10f).toInt(), CommonUtil.dip2px(this, 10f).toInt(), CommonUtil.dip2px(this, 10f).toInt())
            var rowCount: Int //4个一行
            rowCount = if (dto.areaList.size % 4 == 0) {
                dto.areaList.size / 4
            } else {
                dto.areaList.size / 4 + 1
            }
            for (j in 0 until rowCount) {
                val llItem = LinearLayout(this)
                llItem.orientation = LinearLayout.HORIZONTAL
                llItem.gravity = Gravity.CENTER_VERTICAL
                llItem.setPadding(0, CommonUtil.dip2px(this, 5f).toInt(), 0, CommonUtil.dip2px(this, 5f).toInt())
                var k: Int
                var size = j * 4 + 4
                if (size >= dto.areaList.size) {
                    size = dto.areaList.size
                }
                k = j * 4
                while (k < size) {

                    //省份名称
                    val proName = dto.areaList[k]
                    val tvPro = TextView(this)
                    tvPro.gravity = Gravity.CENTER
                    tvPro.setBackgroundResource(R.drawable.corner_unselected_pro)
                    tvPro.setPadding(CommonUtil.dip2px(this, 10f).toInt(), CommonUtil.dip2px(this, 5f).toInt(), CommonUtil.dip2px(this, 10f).toInt(), CommonUtil.dip2px(this, 5f).toInt())
                    tvPro.setTextColor(Color.BLACK)
                    tvPro.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
                    tvPro.text = proName
                    tvPro.tag = dto.partition + "," + proName
                    val p = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    p.width = (CommonUtil.widthPixels(this) - CommonUtil.dip2px(this, 90f)).toInt() / 4
                    tvPro.layoutParams = p
                    tvPro.setOnClickListener(MyOnClickListener())
                    llItem.addView(tvPro)
                    if (TextUtils.equals(proName, provinceName)) {
                        tvPro.setBackgroundResource(R.drawable.corner_selected_pro)
                        tvPro.setTextColor(Color.WHITE)
                    } else {
                        tvPro.setBackgroundResource(R.drawable.corner_unselected_pro)
                        tvPro.setTextColor(Color.BLACK)
                    }
                    k++
                }
                ll3.addView(llItem)
            }
            ll1.addView(ll2)
            ll1.addView(line)
            ll1.addView(ll3)
            llContainer!!.addView(ll1)
            val divider = TextView(this)
            divider.setBackgroundColor(resources.getColor(R.color.light_gray))
            divider.height = CommonUtil.dip2px(this, 5f).toInt()
            llContainer!!.addView(divider)
        }
    }

    /**
     * 设置区域image
     * @param area
     * @param isSelected
     * @return
     */
    private fun setAreaImage(area: String, isSelected: Boolean): Int {
        var drawabele = -1
        if (TextUtils.equals(area, "全国")) {
            drawabele = if (isSelected) {
                R.drawable.skjc_pic_qgs
            } else {
                R.drawable.skjc_pic_qg
            }
        } else if (TextUtils.equals(area, "华北")) {
            drawabele = if (isSelected) {
                R.drawable.skjc_pic_hbs
            } else {
                R.drawable.skjc_pic_hb
            }
        } else if (TextUtils.equals(area, "华东")) {
            drawabele = if (isSelected) {
                R.drawable.skjc_pic_hds
            } else {
                R.drawable.skjc_pic_hd
            }
        } else if (TextUtils.equals(area, "华中")) {
            drawabele = if (isSelected) {
                R.drawable.skjc_pic_hzs
            } else {
                R.drawable.skjc_pic_hz
            }
        } else if (TextUtils.equals(area, "华南")) {
            drawabele = if (isSelected) {
                R.drawable.skjc_pic_hns
            } else {
                R.drawable.skjc_pic_hn
            }
        } else if (TextUtils.equals(area, "东北")) {
            drawabele = if (isSelected) {
                R.drawable.skjc_pic_dbs
            } else {
                R.drawable.skjc_pic_db
            }
        } else if (TextUtils.equals(area, "西北")) {
            drawabele = if (isSelected) {
                R.drawable.skjc_pic_xbs
            } else {
                R.drawable.skjc_pic_xb
            }
        } else if (TextUtils.equals(area, "西南")) {
            drawabele = if (isSelected) {
                R.drawable.skjc_pic_xns
            } else {
                R.drawable.skjc_pic_xn
            }
        }
        return drawabele
    }

    /**
     * 点击省份监听
     */
    private inner class MyOnClickListener : OnClickListener {
        override fun onClick(v: View) {
            val tag = (v.tag as String).split(",").toTypedArray()
            var l = 0
            while (l < llContainer.childCount) {
                val ll1 = llContainer.getChildAt(l) as LinearLayout
                val ll2 = ll1.getChildAt(0) as LinearLayout
                val ivMap = ll2.getChildAt(0) as ImageView
                val tvArea = ll2.getChildAt(1) as TextView
                val areaName = tvArea.text.toString()
                if (TextUtils.equals(tag[0], areaName)) {
                    ivMap.setImageResource(setAreaImage(areaName, true))
                } else {
                    ivMap.setImageResource(setAreaImage(areaName, false))
                }
                val ll3 = ll1.getChildAt(2) as LinearLayout
                for (i in 0 until ll3.childCount) {
                    val llItem = ll3.getChildAt(i) as LinearLayout
                    for (j in 0 until llItem.childCount) {
                        val tvPro = llItem.getChildAt(j) as TextView
                        val proName = tvPro.text.toString()
                        if (TextUtils.equals(tag[1], proName)) {
                            tvPro.setBackgroundResource(R.drawable.corner_selected_pro)
                            tvPro.setTextColor(Color.WHITE)
                            val intent = Intent()
                            intent.putExtra("areaName", areaName)
                            intent.putExtra("provinceName", proName)
                            setResult(RESULT_OK, intent)
                            finish()
                        } else {
                            tvPro.setBackgroundResource(R.drawable.corner_unselected_pro)
                            tvPro.setTextColor(Color.BLACK)
                        }
                    }
                }
                l += 2
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
        }
    }
	
}
