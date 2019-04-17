package com.china.activity;

import android.annotation.SuppressLint;
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

/**
 * 灾情直报详情
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class DisasterReportDetailActivity extends ShawnBaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView vSendername, vEdittime, vEditor, vTaPhone, vCategory, vGeneralLoss, vRzDpop, vSummary, vInfluenceDiscri, vStartTime, vEndTime, dRecordId; 
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.disaster_report_detail);
		initWidget();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("灾情详情");
		vSendername = (TextView) findViewById(R.id.vSendername);
		vEdittime = (TextView) findViewById(R.id.vEdittime);
		vEditor = (TextView) findViewById(R.id.vEditor);
		vTaPhone = (TextView) findViewById(R.id.vTaPhone);
		vCategory = (TextView) findViewById(R.id.vCategory);
		vGeneralLoss = (TextView) findViewById(R.id.vGeneralLoss);
		vRzDpop = (TextView) findViewById(R.id.vRzDpop);
		vSummary = (TextView) findViewById(R.id.vSummary);
		vInfluenceDiscri = (TextView) findViewById(R.id.vInfluenceDiscri);
		vStartTime = (TextView) findViewById(R.id.vStartTime);
		vEndTime = (TextView) findViewById(R.id.vEndTime);
		dRecordId = (TextView) findViewById(R.id.dRecordId);
		
		DisasterReportDto data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			try {
				vSendername.setText(data.vSendername);
				vEdittime.setText("上报时间："+sdf2.format(sdf1.parse(data.vEdittime)));
				vEditor.setText("上报人："+data.vEditor);
				vTaPhone.setText("电话："+data.vTaPhone);
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
				vStartTime.setText("开始时间："+data.vStartTime);
				vEndTime.setText("结束时间："+data.vEndTime);
				dRecordId.setText("直报信息编号："+data.dRecordId);
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
