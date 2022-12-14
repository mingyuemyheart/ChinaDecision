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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
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
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.china.R;
import com.china.adapter.MainAdapter;
import com.china.adapter.SettingAdapter;
import com.china.common.CONST;
import com.china.common.ColumnData;
import com.china.common.MyApplication;
import com.china.dto.NewsDto;
import com.china.dto.SettingDto;
import com.china.dto.WarningDto;
import com.china.dto.WeatherDto;
import com.china.fragment.PdfFragment;
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
import com.squareup.picasso.Picasso;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ?????????
 */
public class MainActivity extends BaseActivity implements OnClickListener, AMapLocationListener {
	
	private Context mContext;
	private TextView tvLocation,tvTime,tvTemperature,tvHumidity,tvWind,tvAqi,tvFifteen,tvHour;
	private ConstraintLayout clTitle,clFact;
	private LinearLayout llWarning,llContainer1,llContainer2,llContainer3;
	private ImageView ivBanner,ivAqi;
	private MyHorizontalScrollView hScrollView1,hScrollView2;
	private long mExitTime;//?????????????????????????????????long?????????
	private AMapLocationClientOption mLocationOption;//??????mLocationOption??????
	private AMapLocationClient mLocationClient;//??????AMapLocationClient?????????
	private LatLng locationLatLng = new LatLng(39.904030, 116.407526);
	private String cityName = "?????????",cityId = "101010100";
	private MainAdapter mAdapter;
	private List<ColumnData> dataList = new ArrayList<>();
	private List<ColumnData> intentList = new ArrayList<>();
	private int min = 0, index = 0;
	private HourView hourView;//?????????view
	private HourItemView hourItemView;
	private List<WeatherDto> tempList = new ArrayList<>();
	private VerticalSwipeRefreshLayout refreshLayout;//??????????????????
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	private MyBroadCastReceiver mReceiver;

	//??????pdf??????
	private List<NewsDto> pdfList = new ArrayList<>();
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private ImageView[] ivTips;//??????????????????
	private ViewGroup viewGroup;
	
