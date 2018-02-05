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
import com.china.dto.AirPolutionDto;
import com.china.dto.AqiDto;
import com.china.manager.RainManager;
import com.china.manager.XiangJiManager;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.view.AqiQualityView;
import com.tendcloud.tenddata.TCAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
 * 空气污染
 * @author shawn_sun
 *
 */

public class AirPolutionActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener,
OnMapClickListener, OnCameraChangeListener, OnMapScreenShotListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView ivShare = null;
	private MapView mMapView = null;
	private AMap aMap = null;
	private List<AirPolutionDto> provinceList = new ArrayList<>();//省级
	private List<AirPolutionDto> cityList = new ArrayList<>();//市级
	private List<AirPolutionDto> districtList = new ArrayList<>();//县级
	private TextView tvName = null;
	private TextView tvTime = null;
	private TextView tvAqiCount = null;
	private TextView tvAqi = null;
	private TextView tvPrompt = null;
	private RelativeLayout reRank = null;
	private TextView tvRank = null;
	private RelativeLayout rePm2_5 = null;
	private TextView tvPm2_5 = null;
	private RelativeLayout rePm10 = null;
	private TextView tvPm10 = null;
	private RelativeLayout reContent = null;
	private RelativeLayout reLegend = null;
	private LinearLayout llTop = null;
	private LinearLayout llCity = null;
	private TextView tvCity = null;
	public final static String SANX_DATA_99 = "sanx_data_99";//加密秘钥名称
	public final static String APPID = "f63d329270a44900";//机密需要用到的AppId
	private float zoom = 3.7f;
	private boolean isClick = false;//判断是否点击
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHH");
	private HorizontalScrollView hScrollView = null;
	private LinearLayout llContainer = null;
	private List<AqiDto> aqiList = new ArrayList<>();
	private List<AqiDto> factAqiList = new ArrayList<>();//实况aqi数据
	private List<AqiDto> foreAqiList = new ArrayList<>();//预报aqi数据
	private int maxAqi = 0, minAqi = 0;
	private String aqiDate = null;
	private ImageView ivExpand = null;
	private Configuration configuration = null;
	private List<Marker> markerList = new ArrayList<>();
	private LatLng leftlatlng = null;
	private LatLng rightLatlng = null;
	private Marker clickMarker = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_air_polution);
		mContext = this;
		showDialog();
		initMap(savedInstanceState);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvName = (TextView) findViewById(R.id.tvName);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvAqiCount = (TextView) findViewById(R.id.tvAqiCount);
		tvAqi = (TextView) findViewById(R.id.tvAqi);
		tvPrompt = (TextView) findViewById(R.id.tvPrompt);
		reRank = (RelativeLayout) findViewById(R.id.reRank);
		tvRank = (TextView) findViewById(R.id.tvRank);
		rePm2_5 = (RelativeLayout) findViewById(R.id.rePm2_5);
		tvPm2_5 = (TextView) findViewById(R.id.tvPm2_5);
		rePm10 = (RelativeLayout) findViewById(R.id.rePm10);
		tvPm10 = (TextView) findViewById(R.id.tvPm10);
		reContent = (RelativeLayout) findViewById(R.id.reContent);
		llTop = (LinearLayout) findViewById(R.id.llTop);
		llContainer = (LinearLayout) findViewById(R.id.llContainer);
		reLegend = (RelativeLayout) findViewById(R.id.reLegend);
		llCity = (LinearLayout) findViewById(R.id.llCity);
		tvCity = (TextView) findViewById(R.id.tvCity);
		hScrollView = (HorizontalScrollView) findViewById(R.id.hScrollView);
		ivExpand = (ImageView) findViewById(R.id.ivExpand);
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
		mMapView = (MapView) findViewById(R.id.map);
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
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);
		
		if (zoom == arg0.zoom && isClick == true) {//如果是地图缩放级别不变，并且点击就不做处理
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
		String URL = "http://scapi.weather.com.cn/weather/getaqiobserve";//空气污染
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
	 * 获取空气质量排行
	 */
	private void OkHttpRank() {
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
				if (result != null) {
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
	private void parseStationInfo(String result, String level, List<AirPolutionDto> list) {
		list.clear();
		try {
			JSONObject obj = new JSONObject(result.toString());
			if (!obj.isNull("data")) {
				String time = obj.getString("time");
				JSONObject dataObj = obj.getJSONObject("data");
				if (!dataObj.isNull(level)) {
					JSONArray array = new JSONArray(dataObj.getString(level));
					for (int i = 0; i < array.length(); i++) {
						AirPolutionDto dto = new AirPolutionDto();
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
							dto.latitude = itemObj.getString("lat");
						}
						if (!itemObj.isNull("lon")) {
							dto.longitude = itemObj.getString("lon");
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
						
						list.add(dto);
					}
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
	private View getTextBitmap(String name, String aqi) {      
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.airpolution_item, null);
		if (view == null) {
			return null;
		}
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		if (!TextUtils.isEmpty(name) && name.length() > 2) {
			name = name.substring(0, 2)+"\n"+name.substring(2, name.length());
		}
		tvName.setText(name);
		int value = Integer.valueOf(aqi);
		icon.setImageResource(getMarker(value));
		return view;
	}
	
	/**
	 * 根据aqi数据获取相对应的marker图标
	 * @param value
	 * @return
	 */
	private int getMarker(int value) {
		int drawable = -1;
		if (value >= 0 && value <= 50) {
			drawable = R.drawable.iv_air1;
		}else if (value >= 51 && value < 100) {
			drawable = R.drawable.iv_air2;
		}else if (value >= 101 && value < 150) {
			drawable = R.drawable.iv_air3;
		}else if (value >= 151 && value < 200) {
			drawable = R.drawable.iv_air4;
		}else if (value >= 201 && value < 300) {
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
	private void addMarker(List<AirPolutionDto> list) {
		if (list.isEmpty()) {
			return;
		}
		
		for (int i = 0; i < list.size(); i++) {
			AirPolutionDto dto = list.get(i);
			double lat = Double.valueOf(dto.latitude);
			double lng = Double.valueOf(dto.longitude);
			if (leftlatlng == null || rightLatlng == null) {
				MarkerOptions options = new MarkerOptions();
				options.title(list.get(i).areaId);
				options.anchor(0.5f, 0.5f);
				options.position(new LatLng(lat, lng));
				options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.name, dto.aqi)));
				Marker marker = aMap.addMarker(options);
				markerList.add(marker);
				markerExpandAnimation(marker);
			}else {
				if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
					MarkerOptions options = new MarkerOptions();
					options.title(list.get(i).areaId);
					options.anchor(0.5f, 0.5f);
					options.position(new LatLng(lat, lng));
					options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.name, dto.aqi)));
					Marker marker = aMap.addMarker(options);
					markerList.add(marker);
					markerExpandAnimation(marker);
				}
			}
		}
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
	
	private void setValue(String areaId, List<AirPolutionDto> list) {
		for (int i = 0; i < list.size(); i++) {
			if (TextUtils.equals(areaId, list.get(i).areaId)) {
				tvName.setText(list.get(i).name);
				tvCity.setText(list.get(i).name+"空气质量指数（AQI）");
				tvAqiCount.setText(list.get(i).aqi);
				int value = Integer.valueOf(list.get(i).aqi);
				tvAqi.setText(getAqiDes(value));
				tvAqi.setBackgroundResource(getCornerBackground(value));
				if (value > 150) {
					tvAqi.setTextColor(getResources().getColor(R.color.white));
				}else {
					tvAqi.setTextColor(getResources().getColor(R.color.black));
				}
				tvPrompt.setText(getString(R.string.likely_prompt)+getPrompt(value));
				reRank.setBackgroundResource(getCicleBackground(value));
				tvRank.setText(list.get(i).rank+"");
				rePm2_5.setBackgroundResource(getCicleBackground(value));
				tvPm2_5.setText(list.get(i).pm2_5);
				rePm10.setBackgroundResource(getCicleBackground(value));
				tvPm10.setText(list.get(i).pm10);
				if (!TextUtils.isEmpty(list.get(i).time)) {
					try {
						tvTime.setText(sdf2.format(sdf1.parse(list.get(i).time)) + getString(R.string.update));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				break;
			}
		}
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
		showAnimation(reContent);
		isClick = true;

		if (zoom <= 6.0f) {
			setValue(clickMarker.getTitle(), provinceList);
		}else if (zoom > 6.0f && zoom <= 8.0f) {
			setValue(clickMarker.getTitle(), provinceList);
			setValue(clickMarker.getTitle(), cityList);
		}else if (zoom > 8.0f) {
			setValue(clickMarker.getTitle(), provinceList);
			setValue(clickMarker.getTitle(), cityList);
			setValue(clickMarker.getTitle(), districtList);
		}

		llContainer.removeAllViews();
		String lat = String.valueOf(clickMarker.getPosition().latitude);
		String lng = String.valueOf(clickMarker.getPosition().longitude);
		getWeatherInfo(clickMarker.getTitle(), lat, lng);
	}

	//需要申请的所有权限
	public static String[] allPermissions = new String[] {
			Manifest.permission.CALL_PHONE,
	};

	//拒绝的权限集合
	public static List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			clickMarker();
		}else {
			deniedList.clear();
			for (int i = 0; i < allPermissions.length; i++) {
				if (ContextCompat.checkSelfPermission(mContext, allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(allPermissions[i]);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				clickMarker();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				ActivityCompat.requestPermissions(AirPolutionActivity.this, permissions, AuthorityUtil.AUTHOR_PHONE);
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
					for (int i = 0; i < grantResults.length; i++) {
						if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//所有权限都授予
						clickMarker();
					}else {//只要有一个没有授权，就提示进入设置界面设置
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用相机权限，是否前往设置？");
					}
				}else {
					for (int i = 0; i < permissions.length; i++) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(AirPolutionActivity.this, permissions[i])) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用相机权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}
	
	/**
	 * 获取实况信息、预报信息
	 */
	private void getWeatherInfo(String cityId, final String lat, final String lng) {
		if (TextUtils.isEmpty(cityId)) {
			return;
		}
		WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
			@Override
			public void onComplete(Weather content) {
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
				
				if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lng)) {
					OkHttpXiangJiAqi(lat, lng);
				}
			}
			
			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
			}
		});
	}
	
	/**
	 * 请求象辑aqi
	 */
	private void OkHttpXiangJiAqi(String lat, String lng) {
    	long timestamp = new Date().getTime();
    	String start1 = sdf3.format(timestamp);
    	String end1 = sdf3.format(timestamp+1000*60*60*24);
    	if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lng)) {
			String url = XiangJiManager.getXJSecretUrl(Double.valueOf(lng), Double.valueOf(lat), start1, end1, timestamp);
			OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					if (!response.isSuccessful()) {
						return;
					}
					String result = response.body().string();
					if (result != null) {
						try {
							JSONObject obj = new JSONObject(result.toString());
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

										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
													setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
													showPortrait();
													ivExpand.setImageResource(R.drawable.iv_expand);
												}else {
													setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
													showLandscape();
													ivExpand.setImageResource(R.drawable.iv_collose);
												}
											}
										});

									}
								} catch (ArrayIndexOutOfBoundsException e) {
									e.printStackTrace();
								}
							}
						} catch (JSONException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
		}
	}
	
	private void showPortrait() {
		ivExpand.setVisibility(View.VISIBLE);
		llTop.setVisibility(View.VISIBLE);
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
		llTop.setVisibility(View.GONE);
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
				hScrollView.scrollTo((int)(dm.widthPixels*2/4), hScrollView.getHeight());
			}
		});
	}
	
	@Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		Bitmap bitmap = null;
		Bitmap bitmap8 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
		if (reContent.getVisibility() == View.VISIBLE) {
			Bitmap bitmap2 = CommonUtil.captureView(llTop);
			Bitmap bitmap3 = CommonUtil.captureView(llContainer);
			Bitmap bitmap4 = CommonUtil.mergeBitmap(mContext, bitmap2, bitmap3, false);
			CommonUtil.clearBitmap(bitmap2);
			CommonUtil.clearBitmap(bitmap3);
			Bitmap bitmap5 = CommonUtil.mergeBitmap(AirPolutionActivity.this, bitmap1, bitmap4, false);
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
		CommonUtil.share(AirPolutionActivity.this, bitmap);
	}

	@Override
	public void onMapScreenShot(Bitmap arg0, int arg1) {
		// TODO Auto-generated method stub
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
				ivExpand.setImageResource(R.drawable.iv_expand);
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
				ivExpand.setImageResource(R.drawable.iv_expand);
			}
			break;
		case R.id.ivShare:
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				aMap.getMapScreenShot(AirPolutionActivity.this);
			}else {
				if (reContent.getVisibility() == View.VISIBLE) {
					Bitmap bitmap1 = CommonUtil.captureView(llCity);
					Bitmap bitmap2 = CommonUtil.captureView(llContainer);
					Bitmap bitmap3 = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap2, false);
					CommonUtil.clearBitmap(bitmap1);
					CommonUtil.clearBitmap(bitmap2);
					Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
					Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
					CommonUtil.clearBitmap(bitmap3);
					CommonUtil.clearBitmap(bitmap4);
					CommonUtil.share(AirPolutionActivity.this, bitmap);
				}else {
					aMap.getMapScreenShot(AirPolutionActivity.this);
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
				ivExpand.setImageResource(R.drawable.iv_expand);
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

}
