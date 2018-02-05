package com.china.activity;

/**
 * 实况监测等模块
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.R;
import com.china.adapter.ProductAdapter;
import com.china.common.CONST;
import com.china.common.ColumnData;
import com.china.dto.NewsDto;
import com.china.utils.CommonUtil;
import com.tendcloud.tenddata.TCAgent;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private GridView gridView = null;
	private ProductAdapter mAdapter = null;
	private List<ColumnData> mList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product);
		mContext = this;
		initWidget();
		initGridView();
	}

	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);

		ColumnData data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			tvTitle.setText(data.name);
			mList.clear();
			mList.addAll(data.child);
			CommonUtil.submitClickCount(data.columnId, data.name);
		}
	}
	
	private void initGridView() {
		gridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new ProductAdapter(mContext, mList);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = mList.get(arg2);
				Intent intent;
				if (TextUtils.equals(dto.showType, CONST.URL)) {//网页类
					intent = new Intent(mContext, WeatherInfoDetailActivity.class);
					NewsDto data = new NewsDto();
					data.title = dto.name;
					data.detailUrl = dto.dataUrl;
					data.imgUrl = dto.icon;
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", data);
					intent.putExtras(bundle);

					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.PDF)) {
					intent = new Intent(mContext, PDFActivity.class);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.NEWS)) {
					intent = new Intent(mContext, WeatherInfoActivity.class);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.PRODUCT)) {
					intent = new Intent(mContext, ProductActivity2.class);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", dto);
					intent.putExtras(bundle);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.LOCAL)) {
					if (TextUtils.equals(dto.id, "101")) {//站点检测
						intent = new Intent(mContext, StationMonitorActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "102")) {//中国大陆区域彩色云图
						intent = new Intent(mContext, WeatherInfoDetailActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, CONST.CLOUD_URL);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "103")) {//台风路径
						intent = new Intent(mContext, TyphoonRouteActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "104")) {//天气统计
						intent = new Intent(mContext, StaticsActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "105")) {//社会化观测
						intent = new Intent(mContext, SocietyObserveActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "106")) {//空气污染
						intent = new Intent(mContext, AirPolutionActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "107")) {//视频会商
						intent = new Intent(mContext, WeatherMeetingActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "109")) {//天气图分析
						intent = new Intent(mContext, WeatherChartAnalysisActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "201")) {//城市天气预报
						intent = new Intent(mContext, CityForecastActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "202")) {//分钟级降水估测
						intent = new Intent(mContext, MinuteFallActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "203")) {//等风来
						intent = new Intent(mContext, WaitWindActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, CONST.WAIT_WIND);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "204")) {//分钟降水与强对流
						intent = new Intent(mContext, StrongStreamActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "301")) {//灾情专报
						intent = new Intent(mContext, DisasterSpecialActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "302")) {//灾情直报
						intent = new Intent(mContext, DisasterReportActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "601")) {//视频直播
						intent = new Intent(mContext, WeatherMeetingActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "-1")) {
						Toast.makeText(mContext, "频道建设中", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
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

	@Override
	protected void onResume() {
		super.onResume();
		if (tvTitle != null) {
			TCAgent.onPageStart(mContext, tvTitle.getText().toString());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (tvTitle != null) {
			TCAgent.onPageEnd(mContext, tvTitle.getText().toString());
		}
	}
	
}
