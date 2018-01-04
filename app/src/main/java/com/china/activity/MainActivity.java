package com.china.activity;

/**
 * 主界面
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.china.R;
import com.china.adapter.MainAdapter;
import com.china.common.CONST;
import com.china.common.ColumnData;
import com.china.dto.NewsDto;
import com.china.dto.WarningDto;
import com.china.dto.WeatherDto;
import com.china.manager.DBManager;
import com.china.manager.DataCleanManager;
import com.china.utils.AutoUpdateUtil;
import com.china.utils.CommonUtil;
import com.china.utils.CustomHttpClient;
import com.china.utils.WeatherUtil;
import com.china.view.HourItemView;
import com.china.view.HourView;
import com.china.view.MyHorizontalScrollView;
import com.china.view.MyHorizontalScrollView.ScrollListener;
import com.china.view.VerticalSwipeRefreshLayout;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;

public class MainActivity extends BaseActivity implements OnClickListener, AMapLocationListener{
	
	private Context mContext = null;
	private TextView tvLocation = null;
	private ImageView ivSetting = null;//设置按钮
	private TextView tvTime = null;
	private RelativeLayout reTitle = null;
	private TextView tvWarning = null;
	private LinearLayout llWarning = null;
	private TextView tvTemperature = null;
	private TextView tvHumidity = null;
	private TextView tvWind = null;
	private ImageView ivWeather = null;
	private RelativeLayout reScrollView = null;
	private MyHorizontalScrollView hScrollView1 = null;
	private LinearLayout llContainer1 = null;
	private MyHorizontalScrollView hScrollView2 = null;
	private LinearLayout llContainer2 = null;
	private LinearLayout llFact = null;
	private ProgressBar progressBar = null;
	private long mExitTime;//记录点击完返回按钮后的long型时间
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private GridView gridView = null;
	private MainAdapter mAdapter = null;
	private ArrayList<ColumnData> channelList = new ArrayList<>();
	private List<WarningDto> warningList = new ArrayList<>();//预警列表
	private String cityName = null;
	private String cityId = null;
	private double lng = 0;
	private double lat = 0;
	private int width = 0;
	private int height = 0;
	private float density = 0;
	private HourView hourView = null;//逐小时view
	private LinearLayout llContainer3 = null;
	private HourItemView hourItemView = null;
	private List<WeatherDto> tempList = new ArrayList<>();
	private int min = 0;
	private int index = 0;
	private VerticalSwipeRefreshLayout refreshLayout = null;//下拉刷新布局
	
	//侧拉页面
//	private RightSlidingMenu rightSlidemenu = null;
	private DrawerLayout drawerlayout = null;
	private RelativeLayout reRight = null;
	private TextView tvUserName = null;
	private TextView tvLogout = null;
	private LinearLayout llSave = null;
	private LinearLayout llClearCache = null;
	private TextView tvCache = null;
	private LinearLayout llVersion = null;
	private LinearLayout llIntro = null;
	private LinearLayout llHotline1 = null;
	private TextView tvHotline1 = null;
	private LinearLayout llHotline2 = null;
	private TextView tvHotline2 = null;
	private LinearLayout llFeedBack = null;
	private LinearLayout llStatistic = null;
	private TextView statisticDivider = null;
	private LinearLayout llRecommend = null;
	private LinearLayout llScreen = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
		if (CommonUtil.isLocationOpen(mContext)) {
			initRefreshLayout();
			initWidget();
			initGridView();
		}else {
			locationDialog(mContext);
		}
    }

    private void locationDialog(Context context) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_location, null);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(context, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.setCancelable(false);
		dialog.show();
		
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivityForResult(intent, 1);
			}
		});
	}
    
    /**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (VerticalSwipeRefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
    
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		reTitle = (RelativeLayout) findViewById(R.id.reTitle);
		tvLocation = (TextView) findViewById(R.id.tvLocation);
		tvLocation.setOnClickListener(this);
		ivSetting = (ImageView) findViewById(R.id.ivSetting);
		ivSetting.setOnClickListener(this);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvTime.setFocusable(true);
		tvTime.setFocusableInTouchMode(true);
		tvTime.requestFocus();
		tvWarning = (TextView) findViewById(R.id.tvWarning);
		llWarning = (LinearLayout) findViewById(R.id.llWarning);
		llWarning.setOnClickListener(this);
		tvTemperature = (TextView) findViewById(R.id.tvTemperature);
		tvHumidity = (TextView) findViewById(R.id.tvHumidity);
		tvWind = (TextView) findViewById(R.id.tvWind);
		ivWeather = (ImageView) findViewById(R.id.ivWeather);
		ivWeather.setOnClickListener(this);
		reScrollView = (RelativeLayout) findViewById(R.id.reScrollView);
		hScrollView1 = (MyHorizontalScrollView) findViewById(R.id.hScrollView1);
		hScrollView1.setScrollListener(scrollListener);
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);
		hScrollView2 = (MyHorizontalScrollView) findViewById(R.id.hScrollView2);
		llContainer2 = (LinearLayout) findViewById(R.id.llContainer2);
		llFact = (LinearLayout) findViewById(R.id.llFact);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		llContainer3 = (LinearLayout) findViewById(R.id.llContainer3);
		
//		rightSlidemenu = (RightSlidingMenu) findViewById(R.id.rightSlidemenu);
//		rightSlidemenu.toggle();
		drawerlayout = (DrawerLayout) findViewById(R.id.drawerlayout);
		drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		reRight = (RelativeLayout) findViewById(R.id.reRight);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvLogout = (TextView) findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);
		llSave = (LinearLayout) findViewById(R.id.llSave);
		llSave.setOnClickListener(this);
		llClearCache = (LinearLayout) findViewById(R.id.llClearCache);
		llClearCache.setOnClickListener(this);
		tvCache = (TextView) findViewById(R.id.tvCache);
		llVersion = (LinearLayout) findViewById(R.id.llVersion);
		llVersion.setOnClickListener(this);
		llIntro = (LinearLayout) findViewById(R.id.llIntro);
		llIntro.setOnClickListener(this);
		llHotline1 = (LinearLayout) findViewById(R.id.llHotline1);
		llHotline1.setOnClickListener(this);
		tvHotline1 = (TextView) findViewById(R.id.tvHotline1);
		llHotline2 = (LinearLayout) findViewById(R.id.llHotline2);
		llHotline2.setOnClickListener(this);
		tvHotline2 = (TextView) findViewById(R.id.tvHotline2);
		llFeedBack = (LinearLayout) findViewById(R.id.llFeedBack);
		llFeedBack.setOnClickListener(this);
		llStatistic = (LinearLayout) findViewById(R.id.llStatistic);
		llStatistic.setOnClickListener(this);
		statisticDivider = (TextView) findViewById(R.id.statisticDivider);
		llRecommend = (LinearLayout) findViewById(R.id.llRecommend);
		llRecommend.setOnClickListener(this);
		llScreen = (LinearLayout) findViewById(R.id.llScreen);
		llScreen.setOnClickListener(this);
		
		if (TextUtils.equals(CONST.USERGROUP, "10") || TextUtils.equals(CONST.USERGROUP, "14") 
				|| TextUtils.equals(CONST.USERGROUP, "20") || TextUtils.equals(CONST.USERGROUP, "52")) {
			llStatistic.setVisibility(View.VISIBLE);
			statisticDivider.setVisibility(View.VISIBLE);
		}else {
			llStatistic.setVisibility(View.GONE);
			statisticDivider.setVisibility(View.GONE);
		}
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		density = dm.density;
		
		ViewGroup.LayoutParams params = reRight.getLayoutParams();
		params.width = width-150;
		reRight.setLayoutParams(params);
		
		SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
		String userName = sharedPreferences.getString(CONST.UserInfo.userName, null);
		if (userName != null) {
			tvUserName.setText(userName);
		}
		
		AutoUpdateUtil.checkUpdate(MainActivity.this, mContext, "52", getString(R.string.app_name), true);
		
		refresh();
	}
	
	private void refresh() {
		try {
			String cache = DataCleanManager.getCacheSize(mContext);
			tvCache.setText(cache);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		llWarning.setVisibility(View.INVISIBLE);
		startLocation();
	}
	
	/**
	 * 获取预警id
	 */
	private String queryWarningIdByCityId(String cityId) {
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = null;
		cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME3 + " where cid = " + "\"" + cityId + "\"",null);
		String warningId = null;
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			warningId = cursor.getString(cursor.getColumnIndex("wid"));
		}
		return warningId;
	}
	
	public Handler handler = new Handler() {
    	@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
    		if (msg.what == com.china.common.CONST.MSG_100) {
    			min = msg.arg1;
    			tempList.clear();
				tempList.addAll((Collection<? extends WeatherDto>) msg.obj);

				for (int i = 0; i < tempList.size(); i++) {
					tempList.get(i).x = tempList.get(0).x;
				}

				hourItemView = new HourItemView(mContext);
				hourItemView.setData(tempList.get(index), min);
				llContainer3.removeAllViews();
				llContainer3.addView(hourItemView);
			}
    	};
    };

	private ScrollListener scrollListener = new ScrollListener() {
		@Override
		public void onScrollChanged(MyHorizontalScrollView hScrollView, int x, int y, int oldx, int oldy) {
			float itemWidth = width/tempList.size();
			index = (int) (x/itemWidth);
			if (index >= tempList.size()) {
				index = tempList.size()-1;
			}
			Log.e("index2", index+"");

			Message msg = new Message();
			msg.what = com.china.common.CONST.MSG_101;
			msg.arg1 = min;
			msg.obj = tempList.get(index);
			hourItemView.handler.sendMessage(msg);
		}
	};

	/**
	 * 开始定位
	 */
	private void startLocation() {
        mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(mContext);//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null && amapLocation.getErrorCode() == 0) {
			String name = amapLocation.getAoiName();
			if (TextUtils.isEmpty(name)) {
				name = amapLocation.getRoad();
			}
    		tvLocation.setText(name);
    		cityName = name;
        	lng = amapLocation.getLongitude();
        	lat = amapLocation.getLatitude();
        	getWeatherInfo(amapLocation.getLongitude(), amapLocation.getLatitude());
        }
	}

	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(double lng, double lat) {
		WeatherAPI.getGeo(mContext, String.valueOf(lng), String.valueOf(lat), new AsyncResponseHandler(){
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				if (!content.isNull("geo")) {
					try {
						JSONObject geoObj = content.getJSONObject("geo");
						if (!geoObj.isNull("id")) {
							cityId = geoObj.getString("id");
							if (!TextUtils.isEmpty(cityId)) {
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
												if (!object.isNull("l1")) {
													String factTemp = WeatherUtil.lastValue(object.getString("l1"));
													tvTemperature.setText(factTemp);
												}
												if (!object.isNull("l2")) {
													String humidity = WeatherUtil.lastValue(object.getString("l2"));
													if (TextUtils.isEmpty(humidity) || TextUtils.equals(humidity, "null")) {
														tvHumidity.setText(getString(R.string.humidity) + "--");
													}else {
														tvHumidity.setText(getString(R.string.humidity) + humidity + getString(R.string.unit_percent));
													}
												}
												if (!object.isNull("l4")) {
													String windDir = WeatherUtil.lastValue(object.getString("l4"));
													if (!object.isNull("l3")) {
														String windForce = WeatherUtil.lastValue(object.getString("l3"));
														tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) +
																WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
													}
												}

												//预警
												warningList.clear();
												JSONArray warningArray = content.getWarningInfo();
												if (warningArray != null && warningArray.length() > 0) {
													for (int j = 0; j < warningArray.length(); j++) {
														JSONObject warningObj = warningArray.getJSONObject(j);
														if (!warningObj.isNull("w11")) {
															WarningDto dto = new WarningDto();
															String html = warningObj.getString("w11");
															dto.html = html;
															if (!TextUtils.isEmpty(html) && html.contains("content2")) {
																dto.html = html.substring(html.indexOf("content2/")+"content2/".length(), html.length());
																String[] array = dto.html.split("-");
																String item0 = array[0];
																String item1 = array[1];
																String item2 = array[2];

																dto.item0 = item0;
																dto.provinceId = item0.substring(0, 2);
																dto.type = item2.substring(0, 5);
																dto.color = item2.substring(5, 7);
																dto.time = item1;
																String w1 = warningObj.getString("w1");
																String w3 = warningObj.getString("w3");
																String w5 = warningObj.getString("w5");
																String w7 = warningObj.getString("w7");
																dto.name = w1+w3+"发布"+w5+w7+"预警";
																warningList.add(dto);

																if (j == 0) {
																	tvWarning.setText(w5+w7+"预警");
																	llWarning.setVisibility(View.VISIBLE);
																}
															}
														}
													}
												}

												//逐小时预报信息
												JSONArray hourlyArray = content.getHourlyFineForecast2();
												List<WeatherDto> hourlyList = new ArrayList<WeatherDto>();
												for (int i = 0; i < hourlyArray.length(); i++) {
													JSONObject itemObj = hourlyArray.getJSONObject(i);
													WeatherDto dto = new WeatherDto();
													dto.hourlyCode = Integer.valueOf(itemObj.getString("ja"));
													dto.hourlyTemp = Integer.valueOf(itemObj.getString("jb"));
													dto.hourlyTime = itemObj.getString("jf");
													hourlyList.add(dto);
												}

												hourView = new HourView(mContext);
												hourView.setData(hourlyList, width*2/density, MainActivity.this);
												llContainer1.removeAllViews();
												llContainer1.addView(hourView, (int)(CommonUtil.dip2px(mContext, width*2/density)), (int)(CommonUtil.dip2px(mContext, 100)));

												//15天预报信息
												llContainer2.removeAllViews();
												for (int i = 1; i <= 15; i++) {
													JSONArray timeArray = content.getTimeInfo(i);
													JSONObject timeObj = timeArray.getJSONObject(0);
													String week = timeObj.getString("t4");//星期几
													String date = timeObj.getString("t1");//日期

													JSONArray weeklyArray = content.getWeatherForecastInfo(i);
													JSONObject weeklyObj = weeklyArray.getJSONObject(0);
													//晚上
													int lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
													String lowPhe  = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
													int lowTemp = Integer.valueOf(weeklyObj.getString("fd"));

													int highPheCode = 0;
													String highPhe = null;
													int highTemp = 0;

													//白天数据缺失时，就使用第二天白天数据
													if (TextUtils.isEmpty(weeklyObj.getString("fa"))) {
														JSONObject secondObj = content.getWeatherForecastInfo(2).getJSONObject(0);
														highPheCode = Integer.valueOf(secondObj.getString("fa"));
														highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(secondObj.getString("fa"))));

														int time1 = Integer.valueOf(secondObj.getString("fc"));
														int time2 = Integer.valueOf(weeklyObj.getString("fd"));
														if (time1 <= time2) {
															highTemp = time2 + 2;
														}else {
															highTemp = Integer.valueOf(secondObj.getString("fc"));
														}
													}else {
														//白天
														highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
														highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
														highTemp = Integer.valueOf(weeklyObj.getString("fc"));
													}

													LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
													View view = inflater.inflate(R.layout.weekly_layout, null);
													TextView tvWeek = (TextView) view.findViewById(R.id.tvWeek);
													ImageView ivPheHigh = (ImageView) view.findViewById(R.id.ivPheHigh);
													ImageView ivPheLow = (ImageView) view.findViewById(R.id.ivPheLow);
													TextView tvTemp = (TextView) view.findViewById(R.id.tvTemp);
													if (i == 1) {
														tvWeek.setText(getString(R.string.today));
													}else {
														String weekStr = mContext.getString(R.string.week)+week.substring(week.length()-1, week.length());
														tvWeek.setText(weekStr);
													}
													ivPheHigh.setImageBitmap(WeatherUtil.getDayBitmap(mContext, highPheCode));
													ivPheLow.setImageBitmap(WeatherUtil.getNightBitmap(mContext, lowPheCode));
													tvTemp.setText(highTemp+getString(R.string.unit_degree)+"/"+lowTemp+getString(R.string.unit_degree));
													llContainer2.addView(view);
												}
											} catch (JSONException e) {
												e.printStackTrace();
											} catch (NullPointerException e) {
												e.printStackTrace();
											}

											refreshLayout.setRefreshing(false);
											progressBar.setVisibility(View.GONE);
											llFact.setVisibility(View.VISIBLE);
										}
									}

									@Override
									public void onError(Throwable error, String content) {
										super.onError(error, content);
									}
								});

