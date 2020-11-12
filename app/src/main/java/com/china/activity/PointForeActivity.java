package com.china.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.china.R;
import com.china.common.CONST;
import com.china.dto.StationMonitorDto;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;

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
 * 格点预报
 */
public class PointForeActivity extends BaseActivity implements OnClickListener, AMapLocationListener, OnCameraChangeListener, OnMapScreenShotListener, AMap.OnMarkerClickListener {
	
	private Context mContext;
	private TextView tvName,tvDataSource,tvTime,tvPublishTime;
	private ImageView ivTemp,ivHumidity,ivWind,ivCloud,ivSwitch,ivDataSource,ivPlay;
	private MapView mMapView;
	private AMap aMap;
	private ConstraintLayout clShare;
	private float zoom = 3.7f;
	private List<StationMonitorDto> dataList = new ArrayList<>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("dd日HH时", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH时", Locale.CHINA);
	private List<Marker> markers = new ArrayList<>();
	private int dataType = 1;//1温度、2湿度、3风速、4能见度、5云量
	private double locationLat = 35.926628, locationLng = 105.178100;
	private int mapType = AMap.MAP_TYPE_NORMAL;
	private LatLng start,end;
	private Bundle savedInstanceState;
	private int currentIndex = 0;//当前时次数据
	private SeekBar seekBar;
	private RadarThread mRadarThread;
	private Marker locationMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_fore);
		mContext = this;
		this.savedInstanceState = savedInstanceState;
		checkAuthority();
	}

	private void init() {
		showDialog();
		initMap();
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvName = findViewById(R.id.tvName);
		clShare = findViewById(R.id.clShare);
		ivTemp = findViewById(R.id.ivTemp);
		ivTemp.setOnClickListener(this);
		ivHumidity = findViewById(R.id.ivHumidity);
		ivHumidity.setOnClickListener(this);
		ivWind = findViewById(R.id.ivWind);
		ivWind.setOnClickListener(this);
		ivCloud = findViewById(R.id.ivCloud);
		ivCloud.setOnClickListener(this);
		ImageView ivLocation = findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		ivSwitch = findViewById(R.id.ivSwitch);
		ivSwitch.setOnClickListener(this);
		tvDataSource = findViewById(R.id.tvDataSource);
		tvDataSource.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		tvDataSource.setOnClickListener(this);
		ivDataSource = findViewById(R.id.ivDataSource);
		ivDataSource.setOnClickListener(this);
		ivPlay = findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		tvTime = findViewById(R.id.tvTime);
		tvPublishTime = findViewById(R.id.tvPublishTime);
		seekBar = findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		if (CommonUtil.isLocationOpen(mContext)) {
			startLocation();
		}else {
			locationComplete();
		}
	}

	private SeekBar.OnSeekBarChangeListener seekbarListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.setCurrent(seekBar.getProgress());
				mRadarThread.stopTracking();
			}
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.startTracking();
			}
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		}
	};

	/**
	 * 开始定位
	 */
	private void startLocation() {
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();//初始化定位参数
		AMapLocationClient mLocationClient = new AMapLocationClient(mContext);//初始化定位
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
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
			locationLat = amapLocation.getLatitude();
			locationLng = amapLocation.getLongitude();
			locationComplete();
		}
	}

	private void locationComplete() {
		if (locationMarker != null) {
			locationMarker.remove();
		}
		LatLng latLng = new LatLng(locationLat, locationLng);
		MarkerOptions options = new MarkerOptions();
		options.anchor(0.5f, 1.0f);
		Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.icon_map_location),
				(int) (CommonUtil.dip2px(mContext, 21)), (int) (CommonUtil.dip2px(mContext, 32)));
		if (bitmap != null) {
			options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
		} else {
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_location));
		}
		options.position(latLng);
		locationMarker = aMap.addMarker(options);
		locationMarker.setClickable(false);
	}
	
	/**
	 * 初始化地图
	 */
	private void initMap() {
		mMapView = findViewById(R.id.mapView);
		mMapView.onCreate(savedInstanceState);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnCameraChangeListener(this);
		aMap.setOnMarkerClickListener(this);
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		Log.e("zoom", arg0.zoom+"");
		Point startPoint = new Point(0, 0);
		Point endPoint = new Point(CommonUtil.widthPixels(this), CommonUtil.heightPixels(this));
		start = aMap.getProjection().fromScreenLocation(startPoint);
		end = aMap.getProjection().fromScreenLocation(endPoint);

		zoom = arg0.zoom;
		getPointInfo();
	}

	/**
	 * 获取格点数据
	 */
	private void getPointInfo() {
		String url = String.format("http://scapi.weather.com.cn/weather/getqggdybql?zoom=%s&statlonlat=%s,%s&endlonlat=%s,%s&test=ncg",
				(int)zoom, start.longitude, start.latitude, end.longitude, end.latitude);
		handler.removeMessages(1001);
		Message msg = handler.obtainMessage(1001);
		msg.obj = url;
		handler.sendMessageDelayed(msg, 1000);
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1001:
					OkHttpList(msg.obj+"");
					break;
			}
		}
	};

	private void OkHttpList(final String url) {
		Log.e("url", url);
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
								cancelDialog();
								if (!TextUtils.isEmpty(result)) {
									try {
										dataList.clear();
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											StationMonitorDto dto = new StationMonitorDto();
											JSONObject itemObj = array.getJSONObject(i);
											dto.lat = itemObj.getDouble("LAT");
											dto.lng = itemObj.getDouble("LON");
											dto.time = itemObj.getString("TIME");

											if (i == 0) {
												try {
													tvPublishTime.setText(sdf3.format(sdf2.parse(dto.time))+"发布");
												} catch (ParseException e) {
													e.printStackTrace();
												}
											}

											JSONArray tempArray = itemObj.getJSONArray("TMP");
											JSONArray humidityArray = itemObj.getJSONArray("RRH");
											JSONArray windSArray = itemObj.getJSONArray("WINS");
											JSONArray windDArray = itemObj.getJSONArray("WIND");
											JSONArray cloudArray = itemObj.getJSONArray("ECT");
											List<StationMonitorDto> list = new ArrayList<>();
											for (int j = 0; j < tempArray.length(); j++) {
												StationMonitorDto data = new StationMonitorDto();
												data.pointTemp = tempArray.getString(j);
												data.humidity = humidityArray.getString(j);
												data.windSpeed = windSArray.getString(j);
												data.windDir = windDArray.getString(j);
												data.cloud = cloudArray.getString(j);
												try {
													long time = sdf2.parse(dto.time).getTime()+1000*60*60*3*j;
													data.time = sdf1.format(time);

													long currentTime = new Date().getTime();
													if (currentTime <= time) {
														list.add(data);
													}
												} catch (ParseException e) {
													e.printStackTrace();
												}
											}
											dto.itemList.addAll(list);

											dataList.add(dto);
										}

										switchElement();
										if (dataList.size() > 0) {
											StationMonitorDto dto = dataList.get(currentIndex);
											if (dto != null && dto.itemList.size() > 0) {
												StationMonitorDto data = dto.itemList.get(0);
												changeProgress(data.time, 0, dto.itemList.size()-1);
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

	private void removeTexts() {
		for (int i = 0; i < markers.size(); i++) {
			markers.get(i).remove();
		}
		markers.clear();
	}

	/**
	 * 切换要素
	 */
	private void switchElement() {
		removeTexts();
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (StationMonitorDto dto : dataList) {
			MarkerOptions options = new MarkerOptions();
			options.position(new LatLng(dto.lat, dto.lng));
			View view = inflater.inflate(R.layout.shawn_point_fore_icon, null);
			TextView tvMarker = view.findViewById(R.id.tvMarker);
			tvMarker.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
			if (mapType == AMap.MAP_TYPE_NORMAL) {
				tvMarker.setTextColor(Color.RED);
			}else if (mapType == AMap.MAP_TYPE_SATELLITE){
				tvMarker.setTextColor(Color.WHITE);
			}

			if (dto.itemList.size() > 0) {
				String content = "";
				if (dataType == 1) {
					content = dto.itemList.get(currentIndex).pointTemp;
				}else if (dataType == 2) {
					content = dto.itemList.get(currentIndex).humidity;
				}else if (dataType == 3) {
					content = dto.itemList.get(currentIndex).windSpeed;
				}else if (dataType == 5) {
					content = dto.itemList.get(currentIndex).cloud;
				}
				tvMarker.setText(content);
				options.icon(BitmapDescriptorFactory.fromView(view));
				if (!TextUtils.isEmpty(content)) {
					float value = Float.valueOf(content);
					if (value < 9000) {
						Marker marker = aMap.addMarker(options);
						markers.add(marker);
					}
				}
			}

		}
	}

	private class RadarThread extends Thread {

		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<StationMonitorDto> itemList;
		private int state;
		private int index;
		private int count;
		private boolean isTracking;

		private RadarThread(List<StationMonitorDto> itemList) {
			this.itemList = itemList;
			this.count = itemList.size();
			this.index = 0;
			this.state = STATE_NONE;
			this.isTracking = false;
		}

		private int getCurrentState() {
			return state;
		}

		@Override
		public void run() {
			super.run();
			this.state = STATE_PLAYING;
			while (true) {
				if (state == STATE_CANCEL) {
					break;
				}
				if (state == STATE_PAUSE) {
					continue;
				}
				if (isTracking) {
					continue;
				}
				sendRadar();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void sendRadar() {
			if (index >= count || index < 0) {
				index = 0;
			}else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						currentIndex = index;
						StationMonitorDto dto = itemList.get(index);
						switchElement();
						changeProgress(dto.time, index++, count-1);
					}
				});
			}
		}

		public void cancel() {
			this.state = STATE_CANCEL;
		}
		public void pause() {
			this.state = STATE_PAUSE;
		}
		public void play() {
			this.state = STATE_PLAYING;
		}

		public void setCurrent(int index) {
			this.index = index;
		}

		public void startTracking() {
			isTracking = true;
		}

		public void stopTracking() {
			isTracking = false;
			if (this.state == STATE_PAUSE) {
				sendRadar();
			}
		}
	}

	@SuppressLint("SimpleDateFormat")
	private void changeProgress(String time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		if (!TextUtils.isEmpty(time)) {
			tvTime.setText(time);
			changeLayerName();
		}
	}

	private void changeLayerName() {
		if (dataType == 1) {
			tvName.setText(tvTime.getText()+"格点温度预报[单位:"+getString(R.string.unit_degree)+"]");
		}else if (dataType == 2) {
			tvName.setText(tvTime.getText()+"格点相对湿度预报[单位:"+getString(R.string.unit_percent)+"]");
		}else if (dataType == 3) {
			tvName.setText(tvTime.getText()+"格点风速预报[单位:"+getString(R.string.unit_speed)+"]");
		}else if (dataType == 5) {
			tvName.setText(tvTime.getText()+"格点云量预报[单位:"+getString(R.string.unit_percent)+"]");
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker != null && marker != locationMarker) {
			Intent intent = new Intent(mContext, PointForeDetailActivity.class);
			intent.putExtra("lat", marker.getPosition().latitude);
			intent.putExtra("lng", marker.getPosition().longitude);
			startActivity(intent);
		}
		return true;
	}

	@Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		Bitmap bitmap;
		Bitmap bitmap8 = BitmapFactory.decodeResource(getResources(), R.drawable.legend_share_portrait);
		Bitmap bitmap2 = CommonUtil.captureView(clShare);
		Bitmap bitmap3 = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap2, true);
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap2);
		bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap8, false);
		CommonUtil.clearBitmap(bitmap3);
		CommonUtil.clearBitmap(bitmap8);
		CommonUtil.share(PointForeActivity.this, bitmap);
	}

	@Override
	public void onMapScreenShot(Bitmap arg0, int arg1) {
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.ivTemp:
				ivTemp.setImageResource(R.drawable.com_temp_press);
				ivHumidity.setImageResource(R.drawable.com_humidity);
				ivWind.setImageResource(R.drawable.com_wind);
				ivCloud.setImageResource(R.drawable.com_cloud);

				dataType = 1;
				switchElement();
				changeLayerName();
				break;
			case R.id.ivHumidity:
				ivTemp.setImageResource(R.drawable.com_temp);
				ivHumidity.setImageResource(R.drawable.com_humidity_press);
				ivWind.setImageResource(R.drawable.com_wind);
				ivCloud.setImageResource(R.drawable.com_cloud);

				dataType = 2;
				switchElement();
				changeLayerName();
				break;
			case R.id.ivWind:
				ivTemp.setImageResource(R.drawable.com_temp);
				ivHumidity.setImageResource(R.drawable.com_humidity);
				ivWind.setImageResource(R.drawable.com_wind_press);
				ivCloud.setImageResource(R.drawable.com_cloud);

				dataType = 3;
				switchElement();
				changeLayerName();
				break;
			case R.id.ivCloud:
				ivTemp.setImageResource(R.drawable.com_temp);
				ivHumidity.setImageResource(R.drawable.com_humidity);
				ivWind.setImageResource(R.drawable.com_wind);
				ivCloud.setImageResource(R.drawable.com_cloud_press);

				dataType = 5;
				switchElement();
				changeLayerName();
				break;
			case R.id.ivSwitch:
				if (mapType == AMap.MAP_TYPE_NORMAL) {
					mapType = AMap.MAP_TYPE_SATELLITE;
					ivSwitch.setImageResource(R.drawable.com_switch_map_press);
					tvName.setTextColor(Color.WHITE);
					tvPublishTime.setTextColor(Color.WHITE);
				}else if (mapType == AMap.MAP_TYPE_SATELLITE) {
					mapType = AMap.MAP_TYPE_NORMAL;
					ivSwitch.setImageResource(R.drawable.com_switch_map);
					tvName.setTextColor(Color.BLACK);
					tvPublishTime.setTextColor(Color.BLACK);
				}
				if (aMap != null) {
					aMap.setMapType(mapType);
				}
				switchElement();
				break;
			case R.id.ivDataSource:
				if (tvDataSource.getVisibility() == View.VISIBLE) {
					tvDataSource.setVisibility(View.GONE);
					ivDataSource.setImageResource(R.drawable.com_data_source);
				}else {
					tvDataSource.setVisibility(View.VISIBLE);
					ivDataSource.setImageResource(R.drawable.com_data_source_press);
				}
				break;
			case R.id.tvDataSource:
				Intent intent = new Intent(mContext, Webview2Activity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, "中央气象台智能网格预报产品");
				intent.putExtra(CONST.WEB_URL, "http://www.cma.gov.cn/2011xzt/2017zt/2017qmt/20170728/");
				startActivity(intent);
				break;
			case R.id.ivLocation:
				if (zoom >= 12.f) {
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), 3.5f));
				}else {
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), 12.0f));
				}
				break;
			case R.id.ivShare:
				aMap.getMapScreenShot(PointForeActivity.this);
				break;
			case R.id.ivPlay:
				if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
					mRadarThread.pause();
					ivPlay.setImageResource(R.drawable.icon_play);
				} else if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
					mRadarThread.play();
					ivPlay.setImageResource(R.drawable.icon_pause);
				} else if (mRadarThread == null) {
					ivPlay.setImageResource(R.drawable.icon_pause);
					if (mRadarThread != null) {
						mRadarThread.cancel();
						mRadarThread = null;
					}
					mRadarThread = new RadarThread(dataList.get(currentIndex).itemList);
					mRadarThread.start();
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
	public static String[] allPermissions = new String[] {
			Manifest.permission.ACCESS_COARSE_LOCATION,
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
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用定位权限、存储权限，是否前往设置？");
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用定位权限、存储权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}

}
