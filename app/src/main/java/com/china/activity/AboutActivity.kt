package com.china.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.china.R
import com.china.common.CONST
import com.china.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.shawn_layout_title.*

/**
 * 关于我们
 */
class AboutActivity : ShawnBaseActivity(), OnClickListener {

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
		val str1 = "       “中国气象”是中国气象局官方手机决策气象服务客户端，客户端面向中国气象局和国务院部委办局提供决策气象服务保障。"
		val str2 = "       客户端由中国气象局应急减灾与公共服务司主办，中国气象局公共气象服务中心负责运行维护。"
		val str3 = "       “中国气象”手机决策气象服务客户端是集多地区、多行业、多功能的综合性决策气象服务新媒体客户端应用。采用了先进的信息网络技术，集合了丰富的气象服务内容，依托美观清晰的服务界面，迎合移动互联网成为网络服务第一终端的发展趋势，为政府、应急等部门提供可靠、专业、及时、准确的气象服务。"
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
