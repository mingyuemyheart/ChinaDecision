package com.china.activity;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
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
import android.view.animation.TranslateAnimation;
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
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.Animation.AnimationListener;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.china.R;
import com.china.adapter.WarningAdapter;
import com.china.adapter.WarningStatisticAdapter;
import com.china.common.CONST;
import com.china.common.MyApplication;
import com.china.dto.WarningDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.view.ArcMenu;
import com.china.view.ArcMenu.OnMenuItemClickListener;

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
 * 预警
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class WarningActivity extends BaseActivity implements OnClickListener, AMapLocationListener, OnMapClickListener, 
OnMarkerClickListener, InfoWindowAdapter, OnCameraChangeListener, OnMapScreenShotListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView ivShare = null;
	private RelativeLayout reShare = null;
	private TextView tvPrompt = null;//没有数据时提示
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private float zoom = 3.7f;
	private ArcMenu arcMenu = null;
	private boolean blue = true, yellow = true, orange = true, red = true;
	private String warningUrl = "http://decision-admin.tianqi.cn/Home/extra/getwarns?order=1";//预警地址
	private List<WarningDto> warningList = new ArrayList<>();
	private List<WarningDto> nationList = new ArrayList<>();
	private List<WarningDto> proList = new ArrayList<>();
	private List<WarningDto> cityList = new ArrayList<>();
	private List<WarningDto> disList = new ArrayList<>();
	private List<WarningDto> blueList = new ArrayList<>();
	private List<WarningDto> yellowList = new ArrayList<>();
	private List<WarningDto> orangeList = new ArrayList<>();
	private List<WarningDto> redList = new ArrayList<>();
	private List<Marker> blueMarkers = new ArrayList<>();
	private List<Marker> yellowMarkers = new ArrayList<>();
	private List<Marker> orangeMarkers = new ArrayList<>();
	private List<Marker> redMarkers = new ArrayList<>();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private boolean isExpandMap = true;//是否放大地图
	private ImageView ivLocation = null;
	private ImageView ivRefresh = null;
	private ImageView ivList = null;
	private ImageView ivStatistic = null;//预警统计
	private TextView tvNation = null;//国家级预警
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private double locationLat = 0, locationLng = 0;
	private int size1 = 30;
	private int size2 = 20;
	private LatLng leftlatlng = null;
	private LatLng rightLatlng = null;
	private Marker selectMarker = null;
	private ImageView ivGuide = null;//引导页
	
	private LinearLayout llPrompt = null;
	private ImageView ivArrow = null;
	private boolean isShowPrompt = false;
	
	//预警统计列表
	private ListView listView1 = null;
	private WarningStatisticAdapter adapter1 = null;
	private List<WarningDto> list1 = new ArrayList<>();
	
	//点击marker的预警信息
	private ListView listView2 = null;
	private WarningAdapter adapter2 = null;
	private List<WarningDto> list2 = new ArrayList<>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warning);
		mContext = this;
		initAmap(savedInstanceState);
		initWidget();
		initListView1();
		initListView2();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		columnId = getIntent().getStringExtra(CONST.COLUMN_ID);//栏目id

		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvPrompt = (TextView) findViewById(R.id.tvPrompt);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		reShare = (RelativeLayout) findViewById(R.id.reShare);
		arcMenu = (ArcMenu) findViewById(R.id.arcMenu);
		arcMenu.setOnMenuItemClickListener(arcMenuListener);
		ivLocation = (ImageView) findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		ivRefresh = (ImageView) findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		ivList = (ImageView) findViewById(R.id.ivList);
		ivList.setOnClickListener(this);
		ivStatistic = (ImageView) findViewById(R.id.ivStatistic);
		ivStatistic.setOnClickListener(this);
		llPrompt = (LinearLayout) findViewById(R.id.llPrompt);
		llPrompt.setOnClickListener(this);
		ivArrow = (ImageView) findViewById(R.id.ivArrow);
		ivArrow.setOnClickListener(this);
		tvNation = (TextView) findViewById(R.id.tvNation);
		tvNation.setOnClickListener(this);
		ivGuide = (ImageView) findViewById(R.id.ivGuide);
		ivGuide.setOnClickListener(this);
		CommonUtil.showGuidePage(mContext, this.getClass().getName(), ivGuide);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		refresh();
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
    }
	
	private void refresh() {
//		android.view.animation.Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.round_animation);
//		ivRefresh.startAnimation(animation);
		showDialog();
		startLocation();
		OkHttpWarning(warningUrl);
	}
	
	/**
	 * 初始化高德地图
	 */
	private void initAmap(Bundle bundle) {
		mapView = (MapView) findViewById(R.id.mapView);
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
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
        mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(mContext);//初始化定位
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
			locationLat = amapLocation.getLatitude();
			locationLng = amapLocation.getLongitude();
			ivLocation.setVisibility(View.VISIBLE);
        }
	}
	
	/**
	 * 异步请求
	 */
	private void OkHttpWarning(final String requestUrl) {
		OkHttpUtil.enqueue(new Request.Builder().url(requestUrl).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				warningList.clear();
				nationList.clear();
				proList.clear();
				cityList.clear();
				disList.clear();
				if (result != null) {
					try {
						final JSONObject object = new JSONObject(result);
						if (object != null) {
							if (!object.isNull("data")) {
								JSONArray jsonArray = object.getJSONArray("data");
								for (int i = jsonArray.length()-1; i >= 0; i--) {
									JSONArray tempArray = jsonArray.getJSONArray(i);
									WarningDto dto = new WarningDto();
									dto.html = tempArray.optString(1);
									String[] array = dto.html.split("-");
									String item0 = array[0];
									String item1 = array[1];
									String item2 = array[2];

									dto.item0 = item0;
									dto.provinceId = item0.substring(0, 2);
									dto.type = item2.substring(0, 5);
									dto.color = item2.substring(5, 7);
									dto.time = item1;
									dto.lng = tempArray.optString(2);
									dto.lat = tempArray.optString(3);
									dto.name = tempArray.optString(0);

									if (!dto.name.contains("解除")) {
										warningList.add(dto);
									}

									if (!TextUtils.isEmpty(item0)) {
										if (!dto.name.contains("解除")) {
											if (TextUtils.equals(item0, "000000")) {
												nationList.add(dto);
											}else if (TextUtils.equals(item0.substring(item0.length()-4, item0.length()), "0000")) {
												proList.add(dto);
											}else if (TextUtils.equals(item0.substring(item0.length()-2, item0.length()), "00")) {
												cityList.add(dto);
											}else {
												disList.add(dto);
											}
										}
									}
								}

								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											String count = warningList.size()+"";
											if (TextUtils.equals(count, "0")) {
												String time = "";
												if (!object.isNull("time")) {
													long t = object.getLong("time");
													time = sdf.format(new Date(t*1000));
												}
												tvPrompt.setText(time+", "+"当前生效预警"+count+"条");
												ivList.setVisibility(View.GONE);
												ivStatistic.setVisibility(View.GONE);
												arcMenu.setVisibility(View.GONE);
												llPrompt.setVisibility(View.VISIBLE);
//														ivRefresh.clearAnimation();
												cancelDialog();
												return;
											}

											String time = "";
											if (!object.isNull("time")) {
												long t = object.getLong("time");
												time = sdf.format(new Date(t*1000));
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
											tvPrompt.setText(builder);
											ivList.setVisibility(View.VISIBLE);
											ivStatistic.setVisibility(View.VISIBLE);
											arcMenu.setVisibility(View.VISIBLE);
											llPrompt.setVisibility(View.VISIBLE);
//													ivRefresh.clearAnimation();
											cancelDialog();

											if (nationList.size() > 0) {
												tvNation.setText("国家级预警"+nationList.size()+"条");
												tvNation.setVisibility(View.VISIBLE);
											}else {
												tvNation.setVisibility(View.GONE);
											}

											unselectedWarning();
											selectedWarning();

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

											list1.clear();
											WarningDto wDto = new WarningDto();
											wDto.colorName = "预警"+warningList.size();
											wDto.nationCount = "国家级"+(rnation+onation+ynation+bnation);
											wDto.proCount = "省级"+(rpro+opro+ypro+bpro);
											wDto.cityCount = "市级"+(rcity+ocity+ycity+bcity);
											wDto.disCount = "县级"+(rdis+odis+ydis+bdis);
											list1.add(wDto);

											wDto = new WarningDto();
											wDto.colorName = "红"+(rnation+rpro+rcity+rdis);
											wDto.nationCount = rnation+"";
											wDto.proCount = rpro+"";
											wDto.cityCount = rcity+"";
											wDto.disCount = rdis+"";
											list1.add(wDto);

											wDto = new WarningDto();
											wDto.colorName = "橙"+(onation+opro+ocity+odis);
											wDto.nationCount = onation+"";
											wDto.proCount = opro+"";
											wDto.cityCount = ocity+"";
											wDto.disCount = odis+"";
											list1.add(wDto);

											wDto = new WarningDto();
											wDto.colorName = "黄"+(ynation+ypro+ycity+ydis);
											wDto.nationCount = ynation+"";
											wDto.proCount = ypro+"";
											wDto.cityCount = ycity+"";
											wDto.disCount = ydis+"";
											list1.add(wDto);

											wDto = new WarningDto();
											wDto.colorName = "蓝"+(bnation+bpro+bcity+bdis);
											wDto.nationCount = bnation+"";
											wDto.proCount = bpro+"";
											wDto.cityCount = bcity+"";
											wDto.disCount = bdis+"";
											list1.add(wDto);

											if (adapter1 != null) {
												adapter1.notifyDataSetChanged();
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
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
	
	/**
	 * 在地图上添加marker
	 */
	private void addWarningMarkers(List<WarningDto> list, List<Marker> markerList) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
		    if (!TextUtils.equals(dto.item0, "000000")) {
		    	double lat = Double.valueOf(dto.lat);
				double lng = Double.valueOf(dto.lng);
				MarkerOptions optionsTemp = new MarkerOptions();
				optionsTemp.title(dto.lat+","+dto.lng);
				optionsTemp.anchor(0.5f, 0.5f);
				optionsTemp.position(new LatLng(lat, lng));
				View mView = inflater.inflate(R.layout.warning_marker_view, null);
				ImageView ivMarker = (ImageView) mView.findViewById(R.id.ivMarker);
				LayoutParams params = ivMarker.getLayoutParams();
				if (zoom < 4.0f) {
					params.width = (int) CommonUtil.dip2px(mContext, size2);
					params.height = (int) CommonUtil.dip2px(mContext, size2);
				}else {
					params.width = (int) CommonUtil.dip2px(mContext, size1);
					params.height = (int) CommonUtil.dip2px(mContext, size1);
				}
				ivMarker.setLayoutParams(params);
				
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
				ivMarker.setImageBitmap(bitmap);
				optionsTemp.icon(BitmapDescriptorFactory.fromView(mView));
				
				if (leftlatlng == null || rightLatlng == null) {
					Marker marker = aMap.addMarker(optionsTemp);
					marker.setVisible(true);
					markerList.add(marker);
					Animation animation = new ScaleAnimation(0,1,0,1);
					animation.setInterpolator(new LinearInterpolator());
					animation.setDuration(300);
					marker.setAnimation(animation);
					marker.startAnimation();
				}else {
					if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
						Marker marker = aMap.addMarker(optionsTemp);
						marker.setVisible(true);
						markerList.add(marker);
						Animation animation = new ScaleAnimation(0,1,0,1);
						animation.setInterpolator(new LinearInterpolator());
						animation.setDuration(300);
						marker.setAnimation(animation);
						marker.startAnimation();
					}
				}
			}
		}
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
//		if (listView2.getVisibility() == View.VISIBLE) {
//			hideAnimation(listView2);
//			listView2.setVisibility(View.GONE);
//		}
		if (selectMarker != null) {
			selectMarker.hideInfoWindow();
			setNormalEmit("12", "");
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		selectMarker = marker;
		marker.showInfoWindow();
//		if (listView2.getVisibility() == View.VISIBLE) {
//			hideAnimation(listView2);
//			listView2.setVisibility(View.GONE);
//		}
//		
//		list2.clear();
//		if (zoom <= 6.0f) {
//			addInfoList(proList, marker, list2);
//		}else if (zoom > 6.0f && zoom <= 8.0f) {
//			addInfoList(proList, marker, list2);
//			addInfoList(cityList, marker, list2);
//		}else if (zoom > 8.0f) {
//			addInfoList(proList, marker, list2);
//			addInfoList(cityList, marker, list2);
//			addInfoList(disList, marker, list2);
//		}
//		if (adapter2 != null) {
//			adapter2.notifyDataSetChanged();
//			setListViewHeight(listView2, list2.size(), 50, 100, 150);
//		}
//		
//		if (listView2.getVisibility() == View.GONE) {
//			showAnimation(listView2);
//			listView2.setVisibility(View.VISIBLE);
//		}
		return true;
	}
	
	@Override
	public View getInfoContents(final Marker marker) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mView = inflater.inflate(R.layout.warning_marker_info, null);
		ListView mListView = null;
		WarningAdapter mAdapter = null;
		final List<WarningDto> infoList = new ArrayList<WarningDto>();
		
		infoList.clear();
		if (zoom <= 6.0f) {
			addInfoList(proList, marker, infoList);
		}else if (zoom > 6.0f && zoom <= 8.0f) {
			addInfoList(proList, marker, infoList);
			addInfoList(cityList, marker, infoList);
		}else if (zoom > 8.0f) {
			addInfoList(proList, marker, infoList);
			addInfoList(cityList, marker, infoList);
			addInfoList(disList, marker, infoList);
		}
		
		mListView = (ListView) mView.findViewById(R.id.listView);
		mAdapter = new WarningAdapter(mContext, infoList, true);
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

		return mView;
	}
	
	private void intentDetail(WarningDto data) {
		Intent intentDetail = new Intent(mContext, WarningDetailActivity.class);
		intentDetail.putExtra(CONST.COLUMN_ID, columnId);
		Bundle bundle = new Bundle();
		bundle.putParcelable("data", data);
		intentDetail.putExtras(bundle);
		startActivity(intentDetail);

		setDetailEmit("9", data.html);
	}
	
	private void addInfoList(List<WarningDto> list, Marker marker, List<WarningDto> infoList) {
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
			String[] latLng = marker.getTitle().split(",");
			if (TextUtils.equals(latLng[0], dto.lat) && TextUtils.equals(latLng[1], dto.lng)) {
				infoList.add(dto);
			}
		}
	}
	
	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 移除地图上指定marker
	 * @param markers
	 */
	private void removeMarkers(List<Marker> markers) {
		for (int i = 0; i < markers.size(); i++) {
			final Marker marker = markers.get(i);
			Animation animation = new ScaleAnimation(1,0,1,0);
			animation.setInterpolator(new LinearInterpolator());
			animation.setDuration(300);
			marker.setAnimation(animation);
			marker.startAnimation();
			marker.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart() {
				}
				@Override
				public void onAnimationEnd() {
					marker.remove();
				}
			});
		}
		markers.clear();
	}
	
	private OnMenuItemClickListener arcMenuListener = new OnMenuItemClickListener() {
		@Override
		public void onClick(View view, int pos) {
			if (pos == 0) {
				setNormalEmit("5", "1");
				if (blue) {
					blue = false;
					((ImageView)view).setImageResource(R.drawable.iv_arc_blue_press);
					removeMarkers(blueMarkers);
				}else {
					blue = true;
					((ImageView)view).setImageResource(R.drawable.iv_arc_blue);
					addWarningMarkers(blueList, blueMarkers);
				}
			}else if (pos == 1) {
				setNormalEmit("5", "2");
				if (yellow) {
					yellow = false;
					((ImageView)view).setImageResource(R.drawable.iv_arc_yellow_press);
					removeMarkers(yellowMarkers);
				}else {
					yellow = true;
					((ImageView)view).setImageResource(R.drawable.iv_arc_yellow);
					addWarningMarkers(yellowList, yellowMarkers);
				}
			}else if (pos == 2) {
				setNormalEmit("5", "3");
				if (orange) {
					orange = false;
					((ImageView)view).setImageResource(R.drawable.iv_arc_orange_press);
					removeMarkers(orangeMarkers);
				}else {
					orange = true;
					((ImageView)view).setImageResource(R.drawable.iv_arc_orange);
					addWarningMarkers(orangeList, orangeMarkers);
				}
			}else if (pos == 3) {
				setNormalEmit("5", "4");
				if (red) {
					red = false;
					((ImageView)view).setImageResource(R.drawable.iv_arc_red_press);
					removeMarkers(redMarkers);
				}else {
					red = true;
					((ImageView)view).setImageResource(R.drawable.iv_arc_red);
					addWarningMarkers(redList, redMarkers);
				}
			}
		}
	};
	
	/**
	 * 不选中预警
	 */
	private void unselectedWarning() {
		removeMarkers(blueMarkers);
		removeMarkers(yellowMarkers);
		removeMarkers(orangeMarkers);
		removeMarkers(redMarkers);
		blueList.clear();
		yellowList.clear();
		orangeList.clear();
		redList.clear();
	}
	
	/**
	 * 选中预警
	 */
	private void selectedWarning() {
		if (zoom <= 6.0f) {
			for (int i = 0; i < proList.size(); i++) {
				WarningDto dto = proList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
		}else if (zoom > 6.0f && zoom <= 8.0f) {
			for (int i = 0; i < proList.size(); i++) {
				WarningDto dto = proList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
			for (int i = 0; i < cityList.size(); i++) {
				WarningDto dto = cityList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
		}else if (zoom > 8.0f) {
			for (int i = 0; i < proList.size(); i++) {
				WarningDto dto = proList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
			for (int i = 0; i < cityList.size(); i++) {
				WarningDto dto = cityList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
			for (int i = 0; i < disList.size(); i++) {
				WarningDto dto = disList.get(i);
				if (TextUtils.equals(dto.color, "01")) {
					blueList.add(dto);
				}else if (TextUtils.equals(dto.color, "02")) {
					yellowList.add(dto);
				}else if (TextUtils.equals(dto.color, "03")) {
					orangeList.add(dto);
				}else if (TextUtils.equals(dto.color, "04")) {
					redList.add(dto);
				}
			}
		}
		
		if (blue) {
			addWarningMarkers(blueList, blueMarkers);
		}
		if (yellow) {
			addWarningMarkers(yellowList, yellowMarkers);
		}
		if (orange) {
			addWarningMarkers(orangeList, orangeMarkers);
		}
		if (red) {
			addWarningMarkers(redList, redMarkers);
		}
	}
	
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

		setMapEmit(arg0.zoom, arg0.target.latitude, arg0.target.longitude);
		
		if (selectMarker != null) {
			selectMarker.hideInfoWindow();
		}
		unselectedWarning();
		selectedWarning();
	}
	
	/**
	 * 初始化预警统计列表
	 */
	private void initListView1() {
		listView1 = (ListView) findViewById(R.id.listView1);
		adapter1 = new WarningStatisticAdapter(mContext, list1);
		listView1.setAdapter(adapter1);
		listView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				clickPromptWarning();
			}
		});
	}
	
	/**
	 * 初始化点击marker的预警列表
	 */
	private void initListView2() {
		listView2 = (ListView) findViewById(R.id.listView2);
		adapter2 = new WarningAdapter(mContext, list2, false);
		listView2.setAdapter(adapter2);
		setListViewHeight(listView2, list2.size(), 50, 100, 150);
		listView2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentDetail(list2.get(arg2));
			}
		});
	}
	
	/**
	 * 设置listview高度
	 * @param listView
	 * @param size
	 */
	private void setListViewHeight(ListView listView, int size, int height1, int height2, int height3) {
		LayoutParams params = listView.getLayoutParams();
		if (size == 1) {
			params.height = (int) CommonUtil.dip2px(mContext, height1);
		}else if (size == 2) {
			params.height = (int) CommonUtil.dip2px(mContext, height2);
		}else if (size > 2){
			params.height = (int) CommonUtil.dip2px(mContext, height3);
		}
		listView.setLayoutParams(params);
	}
	
	/**
	 * 向上弹出动画
	 * @param layout
	 */
	private void showAnimation(View layout) {
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 1f, 
				TranslateAnimation.RELATIVE_TO_SELF, 0);
		animation.setDuration(300);
		layout.startAnimation(animation);
	}
	
	/**
	 * 向下隐藏动画
	 * @param layout
	 */
	private void hideAnimation(View layout) {
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 1f);
		animation.setDuration(300);
		layout.startAnimation(animation);
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
    	int height = CommonUtil.getListViewHeightBasedOnChildren(listView1);
		if (isShowPrompt == false) {
			isShowPrompt = true;
			ivArrow.setImageResource(R.drawable.iv_arrow_black_up);
			hideOrShowListViewAnimator(listView1, 0, height);
		}else {
			isShowPrompt = false;
			ivArrow.setImageResource(R.drawable.iv_arrow_black_down);
			hideOrShowListViewAnimator(listView1, height, 0);
		}
    }
    
    @Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		//bitmap2为覆盖再地图上的view
		Bitmap bitmap2 = CommonUtil.captureView(reShare);
		//bitmap3为bitmap1+bitmap2覆盖叠加在一起的view
		Bitmap bitmap3 = CommonUtil.mergeBitmap(WarningActivity.this, bitmap1, bitmap2, true);
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap2);
		Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
