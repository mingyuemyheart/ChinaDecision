package com.china.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.china.R
import com.china.dto.DisasterReportDto
import com.china.utils.WeatherUtil
import kotlinx.android.synthetic.main.activity_disaster_report_detail.*
import kotlinx.android.synthetic.main.layout_title.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 灾情直报-详情
 * @author shawn_sun
 */
class DisasterReportDetailActivity : BaseActivity(), OnClickListener {
	
	private val sdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
	private val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_disaster_report_detail)
		initWidget()
	}
	
	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvTitle.text = "灾情详情"

		val data : DisasterReportDto = intent.extras.getParcelable("data")
		try {
			vSendername.text = data.vSendername
			vEdittime.text = "上报时间："+sdf2.format(sdf1.parse(data.vEdittime))+"\n上报人："+data.vEditor+"    电话："+data.vTaPhone
			vCategory.text = getString(WeatherUtil.getDisasterClass(data.vCategory.toInt()))
			if (!TextUtils.isEmpty(data.vGeneralLoss)) {
				vGeneralLoss.text = data.vGeneralLoss+"万元"
			}else {
				vGeneralLoss.text = "--"
			}
			if (!TextUtils.isEmpty(data.vRzDpop)) {
				vRzDpop.text = data.vRzDpop+"人"
			}else {
				vRzDpop.text = "--"
			}
			if (!TextUtils.isEmpty(data.vSummary)) {
				vSummary.text = data.vSummary
			}else {
				vSummary.text = "--"
			}
			if (!TextUtils.isEmpty(data.vInfluenceDiscri)) {
				vInfluenceDiscri.text = data.vInfluenceDiscri
			}else {
				vInfluenceDiscri.text = "--"
			}
			vStartTime.text = "开始时间："+data.vStartTime+"\n结束时间："+data.vEndTime+"\n直报信息编号："+data.dRecordId
		} catch (e : ParseException) {
			e.printStackTrace()
		} catch (e : NullPointerException) {
			e.printStackTrace()
		}
	}

	override fun onClick(v: View?) {
		when(v!!.id) {
			R.id.llBack -> finish()
		}
	}
	
}
