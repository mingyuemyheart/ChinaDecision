package com.china.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.china.R;
import com.china.adapter.ShawnMainAdapter;
import com.china.adapter.ShawnSettingAdapter;
import com.china.common.CONST;
import com.china.common.ColumnData;
import com.china.common.MyApplication;
import com.china.dto.NewsDto;
import com.china.dto.ShawnSettingDto;
import com.china.dto.WarningDto;
import com.china.dto.WeatherDto;
import com.china.fragment.PdfFragment;
import com.china.manager.DBManager;
import com.china.manager.DataCleanManager;
import com.china.utils.AuthorityUtil;
import com.china.utils.AutoUpdateUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.SecretUrlUtil;
import com.china.utils.WeatherUtil;
import com.china.view.HourItemView;
import com.china.view.HourView;
import com.china.view.MainViewPager;
import com.china.view.MyHorizontalScrollView;
import com.china.view.MyHorizontalScrollView.ScrollListener;
import com.china.view.ScrollviewGridview;
import com.china.view.VerticalSwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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
 * 主界面
 */
public class ShawnMainActivity extends ShawnBaseActivity implements OnClickListener, AMapLocationListener {
	
	private Context mContext;
	private TextView tvLocation,tvTime,tvTemperature,tvHumidity,tvWind,tvAqi,tvFifteen,tvHour;
	private RelativeLayout reTitle,reFact,reScrollView;
	private LinearLayout llWarning,llContainer1,llContainer2,llContainer3;
	private ImageView ivAqi;
	private MyHorizontalScrollView hScrollView2;
	private long mExitTime;//记录点击完返回按钮后的long型时间
	private AMapLocationClientOption mLocationOption;//声明mLocationOption对象
	private AMapLocationClient mLocationClient;//声明AMapLocationClient类对象
	private LatLng locationLatLng = new LatLng(39.904030, 116.407526);
	private String cityName = "北京市",cityId = "101010100";
	private ShawnMainAdapter mAdapter;
	private List<ColumnData> dataList = new ArrayList<>();
	private List<ColumnData> intentList = new ArrayList<>();
	private int width = 0, height = 0, min = 0, index = 0;
	private float density = 0;
	private HourView hourView;//逐小时view
	private HourItemView hourItemView;
	private List<WeatherDto> tempList = new ArrayList<>();
	private VerticalSwipeRefreshLayout refreshLayout;//下拉刷新布局
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	private MyBroadCastReceiver mReceiver;

	//首页pdf文档
	private List<NewsDto> pdfList = new ArrayList<>();
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private ImageView[] ivTips;//装载点的数组
	private ViewGroup viewGroup;
	
