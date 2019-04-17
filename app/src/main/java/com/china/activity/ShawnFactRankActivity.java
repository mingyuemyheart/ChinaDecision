package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnFactRankAdapter;
import com.china.common.CONST;
import com.china.dto.StationMonitorDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 站点排序
 */
public class ShawnFactRankActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;
	private TextView tvTemp2,tvRain2,tvHumidity2,tvVisibility2,tvPressure2,tvWindSpeed2,tvArea, tvTime;
	private LinearLayout llRain,llTemp,llHumidity,llWind,llVisible,llPressure,llScrollView,llPrompt,llScroll;
	private TextView tv11,tv12,tv13,tv14,tv21,tv22,tv23,tv24,tv25,tv26,tv31,tv32,tv41,tv42,tv51,tv52,tv61,tv62;
	private String startTime,endTime,provinceName = "";
	private ImageView ivGuide;//引导页
	private ListView mListView;
	private ShawnFactRankAdapter mAdapter;
	private List<StationMonitorDto> mList = new ArrayList<>();
	private List<StationMonitorDto> ttList = new ArrayList<>();//高温温度
	private List<StationMonitorDto> ltList = new ArrayList<>();//低温温度
	private List<StationMonitorDto> hMaxList = new ArrayList<>();//湿度
	private List<StationMonitorDto> pMaxList = new ArrayList<>();//气压
	private List<StationMonitorDto> vMaxList = new ArrayList<>();//能见度
	private List<StationMonitorDto> wList = new ArrayList<>();//风速
	private List<StationMonitorDto> rList = new ArrayList<>();//降水
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private SimpleDateFormat sdf4 = new SimpleDateFormat("MM月dd日 HH时", Locale.CHINA);
	private SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMdd08", Locale.CHINA);
	private SimpleDateFormat sdf6 = new SimpleDateFormat("yyyyMMdd20", Locale.CHINA);
	private SimpleDateFormat sdf7 = new SimpleDateFormat("yyyyMMdd02", Locale.CHINA);
	private SimpleDateFormat sdf8 = new SimpleDateFormat("yyyyMMdd14", Locale.CHINA);
	private AVLoadingIndicatorView loadingView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_fact_rank);
		mContext = this;
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		loadingView = findViewById(R.id.loadingView);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("实况排名");
		tvTemp2 = findViewById(R.id.tvTemp2);
		tvTemp2.setOnClickListener(this);
		tvRain2 = findViewById(R.id.tvRain2);
		tvRain2.setOnClickListener(this);
		tvHumidity2 = findViewById(R.id.tvHumidity2);
		tvHumidity2.setOnClickListener(this);
		tvVisibility2 = findViewById(R.id.tvVisibility2);
		tvVisibility2.setOnClickListener(this);
		tvPressure2 = findViewById(R.id.tvPressure2);
		tvPressure2.setOnClickListener(this);
		tvWindSpeed2 = findViewById(R.id.tvWindSpeed2);
		tvWindSpeed2.setOnClickListener(this);
		ImageView ivMapSearch = findViewById(R.id.ivMapSearch);
		ivMapSearch.setOnClickListener(this);
		ivMapSearch.setVisibility(View.VISIBLE);
		tvArea = findViewById(R.id.tvArea);
		tvTime = findViewById(R.id.tvTime);
		llRain = findViewById(R.id.llRain);
		llTemp = findViewById(R.id.llTemp);
		llHumidity = findViewById(R.id.llHumidity);
		llWind = findViewById(R.id.llWind);
		llVisible = findViewById(R.id.llVisible);
		llPressure = findViewById(R.id.llPressure);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		llScrollView = findViewById(R.id.llScrollView);
		llPrompt = findViewById(R.id.llPrompt);
		llScroll = findViewById(R.id.llScroll);
		tv11 = findViewById(R.id.tv11);
		tv11.setOnClickListener(this);
		tv12 = findViewById(R.id.tv12);
		tv12.setOnClickListener(this);
		tv13 = findViewById(R.id.tv13);
		tv13.setOnClickListener(this);
		tv14 = findViewById(R.id.tv14);
		tv14.setOnClickListener(this);
		tv21 = findViewById(R.id.tv21);
		tv21.setOnClickListener(this);
		tv22 = findViewById(R.id.tv22);
		tv22.setOnClickListener(this);
		tv23 = findViewById(R.id.tv23);
		tv23.setOnClickListener(this);
		tv24 = findViewById(R.id.tv24);
		tv24.setOnClickListener(this);
		tv25 = findViewById(R.id.tv25);
		tv25.setOnClickListener(this);
		tv26 = findViewById(R.id.tv26);
		tv26.setOnClickListener(this);
		tv31 = findViewById(R.id.tv31);
		tv31.setOnClickListener(this);
		tv32 = findViewById(R.id.tv32);
		tv32.setOnClickListener(this);
		tv41 = findViewById(R.id.tv41);
		tv41.setOnClickListener(this);
		tv42 = findViewById(R.id.tv42);
		tv42.setOnClickListener(this);
		tv51 = findViewById(R.id.tv51);
		tv51.setOnClickListener(this);
		tv52 = findViewById(R.id.tv52);
		tv52.setOnClickListener(this);
		tv61 = findViewById(R.id.tv61);
		tv61.setOnClickListener(this);
		tv62 = findViewById(R.id.tv62);
		tv62.setOnClickListener(this);
		ivGuide = findViewById(R.id.ivGuide);
		ivGuide.setOnClickListener(this);
		CommonUtil.showGuidePage(mContext, this.getClass().getName(), ivGuide);
		
		try {
			endTime = sdf3.format(new Date());
			startTime = sdf3.format(new Date().getTime()-60*60*1000);
			if (TextUtils.equals(startTime, endTime)) {
				tvTime.setText(sdf4.format(sdf3.parse(startTime)));
			}else {
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
			}
			tvArea.setText(getString(R.string.nation));
			provinceName = "";
			OkHttpList("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+endTime+"&endtime="+endTime+"&province="+provinceName+"&map=all&num=30");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void OkHttpList(final String url) {
		loadingView.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONArray array = new JSONArray(result);
										JSONObject obj0 = array.getJSONObject(0);
										if (!obj0.isNull("balltempmax")) {
											ttList.clear();
											JSONArray itemArray = obj0.getJSONArray("balltempmax");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.ballTemp = itemObj.getString("balltemp");
												dto.value = dto.ballTemp+getString(R.string.unit_degree);
												dto.stationId = itemObj.getString("stationid");
												ttList.add(dto);
											}
										}

										JSONObject obj1 = array.getJSONObject(1);
										if (!obj1.isNull("humiditymax")) {
											hMaxList.clear();
											JSONArray itemArray = obj1.getJSONArray("humiditymax");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.humidity = itemObj.getString("humidity");
												dto.value = dto.humidity+getString(R.string.unit_percent);
												dto.stationId = itemObj.getString("stationid");
												hMaxList.add(dto);
											}
										}

										JSONObject obj2 = array.getJSONObject(2);
										if (!obj2.isNull("airpressuremax")) {
											pMaxList.clear();
											JSONArray itemArray = obj2.getJSONArray("airpressuremax");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.airPressure = itemObj.getString("airpressure");
												dto.value = dto.airPressure+getString(R.string.unit_hPa);
												dto.stationId = itemObj.getString("stationid");
												pMaxList.add(dto);
											}
										}

										JSONObject obj3 = array.getJSONObject(3);
										if (!obj3.isNull("visibilitymin")) {
											vMaxList.clear();
											JSONArray itemArray = obj3.getJSONArray("visibilitymin");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.visibility = itemObj.getString("visibility");
												dto.value = dto.visibility+getString(R.string.unit_km);
												dto.stationId = itemObj.getString("stationid");
												vMaxList.add(dto);
											}
										}

										JSONObject obj4 = array.getJSONObject(4);
										if (!obj4.isNull("windspeedmax")) {
											wList.clear();
											JSONArray itemArray = obj4.getJSONArray("windspeedmax");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");

												float fx = Float.valueOf(itemObj.getString("winddir"));
												String wind_dir = null;
												if (fx == 0 || fx == 360) {
													wind_dir = "北";
												} else if (fx > 0 && fx < 90) {
													wind_dir = "东北";
												} else if (fx == 90) {
													wind_dir = "东";
												} else if (fx > 90 && fx < 180) {
													wind_dir = "东南";
												} else if (fx == 180) {
													wind_dir = "南";
												} else if (fx > 180 && fx < 270) {
													wind_dir = "西南";
												} else if (fx == 270) {
													wind_dir = "西";
												} else if (fx > 270) {
													wind_dir = "西北";
												}
												dto.windDir = wind_dir;
												dto.windSpeed = itemObj.getString("windspeed");
												dto.value = dto.windDir+" "+dto.windSpeed+getString(R.string.unit_speed);
												dto.stationId = itemObj.getString("stationid");
												wList.add(dto);
											}
										}

										JSONObject obj5 = array.getJSONObject(5);
										if (!obj5.isNull("rainfallmax")) {
											rList.clear();
											JSONArray itemArray = obj5.getJSONArray("rainfallmax");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.precipitation1h = itemObj.getString("rainfall");
												dto.value = dto.precipitation1h+getString(R.string.unit_mm);
												dto.stationId = itemObj.getString("stationid");
												rList.add(dto);
											}
										}
										setListData(rList);
										loadingView.setVisibility(View.GONE);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
	/**
	 * 查询单个要素
	 * @param type //1、21、22、3、4、5、6分别代表降水、高温、低温、相对湿度、风向风速、能见度、气压
	 * @param url
	 */
	private void OkHttpSingle(final String url, final int type) {
		loadingView.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("balltempmax")) {
											ttList.clear();
											JSONArray itemArray = obj.getJSONArray("balltempmax");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.ballTemp = itemObj.getString("balltemp");
												dto.value = dto.ballTemp+getString(R.string.unit_degree);
												dto.stationId = itemObj.getString("stationid");
												ttList.add(dto);
											}
										}

										if (!obj.isNull("balltempmin")) {
											ltList.clear();
											JSONArray itemArray = obj.getJSONArray("balltempmin");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.ballTemp = itemObj.getString("balltemp");
												dto.value = dto.ballTemp+getString(R.string.unit_degree);
												dto.stationId = itemObj.getString("stationid");
												ltList.add(dto);
											}
										}

										if (!obj.isNull("humiditymax")) {
											hMaxList.clear();
											JSONArray itemArray = obj.getJSONArray("humiditymax");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.humidity = itemObj.getString("humidity");
												dto.value = dto.humidity+getString(R.string.unit_percent);
												dto.stationId = itemObj.getString("stationid");
												hMaxList.add(dto);
											}
										}

										if (!obj.isNull("airpressuremax")) {
											pMaxList.clear();
											JSONArray itemArray = obj.getJSONArray("airpressuremax");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.airPressure = itemObj.getString("airpressure");
												dto.value = dto.airPressure+getString(R.string.unit_hPa);
												dto.stationId = itemObj.getString("stationid");
												pMaxList.add(dto);
											}
										}

										if (!obj.isNull("visibilitymin")) {
											vMaxList.clear();
											JSONArray itemArray = obj.getJSONArray("visibilitymin");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.visibility = itemObj.getString("visibility");
												dto.value = dto.visibility+getString(R.string.unit_km);
												dto.stationId = itemObj.getString("stationid");
												vMaxList.add(dto);
											}
										}

										if (!obj.isNull("windspeedmax")) {
											wList.clear();
											JSONArray itemArray = obj.getJSONArray("windspeedmax");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");

												float fx = Float.valueOf(itemObj.getString("winddir"));
												String wind_dir = null;
												if (fx == 0 || fx == 360) {
													wind_dir = "北";
												} else if (fx > 0 && fx < 90) {
													wind_dir = "东北";
												} else if (fx == 90) {
													wind_dir = "东";
												} else if (fx > 90 && fx < 180) {
													wind_dir = "东南";
												} else if (fx == 180) {
													wind_dir = "南";
												} else if (fx > 180 && fx < 270) {
													wind_dir = "西南";
												} else if (fx == 270) {
													wind_dir = "西";
												} else if (fx > 270) {
													wind_dir = "西北";
												}
												dto.windDir = wind_dir;
												dto.windSpeed = itemObj.getString("windspeed");
												dto.value = dto.windDir+" "+dto.windSpeed+getString(R.string.unit_speed);
												dto.stationId = itemObj.getString("stationid");
												wList.add(dto);
											}
										}

										if (!obj.isNull("rainfallmax")) {
											rList.clear();
											JSONArray itemArray = obj.getJSONArray("rainfallmax");
											for (int i = 0; i < itemArray.length(); i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.precipitation1h = itemObj.getString("rainfall");
												dto.value = dto.precipitation1h+getString(R.string.unit_mm);
												dto.stationId = itemObj.getString("stationid");
												rList.add(dto);
											}
										}

										if (type == 1) {
											setListData(rList);
										}else if (type == 21) {
											setListData(ttList);
										}else if (type == 22) {
											setListData(ltList);
										}else if (type == 3) {
											setListData(hMaxList);
										}else if (type == 4) {
											setListData(wList);
										}else if (type == 5) {
											setListData(vMaxList);
										}else if (type == 6) {
											setListData(pMaxList);
										}
										loadingView.setVisibility(View.GONE);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
	private void initListView() {
		mListView = findViewById(R.id.listView);
		mAdapter = new ShawnFactRankAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				StationMonitorDto dto = mList.get(arg2);
				Intent intent = new Intent(mContext, ShawnFactRankDetailActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
				intent.putExtra("stationId", dto.stationId);
				intent.putExtra("interface", "newOneDay");
				startActivity(intent);
			}
		});
	}
	
	private void setListData(List<StationMonitorDto> list) {
		mList.clear();
		mList.addAll(list);
		
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivGuide:
			ivGuide.setVisibility(View.GONE);
			CommonUtil.saveGuidePageState(mContext, this.getClass().getName());
			break;
		case R.id.tvRain2:
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date().getTime()-60*60*1000);
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			llRain.setVisibility(View.VISIBLE);
			llTemp.setVisibility(View.GONE);
			llHumidity.setVisibility(View.GONE);
			llWind.setVisibility(View.GONE);
			llVisible.setVisibility(View.GONE);
			llPressure.setVisibility(View.GONE);
			setListData(rList);
			tvRain2.setTextColor(0xff2d5a9d);
			tvRain2.setBackgroundResource(R.drawable.bg_layer_button);
			tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
			tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
			tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
			tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
			tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
			tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));
			break;
		case R.id.tvTemp2:
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			llRain.setVisibility(View.GONE);
			llTemp.setVisibility(View.VISIBLE);
			llHumidity.setVisibility(View.GONE);
			llWind.setVisibility(View.GONE);
			llVisible.setVisibility(View.GONE);
			llPressure.setVisibility(View.GONE);
			setListData(ttList);
			tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
			tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvTemp2.setTextColor(0xff2d5a9d);
			tvTemp2.setBackgroundResource(R.drawable.bg_layer_button);
			tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
			tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
			tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
			tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
			tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));
			break;
		case R.id.tvHumidity2:
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			llRain.setVisibility(View.GONE);
			llTemp.setVisibility(View.GONE);
			llHumidity.setVisibility(View.VISIBLE);
			llWind.setVisibility(View.GONE);
			llVisible.setVisibility(View.GONE);
			llPressure.setVisibility(View.GONE);
			setListData(hMaxList);
			tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
			tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
			tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvHumidity2.setTextColor(0xff2d5a9d);
			tvHumidity2.setBackgroundResource(R.drawable.bg_layer_button);
			tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
			tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
			tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
			tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));
			break;
		case R.id.tvWindSpeed2:
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			llRain.setVisibility(View.GONE);
			llTemp.setVisibility(View.GONE);
			llHumidity.setVisibility(View.GONE);
			llWind.setVisibility(View.VISIBLE);
			llVisible.setVisibility(View.GONE);
			llPressure.setVisibility(View.GONE);
			setListData(wList);
			tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
			tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
			tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
			tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
			tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
			tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvWindSpeed2.setTextColor(0xff2d5a9d);
			tvWindSpeed2.setBackgroundResource(R.drawable.bg_layer_button);
			break;
		case R.id.tvVisibility2:
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			llRain.setVisibility(View.GONE);
			llTemp.setVisibility(View.GONE);
			llHumidity.setVisibility(View.GONE);
			llWind.setVisibility(View.GONE);
			llVisible.setVisibility(View.VISIBLE);
			llPressure.setVisibility(View.GONE);
			setListData(vMaxList);
			tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
			tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
			tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
			tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvVisibility2.setTextColor(0xff2d5a9d);
			tvVisibility2.setBackgroundResource(R.drawable.bg_layer_button);
			tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
			tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
			tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));
			break;
		case R.id.tvPressure2:
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			llRain.setVisibility(View.GONE);
			llTemp.setVisibility(View.GONE);
			llHumidity.setVisibility(View.GONE);
			llWind.setVisibility(View.GONE);
			llVisible.setVisibility(View.GONE);
			llPressure.setVisibility(View.VISIBLE);
			setListData(pMaxList);
			tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
			tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
			tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
			tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
			tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
			tvPressure2.setTextColor(0xff2d5a9d);
			tvPressure2.setBackgroundResource(R.drawable.bg_layer_button);
			tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
			tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));
			break;
		case R.id.tv11:
			tv11.setTextColor(getResources().getColor(R.color.white));
			tv12.setTextColor(getResources().getColor(R.color.text_color4));
			tv13.setTextColor(getResources().getColor(R.color.text_color4));
			tv14.setTextColor(getResources().getColor(R.color.text_color4));
			tv11.setBackgroundColor(0xff2d5a9d);
			tv12.setBackgroundColor(getResources().getColor(R.color.white));
			tv13.setBackgroundColor(getResources().getColor(R.color.white));
			tv14.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date().getTime()-60*60*1000);
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+endTime+"&endtime="+endTime+"&province="+provinceName+"&map=rainfall&num=30", 1);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv12:
			tv11.setTextColor(getResources().getColor(R.color.text_color4));
			tv12.setTextColor(getResources().getColor(R.color.white));
			tv13.setTextColor(getResources().getColor(R.color.text_color4));
			tv14.setTextColor(getResources().getColor(R.color.text_color4));
			tv11.setBackgroundColor(getResources().getColor(R.color.white));
			tv12.setBackgroundColor(0xff2d5a9d);
			tv13.setBackgroundColor(getResources().getColor(R.color.white));
			tv14.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date().getTime()-24*60*60*1000);
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=rainfall&num=30", 1);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv13:
			tv11.setTextColor(getResources().getColor(R.color.text_color4));
			tv12.setTextColor(getResources().getColor(R.color.text_color4));
			tv13.setTextColor(getResources().getColor(R.color.white));
			tv14.setTextColor(getResources().getColor(R.color.text_color4));
			tv11.setBackgroundColor(getResources().getColor(R.color.white));
			tv12.setBackgroundColor(getResources().getColor(R.color.white));
			tv13.setBackgroundColor(0xff2d5a9d);
			tv14.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				long eight = sdf3.parse(sdf5.format(new Date())).getTime();
				long current = new Date().getTime();
				if (current >= eight) {
					startTime = sdf5.format(new Date().getTime()-24*60*60*1000);
					endTime = sdf5.format(new Date());
				}else {
					startTime = sdf5.format(new Date().getTime()-48*60*60*1000);
					endTime = sdf5.format(new Date().getTime()-24*60*60*1000);
				}
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=rainfall&num=30", 1);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv14:
			tv11.setTextColor(getResources().getColor(R.color.text_color4));
			tv12.setTextColor(getResources().getColor(R.color.text_color4));
			tv13.setTextColor(getResources().getColor(R.color.text_color4));
			tv14.setTextColor(getResources().getColor(R.color.white));
			tv11.setBackgroundColor(getResources().getColor(R.color.white));
			tv12.setBackgroundColor(getResources().getColor(R.color.white));
			tv13.setBackgroundColor(getResources().getColor(R.color.white));
			tv14.setBackgroundColor(0xff2d5a9d);
			try {
				long twenty = sdf3.parse(sdf6.format(new Date())).getTime();
				long current = new Date().getTime();
				if (current >= twenty) {
					startTime = sdf6.format(new Date().getTime()-24*60*60*1000);
					endTime = sdf6.format(new Date());
				}else {
					startTime = sdf6.format(new Date().getTime()-48*60*60*1000);
					endTime = sdf6.format(new Date().getTime()-24*60*60*1000);
				}
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=rainfall&num=30", 1);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv21:
			tv21.setTextColor(getResources().getColor(R.color.white));
			tv22.setTextColor(getResources().getColor(R.color.text_color4));
			tv23.setTextColor(getResources().getColor(R.color.text_color4));
			tv24.setTextColor(getResources().getColor(R.color.text_color4));
			tv25.setTextColor(getResources().getColor(R.color.text_color4));
			tv26.setTextColor(getResources().getColor(R.color.text_color4));
			tv21.setBackgroundColor(0xff2d5a9d);
			tv22.setBackgroundColor(getResources().getColor(R.color.white));
			tv23.setBackgroundColor(getResources().getColor(R.color.white));
			tv24.setBackgroundColor(getResources().getColor(R.color.white));
			tv25.setBackgroundColor(getResources().getColor(R.color.white));
			tv26.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=temperature&num=30", 21);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv22:
			tv21.setTextColor(getResources().getColor(R.color.text_color4));
			tv22.setTextColor(getResources().getColor(R.color.white));
			tv23.setTextColor(getResources().getColor(R.color.text_color4));
			tv24.setTextColor(getResources().getColor(R.color.text_color4));
			tv25.setTextColor(getResources().getColor(R.color.text_color4));
			tv26.setTextColor(getResources().getColor(R.color.text_color4));
			tv21.setBackgroundColor(getResources().getColor(R.color.white));
			tv22.setBackgroundColor(0xff2d5a9d);
			tv23.setBackgroundColor(getResources().getColor(R.color.white));
			tv24.setBackgroundColor(getResources().getColor(R.color.white));
			tv25.setBackgroundColor(getResources().getColor(R.color.white));
			tv26.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date().getTime()-24*60*60*1000);
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=temperature&num=30", 21);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv23:
			tv21.setTextColor(getResources().getColor(R.color.text_color4));
			tv22.setTextColor(getResources().getColor(R.color.text_color4));
			tv23.setTextColor(getResources().getColor(R.color.white));
			tv24.setTextColor(getResources().getColor(R.color.text_color4));
			tv25.setTextColor(getResources().getColor(R.color.text_color4));
			tv26.setTextColor(getResources().getColor(R.color.text_color4));
			tv21.setBackgroundColor(getResources().getColor(R.color.white));
			tv22.setBackgroundColor(getResources().getColor(R.color.white));
			tv23.setBackgroundColor(0xff2d5a9d);
			tv24.setBackgroundColor(getResources().getColor(R.color.white));
			tv25.setBackgroundColor(getResources().getColor(R.color.white));
			tv26.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				long two = sdf3.parse(sdf7.format(new Date())).getTime();
				long current = new Date().getTime();
				if (current >= two) {
					startTime = sdf7.format(new Date().getTime()-24*60*60*1000);
					endTime = sdf7.format(new Date());
				}else {
					startTime = sdf7.format(new Date().getTime()-48*60*60*1000);
					endTime = sdf7.format(new Date().getTime()-24*60*60*1000);
				}
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=temperature&num=30", 21);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv24:
			tv21.setTextColor(getResources().getColor(R.color.text_color4));
			tv22.setTextColor(getResources().getColor(R.color.text_color4));
			tv23.setTextColor(getResources().getColor(R.color.text_color4));
			tv24.setTextColor(getResources().getColor(R.color.white));
			tv25.setTextColor(getResources().getColor(R.color.text_color4));
			tv26.setTextColor(getResources().getColor(R.color.text_color4));
			tv21.setBackgroundColor(getResources().getColor(R.color.white));
			tv22.setBackgroundColor(getResources().getColor(R.color.white));
			tv23.setBackgroundColor(getResources().getColor(R.color.white));
			tv24.setBackgroundColor(0xff2d5a9d);
			tv25.setBackgroundColor(getResources().getColor(R.color.white));
			tv26.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=temperature&num=30", 22);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv25:
			tv21.setTextColor(getResources().getColor(R.color.text_color4));
			tv22.setTextColor(getResources().getColor(R.color.text_color4));
			tv23.setTextColor(getResources().getColor(R.color.text_color4));
			tv24.setTextColor(getResources().getColor(R.color.text_color4));
			tv25.setTextColor(getResources().getColor(R.color.white));
			tv26.setTextColor(getResources().getColor(R.color.text_color4));
			tv21.setBackgroundColor(getResources().getColor(R.color.white));
			tv22.setBackgroundColor(getResources().getColor(R.color.white));
			tv23.setBackgroundColor(getResources().getColor(R.color.white));
			tv24.setBackgroundColor(getResources().getColor(R.color.white));
			tv25.setBackgroundColor(0xff2d5a9d);
			tv26.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date().getTime()-24*60*60*1000);
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=temperature&num=30", 22);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv26:
			tv21.setTextColor(getResources().getColor(R.color.text_color4));
			tv22.setTextColor(getResources().getColor(R.color.text_color4));
			tv23.setTextColor(getResources().getColor(R.color.text_color4));
			tv24.setTextColor(getResources().getColor(R.color.text_color4));
			tv25.setTextColor(getResources().getColor(R.color.text_color4));
			tv26.setTextColor(getResources().getColor(R.color.white));
			tv21.setBackgroundColor(getResources().getColor(R.color.white));
			tv22.setBackgroundColor(getResources().getColor(R.color.white));
			tv23.setBackgroundColor(getResources().getColor(R.color.white));
			tv24.setBackgroundColor(getResources().getColor(R.color.white));
			tv25.setBackgroundColor(getResources().getColor(R.color.white));
			tv26.setBackgroundColor(0xff2d5a9d);
			try {
				long two = sdf3.parse(sdf8.format(new Date())).getTime();
				long current = new Date().getTime();
				if (current >= two) {
					startTime = sdf8.format(new Date().getTime()-24*60*60*1000);
					endTime = sdf8.format(new Date());
				}else {
					startTime = sdf8.format(new Date().getTime()-48*60*60*1000);
					endTime = sdf8.format(new Date().getTime()-24*60*60*1000);
				}
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=temperature&num=30", 22);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv31:
			tv31.setTextColor(getResources().getColor(R.color.white));
			tv32.setTextColor(getResources().getColor(R.color.text_color4));
			tv31.setBackgroundColor(0xff2d5a9d);
			tv32.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=humidity&num=30", 3);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv32:
			tv31.setTextColor(getResources().getColor(R.color.text_color4));
			tv32.setTextColor(getResources().getColor(R.color.white));
			tv31.setBackgroundColor(getResources().getColor(R.color.white));
			tv32.setBackgroundColor(0xff2d5a9d);
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date().getTime()-24*60*60*1000);
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=humidity&num=30", 3);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv41:
			tv41.setTextColor(getResources().getColor(R.color.white));
			tv42.setTextColor(getResources().getColor(R.color.text_color4));
			tv41.setBackgroundColor(0xff2d5a9d);
			tv42.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=windspeed&num=30", 4);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv42:
			tv41.setTextColor(getResources().getColor(R.color.text_color4));
			tv42.setTextColor(getResources().getColor(R.color.white));
			tv41.setBackgroundColor(getResources().getColor(R.color.white));
			tv42.setBackgroundColor(0xff2d5a9d);
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date().getTime()-24*60*60*1000);
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=windspeed&num=30", 4);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv51:
			tv51.setTextColor(getResources().getColor(R.color.white));
			tv52.setTextColor(getResources().getColor(R.color.text_color4));
			tv51.setBackgroundColor(0xff2d5a9d);
			tv52.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=visibility&num=30", 5);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv52:
			tv51.setTextColor(getResources().getColor(R.color.text_color4));
			tv52.setTextColor(getResources().getColor(R.color.white));
			tv51.setBackgroundColor(getResources().getColor(R.color.white));
			tv52.setBackgroundColor(0xff2d5a9d);
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date().getTime()-24*60*60*1000);
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=visibility&num=30", 5);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv61:
			tv61.setTextColor(getResources().getColor(R.color.white));
			tv62.setTextColor(getResources().getColor(R.color.text_color4));
			tv61.setBackgroundColor(0xff2d5a9d);
			tv62.setBackgroundColor(getResources().getColor(R.color.white));
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date());
				if (TextUtils.equals(startTime, endTime)) {
					tvTime.setText(sdf4.format(sdf3.parse(startTime)));
				}else {
					tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				}
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=airpressure&num=30", 6);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv62:
			tv61.setTextColor(getResources().getColor(R.color.text_color4));
			tv62.setTextColor(getResources().getColor(R.color.white));
			tv61.setBackgroundColor(getResources().getColor(R.color.white));
			tv62.setBackgroundColor(0xff2d5a9d);
			try {
				endTime = sdf3.format(new Date());
				startTime = sdf3.format(new Date().getTime()-24*60*60*1000);
				tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "")) {
					tvArea.setText(getString(R.string.nation));
				}
				OkHttpSingle("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=airpressure&num=30", 6);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		case R.id.ivMapSearch:
			Intent intent = new Intent(mContext, ShawnFactRankSearchActivity.class);
			intent.putExtra("startTime", startTime);
			intent.putExtra("endTime", endTime);
			intent.putExtra("provinceName", tvArea.getText().toString());
			startActivityForResult(intent, 0);
			break;
		case R.id.ivShare:
			Bitmap bitmap1 = CommonUtil.captureView(llPrompt);
			Bitmap bitmap2 = CommonUtil.captureView(mListView);
			Bitmap bitmap3 = CommonUtil.mergeBitmap(ShawnFactRankActivity.this, bitmap1, bitmap2, false);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap2);
			Bitmap bitmap4 = CommonUtil.captureView(llScroll);
			Bitmap bitmap5 = CommonUtil.mergeBitmap(ShawnFactRankActivity.this, bitmap3, bitmap4, false);
			CommonUtil.clearBitmap(bitmap3);
			CommonUtil.clearBitmap(bitmap4);
			Bitmap bitmap6 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
			Bitmap bitmap = CommonUtil.mergeBitmap(ShawnFactRankActivity.this, bitmap5, bitmap6, false);
			CommonUtil.clearBitmap(bitmap5);
			CommonUtil.clearBitmap(bitmap6);
			CommonUtil.share(ShawnFactRankActivity.this, bitmap);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 0:
				Bundle bundle = data.getExtras();
				startTime = bundle.getString("startTime");
				endTime = bundle.getString("endTime");
				try {
					if (TextUtils.equals(startTime, endTime)) {
						tvTime.setText(sdf4.format(sdf3.parse(startTime)));
					}else {
						tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				provinceName = bundle.getString("provinceName");
				tvArea.setText(provinceName);
				provinceName = provinceName.replace(getString(R.string.city), "");
				provinceName = provinceName.replace(getString(R.string.province), "");
				if (TextUtils.equals(provinceName, getString(R.string.nation))) {
					provinceName = "";
				}
				OkHttpList("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime="+startTime+"&endtime="+endTime+"&province="+provinceName+"&map=all&num=30");
				break;

			default:
				break;
			}
		}
	}

}
