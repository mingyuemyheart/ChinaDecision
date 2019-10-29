package com.china.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnWeeklyForecastAdapter;
import com.china.common.MyApplication;
import com.china.dto.WeatherDto;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.WeatherUtil;
import com.china.view.ScrollviewListview;
import com.china.view.WeeklyView;
import com.wang.avi.AVLoadingIndicatorView;

import org.jetbrains.annotations.NotNull;
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

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 天气预报
 */
public class ShawnForecastActivity extends ShawnBaseActivity implements OnClickListener {
	
	private Context mContext;
	private TextView tvCity,tvTime,tvTemp,tvPhe,tvFactInfo;
	private ImageView ivPhenomenon,ivSwitcher;
	private LinearLayout llContainer1,llContainer2,llHourly;
	private ScrollviewListview mListView;//一周预报列表listview
	private ShawnWeeklyForecastAdapter mAdapter;
	private ScrollView scrollView;//全屏
	private int width = 0;
	private List<WeatherDto> weeklyList = new ArrayList<>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	private RelativeLayout reLocation,reWeekly;
	private AVLoadingIndicatorView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_forecast);
		mContext = this;
		checkAuthority();
	}

	private void init() {
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
		tvTitle.setText("天气详情");
		//解决scrollView嵌套listview，动态计算listview高度后，自动滚动到屏幕底部
		tvCity = findViewById(R.id.tvCity);
		tvTime = findViewById(R.id.tvTime);
		tvTime.setFocusable(true);
		tvTime.setFocusableInTouchMode(true);
		tvTime.requestFocus();
		ivSwitcher = findViewById(R.id.ivSwitcher);
		ivSwitcher.setOnClickListener(this);
		tvTemp = findViewById(R.id.tvTemp);
		ivPhenomenon = findViewById(R.id.ivPhenomenon);
		tvPhe = findViewById(R.id.tvPhe);
		tvFactInfo = findViewById(R.id.tvFactInfo);
		llContainer1 = findViewById(R.id.llContainer1);
		llContainer2 = findViewById(R.id.llContainer2);
		scrollView = findViewById(R.id.scrollView);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		reLocation = findViewById(R.id.reLocation);
		llHourly = findViewById(R.id.llHourly);
		reWeekly = findViewById(R.id.reWeekly);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		
		refresh();
	}
	
	private void refresh() {
		String areaName = getIntent().getStringExtra("cityName");
		if (!TextUtils.isEmpty(areaName)) {
			tvCity.setText(areaName);
		}
		
		String cityId = getIntent().getStringExtra("cityId");
		if (!TextUtils.isEmpty(cityId)) {
			getWeatherInfo(cityId);
		}
	}

	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(final String cityId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
					@Override
					public void onComplete(final Weather content) {
						super.onComplete(content);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								String result = content.toString();
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);

										//逐小时预报信息
										if (!obj.isNull("jh")) {
											JSONArray jh = obj.getJSONArray("jh");
											llContainer1.removeAllViews();
											for (int i = 0; i < jh.length(); i++) {
												JSONObject itemObj = jh.getJSONObject(i);
												int hourlyCode = Integer.valueOf(itemObj.getString("ja"));
												int hourlyTemp = Integer.valueOf(itemObj.getString("jb"));
												String hourlyTime = itemObj.getString("jf");

												LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
												View view = inflater.inflate(R.layout.shawn_layout_hour_forecast, null);
												TextView tvHour = view.findViewById(R.id.tvHour);
												ImageView ivPhe = view.findViewById(R.id.ivPhe);
												TextView tvPhe = view.findViewById(R.id.tvPhe);
												TextView tvTemp = view.findViewById(R.id.tvTemp);
												try {
													int current = Integer.parseInt(sdf2.format(sdf1.parse(hourlyTime)));
													tvHour.setText(current+"时");
													if (current >= 5 && current < 18) {
														ivPhe.setImageBitmap(WeatherUtil.getDayBitmap(mContext, hourlyCode));
													}else {
														ivPhe.setImageBitmap(WeatherUtil.getNightBitmap(mContext, hourlyCode));
													}
												} catch (ParseException e) {
													e.printStackTrace();
												}
												tvPhe.setText(getString(WeatherUtil.getWeatherId(hourlyCode)));
												tvTemp.setText(hourlyTemp+getString(R.string.unit_degree));
												llContainer1.addView(view);
											}
										}

										//15天预报
										if (!obj.isNull("f")) {
											weeklyList.clear();
											JSONObject f = obj.getJSONObject("f");
											String f0 = f.getString("f0");
											long foreDate = 0,currentDate = 0;
											try {
												String fTime = sdf3.format(sdf1.parse(f0));
												foreDate = sdf3.parse(fTime).getTime();
												currentDate = sdf3.parse(sdf3.format(new Date())).getTime();
											} catch (ParseException e) {
												e.printStackTrace();
											}

											if (!f.isNull("f1")) {
												JSONArray f1 = f.getJSONArray("f1");
												for (int i = 0; i < f1.length(); i++) {
													WeatherDto dto = new WeatherDto();

													JSONObject weeklyObj = f1.getJSONObject(i);
													//晚上
													dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
													dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
													dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"));
													dto.lowWindDir = Integer.valueOf(weeklyObj.getString("ff"));
													dto.lowWindForce = Integer.valueOf(weeklyObj.getString("fh"));

													//白天
													dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
													dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
													dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"));
													dto.highWindDir = Integer.valueOf(weeklyObj.getString("fe"));
													dto.highWindForce = Integer.valueOf(weeklyObj.getString("fg"));

													if (currentDate > foreDate) {
														if (i == 0) {
															dto.week = "昨天";
														}else if (i == 1) {
															dto.week = "今天";
															tvFactInfo.setText(dto.highTemp+"℃"+"/"+dto.lowTemp+"℃"+"\n");
														}else if (i == 2) {
															dto.week = "明天";
														}else {
															dto.week = CommonUtil.getWeek(i-1);//星期几
														}
														dto.date = CommonUtil.getDate(f0, i);//日期
													}else {
														if (i == 0) {
															dto.week = "今天";
															tvFactInfo.setText(dto.highTemp+"℃"+"/"+dto.lowTemp+"℃"+"\n");
														}else if (i == 1) {
															dto.week = "明天";
														}else {
															dto.week = CommonUtil.getWeek(i);//星期几
														}
														dto.date = CommonUtil.getDate(f0, i);//日期
													}

													weeklyList.add(dto);
												}
											}

											//一周预报列表
											if (mAdapter != null) {
												mAdapter.notifyDataSetChanged();
											}

											//一周预报曲线
											WeeklyView weeklyView = new WeeklyView(mContext);
											weeklyView.setData(weeklyList, foreDate, currentDate);
											llContainer2.removeAllViews();
											llContainer2.addView(weeklyView, width*2, (int)(CommonUtil.dip2px(mContext, 400)));

											//空气质量
											if (!obj.isNull("k")) {
												JSONObject k = obj.getJSONObject("k");
												if (!k.isNull("k3")) {
													String num = WeatherUtil.lastValue(k.getString("k3"));
													if (!TextUtils.isEmpty(num)) {
														tvFactInfo.setText(tvFactInfo.getText().toString()+"空气质量 "+WeatherUtil.getAqi(mContext, Integer.valueOf(num))+" "+num+"\n");
													}
												}
											}

											//实况信息
											if (!obj.isNull("l")) {
												JSONObject l = obj.getJSONObject("l");
												if (!l.isNull("l7")) {
													String time = l.getString("l7");
													if (time != null) {
														tvTime.setText(time+"发布");
													}
												}
												if (!l.isNull("l5")) {
													String weatherCode = WeatherUtil.lastValue(l.getString("l5"));
													int current = Integer.parseInt(sdf2.format(new Date()));
													if (current >= 5 && current < 18) {
														ivPhenomenon.setImageBitmap(WeatherUtil.getDayBitmap(mContext, Integer.valueOf(weatherCode)));
													}else {
														ivPhenomenon.setImageBitmap(WeatherUtil.getNightBitmap(mContext, Integer.valueOf(weatherCode)));
													}
													tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode))));
												}

												if (!MyApplication.FACTENABLE) {
													if (!l.isNull("l1")) {
														String factTemp = WeatherUtil.lastValue(l.getString("l1"));
														tvTemp.setText(factTemp);
													}
													if (!l.isNull("l2")) {
														String humidity = WeatherUtil.lastValue(l.getString("l2"));
														tvFactInfo.setText(tvFactInfo.getText().toString()+"相对湿度 "+humidity + getString(R.string.unit_percent)+"\n");
													}
													if (!l.isNull("l4")) {
														String windDir = WeatherUtil.lastValue(l.getString("l4"));
														if (!l.isNull("l3")) {
															String windForce = WeatherUtil.lastValue(l.getString("l3"));
															if (!TextUtils.isEmpty(windDir) && !TextUtils.isEmpty(windForce)) {
																tvFactInfo.setText(tvFactInfo.getText().toString()+getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir)))+
																		" " + WeatherUtil.getFactWindForce(Integer.valueOf(windForce))+"\n");
															}
														}
													}
												} else {
													OkHttpFact();
												}
											}

										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								scrollView.setVisibility(View.VISIBLE);
								loadingView.setVisibility(View.GONE);

							}
						});
					}

					@Override
					public void onError(Throwable error, String content) {
						super.onError(error, content);
					}
				});
			}
		}).start();
	}

	private void OkHttpFact() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				double lat = getIntent().getDoubleExtra("lat", 0);
				double lng = getIntent().getDoubleExtra("lng", 0);
				final String url = "http://123.56.215.19:8008/lsb/api?elements=TEM,PRE,RHU,WINS,WIND,WEA&interfaceId=getSurfEleInLocationByTime&lat="+lat+"&lon="+lng+"&apikey=AxEkluey201exDxyBoxUeYSw&nsukey=IGzynTgkKQ1Hfa3iJTwv4lci%2F%2F13c%2FQm3p83hih8xiri%2Bc5bm0ia85VASrEHrZRsgj6nlBF1U6F3m5PDkUd6oPtd7itR8p%2BwpJi7yIE%2FVcBsCwya6rhj%2BP%2BhBPCCyrb%2BsyYZLhRk5pkL73jJKE%2Ff4O7PWGPRwVtgQAqgFQ1XEXROJp7qMek79o6%2BiukbiCuY";
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
					}
					@Override
					public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
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
										if (!obj.isNull("DS")) {
											JSONArray array = obj.getJSONArray("DS");
											if (array.length() > 0) {
												JSONObject itemObj = array.getJSONObject(0);
												if (!itemObj.isNull("TEM")) {
													tvTemp.setText(itemObj.getString("TEM"));
												}
												if (!itemObj.isNull("RHU")) {
													String humidity = itemObj.getString("RHU");
													tvFactInfo.setText(tvFactInfo.getText().toString()+"相对湿度 "+humidity + getString(R.string.unit_percent)+"\n");
												}
												if (!itemObj.isNull("WIND")) {
													int windDir = (int)Float.parseFloat(itemObj.getString("WIND"));
													int windForce = (int)Float.parseFloat(itemObj.getString("WINS"));
													String dir = getString(WeatherUtil.getWindDirection(windDir));
													if (TextUtils.isEmpty(dir)) {
														dir = "无持续风向";
													}
													tvFactInfo.setText(tvFactInfo.getText().toString()+dir+
															" " + WeatherUtil.getFactWindForce(windForce)+"\n");
												}
											}
										}
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
	 * 初始化listview
	 */
	private void initListView() {
		mListView = findViewById(R.id.listView);
		mAdapter = new ShawnWeeklyForecastAdapter(mContext, weeklyList);
		mListView.setAdapter(mAdapter);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivSwitcher:
			if (mListView.getVisibility() == View.VISIBLE) {
				ivSwitcher.setImageResource(R.drawable.shawn_icon_switch_trend);
				mListView.setVisibility(View.GONE);
				llContainer2.setVisibility(View.VISIBLE);
			}else {
				ivSwitcher.setImageResource(R.drawable.shawn_icon_switch_list);
				mListView.setVisibility(View.VISIBLE);
				llContainer2.setVisibility(View.GONE);
			}
			break;
		case R.id.ivShare:
			Bitmap bitmap;
			if (mListView.getVisibility() == View.VISIBLE) {
				Bitmap bitmap1 = CommonUtil.captureScrollView(scrollView);
				Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
				bitmap = CommonUtil.mergeBitmap(ShawnForecastActivity.this, bitmap1, bitmap2, false);
				CommonUtil.clearBitmap(bitmap1);
				CommonUtil.clearBitmap(bitmap2);
			}else {
				Bitmap bitmap1 = CommonUtil.captureView(reLocation);
				Bitmap bitmap2 = CommonUtil.captureView(llHourly);
				Bitmap bitmap3 = CommonUtil.mergeBitmap(ShawnForecastActivity.this, bitmap1, bitmap2, false);
				CommonUtil.clearBitmap(bitmap1);
				CommonUtil.clearBitmap(bitmap2);
				Bitmap bitmap4 = CommonUtil.captureView(llContainer1);
				Bitmap bitmap5 = CommonUtil.mergeBitmap(ShawnForecastActivity.this, bitmap3, bitmap4, false);
				CommonUtil.clearBitmap(bitmap3);
				CommonUtil.clearBitmap(bitmap4);
				Bitmap bitmap6 = CommonUtil.captureView(reWeekly);
				Bitmap bitmap7 = CommonUtil.mergeBitmap(ShawnForecastActivity.this, bitmap5, bitmap6, false);
				CommonUtil.clearBitmap(bitmap5);
				CommonUtil.clearBitmap(bitmap6);
				Bitmap bitmap8 = CommonUtil.captureView(llContainer2);
				Bitmap bitmap9 = CommonUtil.mergeBitmap(ShawnForecastActivity.this, bitmap7, bitmap8, false);
				CommonUtil.clearBitmap(bitmap7);
				CommonUtil.clearBitmap(bitmap8);
				Bitmap bitmap10 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
				bitmap = CommonUtil.mergeBitmap(ShawnForecastActivity.this, bitmap9, bitmap10, false);
				CommonUtil.clearBitmap(bitmap9);
				CommonUtil.clearBitmap(bitmap10);
			}
			CommonUtil.share(ShawnForecastActivity.this, bitmap);
			break;

		default:
			break;
		}
	}

	//需要申请的所有权限
	public static String[] allPermissions = new String[] {
			Manifest.permission.CALL_PHONE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	//拒绝的权限集合
	public static List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			init();
		}else {
			deniedList.clear();
			for (String permission : allPermissions) {
				if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(permission);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				init();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_LOCATION);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_LOCATION:
				if (grantResults.length > 0) {
					boolean isAllGranted = true;//是否全部授权
					for (int gResult : grantResults) {
						if (gResult != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//所有权限都授予
						init();
					}else {//只要有一个没有授权，就提示进入设置界面设置
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用电话权限、存储权限，是否前往设置？");
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用电话权限、存储权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}

}