//		Bitmap bitmap4 = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable-hdpi/iv_share_bottom.png"));
		Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
		CommonUtil.clearBitmap(bitmap3);
		CommonUtil.clearBitmap(bitmap4);
		CommonUtil.share(WarningActivity.this, bitmap);
	}

	@Override
	public void onMapScreenShot(Bitmap arg0, int arg1) {
		// TODO Auto-generated method stub
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
		case R.id.llPrompt:
		case R.id.ivArrow:
			clickPromptWarning();
			break;
		case R.id.ivLocation:
			if (isExpandMap) {
				isExpandMap = false;
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), 10.0f));
			}else {
				isExpandMap = true;
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), 3.5f));
			}
			break;
		case R.id.ivRefresh:
			refresh();
			break;
		case R.id.ivList:
			setNormalEmit("6", "");
			Intent intent = new Intent(mContext, WarningListActivity.class);
			intent.putExtra(CONST.COLUMN_ID, columnId);
			intent.putExtra("isVisible", true);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) warningList);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivStatistic:
			startActivity(new Intent(mContext, WarningStatisticActivity.class));
			break;
		case R.id.tvNation:
			setNormalEmit("2", "");
			intent = new Intent(mContext, HeadWarningActivity.class);
			bundle = new Bundle();
			bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) nationList);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivShare:
			aMap.getMapScreenShot(WarningActivity.this);
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
