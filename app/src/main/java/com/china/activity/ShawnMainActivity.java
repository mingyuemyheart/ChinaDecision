package com.china.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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
import com.china.common.CONST;
import com.china.common.ColumnData;
import com.china.common.MyApplication;
import com.china.dto.NewsDto;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class ShawnMainActivity extends BaseActivity implements OnClickListener, AMapLocationListener, MyApplication.NavigationListener{
	
	private Context mContext;
	private TextView tvLocation,tvTime,tvTemperature,tvHumidity,tvWind,tvQuality,tvFifteen,tvHour;
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
	private ArrayList<ColumnData> channelList = new ArrayList<>();
	private int width = 0, height = 0, min = 0, index = 0;
	private float density = 0;
	private HourView hourView;//逐小时view
	private HourItemView hourItemView;
	private List<WeatherDto> tempList = new ArrayList<>();
	private VerticalSwipeRefreshLayout refreshLayout;//下拉刷新布局

	//首页pdf文档
	private List<NewsDto> pdfList = new ArrayList<>();
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	
	//侧拉页面
	private DrawerLayout drawerlayout;
	private RelativeLayout reRight;
	private TextView tvCache;
	private TextView tvHotline1,tvHotline2;
	private String dialNumber = "";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_main);
        mContext = this;
		initRefreshLayout();
		initWidget();
		initViewPager();
		initGridView();
		MyApplication.setNavigationListener(this);
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
				refresh();
			}
		});
	}
    
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		AutoUpdateUtil.checkUpdate(ShawnMainActivity.this, mContext, "52", getString(R.string.app_name), true);
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
		tvQuality = findViewById(R.id.tvQuality);
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

		//侧拉页面
		drawerlayout = findViewById(R.id.drawerlayout);
		drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		reRight = findViewById(R.id.reRight);
		TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvLogout = findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);
		LinearLayout llSave = findViewById(R.id.llSave);
		llSave.setOnClickListener(this);
		LinearLayout llClearCache = findViewById(R.id.llClearCache);
		llClearCache.setOnClickListener(this);
		tvCache = findViewById(R.id.tvCache);
		LinearLayout llVersion = findViewById(R.id.llVersion);
		llVersion.setOnClickListener(this);
        LinearLayout llIntro = findViewById(R.id.llIntro);
		llIntro.setOnClickListener(this);
        LinearLayout llHotline1 = findViewById(R.id.llHotline1);
		llHotline1.setOnClickListener(this);
		tvHotline1 = findViewById(R.id.tvHotline1);
        LinearLayout llHotline2 = findViewById(R.id.llHotline2);
		llHotline2.setOnClickListener(this);
		tvHotline2 = findViewById(R.id.tvHotline2);
        LinearLayout llFeedBack = findViewById(R.id.llFeedBack);
		llFeedBack.setOnClickListener(this);
        LinearLayout llStatistic = findViewById(R.id.llStatistic);
		llStatistic.setOnClickListener(this);
		TextView statisticDivider = findViewById(R.id.statisticDivider);
        LinearLayout llRecommend = findViewById(R.id.llRecommend);
		llRecommend.setOnClickListener(this);
        LinearLayout llScreen = findViewById(R.id.llScreen);
		llScreen.setOnClickListener(this);

		getDisplayWidthHeight();

		ViewGroup.LayoutParams params = reRight.getLayoutParams();
		params.width = width-150;
		reRight.setLayoutParams(params);
		
		if (TextUtils.equals(MyApplication.USERGROUP, "10") || TextUtils.equals(MyApplication.USERGROUP, "14")
				|| TextUtils.equals(MyApplication.USERGROUP, "20") || TextUtils.equals(MyApplication.USERGROUP, "52")) {
			llStatistic.setVisibility(View.VISIBLE);
			statisticDivider.setVisibility(View.VISIBLE);
		}else {
			llStatistic.setVisibility(View.GONE);
			statisticDivider.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(MyApplication.USERNAME)) {
			tvUserName.setText(MyApplication.USERNAME);
		}
		
		refresh();
	}

	private void getDisplayWidthHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        density = dm.density;
    }
	
	private void refresh() {
		try {
			String cache = DataCleanManager.getCacheSize(mContext);
			tvCache.setText(cache);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		llWarning.setVisibility(View.INVISIBLE);
		checkAuthority();
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
		if (amapLocation != null && amapLocation.getErrorCode() == 0) {
			String position = amapLocation.getCity()+amapLocation.getDistrict()+amapLocation.getStreet()+amapLocation.getStreetNum();
			locationLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
			cityName = position;
			tvLocation.setText(cityName);
			OkHttpGeo(locationLatLng.longitude, locationLatLng.latitude);
        }
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

										//实况信息
										if (!obj.isNull("l")) {
											JSONObject l = obj.getJSONObject("l");
											if (!l.isNull("l7")) {
												String time = l.getString("l7");
												if (time != null) {
													tvTime.setText(time+"发布");
												}
											}
											if (!l.isNull("l1")) {
												String factTemp = WeatherUtil.lastValue(l.getString("l1"));
												if (!TextUtils.isEmpty(factTemp)) {
													tvTemperature.setText(factTemp);
												}
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
													tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) +
															WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
												}
											}
										}

										//aqi信息
										if (!obj.isNull("k")) {
											JSONObject k = obj.getJSONObject("k");
											if (!k.isNull("k3")) {
												String num = WeatherUtil.lastValue(k.getString("k3"));
												if (!TextUtils.isEmpty(num)) {
													tvQuality.setText("AQI "+WeatherUtil.getAqi(mContext, Integer.valueOf(num))+" "+num);
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
												if (i == 0) {
													tvWeek.setText("今天");
												}else if (i == 1) {
													tvWeek.setText("明天");
												}else {
													tvWeek.setText(week);
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

					@Override
					public void onError(Throwable error, String content) {
						super.onError(error, content);
					}
				});

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//获取预警信息
						String warningId = queryWarningIdByCityId(cityId);
						if (!TextUtils.isEmpty(warningId)) {
							OkHttpWarning(warningId);
						}
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
		llWarning.removeAllViews();
		final String url = "http://decision-admin.tianqi.cn/Home/extra/getwarns?order=0&areaid="+warningId.substring(0, 4);
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
													if (TextUtils.equals(item0, warningId) || TextUtils.equals(item0, warningId.substring(0,4)+"00"))
													warningList.add(dto);
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
														Intent intent = new Intent(mContext, ShawnWarningDetailActivity.class);
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
		}).start();
	}

	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		pdfList.clear();
		pdfList.addAll(getIntent().getExtras().<NewsDto>getParcelableArrayList("pdfList"));
		for (int i = 0; i < pdfList.size(); i++) {
			Fragment fragment = new PdfFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable("data", pdfList.get(i));
			fragment.setArguments(bundle);
			fragments.add(fragment);
		}

		viewPager = findViewById(R.id.viewPager);
		if (pdfList.size() == 0) {
			viewPager.setVisibility(View.GONE);
		}
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setAdapter(new MyPagerAdapter());

		if (fragments.size() > 1) {
			mHandler.sendEmptyMessageDelayed(AUTO_PLUS, PHOTO_CHANGE_TIME);
		}
	}

	private final int AUTO_PLUS = 1001;
	private static final int PHOTO_CHANGE_TIME = 2000;//定时变量
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

	private void initGridView() {
		channelList.clear();
		if (!getIntent().hasExtra("dataList")) {
			return;
		}
		List<ColumnData> dataList = getIntent().getExtras().getParcelableArrayList("dataList");
		if (dataList == null || dataList.size() <= 0) {
			return;
		}
		channelList.addAll(dataList);
        ScrollviewGridview gridView = findViewById(R.id.gridView);
		mAdapter = new ShawnMainAdapter(mContext, channelList);
		gridView.setAdapter(mAdapter);
		onLayoutMeasure();
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = channelList.get(arg2);
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
						intent = new Intent(mContext, DisasterSpecialActivity.class);
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
						intent = new Intent(mContext, DecisionNewsActivity.class);
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
						intent = new Intent(mContext, WeatherMeetingActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "109")) {//天气图分析
						intent = new Intent(mContext, ShawnWeatherChartActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "110")) {//格点实况
                        intent = new Intent(mContext, PointFactActivity.class);
                        intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
                        startActivity(intent);
                    }else if (TextUtils.equals(dto.id, "111")) {//综合预报
						intent = new Intent(mContext, ComprehensiveForecastActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "112")) {//强对流天气实况（新）
						intent = new Intent(mContext, StreamFactActivity.class);
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
						intent = new Intent(mContext, StrongStreamActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "207")) {//格点预报
						intent = new Intent(mContext, ShawnPointForeActivity.class);
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
				startActivity(new Intent(mContext, ShawnLoginActivity.class));
				finish();
			}
		});
	}

	/**
	 * 清除缓存对话框
	 * @param content 内容
	 * @param flag 0删除本地存储，1删除缓存
	 */
	private void dialoaCache(final boolean flag, String content, final TextView textView) {
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
					try {
						textView.setText(DataCleanManager.getCacheSize(mContext));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
//					ChannelsManager.clearData(mContext);//清除保存在本地的频道数据
					DataCleanManager.clearLocalSave(mContext);
					try {
						textView.setText(DataCleanManager.getLocalSaveSize(mContext));
					} catch (Exception e) {
						e.printStackTrace();
					}
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
		case R.id.llSave:
			intent = new Intent(mContext, ShawnCollectionActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_save));
			startActivity(intent);
			break;
		case R.id.llClearCache:
			dialoaCache(true, getString(R.string.sure_delete_cache), tvCache);
			break;
		case R.id.llVersion:
			intent = new Intent(mContext, ShawnAboutActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, "关于我们");
			startActivity(intent);
			break;
		case R.id.tvLogout:
			dialogLogout(getString(R.string.sure_logout));
			break;
		case R.id.llIntro:
			intent = new Intent(mContext, ShawnNewsDetailActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, "中国气象局简介");
			intent.putExtra(CONST.WEB_URL, "http://www.cma.gov.cn/2011zwxx/2011zbmgk/201110/t20111026_117793.html");
			startActivity(intent);
			break;
		case R.id.llFeedBack:
			intent = new Intent(mContext, ShawnFeedbackActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_feedback));
			intent.putExtra(CONST.INTENT_APPID, com.china.common.CONST.APPID);
			startActivity(intent);
			break;
		case R.id.llHotline1:
			dialogDial(getString(R.string.setting_hotline1)+"\n"+tvHotline1.getText().toString(), getString(R.string.dial));
			break;
		case R.id.llHotline2:
			dialogDial(getString(R.string.setting_hotline2)+"\n"+tvHotline2.getText().toString(), getString(R.string.dial));
			break;
		case R.id.llStatistic:
			intent = new Intent(mContext, ShawnWebviewActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, "周报统计");
			intent.putExtra(CONST.WEB_URL, CONST.COUNTURL);
			startActivity(intent);
			break;
		case R.id.llRecommend:
			intent = new Intent(mContext, ShawnWebviewActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, "应用推荐");
			intent.putExtra(CONST.WEB_URL, CONST.RECOMMENDURL);
			startActivity(intent);
			break;
		case R.id.llScreen:
			startActivity(new Intent(mContext, ConnectionActivity.class));
			break;

		default:
			break;
		}
	}

    @Override
    public void showNavigation(boolean show) {
        onLayoutMeasure();
    }

    /**
     * 判断navigation是否显示，重新计算页面布局
     */
    private void onLayoutMeasure() {
        getDisplayWidthHeight();

        int statusBarHeight = -1;//状态栏高度
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
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
            mAdapter.height = height-statusBarHeight-height1-height2-height3;
            mAdapter.notifyDataSetChanged();
        }
    }

	//需要申请的所有权限
	private String[] allPermissions = new String[] {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.CALL_PHONE
	};

	//拒绝的权限集合
	public static List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			if (CommonUtil.isLocationOpen(mContext)) {
				startLocation();
			}else {
				getWeatherInfo();
			}
		}else {
			deniedList.clear();
			for (String permission : allPermissions) {
				if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(permission);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				if (CommonUtil.isLocationOpen(mContext)) {
					startLocation();
				}else {
					getWeatherInfo();
				}
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				ActivityCompat.requestPermissions(ShawnMainActivity.this, permissions, AuthorityUtil.AUTHOR_LOCATION);
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
				ActivityCompat.requestPermissions(ShawnMainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, AuthorityUtil.AUTHOR_PHONE);
			}else {
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+dialNumber)));
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
						if (CommonUtil.isLocationOpen(mContext)) {
							startLocation();
						}else {
							getWeatherInfo();
						}
					}else {//只要有一个没有授权，就提示进入设置界面设置
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、电话权限，是否前往设置？");
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(ShawnMainActivity.this, permission)) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、电话权限，是否前往设置？");
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
					if (!ActivityCompat.shouldShowRequestPermissionRationale(ShawnMainActivity.this, Manifest.permission.CALL_PHONE)) {
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用电话权限，是否前往设置？");
					}
				}
				break;
		}
	}

}
