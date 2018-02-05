package com.china.activity;

/**
 * 天气统计
 */

import android.Manifest;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.china.dto.WeatherStaticsDto;
import com.china.manager.RainManager;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.view.CircularProgressBar;
import com.tendcloud.tenddata.TCAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class StaticsActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener, 
 OnMapClickListener, OnCameraChangeListener, OnMapScreenShotListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MapView mMapView = null;
	private AMap aMap = null;
	private List<WeatherStaticsDto> provinceList = new ArrayList<>();//省级
	private List<WeatherStaticsDto> cityList = new ArrayList<>();//市级
	private List<WeatherStaticsDto> districtList = new ArrayList<>();//县级
	private CircularProgressBar mCircularProgressBar1 = null;
	private CircularProgressBar mCircularProgressBar2 = null;
	private CircularProgressBar mCircularProgressBar3 = null;
	private CircularProgressBar mCircularProgressBar4 = null;
	private CircularProgressBar mCircularProgressBar5 = null;
	private TextView tvName = null;
	private TextView tvBar1 = null;
	private TextView tvBar2 = null;
	private TextView tvBar3 = null;
	private TextView tvBar4 = null;
	private TextView tvBar5 = null;
	private TextView tvDetail = null;
	private RelativeLayout reDetail = null;
	private RelativeLayout reContent = null;
	private ProgressBar progressBar = null;
	public final static String SANX_DATA_99 = "sanx_data_99";//加密秘钥名称
	public final static String APPID = "f63d329270a44900";//机密需要用到的AppId
	private float zoom = 3.7f;
	private boolean isClick = false;//判断是否点击
	private ImageView ivShare = null;
	private List<Marker> markerList = new ArrayList<>();
	private LatLng leftlatlng = null;
	private LatLng rightLatlng = null;
	private Bundle bundle = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statics);
		bundle = savedInstanceState;
		mContext = this;
		checkAuthority();
	}

	private void init() {
		showDialog();
		initWidget();
		initMap(bundle);
	}

	//需要申请的所有权限
	public static String[] allPermissions = new String[] {
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
			for (int i = 0; i < allPermissions.length; i++) {
				if (ContextCompat.checkSelfPermission(mContext, allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(allPermissions[i]);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				init();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				ActivityCompat.requestPermissions(StaticsActivity.this, permissions, AuthorityUtil.AUTHOR_STORAGE);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_STORAGE:
				if (grantResults.length > 0) {
					boolean isAllGranted = true;//是否全部授权
					for (int i = 0; i < grantResults.length; i++) {
						if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//所有权限都授予
						init();
					}else {//只要有一个没有授权，就提示进入设置界面设置
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用存储权限，是否前往设置？");
					}
				}else {
					for (int i = 0; i < permissions.length; i++) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(StaticsActivity.this, permissions[i])) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用存储权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvName = (TextView) findViewById(R.id.tvName);
		tvBar1 = (TextView) findViewById(R.id.tvBar1);
		tvBar2 = (TextView) findViewById(R.id.tvBar2);
		tvBar3 = (TextView) findViewById(R.id.tvBar3);
		tvBar4 = (TextView) findViewById(R.id.tvBar4);
		tvBar5 = (TextView) findViewById(R.id.tvBar5);
		tvDetail = (TextView) findViewById(R.id.tvDetail);
		mCircularProgressBar1 = (CircularProgressBar) findViewById(R.id.bar1);
		mCircularProgressBar2 = (CircularProgressBar) findViewById(R.id.bar2);
		mCircularProgressBar3 = (CircularProgressBar) findViewById(R.id.bar3);
		mCircularProgressBar4 = (CircularProgressBar) findViewById(R.id.bar4);
		mCircularProgressBar5 = (CircularProgressBar) findViewById(R.id.bar5);
		reDetail = (RelativeLayout) findViewById(R.id.reDetail);
		reContent = (RelativeLayout) findViewById(R.id.reContent);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	/**
	 * 初始化地图
	 */
	private void initMap(Bundle bundle) {
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setOnCameraChangeListener(this);

		OkHttpList();
	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);
		
		if (zoom == arg0.zoom && isClick == true) {//如果是地图缩放级别不变，并且点击就不错处理
			isClick = false;
			return;
		}
		
		zoom = arg0.zoom;
		removeMarkers();
		if (arg0.zoom <= 6.0f) {
			addMarker(provinceList);
		}else if (arg0.zoom > 6.0f && arg0.zoom <= 8.0f) {
			addMarker(provinceList);
			addMarker(cityList);
		}else if (arg0.zoom > 8.0f) {
			addMarker(provinceList);
			addMarker(cityList);
			addMarker(districtList);
		}
	}
	
	/**
	 * 加密请求字符串
	 * @return
	 */
	private String getSecretUrl() {
		String URL = "http://scapi.weather.com.cn/weather/stationinfo";//天气统计地址
		String sysdate = RainManager.getDate(Calendar.getInstance(), "yyyyMMddHHmm");//系统时间
		StringBuffer buffer = new StringBuffer();
		buffer.append(URL);
		buffer.append("?");
		buffer.append("date=").append(sysdate);
		buffer.append("&");
		buffer.append("appid=").append(APPID);
		
		String key = RainManager.getKey(SANX_DATA_99, buffer.toString());
		buffer.delete(buffer.lastIndexOf("&"), buffer.length());
		
		buffer.append("&");
		buffer.append("appid=").append(APPID.substring(0, 6));
		buffer.append("&");
		buffer.append("key=").append(key.substring(0, key.length() - 3));
		String result = buffer.toString();
		return result;
	}
	
	/**
	 * 获取天气统计数据
	 */
	private void OkHttpList() {
		OkHttpUtil.enqueue(new Request.Builder().url(getSecretUrl()).build(), new Callback() {
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
					parseStationInfo(result, "level1", provinceList);
					parseStationInfo(result, "level2", cityList);
					parseStationInfo(result, "level3", districtList);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							addMarker(provinceList);
							cancelDialog();
						}
					});
				}
			}
		});
	}
	
	/**
	 * 解析数据
	 */
	private void parseStationInfo(String result, String level, List<WeatherStaticsDto> list) {
		list.clear();
		try {
			JSONObject obj = new JSONObject(result);
			if (!obj.isNull(level)) {
				JSONArray array = new JSONArray(obj.getString(level));
				for (int i = 0; i < array.length(); i++) {
					WeatherStaticsDto dto = new WeatherStaticsDto();
					JSONObject itemObj = array.getJSONObject(i);
					if (!itemObj.isNull("name")) {
						dto.name = itemObj.getString("name");
					}
					if (!itemObj.isNull("stationid")) {
						dto.stationId = itemObj.getString("stationid");
					}
					if (!itemObj.isNull("level")) {
						dto.level = itemObj.getString("level");
					}
					if (!itemObj.isNull("areaid")) {
						dto.areaId = itemObj.getString("areaid");
					}
					if (!itemObj.isNull("lat")) {
						dto.latitude = itemObj.getString("lat");
					}
					if (!itemObj.isNull("lon")) {
						dto.longitude = itemObj.getString("lon");
					}
					list.add(dto);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 给marker添加文字
	 * @param name 城市名称
	 * @return
	 */
	private View getTextBitmap(String name) {      
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.layout_marker_statistic, null);
		if (view == null) {
			return null;
		}
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		if (!TextUtils.isEmpty(name) && name.length() > 2) {
			name = name.substring(0, 2)+"\n"+name.substring(2, name.length());
		}
		tvName.setText(name);
		return view;
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
	
	private void removeMarkers() {
		for (int i = 0; i < markerList.size(); i++) {
			Marker marker = markerList.get(i);
			markerColloseAnimation(marker);
			marker.remove();
		}
		markerList.clear();
	}
	
	/**
	 * 添加marker
	 */
	private void addMarker(List<WeatherStaticsDto> list) {
		if (list.isEmpty()) {
			return;
		}
		
		for (int i = 0; i < list.size(); i++) {
			WeatherStaticsDto dto = list.get(i);
			double lat = Double.valueOf(dto.latitude);
			double lng = Double.valueOf(dto.longitude);
			if (leftlatlng == null || rightLatlng == null) {
				MarkerOptions options = new MarkerOptions();
				options.title(list.get(i).areaId);
				options.anchor(0.5f, 0.5f);
				options.position(new LatLng(lat, lng));
				options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(list.get(i).name)));
				Marker marker = aMap.addMarker(options);
				markerList.add(marker);
				markerExpandAnimation(marker);
			}else {
				if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
					MarkerOptions options = new MarkerOptions();
					options.title(list.get(i).areaId);
					options.anchor(0.5f, 0.5f);
					options.position(new LatLng(lat, lng));
					options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(list.get(i).name)));
					Marker marker = aMap.addMarker(options);
					markerList.add(marker);
					markerExpandAnimation(marker);
				}
			}
		}
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		if (reDetail.getVisibility() == View.VISIBLE) {
			hideAnimation(reDetail);
		}
	}
	
	/**
	 * 向上弹出动画
	 * @param layout
	 */
	private void showAnimation(final View layout) {
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
		showAnimation(reDetail);
		isClick = true;
		String name = null;
		String areaId = null;
		String stationId = null;
		
		if (zoom <= 6.0f) {
			for (int i = 0; i < provinceList.size(); i++) {
				if (TextUtils.equals(marker.getTitle(), provinceList.get(i).areaId)) {
					areaId = provinceList.get(i).areaId;
					stationId = provinceList.get(i).stationId;
					name = provinceList.get(i).name;
					break;
				}
			}
		}else if (zoom > 6.0f && zoom <= 8.0f) {
			for (int i = 0; i < provinceList.size(); i++) {
				if (TextUtils.equals(marker.getTitle(), provinceList.get(i).areaId)) {
					areaId = provinceList.get(i).areaId;
					stationId = provinceList.get(i).stationId;
					name = provinceList.get(i).name;
					break;
				}
			}
			for (int i = 0; i < cityList.size(); i++) {
				if (TextUtils.equals(marker.getTitle(), cityList.get(i).areaId)) {
					areaId = cityList.get(i).areaId;
					stationId = cityList.get(i).stationId;
					name = cityList.get(i).name;
					break;
				}
			}
		}else if (zoom > 8.0f) {
			for (int i = 0; i < provinceList.size(); i++) {
				if (TextUtils.equals(marker.getTitle(), provinceList.get(i).areaId)) {
					areaId = provinceList.get(i).areaId;
					stationId = provinceList.get(i).stationId;
					name = provinceList.get(i).name;
					break;
				}
			}
			for (int i = 0; i < cityList.size(); i++) {
				if (TextUtils.equals(marker.getTitle(), cityList.get(i).areaId)) {
					areaId = cityList.get(i).areaId;
					stationId = cityList.get(i).stationId;
					name = cityList.get(i).name;
					break;
				}
			}
			for (int i = 0; i < districtList.size(); i++) {
				if (TextUtils.equals(marker.getTitle(), districtList.get(i).areaId)) {
					areaId = districtList.get(i).areaId;
					stationId = districtList.get(i).stationId;
					name = districtList.get(i).name;
					break;
				}
			}
		}
		
		tvName.setText(name + " " + stationId);
		tvDetail.setText("");
		progressBar.setVisibility(View.VISIBLE);
		reContent.setVisibility(View.INVISIBLE);
		
		OkHttpDetail(getSecretUrl2(stationId, areaId));
		return true;
	}

	/**
	 * 加密请求字符串
	 * @return
	 */
	private String getSecretUrl2(String stationid, String areaid) {
		String URL = "http://scapi.weather.com.cn/weather/historycount";
		String sysdate = RainManager.getDate(Calendar.getInstance(), "yyyyMMddHHmm");//系统时间
		StringBuffer buffer = new StringBuffer();
		buffer.append(URL);
		buffer.append("?");
		buffer.append("stationid=").append(stationid);
		buffer.append("&");
		buffer.append("areaid=").append(areaid);
		buffer.append("&");
		buffer.append("date=").append(sysdate);
		buffer.append("&");
		buffer.append("appid=").append(APPID);

		String key = RainManager.getKey(SANX_DATA_99, buffer.toString());
		buffer.delete(buffer.lastIndexOf("&"), buffer.length());

		buffer.append("&");
		buffer.append("appid=").append(APPID.substring(0, 6));
		buffer.append("&");
		buffer.append("key=").append(key.substring(0, key.length() - 3));
		String result = buffer.toString();
		return result;
	}

	private void OkHttpDetail(String url) {
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
								SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
								SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
								String startTime = sdf2.format(sdf.parse(obj.getString("starttime")));
								String endTime = sdf2.format(sdf.parse(obj.getString("endtime")));
								String no_rain_lx = obj.getInt("no_rain_lx")+"";//连续没雨天数
								if (TextUtils.equals(no_rain_lx, "-1")) {
									no_rain_lx = getString(R.string.no_statics);
								}else {
									no_rain_lx = no_rain_lx+"天";
								}
								String mai_lx = obj.getInt("mai_lx")+"";//连续霾天数
								if (TextUtils.equals(mai_lx, "-1")) {
									mai_lx = getString(R.string.no_statics);
								}else {
									mai_lx = mai_lx+"天";
								}
								String highTemp = null;//高温
								String lowTemp = null;//低温
								String highWind = null;//最大风速
								String highRain = null;//最大降水量

								if (!obj.isNull("count")) {
									JSONArray array = new JSONArray(obj.getString("count"));
									JSONObject itemObj0 = array.getJSONObject(0);//温度
									JSONObject itemObj1 = array.getJSONObject(1);//降水
									JSONObject itemObj5 = array.getJSONObject(5);//风速

									if (!itemObj0.isNull("max") && !itemObj0.isNull("min")) {
										highTemp = itemObj0.getString("max");
										if (TextUtils.equals(highTemp, "-1.0")) {
											highTemp = getString(R.string.no_statics);
										}else {
											highTemp = highTemp+"℃";
										}
										lowTemp = itemObj0.getString("min");
										if (TextUtils.equals(lowTemp, "-1.0")) {
											lowTemp = getString(R.string.no_statics);
										}else {
											lowTemp = lowTemp+"℃";
										}
									}
									if (!itemObj1.isNull("max")) {
										highRain = itemObj1.getString("max");
										if (TextUtils.equals(highRain, "-1.0")) {
											highRain = getString(R.string.no_statics);
										}else {
											highRain = highRain+"mm";
										}
									}
									if (!itemObj5.isNull("max")) {
										highWind = itemObj5.getString("max");
										if (TextUtils.equals(highWind, "-1.0")) {
											highWind = getString(R.string.no_statics);
										}else {
											highWind = highWind+"m/s";
										}
									}
								}

								if (startTime != null && endTime != null && highTemp != null && lowTemp != null && highWind != null && highRain != null) {
									StringBuffer buffer = new StringBuffer();
									buffer.append(getString(R.string.from)).append(startTime);
									buffer.append(getString(R.string.to)).append(endTime);
									buffer.append("：\n");
									buffer.append(getString(R.string.highest_temp)).append(highTemp).append("，");
									buffer.append(getString(R.string.lowest_temp)).append(lowTemp).append("，");
									buffer.append(getString(R.string.max_speed)).append(highWind).append("，");
									buffer.append(getString(R.string.max_fall)).append(highRain).append("，");
									buffer.append(getString(R.string.lx_no_fall)).append(no_rain_lx).append("，");
									buffer.append(getString(R.string.lx_no_mai)).append(mai_lx).append("。");

									SpannableStringBuilder builder = new SpannableStringBuilder(buffer.toString());
									ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.builder));
									ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.builder));
									ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(getResources().getColor(R.color.builder));
									ForegroundColorSpan builderSpan4 = new ForegroundColorSpan(getResources().getColor(R.color.builder));
									ForegroundColorSpan builderSpan5 = new ForegroundColorSpan(getResources().getColor(R.color.builder));
									ForegroundColorSpan builderSpan6 = new ForegroundColorSpan(getResources().getColor(R.color.builder));

									builder.setSpan(builderSpan1, 29, 29+highTemp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
									builder.setSpan(builderSpan2, 29+highTemp.length()+6, 29+highTemp.length()+6+lowTemp.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
									builder.setSpan(builderSpan3, 29+highTemp.length()+6+lowTemp.length()+6, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
									builder.setSpan(builderSpan4, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7+highRain.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
									builder.setSpan(builderSpan5, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7+highRain.length()+8, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7+highRain.length()+8+no_rain_lx.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
									builder.setSpan(builderSpan6, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7+highRain.length()+8+no_rain_lx.length()+6, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7+highRain.length()+8+no_rain_lx.length()+6+mai_lx.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
									tvDetail.setText(builder);

									long start = sdf2.parse(startTime).getTime();
									long end = sdf2.parse(endTime).getTime();
									float dayCount = (float) ((end - start) / (1000*60*60*24)) + 1;
									if (!obj.isNull("tqxxcount")) {
										JSONArray array = new JSONArray(obj.getString("tqxxcount"));
										for (int i = 0; i < array.length(); i++) {
											JSONObject itemObj = array.getJSONObject(i);
											String name = itemObj.getString("name");
											int value = itemObj.getInt("value");

											if (i == 0) {
												if (value == -1) {
													tvBar1.setText(name + "\n" + "--");
													animate(mCircularProgressBar1, null, 0, 1000);
													mCircularProgressBar1.setProgress(0);
												}else {
													tvBar1.setText(name + "\n" + value + "天");
													animate(mCircularProgressBar1, null, -value/dayCount, 1000);
													mCircularProgressBar1.setProgress(-value/dayCount);
												}
											}else if (i == 1) {
												if (value == -1) {
													tvBar2.setText(name + "\n" + "--");
													animate(mCircularProgressBar2, null, 0, 1000);
													mCircularProgressBar2.setProgress(0);
												}else {
													tvBar2.setText(name + "\n" + value + "天");
													animate(mCircularProgressBar2, null, -value/dayCount, 1000);
													mCircularProgressBar2.setProgress(-value/dayCount);
												}
											}else if (i == 2) {
												if (value == -1) {
													tvBar3.setText(name + "\n" + "--");
													animate(mCircularProgressBar3, null, 0, 1000);
													mCircularProgressBar3.setProgress(0);
												}else {
													tvBar3.setText(name + "\n" + value + "天");
													animate(mCircularProgressBar3, null, -value/dayCount, 1000);
													mCircularProgressBar3.setProgress(-value/dayCount);
												}
											}else if (i == 3) {
												if (value == -1) {
													tvBar4.setText(name + "\n" + "--");
													animate(mCircularProgressBar4, null, 0, 1000);
													mCircularProgressBar4.setProgress(0);
												}else {
													tvBar4.setText(name + "\n" + value + "天");
													animate(mCircularProgressBar4, null, -value/dayCount, 1000);
													mCircularProgressBar4.setProgress(-value/dayCount);
												}
											}else if (i == 4) {
												if (value == -1) {
													tvBar5.setText(name + "\n" + "--");
													animate(mCircularProgressBar5, null, 0, 1000);
													mCircularProgressBar5.setProgress(0);
												}else {
													tvBar5.setText(name + "\n" + value + "天");
													animate(mCircularProgressBar5, null, -value/dayCount, 1000);
													mCircularProgressBar5.setProgress(-value/dayCount);
												}
											}
										}
									}
								}
								progressBar.setVisibility(View.INVISIBLE);
								reContent.setVisibility(View.VISIBLE);
							} catch (JSONException e) {
								e.printStackTrace();
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		});
	}
	
	/**
	 * 进度条动画
	 * @param progressBar
	 * @param listener
	 * @param progress
	 * @param duration
	 */
	private void animate(final CircularProgressBar progressBar, final AnimatorListener listener,final float progress, final int duration) {
		ObjectAnimator mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
		mProgressBarAnimator.setDuration(duration);
		mProgressBarAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(final Animator animation) {
			}

			@Override
			public void onAnimationEnd(final Animator animation) {
				progressBar.setProgress(progress);
			}

			@Override
			public void onAnimationRepeat(final Animator animation) {
			}

			@Override
			public void onAnimationStart(final Animator animation) {
			}
		});
		if (listener != null) {
			mProgressBarAnimator.addListener(listener);
		}
		mProgressBarAnimator.reverse();
		mProgressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				progressBar.setProgress((Float) animation.getAnimatedValue());
			}
		});
