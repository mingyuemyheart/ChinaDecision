package com.china.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.china.R
import com.china.common.CONST
import com.china.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.layout_title.*

/**
 * 关于我们
 */
class AboutActivity : BaseActivity(), OnClickListener {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_about)
		initWidget()
	}

	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvVersion.text = CommonUtil.getVersion(this)
		val str1 = "       “中国气象”是中国气象局官方手机决策气象服务客户端，面向政府部门提供决策气象服务支撑，客户端由中国气象局公共气象服务中心主办。"
		val str2 = "       “中国气象”客户端在提供权威的气象信息基础上还为用户提供图形化服务，通过电子地图形式展现气象监测、预报服务、气象灾害预警、云图、台风路径、雷达图、降雨图等多元化气象信息。"
		val str3 = "       “中国气象”同时提供全国天气信息资讯、气象服务专报等专业服务信息，是集多地区、多行业、多功能的综合性决策气象服务新媒体客户端应用。采用了先进的信息网络技术，集合了丰富的气象服务内容，依托美观清晰的服务界面，为用户提供可靠、专业、及时、准确的气象服务。"
		tvContent.text = str1+"\n\n$str2"+"\n\n$str3"

		val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
		if (!TextUtils.isEmpty(title)) {
			tvTitle.text = title
		}
	}

	override fun onClick(v: View?) {
		when(v!!.id) {
			R.id.llBack -> finish()
		}
	}

}