	//????????????
	private DrawerLayout drawerlayout;
	private ConstraintLayout clRight;
	private String dialNumber = "";
	private SettingAdapter sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
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
		okHttpOutReport();
	}

    /**
	 * ???????????????????????????
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
	 * ???????????????
	 */
	private void initWidget() {
		checkStorageAuthority();
		clTitle = findViewById(R.id.clTitle);
		ImageView ivAdd = findViewById(R.id.ivAdd);
		ivAdd.setOnClickListener(this);
		tvLocation = findViewById(R.id.tvLocation);
		tvLocation.setOnClickListener(this);
		tvLocation.setText(cityName);
		ImageView ivSetting = findViewById(R.id.ivSetting);
		ivSetting.setOnClickListener(this);
		ivBanner = findViewById(R.id.ivBanner);
		ivBanner.setOnClickListener(this);
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
        hScrollView1 = findViewById(R.id.hScrollView1);
		hScrollView1.setScrollListener(scrollListener);
		llContainer1 = findViewById(R.id.llContainer1);
		hScrollView2 = findViewById(R.id.hScrollView2);
		llContainer2 = findViewById(R.id.llContainer2);
		clFact = findViewById(R.id.clFact);
		llContainer3 = findViewById(R.id.llContainer3);
		viewGroup = findViewById(R.id.viewGroup);

		if (!TextUtils.isEmpty(MyApplication.getTop_img())) {
			Picasso.get().load(MyApplication.getTop_img()).into(ivBanner);
			ivBanner.setVisibility(View.VISIBLE);
		} else {
			ivBanner.setVisibility(View.GONE);
		}

		//????????????
		drawerlayout = findViewById(R.id.drawerlayout);
		drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		clRight = findViewById(R.id.clRight);
		TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvLogout = findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);
		ImageView ivPortrait = findViewById(R.id.ivPortrait);
		ivPortrait.setOnClickListener(this);
		if (!TextUtils.isEmpty(MyApplication.PORTRAIT)) {
			Picasso.get().load(MyApplication.PORTRAIT).into(ivPortrait);
		}

		ViewGroup.LayoutParams params = clRight.getLayoutParams();
		params.width = CommonUtil.widthPixels(this)-(int)CommonUtil.dip2px(this, 50);
		clRight.setLayoutParams(params);

		if (!TextUtils.isEmpty(MyApplication.USERNAME)) {
			tvUserName.setText(MyApplication.USERNAME);
		}

		llWarning.setVisibility(View.INVISIBLE);
		startLocation();
	}

	/**
	 * ????????????
	 */
	private void startLocation() {
		if (mLocationOption == null) {
			mLocationOption = new AMapLocationClientOption();//?????????????????????
		}
		if (mLocationClient == null) {
			mLocationClient = new AMapLocationClient(mContext);//???????????????
		}
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//???????????????????????????????????????Battery_Saving?????????????????????Device_Sensors??????????????????
        mLocationOption.setNeedAddress(true);//????????????????????????????????????????????????????????????
        mLocationOption.setOnceLocation(true);//???????????????????????????,?????????false
        mLocationOption.setMockEnable(false);//??????????????????????????????,?????????false????????????????????????
        mLocationOption.setInterval(2000);//??????????????????,????????????,?????????2000ms
        mLocationClient.setLocationOption(mLocationOption);//??????????????????????????????????????????
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//????????????
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null && amapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
			cityName = amapLocation.getCity()+amapLocation.getDistrict()+amapLocation.getStreet()+amapLocation.getStreetNum();
			locationLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
			tvLocation.setText(cityName);
			OkHttpGeo(locationLatLng.longitude, locationLatLng.latitude, amapLocation.getAdCode());
			initViewPager();
        }
	}

	/**
	 * ??????????????????
	 */
	private void OkHttpGeo(final double lng, final double lat, final String adCode) {
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
										getWeatherInfo(adCode);
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

	private void getWeatherInfo(final String adCode) {
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

										//????????????
										if (!obj.isNull("l")) {
											JSONObject l = obj.getJSONObject("l");
											if (!l.isNull("l7")) {
												String time = l.getString("l7");
												if (time != null) {
													tvTime.setText(time+"??????");
												}
											}

											if (!l.isNull("l1")) {
												String factTemp = WeatherUtil.lastValue(l.getString("l1"));
												tvTemperature.setText(factTemp);
											}
											if (!l.isNull("l2")) {
												String humidity = WeatherUtil.lastValue(l.getString("l2"));
												if (TextUtils.isEmpty(humidity) || TextUtils.equals(humidity, "null")) {
													tvHumidity.setText("??????"+"--");
												}else {
													tvHumidity.setText("??????"+humidity+getString(R.string.unit_percent));
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
											if (MyApplication.FACTENABLE) {
												OkHttpFact();
											}
										}

										//aqi??????
										if (!obj.isNull("k")) {
											JSONObject k = obj.getJSONObject("k");
											if (!k.isNull("k3")) {
												String num = WeatherUtil.lastValue(k.getString("k3"));
												if (!TextUtils.isEmpty(num)) {
													tvAqi.setText("AQI "+WeatherUtil.getAqi(mContext, Integer.valueOf(num))+" "+num);
													if (TextUtils.equals("1", MyApplication.getAppTheme())) {
														ivAqi.setVisibility(View.GONE);
													} else {
														ivAqi.setImageResource(WeatherUtil.getAqiIcon(Integer.valueOf(num)));
													}
												}
											}
										}

										//?????????????????????
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
											hourView.setData(hourlyList, CommonUtil.widthPixels(mContext)*2/CommonUtil.density(mContext), MainActivity.this);
											llContainer1.removeAllViews();
											llContainer1.addView(hourView, (int)(CommonUtil.dip2px(mContext, CommonUtil.widthPixels(mContext)*2/CommonUtil.density(mContext))), (int)(CommonUtil.dip2px(mContext, 100)));
										}

										//15???????????????
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
												String week = CommonUtil.getWeek(i);//?????????
												String date = CommonUtil.getDate(f0, i);//??????

												//????????????
												JSONObject weeklyObj = f1.getJSONObject(i);
												//??????
												int lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
												int lowTemp = Integer.valueOf(weeklyObj.getString("fd"));

												//??????
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
														tvWeek.setText("??????");
													}else if (i == 1) {
														tvWeek.setText("??????");
													}else if (i == 2) {
														tvWeek.setText("??????");
													}else {
														tvWeek.setText(CommonUtil.getWeek(i-1));
													}
												}else {
													if (i == 0) {
														tvWeek.setText("??????");
													}else if (i == 1) {
														tvWeek.setText("??????");
													}else {
														tvWeek.setText(CommonUtil.getWeek(i));
													}
												}

												if (TextUtils.equals("1", MyApplication.getAppTheme())) {
													ivPheHigh.setImageBitmap(CommonUtil.grayScaleImage(WeatherUtil.getDayBitmap(mContext, highPheCode)));
													ivPheLow.setImageBitmap(CommonUtil.grayScaleImage(WeatherUtil.getNightBitmap(mContext, lowPheCode)));
												} else {
													ivPheHigh.setImageBitmap(WeatherUtil.getDayBitmap(mContext, highPheCode));
													ivPheLow.setImageBitmap(WeatherUtil.getNightBitmap(mContext, lowPheCode));
												}
												tvTemp.setText(highTemp+getString(R.string.unit_degree)+"/"+lowTemp+getString(R.string.unit_degree));
												llContainer2.addView(view);
											}
										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								refreshLayout.setRefreshing(false);
								clFact.setVisibility(View.VISIBLE);
								if (TextUtils.equals("1", MyApplication.getAppTheme())) {
									clFact.setBackgroundColor(Color.BLACK);
								}

							}
						});
					}
				});

				//??????????????????
				if (!TextUtils.isEmpty(adCode)) {
					OkHttpWarning(adCode);
				}

			}
		}).start();
	}

	private void OkHttpFact() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = "https://music.data.cma.cn/lsb/api?elements=TEM,PRE,RHU,WINS,WIND,WEA&interfaceId=getSurfEleInLocationByTime&lat="+locationLatLng.latitude+"&lon="+locationLatLng.longitude+"&apikey=AxEkluey201exDxyBoxUeYSw&nsukey=IGzynTgkKQ1Hfa3iJTwv4lci%2F%2F13c%2FQm3p83hih8xiri%2Bc5bm0ia85VASrEHrZRsgj6nlBF1U6F3m5PDkUd6oPtd7itR8p%2BwpJi7yIE%2FVcBsCwya6rhj%2BP%2BhBPCCyrb%2BsyYZLhRk5pkL73jJKE%2Ff4O7PWGPRwVtgQAqgFQ1XEXROJp7qMek79o6%2BiukbiCuY";
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
													tvHumidity.setText("?????? "+humidity + getString(R.string.unit_percent));
												}
												if (!itemObj.isNull("WIND")) {
													int windDir = (int)Float.parseFloat(itemObj.getString("WIND"));
													int windForce = (int)Float.parseFloat(itemObj.getString("WINS"));
													String dir = getString(WeatherUtil.getWindDirection(windDir));
													if (TextUtils.isEmpty(dir)) {
														dir = "???????????????";
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
	 * ??????????????????
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

										if (!TextUtils.isEmpty(dto.name) && !dto.name.contains("??????")) {
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
										if (TextUtils.equals("1", MyApplication.getAppTheme())) {
											ivWarning.setImageBitmap(CommonUtil.grayScaleImage(bitmap));
										} else {
											ivWarning.setImageBitmap(bitmap);
										}
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
	 * ?????????viewPager
	 */
	private void initViewPager() {
		mHandler.removeMessages(AUTO_PLUS);
		pdfList.clear();
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
		} else if (pdfList.size() == 1) {
			viewGroup.setVisibility(View.GONE);
		}
		viewPager.setSlipping(true);//??????ViewPager??????????????????
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

		onLayoutMeasure();
	}

	private final int AUTO_PLUS = 1001;
	private static final int PHOTO_CHANGE_TIME = 3000;//????????????
	private int index_plus = 0;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case AUTO_PLUS:
					viewPager.setCurrentItem(index_plus++);//?????????????????????????????????????????????
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
			if (!fragment.isAdded()) { // ??????fragment?????????added
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(fragment, fragment.getClass().getSimpleName());
				ft.commit();
				/**
				 * ??????FragmentTransaction.commit()????????????FragmentTransaction?????????
				 * ???????????????????????????,??????????????????????????????
				 * ????????????????????????????????????????????????,????????????????????????(???????????????????????????)???
				 * ???????????????,????????????????????????????????????????????????????????????????????????,???????????????????????????????????????????????????
				 */
				getFragmentManager().executePendingTransactions();
			}

			if (fragment.getView().getParent() == null) {
				container.addView(fragment.getView()); // ???viewpager????????????
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
			float itemWidth = CommonUtil.widthPixels(mContext)/tempList.size();
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
				if (columnIds.contains(dto.columnId)) {//????????????????????????
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
		mAdapter = new MainAdapter(mContext, dataList);
		gridView.setAdapter(mAdapter);
		onLayoutMeasure();
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = dataList.get(arg2);
				Intent intent;
				if (TextUtils.equals(dto.showType, CONST.PRODUCT)) {
					//????????????????????????????????????????????????????????????????????????
					intent = new Intent(mContext, ProductActivity.class);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", dto);
					intent.putExtras(bundle);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.URL)) {
					intent = new Intent(mContext, WebviewActivity.class);

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
				}else if (TextUtils.equals(dto.showType, CONST.NEWS)) {//????????????
					intent = new Intent(mContext, PdfTitleActivity.class);
					ArrayList<ColumnData> list = new ArrayList<>();
					ColumnData cd = new ColumnData();
					cd.columnId = dto.columnId;
					cd.name = dto.name;
					cd.dataUrl = dto.dataUrl;
					list.add(cd);
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList("dataList", list);
					intent.putExtras(bundle);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.LOCAL)) {
					if (TextUtils.equals(dto.id, "1") || TextUtils.equals(dto.id, "3") || TextUtils.equals(dto.id, "301")) {//????????????????????????????????????????????????
						intent = new Intent(mContext, PdfTitleActivity.class);
						ArrayList<ColumnData> list = new ArrayList<>();
						ColumnData cd = new ColumnData();
						cd.columnId = dto.columnId;
						cd.name = dto.name;
						cd.dataUrl = dto.dataUrl;
						list.add(cd);
						Bundle bundle = new Bundle();
						bundle.putParcelableArrayList("dataList", list);
						intent.putExtras(bundle);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "2")) {//????????????
						intent = new Intent(mContext, WarningActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "101")) {//????????????
						intent = new Intent(mContext, FactActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "102")) {//??????????????????????????????
						intent = new Intent(mContext, WebviewActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, CONST.CLOUD_URL);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "103")) {//????????????
						intent = new Intent(mContext, TyhpoonActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "104")) {//????????????
						intent = new Intent(mContext, WeatherStaticsActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "105")) {//???????????????
						intent = new Intent(mContext, SocietyObserveActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "106")) {//????????????
						intent = new Intent(mContext, AirQualityActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "107")) {//????????????
						intent = new Intent(mContext, WeatherMeetingActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "109")) {//???????????????
						intent = new Intent(mContext, WeatherChartActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "110")) {//????????????
                        intent = new Intent(mContext, PointFactActivity.class);
                        intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
                        startActivity(intent);
                    }else if (TextUtils.equals(dto.id, "111")) {//????????????
						intent = new Intent(mContext, ComForecastActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "112")) {//??????????????????????????????
						intent = new Intent(mContext, StreamFactActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "113")) {//????????????
						intent = new Intent(mContext, ProductCustomActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "114")) {//5??????????????????
						intent = new Intent(mContext, FiveRainActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "115")) {//??????????????????
						intent = new Intent(mContext, WeatherFactActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "201")) {//??????????????????
						intent = new Intent(mContext, CityForecastActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "205")) {//????????????????????????
						intent = new Intent(mContext, NationForecastActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "206")) {//?????????????????????
						intent = new Intent(mContext, HbhForecastActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "202")) {//?????????????????????
						intent = new Intent(mContext, MinuteFallActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "203")) {//?????????
						intent = new Intent(mContext, WaitWindActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, com.china.common.CONST.WAIT_WIND);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "204")) {//????????????????????????
						intent = new Intent(mContext, StrongStreamActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "207")) {//????????????
						intent = new Intent(mContext, PointForeActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "302")) {//????????????
						intent = new Intent(mContext, DisasterReportActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "601")) {//????????????
						intent = new Intent(mContext, WeatherMeetingActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "1205")) {//????????????
						intent = new Intent(mContext, BroadcastWeatherActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "-1")) {
						Toast.makeText(mContext, "???????????????", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	/**
	 * ?????????????????????
	 */
	private void okHttpOutReport() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = "http://decision-admin.tianqi.cn/Home/work2019/report_wbm?uid="+MyApplication.UID;
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								initListView("");
							}
						});
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
								initListView(result);
							}
						});
					}
				});
			}
		}).start();
	}

	private void initListView(String result) {
		final List<SettingDto> list = new ArrayList<>();
		SettingDto dto = new SettingDto();
		dto.setType(0);
		dto.setDrawable(R.drawable.shawn_icon_collection_gray);
		dto.setName("????????????");
		dto.setValue("");
		list.add(dto);
		dto = new SettingDto();
		dto.setType(1);
		dto.setDrawable(R.drawable.shawn_icon_feedback);
		dto.setName("????????????");
		dto.setValue("");
		list.add(dto);
		dto = new SettingDto();
		dto.setType(2);
		dto.setDrawable(R.drawable.shawn_icon_cache);
		dto.setName("????????????");
		dto.setValue(DataCleanManager.getCacheSize(this));
		list.add(dto);
		dto = new SettingDto();
		dto.setType(3);
		dto.setDrawable(R.drawable.shawn_icon_brief_intro);
		dto.setName("?????????????????????");
		dto.setValue("");
		list.add(dto);

		if (TextUtils.equals(MyApplication.USERGROUP, "10") || TextUtils.equals(MyApplication.USERGROUP, "14")
				|| TextUtils.equals(MyApplication.USERGROUP, "20") || TextUtils.equals(MyApplication.USERGROUP, "52")) {
			dto = new SettingDto();
			dto.setType(4);
			dto.setDrawable(R.drawable.shawn_icon_weekly_statistic);
			dto.setName("????????????");
			dto.setValue("");
			list.add(dto);
		}

		dto = new SettingDto();
		dto.setType(5);
		dto.setDrawable(R.drawable.shawn_icon_app_recommand);
		dto.setName("????????????");
		dto.setValue("");
		list.add(dto);
		dto = new SettingDto();
		dto.setType(6);
		dto.setDrawable(R.drawable.shawn_icon_about);
		dto.setName("????????????");
		dto.setValue("");
		list.add(dto);
		dto = new SettingDto();
		dto.setType(7);
		dto.setDrawable(R.drawable.shawn_icon_control);
		dto.setName("????????????");
		dto.setValue("");
		list.add(dto);
		dto = new SettingDto();
		dto.setType(8);
		dto.setDrawable(R.drawable.shawn_icon_product);
		dto.setName("????????????");
		dto.setValue("");
		list.add(dto);
		if (!TextUtils.isEmpty(result)) {
			try {
				final JSONObject obj = new JSONObject(result);
				if (!obj.isNull("code")) {
					String code = obj.getString("code");
					if (TextUtils.equals(code, "1")) {
						dto = new SettingDto();
						dto.setType(15);
						dto.setDrawable(R.drawable.shawn_icon_weekly_statistic);
						if (!obj.isNull("data")) {
							JSONObject itemObj = obj.getJSONObject("data");
							if (!itemObj.isNull("name")) {
								dto.setName(itemObj.getString("name"));
							}
							if (!itemObj.isNull("url")) {
								dto.setDataUrl(itemObj.getString("url"));
							}
						}
						list.add(dto);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		dto = new SettingDto();
		dto.setType(12);
		dto.setDrawable(R.drawable.shawn_icon_product);
		dto.setName("????????????");
		dto.setValue("");
		list.add(dto);
//		dto = new ShawnSettingDto();
//		dto.setType(9);
//		dto.setDrawable(R.drawable.shawn_icon_connection);
//		dto.setName("????????????");
//		dto.setValue("");
//		list.add(dto);
		dto = new SettingDto();
		dto.setType(10);
		dto.setDrawable(R.drawable.shawn_icon_hotline1);
		dto.setName("??????????????????");
		dto.setValue("400-6000-121");
		list.add(dto);
		dto = new SettingDto();
		dto.setType(11);
		dto.setDrawable(R.drawable.shawn_icon_hotline2);
		dto.setName("??????????????????");
		dto.setValue("010-68408068");
		list.add(dto);
		dto = new SettingDto();
		dto.setType(13);
		dto.setDrawable(R.drawable.shawn_icon_product);
		dto.setName("????????????");
		dto.setValue("");
		list.add(dto);
		dto = new SettingDto();
		dto.setType(14);
		dto.setDrawable(R.drawable.shawn_icon_product);
		dto.setName("????????????");
		dto.setValue("");
		list.add(dto);

		ListView listView = findViewById(R.id.listView);
		sAdapter = new SettingAdapter(this, list);
		listView.setAdapter(sAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SettingDto data = list.get(position);
				Intent intent;
				switch (data.getType()) {
					case 0:
						intent = new Intent(mContext, CollectionActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						startActivity(intent);
						break;
					case 1:
						intent = new Intent(mContext, FeedbackActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						startActivity(intent);
						break;
					case 2:
						dialoaCache(true, getString(R.string.sure_delete_cache), data);
						break;
					case 3:
						intent = new Intent(mContext, HtmlActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						intent.putExtra(CONST.WEB_URL, "http://www.cma.gov.cn/2011zwxx/2011zbmgk/201110/t20111026_117793.html");
						startActivity(intent);
						break;
					case 4:
						intent = new Intent(mContext, HtmlActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						intent.putExtra(CONST.WEB_URL, CONST.COUNTURL);
						startActivity(intent);
						break;
					case 5:
						intent = new Intent(mContext, HtmlActivity.class);
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
						intent = new Intent(mContext, ManageActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) intentList);
						intent.putExtras(bundle);
						startActivityForResult(intent, 1001);
						break;
					case 8:
						startActivity(new Intent(mContext, ProductOrderActivity.class));
						break;
					case 9:
						startActivity(new Intent(mContext, ConnectionActivity.class));
						break;
					case 10:
						dialogDial("??????????????????\n"+data.getValue(), "??????");
						break;
					case 11:
						dialogDial("??????????????????\n"+data.getValue(), "??????");
						break;
					case 12:

						break;
					case 13:
						intent = new Intent(mContext, HtmlActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, "????????????");
						intent.putExtra(CONST.WEB_URL, CONST.yhxy);
						startActivity(intent);
						break;
					case 14:
						intent = new Intent(mContext, HtmlActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, "????????????");
						intent.putExtra(CONST.WEB_URL, CONST.yszc);
						startActivity(intent);
						break;
					case 15:
						if (data.getDataUrl().endsWith(".pdf") || data.getDataUrl().endsWith(".PDF")) {
							intent = new Intent(mContext, PDFActivity.class);
						} else {
							intent = new Intent(mContext, WebviewActivity.class);
						}
						intent.putExtra(CONST.ACTIVITY_NAME, data.getName());
						intent.putExtra(CONST.WEB_URL, data.getDataUrl());
						startActivity(intent);
						break;
				}
			}
		});
	}

	/**
	 * ???????????????
	 * @param content ??????
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
	 * ?????????????????????
	 * @param content ??????
	 * @param flag 0?????????????????????1????????????
	 */
	private void dialoaCache(final boolean flag, String content, final SettingDto data) {
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
	 * ?????????????????????
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
    		if (drawerlayout != null && clRight != null) {
    			if (drawerlayout.isDrawerOpen(clRight)) {
    				drawerlayout.closeDrawer(clRight);
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
				if (drawerlayout.isDrawerOpen(clRight)) {
					drawerlayout.closeDrawer(clRight);
				}else {
					drawerlayout.openDrawer(clRight);
				}
				break;
			case R.id.tvLocation:
			case R.id.tvTime:
				intent = new Intent(mContext, ForecastActivity.class);
				intent.putExtra("cityName", cityName);
				intent.putExtra("cityId", cityId);
				intent.putExtra("lat", locationLatLng.latitude);
				intent.putExtra("lng", locationLatLng.longitude);
				startActivity(intent);
				break;
			case R.id.ivAdd:
				intent = new Intent(mContext, ReserveCityActivity.class);
				intent.putExtra("cityName", cityName);
				intent.putExtra("cityId", cityId);
				startActivity(intent);
				break;
			case R.id.ivBanner:
				if (TextUtils.isEmpty(MyApplication.getTop_img_title()) || TextUtils.isEmpty(MyApplication.getTop_img_url())) {
					return;
				}
				intent = new Intent(mContext, WebviewActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, MyApplication.getTop_img_title());
				intent.putExtra(CONST.WEB_URL, MyApplication.getTop_img_url());
				startActivity(intent);
				break;
			case R.id.tvFifteen:
				if (hScrollView2.getVisibility() == View.VISIBLE) {
					return;
				}
				tvFifteen.setTextColor(Color.WHITE);
				tvHour.setTextColor(0x60ffffff);
                hScrollView1.setVisibility(View.GONE);
                llContainer3.setVisibility(View.GONE);
				hScrollView2.setVisibility(View.VISIBLE);
				break;
			case R.id.tvHour:
				if (hScrollView2.getVisibility() == View.GONE) {
					return;
				}
				tvFifteen.setTextColor(0x60ffffff);
				tvHour.setTextColor(Color.WHITE);
                hScrollView1.setVisibility(View.VISIBLE);
                llContainer3.setVisibility(View.VISIBLE);
				hScrollView2.setVisibility(View.GONE);
				break;
			case R.id.tvLogout:
				dialogLogout(getString(R.string.sure_logout));
				break;
			case R.id.ivPortrait:
				startActivity(new Intent(mContext, PersonInfoActivity.class));
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
     * ??????navigation???????????????????????????????????????
     */
    private void onLayoutMeasure() {
        int statusBarHeight = CommonUtil.statusBarHeight(this);
        clTitle.measure(0, 0);
        int height1 = clTitle.getMeasuredHeight();
		clFact.measure(0, 0);
        int height2 = clFact.getMeasuredHeight();
        int height3 = 0;
        if (viewPager != null && pdfList.size() > 0) {
            viewPager.measure(0, 0);
            height3 = viewPager.getMeasuredHeight();
        }
        if (mAdapter != null) {
        	mAdapter.setHeight(CommonUtil.heightPixels(this)-height1-height2-height3-statusBarHeight);
            mAdapter.notifyDataSetChanged();
        }
    }

	//???????????????????????????
	private String[] allPermissions = new String[] {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	//?????????????????????
	private static List<String> deniedList = new ArrayList<>();
	/**
	 * ??????????????????
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
			if (deniedList.isEmpty()) {//?????????????????????
				init();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//???list????????????
				ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_MULTI);
			}
		}
	}

	/**
	 * ??????????????????
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
	 * ??????????????????
	 */
	private void checkStorageAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			try {
				if (!TextUtils.equals(MyApplication.USERGROUP, "17")) {//????????????????????????????????????????????????????????????
					AutoUpdateUtil.checkUpdate(MainActivity.this, mContext, "52", getString(R.string.app_name), true);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}else {
			if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, AuthorityUtil.AUTHOR_STORAGE);
			}else {
				if (!TextUtils.equals(MyApplication.USERGROUP, "17")) {//????????????????????????????????????????????????????????????
					AutoUpdateUtil.checkUpdate(MainActivity.this, mContext, "52", getString(R.string.app_name), true);
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
					boolean isAllGranted = true;//??????????????????
					for (int gResult : grantResults) {
						if (gResult != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//?????????????????????
						init();
					}else {//???????????????????????????????????????????????????????????????
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"??????????????????????????????????????????????????????????????????????????????????????????");
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"??????????????????????????????????????????????????????????????????????????????????????????");
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
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"????????????????????????????????????????????????");
					}
				}
				break;
		}
	}

}
