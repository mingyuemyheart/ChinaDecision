package com.china.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.dto.DisasterReportDto;
import com.china.utils.WeatherUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 灾情直报详情
 * @author shawn_sun
 */
public class ShawnDisasterReportDetailActivity extends ShawnBaseActivity implements OnClickListener{
	
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_disaster_report_detail);
		initWidget();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("灾情详情");
		TextView vSendername = findViewById(R.id.vSendername);
		TextView vEdittime = findViewById(R.id.vEdittime);
		TextView vCategory = findViewById(R.id.vCategory);
		TextView vGeneralLoss = findViewById(R.id.vGeneralLoss);
		TextView vRzDpop = findViewById(R.id.vRzDpop);
		TextView vSummary = findViewById(R.id.vSummary);
		TextView vInfluenceDiscri = findViewById(R.id.vInfluenceDiscri);
		TextView vStartTime = findViewById(R.id.vStartTime);

		DisasterReportDto data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			try {
				vSendername.setText(data.vSendername);
				vEdittime.setText("上报时间："+sdf2.format(sdf1.parse(data.vEdittime))+"\n上报人："+data.vEditor+"    电话："+data.vTaPhone);
				vCategory.setText(WeatherUtil.getDisasterClass(Integer.valueOf(data.vCategory)));
				if (!TextUtils.isEmpty(data.vGeneralLoss)) {
					vGeneralLoss.setText(data.vGeneralLoss+"万元");
				}else {
					vGeneralLoss.setText("--");
				}
				if (!TextUtils.isEmpty(data.vRzDpop)) {
					vRzDpop.setText(data.vRzDpop+"人");
				}else {
					vRzDpop.setText("--");
				}
				if (!TextUtils.isEmpty(data.vSummary)) {
					vSummary.setText(data.vSummary);
				}else {
					vSummary.setText("--");
				}
				if (!TextUtils.isEmpty(data.vInfluenceDiscri)) {
					vInfluenceDiscri.setText(data.vInfluenceDiscri);
				}else {
					vInfluenceDiscri.setText("--");
				}
				vStartTime.setText("开始时间："+data.vStartTime+"\n结束时间："+data.vEndTime+"\n直报信息编号："+data.dRecordId);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
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
