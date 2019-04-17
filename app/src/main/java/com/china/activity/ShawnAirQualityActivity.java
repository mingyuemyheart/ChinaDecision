package com.china.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.china.R;
import com.china.common.CONST;
import com.china.dto.AirQualityDto;
import com.china.dto.AqiDto;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.SecretUrlUtil;
import com.china.view.AqiQualityView;
import com.tendcloud.tenddata.TCAgent;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 空气质量
 */
public class ShawnAirQualityActivity extends ShawnBaseActivity implements OnClickListener, OnMarkerClickListener,
OnMapClickListener, OnCameraChangeListener, OnMapScreenShotListener{
	
	private Context mContext;
	private TextView tvTitle,tvName,tvTime,tvAqiCount,tvAqi,tvPrompt,tvRank,tvPm2_5,tvPm10,tvCity;
	private RelativeLayout reContent,reLegend,reTop;
	private LinearLayout llCity,llContainer;
	private ImageView ivExpand;
	private MapView mMapView;
	private AMap aMap;
	private List<AirQualityDto> level1List = new ArrayList<>();
	private List<AirQualityDto> level2List = new ArrayList<>();
	private List<AirQualityDto> level3List = new ArrayList<>();
	private Map<String, AirQualityDto> areaIdMap = new LinkedHashMap<>();//按区域id区分
	private Map<String, Marker> markerMap = new LinkedHashMap<>();//按区域id区分
	private float zoom = 3.7f, zoom1 = 6.5f, zoom2 = 8.5f;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.CHINA);
	private HorizontalScrollView hScrollView;
	private List<AqiDto> aqiList = new ArrayList<>();
	private List<AqiDto> factAqiList = new ArrayList<>();//实况aqi数据
	private List<AqiDto> foreAqiList = new ArrayList<>();//预报aqi数据
	private int maxAqi = 0, minAqi = 0;
	private String aqiDate;
	private Configuration configuration;
	private LatLng leftlatlng = new LatLng(-16.305714763804854,75.13831436634065);
	private LatLng rightLatlng = new LatLng(63.681687310440864,135.21788656711578);
	private Marker clickMarker;
	private final String level1 = "level1", level2 = "level2", level3 = "level3";
	private AVLoadingIndicatorView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_air_quality);
		mContext = this;
		initMap(savedInstanceState);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		loadingView = findViewById(R.id.loadingView);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		tvTitle = findViewById(R.id.tvTitle);
		tvName = findViewById(R.id.tvName);
		tvTime = findViewById(R.id.tvTime);
		tvAqiCount = findViewById(R.id.tvAqiCount);
		tvAqi = findViewById(R.id.tvAqi);
		tvPrompt = findViewById(R.id.tvPrompt);
		tvRank = findViewById(R.id.tvRank);
		tvPm2_5 = findViewById(R.id.tvPm2_5);
		tvPm10 = findViewById(R.id.tvPm10);
		reContent = findViewById(R.id.reContent);
		reTop = findViewById(R.id.reTop);
		llContainer = findViewById(R.id.llContainer);
		reLegend = findViewById(R.id.reLegend);
		llCity = findViewById(R.id.llCity);
		tvCity = findViewById(R.id.tvCity);
		hScrollView = findViewById(R.id.hScrollView);
		ivExpand = findViewById(R.id.ivExpand);
		ivExpand.setOnClickListener(this);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		configuration = getResources().getConfiguration();

		OkHttpRank();

		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	/**
	 * 初始化地图
	 */
	private void initMap(Bundle bundle) {
		mMapView = findViewById(R.id.mapView);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setOnCameraChangeListener(this);
	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
	}
	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		if (reContent.getVisibility() == View.VISIBLE) {
			hideAnimation(reContent);
		}

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);
		
		zoom = arg0.zoom;
		switchMarkers();
		addMarkers();
	}

	/**
	 * 获取空气质量排行
	 */
	private void OkHttpRank() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.airpollution()).build(), new Callback() {
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
							level1List.clear();
							level2List.clear();
							level3List.clear();
							parseStationInfo(result, level1);
							parseStationInfo(result, level2);
							parseStationInfo(result, level3);
							addMarkers();

							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									loadingView.setVisibility(View.GONE);
								}
							});
						}
					}
				});
			}
		}).start();
	}
	
	/**
	 * 解析数据
	 */
	private void parseStationInfo(String result, String level) {
		try {
			JSONObject obj = new JSONObject(result);
			if (!obj.isNull("data")) {
				String time = obj.getString("time");
				JSONObject dataObj = obj.getJSONObject("data");
				if (!dataObj.isNull(level)) {
					JSONArray array = new JSONArray(dataObj.getString(level));
					for (int i = 0; i < array.length(); i++) {
						AirQualityDto dto = new AirQualityDto();
						JSONObject itemObj = array.getJSONObject(i);
						if (!itemObj.isNull("name")) {
							dto.name = itemObj.getString("name");
						}
						if (!itemObj.isNull("level")) {
							dto.level = itemObj.getString("level");
						}
						if (!itemObj.isNull("areaid")) {
							dto.areaId = itemObj.getString("areaid");
						}
						if (!itemObj.isNull("lat")) {
							dto.lat = itemObj.getDouble("lat");
						}
						if (!itemObj.isNull("lon")) {
							dto.lng = itemObj.getDouble("lon");
						}
						if (!itemObj.isNull("aqi")) {
							dto.aqi = itemObj.getString("aqi");
						}
						if (!itemObj.isNull("pm10")) {
							dto.pm10 = itemObj.getString("pm10");
						}
						if (!itemObj.isNull("pm2_5")) {
							dto.pm2_5 = itemObj.getString("pm2_5");
						}
						if (!itemObj.isNull("rank")) {
							dto.rank = itemObj.getInt("rank");
						}
						dto.time = time;

						areaIdMap.put(dto.areaId, dto);

						if (TextUtils.equals(level, level1)) {
							level1List.add(dto);
						}else if (TextUtils.equals(level, level2)) {
							level2List.add(dto);
						}else if (TextUtils.equals(level, level3)) {
							level3List.add(dto);
						}

					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void switchMarkers() {
	    new Thread(new Runnable() {
			@Override
			public void run() {
				for (String areaId : markerMap.keySet()) {
					if (!TextUtils.isEmpty(areaId) && markerMap.containsKey(areaId)) {
						Marker marker = markerMap.get(areaId);
						String level = marker.getSnippet();
						double lat = marker.getPosition().latitude;
						double lng = marker.getPosition().longitude;
						if (zoom <= zoom1) {
							if (TextUtils.equals(level, "2") || TextUtils.equals(level, "3")) {
								marker.remove();
							}
						}else if (zoom > zoom1 && zoom <= zoom2) {
							if (TextUtils.equals(level, "3")) {
								marker.remove();
							}
						}else {

						}
						if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
							//已经在可是范围内则不处理
						}else {
							marker.remove();
						}
					}
				}
			}
		}).start();
	}

	/**
	 * 添加marker
	 */
	private void addMarkers() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<AirQualityDto> list = new ArrayList<>();
				if (zoom <= zoom1) {
					list.addAll(level1List);
				}else if (zoom > zoom1 && zoom <= zoom2) {
					list.addAll(level1List);
					list.addAll(level2List);
				}else {
					list.addAll(level1List);
					list.addAll(level2List);
					list.addAll(level3List);
				}

				Map<String, Marker> markers = new LinkedHashMap<>();//为防止ConcurrentModificationException异常，待循环执行完毕后，再对markerMap进行修改
				for (AirQualityDto dto : list) {
					if (markerMap.containsKey(dto.areaId)) {
						Marker m = markerMap.get(dto.areaId);
						if (m != null && m.isVisible()) {
							//已经在可是区域添加过了，就不重复绘制了
						}else {
							addVisibleAreaMarker(dto, markers);
						}
					}else {
						addVisibleAreaMarker(dto, markers);
					}
				}
				markerMap.putAll(markers);
			}
		}).start();
	}

	/**
	 * 添加可视区域对应的marker
	 * @param dto
	 */
	private void addVisibleAreaMarker(AirQualityDto dto, Map<String, Marker> markers) {
		if (dto.lat > leftlatlng.latitude && dto.lat < rightLatlng.latitude && dto.lng > leftlatlng.longitude && dto.lng < rightLatlng.longitude) {
			MarkerOptions options = new MarkerOptions();
			options.title(dto.areaId);
			options.snippet(dto.level);
			options.anchor(0.5f, 1.0f);
			options.position(new LatLng(dto.lat, dto.lng));
			options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.name, dto.aqi)));
			Marker marker = aMap.addMarker(options);
			markers.put(dto.areaId, marker);
			markerExpandAnimation(marker);
		}
	}

	/**
	 * 给marker添加文字
	 * @param name 城市名称
	 * @return
	 */
	private View getTextBitmap(String name, String aqi) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_air_marker_icon, null);
		if (view == null) {
			return null;
		}
		TextView tvName = view.findViewById(R.id.tvName);
		ImageView icon = view.findViewById(R.id.icon);
		if (!TextUtils.isEmpty(name) && name.length() > 2) {
			name = name.substring(0, 2)+"\n"+name.substring(2, name.length());
		}
		tvName.setText(name);
		int value = Integer.valueOf(aqi);
		icon.setImageResource(getAirBackgroud(value));
		return view;
	}

	/**
	 * 根据aqi数据获取相对应的marker图标
	 * @param value
	 * @return
	 */
	private int getAirBackgroud(int value) {
		int drawable = -1;
		if (value >= 0 && value <= 50) {
			drawable = R.drawable.iv_air1;
		}else if (value >= 51 && value <= 100) {
			drawable = R.drawable.iv_air2;
		}else if (value >= 101 && value <= 150) {
			drawable = R.drawable.iv_air3;
		}else if (value >= 151 && value <= 200) {
			drawable = R.drawable.iv_air4;
		}else if (value >= 201 && value <= 300) {
			drawable = R.drawable.iv_air5;
		}else if (value >= 301) {
			drawable = R.drawable.iv_air6;
		}
		return drawable;
	}

	private void markerExpandAnimation(Marker marker) {
		ScaleAnimation animation = new ScaleAnimation(0,1,0,1);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(300);
		marker.setAnimation(animation);
		marker.startAnimation();
	}

	private void markerColloseAnimation(Marker marker) {
		ScaleAnimation animation = new ScaleAnimation(1,0,1,0);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(300);
		marker.setAnimation(animation);
		marker.startAnimation();
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		hideAnimation(reContent);
	}
	
	/**
	 * 根据aqi值获取aqi的描述（优、良等）
	 * @param value
	 * @return
	 */
	private String getAqiDes(int value) {
		String aqi = null;
		if (value >= 0 && value <= 50) {
			aqi = getString(R.string.aqi_level1);
		}else if (value >= 51 && value < 100) {
			aqi = getString(R.string.aqi_level2);
		}else if (value >= 101 && value < 150) {
			aqi = getString(R.string.aqi_level3);
		}else if (value >= 151 && value < 200) {
			aqi = getString(R.string.aqi_level4);
		}else if (value >= 201 && value < 300) {
			aqi = getString(R.string.aqi_level5);
		}else if (value >= 301) {
			aqi = getString(R.string.aqi_level6);
		}
		return aqi;
	}
	
	/**
	 * 根据aqi值获取aqi的提示信息
	 * @param value
	 * @return
	 */
	private String getPrompt(int value) {
		String aqi = null;
		if (value >= 0 && value <= 50) {
			aqi = getString(R.string.aqi1_text);
		}else if (value >= 51 && value < 100) {
			aqi = getString(R.string.aqi2_text);
		}else if (value >= 101 && value < 150) {
			aqi = getString(R.string.aqi3_text);
		}else if (value >= 151 && value < 200) {
			aqi = getString(R.string.aqi4_text);
		}else if (value >= 201 && value < 300) {
			aqi = getString(R.string.aqi5_text);
		}else if (value >= 301) {
			aqi = getString(R.string.aqi6_text);
		}
		return aqi;
	}
	
	/**
	 * 根据aqi数据获取相对应的背景图标
	 * @param value
	 * @return
	 */
	private int getCicleBackground(int value) {
		int drawable = -1;
		if (value >= 0 && value <= 50) {
			drawable = R.drawable.circle_aqi_one;
		}else if (value >= 51 && value < 100) {
			drawable = R.drawable.circle_aqi_two;
		}else if (value >= 101 && value < 150) {
			drawable = R.drawable.circle_aqi_three;
		}else if (value >= 151 && value < 200) {
			drawable = R.drawable.circle_aqi_four;
		}else if (value >= 201 && value < 300) {
			drawable = R.drawable.circle_aqi_five;
		}else if (value >= 301) {
			drawable = R.drawable.circle_aqi_six;
		}
		return drawable;
	}
	
	/**
	 * 根据aqi数据获取相对应的背景图标
	 * @param value
	 * @return
	 */
	private int getCornerBackground(int value) {
		int drawable = -1;
		if (value >= 0 && value <= 50) {
			drawable = R.drawable.corner_aqi_one;
		}else if (value >= 51 && value < 100) {
			drawable = R.drawable.corner_aqi_two;
		}else if (value >= 101 && value < 150) {
			drawable = R.drawable.corner_aqi_three;
		}else if (value >= 151 && value < 200) {
			drawable = R.drawable.corner_aqi_four;
		}else if (value >= 201 && value < 300) {
			drawable = R.drawable.corner_aqi_five;
		}else if (value >= 301) {
			drawable = R.drawable.corner_aqi_six;
		}
		return drawable;
	}
	
	/**
	 * 向上弹出动画
	 * @param layout
	 */
	private void showAnimation(final View layout) {
		if (layout.getVisibility() == View.VISIBLE) {
			return;
		}
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 1f, 
				TranslateAnimation.RELATIVE_TO_SELF, 0);
		animation.setDuration(300);
		layout.startAnimation(animation);
		layout.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 向下隐藏动画
	 * @param layout
	 */
	private void hideAnimation(final View layout) {
		if (layout.getVisibility() == View.GONE) {
			return;
		}
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 1f);
		animation.setDuration(300);
		layout.startAnimation(animation);
		layout.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		clickMarker = marker;
		if (clickMarker != null) {
			checkAuthority();
		}
		return true;
	}

	private void clickMarker() {
		if (reContent.getVisibility() != View.VISIBLE) {
			showAnimation(reContent);
		}
		if (clickMarker == null) {
			return;
		}
		if (areaIdMap.containsKey(clickMarker.getTitle())) {
			AirQualityDto dto = areaIdMap.get(clickMarker.getTitle());
			if (dto != null) {
				tvName.setText(dto.name);
				tvCity.setText(dto.name+"空气质量指数（AQI）");
				tvAqiCount.setText(dto.aqi);
				int value = Integer.valueOf(dto.aqi);
				tvAqi.setText(getAqiDes(value));
				tvAqi.setBackgroundResource(getCornerBackground(value));
				if (value > 150) {
					tvAqi.setTextColor(getResources().getColor(R.color.white));
				}else {
					tvAqi.setTextColor(getResources().getColor(R.color.black));
				}
				tvPrompt.setText("温馨提示："+getPrompt(value));
				tvRank.setBackgroundResource(getCicleBackground(value));
				tvRank.setText(dto.rank+"");
				tvPm2_5.setBackgroundResource(getCicleBackground(value));
				tvPm2_5.setText(dto.pm2_5+"\n"+"ug/m³");
                tvPm10.setBackgroundResource(getCicleBackground(value));
				tvPm10.setText(dto.pm10+"\n"+"ug/m³");
				if (!TextUtils.isEmpty(dto.time)) {
					try {
						tvTime.setText(sdf2.format(sdf1.parse(dto.time)) + getString(R.string.update));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}

		llContainer.removeAllViews();
		double lat = clickMarker.getPosition().latitude;
		double lng = clickMarker.getPosition().longitude;
		getWeatherInfo(clickMarker.getTitle(), lat, lng);
	}
	
	/**
	 * 获取实况信息、预报信息
	 */
	private void getWeatherInfo(final String cityId, final double lat, final double lng) {
		if (TextUtils.isEmpty(cityId)) {
			return;
		}
		loadingView.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
					@Override
					public void onComplete(final Weather content) {
						super.onComplete(content);
						aqiList.clear();
						if (content != null) {
							//空气质量
							try {
								JSONObject obj = content.getAirQualityInfo();
								if (!obj.isNull("k3")) {
									String[] array = obj.getString("k3").split("\\|");
									factAqiList.clear();
									for (int i = 0; i < array.length; i++) {
										AqiDto data = new AqiDto();
										if (!TextUtils.isEmpty(array[i]) && !TextUtils.equals(array[i], "?")) {
											if (i == array.length-1) {
												data.aqi = tvAqiCount.getText().toString();
											}else {
												data.aqi = array[i];
											}
											factAqiList.add(data);
										}
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}

						OkHttpXiangJiAqi(lng, lat);
					}

					@Override
					public void onError(Throwable error, String content) {
						super.onError(error, content);
					}
				});
			}
		}).start();
	}
	
	/**
	 * 请求象辑aqi
	 */
	private void OkHttpXiangJiAqi(final double lng, final double lat) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.airForecast(lng, lat)).build(), new Callback() {
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
										if (!obj.isNull("reqTime")) {
											aqiDate = obj.getString("reqTime");
										}

										if (!obj.isNull("series")) {
											aqiList.clear();
											JSONArray array = obj.getJSONArray("series");
											foreAqiList.clear();
											for (int i = 0; i < array.length(); i++) {
												AqiDto data = new AqiDto();
												data.aqi = String.valueOf(array.get(i));
												foreAqiList.add(data);
											}
											aqiList.addAll(factAqiList);
											aqiList.addAll(foreAqiList);
										}

										if (aqiList.size() > 0) {
											try {
												if (!TextUtils.isEmpty(aqiList.get(0).aqi)) {
													maxAqi = Integer.valueOf(aqiList.get(0).aqi);
													minAqi = Integer.valueOf(aqiList.get(0).aqi);
													for (int i = 0; i < aqiList.size(); i++) {
														if (!TextUtils.isEmpty(aqiList.get(i).aqi)) {
															if (maxAqi <= Integer.valueOf(aqiList.get(i).aqi)) {
																maxAqi = Integer.valueOf(aqiList.get(i).aqi);
															}
															if (minAqi >= Integer.valueOf(aqiList.get(i).aqi)) {
																minAqi = Integer.valueOf(aqiList.get(i).aqi);
															}
														}
													}
													maxAqi = maxAqi + (50 - maxAqi%50);

													if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
														setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
														showPortrait();
														ivExpand.setImageResource(R.drawable.shawn_icon_expand);
													}else {
														setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
														showLandscape();
														ivExpand.setImageResource(R.drawable.iv_collose);
													}

												}
											} catch (ArrayIndexOutOfBoundsException e) {
												e.printStackTrace();
											}
										}
									} catch (JSONException e1) {
										e1.printStackTrace();
									}
								}

								loadingView.setVisibility(View.GONE);
							}
						});
					}
				});
			}
		}).start();
	}
	
	private void showPortrait() {
		ivExpand.setVisibility(View.VISIBLE);
		reTop.setVisibility(View.VISIBLE);
		llCity.setVisibility(View.GONE);
		AqiQualityView aqiView = new AqiQualityView(mContext);
		aqiView.setData(aqiList, aqiDate);
		int viewHeight = (int)(CommonUtil.dip2px(mContext, 180));
//		if (maxAqi <= 100) {
//			viewHeight = (int)(CommonUtil.dip2px(mContext, 150));
//		}else if (maxAqi > 100 && maxAqi <= 150) {
//			viewHeight = (int)(CommonUtil.dip2px(mContext, 200));
//		}else if (maxAqi > 150) {
//			viewHeight = (int)(CommonUtil.dip2px(mContext, 250));
//		}
		final DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		llContainer.removeAllViews();
		llContainer.addView(aqiView, dm.widthPixels*4, viewHeight);
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				hScrollView.scrollTo(dm.widthPixels*3/2, hScrollView.getHeight());
			}
		});
	}
	
	private void showLandscape() {
		ivExpand.setVisibility(View.VISIBLE);
		reTop.setVisibility(View.GONE);
		llCity.setVisibility(View.VISIBLE);
		AqiQualityView aqiView = new AqiQualityView(mContext);
		aqiView.setData(aqiList, aqiDate);
		final DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		llContainer.removeAllViews();
		llContainer.addView(aqiView, dm.widthPixels*2, LinearLayout.LayoutParams.MATCH_PARENT);
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				hScrollView.scrollTo(dm.widthPixels*2/4, hScrollView.getHeight());
			}
		});
	}
	
	@Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		Bitmap bitmap;
		Bitmap bitmap8 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
		if (reContent.getVisibility() == View.VISIBLE) {
			Bitmap bitmap2 = CommonUtil.captureView(reTop);
			Bitmap bitmap3 = CommonUtil.captureView(llContainer);
			Bitmap bitmap4 = CommonUtil.mergeBitmap(mContext, bitmap2, bitmap3, false);
			CommonUtil.clearBitmap(bitmap2);
			CommonUtil.clearBitmap(bitmap3);
			Bitmap bitmap5 = CommonUtil.mergeBitmap(ShawnAirQualityActivity.this, bitmap1, bitmap4, false);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap4);
			bitmap = CommonUtil.mergeBitmap(mContext, bitmap5, bitmap8, false);
			CommonUtil.clearBitmap(bitmap5);
			CommonUtil.clearBitmap(bitmap8);
		}else {
			Bitmap bitmap2 = CommonUtil.captureView(reLegend);
			Bitmap bitmap3 = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap2, true);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap2);
			bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap8, false);
			CommonUtil.clearBitmap(bitmap3);
			CommonUtil.clearBitmap(bitmap8);
		}
		CommonUtil.share(ShawnAirQualityActivity.this, bitmap);
	}

	@Override
	public void onMapScreenShot(Bitmap arg0, int arg1) {
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				if (reContent.getVisibility() == View.VISIBLE) {
					hideAnimation(reContent);
					llCity.setVisibility(View.GONE);
					return false;
				} else {
					setBackEmit();
					finish();
				}
			}else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				showPortrait();
				ivExpand.setImageResource(R.drawable.shawn_icon_expand);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				if (reContent.getVisibility() == View.VISIBLE) {
					hideAnimation(reContent);
					llCity.setVisibility(View.GONE);
				} else {
					setBackEmit();
					finish();
				}
			}else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				showPortrait();
				ivExpand.setImageResource(R.drawable.shawn_icon_expand);
			}
			break;
		case R.id.ivShare:
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				aMap.getMapScreenShot(ShawnAirQualityActivity.this);
			}else {
				if (reContent.getVisibility() == View.VISIBLE) {
					Bitmap bitmap1 = CommonUtil.captureView(llCity);
					Bitmap bitmap2 = CommonUtil.captureView(llContainer);
					Bitmap bitmap3 = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap2, false);
					CommonUtil.clearBitmap(bitmap1);
					CommonUtil.clearBitmap(bitmap2);
					Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
					Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
					CommonUtil.clearBitmap(bitmap3);
					CommonUtil.clearBitmap(bitmap4);
					CommonUtil.share(ShawnAirQualityActivity.this, bitmap);
				}else {
					aMap.getMapScreenShot(ShawnAirQualityActivity.this);
				}
			}
			break;
		case R.id.ivExpand:
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				showLandscape();
				ivExpand.setImageResource(R.drawable.iv_collose);
			}else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				showPortrait();
				ivExpand.setImageResource(R.drawable.shawn_icon_expand);
			}
			break;

		default:
			break;
		}
	}
	
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (tvTitle != null) {
			TCAgent.onPageStart(mContext, tvTitle.getText().toString());
		}
		if (mMapView != null) {
			mMapView.onResume();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (tvTitle != null) {
			TCAgent.onPageEnd(mContext, tvTitle.getText().toString());
		}
		if (mMapView != null) {
			mMapView.onPause();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mMapView != null) {
			mMapView.onSaveInstanceState(outState);
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
	}

	//需要申请的所有权限
	private String[] allPermissions = new String[] {
			Manifest.permission.CALL_PHONE,
	};

	//拒绝的权限集合
	private List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			clickMarker();
		}else {
			deniedList.clear();
			for (String permission : allPermissions) {
				if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(permission);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				clickMarker();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				ActivityCompat.requestPermissions(ShawnAirQualityActivity.this, permissions, AuthorityUtil.AUTHOR_PHONE);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_PHONE:
				if (grantResults.length > 0) {
					boolean isAllGranted = true;//是否全部授权
					for (int gResult : grantResults) {
						if (gResult != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//所有权限都授予
						clickMarker();
					}else {//只要有一个没有授权，就提示进入设置界面设置
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用电话权限，是否前往设置？");
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(ShawnAirQualityActivity.this, permission)) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用电话权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}

}