//		progressBar.setMarkerProgress(0f);
		mProgressBarAnimator.start();
	}
	
	@Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		Bitmap bitmap = null;
		Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
//		Bitmap bitmap4 = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable-hdpi/iv_share_bottom.png"));
		if (reDetail.getVisibility() == View.VISIBLE) {
			//bitmap2为覆盖再地图上的view
			Bitmap bitmap2 = CommonUtil.captureView(reDetail);
			//bitmap3为bitmap1+bitmap2覆盖叠加在一起的view
			Bitmap bitmap3 = CommonUtil.mergeBitmap(StaticsActivity.this, bitmap1, bitmap2, true);
			CommonUtil.clearBitmap(bitmap2);
			bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
			CommonUtil.clearBitmap(bitmap3);
		}else {
			bitmap = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap4, false);
		}
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap4);
		CommonUtil.share(StaticsActivity.this, bitmap);
	}

	@Override
	public void onMapScreenShot(Bitmap arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (reDetail.getVisibility() == View.VISIBLE) {
				hideAnimation(reDetail);
				return false;
			} else {
				setBackEmit();
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (reDetail.getVisibility() == View.VISIBLE) {
				hideAnimation(reDetail);
			} else {
				setBackEmit();
				finish();
			}
			break;
		case R.id.ivShare:
			aMap.getMapScreenShot(StaticsActivity.this);
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

}
