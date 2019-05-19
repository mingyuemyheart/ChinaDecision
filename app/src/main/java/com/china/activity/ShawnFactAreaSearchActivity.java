package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnFactAreaSearchAdapter;
import com.china.common.CONST;
import com.china.dto.StationMonitorDto;
import com.china.manager.DBManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 实况-城市选择
 * @author shawn_sun
 */
public class ShawnFactAreaSearchActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private ListView listView = null;
	private ShawnFactAreaSearchAdapter searchAdapter = null;
	private List<StationMonitorDto> searchList = new ArrayList<>();
	private ProgressBar progressBar = null;
	private LinearLayout llContainer = null;
	private int widht = 0;
	private float density = 0;
	private ScrollView scrollView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_fact_area_search);
		mContext = this;
		initWidget();
		initListView();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("城市选择");
		EditText etSearch = findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(watcher);
		progressBar = findViewById(R.id.progressBar);
		llContainer = findViewById(R.id.llContainer);
		scrollView = findViewById(R.id.scrollView);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		widht = dm.widthPixels;
		density = dm.density;

		setProvinceData();
	}

	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			progressBar.setVisibility(View.VISIBLE);
			searchList.clear();
			if (arg0.toString().trim().equals("")) {
				if (listView != null) {
					listView.setVisibility(View.GONE);
				}
				scrollView.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			}else {
				if (listView != null) {
					listView.setVisibility(View.VISIBLE);
				}
				queryInfoByStationId(arg0.toString().trim());
			}
		}
	};

	private void queryInfoByStationId(String keywords) {
		searchList.clear();
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		try {
			if (database != null && database.isOpen()) {
				Cursor cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME1 + " where Pro like " + "\"%"+keywords+"%\""
						+  " or CITY like " + "\"%"+keywords+"%\"" +  " or DIST like " + "\"%"+keywords+"%\"" +  " or ADDR like " + "\"%"+keywords+"%\"" +  " or SID like " + "\"%"+keywords+"%\"", null);
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					StationMonitorDto data = new StationMonitorDto();
					data.lat = cursor.getDouble(cursor.getColumnIndex("LAT"));
					data.lng = cursor.getDouble(cursor.getColumnIndex("LON"));
					data.provinceName = cursor.getString(cursor.getColumnIndex("PRO"));
					data.cityName = cursor.getString(cursor.getColumnIndex("CITY"));
					data.districtName = cursor.getString(cursor.getColumnIndex("DIST"));
					data.addr = cursor.getString(cursor.getColumnIndex("ADDR"));
					data.stationId = cursor.getString(cursor.getColumnIndex("SID"));
					searchList.add(data);
				}
				cursor.close();
				dbManager.closeDatabase();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (searchList.size() > 0 && searchAdapter != null) {
			searchAdapter.notifyDataSetChanged();
			listView.setVisibility(View.VISIBLE);
			scrollView.setVisibility(View.GONE);
			progressBar.setVisibility(View.GONE);
		}
	}

	private void initListView() {
		listView = findViewById(R.id.listView);
		searchAdapter = new ShawnFactAreaSearchAdapter(mContext, searchList);
		listView.setAdapter(searchAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				StationMonitorDto dto = searchList.get(position);
				Intent intent = new Intent(mContext, ShawnFactRankDetailActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, dto.addr+"监测站");
				intent.putExtra("stationId", dto.stationId);
				intent.putExtra("interface", "oneDay");
				startActivity(intent);
			}
		});
	}

	/**
	 * 设置区域及省份信息
	 */
	private void setProvinceData() {
		List<StationMonitorDto> dataList = new ArrayList<>();
		StationMonitorDto dto = new StationMonitorDto();
		dto.partition = "华北";
		dto.areaList.add("北京");
		dto.areaList.add("天津");
		dto.areaList.add("河北");
		dto.areaList.add("山西");
		dto.areaList.add("内蒙古");
		dataList.add(dto);
		dto = new StationMonitorDto();
		dto.partition = "华东";
		dto.areaList.add("上海");
		dto.areaList.add("山东");
		dto.areaList.add("江苏");
		dto.areaList.add("浙江");
		dto.areaList.add("江西");
		dto.areaList.add("安徽");
		dto.areaList.add("福建");
		dataList.add(dto);
		dto = new StationMonitorDto();
		dto.partition = "华中";
		dto.areaList.add("湖北");
		dto.areaList.add("湖南");
		dto.areaList.add("河南");
		dataList.add(dto);
		dto = new StationMonitorDto();
		dto.partition = "华南";
		dto.areaList.add("广东");
		dto.areaList.add("广西");
		dto.areaList.add("海南");
		dataList.add(dto);dto = new StationMonitorDto();
		dto.partition = "东北";
		dto.areaList.add("黑龙江");
		dto.areaList.add("吉林");
		dto.areaList.add("辽宁");
		dataList.add(dto);
		dto = new StationMonitorDto();
		dto.partition = "西北";
		dto.areaList.add("陕西");
		dto.areaList.add("甘肃");
		dto.areaList.add("宁夏");
		dto.areaList.add("新疆");
		dto.areaList.add("青海");
		dataList.add(dto);
		dto = new StationMonitorDto();
		dto.partition = "西南";
		dto.areaList.add("重庆");
		dto.areaList.add("四川");
		dto.areaList.add("贵州");
		dto.areaList.add("云南");
		dto.areaList.add("西藏");
		dataList.add(dto);

		addProvinceView(dataList);
	}
	
	/**
	 * 添加省份信息
	 */
	private void addProvinceView(List<StationMonitorDto> dataList) {
		llContainer.removeAllViews();

		for (int i = 0; i < dataList.size(); i++) {
			StationMonitorDto dto = dataList.get(i);

			//整个区域
			LinearLayout ll1 = new LinearLayout(mContext);
			ll1.setOrientation(LinearLayout.HORIZONTAL);
			ll1.setGravity(Gravity.CENTER_VERTICAL);

			//图片、区域名称
			LinearLayout ll2 = new LinearLayout(mContext);
			ll2.setOrientation(LinearLayout.VERTICAL);
			ll2.setGravity(Gravity.CENTER_HORIZONTAL);
			ll2.setPadding((int)(10*density), (int)(10*density), (int)(10*density), (int)(10*density));
			//图片
			ImageView ivMap = new ImageView(mContext);
			ivMap.setImageResource(setAreaImage(dto.partition, false));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.width = (int)(50*density);
			params.height = (int)(50*density);
			ivMap.setLayoutParams(params);
			ll2.addView(ivMap);
			//区域名称
			TextView tvArea = new TextView(mContext);
			tvArea.setGravity(Gravity.CENTER);
			tvArea.setTextColor(Color.BLACK);
			tvArea.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
			tvArea.setText(dto.partition);
			ll2.addView(tvArea);

			//间隔线
			TextView line = new TextView(mContext);
			line.setBackgroundColor(getResources().getColor(R.color.light_gray));
			line.setWidth((int)(1*density));
			line.setHeight((int)(60*density));

			//省份名称部分
			LinearLayout ll3 = new LinearLayout(mContext);
			ll3.setOrientation(LinearLayout.VERTICAL);
			ll3.setPadding((int)(10*density), (int)(10*density), (int)(10*density), (int)(10*density));
			int rowCount;//4个一行
			if (dto.areaList.size() % 4 == 0) {
				rowCount = dto.areaList.size() / 4;
			}else {
				rowCount = dto.areaList.size() / 4 + 1;
			}
			for (int j = 0; j < rowCount; j++) {
				LinearLayout llItem = new LinearLayout(mContext);
				llItem.setOrientation(LinearLayout.HORIZONTAL);
				llItem.setGravity(Gravity.CENTER_VERTICAL);
				llItem.setPadding(0, (int)(5*density), 0, (int)(5*density));

				int k;
				int size = j*4+4;
				if (size >= dto.areaList.size()) {
					size = dto.areaList.size();
				}
				for ( k = j*4; k < size; k++) {
					//省份名称
					final String proName = dto.areaList.get(k);
					final TextView tvPro = new TextView(mContext);
					tvPro.setGravity(Gravity.CENTER);
					tvPro.setBackgroundResource(R.drawable.corner_unselected_pro);
					tvPro.setPadding((int)(10*density), (int)(5*density), (int)(10*density), (int)(5*density));
					tvPro.setTextColor(Color.BLACK);
					tvPro.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
					tvPro.setText(proName);
					tvPro.setTag(dto.partition+","+proName);
					ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					p.width = (int)(widht-90*density)/4;
					tvPro.setLayoutParams(p);
					tvPro.setOnClickListener(new MyOnClickListener());
					llItem.addView(tvPro);
				}
				ll3.addView(llItem);
			}

			ll1.addView(ll2);
			ll1.addView(line);
			ll1.addView(ll3);
			llContainer.addView(ll1);

			TextView divider = new TextView(mContext);
			divider.setBackgroundColor(getResources().getColor(R.color.light_gray));
			divider.setHeight((int)(10*density));
			llContainer.addView(divider);
		}
	}

	/**
	 * 设置区域image
	 * @param area
	 * @param isSelected
	 * @return
	 */
	private int setAreaImage(String area, boolean isSelected) {
		int drawabele = -1;
		if (TextUtils.equals(area, "华北")) {
			if (isSelected) {
				drawabele = R.drawable.skjc_pic_hbs;
			}else {
				drawabele = R.drawable.skjc_pic_hb;
			}
		}else if (TextUtils.equals(area, "华东")) {
			if (isSelected) {
				drawabele = R.drawable.skjc_pic_hds;
			}else {
				drawabele = R.drawable.skjc_pic_hd;
			}
		}else if (TextUtils.equals(area, "华中")) {
			if (isSelected) {
				drawabele = R.drawable.skjc_pic_hzs;
			}else {
				drawabele = R.drawable.skjc_pic_hz;
			}
		}else if (TextUtils.equals(area, "华南")) {
			if (isSelected) {
				drawabele = R.drawable.skjc_pic_hns;
			}else {
				drawabele = R.drawable.skjc_pic_hn;
			}
		}else if (TextUtils.equals(area, "东北")) {
			if (isSelected) {
				drawabele = R.drawable.skjc_pic_dbs;
			}else {
				drawabele = R.drawable.skjc_pic_db;
			}
		}else if (TextUtils.equals(area, "西北")) {
			if (isSelected) {
				drawabele = R.drawable.skjc_pic_xbs;
			}else {
				drawabele = R.drawable.skjc_pic_xb;
			}
		}else if (TextUtils.equals(area, "西南")) {
			if (isSelected) {
				drawabele = R.drawable.skjc_pic_xns;
			}else {
				drawabele = R.drawable.skjc_pic_xn;
			}
		}
		return drawabele;
	}

	/**
	 * 点击省份监听
	 */
	private class MyOnClickListener implements View.OnClickListener {

		private MyOnClickListener() {
		}

		@Override
		public void onClick(View v) {
			String[] tag = ((String) v.getTag()).split(",");
//			Toast.makeText(mContext, (String)v.getTag(), Toast.LENGTH_SHORT).show();
			for (int l = 0; l < llContainer.getChildCount(); l+=2) {
				LinearLayout ll1 = (LinearLayout) llContainer.getChildAt(l);

				LinearLayout ll2 = (LinearLayout) ll1.getChildAt(0);
				ImageView ivMap = (ImageView) ll2.getChildAt(0);
				TextView tvArea = (TextView) ll2.getChildAt(1);
				String areaName = tvArea.getText().toString();
				if (TextUtils.equals(tag[0], areaName)) {
					ivMap.setImageResource(setAreaImage(areaName, true));
				}else {
					ivMap.setImageResource(setAreaImage(areaName, false));
				}

				LinearLayout ll3 = (LinearLayout) ll1.getChildAt(2);
				for (int i = 0; i < ll3.getChildCount(); i++) {
					LinearLayout llItem = (LinearLayout) ll3.getChildAt(i);
					for (int j = 0; j < llItem.getChildCount(); j++) {
						TextView tvPro = (TextView) llItem.getChildAt(j);
						String proName = tvPro.getText().toString();
						if (TextUtils.equals(tag[1], proName)) {
							tvPro.setBackgroundResource(R.drawable.corner_selected_pro);
							tvPro.setTextColor(Color.WHITE);

							Intent intent = new Intent();
							intent.putExtra("provinceName", proName);
							setResult(RESULT_OK, intent);
							finish();
						}else {
							tvPro.setBackgroundResource(R.drawable.corner_unselected_pro);
							tvPro.setTextColor(Color.BLACK);
						}
					}
				}

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
