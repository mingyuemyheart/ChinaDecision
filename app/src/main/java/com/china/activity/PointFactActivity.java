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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.china.R;
import com.china.common.CONST;
import com.china.dto.StationMonitorDto;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.tendcloud.tenddata.TCAgent;

import net.tsz.afinal.FinalBitmap;

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
 * 格点实况
 * @author shawn_sun
 *
 */

public class PointFactActivity extends BaseActivity implements OnClickListener, AMapLocationListener, OnCameraChangeListener, OnMapScreenShotListener{
	
	private Context mContext;
	private LinearLayout llBack,llShowType;
	private TextView tvTitle,tvName,tvShowType1,tvShowType2,tvDataSource;
	private ImageView ivTemp,ivHumidity,ivWind,ivVisible,ivCloud,ivLocation,ivLegendPrompt,ivLegend,ivShowType,ivSwitch,ivDataSource,ivShare;
	private MapView mMapView;
	private AMap aMap;
	private RelativeLayout reShare;
	private float zoom = 3.7f;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH时");
	private List<StationMonitorDto> dataList = new ArrayList<>();
	private List<Text> texts = new ArrayList<>();
	private int dataType = 1;//1温度、2湿度、3风速、4能见度、5云量
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private double locationLat = 35.926628, locationLng = 105.178100;
	private int mapType = AMap.MAP_TYPE_NORMAL;
	private boolean showType = true;//true为落区图，false为数值
	private LatLng start,end;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_fact);
		mContext = this;
		showDialog();
		initMap(savedInstanceState);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		tvTitle = findViewById(R.id.tvTitle);
		tvName = findViewById(R.id.tvName);
		reShare = findViewById(R.id.reShare);
		ivTemp = findViewById(R.id.ivTemp);
		ivTemp.setOnClickListener(this);
		ivHumidity = findViewById(R.id.ivHumidity);
		ivHumidity.setOnClickListener(this);
		ivWind = findViewById(R.id.ivWind);
		ivWind.setOnClickListener(this);
		ivVisible = findViewById(R.id.ivVisible);
		ivVisible.setOnClickListener(this);
		ivCloud = findViewById(R.id.ivCloud);
		ivCloud.setOnClickListener(this);
		ivLegendPrompt = findViewById(R.id.ivLegendPrompt);
		ivLegendPrompt.setOnClickListener(this);
		ivLegend = findViewById(R.id.ivLegend);
		ivLocation = findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		ivSwitch = findViewById(R.id.ivSwitch);
		ivSwitch.setOnClickListener(this);
		llShowType = findViewById(R.id.llShowType);
		tvShowType1 = findViewById(R.id.tvShowType1);
		tvShowType1.setOnClickListener(this);
		tvShowType2 = findViewById(R.id.tvShowType2);
		tvShowType2.setOnClickListener(this);
		ivShowType = findViewById(R.id.ivShowType);
		ivShowType.setOnClickListener(this);
		tvDataSource = findViewById(R.id.tvDataSource);
		tvDataSource.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		tvDataSource.setOnClickListener(this);
		ivDataSource = findViewById(R.id.ivDataSource);
		ivDataSource.setOnClickListener(this);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		checkAuthority();
		OkHttpLayer();

		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}

	/**
	 * 申请权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			startLocation();
		}else {
			if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, AuthorityUtil.AUTHOR_LOCATION);
			}else {
				startLocation();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_LOCATION:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					startLocation();
				}else {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用定位权限，是否前往设置？");
					}
				}
				break;

		}
	}

	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
		mLocationClient = new AMapLocationClient(mContext);//初始化定位
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
		mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
		mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
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
			ivLocation.setVisibility(View.VISIBLE);
			LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
			MarkerOptions options = new MarkerOptions();
			options.anchor(0.5f, 0.5f);
			Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.shawn_icon_location_point),
					(int) (CommonUtil.dip2px(mContext, 15)), (int) (CommonUtil.dip2px(mContext, 15)));
			if (bitmap != null) {
				options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
			} else {
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.shawn_icon_location_point));
			}
			options.position(latLng);
			Marker locationMarker = aMap.addMarker(options);
			locationMarker.setClickable(false);
		}
	}
	
	/**
	 * 初始化地图
	 */
	private void initMap(Bundle bundle) {
		mMapView = findViewById(R.id.map);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnCameraChangeListener(this);
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		Log.e("zoom", arg0.zoom+"");
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point startPoint = new Point(0, 0);
		Point endPoint = new Point(dm.widthPixels, dm.heightPixels);
		start = aMap.getProjection().fromScreenLocation(startPoint);
		end = aMap.getProjection().fromScreenLocation(endPoint);

		zoom = arg0.zoom;
		getPointInfo(1000);
	}

	/**
	 * 获取格点数据
	 */
	private void getPointInfo(long delayMillis) {
		if (showType == false) {
			String url = String.format("http://scapi.weather.com.cn/weather/getqggdsk?zoom=%s&statlonlat=%s,%s&endlonlat=%s,%s&test=ncg",
					(int)zoom, start.longitude, start.latitude, end.longitude, end.latitude);
			handler.removeMessages(1000);
			Message msg = handler.obtainMessage(1000);
			msg.obj = url;
			handler.sendMessageDelayed(msg, delayMillis);
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1000:
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
								try {
									dataList.clear();
									JSONArray array = new JSONArray(result);
									for (int i = 0; i < array.length(); i++) {
										JSONObject itemObj = array.getJSONObject(i);
										StationMonitorDto dto = new StationMonitorDto();
										dto.lat = itemObj.getDouble("LAT");
										dto.lng = itemObj.getDouble("LON");
										dto.pointTemp = itemObj.getString("TEM");
										dto.humidity = itemObj.getString("RHU");
										dto.windSpeed = itemObj.getString("WINS");
										dto.visibility = itemObj.getString("VIS");
										dto.cloud = itemObj.getString("TCDC");
										dataList.add(dto);
									}
									cancelDialog();
									switchElement();
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						});
					}
				});
			}
		}).start();
	}

	private void removeTexts() {
		for (int i = 0; i < texts.size(); i++) {
			texts.get(i).remove();
		}
		texts.clear();
	}

	/**
	 * 获取图层、图例等数据
	 */
	private void OkHttpLayer() {
		final String url = "http://decision-admin.tianqi.cn/Home/extra/decision_gdskimages";
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
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("TEM")) {
											JSONObject itemObj = obj.getJSONObject("TEM");
											String imgurl = itemObj.getString("imgurl");
											String tuliurl = itemObj.getString("tuliurl");
											String time = itemObj.getString("time");
											String minlat = itemObj.getString("minlat");
											String minlon = itemObj.getString("minlon");
											String maxlat = itemObj.getString("maxlat");
											String maxlon = itemObj.getString("maxlon");
											ivTemp.setTag(imgurl+","+tuliurl+","+time+","+minlat+","+minlon+","+maxlat+","+maxlon);
											switchElement();
										}
										if (!obj.isNull("RHU")) {
											JSONObject itemObj = obj.getJSONObject("RHU");
											String imgurl = itemObj.getString("imgurl");
											String tuliurl = itemObj.getString("tuliurl");
											String time = itemObj.getString("time");
											String minlat = itemObj.getString("minlat");
											String minlon = itemObj.getString("minlon");
											String maxlat = itemObj.getString("maxlat");
											String maxlon = itemObj.getString("maxlon");
											ivHumidity.setTag(imgurl+","+tuliurl+","+time+","+minlat+","+minlon+","+maxlat+","+maxlon);
										}
										if (!obj.isNull("WINS")) {
											JSONObject itemObj = obj.getJSONObject("WINS");
											String imgurl = itemObj.getString("imgurl");
											String tuliurl = itemObj.getString("tuliurl");
											String time = itemObj.getString("time");
											String minlat = itemObj.getString("minlat");
											String minlon = itemObj.getString("minlon");
											String maxlat = itemObj.getString("maxlat");
											String maxlon = itemObj.getString("maxlon");
											ivWind.setTag(imgurl+","+tuliurl+","+time+","+minlat+","+minlon+","+maxlat+","+maxlon);
										}
										if (!obj.isNull("VIS")) {
											JSONObject itemObj = obj.getJSONObject("VIS");
											String imgurl = itemObj.getString("imgurl");
											String tuliurl = itemObj.getString("tuliurl");
											String time = itemObj.getString("time");
											String minlat = itemObj.getString("minlat");
											String minlon = itemObj.getString("minlon");
											String maxlat = itemObj.getString("maxlat");
											String maxlon = itemObj.getString("maxlon");
											ivVisible.setTag(imgurl+","+tuliurl+","+time+","+minlat+","+minlon+","+maxlat+","+maxlon);
										}
										if (!obj.isNull("TCDC")) {
											JSONObject itemObj = obj.getJSONObject("TCDC");
											String imgurl = itemObj.getString("imgurl");
											String tuliurl = itemObj.getString("tuliurl");
											String time = itemObj.getString("time");
											String minlat = itemObj.getString("minlat");
											String minlon = itemObj.getString("minlon");
											String maxlat = itemObj.getString("maxlat");
											String maxlon = itemObj.getString("maxlon");
											ivCloud.setTag(imgurl+","+tuliurl+","+time+","+minlat+","+minlon+","+maxlat+","+maxlon);
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
								cancelDialog();
							}
						});
					}
				});
			}
		}).start();
	}

	/**
	 * 切换要素
	 */
	private void switchElement() {
		String[] tags = null;
		if (dataType == 1) {
			tags = String.valueOf(ivTemp.getTag()).split(",");
		}else if (dataType == 2) {
			tags = String.valueOf(ivHumidity.getTag()).split(",");
		}else if (dataType == 3) {
			tags = String.valueOf(ivWind.getTag()).split(",");
		}else if (dataType == 4) {
			tags = String.valueOf(ivVisible.getTag()).split(",");
		}else if (dataType == 5) {
			tags = String.valueOf(ivCloud.getTag()).split(",");
		}

		//标题
		String end = sdf1.format(new Date());
		String start = sdf1.format(new Date().getTime()-1000*60*60);
		String time = "("+start+"-"+end+")";
		if (dataType == 1) {
			tvName.setText(String.format("逐小时%s格点实况%s%s", "温度", time, "[单位:"+getString(R.string.unit_degree)+"]"));
		}else if (dataType == 2) {
			tvName.setText(String.format("逐小时%s格点实况%s%s", "相对湿度", time, "[单位:"+getString(R.string.unit_percent)+"]"));
		}else if (dataType == 3) {
			tvName.setText(String.format("逐小时%s格点实况%s%s", "风速", time, "[单位:"+getString(R.string.unit_speed)+"]"));
		}else if (dataType == 4) {
			tvName.setText(String.format("逐小时%s格点实况%s%s", "能见度", time, "[单位:"+getString(R.string.unit_km)+"]"));
		}else if (dataType == 5) {
			tvName.setText(String.format("逐小时%s格点实况%s%s", "云量", time, "[单位:"+getString(R.string.unit_percent)+"]"));
		}

		if (layerOverlay != null) {
			layerOverlay.remove();
			layerOverlay = null;
		}
		ivLegendPrompt.setVisibility(View.GONE);
		ivLegend.setVisibility(View.GONE);
		removeTexts();
		if (showType) {
			OkHttpImage(tags);
			if (!TextUtils.isEmpty(tags[1])) {
				FinalBitmap finalBitmap = FinalBitmap.create(mContext);
				finalBitmap.display(ivLegend, tags[1], null, 0);
				ivLegendPrompt.setVisibility(View.VISIBLE);
				ivLegend.setVisibility(View.VISIBLE);
			}
		}else {
			for (StationMonitorDto dto : dataList) {
				double lat = Double.valueOf(dto.lat);
				double lng = Double.valueOf(dto.lng);
				TextOptions options = new TextOptions();
				options.position(new LatLng(lat, lng));
				options.backgroundColor(Color.TRANSPARENT);
				if (mapType == AMap.MAP_TYPE_NORMAL) {
					options.fontColor(Color.RED);
				}else if (mapType == AMap.MAP_TYPE_SATELLITE){
					options.fontColor(Color.WHITE);
				}
				options.fontSize(30);
				String content = "";
				if (dataType == 1) {
					content = dto.pointTemp;
				}else if (dataType == 2) {
					content = dto.humidity;
				}else if (dataType == 3) {
					content = dto.windSpeed;
				}else if (dataType == 4) {
					content = dto.visibility;
				}else if (dataType == 5) {
					content = dto.cloud;
				}
				options.text(content);
				if (!TextUtils.isEmpty(content) && !content.contains("99999")) {
					Text text = aMap.addText(options);
					texts.add(text);
				}
			}
		}

	}

	/**
	 * 获取图层
	 */
	private GroundOverlay layerOverlay;
	private void OkHttpImage(final String[] tags) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(tags[0]).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final byte[] bytes = response.body().bytes();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
								if (bitmap != null) {
									BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
									LatLngBounds bounds = new LatLngBounds.Builder()
											.include(new LatLng(Double.parseDouble(tags[3]), Double.parseDouble(tags[4])))
											.include(new LatLng(Double.parseDouble(tags[5]), Double.parseDouble(tags[6])))
											.build();

									if (layerOverlay != null) {
										layerOverlay.remove();
										layerOverlay = null;
									}
									layerOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
											.anchor(0.5f, 0.5f)
											.positionFromBounds(bounds)
											.image(fromView)
											.transparency(0.2f));
								}
							}
						});
					}
				});
			}
		}).start();
	}

	@Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		Bitmap bitmap;
		Bitmap bitmap8 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
		Bitmap bitmap2 = CommonUtil.captureView(reShare);
		Bitmap bitmap3 = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap2, true);
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap2);
		bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap8, false);
		CommonUtil.clearBitmap(bitmap3);
		CommonUtil.clearBitmap(bitmap8);
		CommonUtil.share(PointFactActivity.this, bitmap);
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
				ivVisible.setImageResource(R.drawable.com_visible);
				ivCloud.setImageResource(R.drawable.com_cloud);

				dataType = 1;
				switchElement();
				break;
			case R.id.ivHumidity:
				ivTemp.setImageResource(R.drawable.com_temp);
				ivHumidity.setImageResource(R.drawable.com_humidity_press);
				ivWind.setImageResource(R.drawable.com_wind);
				ivVisible.setImageResource(R.drawable.com_visible);
				ivCloud.setImageResource(R.drawable.com_cloud);

				dataType = 2;
				switchElement();
				break;
			case R.id.ivWind:
				ivTemp.setImageResource(R.drawable.com_temp);
				ivHumidity.setImageResource(R.drawable.com_humidity);
				ivWind.setImageResource(R.drawable.com_wind_press);
				ivVisible.setImageResource(R.drawable.com_visible);
				ivCloud.setImageResource(R.drawable.com_cloud);

				dataType = 3;
				switchElement();
				break;
			case R.id.ivVisible:
				ivTemp.setImageResource(R.drawable.com_temp);
				ivHumidity.setImageResource(R.drawable.com_humidity);
				ivWind.setImageResource(R.drawable.com_wind);
				ivVisible.setImageResource(R.drawable.com_visible_press);
				ivCloud.setImageResource(R.drawable.com_cloud);

				dataType = 4;
				switchElement();
				break;
			case R.id.ivCloud:
				ivTemp.setImageResource(R.drawable.com_temp);
				ivHumidity.setImageResource(R.drawable.com_humidity);
				ivWind.setImageResource(R.drawable.com_wind);
				ivVisible.setImageResource(R.drawable.com_visible);
				ivCloud.setImageResource(R.drawable.com_cloud_press);

				dataType = 5;
				switchElement();
				break;
			case R.id.ivShowType:
				if (llShowType.getVisibility() == View.VISIBLE) {
					llShowType.setVisibility(View.GONE);
					ivShowType.setImageResource(R.drawable.com_show_type);
				}else {
					llShowType.setVisibility(View.VISIBLE);
					ivShowType.setImageResource(R.drawable.com_show_type_press);
				}
				break;
			case R.id.tvShowType1:
				tvShowType1.setTextColor(getResources().getColor(R.color.blue));
				tvShowType2.setTextColor(getResources().getColor(R.color.black));
				tvShowType1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				tvShowType2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
				showType = true;
				switchElement();
				break;
			case R.id.tvShowType2:
				tvShowType1.setTextColor(getResources().getColor(R.color.black));
				tvShowType2.setTextColor(getResources().getColor(R.color.blue));
				tvShowType1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
				tvShowType2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				showType = false;
				if (dataList.size() <= 0) {
					getPointInfo(0);
				}else {
					switchElement();
				}
				break;
			case R.id.ivSwitch:
				if (mapType == AMap.MAP_TYPE_NORMAL) {
					mapType = AMap.MAP_TYPE_SATELLITE;
					ivSwitch.setImageResource(R.drawable.com_switch_map_press);
					tvName.setTextColor(Color.WHITE);
				}else if (mapType == AMap.MAP_TYPE_SATELLITE) {
					mapType = AMap.MAP_TYPE_NORMAL;
					ivSwitch.setImageResource(R.drawable.com_switch_map);
					tvName.setTextColor(Color.BLACK);
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
				Intent intent = new Intent(mContext, ShawnNewsDetailActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, "中国气象局陆面数据同化系统（CLDAS-V2.0）实时产品数据集");
				intent.putExtra(CONST.WEB_URL, "http://data.cma.cn/data/detail/dataCode/NAFP_CLDAS2.0_RT.html");
				startActivity(intent);
				break;
			case R.id.ivLocation:
				if (zoom >= 12.f) {
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), 3.5f));
				}else {
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), 12.0f));
				}
				break;
			case R.id.ivLegendPrompt:
				if (ivLegend.getVisibility() == View.VISIBLE) {
					ivLegend.setVisibility(View.INVISIBLE);
				}else {
					ivLegend.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.ivShare:
				aMap.getMapScreenShot(PointFactActivity.this);
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
