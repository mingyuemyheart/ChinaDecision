package com.china.activity;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.china.R;
import com.china.adapter.ShawnWarningAdapter;
import com.china.adapter.ShawnWarningStatisticAdapter;
import com.china.common.CONST;
import com.china.common.MyApplication;
import com.china.dto.TyphoonDto;
import com.china.dto.WarningDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.SecretUrlUtil;
import com.china.view.ArcMenu;
import com.china.view.ArcMenu.OnMenuItemClickListener;
import com.tendcloud.tenddata.TCAgent;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 预警
 */
public class ShawnWarningActivity extends BaseActivity implements OnClickListener, AMapLocationListener, OnMapClickListener,
OnMarkerClickListener, InfoWindowAdapter, OnCameraChangeListener, OnMapScreenShotListener{
	
	private Context mContext;
	private LatLng locationLatLng = new LatLng(39.904030, 116.407526);
	private AVLoadingIndicatorView loadingView;
	private TextView tvTitle,tvWarningStatistic,tvNation;
	private RelativeLayout reShare,reWarningStatistic;
	private MapView mapView;//高德地图
	private AMap aMap;//高德地图
	private float zoom = 3.7f, zoom1 = 6.5f, zoom2 = 8.5f;
	private ArcMenu arcMenu;
	private boolean blue = true, yellow = true, orange = true, red = true;
	private List<WarningDto> warningList = new ArrayList<>();
	private List<WarningDto> nationList = new ArrayList<>();
	private Map<String, Marker> markerMap = new LinkedHashMap<>();//按html区分
	private boolean isExpandMap = false;//是否放大地图
	private ImageView ivList,ivStatistic,ivGuide,ivArrow;
    private LatLng leftlatlng = new LatLng(-16.305714763804854,75.13831436634065);
    private LatLng rightLatlng = new LatLng(63.681687310440864,135.21788656711578);
	private Marker locationMarker,selectMarker;
	private boolean isShowPrompt = true;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

	//预警统计列表
	private ListView listView;
	private ShawnWarningStatisticAdapter statisticAdapter;
	private List<WarningDto> statisticList = new ArrayList<>();
	
	//国家级预警图层
	private HashMap<String, String> nationMap = new HashMap<>();
	private LinearLayout llLegend;
	private ImageView iv1, iv2, iv3, iv4, iv5, iv6, iv7, iv8, iv9, iv10, ivLegend1, ivLegend2, ivLegend3, ivLegend4, ivLegend5, ivLegend7, ivLegend8, ivLegend9, ivLegend10;
	private boolean flag1 = false, flag2 = false, flag3 = false, flag4 = false, flag5 = false, flag6 = false, flag7 = false, flag8 = false, flag9 = false, flag10 = false;
	private String warningType1 = "fog", warningType2 = "baoyu", warningType3 = "shachen", warningType4 = "daxue", warningType5 = "gaowen",
			warningType7 = "lengkongqi", warningType8 = "lengkongqi", warningType9 = "wind", warningType10 = "thunderstorm";
	private List<Polyline> polyline11 = new ArrayList<>();
	private List<Polyline> polyline12 = new ArrayList<>();
	private List<Polygon> polygons13 = new ArrayList<>();
	private List<Polyline> polyline21 = new ArrayList<>();
	private List<Polyline> polyline22 = new ArrayList<>();
	private List<Polygon> polygons23 = new ArrayList<>();
	private List<Polyline> polyline31 = new ArrayList<>();
	private List<Polyline> polyline32 = new ArrayList<>();
	private List<Polygon> polygons33 = new ArrayList<>();
	private List<Polyline> polyline41 = new ArrayList<>();
	private List<Polyline> polyline42 = new ArrayList<>();
	private List<Polygon> polygons43 = new ArrayList<>();
	private List<Polyline> polyline51 = new ArrayList<>();
	private List<Polyline> polyline52 = new ArrayList<>();
	private List<Polygon> polygons53 = new ArrayList<>();
	private List<Polyline> polyline71 = new ArrayList<>();
	private List<Polyline> polyline72 = new ArrayList<>();
	private List<Polygon> polygons73 = new ArrayList<>();
	private List<Polyline> polyline81 = new ArrayList<>();
	private List<Polyline> polyline82 = new ArrayList<>();
	private List<Polygon> polygons83 = new ArrayList<>();
	private List<Polyline> polyline91 = new ArrayList<>();
	private List<Polyline> polyline92 = new ArrayList<>();
	private List<Polygon> polygons93 = new ArrayList<>();
	private List<Polyline> polyline101 = new ArrayList<>();
	private List<Polyline> polyline102 = new ArrayList<>();
	private List<Polygon> polygons103 = new ArrayList<>();
	private List<TyphoonDto> typhoonList = new ArrayList<>();
	private List<Marker> typhoonMarkers = new ArrayList<>();//台风markers
	private String markerType1 = "WARNING", markerType2 = "TYPHOON";//marker类型
	private MyBroadCastReceiver mReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_warning);
		mContext = this;
		initBroadCast();
		initAmap(savedInstanceState);
		initWidget();
		initListView();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
        loadingView = findViewById(R.id.loadingView);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvWarningStatistic = findViewById(R.id.tvWarningStatistic);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		reShare = findViewById(R.id.reShare);
		arcMenu = findViewById(R.id.arcMenu);
		arcMenu.setOnMenuItemClickListener(arcMenuListener);
		ImageView ivLocation = findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		ImageView ivRefresh = findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		ivList = findViewById(R.id.ivList);
		ivList.setOnClickListener(this);
		ivStatistic = findViewById(R.id.ivStatistic);
		ivStatistic.setOnClickListener(this);
		reWarningStatistic = findViewById(R.id.reWarningStatistic);
		reWarningStatistic.setOnClickListener(this);
		ivArrow = findViewById(R.id.ivArrow);
		ivArrow.setOnClickListener(this);
		tvNation = findViewById(R.id.tvNation);
		tvNation.setOnClickListener(this);
		ivGuide = findViewById(R.id.ivGuide);
		ivGuide.setOnClickListener(this);
		llLegend = findViewById(R.id.llLegend);
		iv1 = findViewById(R.id.iv1);
		iv1.setOnClickListener(this);
		iv2 = findViewById(R.id.iv2);
		iv2.setOnClickListener(this);
		iv3 = findViewById(R.id.iv3);
		iv3.setOnClickListener(this);
		iv4 = findViewById(R.id.iv4);
		iv4.setOnClickListener(this);
		iv5 = findViewById(R.id.iv5);
		iv5.setOnClickListener(this);
		iv6 = findViewById(R.id.iv6);
		iv6.setOnClickListener(this);
		iv7 = findViewById(R.id.iv7);
		iv7.setOnClickListener(this);
		iv8 = findViewById(R.id.iv8);
		iv8.setOnClickListener(this);
		iv9 = findViewById(R.id.iv9);
		iv9.setOnClickListener(this);
		iv10 = findViewById(R.id.iv10);
		iv10.setOnClickListener(this);
		ivLegend1 = findViewById(R.id.ivLegend1);
		ivLegend2 = findViewById(R.id.ivLegend2);
		ivLegend3 = findViewById(R.id.ivLegend3);
		ivLegend4 = findViewById(R.id.ivLegend4);
		ivLegend5 = findViewById(R.id.ivLegend5);
		ivLegend7 = findViewById(R.id.ivLegend7);
		ivLegend8 = findViewById(R.id.ivLegend8);
		ivLegend9 = findViewById(R.id.ivLegend9);
		ivLegend10 = findViewById(R.id.ivLegend10);

        refresh();
		CommonUtil.showGuidePage(mContext, this.getClass().getName(), ivGuide);
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
    }
	
	private void refresh() {
		loadingView.setVisibility(View.VISIBLE);
		OkHttpWarning();
		OkHttpTyphoonList();
	}
	
	/**
	 * 初始化高德地图
	 */
	private void initAmap(Bundle bundle) {
		mapView = findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMapClickListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnCameraChangeListener(this);
		aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
			@Override
			public void onMapLoaded() {
				startLocation();
				addLocationMarker();
			}
		});
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
			locationLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
			addLocationMarker();
        }
	}

	/**
	 * 添加定位标记
	 */
	private void addLocationMarker() {
		if (locationLatLng == null) {
			return;
		}
		MarkerOptions options = new MarkerOptions();
		options.position(locationLatLng);
		options.anchor(0.5f, 1.0f);
		Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.shawn_icon_map_location),
				(int)(CommonUtil.dip2px(mContext, 21)), (int)(CommonUtil.dip2px(mContext, 32)));
		if (bitmap != null) {
			options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
		}else {
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.shawn_icon_map_location));
		}
		if (locationMarker != null) {
			locationMarker.remove();
		}
		locationMarker = aMap.addMarker(options);
		locationMarker.setClickable(false);
	}
	
	/**
	 * 获取预警信息
	 */
	private void OkHttpWarning() {
		final String url = "http://decision-admin.tianqi.cn/Home/extra/getwarns?order=0";
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
										final JSONObject object = new JSONObject(result);
										if (object != null) {
											warningList.clear();
											nationList.clear();
											if (!object.isNull("data")) {
												JSONArray jsonArray = object.getJSONArray("data");
												for (int i = 0; i < jsonArray.length(); i++) {
													JSONArray tempArray = jsonArray.getJSONArray(i);
													WarningDto dto = new WarningDto();
													dto.html = tempArray.getString(1);
													String[] array = dto.html.split("-");
													String item0 = array[0];
													String item1 = array[1];
													String item2 = array[2];

													dto.item0 = item0;
													dto.provinceId = item0.substring(0, 2);
													dto.type = item2.substring(0, 5);
													dto.color = item2.substring(5, 7);
													dto.time = item1;
													dto.lng = tempArray.getDouble(2);
													dto.lat = tempArray.getDouble(3);
													dto.name = tempArray.getString(0);

													if (!dto.name.contains("解除")) {
														warningList.add(dto);
													}

													if (!TextUtils.isEmpty(item0)) {
														if (!dto.name.contains("解除")) {
															if (TextUtils.equals(item0, "000000")) {
																nationList.add(dto);
																nationMap.put(dto.type, dto.type);
															}
														}
													}
												}

												addWarningMarkers();

												try {
													String count = warningList.size()+"";
													if (TextUtils.equals(count, "0")) {
														String time = "";
														if (!object.isNull("time")) {
															long t = object.getLong("time");
															time = sdf3.format(new Date(t*1000));
														}
														tvWarningStatistic.setText(time+", "+"当前生效预警"+count+"条");
														ivList.setVisibility(View.GONE);
														ivStatistic.setVisibility(View.GONE);
														arcMenu.setVisibility(View.GONE);
														reWarningStatistic.setVisibility(View.VISIBLE);
														loadingView.setVisibility(View.GONE);
														return;
													}

													String time = "";
													if (!object.isNull("time")) {
														long t = object.getLong("time");
														time = sdf3.format(new Date(t*1000));
													}
													String str1 = time+", "+"当前生效预警";
													String str2 = "条";
													String warningInfo = str1+count+str2;
													SpannableStringBuilder builder = new SpannableStringBuilder(warningInfo);
													ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.text_color3));
													ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.red));
													ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(getResources().getColor(R.color.text_color3));
													builder.setSpan(builderSpan1, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
													builder.setSpan(builderSpan2, str1.length(), str1.length()+count.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
													builder.setSpan(builderSpan3, str1.length()+count.length(), str1.length()+count.length()+str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
													tvWarningStatistic.setText(builder);
													ivList.setVisibility(View.VISIBLE);
													ivStatistic.setVisibility(View.VISIBLE);
													arcMenu.setVisibility(View.VISIBLE);
													reWarningStatistic.setVisibility(View.VISIBLE);
													loadingView.setVisibility(View.GONE);

													if (nationList.size() > 0) {
														tvNation.setText("国家级预警"+nationList.size()+"条");
														tvNation.setVisibility(View.VISIBLE);

														handlerNationWarning();
													}else {
														tvNation.setVisibility(View.GONE);
													}

													//计算统计列表信息
													int rnation = 0;int rpro = 0;int rcity = 0;int rdis = 0;
													int onation = 0;int opro = 0;int ocity = 0;int odis = 0;
													int ynation = 0;int ypro = 0;int ycity = 0;int ydis = 0;
													int bnation = 0;int bpro = 0;int bcity = 0;int bdis = 0;
													for (int i = 0; i < warningList.size(); i++) {
														WarningDto dto = warningList.get(i);
														if (TextUtils.equals(dto.color, "04")) {
															if (TextUtils.equals(dto.item0, "000000")) {
																rnation += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
																rpro += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
																rcity += 1;
															}else {
																rdis += 1;
															}
														}else if (TextUtils.equals(dto.color, "03")) {
															if (TextUtils.equals(dto.item0, "000000")) {
																onation += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
																opro += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
																ocity += 1;
															}else {
																odis += 1;
															}
														}else if (TextUtils.equals(dto.color, "02")) {
															if (TextUtils.equals(dto.item0, "000000")) {
																ynation += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
																ypro += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
																ycity += 1;
															}else {
																ydis += 1;
															}
														}else if (TextUtils.equals(dto.color, "01")) {
															if (TextUtils.equals(dto.item0, "000000")) {
																bnation += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
																bpro += 1;
															}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
																bcity += 1;
															}else {
																bdis += 1;
															}
														}
													}

													statisticList.clear();
													WarningDto wDto = new WarningDto();
													wDto.colorName = "预警"+warningList.size();
													wDto.nationCount = "国家级"+(rnation+onation+ynation+bnation);
													wDto.proCount = "省级"+(rpro+opro+ypro+bpro);
													wDto.cityCount = "市级"+(rcity+ocity+ycity+bcity);
													wDto.disCount = "县级"+(rdis+odis+ydis+bdis);
													statisticList.add(wDto);

													wDto = new WarningDto();
													wDto.colorName = "红"+(rnation+rpro+rcity+rdis);
													wDto.nationCount = rnation+"";
													wDto.proCount = rpro+"";
													wDto.cityCount = rcity+"";
													wDto.disCount = rdis+"";
													statisticList.add(wDto);

													wDto = new WarningDto();
													wDto.colorName = "橙"+(onation+opro+ocity+odis);
													wDto.nationCount = onation+"";
													wDto.proCount = opro+"";
													wDto.cityCount = ocity+"";
													wDto.disCount = odis+"";
													statisticList.add(wDto);

													wDto = new WarningDto();
													wDto.colorName = "黄"+(ynation+ypro+ycity+ydis);
													wDto.nationCount = ynation+"";
													wDto.proCount = ypro+"";
													wDto.cityCount = ycity+"";
													wDto.disCount = ydis+"";
													statisticList.add(wDto);

													wDto = new WarningDto();
													wDto.colorName = "蓝"+(bnation+bpro+bcity+bdis);
													wDto.nationCount = bnation+"";
													wDto.proCount = bpro+"";
													wDto.cityCount = bcity+"";
													wDto.disCount = bdis+"";
													statisticList.add(wDto);

													if (statisticAdapter != null) {
														statisticAdapter.notifyDataSetChanged();
													}
												} catch (Exception e) {
													e.printStackTrace();
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
	 * 处理国家级预警
	 */
	private void handlerNationWarning() {
		if (nationMap.containsKey("11B17")) {//雾
			iv1.setVisibility(View.VISIBLE);
		}
		if (nationMap.containsKey("11B03")) {//暴雨
			iv2.setVisibility(View.VISIBLE);
		}
		if (nationMap.containsKey("11B07")) {//沙尘
			iv3.setVisibility(View.VISIBLE);
		}
		if (nationMap.containsKey("11B04")) {//暴雪
			iv4.setVisibility(View.VISIBLE);
		}
		if (nationMap.containsKey("11B09")) {//高温
			iv5.setVisibility(View.VISIBLE);
		}
		if (nationMap.containsKey("11B06")) {//冷空气
			iv7.setVisibility(View.VISIBLE);
		}
		if (nationMap.containsKey("11B05")) {//寒潮
			iv8.setVisibility(View.VISIBLE);
		}
		if (nationMap.containsKey("11B23")) {//海上大风
			iv9.setVisibility(View.VISIBLE);
		}
		if (nationMap.containsKey("11B31")) {//强对流
			iv10.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 在地图上添加marker
	 */
	private void addWarningMarkers() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Map<String, Marker> markers = new LinkedHashMap<>();//为防止ConcurrentModificationException异常，待循环执行完毕后，再对markerMap进行修改
				for (WarningDto dto : warningList) {
					if (markerMap.containsKey(dto.html)) {
						Marker m = markerMap.get(dto.html);
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
	private void addVisibleAreaMarker(WarningDto dto, Map<String, Marker> markers) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (dto.lat > leftlatlng.latitude && dto.lat < rightLatlng.latitude && dto.lng > leftlatlng.longitude && dto.lng < rightLatlng.longitude) {
			MarkerOptions optionsTemp = new MarkerOptions();
			optionsTemp.title(dto.lat+","+dto.lng+","+dto.item0+","+dto.color);
			optionsTemp.snippet(markerType1);
			optionsTemp.anchor(0.5f, 0.5f);
			optionsTemp.position(new LatLng(dto.lat, dto.lng));
			View mView = inflater.inflate(R.layout.shawn_warning_marker_icon, null);
			ImageView ivMarker = mView.findViewById(R.id.ivMarker);
			Bitmap bitmap = null;
			if (dto.color.equals(CONST.blue[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.blue[1]+CONST.imageSuffix);
			}else if (dto.color.equals(CONST.yellow[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.yellow[1]+CONST.imageSuffix);
			}else if (dto.color.equals(CONST.orange[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.orange[1]+CONST.imageSuffix);
			}else if (dto.color.equals(CONST.red[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.red[1]+CONST.imageSuffix);
			}
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.imageSuffix);
			}
			ivMarker.setImageBitmap(bitmap);
			optionsTemp.icon(BitmapDescriptorFactory.fromView(mView));
			Marker marker = null;
            if (zoom <= zoom1) {
                if (dto.item0.endsWith("0000")) {
                    if (TextUtils.equals(dto.color, "01") && blue) {
                        marker = aMap.addMarker(optionsTemp);
                    }else if (TextUtils.equals(dto.color, "02") && yellow) {
                        marker = aMap.addMarker(optionsTemp);
                    }else if (TextUtils.equals(dto.color, "03") && orange) {
                        marker = aMap.addMarker(optionsTemp);
                    }else if (TextUtils.equals(dto.color, "04") && red) {
                        marker = aMap.addMarker(optionsTemp);
                    }
                }
            }else if (zoom > zoom1 && zoom <= zoom2) {
                if (dto.item0.endsWith("00")) {
                    if (TextUtils.equals(dto.color, "01") && blue) {
                        marker = aMap.addMarker(optionsTemp);
                    }else if (TextUtils.equals(dto.color, "02") && yellow) {
                        marker = aMap.addMarker(optionsTemp);
                    }else if (TextUtils.equals(dto.color, "03") && orange) {
                        marker = aMap.addMarker(optionsTemp);
                    }else if (TextUtils.equals(dto.color, "04") && red) {
                        marker = aMap.addMarker(optionsTemp);
                    }
                }
            }else {
                if (TextUtils.equals(dto.color, "01") && blue) {
                    marker = aMap.addMarker(optionsTemp);
                }else if (TextUtils.equals(dto.color, "02") && yellow) {
                    marker = aMap.addMarker(optionsTemp);
                }else if (TextUtils.equals(dto.color, "03") && orange) {
                    marker = aMap.addMarker(optionsTemp);
                }else if (TextUtils.equals(dto.color, "04") && red) {
                    marker = aMap.addMarker(optionsTemp);
                }
            }
			if (marker != null) {
                markers.put(dto.html, marker);
                markerExpandAnimation(marker);
            }
		}
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

	/**
	 * 绘制预警markers
	 */
	private void switchWarningMarkers() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (String html : markerMap.keySet()) {
					if (!TextUtils.isEmpty(html) && markerMap.containsKey(html)) {
						Marker marker = markerMap.get(html);
						String[] title = marker.getTitle().split(",");
						double lat = marker.getPosition().latitude;
						double lng = marker.getPosition().longitude;
						String item0 = title[2];
						String color = title[3];
						if (zoom <= zoom1) {
							if (!item0.endsWith("0000")) {
								marker.remove();
							}
						}else if (zoom > zoom1 && zoom <= zoom2) {
							if (!item0.endsWith("00")) {
								marker.remove();
							}
						}else {

						}
						if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
							//已经在可是范围内则不处理
							if (TextUtils.equals(color, "01") && blue) {
							}else if (TextUtils.equals(color, "02") && yellow) {
							}else if (TextUtils.equals(color, "03") && orange) {
							}else if (TextUtils.equals(color, "04") && red) {
							}else {
								marker.remove();
							}
						}else {
							marker.remove();
						}
					}
				}
			}
		}).start();
	}

	/**
	 * 筛选红橙黄蓝
	 * @param color
	 */
	private void screenWarningMarkers(final String color) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (String html : markerMap.keySet()) {
					if (!TextUtils.isEmpty(html) && markerMap.containsKey(html)) {
						Marker marker = markerMap.get(html);
						String[] title = marker.getTitle().split(",");
						String item0 = title[2];
						String warningColor = title[3];

						if (zoom <= zoom1) {
							if (!item0.endsWith("0000") || TextUtils.equals(warningColor, color)) {
								marker.remove();
							}
						}else if (zoom > zoom1 && zoom <= zoom2) {
							if (item0.endsWith("00") && TextUtils.equals(warningColor, color)) {
								marker.remove();
							}
						}else {
						}
					}
				}
			}
		}).start();
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		if (selectMarker != null) {
			selectMarker.hideInfoWindow();
			setNormalEmit("12", "");
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker != null && marker != locationMarker) {
			selectMarker = marker;
			if (selectMarker.isInfoWindowShown()) {
				selectMarker.hideInfoWindow();
			}else {
				selectMarker.showInfoWindow();
			}
		}
		return true;
	}
	
	@Override
	public View getInfoContents(final Marker marker) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mView = null;
		if (TextUtils.equals(marker.getSnippet(), markerType1)) {//预警marker
			mView = inflater.inflate(R.layout.shawn_warning_marker_icon_info, null);
			final List<WarningDto> infoList = addInfoList(marker);
			ListView mListView = mView.findViewById(R.id.listView);
			ShawnWarningAdapter mAdapter = new ShawnWarningAdapter(mContext, infoList, true);
			mListView.setAdapter(mAdapter);
			LayoutParams params = mListView.getLayoutParams();
			if (infoList.size() == 1) {
				params.height = (int) CommonUtil.dip2px(mContext, 50);
			}else if (infoList.size() == 2) {
				params.height = (int) CommonUtil.dip2px(mContext, 100);
			}else if (infoList.size() > 2){
				params.height = (int) CommonUtil.dip2px(mContext, 150);
			}
			mListView.setLayoutParams(params);
			mListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					intentDetail(infoList.get(arg2));
				}
			});

			setSelectEmit("11", infoList.get(0));
		}else if (TextUtils.equals(marker.getSnippet(), markerType2)) {
			mView = inflater.inflate(R.layout.shawn_typhoon_marker_icon_info, null);
			TextView tvName = mView.findViewById(R.id.tvName);
			TextView tvInfo = mView.findViewById(R.id.tvInfo);
			ImageView ivDelete = mView.findViewById(R.id.ivDelete);
			if (!TextUtils.isEmpty(marker.getTitle())) {
				String[] str = marker.getTitle().split("\\|");
				if (!TextUtils.isEmpty(str[0])) {
					tvName.setText(str[0]);
				}
				if (!TextUtils.isEmpty(str[1])) {
					tvInfo.setText(str[1]);
				}
			}
			ivDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					marker.hideInfoWindow();
				}
			});
		}

		return mView;
	}

	private List<WarningDto> addInfoList(Marker marker) {
		List<WarningDto> infoList = new ArrayList<>();
		for (WarningDto dto : warningList) {
			String[] latLng = marker.getTitle().split(",");
			if (TextUtils.equals(latLng[0], dto.lat+"") && TextUtils.equals(latLng[1], dto.lng+"")) {
				infoList.add(dto);
			}
		}
		return infoList;
	}
	
	private void intentDetail(WarningDto data) {
		Intent intentDetail = new Intent(mContext, ShawnWarningDetailActivity.class);
		intentDetail.putExtra(CONST.COLUMN_ID, columnId);
		Bundle bundle = new Bundle();
		bundle.putParcelable("data", data);
		intentDetail.putExtras(bundle);
		startActivity(intentDetail);

		setDetailEmit("9", data.html);
	}
	
	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}
	
	private OnMenuItemClickListener arcMenuListener = new OnMenuItemClickListener() {
		@Override
		public void onClick(View view, int pos) {
			if (pos == 0) {
				blue = !blue;
				if (!blue) {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_blue_press);
				}else {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_blue);
				}
				screenWarningMarkers("01");
				setNormalEmit("5", "1");
			}else if (pos == 1) {
				yellow = !yellow;
				if (!yellow) {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_yellow_press);
				}else {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_yellow);
				}
				screenWarningMarkers("02");
				setNormalEmit("5", "2");
			}else if (pos == 2) {
				orange = !orange;
				if (!orange) {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_orange_press);
				}else {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_orange);
				}
				screenWarningMarkers("03");
				setNormalEmit("5", "3");
			}else if (pos == 3) {
				red = !red;
				if (!red) {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_red_press);
				}else {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_red);
				}
				screenWarningMarkers("04");
				setNormalEmit("5", "4");
			}
		}
	};
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
	}
	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		zoom = arg0.zoom;
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);

		switchWarningMarkers();
		addWarningMarkers();

		setMapEmit(arg0.zoom, arg0.target.latitude, arg0.target.longitude);
	}
	
	/**
	 * 初始化预警统计列表
	 */
	private void initListView() {
		listView = findViewById(R.id.listView);
		statisticAdapter = new ShawnWarningStatisticAdapter(mContext, statisticList);
		listView.setAdapter(statisticAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				clickPromptWarning();
			}
		});
	}
	
	/**
     * 隐藏或显示ListView的动画 
     */  
    public void hideOrShowListViewAnimator(final View view, final int startValue,final int endValue){  
        //1.设置属性的初始值和结束值  
        ValueAnimator mAnimator = ValueAnimator.ofInt(0,100);
        //2.为目标对象的属性变化设置监听器  
        mAnimator.addUpdateListener(new AnimatorUpdateListener() {  
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
    
    private void clickPromptWarning() {
		setNormalEmit("1", "");
    	int height = CommonUtil.getListViewHeightBasedOnChildren(listView);
		isShowPrompt = !isShowPrompt;
		if (!isShowPrompt) {
			ivArrow.setImageResource(R.drawable.shawn_icon_arrow_up_black);
			hideOrShowListViewAnimator(listView, 0, height);
		}else {
			ivArrow.setImageResource(R.drawable.shawn_icon_arrow_down_black);
			hideOrShowListViewAnimator(listView, height, 0);
		}
    }
    
    @Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		Bitmap bitmap2 = CommonUtil.captureView(reShare);
		Bitmap bitmap3 = CommonUtil.mergeBitmap(ShawnWarningActivity.this, bitmap1, bitmap2, true);
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap2);
		Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
		Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
		CommonUtil.clearBitmap(bitmap3);
		CommonUtil.clearBitmap(bitmap4);
		CommonUtil.share(ShawnWarningActivity.this, bitmap);
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
		case R.id.ivGuide:
			ivGuide.setVisibility(View.GONE);
			CommonUtil.saveGuidePageState(mContext, this.getClass().getName());
			break;
		case R.id.reWarningStatistic:
		case R.id.ivArrow:
			clickPromptWarning();
			break;
		case R.id.ivLocation:
			isExpandMap = !isExpandMap;
			if (isExpandMap) {
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 10.0f));
			}else {
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), 3.5f));
			}
			break;
		case R.id.ivRefresh:
			refresh();
			break;
		case R.id.ivList:
			setNormalEmit("6", "");
			Intent intent = new Intent(mContext, ShawnWarningListActivity.class);
			intent.putExtra(CONST.COLUMN_ID, columnId);
			intent.putExtra("isVisible", true);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) warningList);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivStatistic:
			startActivity(new Intent(mContext, ShawnWarningStatisticActivity.class));
			break;
		case R.id.tvNation:
			setNormalEmit("2", "");
			intent = new Intent(mContext, ShawnHeadWarningActivity.class);
			bundle = new Bundle();
			bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) nationList);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivShare:
			aMap.getMapScreenShot(ShawnWarningActivity.this);
			break;
		case R.id.iv1:
			if (!flag1) {
				drawWarningLayer(SecretUrlUtil.warningLayer(warningType1), warningType1);
				flag1 = true;
				iv1.setImageResource(R.drawable.shawn_icon_warning_fog_open);
				ivLegend1.setVisibility(View.VISIBLE);
			}else {
				removeWarningLayer(warningType1);
				flag1 = false;
				iv1.setImageResource(R.drawable.shawn_icon_warning_fog_close);
				ivLegend1.setVisibility(View.GONE);
			}
			break;
		case R.id.iv2:
			if (!flag2) {
				drawWarningLayer(SecretUrlUtil.warningLayer(warningType2), warningType2);
				flag2 = true;
				iv2.setImageResource(R.drawable.shawn_icon_warning_rain_open);
				ivLegend2.setVisibility(View.VISIBLE);
			}else {
				removeWarningLayer(warningType2);
				flag2 = false;
				iv2.setImageResource(R.drawable.shawn_icon_warning_rain_close);
				ivLegend2.setVisibility(View.GONE);
			}
			break;
		case R.id.iv3:
			if (!flag3) {
				drawWarningLayer(SecretUrlUtil.warningLayer(warningType3), warningType3);
				flag3 = true;
				iv3.setImageResource(R.drawable.shawn_icon_warning_sand_open);
				ivLegend3.setVisibility(View.VISIBLE);
			}else {
				removeWarningLayer(warningType3);
				flag3 = false;
				iv3.setImageResource(R.drawable.shawn_icon_warning_sand_close);
				ivLegend3.setVisibility(View.GONE);
			}
			break;
		case R.id.iv4:
			if (!flag4) {
				drawWarningLayer(SecretUrlUtil.warningLayer(warningType4), warningType4);
				flag4 = true;
				iv4.setImageResource(R.drawable.shawn_icon_warning_snow_open);
				ivLegend4.setVisibility(View.VISIBLE);
			}else {
				removeWarningLayer(warningType4);
				flag4 = false;
				iv4.setImageResource(R.drawable.shawn_icon_warning_snow_close);
				ivLegend4.setVisibility(View.GONE);
			}
			break;
		case R.id.iv5:
			if (!flag5) {
				drawWarningLayer(SecretUrlUtil.warningLayer(warningType5), warningType5);
				flag5 = true;
				iv5.setImageResource(R.drawable.shawn_icon_warning_temp_open);
				ivLegend5.setVisibility(View.VISIBLE);
			}else {
				removeWarningLayer(warningType5);
				flag5 = false;
				iv5.setImageResource(R.drawable.shawn_icon_warning_temp_close);
				ivLegend5.setVisibility(View.GONE);
			}
			break;
		case R.id.iv6:
			if (!flag6) {
				for (int i = 0; i < typhoonMarkers.size(); i++) {
					typhoonMarkers.get(i).setVisible(true);
				}
				flag6 = true;
				iv6.setImageResource(R.drawable.shawn_icon_warning_typhoon_open);
			}else {
				for (int i = 0; i < typhoonMarkers.size(); i++) {
					typhoonMarkers.get(i).setVisible(false);
				}
				flag6 = false;
				iv6.setImageResource(R.drawable.shawn_icon_warning_typhoon_close);
			}
			break;
		case R.id.iv7:
			if (!flag7) {
				drawWarningLayer(SecretUrlUtil.warningLayer(warningType7), warningType7);
				flag7 = true;
				iv7.setImageResource(R.drawable.shawn_icon_warning_wind_open);
				ivLegend7.setVisibility(View.VISIBLE);
			}else {
				removeWarningLayer(warningType7);
				flag7 = false;
				iv7.setImageResource(R.drawable.shawn_icon_warning_wind_open);
				ivLegend7.setVisibility(View.GONE);
			}
			break;
		case R.id.iv8:
			if (!flag8) {
				drawWarningLayer(SecretUrlUtil.warningLayer(warningType8), warningType8);
				flag8 = true;
				iv8.setImageResource(R.drawable.shawn_icon_warning_hanchao_open);
				ivLegend8.setVisibility(View.VISIBLE);
			}else {
				removeWarningLayer(warningType8);
				flag8 = false;
				iv8.setImageResource(R.drawable.shawn_icon_warning_hanchao_close);
				ivLegend8.setVisibility(View.GONE);
			}
			break;
		case R.id.iv9:
			if (!flag9) {
				drawWarningLayer(SecretUrlUtil.warningLayer(warningType9), warningType9);
				flag9 = true;
				iv9.setImageResource(R.drawable.shawn_icon_warning_wind_open);
				ivLegend9.setVisibility(View.VISIBLE);
			}else {
				removeWarningLayer(warningType9);
				flag9 = false;
				iv9.setImageResource(R.drawable.shawn_icon_warning_wind_close);
				ivLegend9.setVisibility(View.GONE);
			}
			break;
		case R.id.iv10:
			if (!flag10) {
				drawWarningLayer(SecretUrlUtil.warningLayer(warningType10), warningType10);
				flag10 = true;
				iv10.setImageResource(R.drawable.shawn_icon_warning_qiangduiliu_open);
				ivLegend10.setVisibility(View.VISIBLE);
			}else {
				removeWarningLayer(warningType10);
				flag10 = false;
				iv10.setImageResource(R.drawable.shawn_icon_warning_qiangduiliu_close);
				ivLegend10.setVisibility(View.GONE);
			}
			break;

		default:
			break;
		}
	}

	private void initBroadCast() {
    	mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ArcMenu.BROADCASTCLICK);
		registerReceiver(mReceiver, intentFilter);
	}

	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ArcMenu.BROADCASTCLICK)) {
				Bundle bundle = intent.getExtras();
				boolean status = bundle.getBoolean("STATUS");
				if (!status) {
					narrowAnimation(llLegend);
				}else {
					enlargeAnimation(llLegend);
				}
			}
		}
	}

	/**
	 * 缩小动画
	 */
	private void narrowAnimation(View view) {
		if (view == null) {
			return;
		}
		android.view.animation.ScaleAnimation animation = new android.view.animation.ScaleAnimation(
				1.0f, 0.7f, 1.0f, 0.7f, android.view.animation.Animation.RELATIVE_TO_SELF, 1.0f, android.view.animation.Animation.RELATIVE_TO_SELF, 1.0f
		);
		animation.setDuration(200);
		animation.setFillAfter(true);
		view.startAnimation(animation);
	}

	/**
	 * 放大动画
	 */
	private void enlargeAnimation(View view) {
		if (view == null) {
			return;
		}
		android.view.animation.ScaleAnimation animation = new android.view.animation.ScaleAnimation(
				0.7f, 1.0f, 0.7f, 1.0f, android.view.animation.Animation.RELATIVE_TO_SELF, 1.0f, android.view.animation.Animation.RELATIVE_TO_SELF, 1.0f
		);
		animation.setDuration(200);
		animation.setFillAfter(true);
		view.startAnimation(animation);
	}

	/**
	 * 清除预警图层
	 */
	private void removeWarningLayer(String type) {
		if (TextUtils.equals(type, warningType1)) {
			for (int i = 0; i < polyline11.size(); i++) {
				polyline11.get(i).remove();
			}
			polyline11.clear();

			for (int i = 0; i < polyline12.size(); i++) {
				polyline12.get(i).remove();
			}
			polyline12.clear();

			for (int i = 0; i < polygons13.size(); i++) {
				polygons13.get(i).remove();
			}
			polygons13.clear();
		}else if (TextUtils.equals(type, warningType2)) {
			for (int i = 0; i < polyline21.size(); i++) {
				polyline21.get(i).remove();
			}
			polyline21.clear();

			for (int i = 0; i < polyline22.size(); i++) {
				polyline22.get(i).remove();
			}
			polyline22.clear();

			for (int i = 0; i < polygons23.size(); i++) {
				polygons23.get(i).remove();
			}
			polygons23.clear();
		}else if (TextUtils.equals(type, warningType3)) {
			for (int i = 0; i < polyline31.size(); i++) {
				polyline31.get(i).remove();
			}
			polyline31.clear();

			for (int i = 0; i < polyline32.size(); i++) {
				polyline32.get(i).remove();
			}
			polyline32.clear();

			for (int i = 0; i < polygons33.size(); i++) {
				polygons33.get(i).remove();
			}
			polygons33.clear();
		}else if (TextUtils.equals(type, warningType4)) {
			for (int i = 0; i < polyline41.size(); i++) {
				polyline41.get(i).remove();
			}
			polyline41.clear();

			for (int i = 0; i < polyline42.size(); i++) {
				polyline42.get(i).remove();
			}
			polyline42.clear();

			for (int i = 0; i < polygons43.size(); i++) {
				polygons43.get(i).remove();
			}
			polygons43.clear();
		}else if (TextUtils.equals(type, warningType5)) {
			for (int i = 0; i < polyline51.size(); i++) {
				polyline51.get(i).remove();
			}
			polyline51.clear();

			for (int i = 0; i < polyline52.size(); i++) {
				polyline52.get(i).remove();
			}
			polyline52.clear();

			for (int i = 0; i < polygons53.size(); i++) {
				polygons53.get(i).remove();
			}
			polygons53.clear();
		}else if (TextUtils.equals(type, warningType7)) {
			for (int i = 0; i < polyline71.size(); i++) {
				polyline71.get(i).remove();
			}
			polyline71.clear();

			for (int i = 0; i < polyline72.size(); i++) {
				polyline72.get(i).remove();
			}
			polyline72.clear();

			for (int i = 0; i < polygons73.size(); i++) {
				polygons73.get(i).remove();
			}
			polygons73.clear();
		}else if (TextUtils.equals(type, warningType8)) {
			for (int i = 0; i < polyline81.size(); i++) {
				polyline81.get(i).remove();
			}
			polyline81.clear();

			for (int i = 0; i < polyline82.size(); i++) {
				polyline82.get(i).remove();
			}
			polyline82.clear();

			for (int i = 0; i < polygons83.size(); i++) {
				polygons83.get(i).remove();
			}
			polygons83.clear();
		}else if (TextUtils.equals(type, warningType9)) {
			for (int i = 0; i < polyline91.size(); i++) {
				polyline91.get(i).remove();
			}
			polyline91.clear();

			for (int i = 0; i < polyline92.size(); i++) {
				polyline92.get(i).remove();
			}
			polyline92.clear();

			for (int i = 0; i < polygons93.size(); i++) {
				polygons93.get(i).remove();
			}
			polygons93.clear();
		}else if (TextUtils.equals(type, warningType10)) {
			for (int i = 0; i < polyline101.size(); i++) {
				polyline101.get(i).remove();
			}
			polyline101.clear();

			for (int i = 0; i < polyline102.size(); i++) {
				polyline102.get(i).remove();
			}
			polyline102.clear();

			for (int i = 0; i < polygons103.size(); i++) {
				polygons103.get(i).remove();
			}
			polygons103.clear();
		}
	}

	/**
	 * 绘制预警图层
	 * @param url
	 */
	private void drawWarningLayer(String url, final String type) {
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
						if (!obj.isNull("micaps14_"+type)) {
							String dataUrl = obj.getString("micaps14_"+type);
							OkHttpSpecialLayer(dataUrl, type);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	private void OkHttpSpecialLayer(final String url, final String type) {
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
										if (!obj.isNull("lines")) {
											JSONArray lines = obj.getJSONArray("lines");
											for (int i = 0; i < lines.length(); i++) {
												JSONObject itemObj = lines.getJSONObject(i);
												if (!itemObj.isNull("point")) {
													JSONArray points = itemObj.getJSONArray("point");
													PolylineOptions polylineOption = new PolylineOptions();
													polylineOption.width(6).color(0xff406bbf);
													for (int j = 0; j < points.length(); j++) {
														JSONObject point = points.getJSONObject(j);
														double lat = point.getDouble("y");
														double lng = point.getDouble("x");
														polylineOption.add(new LatLng(lat, lng));
													}
													Polyline p = aMap.addPolyline(polylineOption);
													if (TextUtils.equals(type, warningType1)) {
														polyline11.add(p);
													}else if (TextUtils.equals(type, warningType2)) {
														polyline21.add(p);
													}else if (TextUtils.equals(type, warningType3)) {
														polyline31.add(p);
													}else if (TextUtils.equals(type, warningType4)) {
														polyline41.add(p);
													}else if (TextUtils.equals(type, warningType5)) {
														polyline51.add(p);
													}else if (TextUtils.equals(type, warningType7)) {
														polyline71.add(p);
													}else if (TextUtils.equals(type, warningType8)) {
														polyline81.add(p);
													}else if (TextUtils.equals(type, warningType9)) {
														polyline91.add(p);
													}else if (TextUtils.equals(type, warningType10)) {
														polyline101.add(p);
													}
												}
//							if (!itemObj.isNull("flags")) {
//								JSONObject flags = itemObj.getJSONObject("flags");
//								String text = "";
//								if (!flags.isNull("text")) {
//									text = flags.getString("text");
//								}
//								if (!flags.isNull("items")) {
//									JSONArray items = flags.getJSONArray("items");
//									JSONObject item = items.getJSONObject(0);
//									double lat = item.getDouble("y");
//									double lng = item.getDouble("x");
//									TextOptions to = new TextOptions();
//									to.position(new LatLng(lat, lng));
//									to.text(text);
//									to.fontColor(Color.BLACK);
//									to.fontSize(30);
//									to.backgroundColor(Color.TRANSPARENT);
//									Text t = aMap.addText(to);
//									textList1.add(t);
//								}
//							}
											}
										}
										if (!obj.isNull("line_symbols")) {
											JSONArray line_symbols = obj.getJSONArray("line_symbols");
											for (int i = 0; i < line_symbols.length(); i++) {
												JSONObject itemObj = line_symbols.getJSONObject(i);
												if (!itemObj.isNull("items")) {
													JSONArray items = itemObj.getJSONArray("items");
													PolylineOptions polylineOption = new PolylineOptions();
													polylineOption.width(6).color(0xff406bbf);
													for (int j = 0; j < items.length(); j++) {
														JSONObject item = items.getJSONObject(j);
														double lat = item.getDouble("y");
														double lng = item.getDouble("x");
														polylineOption.add(new LatLng(lat, lng));
													}
													Polyline p = aMap.addPolyline(polylineOption);
													if (TextUtils.equals(type, warningType1)) {
														polyline12.add(p);
													}else if (TextUtils.equals(type, warningType2)) {
														polyline22.add(p);
													}else if (TextUtils.equals(type, warningType3)) {
														polyline32.add(p);
													}else if (TextUtils.equals(type, warningType4)) {
														polyline42.add(p);
													}else if (TextUtils.equals(type, warningType5)) {
														polyline52.add(p);
													}else if (TextUtils.equals(type, warningType7)) {
														polyline72.add(p);
													}else if (TextUtils.equals(type, warningType8)) {
														polyline82.add(p);
													}else if (TextUtils.equals(type, warningType9)) {
														polyline92.add(p);
													}else if (TextUtils.equals(type, warningType10)) {
														polyline102.add(p);
													}
												}
											}
										}
//								if (!obj.isNull("symbols")) {
//									JSONArray symbols = obj.getJSONArray("symbols");
//									for (int i = 0; i < symbols.length(); i++) {
//										JSONObject itemObj = symbols.getJSONObject(i);
//										String text = "";
//										int color = Color.BLACK;
//										if (!itemObj.isNull("type")) {
//											String type = itemObj.getString("type");
//											if (TextUtils.equals(type, "60")) {
//												text = "H";
//												color = Color.RED;
//											}else if (TextUtils.equals(type, "61")) {
//												text = "L";
//												color = Color.BLUE;
//											}else if (TextUtils.equals(type, "37")) {
//												text = "台";
//												color = Color.GREEN;
//											}
//										}
//										double lat = itemObj.getDouble("y");
//										double lng = itemObj.getDouble("x");
//										TextOptions to = new TextOptions();
//										to.position(new LatLng(lat, lng));
//										to.text(text);
//										to.fontColor(color);
//										to.fontSize(60);
//										to.backgroundColor(Color.TRANSPARENT);
//										Text t = aMap.addText(to);
//										textList2.add(t);
//									}
//								}
										if (!obj.isNull("areas")) {
											JSONArray array = obj.getJSONArray("areas");
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												String color = itemObj.getString("c");
												if (color.contains("#")) {
													color = color.replace("#", "");
												}
												int r = Integer.parseInt(color.substring(0,2), 16);
												int g = Integer.parseInt(color.substring(2,4), 16);
												int b = Integer.parseInt(color.substring(4,6), 16);
												if (!itemObj.isNull("items")) {
													JSONArray items = itemObj.getJSONArray("items");
													PolygonOptions polygonOption = new PolygonOptions();
													polygonOption.strokeColor(Color.rgb(r, g, b)).fillColor(Color.rgb(r, g, b));
													for (int j = 0; j < items.length(); j++) {
														JSONObject item = items.getJSONObject(j);
														double lat = item.getDouble("y");
														double lng = item.getDouble("x");
														polygonOption.add(new LatLng(lat, lng));
													}
													Polygon p = aMap.addPolygon(polygonOption);
													if (TextUtils.equals(type, warningType1)) {
														polygons13.add(p);
													}else if (TextUtils.equals(type, warningType2)) {
														polygons23.add(p);
													}else if (TextUtils.equals(type, warningType3)) {
														polygons33.add(p);
													}else if (TextUtils.equals(type, warningType4)) {
														polygons43.add(p);
													}else if (TextUtils.equals(type, warningType5)) {
														polygons53.add(p);
													}else if (TextUtils.equals(type, warningType7)) {
														polygons73.add(p);
													}else if (TextUtils.equals(type, warningType8)) {
														polygons83.add(p);
													}else if (TextUtils.equals(type, warningType9)) {
														polygons93.add(p);
													}else if (TextUtils.equals(type, warningType10)) {
														polygons103.add(p);
													}
												}
//							if (!itemObj.isNull("symbols")) {
//								JSONObject symbols = itemObj.getJSONObject("symbols");
//								String text = symbols.getString("text");
//								JSONArray items = symbols.getJSONArray("items");
//								if (items.length() > 0) {
//									JSONObject o = items.getJSONObject(0);
//									double lat = o.getDouble("y");
//									double lng = o.getDouble("x");
//									TextOptions to = new TextOptions();
//									to.position(new LatLng(lat, lng));
//									to.text(text);
//									to.fontColor(Color.BLACK);
//									to.fontSize(30);
//									to.backgroundColor(Color.TRANSPARENT);
//									Text t = aMap.addText(to);
//									textList3.add(t);
//								}
//							}
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
	 * 获取当年的台风列表信息
	 */
	private void OkHttpTyphoonList() {
		int currentYear = Integer.valueOf(sdf1.format(new Date()));
		final String url = "http://decision-admin.tianqi.cn/Home/extra/gettyphoon/list/"+currentYear;
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
						final String requestResult = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(requestResult)) {
									String c = "(";
									String c2 = "})";
									String result = requestResult.substring(requestResult.indexOf(c)+c.length(), requestResult.indexOf(c2)+1);
									if (!TextUtils.isEmpty(result)) {
										try {
											JSONObject obj = new JSONObject(result);
											if (!obj.isNull("typhoonList")) {
												typhoonList.clear();
												JSONArray array = obj.getJSONArray("typhoonList");
												for (int i = 0; i < array.length(); i++) {
													JSONArray itemArray = array.getJSONArray(i);
													TyphoonDto dto = new TyphoonDto();
													dto.id = itemArray.getString(0);
													dto.enName = itemArray.getString(1);
													dto.name = itemArray.getString(2);
													dto.code = itemArray.getString(4);
													dto.status = itemArray.getString(7);
													//把活跃台风过滤出来存放
													if (TextUtils.equals(dto.status, "start")) {
														typhoonList.add(dto);
														if (TextUtils.isEmpty(dto.id)) {
															return;
														}
														String name = "";
														if (TextUtils.equals(dto.enName, "nameless")) {
															name = dto.code + " " + dto.enName;
														}else {
															name = dto.code + " " + dto.name + " " + dto.enName;
														}
														OkHttpTyphoonDetail("http://decision-admin.tianqi.cn/Home/extra/gettyphoon/view/"+dto.id, name);
													}
												}

												if (typhoonList.size() > 0) {
													iv6.setVisibility(View.VISIBLE);
												}

											}
										} catch (JSONException e) {
											e.printStackTrace();
										}

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
	 * 获取台风详情
	 */
	private void OkHttpTyphoonDetail(final String url, final String name) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
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
						final String requestResult = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(requestResult)) {
									String c = "(";
									String result = requestResult.substring(requestResult.indexOf(c)+c.length(), requestResult.indexOf(")"));
									if (!TextUtils.isEmpty(result)) {
										try {
											JSONObject obj = new JSONObject(result);
											if (!obj.isNull("typhoon")) {
												JSONArray array = obj.getJSONArray("typhoon");
												JSONArray itemArray = array.getJSONArray(8);
												if (itemArray.length() > 0) {
													JSONArray itemArray2 = itemArray.getJSONArray(itemArray.length()-1);
													TyphoonDto dto = new TyphoonDto();
													if (!TextUtils.isEmpty(name)) {
														dto.name = name;
													}
													long longTime = itemArray2.getLong(2);
													dto.time = sdf2.format(new Date(longTime));

													dto.lng = itemArray2.getDouble(4);
													dto.lat = itemArray2.getDouble(5);
													dto.pressure = itemArray2.getString(6);
													dto.max_wind_speed = itemArray2.getString(7);
													dto.move_speed = itemArray2.getString(9);
													String fx_string = itemArray2.getString(8);
													if( !TextUtils.isEmpty(fx_string)){
														String windDir = "";
														for (int i = 0; i < fx_string.length(); i++) {
															String item = fx_string.substring(i, i+1);
															if (TextUtils.equals(item, "N")) {
																item = "北";
															}else if (TextUtils.equals(item, "S")) {
																item = "南";
															}else if (TextUtils.equals(item, "W")) {
																item = "西";
															}else if (TextUtils.equals(item, "E")) {
																item = "东";
															}
															windDir = windDir+item;
														}
														dto.wind_dir = windDir;
													}

													String type = itemArray2.getString(3);
													if (TextUtils.equals(type, "TD")) {//热带低压
														type = "1";
													}else if (TextUtils.equals(type, "TS")) {//热带风暴
														type = "2";
													}else if (TextUtils.equals(type, "STS")) {//强热带风暴
														type = "3";
													}else if (TextUtils.equals(type, "TY")) {//台风
														type = "4";
													}else if (TextUtils.equals(type, "STY")) {//强台风
														type = "5";
													}else if (TextUtils.equals(type, "SuperTY")) {//超强台风
														type = "6";
													}
													dto.type = type;
													dto.isFactPoint = true;

													JSONArray array10 = itemArray2.getJSONArray(10);
													for (int m = 0; m < array10.length(); m++) {
														JSONArray itemArray10 = array10.getJSONArray(m);
														if (m == 0) {
															dto.radius_7 = itemArray10.getString(1);
														}else if (m == 1) {
															dto.radius_10 = itemArray10.getString(1);
														}
													}

													MarkerOptions tOption = new MarkerOptions();
													tOption.title(name+"|"+dto.content(mContext));
													tOption.snippet(markerType2);
													tOption.position(new LatLng(dto.lat, dto.lng));
													tOption.anchor(0.5f, 0.5f);
													ArrayList<BitmapDescriptor> iconList = new ArrayList<>();
													for (int i = 1; i <= 9; i++) {
														iconList.add(BitmapDescriptorFactory.fromAsset("typhoon/typhoon_icon"+i+".png"));
													}
													tOption.icons(iconList);
													tOption.period(6);
													Marker marker = aMap.addMarker(tOption);
													if (flag6) {
														marker.setVisible(true);
													}else {
														marker.setVisible(false);
													}
													typhoonMarkers.add(marker);
												}
											}
										} catch (JSONException e) {
											e.printStackTrace();
										}
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
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (tvTitle != null) {
			TCAgent.onPageStart(mContext, tvTitle.getText().toString());
		}
		if (mapView != null) {
			mapView.onResume();
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
		if (mapView != null) {
			mapView.onPause();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mapView != null) {
			mapView.onSaveInstanceState(outState);
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mapView != null) {
			mapView.onDestroy();
		}
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
	}

	private String columnId = "";//栏目id

	/**
	 * 普通发送指令，
	 * @param id
	 * @param sid
	 */
	private void setNormalEmit(String id, String sid) {
		try {
			if (socket == null) {
				socket = MyApplication.getSocket();
			}
			if (socket != null && socket.connected()) {
				JSONObject obj = new JSONObject();
				obj.put("computerInfo", MyApplication.computerInfo);
				JSONObject commond = new JSONObject();
				commond.put("id", id);
				JSONObject message = new JSONObject();
				if (!TextUtils.isEmpty(sid)) {
					message.put("sid", sid);
				}
				commond.put("message", message);
				obj.put("commond", commond);
				socket.emit(columnId, obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 点击marker发送指令
	 * @param id
	 * @param dto
	 */
	private void setSelectEmit(String id, WarningDto dto) {
		try {
			if (socket == null) {
				socket = MyApplication.getSocket();
			}
			if (socket != null && socket.connected()) {
				JSONObject obj = new JSONObject();
				obj.put("computerInfo", MyApplication.computerInfo);
				JSONObject commond = new JSONObject();
				commond.put("id", id);
				JSONObject message = new JSONObject();
				JSONArray array = new JSONArray();
				array.put(0, dto.name);
				array.put(1, dto.html);
				array.put(2, dto.lng);
				array.put(3, dto.lat);
				message.put("data", array);
				commond.put("message", message);
				obj.put("commond", commond);
				socket.emit(columnId, obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 预警详情发送指令，
	 * @param id
	 * @param name
	 */
	private void setDetailEmit(String id, String name) {
		try {
			if (socket == null) {
				socket = MyApplication.getSocket();
			}
			if (socket != null && socket.connected()) {
				JSONObject obj = new JSONObject();
				obj.put("computerInfo", MyApplication.computerInfo);
				JSONObject commond = new JSONObject();
				commond.put("id", id);
				JSONObject message = new JSONObject();
				if (!TextUtils.isEmpty(name)) {
					message.put("data", name);
				}
				commond.put("message", message);
				obj.put("commond", commond);
				socket.emit(columnId, obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