	//侧拉页面
	private DrawerLayout drawerlayout;
	private RelativeLayout reRight;
	private String dialNumber = "";
	private ShawnSettingAdapter sAdapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_main);
        mContext = this;
		initRefreshLayout();
		initBroadCast();
		checkMultiAuthority();
	}

	private void initBroadCast() {
    	mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CONST.BROADCAST_FACT);
		registerReceiver(mReceiver, intentFilter);
	}

	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (TextUtils.equals(intent.getAction(), CONST.BROADCAST_FACT)) {
				refreshLayout.setRefreshing(true);
				init();
			}
		}
	}

	private void init() {
		initWidget();
		initGridView();
		initListView();
	}

    /**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
        refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				init();
			}
		});
	}
    
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		checkStorageAuthority();
		reTitle = findViewById(R.id.reTitle);
		ImageView ivAdd = findViewById(R.id.ivAdd);
		ivAdd.setOnClickListener(this);
		tvLocation = findViewById(R.id.tvLocation);
		tvLocation.setOnClickListener(this);
		tvLocation.setText(cityName);
		ImageView ivSetting = findViewById(R.id.ivSetting);
		ivSetting.setOnClickListener(this);
		tvTime = findViewById(R.id.tvTime);
		tvTime.setFocusable(true);
		tvTime.setFocusableInTouchMode(true);
		tvTime.requestFocus();
		tvTime.setOnClickListener(this);
		llWarning = findViewById(R.id.llWarning);
		llWarning.setOnClickListener(this);
		tvTemperature = findViewById(R.id.tvTemperature);
		tvHumidity = findViewById(R.id.tvHumidity);
		tvWind = findViewById(R.id.tvWind);
		tvAqi = findViewById(R.id.tvAqi);
		tvFifteen = findViewById(R.id.tvFifteen);
		tvFifteen.setOnClickListener(this);
		tvHour = findViewById(R.id.tvHour);
		tvHour.setOnClickListener(this);
		ivAqi = findViewById(R.id.ivAqi);
		reScrollView = findViewById(R.id.reScrollView);
        MyHorizontalScrollView hScrollView1 = findViewById(R.id.hScrollView1);
		hScrollView1.setScrollListener(scrollListener);
		llContainer1 = findViewById(R.id.llContainer1);
		hScrollView2 = findViewById(R.id.hScrollView2);
		llContainer2 = findViewById(R.id.llContainer2);
		reFact = findViewById(R.id.reFact);
		llContainer3 = findViewById(R.id.llContainer3);
		viewGroup = findViewById(R.id.viewGroup);

		//侧拉页面
		drawerlayout = findViewById(R.id.drawerlayout);
		drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		reRight = findViewById(R.id.reRight);
		TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvLogout = findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);

		getDisplayWidthHeight();

		ViewGroup.LayoutParams params = reRight.getLayoutParams();
		params.width = width-(int)CommonUtil.dip2px(this, 50);
		reRight.setLayoutParams(params);

		if (!TextUtils.isEmpty(MyApplication.USERNAME)) {
			tvUserName.setText(MyApplication.USERNAME);
		}

		llWarning.setVisibility(View.INVISIBLE);
		startLocation();
	}

	private void getDisplayWidthHeight() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		density = dm.density;
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
		if (mLocationOption == null) {
			mLocationOption = new AMapLocationClientOption();//初始化定位参数
		}
		if (mLocationClient == null) {
			mLocationClient = new AMapLocationClient(mContext);//初始化定位
		}
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null && amapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
			cityName = amapLocation.getCity()+amapLocation.getDistrict()+amapLocation.getStreet()+amapLocation.getStreetNum();
			locationLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
			tvLocation.setText(cityName);
			OkHttpGeo(locationLatLng.longitude, locationLatLng.latitude);

			String pro = amapLocation.getProvince();
			if (pro.startsWith("北京") || pro.startsWith("天津") || pro.startsWith("上海") || pro.startsWith("重庆")) {
				okHttpInfo(amapLocation.getCity(), amapLocation.getDistrict());
			} else {
				okHttpInfo(amapLocation.getProvince(), amapLocation.getCity());
			}
        }
	}

	/**
	 * 获取疫情
	 */
	private void okHttpInfo(final String pro, final String city) {
		final String url = String.format("http://warn-wx.tianqi.cn/Test/getwhqydata?pro=%s&city=%s", pro, city);
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								pdfList.clear();
								initViewPager();
							}
						});
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
										String proCount = "";
										if (!obj.isNull("total_pro")) {
											JSONObject proObj = obj.getJSONObject("total_pro");
											if (!proObj.isNull("confirm")) {
												proCount = proObj.getString("confirm");
											}
										}
										String cityCount = "";
										if (!obj.isNull("total")) {
											JSONObject cityObj = obj.getJSONObject("total");
											if (!cityObj.isNull("confirm")) {
												cityCount = cityObj.getString("confirm");
											}
										}

										pdfList.clear();
										NewsDto dto = new NewsDto();
										dto.header = "【最新疫情】";
										dto.title = dto.header+String.format("%s确诊%s例，%s确诊%s例。", city, cityCount, pro, proCount);
										dto.detailUrl = "https://voice.baidu.com/act/newpneumonia/newpneumonia?fraz=partner&paaz=gjyj";
										dto.showType = CONST.URL;
										dto.imgUrl = "http://decision-admin.tianqi.cn/infomes/data/common/images/img_new@2x.png";
										pdfList.add(dto);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
								initViewPager();
							}
						});
					}
				});
			}
		}).start();
	}

	/**
	 * 获取天气数据
	 */
	private void OkHttpGeo(final double lng, final double lat) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.geo(lng, lat)).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject obj = new JSONObject(result);
								if (!obj.isNull("geo")) {
									JSONObject geoObj = obj.getJSONObject("geo");
									if (!geoObj.isNull("id")) {
										cityId = geoObj.getString("id");
										getWeatherInfo();
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		}).start();
	}

	private void getWeatherInfo() {
		if (TextUtils.isEmpty(cityId)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(String.format("https://videoshfcx.tianqi.cn/dav_tqwy/ty_weather/data/%s.html", cityId)).build(), new Callback() {
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

										//实况信息
										if (!obj.isNull("l")) {
											JSONObject l = obj.getJSONObject("l");
											if (!l.isNull("l7")) {
												String time = l.getString("l7");
												if (time != null) {
													tvTime.setText(time+"发布");
												}
											}

											if (!MyApplication.FACTENABLE) {
												if (!l.isNull("l1")) {
													String factTemp = WeatherUtil.lastValue(l.getString("l1"));
													tvTemperature.setText(factTemp);
												}
												if (!l.isNull("l2")) {
													String humidity = WeatherUtil.lastValue(l.getString("l2"));
													if (TextUtils.isEmpty(humidity) || TextUtils.equals(humidity, "null")) {
														tvHumidity.setText("湿度"+"--");
													}else {
														tvHumidity.setText("湿度"+humidity+getString(R.string.unit_percent));
													}
												}
												if (!l.isNull("l4")) {
													String windDir = WeatherUtil.lastValue(l.getString("l4"));
													if (!l.isNull("l3")) {
														String windForce = WeatherUtil.lastValue(l.getString("l3"));
														tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) + " " +
																WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
													}
												}
											} else {
												OkHttpFact();
											}
										}

										//aqi信息
										if (!obj.isNull("k")) {
											JSONObject k = obj.getJSONObject("k");
											if (!k.isNull("k3")) {
												String num = WeatherUtil.lastValue(k.getString("k3"));
												if (!TextUtils.isEmpty(num)) {
													tvAqi.setText("AQI "+WeatherUtil.getAqi(mContext, Integer.valueOf(num))+" "+num);
													ivAqi.setImageResource(WeatherUtil.getAqiIcon(Integer.valueOf(num)));
												}
											}
										}

										//逐小时预报信息
										if (!obj.isNull("jh")) {
											JSONArray jh = obj.getJSONArray("jh");
											List<WeatherDto> hourlyList = new ArrayList<>();
											for (int i = 0; i < jh.length(); i++) {
												JSONObject itemObj = jh.getJSONObject(i);
												WeatherDto dto = new WeatherDto();
												dto.hourlyCode = Integer.valueOf(itemObj.getString("ja"));
												dto.hourlyTemp = Integer.valueOf(itemObj.getString("jb"));
												dto.hourlyTime = itemObj.getString("jf");
												hourlyList.add(dto);
											}
											hourView = new HourView(mContext);
											hourView.setData(hourlyList, width*2/density, ShawnMainActivity.this);
											llContainer1.removeAllViews();
											llContainer1.addView(hourView, (int)(CommonUtil.dip2px(mContext, width*2/density)), (int)(CommonUtil.dip2px(mContext, 100)));
										}

										//15天预报信息
										if (!obj.isNull("f")) {
											llContainer2.removeAllViews();
											JSONObject f = obj.getJSONObject("f");
											String f0 = f.getString("f0");
											long foreDate = 0,currentDate = 0;
											try {
												foreDate = sdf3.parse(sdf3.format(sdf1.parse(f0))).getTime();
												currentDate = sdf3.parse(sdf3.format(new Date())).getTime();
											} catch (ParseException e) {
												e.printStackTrace();
											}
											JSONArray f1 = f.getJSONArray("f1");
											for (int i = 0; i < f1.length(); i++) {
												String week = CommonUtil.getWeek(i);//星期几
												String date = CommonUtil.getDate(f0, i);//日期

												//预报内容
												JSONObject weeklyObj = f1.getJSONObject(i);
												//晚上
												int lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
												int lowTemp = Integer.valueOf(weeklyObj.getString("fd"));

												//白天
												int highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
												int highTemp = Integer.valueOf(weeklyObj.getString("fc"));

												LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
												View view = inflater.inflate(R.layout.shawn_layout_weekly_forecast, null);
												TextView tvWeek = view.findViewById(R.id.tvWeek);
												ImageView ivPheHigh = view.findViewById(R.id.ivPheHigh);
												ImageView ivPheLow = view.findViewById(R.id.ivPheLow);
												TextView tvTemp = view.findViewById(R.id.tvTemp);

												if (currentDate > foreDate) {
													if (i == 0) {
														tvWeek.setText("昨天");
													}else if (i == 1) {
														tvWeek.setText("今天");
													}else if (i == 2) {
														tvWeek.setText("明天");
													}else {
														tvWeek.setText(CommonUtil.getWeek(i-1));
													}
												}else {
													if (i == 0) {
														tvWeek.setText("今天");
													}else if (i == 1) {
														tvWeek.setText("明天");
													}else {
														tvWeek.setText(CommonUtil.getWeek(i));
													}
												}

												ivPheHigh.setImageBitmap(WeatherUtil.getDayBitmap(mContext, highPheCode));
												ivPheLow.setImageBitmap(WeatherUtil.getNightBitmap(mContext, lowPheCode));
												tvTemp.setText(highTemp+getString(R.string.unit_degree)+"/"+lowTemp+getString(R.string.unit_degree));
												llContainer2.addView(view);
											}
										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								refreshLayout.setRefreshing(false);
								reFact.setVisibility(View.VISIBLE);

							}
						});
					}
				});

				//获取预警信息
				String warningId = queryWarningIdByCityId(cityId);
				if (!TextUtils.isEmpty(warningId)) {
					OkHttpWarning(warningId);
				}

			}
		}).start();
	}

	private void OkHttpFact() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = "http://123.56.215.19:8008/lsb/api?elements=TEM,PRE,RHU,WINS,WIND,WEA&interfaceId=getSurfEleInLocationByTime&lat="+locationLatLng.latitude+"&lon="+locationLatLng.longitude+"&apikey=AxEkluey201exDxyBoxUeYSw&nsukey=IGzynTgkKQ1Hfa3iJTwv4lci%2F%2F13c%2FQm3p83hih8xiri%2Bc5bm0ia85VASrEHrZRsgj6nlBF1U6F3m5PDkUd6oPtd7itR8p%2BwpJi7yIE%2FVcBsCwya6rhj%2BP%2BhBPCCyrb%2BsyYZLhRk5pkL73jJKE%2Ff4O7PWGPRwVtgQAqgFQ1XEXROJp7qMek79o6%2BiukbiCuY";
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
													tvTemperature.setText(itemObj.getString("TEM"));
												}
												if (!itemObj.isNull("RHU")) {
													String humidity = itemObj.getString("RHU");
													tvHumidity.setText("湿度 "+humidity + getString(R.string.unit_percent));
												}
												if (!itemObj.isNull("WIND")) {
													int windDir = (int)Float.parseFloat(itemObj.getString("WIND"));
													int windForce = (int)Float.parseFloat(itemObj.getString("WINS"));
													String dir = getString(WeatherUtil.getWindDirection(windDir));
													if (TextUtils.isEmpty(dir)) {
														dir = "无持续风向";
													}
													tvWind.setText(dir + " " + WeatherUtil.getFactWindForce(windForce));
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
	 * 获取预警id
	 */
	private String queryWarningIdByCityId(String cityId) {
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME3 + " where cid = " + "\"" + cityId + "\"",null);
		String warningId = null;
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			warningId = cursor.getString(cursor.getColumnIndex("wid"));
		}
		cursor.close();
		return warningId;
	}

	/**
	 * 获取预警信息
	 */
	private void OkHttpWarning(final String warningId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				llWarning.removeAllViews();
			}
		});
		String wId = warningId.substring(0, 4);
		if (warningId.startsWith("11") || warningId.startsWith("31") || warningId.startsWith("12") || warningId.startsWith("50")) {
			wId = warningId.substring(0, 2);
		}
		final String url = "https://decision-admin.tianqi.cn/Home/work2019/getwarns?order=0&areaid="+wId;
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
								JSONObject object = new JSONObject(result);
								if (!object.isNull("data")) {
									List<WarningDto> warningList = new ArrayList<>();
									JSONArray jsonArray = object.getJSONArray("data");
									for (int i = 0; i < jsonArray.length(); i++) {
										JSONArray tempArray = jsonArray.getJSONArray(i);
										WarningDto dto = new WarningDto();
										dto.html = tempArray.getString(1);
										String[] array = dto.html.split("-");
										String item0 = array[0];
										String item1 = array[1];
										String item2 = array[2];

										dto.provinceId = item0.substring(0, 2);
										dto.type = item2.substring(0, 5);
										dto.color = item2.substring(5, 7);
										dto.time = item1;
										dto.lng = tempArray.getDouble(2);
										dto.lat = tempArray.getDouble(3);
										dto.name = tempArray.getString(0);

										if (!TextUtils.isEmpty(dto.name) && !dto.name.contains("解除")) {
											if (warningId.startsWith("11") || warningId.startsWith("31") || warningId.startsWith("12") || warningId.startsWith("50")) {
												if (TextUtils.equals(item0, warningId) || TextUtils.equals(item0, warningId.substring(0,2)+"0000")) {
													warningList.add(dto);
												}
											}else {
												if (TextUtils.equals(item0, warningId) || TextUtils.equals(item0, warningId.substring(0,4)+"00")) {
													warningList.add(dto);
												}
											}
										}
									}

									llWarning.removeAllViews();
									LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)CommonUtil.dip2px(mContext, 30), (int)CommonUtil.dip2px(mContext, 30));
									for (WarningDto dto : warningList) {
										ImageView ivWarning = new ImageView(mContext);
										ivWarning.setTag(dto);
										Bitmap bitmap = null;
										if (dto.color.equals(CONST.blue[0])) {
											bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.blue[1]+CONST.imageSuffix);
											if (bitmap == null) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
											}
										}else if (dto.color.equals(CONST.yellow[0])) {
											bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.yellow[1]+CONST.imageSuffix);
											if (bitmap == null) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
											}
										}else if (dto.color.equals(CONST.orange[0])) {
											bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.orange[1]+CONST.imageSuffix);
											if (bitmap == null) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
											}
										}else if (dto.color.equals(CONST.red[0])) {
											bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.red[1]+CONST.imageSuffix);
											if (bitmap == null) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
											}
										}
										if (bitmap == null) {
											bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.imageSuffix);
										}
										ivWarning.setImageBitmap(bitmap);
										ivWarning.setLayoutParams(params);
										llWarning.addView(ivWarning);
										llWarning.setVisibility(View.VISIBLE);

										ivWarning.setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												WarningDto data = (WarningDto) v.getTag();
												Intent intent = new Intent(mContext, WarningDetailActivity.class);
												Bundle bundle = new Bundle();
												bundle.putParcelable("data", data);
												intent.putExtras(bundle);
												startActivity(intent);
											}
										});

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

	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		mHandler.removeMessages(AUTO_PLUS);
		pdfList.addAll(getIntent().getExtras().<NewsDto>getParcelableArrayList("pdfList"));
		ivTips = new ImageView[pdfList.size()];
		viewGroup.removeAllViews();
		fragments.clear();
		for (int i = 0; i < pdfList.size(); i++) {
			NewsDto data = pdfList.get(i);
			Fragment fragment = new PdfFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable("data", data);
			fragment.setArguments(bundle);
			fragments.add(fragment);

			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new ViewGroup.LayoutParams(5, 5));
			ivTips[i] = imageView;
			if(i == 0){
				ivTips[i].setBackgroundResource(R.drawable.point_black);
			}else{
				ivTips[i].setBackgroundResource(R.drawable.point_gray);
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			viewGroup.addView(imageView, layoutParams);
		}

		viewPager = findViewById(R.id.viewPager);
		if (pdfList.size() == 0) {
			viewPager.setVisibility(View.GONE);
			viewGroup.setVisibility(View.GONE);
		}
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			@Override
			public void onPageSelected(int position) {
				for (int i = 0; i < pdfList.size(); i++) {
					if(i == position){
						ivTips[i].setBackgroundResource(R.drawable.point_black);
					}else{
						ivTips[i].setBackgroundResource(R.drawable.point_gray);
					}
				}
			}
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		viewPager.setAdapter(new MyPagerAdapter());

		if (fragments.size() > 1) {
			mHandler.sendEmptyMessageDelayed(AUTO_PLUS, PHOTO_CHANGE_TIME);
		}
	}

	private final int AUTO_PLUS = 1001;
	private static final int PHOTO_CHANGE_TIME = 3000;//定时变量
	private int index_plus = 0;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case AUTO_PLUS:
					viewPager.setCurrentItem(index_plus++);//收到消息后设置当前要显示的图片
					mHandler.sendEmptyMessageDelayed(AUTO_PLUS, PHOTO_CHANGE_TIME);
					if (index_plus >= fragments.size()) {
						index_plus = 0;
					}
					break;
				default:
					break;
			}
		};
	};

	private class MyPagerAdapter extends PagerAdapter {
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(fragments.get(position).getView());
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = fragments.get(position);
			if (!fragment.isAdded()) { // 如果fragment还没有added
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(fragment, fragment.getClass().getSimpleName());
				ft.commit();
				/**
				 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
				 * 会在进程的主线程中,用异步的方式来执行。
				 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
				 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
				 */
				getFragmentManager().executePendingTransactions();
			}

			if (fragment.getView().getParent() == null) {
				container.addView(fragment.getView()); // 为viewpager增加布局
			}
			return fragment.getView();
		}
	}

	@SuppressLint("HandlerLeak")
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

	private void setGridViewData() {
		if (!getIntent().hasExtra("dataList")) {
			return;
		}
		List<ColumnData> list = getIntent().getExtras().getParcelableArrayList("dataList");
		if (list == null || list.size() <= 0) {
			return;
		}
		intentList.clear();
		intentList.addAll(list);
		dataList.clear();
		String columnIds = MyApplication.getColumnIds(this);
		if (!TextUtils.isEmpty(columnIds)) {
			for (int i = 0; i < list.size(); i++) {
				ColumnData dto = list.get(i);
				if (columnIds.contains(dto.columnId)) {//已经有保存的栏目
					dataList.add(dto);
				}
			}
		}else {
			dataList.addAll(list);
		}
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	private void initGridView() {
		setGridViewData();
        ScrollviewGridview gridView = findViewById(R.id.gridView);
		mAdapter = new ShawnMainAdapter(mContext, dataList);
		gridView.setAdapter(mAdapter);
		onLayoutMeasure();
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = dataList.get(arg2);
				Intent intent;
				if (TextUtils.equals(dto.showType, CONST.PRODUCT)) {
					if (TextUtils.isEmpty(dto.dataUrl)) {//实况监测、天气预报、专业服务、灾情信息、天气会商
						intent = new Intent(mContext, ShawnProductActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else {//农业气象
						intent = new Intent(mContext, ShawnProductActivity2.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}
				}else if (TextUtils.equals(dto.showType, CONST.URL)) {
					intent = new Intent(mContext, ShawnNewsDetailActivity.class);

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
					intent = new Intent(mContext, ShawnWeatherInfoActivity.class);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.LOCAL)) {
					if (TextUtils.equals(dto.id, "1")) {//灾情信息
						intent = new Intent(mContext, ShawnDisasterSpecialActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "2")) {//预警信息
						intent = new Intent(mContext, ShawnWarningActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "3")) {//决策专报
						intent = new Intent(mContext, ShawnDecisionNewsActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "101")) {//站点检测
						intent = new Intent(mContext, ShawnFactActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "102")) {//中国大陆区域彩色云图
						intent = new Intent(mContext, ShawnNewsDetailActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, CONST.CLOUD_URL);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "103")) {//台风路径
						intent = new Intent(mContext, ShawnTyhpoonActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "104")) {//天气统计
						intent = new Intent(mContext, ShawnWeatherStaticsActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "105")) {//社会化观测
						intent = new Intent(mContext, ShawnSocietyObserveActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "106")) {//空气质量
						intent = new Intent(mContext, ShawnAirQualityActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "107")) {//视频会商
						intent = new Intent(mContext, ShawnWeatherMeetingActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "109")) {//天气图分析
						intent = new Intent(mContext, ShawnWeatherChartActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "110")) {//格点实况
                        intent = new Intent(mContext, ShawnPointFactActivity.class);
                        intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
                        startActivity(intent);
                    }else if (TextUtils.equals(dto.id, "111")) {//综合预报
						intent = new Intent(mContext, ShawnComForecastActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "112")) {//强对流天气实况（新）
						intent = new Intent(mContext, ShawnStreamFactActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "113")) {//产品定制
						intent = new Intent(mContext, ShawnProductOrderActivity2.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "114")) {//5天降水量统计
						intent = new Intent(mContext, ShawnFiveRainActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "201")) {//城市天气预报
						intent = new Intent(mContext, ShawnCityForecastActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "202")) {//分钟级降水估测
						intent = new Intent(mContext, ShawnMinuteFallActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "203")) {//等风来
						intent = new Intent(mContext, ShawnWaitWindActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, com.china.common.CONST.WAIT_WIND);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "204")) {//分钟降水与强对流
						intent = new Intent(mContext, ShawnStrongStreamActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "207")) {//格点预报
						intent = new Intent(mContext, ShawnPointForeActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "301")) {//灾情专报
						intent = new Intent(mContext, ShawnDisasterSpecialActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "302")) {//灾情直报
						intent = new Intent(mContext, ShawnDisasterReportActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "601")) {//视频直播
						intent = new Intent(mContext, ShawnWeatherMeetingActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "-1")) {
						Toast.makeText(mContext, "频道建设中", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	private void initListView() {
		final List<ShawnSettingDto> list = new ArrayList<>();
		ShawnSettingDto dto = new ShawnSettingDto();
		dto.setType(0);
		dto.setDrawable(R.drawable.shawn_icon_collection_gray);
		dto.setName("我的收藏");
		dto.setValue("");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(1);
		dto.setDrawable(R.drawable.shawn_icon_feedback);
		dto.setName("意见反馈");
		dto.setValue("");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(2);
		dto.setDrawable(R.drawable.shawn_icon_cache);
		dto.setName("清除缓存");
		dto.setValue(DataCleanManager.getCacheSize(this));
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(3);
		dto.setDrawable(R.drawable.shawn_icon_brief_intro);
		dto.setName("中国气象局简介");
		dto.setValue("");
		list.add(dto);

		if (TextUtils.equals(MyApplication.USERGROUP, "10") || TextUtils.equals(MyApplication.USERGROUP, "14")
				|| TextUtils.equals(MyApplication.USERGROUP, "20") || TextUtils.equals(MyApplication.USERGROUP, "52")) {
			dto = new ShawnSettingDto();
			dto.setType(4);
			dto.setDrawable(R.drawable.shawn_icon_weekly_statistic);
			dto.setName("周报统计");
			dto.setValue("");
			list.add(dto);
		}

		dto = new ShawnSettingDto();
		dto.setType(5);
		dto.setDrawable(R.drawable.shawn_icon_app_recommand);
		dto.setName("应用推荐");
		dto.setValue("");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(6);
		dto.setDrawable(R.drawable.shawn_icon_about);
		dto.setName("关于我们");
		dto.setValue("");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(7);
		dto.setDrawable(R.drawable.shawn_icon_control);
		dto.setName("模块管理");
		dto.setValue("");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(8);
		dto.setDrawable(R.drawable.shawn_icon_product);
		dto.setName("产品订阅");
		dto.setValue("");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(12);
		dto.setDrawable(R.drawable.shawn_icon_product);
		dto.setName("格点实况");
		dto.setValue("");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(9);
		dto.setDrawable(R.drawable.shawn_icon_connection);
		dto.setName("屏屏联动");
		dto.setValue("");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(10);
		dto.setDrawable(R.drawable.shawn_icon_hotline1);
		dto.setName("气象服务热线");
		dto.setValue("400-6000-121");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(11);
		dto.setDrawable(R.drawable.shawn_icon_hotline2);
		dto.setName("运行保障热线");
		dto.setValue("010-68408068");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(13);
		dto.setDrawable(R.drawable.shawn_icon_product);
		dto.setName("用户协议");
		dto.setValue("");
		list.add(dto);
		dto = new ShawnSettingDto();
		dto.setType(14);
		dto.setDrawable(R.drawable.shawn_icon_product);
		dto.setName("隐私政策");
		dto.setValue("");
		list.add(dto);

		ListView listView = findViewById(R.id.listView);
		sAdapter = new ShawnSettingAdapter(this, list);
		listView.setAdapter(sAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ShawnSettingDto data = list.get(position);
				Intent intent;
				switch (data.getType()) {
					case 0:
						intent = new Intent(mContext, ShawnCollectionActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						startActivity(intent);
						break;
					case 1:
						intent = new Intent(mContext, ShawnFeedbackActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						startActivity(intent);
						break;
					case 2:
						dialoaCache(true, getString(R.string.sure_delete_cache), data);
						break;
					case 3:
						intent = new Intent(mContext, ShawnNewsDetailActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						intent.putExtra(CONST.WEB_URL, "http://www.cma.gov.cn/2011zwxx/2011zbmgk/201110/t20111026_117793.html");
						startActivity(intent);
						break;
					case 4:
						intent = new Intent(mContext, WebviewActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						intent.putExtra(CONST.WEB_URL, CONST.COUNTURL);
						startActivity(intent);
						break;
					case 5:
						intent = new Intent(mContext, WebviewActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						intent.putExtra(CONST.WEB_URL, CONST.RECOMMENDURL);
						startActivity(intent);
						break;
					case 6:
						intent = new Intent(mContext, AboutActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						startActivity(intent);
						break;
					case 7:
						intent = new Intent(mContext, ShawnManageActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) intentList);
						intent.putExtras(bundle);
						startActivityForResult(intent, 1001);
						break;
					case 8:
						startActivity(new Intent(mContext, ShawnProductOrderActivity.class));
						break;
					case 9:
						startActivity(new Intent(mContext, ShawnConnectionActivity.class));
						break;
					case 10:
						dialogDial("气象服务热线\n"+data.getValue(), "拨打");
						break;
					case 11:
						dialogDial("运行保障热线\n"+data.getValue(), "拨打");
						break;
					case 12:

						break;
					case 13:
						intent = new Intent(mContext, WebviewActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, "用户协议");
						intent.putExtra(CONST.WEB_URL, "http://decision-admin.tianqi.cn/Public/share/chinaweather_links/yhxy.html");
						startActivity(intent);
						break;
					case 14:
						intent = new Intent(mContext, WebviewActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, "隐私政策");
						intent.putExtra(CONST.WEB_URL, "http://decision-admin.tianqi.cn/Public/share/chinaweather_links/yszc.html");
						startActivity(intent);
						break;
				}
			}
		});
	}

	/**
	 * 登出对话框
	 * @param content 内容
	 */
	private void dialogLogout(String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_dialog_cache, null);
		TextView tvContent = view.findViewById(R.id.tvContent);
		TextView tvNegtive = view.findViewById(R.id.tvNegtive);
		TextView tvPositive = view.findViewById(R.id.tvPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		tvNegtive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		tvPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				MyApplication.clearUserInfo(mContext);
				startActivity(new Intent(mContext, LoginActivity.class));
				finish();
			}
		});
	}

	/**
	 * 清除缓存对话框
	 * @param content 内容
	 * @param flag 0删除本地存储，1删除缓存
	 */
	private void dialoaCache(final boolean flag, String content, final ShawnSettingDto data) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_dialog_cache, null);
		TextView tvContent = view.findViewById(R.id.tvContent);
		TextView tvNegtive = view.findViewById(R.id.tvNegtive);
		TextView tvPositive = view.findViewById(R.id.tvPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvContent.setText(content);
		tvNegtive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		tvPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				if (flag) {
					DataCleanManager.clearCache(mContext);
					data.setValue(DataCleanManager.getCacheSize(mContext));
					if (sAdapter != null) {
						sAdapter.notifyDataSetChanged();
					}
				}else {
					DataCleanManager.clearLocalSave(mContext);
				}
			}
		});
	}

	/**
	 * 拨打电话对话框
	 * @param content
	 * @param positive
	 */
	private void dialogDial(final String content, String positive) {
		dialNumber = content;
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_dialog_cache, null);
		TextView tvContent = view.findViewById(R.id.tvContent);
		TextView tvNegtive = view.findViewById(R.id.tvNegtive);
		TextView tvPositive = view.findViewById(R.id.tvPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvPositive.setText(positive);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		tvNegtive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		tvPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				checkPhoneAuthority(dialNumber);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
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
		Intent intent;
		switch (v.getId()) {
			case R.id.ivSetting:
				if (drawerlayout.isDrawerOpen(reRight)) {
					drawerlayout.closeDrawer(reRight);
				}else {
					drawerlayout.openDrawer(reRight);
				}
				break;
			case R.id.tvLocation:
			case R.id.tvTime:
				intent = new Intent(mContext, ShawnForecastActivity.class);
				intent.putExtra("cityName", cityName);
				intent.putExtra("cityId", cityId);
				intent.putExtra("lat", locationLatLng.latitude);
				intent.putExtra("lng", locationLatLng.longitude);
				startActivity(intent);
				break;
			case R.id.ivAdd:
				intent = new Intent(mContext, ShawnReserveCityActivity.class);
				intent.putExtra("cityName", cityName);
				intent.putExtra("cityId", cityId);
				startActivity(intent);
				break;
			case R.id.tvFifteen:
				if (hScrollView2.getVisibility() == View.VISIBLE) {
					return;
				}
				tvFifteen.setTextColor(Color.WHITE);
				tvHour.setTextColor(0x60ffffff);
				reScrollView.setVisibility(View.GONE);
				hScrollView2.setVisibility(View.VISIBLE);
				break;
			case R.id.tvHour:
				if (hScrollView2.getVisibility() == View.GONE) {
					return;
				}
				tvFifteen.setTextColor(0x60ffffff);
				tvHour.setTextColor(Color.WHITE);
				reScrollView.setVisibility(View.VISIBLE);
				hScrollView2.setVisibility(View.GONE);
				break;
			case R.id.tvLogout:
				dialogLogout(getString(R.string.sure_logout));
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
				case 1001:
					if (data != null) {
						String columnIds = data.getStringExtra("columnIds");
						if (!TextUtils.isEmpty(columnIds)) {
							Log.e("columnIds", columnIds);
							MyApplication.saveColumnIds(this, columnIds);
							setGridViewData();
						}
					}
					break;
			}
		}
	}

    /**
     * 判断navigation是否显示，重新计算页面布局
     */
    private void onLayoutMeasure() {
        getDisplayWidthHeight();
        reTitle.measure(0, 0);
        int height1 = reTitle.getMeasuredHeight();
		reFact.measure(0, 0);
        int height2 = reFact.getMeasuredHeight();
        int height3 = 0;
        if (viewPager != null && pdfList.size() > 0) {
            viewPager.measure(0, 0);
            height3 = (int)(30*density);
        }
        if (mAdapter != null) {
            mAdapter.height = height-height1-height2-height3;
            mAdapter.notifyDataSetChanged();
        }
    }

	//需要申请的所有权限
	private String[] allPermissions = new String[] {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	//拒绝的权限集合
	private static List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkMultiAuthority() {
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
				ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_MULTI);
			}
		}
	}

	/**
	 * 申请电话权限
	 */
	private void checkPhoneAuthority(String dialNumber) {
		if (Build.VERSION.SDK_INT < 23) {
			try {
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+dialNumber)));
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}else {
			if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, AuthorityUtil.AUTHOR_PHONE);
			}else {
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+dialNumber)));
			}
		}
	}

	/**
	 * 申请存储权限
	 */
	private void checkStorageAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			try {
				if (!TextUtils.equals(MyApplication.USERGROUP, "17")) {//大众用户不使用自动更新，使用应用市场更新
					AutoUpdateUtil.checkUpdate(ShawnMainActivity.this, mContext, "52", getString(R.string.app_name), true);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}else {
			if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(ShawnMainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, AuthorityUtil.AUTHOR_STORAGE);
			}else {
				if (!TextUtils.equals(MyApplication.USERGROUP, "17")) {//大众用户不使用自动更新，使用应用市场更新
					AutoUpdateUtil.checkUpdate(ShawnMainActivity.this, mContext, "52", getString(R.string.app_name), true);
				}
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_MULTI:
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
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、设备信息权限、存储权限，是否前往设置？");
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、设备信息权限、存储权限，是否前往设置？");
							break;
						}
					}
				}
				break;
			case AuthorityUtil.AUTHOR_PHONE:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					try {
						startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+dialNumber)));
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}else {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用电话权限，是否前往设置？");
					}
				}
				break;
		}
	}

}
