package com.china.activity;

/**
 * 天气预报
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.WeeklyForecastAdapter;
import com.china.dto.WeatherDto;
import com.china.utils.CommonUtil;
import com.china.utils.CustomHttpClient;
import com.china.utils.WeatherUtil;
import com.china.view.WeeklyView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;

public class ForecastActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvCity = null;
	private TextView tvTime = null;//更新时间
	private TextView tvRain = null;//下雨、下雪信息
	private TextView tvTemp = null;//实况温度
	private TextView tvForeTemp = null;//预报温度
	private ImageView ivPhenomenon = null;//天气显现对应的图标
	private TextView tvPhe = null;
	private TextView tvHumidity = null;//相对湿度
	private TextView tvWind = null;//风速
	private TextView tvQuality = null;//空气质量
	private LinearLayout llContainer1 = null;//加载逐小时预报曲线容器
	private ImageView ivSwitcher = null;//列表和趋势开关
	private ListView mListView = null;//一周预报列表listview
	private WeeklyForecastAdapter mAdapter = null;
	private LinearLayout llContainer2 = null;//一周预报曲线容器
	private ScrollView scrollView = null;//全屏
	private int width = 0;
	private List<WeatherDto> weeklyList = new ArrayList<>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH");
	private ImageView ivShare = null;
	private RelativeLayout reLocation = null;
	private LinearLayout llThree = null;
	private RelativeLayout reWeekly = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecast);
		mContext = this;
		showDialog();
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		//解决scrollView嵌套listview，动态计算listview高度后，自动滚动到屏幕底部
		tvCity = (TextView) findViewById(R.id.tvCity);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvTime.setFocusable(true);
		tvTime.setFocusableInTouchMode(true);
		tvTime.requestFocus();
		tvRain = (TextView) findViewById(R.id.tvRain);
		ivSwitcher = (ImageView) findViewById(R.id.ivSwitcher);
		ivSwitcher.setOnClickListener(this);
		tvTemp = (TextView) findViewById(R.id.tvTemp);
		tvForeTemp = (TextView) findViewById(R.id.tvForeTemp);
		ivPhenomenon = (ImageView) findViewById(R.id.ivPhenomenon);
		tvPhe = (TextView) findViewById(R.id.tvPhe);
		tvQuality = (TextView) findViewById(R.id.tvQuality);
		tvHumidity = (TextView) findViewById(R.id.tvHumidity);
		tvWind = (TextView) findViewById(R.id.tvWind);
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);
		llContainer2 = (LinearLayout) findViewById(R.id.llContainer2);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		reLocation = (RelativeLayout) findViewById(R.id.reLocation);
		llThree = (LinearLayout) findViewById(R.id.llThree);
		reWeekly = (RelativeLayout) findViewById(R.id.reWeekly);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		
		refresh();
	}
	
	private void refresh() {
		String areaName = getIntent().getStringExtra("cityName");
		if (!TextUtils.isEmpty(areaName)) {
			tvTitle.setText(areaName);
			tvCity.setText(areaName);
		}
		
		String cityId = getIntent().getStringExtra("cityId");
		if (!TextUtils.isEmpty(cityId)) {
			getWeatherInfo(cityId);
		}
//		else {
//			double lng = getIntent().getDoubleExtra("lng", 0);
//			double lat = getIntent().getDoubleExtra("lat", 0);
//			getCityId(lng, lat);
//		}
	}

	/**
	 * 获取天气数据
	 */
	private void getCityId(double lng, double lat) {
		WeatherAPI.getGeo(mContext, String.valueOf(lng), String.valueOf(lat), new AsyncResponseHandler(){
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				if (!content.isNull("geo")) {
					try {
						JSONObject geoObj = content.getJSONObject("geo");
						if (!geoObj.isNull("id")) {
							String cityId = geoObj.getString("id");
							if (!TextUtils.isEmpty(cityId)) {
								getWeatherInfo(cityId);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
			}
		});
	}
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(String cityId) {
		WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
			@Override
			public void onComplete(Weather content) {
				super.onComplete(content);
				if (content != null) {
					try {
						//实况信息
						JSONObject object = content.getWeatherFactInfo();
						if (!object.isNull("l7")) {
							String time = object.getString("l7");
							if (time != null) {
								tvTime.setText(time + getString(R.string.publish));
							}
						}
						if (!object.isNull("l5")) {
							String weatherCode = WeatherUtil.lastValue(object.getString("l5"));
							
							try {
								long zao8 = sdf2.parse("08").getTime();
								long wan8 = sdf2.parse("20").getTime();
								long current = sdf2.parse(sdf2.format(new Date())).getTime();
								if (current >= zao8 && current < wan8) {
									ivPhenomenon.setImageBitmap(WeatherUtil.getDayBitmap(mContext, Integer.valueOf(weatherCode)));
								}else {
									ivPhenomenon.setImageBitmap(WeatherUtil.getNightBitmap(mContext, Integer.valueOf(weatherCode)));
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
							tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode))));
						}
						if (!object.isNull("l1")) {
							String factTemp = WeatherUtil.lastValue(object.getString("l1"));
							tvTemp.setText(factTemp);
						}
						if (!object.isNull("l2")) {
							String humidity = WeatherUtil.lastValue(object.getString("l2"));
							tvHumidity.setText(getString(R.string.humidity)+" "+humidity + getString(R.string.unit_percent));
						}
						if (!object.isNull("l4")) {
							String windDir = WeatherUtil.lastValue(object.getString("l4"));
							if (!object.isNull("l3")) {
								String windForce = WeatherUtil.lastValue(object.getString("l3"));
								tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir)))+
										" " + WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
							}
						}
						
						//空气质量
						JSONObject obj = content.getAirQualityInfo();
						if (obj != null) {
							if (!obj.isNull("k3")) {
								String num = WeatherUtil.lastValue(obj.getString("k3"));
								if (!TextUtils.isEmpty(num)) {
									tvQuality.setText(getString(R.string.weather_quality) + " " +
											WeatherUtil.getAqi(mContext, Integer.valueOf(num)) + " " + num);
								}
							}
						}
						
						//逐小时预报信息
						JSONArray hourlyArray = content.getHourlyFineForecast2();
						llContainer1.removeAllViews();
						for (int i = 0; i < hourlyArray.length(); i++) {
							JSONObject itemObj = hourlyArray.getJSONObject(i);
							int hourlyCode = Integer.valueOf(itemObj.getString("ja"));
							int hourlyTemp = Integer.valueOf(itemObj.getString("jb"));
							String hourlyTime = itemObj.getString("jf");
							
							LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							View view = inflater.inflate(R.layout.layout_hour_forecast, null);
							TextView tvHour = (TextView) view.findViewById(R.id.tvHour);
							ImageView ivPhe = (ImageView) view.findViewById(R.id.ivPhe);
							TextView tvPhe = (TextView) view.findViewById(R.id.tvPhe);
							TextView tvTemp = (TextView) view.findViewById(R.id.tvTemp);
							try {
								tvHour.setText(sdf2.format(sdf1.parse(hourlyTime))+getString(R.string.hour));
							} catch (ParseException e) {
								e.printStackTrace();
							}
							tvPhe.setText(getString(WeatherUtil.getWeatherId(hourlyCode)));
							
							try {
								long zao8 = sdf2.parse("08").getTime();
								long wan8 = sdf2.parse("20").getTime();
								long current = sdf2.parse(sdf2.format(sdf1.parse(hourlyTime))).getTime();
								if (current >= zao8 && current < wan8) {
									ivPhe.setImageBitmap(WeatherUtil.getDayBitmap(mContext, hourlyCode));
								}else {
									ivPhe.setImageBitmap(WeatherUtil.getNightBitmap(mContext, hourlyCode));
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
							tvTemp.setText(hourlyTemp+getString(R.string.unit_degree));
							llContainer1.addView(view);
						}
						
						//一周预报信息
						weeklyList.clear();
						//这里只去一周预报，默认为15天，所以遍历7次
						for (int i = 1; i <= 15; i++) {
							WeatherDto dto = new WeatherDto();
							
							JSONArray weeklyArray = content.getWeatherForecastInfo(i);
							JSONObject weeklyObj = weeklyArray.getJSONObject(0);
							
							//晚上
							dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
							dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
							dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"));
							dto.lowWindDir = Integer.valueOf(weeklyObj.getString("ff"));
							dto.lowWindForce = Integer.valueOf(weeklyObj.getString("fh"));
							
							//白天数据缺失时，就使用第二天白天数据
							if (TextUtils.isEmpty(weeklyObj.getString("fa"))) {
								JSONObject secondObj = content.getWeatherForecastInfo(2).getJSONObject(0);
								dto.highPheCode = Integer.valueOf(secondObj.getString("fa"));
								dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(secondObj.getString("fa"))));
								
								int time1 = Integer.valueOf(secondObj.getString("fc"));
								int time2 = Integer.valueOf(weeklyObj.getString("fd"));
								if (time1 <= time2) {
									dto.highTemp = time2 + 2;
								}else {
									dto.highTemp = Integer.valueOf(secondObj.getString("fc"));
								}

								dto.highWindDir = Integer.valueOf(secondObj.getString("fe"));
								dto.highWindForce = Integer.valueOf(secondObj.getString("fg"));
							}else {
								//白天
								dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
								dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
								dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"));
								dto.highWindDir = Integer.valueOf(weeklyObj.getString("fe"));
								dto.highWindForce = Integer.valueOf(weeklyObj.getString("fg"));
							}
							
							JSONArray timeArray =  content.getTimeInfo(i);
							JSONObject timeObj = timeArray.getJSONObject(0);
							dto.week = timeObj.getString("t4");//星期几
							dto.date = timeObj.getString("t1");//日期
							
							weeklyList.add(dto);

							if (i == 1) {
								tvForeTemp.setText(dto.highTemp+"℃"+"/"+dto.lowTemp+"℃");
							}
						}
						
						//一周预报列表
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
							CommonUtil.setListViewHeightBasedOnChildren(mListView);
						}
						
						//一周预报曲线
						WeeklyView weeklyView = new WeeklyView(mContext);
						weeklyView.setData(weeklyList);
						llContainer2.removeAllViews();
						llContainer2.addView(weeklyView, width*2, (int)(CommonUtil.dip2px(mContext, 400)));
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					
					scrollView.setVisibility(View.VISIBLE);
					cancelDialog();
				}
			}
			
			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
				cancelDialog();
			}
		});
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new WeeklyForecastAdapter(mContext, weeklyList);
		mListView.setAdapter(mAdapter);
		CommonUtil.setListViewHeightBasedOnChildren(mListView);
	}
	
	/**
	 * 异步加载一小时内降雨、或降雪信息
	 * @param lng
	 * @param lat
	 */
	private void query(double lng, double lat) {
		String url = "http://api.caiyunapp.com/v2/HyTVV5YAkoxlQ3Zd/"+lng+","+lat+"/forecast";
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("result")) {
							JSONObject objResult = object.getJSONObject("result");
							if (!objResult.isNull("minutely")) {
								JSONObject objMin = objResult.getJSONObject("minutely");
								if (!objMin.isNull("description")) {
									String rain = objMin.getString("description");
									if (!TextUtils.isEmpty(rain)) {
										tvRain.setText(rain.replace(getString(R.string.little_caiyun), ""));
										tvRain.setVisibility(View.VISIBLE);
									}else {
										tvRain.setVisibility(View.GONE);
									}
								}
							}
						}
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivSwitcher:
			if (mListView.getVisibility() == View.VISIBLE) {
				ivSwitcher.setImageResource(R.drawable.iv_trend);
				mListView.setVisibility(View.GONE);
				llContainer2.setVisibility(View.VISIBLE);
			}else {
				ivSwitcher.setImageResource(R.drawable.iv_list);
				mListView.setVisibility(View.VISIBLE);
				llContainer2.setVisibility(View.GONE);
			}
			CommonUtil.setListViewHeightBasedOnChildren(mListView);
			break;
		case R.id.ivShare:
			Bitmap bitmap = null;
			if (mListView.getVisibility() == View.VISIBLE) {
				Bitmap bitmap1 = CommonUtil.captureScrollView(scrollView);
				Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
				bitmap = CommonUtil.mergeBitmap(ForecastActivity.this, bitmap1, bitmap2, false);
				CommonUtil.clearBitmap(bitmap1);
				CommonUtil.clearBitmap(bitmap2);
			}else {
				Bitmap bitmap1 = CommonUtil.captureView(reLocation);
				Bitmap bitmap2 = CommonUtil.captureView(llThree);
				Bitmap bitmap3 = CommonUtil.mergeBitmap(ForecastActivity.this, bitmap1, bitmap2, false);
				CommonUtil.clearBitmap(bitmap1);
				CommonUtil.clearBitmap(bitmap2);
				Bitmap bitmap4 = CommonUtil.captureView(llContainer1);
				Bitmap bitmap5 = CommonUtil.mergeBitmap(ForecastActivity.this, bitmap3, bitmap4, false);
				CommonUtil.clearBitmap(bitmap3);
				CommonUtil.clearBitmap(bitmap4);
				Bitmap bitmap6 = CommonUtil.captureView(reWeekly);
				Bitmap bitmap7 = CommonUtil.mergeBitmap(ForecastActivity.this, bitmap5, bitmap6, false);
				CommonUtil.clearBitmap(bitmap5);
				CommonUtil.clearBitmap(bitmap6);
				Bitmap bitmap8 = CommonUtil.captureView(llContainer2);
				Bitmap bitmap9 = CommonUtil.mergeBitmap(ForecastActivity.this, bitmap7, bitmap8, false);
				CommonUtil.clearBitmap(bitmap7);
				CommonUtil.clearBitmap(bitmap8);
				Bitmap bitmap10 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
				bitmap = CommonUtil.mergeBitmap(ForecastActivity.this, bitmap9, bitmap10, false);
				CommonUtil.clearBitmap(bitmap9);
				CommonUtil.clearBitmap(bitmap10);
			}
			CommonUtil.share(ForecastActivity.this, bitmap);
			break;

		default:
			break;
		}
	}

}