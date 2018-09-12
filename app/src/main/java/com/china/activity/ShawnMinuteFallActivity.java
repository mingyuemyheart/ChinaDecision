package com.china.activity;

import android.Manifest;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.china.R;
import com.china.common.CONST;
import com.china.dto.MinuteFallDto;
import com.china.dto.RadarDto;
import com.china.dto.WeatherDto;
import com.china.manager.CaiyunManager;
import com.china.manager.CaiyunManager.RadarListener;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.view.MinuteFallView;
import com.tendcloud.tenddata.TCAgent;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 分钟级降水
 */
public class ShawnMinuteFallActivity extends BaseActivity implements OnClickListener, RadarListener, AMapLocationListener,
OnMapClickListener, AMap.OnMarkerClickListener, OnGeocodeSearchListener, OnMapScreenShotListener{
	
	private Context mContext;
	private TextView tvTitle,tvTime,tvAddr,tvRain;
	private MapView mMapView;
	private AMap aMap;
	private float zoom = 3.7f;
	private List<MinuteFallDto> dataList = new ArrayList<>();
	private GroundOverlay mOverlay;
	private CaiyunManager mRadarManager;
	private RadarThread mRadarThread;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_PROGRESS = 2;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private static final int HANDLER_PAUSE = 4;
	private LinearLayout llTop,llContainer,llSeekBar;
	private ImageView ivRadar,ivSwitch,ivLocation,ivRank,ivPlay,ivLegend,ivArrow;
	private SeekBar seekBar;
    private Marker clickMarker;
	private GeocodeSearch geocoderSearch;
	private RelativeLayout reRain,reShare;
	private Bundle savedInstanceState;
	private LatLng locationLatLng = new LatLng(39.904030, 116.407526);
	private int width;
	private boolean isShowDetail = false;
	private List<Marker> radarMarkers = new ArrayList<>();
	private boolean isShowRadar = false;
	private AVLoadingIndicatorView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_minute_fall);
		this.savedInstanceState = savedInstanceState;
		mContext = this;
		checkAuthority();
	}

	private void init() {
		initMap(savedInstanceState);
		initWidget();
	}

	private void initMap(Bundle bundle) {
		mMapView = findViewById(R.id.mapView);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnMapClickListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
	}
	
	private void initWidget() {
		loadingView = findViewById(R.id.loadingView);
		ivPlay = findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		tvTime = findViewById(R.id.tvTime);
		llSeekBar = findViewById(R.id.llSeekBar);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvAddr = findViewById(R.id.tvAddr);
		tvRain = findViewById(R.id.tvRain);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		reRain = findViewById(R.id.reRain);
		reShare = findViewById(R.id.reShare);
		ivRank = findViewById(R.id.ivRank);
		ivRank.setOnClickListener(this);
		ivLegend = findViewById(R.id.ivLegend);
		ivSwitch = findViewById(R.id.ivSwitch);
		ivSwitch.setOnClickListener(this);
		ivRadar = findViewById(R.id.ivRadar);
		ivRadar.setOnClickListener(this);
		ivLocation = findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		llContainer = findViewById(R.id.llContainer);
		ivArrow = findViewById(R.id.ivArrow);
		llTop = findViewById(R.id.llTop);
		llTop.setOnClickListener(this);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		geocoderSearch = new GeocodeSearch(mContext);
		geocoderSearch.setOnGeocodeSearchListener(this);

		mRadarManager = new CaiyunManager(mContext);

		OkHttpCaiyun("http://api.tianqi.cn:8070/v1/img.py");
		if (CommonUtil.isLocationOpen(mContext)) {
			startLocation();
		}else {
			addLocationMarker(locationLatLng);
		}

		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();//初始化定位参数
		AMapLocationClient mLocationClient = new AMapLocationClient(mContext);//初始化定位
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
        	if (amapLocation.getLongitude() != 0 && amapLocation.getLatitude() != 0) {
				locationLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
				addLocationMarker(locationLatLng);
			}
        }
	}
	
	private OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {
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
	 * 添加定位标记
	 */
	private void addLocationMarker(LatLng latLng) {
		if (latLng == null) {
			return;
		}
		MarkerOptions options = new MarkerOptions();
		options.position(latLng);
		options.anchor(0.5f, 1.0f);
		Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.shawn_icon_map_location),
				(int)(CommonUtil.dip2px(mContext, 21)), (int)(CommonUtil.dip2px(mContext, 32)));
		if (bitmap != null) {
			options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
		}else {
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.shawn_icon_map_location));
		}
		if (clickMarker != null) {
			clickMarker.remove();
		}
		clickMarker = aMap.addMarker(options);
		clickMarker.setClickable(false);

		OkHttpMinute(latLng.longitude, latLng.latitude);
		searchAddrByLatLng(latLng.latitude, latLng.longitude);
	}

	private void OkHttpMinute(double lng, double lat) {
		final String url = "http://api.caiyunapp.com/v2/HyTVV5YAkoxlQ3Zd/"+lng+","+lat+"/forecast";
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
										if (!object.isNull("result")) {
											JSONObject obj = object.getJSONObject("result");
											if (!obj.isNull("minutely")) {
												JSONObject objMin = obj.getJSONObject("minutely");
												if (!objMin.isNull("description")) {
													String rain = objMin.getString("description");
													if (!TextUtils.isEmpty(rain)) {
														tvRain.setText(rain.replace("小彩云", ""));
														tvRain.setVisibility(View.VISIBLE);
													}else {
														tvRain.setVisibility(View.GONE);
													}
												}
												if (!objMin.isNull("precipitation_2h")) {
													JSONArray array = objMin.getJSONArray("precipitation_2h");
													List<WeatherDto> minuteList = new ArrayList<>();
													for (int i = 0; i < array.length(); i++) {
														WeatherDto dto = new WeatherDto();
														dto.minuteFall = (float) array.getDouble(i);
														minuteList.add(dto);
													}

													MinuteFallView minuteFallView = new MinuteFallView(mContext);
													minuteFallView.setData(minuteList, tvRain.getText().toString());
													llContainer.removeAllViews();
													llContainer.addView(minuteFallView, width, (int)(CommonUtil.dip2px(mContext, 120)));
												}
											}
										}
									} catch (JSONException e1) {
										e1.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
	@Override
	public void onMapClick(LatLng latLng) {
		tvAddr.setText("");
		tvRain.setText("");
		addLocationMarker(latLng);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker != null && marker != clickMarker) {
			Intent intent = new Intent(mContext, ShawnRadarDetailActivity.class);
			intent.putExtra("radarName", marker.getTitle());
			intent.putExtra("radarCode", marker.getSnippet());
			startActivity(intent);
		}
		return true;
	}
	
	/**
	 * 通过经纬度获取地理位置信息
	 * @param lat
	 * @param lng
	 */
	private void searchAddrByLatLng(double lat, double lng) {
		//latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系   
		RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(lat, lng), 200, GeocodeSearch.AMAP); 
    	geocoderSearch.getFromLocationAsyn(query); 
	}
	
	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
	}
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if (rCode == AMapException.CODE_AMAP_SUCCESS) {
			if (result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null) {
				String addr;
				if (result.getRegeocodeAddress().getCity().contains(result.getRegeocodeAddress().getProvince())) {
					addr = result.getRegeocodeAddress().getCity()+result.getRegeocodeAddress().getDistrict();
				}else {
					addr = result.getRegeocodeAddress().getProvince()+result.getRegeocodeAddress().getCity()+result.getRegeocodeAddress().getDistrict();
				}
				tvAddr.setText(addr);
			}
		}
	}
	
	/**
	 * 获取彩云数据
	 * @param url
	 */
	private void OkHttpCaiyun(final String url) {
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
						String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject obj = new JSONObject(result);
								if (!obj.isNull("status")) {
									if (obj.getString("status").equals("ok")) {
										if (!obj.isNull("radar_img")) {
											JSONArray array = new JSONArray(obj.getString("radar_img"));
											for (int i = 0; i < array.length(); i++) {
												JSONArray array0 = array.getJSONArray(i);
												MinuteFallDto dto = new MinuteFallDto();
												dto.setImgUrl(array0.optString(0));
												dto.setTime(array0.optLong(1));
												JSONArray itemArray = array0.getJSONArray(2);
												dto.setP1(itemArray.optDouble(0));
												dto.setP2(itemArray.optDouble(1));
												dto.setP3(itemArray.optDouble(2));
												dto.setP4(itemArray.optDouble(3));
												dataList.add(dto);
											}
											if (dataList.size() > 0) {
												startDownLoadImgs(dataList);
											}
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
		}).start();
	}
	
	private void startDownLoadImgs(List<MinuteFallDto> list) {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
 		mRadarManager.loadImagesAsyn(list, this);
	}
	
	@Override
	public void onResult(int result, List<MinuteFallDto> images) {
		mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		if (result == RadarListener.RESULT_SUCCESSED) {
//			if (mRadarThread != null) {
//				mRadarThread.cancel();
//				mRadarThread = null;
//			}
//			mRadarThread = new RadarThread(images);
//			mRadarThread.start();
			
			//把最新的一张降雨图片覆盖在地图上
			MinuteFallDto radar = images.get(images.size()-1);
			Message message = mHandler.obtainMessage();
			message.what = HANDLER_SHOW_RADAR;
			message.obj = radar;
			message.arg1 = 100;
			message.arg2 = 100;
			mHandler.sendMessage(message);
		}
	}

	@Override
	public void onProgress(String url, int progress) {
		Message msg = new Message();
		msg.obj = progress;
		msg.what = HANDLER_PROGRESS;
		mHandler.sendMessage(msg);
	}
	
	private void showRadar(Bitmap bitmap, double p1, double p2, double p3, double p4) {
		BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = new LatLngBounds.Builder()
		.include(new LatLng(p3, p2))
		.include(new LatLng(p1, p4))
		.build();
		
		if (mOverlay == null) {
			mOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
				.anchor(0.5f, 0.5f)
				.positionFromBounds(bounds)
				.image(fromView)
				.transparency(0.0f));
		} else {
			mOverlay.setImage(null);
			mOverlay.setPositionFromBounds(bounds);
			mOverlay.setImage(fromView);
		}
		aMap.runOnDrawFrame();
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_SHOW_RADAR: 
				if (msg.obj != null) {
					MinuteFallDto dto = (MinuteFallDto) msg.obj;
					if (dto.getPath() != null) {
						Bitmap bitmap = BitmapFactory.decodeFile(dto.getPath());
						if (bitmap != null) {
							showRadar(bitmap, dto.getP1(), dto.getP2(), dto.getP3(), dto.getP4());
						}
					}
					changeProgress(dto.getTime(), msg.arg2, msg.arg1);
				}
				break;
			case HANDLER_PROGRESS: 
//				if (mDialog != null) {
//					if (msg.obj != null) {
//						int progress = (Integer) msg.obj;
//						mDialog.setPercent(progress);
//					}
//				}
				break;
			case HANDLER_LOAD_FINISHED: 
				loadingView.setVisibility(View.GONE);
				llSeekBar.setVisibility(View.VISIBLE);
				break;
			case HANDLER_PAUSE:
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.shawn_icon_play);
				}
				break;
			default:
				break;
			}
			
		};
	};

	private class RadarThread extends Thread {

		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<MinuteFallDto> images;
		private int state;
		private int index;
		private int count;
		private boolean isTracking;

		private RadarThread(List<MinuteFallDto> images) {
			this.images = images;
			this.count = images.size();
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
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void sendRadar() {
			if (index >= count || index < 0) {
				index = 0;
			}else {
				MinuteFallDto radar = images.get(index);
				Message message = mHandler.obtainMessage();
				message.what = HANDLER_SHOW_RADAR;
				message.obj = radar;
				message.arg1 = count - 1;
				message.arg2 = index ++;
				mHandler.sendMessage(message);
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
	private void changeProgress(long time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String value = time + "000";
		Date date = new Date(Long.valueOf(value));
		tvTime.setText(sdf.format(date));
	}

	/**
	 * 隐藏或显示ListView的动画
	 */
	public void hideOrShowListViewAnimator(final View view, final int startValue,final int endValue){
		//1.设置属性的初始值和结束值
		ValueAnimator mAnimator = ValueAnimator.ofInt(0,100);
		//2.为目标对象的属性变化设置监听器
		mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int animatorValue = (Integer) animation.getAnimatedValue();
				float fraction = animatorValue/100f;
				IntEvaluator mEvaluator = new IntEvaluator();
				//3.使用IntEvaluator计算属性值并赋值给ListView的高
				view.getLayoutParams().height = mEvaluator.evaluate(fraction, startValue, endValue);
				view.requestLayout();
			}
		});
		//4.为ValueAnimator设置LinearInterpolator
		mAnimator.setInterpolator(new LinearInterpolator());
		//5.设置动画的持续时间
		mAnimator.setDuration(200);
		//6.为ValueAnimator设置目标对象并开始执行动画
		mAnimator.setTarget(view);
		mAnimator.start();
	}

	private void clickRainChart() {
		int height = (int)CommonUtil.dip2px(mContext, 120);
		isShowDetail = !isShowDetail;
		if (isShowDetail) {
			ivArrow.setImageResource(R.drawable.shawn_icon_animation_up);
			hideOrShowListViewAnimator(llContainer, 0, height);
			llContainer.setVisibility(View.VISIBLE);
		}else {
			ivArrow.setImageResource(R.drawable.shawn_icon_animation_down);
			hideOrShowListViewAnimator(llContainer, height, 0);
		}
	}

	/**
	 * 切换雷达站点显示、隐藏
	 */
	private void switchRadars() {
		isShowRadar = !isShowRadar;
		if (isShowRadar) {
			ivRadar.setImageResource(R.drawable.shawn_icon_minute_radar_on);
			if (radarMarkers.size() <= 0) {
				final String radar = CommonUtil.getFromAssets(mContext, "json/nation_radars.json");
				if (TextUtils.isEmpty(radar)) {
					return;
				}
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							JSONArray array = new JSONArray(radar);
							LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							for (int i = 0; i < array.length(); i++) {
								JSONObject itemObj = array.getJSONObject(i);
								double lat = itemObj.getDouble("lat");
								double lng = itemObj.getDouble("lon");
								String radarName = itemObj.getString("name");
								String radarCode = itemObj.getString("id");

								MarkerOptions options = new MarkerOptions();
								options.title(radarName);
								options.snippet(radarCode);
								options.anchor(0.5f, 0.5f);
								options.position(new LatLng(lat, lng));
								View mView = inflater.inflate(R.layout.shawn_radar_marker_icon, null);
								ImageView ivMarker = mView.findViewById(R.id.ivMarker);
								ivMarker.setImageResource(R.drawable.shawn_icon_map_radar);
								options.icon(BitmapDescriptorFactory.fromView(mView));
								Marker marker = aMap.addMarker(options);
								marker.setVisible(true);
								radarMarkers.add(marker);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}else {
				for (Marker marker : radarMarkers) {
					marker.setVisible(true);
				}
			}
		}else {
			ivRadar.setImageResource(R.drawable.shawn_icon_minute_radar_off);
			for (Marker marker : radarMarkers) {
				marker.setVisible(false);
			}
		}
	}
	
	@Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		if (llContainer.getVisibility() == View.VISIBLE) {
			Bitmap bitmap2 = CommonUtil.captureView(reRain);
			Bitmap bitmap3 = CommonUtil.captureView(llContainer);
			Bitmap bitmap4 = CommonUtil.mergeBitmap(ShawnMinuteFallActivity.this, bitmap2, bitmap3, false);
			CommonUtil.clearBitmap(bitmap2);
			CommonUtil.clearBitmap(bitmap3);
			Bitmap bitmap5 = CommonUtil.mergeBitmap(ShawnMinuteFallActivity.this, bitmap1, bitmap4, true);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap4);
			Bitmap bitmap6 = CommonUtil.captureView(reShare);
			Bitmap bitmap7 = CommonUtil.mergeBitmap(ShawnMinuteFallActivity.this, bitmap5, bitmap6, true);
			CommonUtil.clearBitmap(bitmap5);
			CommonUtil.clearBitmap(bitmap6);
			Bitmap bitmap8 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
			Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap7, bitmap8, false);
			CommonUtil.clearBitmap(bitmap7);
			CommonUtil.clearBitmap(bitmap8);
			CommonUtil.share(ShawnMinuteFallActivity.this, bitmap);
		}else {
			Bitmap bitmap2 = CommonUtil.captureView(llTop);
			Bitmap bitmap3 = CommonUtil.mergeBitmap(ShawnMinuteFallActivity.this, bitmap1, bitmap2, true);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap2);
			Bitmap bitmap4 = CommonUtil.captureView(reShare);
			Bitmap bitmap5 = CommonUtil.mergeBitmap(ShawnMinuteFallActivity.this, bitmap3, bitmap4, true);
			CommonUtil.clearBitmap(bitmap3);
			CommonUtil.clearBitmap(bitmap4);
			Bitmap bitmap6 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
			Bitmap bitmap = CommonUtil.mergeBitmap(ShawnMinuteFallActivity.this, bitmap5, bitmap6, false);
			CommonUtil.clearBitmap(bitmap5);
			CommonUtil.clearBitmap(bitmap6);
			CommonUtil.share(ShawnMinuteFallActivity.this, bitmap);
		}
	}

	@Override
	public void onMapScreenShot(Bitmap arg0, int arg1) {
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setBackEmit();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				setBackEmit();
				finish();
				break;
			case R.id.ivRadar:
				switchRadars();
				break;
			case R.id.ivSwitch:
				if (aMap.getMapType() == AMap.MAP_TYPE_NORMAL) {
					aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
					tvAddr.setTextColor(Color.WHITE);
					tvRain.setTextColor(Color.WHITE);
					ivSwitch.setImageResource(R.drawable.shawn_icon_switch_map_on);
				} else {
					aMap.setMapType(AMap.MAP_TYPE_NORMAL);
					tvAddr.setTextColor(Color.BLACK);
					tvRain.setTextColor(Color.BLACK);
					ivSwitch.setImageResource(R.drawable.shawn_icon_switch_map_off);
				}
				break;
			case R.id.ivLocation:
				if (zoom < 10.f) {
					zoom = 10.0f;
					ivLocation.setImageResource(R.drawable.shawn_icon_location_on);
				}else {
					zoom = 3.7f;
					ivLocation.setImageResource(R.drawable.shawn_icon_location_off);
				}
				if (locationLatLng != null) {
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, zoom));
				}
				break;
			case R.id.ivRank:
				if (ivLegend.getVisibility() == View.VISIBLE) {
					ivLegend.setVisibility(View.INVISIBLE);
					ivRank.setImageResource(R.drawable.shawn_icon_map_legend_off);
				} else {
					ivLegend.setVisibility(View.VISIBLE);
					ivRank.setImageResource(R.drawable.shawn_icon_map_legend_on);
				}
				break;
			case R.id.ivPlay:
				if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
					mRadarThread.pause();
					ivPlay.setImageResource(R.drawable.shawn_icon_play);
				} else if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
					mRadarThread.play();
					ivPlay.setImageResource(R.drawable.shawn_icon_pause);
				} else if (mRadarThread == null) {
					ivPlay.setImageResource(R.drawable.shawn_icon_pause);
					if (mRadarThread != null) {
						mRadarThread.cancel();
						mRadarThread = null;
					}
					mRadarThread = new RadarThread(dataList);
					mRadarThread.start();
				}
				break;
			case R.id.ivShare:
				aMap.getMapScreenShot(ShawnMinuteFallActivity.this);
				break;
			case R.id.llTop:
			case R.id.ivArrow:
				clickRainChart();
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
		if (mRadarManager != null) {
			mRadarManager.onDestory();
		}
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
	}

	//需要申请的所有权限
	private String[] allPermissions = new String[] {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
	};

	//拒绝的权限集合
	private List<String> deniedList = new ArrayList<>();
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
				ActivityCompat.requestPermissions(ShawnMinuteFallActivity.this, permissions, AuthorityUtil.AUTHOR_LOCATION);
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
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、存储权限，是否前往设置？");
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(ShawnMinuteFallActivity.this, permission)) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、存储权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}

}