//								//获取预警信息
//								String warningId = queryWarningIdByCityId(cityId);
//								if (!TextUtils.isEmpty(warningId)) {
//									asyncQueryWarning("http://decision-admin.tianqi.cn/Home/extra/getwarns?order=1&areaid="+warningId);
//								}
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
	 * 异步请求
	 */
	private void asyncQueryWarning(String requestUrl) {
		HttpAsyncTaskWarning task = new HttpAsyncTaskWarning();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}

	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTaskWarning extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();

		public HttpAsyncTaskWarning() {
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
						if (!object.isNull("data")) {
							warningList.clear();
							JSONArray jsonArray = object.getJSONArray("data");
							for (int i = jsonArray.length()-1; i >= 0; i--) {
								JSONArray tempArray = jsonArray.getJSONArray(i);
								WarningDto dto = new WarningDto();
								dto.html = tempArray.optString(1);
								String[] array = dto.html.split("-");
								String item0 = array[0];
								String item1 = array[1];
								String item2 = array[2];

								dto.provinceId = item0.substring(0, 2);
								dto.type = item2.substring(0, 5);
								dto.color = item2.substring(5, 7);
								dto.time = item1;
								dto.lng = tempArray.optString(2);
								dto.lat = tempArray.optString(3);
								dto.name = tempArray.optString(0);

								warningList.add(dto);

								try {
									if (i == 0 && !TextUtils.isEmpty(dto.name)) {
										if (dto.name.contains("发布")) {
											String[] nameArray = dto.name.split("发布");
											if (!TextUtils.isEmpty(nameArray[1])) {
												if (nameArray[1].contains("[") && nameArray[1].contains("]")) {
													tvWarning.setText(nameArray[1].substring(0, nameArray[1].indexOf("[")));
												}else {
													tvWarning.setText(nameArray[1]);
												}
												llWarning.setVisibility(View.VISIBLE);
											}
										}
									}
								} catch (IndexOutOfBoundsException e) {
									e.printStackTrace();
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
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

	@SuppressWarnings("unchecked")
	private void initGridView() {
		reTitle.measure(0, 0);
		int height1 = reTitle.getMeasuredHeight();
		llFact.measure(0, 0);
		int height2 = llFact.getMeasuredHeight();

		channelList.clear();
		channelList.addAll(getIntent().getExtras().<ColumnData>getParcelableArrayList("dataList"));
		gridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new MainAdapter(mContext, channelList, height-height1-height2);
		gridView.setAdapter(mAdapter);
		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = (int) ((height-height1-height2)/3*Math.ceil((double)(channelList.size()/3.0f)));
		gridView.setLayoutParams(params);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = channelList.get(arg2);
				Intent intent;
				if (TextUtils.equals(dto.showType, CONST.PRODUCT)) {
					if (TextUtils.isEmpty(dto.dataUrl)) {//实况监测、天气预报、专业服务、灾情信息、天气会商
						intent = new Intent(mContext, ProductActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else {//农业气象
						intent = new Intent(mContext, ProductActivity2.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}
				}else if (TextUtils.equals(dto.showType, CONST.URL)) {
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
				}else if (TextUtils.equals(dto.showType, CONST.NEWS)) {//天气资讯
					intent = new Intent(mContext, WeatherInfoActivity.class);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.LOCAL)) {
					if (TextUtils.equals(dto.id, "1")) {//灾情信息
						intent = new Intent(mContext, DisasterSpecialActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "2")) {//预警信息
						intent = new Intent(mContext, WarningActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "3")) {//决策专报
						intent = new Intent(mContext, DecisionNewsActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "101")) {//站点检测
						intent = new Intent(mContext, StationMonitorActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "102")) {//中国大陆区域彩色云图
						intent = new Intent(mContext, WeatherInfoDetailActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, com.china.common.CONST.CLOUD_URL);
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
						intent.putExtra(CONST.WEB_URL, com.china.common.CONST.WAIT_WIND);
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

	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 * @param flag 0删除本地存储，1删除缓存
	 */
	private void deleteDialog(final boolean flag, String message, String content, final TextView textView) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (flag) {
					DataCleanManager.clearCache(mContext);
					try {
						String cache = DataCleanManager.getCacheSize(mContext);
						if (cache != null) {
							textView.setText(cache);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
//					ChannelsManager.clearData(mContext);//清除保存在本地的频道数据
					DataCleanManager.clearLocalSave(mContext);
					try {
						String data = DataCleanManager.getLocalSaveSize(mContext);
						if (data != null) {
							textView.setText(data);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				dialog.dismiss();
			}
		});
	}

	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 */
	private void logout(String message, String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
				Editor editor = sharedPreferences.edit();
				editor.clear();
				editor.commit();
				startActivity(new Intent(mContext, LoginActivity.class));
				finish();
			}
		});
	}

	private void dialPhone(String message, final String content, String positive) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		TextView tvPositive = (TextView) view.findViewById(R.id.tvPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvPositive.setText(positive);
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+content)));
			}
		});
	}

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if (rightSlidemenu.isOpen == false) {
//				rightSlidemenu.openMenu();
//			}
    		if (drawerlayout != null && reRight != null) {
    			if (drawerlayout.isDrawerOpen(reRight)) {
    				drawerlayout.closeDrawer(reRight);
    			}else {
    				if ((System.currentTimeMillis() - mExitTime) > 2000) {
    					Toast.makeText(mContext, getString(R.string.confirm_exit)+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
    					mExitTime = System.currentTimeMillis();
    				} else {
    					finish();
    				}
    			}
			}else {
				if ((System.currentTimeMillis() - mExitTime) > 2000) {
					Toast.makeText(mContext, getString(R.string.confirm_exit)+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
					mExitTime = System.currentTimeMillis();
				} else {
					finish();
				}
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivSetting:
//			rightSlidemenu.toggle();
			if (drawerlayout.isDrawerOpen(reRight)) {
				drawerlayout.closeDrawer(reRight);
			}else {
				drawerlayout.openDrawer(reRight);
			}
			break;
		case R.id.llWarning:
			Intent intent = new Intent(mContext, HeadWarningActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) warningList);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.tvLocation:
			Intent intentLo = new Intent(mContext, ForecastActivity.class);
			intentLo.putExtra("cityName", cityName);
			intentLo.putExtra("cityId", cityId);
			intentLo.putExtra("lng", lng);
			intentLo.putExtra("lat", lat);
			startActivity(intentLo);
			break;
		case R.id.ivWeather:
			if (hScrollView2.getVisibility() == View.VISIBLE) {
				ivWeather.setImageResource(R.drawable.iv_24h_weather_off);
				reScrollView.setVisibility(View.VISIBLE);
				hScrollView2.setVisibility(View.GONE);
			}else {
				ivWeather.setImageResource(R.drawable.iv_24h_weather_on);
				reScrollView.setVisibility(View.GONE);
				hScrollView2.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.llSave:
			Intent intentSave = new Intent(mContext, MyCollectionActivity.class);
			intentSave.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_save));
			startActivity(intentSave);
			break;
		case R.id.llClearCache:
			deleteDialog(true, getString(R.string.delete_cache), getString(R.string.sure_delete_cache), tvCache);
			break;
		case R.id.llVersion:
			intent = new Intent(mContext, AboutActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, "关于我们");
			startActivity(intent);
			break;
		case R.id.tvLogout:
			logout(getString(R.string.logout), getString(R.string.sure_logout));
			break;
		case R.id.llIntro:
			intent = new Intent(mContext, WeatherInfoDetailActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, "中国气象局简介");
			intent.putExtra(CONST.WEB_URL, "http://www.cma.gov.cn/2011zwxx/2011zbmgk/201110/t20111026_117793.html");
			startActivity(intent);
			break;
		case R.id.llFeedBack:
			Intent intentF = new Intent(mContext, FeedbackActivity.class);
			intentF.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_feedback));
			intentF.putExtra(CONST.INTENT_APPID, com.china.common.CONST.APPID);
			startActivity(intentF);
			break;
		case R.id.llHotline1:
			dialPhone(getString(R.string.setting_hotline1), tvHotline1.getText().toString(), getString(R.string.dial));
			break;
		case R.id.llHotline2:
			dialPhone(getString(R.string.setting_hotline2), tvHotline2.getText().toString(), getString(R.string.dial));
			break;
		case R.id.llStatistic:
			intent = new Intent(mContext, Url2Activity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, "周报统计");
			intent.putExtra(CONST.WEB_URL, CONST.COUNTURL);
			startActivity(intent);
			break;
		case R.id.llRecommend:
			intent = new Intent(mContext, Url2Activity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, "应用推荐");
			intent.putExtra(CONST.WEB_URL, CONST.RECOMMENDURL);
			startActivity(intent);
			break;
		case R.id.llScreen:
			intent = new Intent(mContext, ConnectionActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 0) {
			switch (requestCode) {
			case 1:
				if (CommonUtil.isLocationOpen(mContext)) {
					initRefreshLayout();
					initWidget();
					initGridView();
				}else {
					locationDialog(mContext);
				}
				break;

			default:
				break;
			}
		}
	}

}
