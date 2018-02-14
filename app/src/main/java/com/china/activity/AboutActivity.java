package com.china.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.common.CONST;
import com.china.utils.CommonUtil;

/**
 * 关于我们
 * @author shawn_sun
 *
 */

public class AboutActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvContent1;
	private TextView tvVersion = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvVersion = (TextView) findViewById(R.id.tvVersion);
		tvVersion.setText(CommonUtil.getVersion(AboutActivity.this));
		tvContent1 = (TextView) findViewById(R.id.tvContent);
		String str1 = "       “中国气象”是中国气象局官方手机决策气象服务客户端，客户端面向中国气象局和国务院部委办局提供决策气象服务保障。";
		String str2 = "       客户端由中国气象局应急减灾与公共服务司主办，中国气象局公共气象服务中心负责运行维护。";
		String str3 = "       “中国气象”手机决策气象服务客户端是集多地区、多行业、多功能的综合性决策气象服务新媒体客户端应用。采用了先进的信息网络技术，集合了丰富的气象服务内容，依托美观清晰的服务界面，迎合移动互联网成为网络服务第一终端的发展趋势，为政府、应急等部门提供可靠、专业、及时、准确的气象服务。";
		tvContent1.setText(str1+"\n\n"+str2+"\n\n"+str3);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
