package com.china.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.china.R;
import com.china.adapter.ShawnTyphoonNameAdapter;
import com.china.adapter.ShawnTyphoonPublishAdapter;
import com.china.adapter.ShawnTyphoonYearAdapter;
import com.china.adapter.WarningAdapter;
import com.china.common.CONST;
import com.china.dto.MinuteFallDto;
import com.china.dto.TyphoonDto;
import com.china.dto.WarningDto;
import com.china.dto.WindData;
import com.china.dto.WindDto;
import com.china.manager.CaiyunManager;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.SecretUrlUtil;
import com.china.utils.WeatherUtil;
import com.china.view.WaitWindView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ????????????
 */
public class TyhpoonActivity extends BaseActivity implements OnClickListener, OnMapClickListener, AMapLocationListener,
		OnMarkerClickListener, InfoWindowAdapter, AMap.OnMapScreenShotListener, OnCameraChangeListener{

	private Context mContext;
	private TextView tvTitle,tvTyphoonName,tvFileTime;
	private TextSwitcher tvTyphoonInfo;
	private ImageView ivLegend,ivTyphoonList,ivTyphoonPlay,ivTyphoonRadar,ivTyphoonCloud,ivTyphoonWarning,ivTyphoonWind,ivWarning,ivTyphoonRange,ivGuide;
	private RelativeLayout reLegend,reTyphoonList;
	private ConstraintLayout reShare;
	private MapView mapView;
	private AMap aMap;
	private float zoom = 3.0f;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy???MM???dd???HH???", Locale.CHINA);
	private SimpleDateFormat sdf8 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0", Locale.CHINA);
	private String locationCity = "?????????";
	private Bundle savedInstanceState;
	private boolean isRadarOn = false,isCloudOn = false,isWindOn = false, isWarningOn = false;
	private List<WarningDto> typhoonWarnings = new ArrayList<>();//?????????????????????????????????
	private List<Marker> typhoonWarningMarkers = new ArrayList<>();
	private String markerType2 = "typhoon", markerType1 = "warning";

	//???????????????
	private CaiyunManager mRadarManager;
	private List<MinuteFallDto> radarList = new ArrayList<>();
	private RadarThread mRadarThread;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private GroundOverlay radarOverlay,cloudOverlay;
	private Bitmap cloudBitmap;

	//??????
	private ConstraintLayout container;
	public ConstraintLayout container2;
	private WindData windData;
	private WaitWindView waitWindView;

	//??????
	private Circle circle100, circle300, circle500;
	private Text text100, text300, text500;
	private LatLng locationLatLng = new LatLng(39.904030, 116.407526);
	private LatLng clickLatLng = new LatLng(39.904030, 116.407526);
    private Map<String, List<Polyline>> factLinesMap = new HashMap<>();//????????????
    private Map<String, List<Polyline>> foreLinesMap = new HashMap<>();//????????????
    private Map<String, List<Marker>> markerPointsMap = new HashMap<>();//???????????????
    private Map<String, Marker> rotateMarkersMap = new HashMap<>();//????????????markers
    private Map<String, Marker> factTimeMarkersMap = new HashMap<>();//???????????????????????????markers
    private Map<String, List<Polyline>> rangeLinesMap = new HashMap<>();//??????????????????
    private Map<String, Marker> rangeMarkersMap = new HashMap<>();//??????????????????marker
    private Map<String, TyphoonDto> lastFactPointMap = new HashMap<>();//?????????????????????????????????
	private Marker clickMarker,locationMarker;//????????????marker
	private List<Polygon> windCirclePolygons = new ArrayList<>();//??????
	private ListView publishListView;
	private ShawnTyphoonPublishAdapter publishAdapter;
	private List<TyphoonDto> publishList = new ArrayList<>();
	private ShawnTyphoonYearAdapter yearAdapter;
	private List<TyphoonDto> yearList = new ArrayList<>();
	private ShawnTyphoonNameAdapter nameAdapter;
	private List<TyphoonDto> nameList = new ArrayList<>();//?????????????????????
	private List<TyphoonDto> startList = new ArrayList<>();//?????????????????????
	private Map<String, List<TyphoonDto>> pointsMap = new HashMap<>();//????????????????????????????????????
	private RoadThread mRoadThread;//????????????????????????
	private final int MSG_ROLING_TYPHOON = 1001;
	private final int DRAW_TYPHOON_COMPLETE = 1002;//????????????????????????
	private boolean isShowInfoWindow = true;//??????????????????
	private boolean isRanging = false;//??????????????????
	private GeocodeSearch geocoderSearch;
	private RollingThread rollingThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_typhoon);
		mContext = this;
		this.savedInstanceState = savedInstanceState;
		checkAuthority();
	}

	private void init() {
		initAmap(savedInstanceState);
		initWidget();
		initPublishListView();
		initYearListView();
		initNameListView();
	}

	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvTyphoonName = findViewById(R.id.tvTyphoonName);
		ivLegend = findViewById(R.id.ivLegend);
		ivLegend.setOnClickListener(this);
		reLegend = findViewById(R.id.reLegend);
		ImageView ivCancelLegend = findViewById(R.id.ivCancelLegend);
		ivCancelLegend.setOnClickListener(this);
		ImageView ivLocation = findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		ivTyphoonList = findViewById(R.id.ivTyphoonList);
		ivTyphoonList.setOnClickListener(this);
		reTyphoonList = findViewById(R.id.reTyphoonList);
		ImageView ivCancelList = findViewById(R.id.ivCancelList);
		ivCancelList.setOnClickListener(this);
		ivTyphoonPlay = findViewById(R.id.ivTyphoonPlay);
		ivTyphoonPlay.setOnClickListener(this);
		ivTyphoonRange =  findViewById(R.id.ivTyphoonRange);
		ivTyphoonRange.setOnClickListener(this);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		reShare = findViewById(R.id.reShare);
		tvFileTime = findViewById(R.id.tvFileTime);
		ivTyphoonWind = findViewById(R.id.ivTyphoonWind);
		ivTyphoonWind.setOnClickListener(this);
		ivTyphoonRadar = findViewById(R.id.ivTyphoonRadar);
		ivTyphoonRadar.setOnClickListener(this);
		ivTyphoonCloud = findViewById(R.id.ivTyphoonCloud);
		ivTyphoonCloud.setOnClickListener(this);
		ivWarning = findViewById(R.id.ivWarning);
		container = findViewById(R.id.container);
		container2 = findViewById(R.id.container2);
		ivGuide = findViewById(R.id.ivGuide);
		ivGuide.setOnClickListener(this);
		tvTyphoonInfo = findViewById(R.id.tvTyphoonInfo);
		ivTyphoonWarning = findViewById(R.id.ivTyphoonWarning);
		ivTyphoonWarning.setOnClickListener(this);

		mRadarManager = new CaiyunManager(mContext);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		OkHttpWarning();

		CommonUtil.showGuidePage(mContext, this.getClass().getName(), ivGuide);
	}

	private void initAmap(Bundle savedInstanceState) {
		mapView = findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnCameraChangeListener(this);
		aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
			@Override
			public void onMapLoaded() {
				drawWarningLines();
				if (CommonUtil.isLocationOpen(mContext)) {
					startLocation();
				}else {
					addLocationMarker();
				}
			}
		});
	}

	/**
	 * ??????24h???48h?????????
	 */
	private void drawWarningLines() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				//24??????
				PolylineOptions line1 = new PolylineOptions();
				line1.width(CommonUtil.dip2px(mContext, 2));
				line1.color(getResources().getColor(R.color.red));
				line1.add(new LatLng(34.005024, 126.993568), new LatLng(21.971252, 126.993568));
				line1.add(new LatLng(17.965860, 118.995521), new LatLng(10.971050, 118.995521));
				line1.add(new LatLng(4.486270, 113.018959) ,new LatLng(-0.035506, 104.998939));
				aMap.addPolyline(line1);
				drawWarningText(getString(R.string.line_24h), getResources().getColor(R.color.red), new LatLng(30.959474, 126.993568));

				//48??????
				PolylineOptions line2 = new PolylineOptions();
				line2.width(CommonUtil.dip2px(mContext, 2));
				line2.color(getResources().getColor(R.color.yellow));
				line2.add(new LatLng(-0.035506, 104.998939), new LatLng(-0.035506, 119.962318));
				line2.add(new LatLng(14.968860, 131.981361) ,new LatLng(33.959474, 131.981361));
				aMap.addPolyline(line2);
				drawWarningText(getString(R.string.line_48h), getResources().getColor(R.color.yellow), new LatLng(30.959474, 131.981361));
			}
		}).start();
	}

	/**
	 * ???????????????????????????
	 */
	private void drawWarningText(String text, int textColor, LatLng latLng) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_typhoon_warning_line, null);
		TextView tvLine = view.findViewById(R.id.tvLine);
		tvLine.setText(text);
		tvLine.setTextColor(textColor);
		MarkerOptions options = new MarkerOptions();
		options.anchor(-0.3f, 0.2f);
		options.position(latLng);
		options.icon(BitmapDescriptorFactory.fromView(view));
		aMap.addMarker(options);
	}

	/**
	 * ????????????
	 */
	private void startLocation() {
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();//?????????????????????
		AMapLocationClient mLocationClient = new AMapLocationClient(mContext);//???????????????
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//???????????????????????????????????????Battery_Saving?????????????????????Device_Sensors??????????????????
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
		if (amapLocation != null && amapLocation.getErrorCode() == 0) {
			locationCity = amapLocation.getCity();
			if (amapLocation.getLongitude() != 0 && amapLocation.getLatitude() != 0) {
				locationLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
				clickLatLng = locationLatLng;
				addLocationMarker();
			}
		}
	}

	/**
	 * ??????????????????
	 */
	private void addLocationMarker() {
		if (clickLatLng == null) {
			return;
		}
		MarkerOptions options = new MarkerOptions();
		options.position(clickLatLng);
		options.anchor(0.5f, 1.0f);
		Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.icon_map_location),
				(int)(CommonUtil.dip2px(mContext, 21)), (int)(CommonUtil.dip2px(mContext, 32)));
		if (bitmap != null) {
			options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
		}else {
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_location));
		}
		if (locationMarker != null) {
			locationMarker.remove();
		}
		locationMarker = aMap.addMarker(options);
		locationMarker.setClickable(false);
	}

	/**
	 * ????????????????????????100???300???500km?????????
	 */
	private void removeLocationCirces() {
		if (circle100 != null) {
			circle100.remove();
			circle100 = null;
		}
		if (circle300 != null) {
			circle300.remove();
			circle300 = null;
		}
		if (circle500 != null) {
			circle500.remove();
			circle500 = null;
		}
		if (text100 != null) {
			text100.remove();
			text100 = null;
		}
		if (text300 != null) {
			text300.remove();
			text300 = null;
		}
		if (text500 != null) {
			text500.remove();
			text500 = null;
		}
	}

	/**
	 * ???????????????????????????
	 */
	private void addLocationCircles() {
		removeLocationCirces();
		if (clickLatLng == null) {
			return;
		}
		if (isRanging) {
			circle100 = aMap.addCircle(new CircleOptions().center(clickLatLng)
					.radius(100000).strokeColor(0x90ff6c00).strokeWidth(4));

			circle300 = aMap.addCircle(new CircleOptions().center(clickLatLng)
					.radius(300000).strokeColor(0x90ffd800).strokeWidth(4));

			circle500 = aMap.addCircle(new CircleOptions().center(clickLatLng)
					.radius(500000).strokeColor(0x9000b4ff).strokeWidth(4));

			text100 = addCircleText(clickLatLng, 100000, 0xffff6c00, "100km");

			text300 = addCircleText(clickLatLng, 300000, 0xffffd800, "300km");

			text500 = addCircleText(clickLatLng, 500000, 0xff00b4ff, "500km");
		}
	}

	/**
	 * ??????????????????
	 * @param center
	 * @param radius
	 * @param color
	 * @param distance
	 * @return
	 */
	private Text addCircleText(LatLng center, int radius, int color, String distance) {
		double r = 6371000.79;
		int numpoints = 360;
		double phase = 2 * Math.PI / numpoints;

		double dx = (radius * Math.cos(numpoints*3/4 * phase));
		double dy = (radius * Math.sin(numpoints*3/4 * phase));//??????1.6 ????????????

		double dlng = dx / (r * Math.cos(center.latitude * Math.PI / 180) * Math.PI / 180);
		double dlat = dy / (r * Math.PI / 180);

		TextOptions textOptions = new TextOptions();
		textOptions.backgroundColor(Color.TRANSPARENT);
		textOptions.fontSize(40);
		textOptions.fontColor(color);
		textOptions.text(distance);
		textOptions.position(new LatLng(center.latitude + dlat, center.longitude + dlng));
		Text text = aMap.addText(textOptions);
		return text;
	}

	/**
	 * ??????????????????
	 */
	private void OkHttpWarning() {
		final String url = "https://decision-admin.tianqi.cn/Home/work2019/getwarns?order=0";
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
										if (!object.isNull("data")) {
											JSONArray jsonArray = object.getJSONArray("data");
											for (int i = 0; i < jsonArray.length(); i++) {
												JSONArray tempArray = jsonArray.getJSONArray(i);
												final WarningDto dto = new WarningDto();
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

												if (!dto.name.contains("??????") && !TextUtils.equals(item0, "000000") && item2.startsWith("11B01")) {//??????????????????
													typhoonWarnings.add(dto);
												}

												if (!dto.name.contains("??????") && TextUtils.equals(item0, "000000") && item2.startsWith("11B01")) {//?????????????????????
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
													if (bitmap != null) {
														ivWarning.setImageBitmap(bitmap);
														ivWarning.setVisibility(View.VISIBLE);
														ivWarning.setOnClickListener(new OnClickListener() {
															@Override
															public void onClick(View v) {
																Intent intentDetail = new Intent(mContext, WarningDetailActivity.class);
																Bundle bundle = new Bundle();
																bundle.putParcelable("data", dto);
																intentDetail.putExtras(bundle);
																startActivity(intentDetail);
															}
														});
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
				});
			}
		}).start();
	}

	/**
	 * ??????????????????markers
	 */
	private void removeTyphoonWarningMarkers() {
		for (Marker marker : typhoonWarningMarkers) {
			marker.remove();
		}
		typhoonWarningMarkers.clear();
	}

	/**
	 * ??????????????????markers
	 */
	private void addTyphoonWarningMarkers() {
		if (typhoonWarnings.size() <= 0) {
			Toast.makeText(mContext, "??????????????????????????????????????????", Toast.LENGTH_SHORT).show();
			return;
		}
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LatLngBounds.Builder builder = LatLngBounds.builder();
		for (WarningDto dto : typhoonWarnings) {
			MarkerOptions optionsTemp = new MarkerOptions();
			optionsTemp.title(dto.lat+","+dto.lng+","+dto.item0+","+dto.color);
			optionsTemp.snippet(markerType1);
			optionsTemp.anchor(0.5f, 0.5f);
			LatLng latLng = new LatLng(dto.lat, dto.lng);
			builder.include(latLng);
			optionsTemp.position(latLng);
			View mView = inflater.inflate(R.layout.warning_marker_icon, null);
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
			Marker marker = aMap.addMarker(optionsTemp);
			typhoonWarningMarkers.add(marker);
			markerExpandAnimation(marker);
		}
		if (typhoonWarnings.size() > 0) {
			aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
		}
	}

	private void markerExpandAnimation(Marker marker) {
		ScaleAnimation animation = new ScaleAnimation(0,1,0,1);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(300);
		marker.setAnimation(animation);
		marker.startAnimation();
	}

	/**
	 * ?????????????????????????????????
	 */
	private void initPublishListView() {
		publishList.clear();
		TyphoonDto dto = new TyphoonDto();
		dto.publishName = "?????????";
		dto.publishCode = "BABJ";
		dto.isSelected = true;
		publishList.add(dto);
		dto = new TyphoonDto();
		dto.publishName = "?????????";
		dto.publishCode = "BCGZ";
		publishList.add(dto);
		dto = new TyphoonDto();
		dto.publishName = "?????????";
		dto.publishCode = "VHHH";
		publishList.add(dto);
		dto = new TyphoonDto();
		dto.publishName = "?????????";
		dto.publishCode = "RJTD";
		publishList.add(dto);
		dto = new TyphoonDto();
		dto.publishName = "?????????";
		dto.publishCode = "PGTW";
		publishList.add(dto);
//        dto = new TyphoonDto();
//        dto.publishName = "?????????";
//        dto.publishCode = "ECMF";
//        publishList.add(dto);
//        dto = new TyphoonDto();
//        dto.publishName = "???????????????KM";
//        dto.publishCode = "GZRD";
//        publishList.add(dto);
//        dto = new TyphoonDto();
//        dto.publishName = "???????????????9KM";
//        dto.publishCode = "GZRD9KM";
//        publishList.add(dto);


		publishListView = findViewById(R.id.publishListView);
		publishAdapter = new ShawnTyphoonPublishAdapter(mContext, publishList);
		publishListView.setAdapter(publishAdapter);
		publishListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TyphoonDto data = publishList.get(position);
				data.isSelected = !data.isSelected;
				if (publishAdapter != null) {
					publishAdapter.notifyDataSetChanged();
				}

				if (data.isSelected) {
					//???????????????????????????
					for (TyphoonDto dto : nameList) {
						if (dto.isSelected) {
							isShowInfoWindow = true;
							String name = dto.code+" "+dto.name+" "+dto.enName;
							if (TextUtils.equals(data.publishCode, "BABJ") && !TextUtils.isEmpty(dto.id)) {
								OkHttpTyphoonDetailBABJ(data.publishName, data.publishCode, dto.id, name);
							}else {
								OkHttpTyphoonDetailIdea(data.publishName, data.publishCode, dto.tId, name);
							}
						}
					}
				}else {//?????????????????????????????????????????????
					for (TyphoonDto dto : nameList) {
						if (TextUtils.equals(data.publishCode, "BABJ") && !TextUtils.isEmpty(dto.tId)) {
							clearAllPoints(data.publishCode+dto.tId);
						}else {
							clearAllPoints(data.publishCode+dto.id);
						}
					}
				}

			}
		});

	}

	private void initYearListView() {
		yearList.clear();
		final int currentYear = Integer.valueOf(sdf1.format(new Date()));
		int years = 5;//????????????????????????
		for (int i = 0; i < years; i++) {
			TyphoonDto dto = new TyphoonDto();
			dto.yearly = currentYear-i;
			if (i == 0) {
				dto.isSelected = true;
			}
			yearList.add(dto);
		}
		final ListView yearListView = findViewById(R.id.yearListView);
		yearAdapter = new ShawnTyphoonYearAdapter(mContext, yearList);
		yearListView.setAdapter(yearAdapter);
		yearListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				TyphoonDto dto = yearList.get(arg2);
				if (dto.isSelected) {
					return;
				}
				for (TyphoonDto data : yearList) {
					if (data.yearly == dto.yearly) {
						data.isSelected = true;
					}else {
						data.isSelected = false;
					}
				}
				if (yearAdapter != null) {
					yearAdapter.notifyDataSetChanged();
				}

				for (TyphoonDto data : nameList) {
					if (TextUtils.equals(data.status, "1")) {
						data.isSelected = true;
					}else {
						data.isSelected = false;
					}
				}
				if (nameAdapter != null) {
					nameAdapter.notifyDataSetChanged();
				}

				OkHttpTyphoonList(currentYear, dto.yearly);
			}
		});

		OkHttpTyphoonList(currentYear, currentYear);
	}

	/**
	 * ????????????????????????????????????
	 */
	private void OkHttpTyphoonList(final int currentYear, final int selectYear) {
		showDialog();
		final String url = "http://decision-admin.tianqi.cn/Home/other/decision_get_zstflist/year/"+selectYear;
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
										if (!obj.isNull("DATA")) {
											nameList.clear();
											startList.clear();
											JSONArray array = obj.getJSONArray("DATA");
											for (int i = 0; i < array.length(); i++) {
												TyphoonDto dto = new TyphoonDto();
												JSONObject itemObj = array.getJSONObject(i);
												if (!itemObj.isNull("TSID")) {
													dto.tId = itemObj.getString("TSID");
												}
												if (!itemObj.isNull("TFWID")) {
													dto.id = itemObj.getString("TFWID");
												}
												if (!itemObj.isNull("TSENAME")) {
													dto.enName = itemObj.getString("TSENAME");
												}
												if (!itemObj.isNull("TSCNAME")) {
													dto.name = itemObj.getString("TSCNAME");
												}
												if (!itemObj.isNull("INTLID")) {
													dto.code = itemObj.getString("INTLID");
												}
												if (!itemObj.isNull("CRTTIME")) {
													dto.createTime = itemObj.getString("CRTTIME");
												}
												if (!itemObj.isNull("status")) {
													dto.status = itemObj.getString("status");
													if (TextUtils.equals(dto.status, "1")) {
														dto.isSelected = true;//??????????????????????????????
													}else {
														dto.isSelected = false;
													}
												}else {
													dto.status = "0";
													dto.isSelected = false;
												}
												nameList.add(dto);

												//?????????????????????????????????
												if (TextUtils.equals(dto.status, "1")) {
													startList.add(dto);
												}

											}

											//??????????????????????????????????????????????????????????????????????????????????????????
											if (currentYear == selectYear) {
												String typhoonName = "";
												for (TyphoonDto data : startList) {
													String name;
													if (TextUtils.equals(data.enName, "nameless")) {
														if (!TextUtils.isEmpty(typhoonName)) {
															typhoonName = data.enName+"\n"+typhoonName;
														}else {
															typhoonName = data.enName;
														}
														name = data.code + " " + data.enName;
													}else {
														if (!TextUtils.isEmpty(typhoonName)) {
															typhoonName = data.code + " " + data.name + " " + data.enName+"\n"+typhoonName;
														}else {
															typhoonName = data.code + " " + data.name + " " + data.enName;
														}
														name = data.code + " " + data.name + " " + data.enName;
													}
													for (TyphoonDto pub : publishList) {
														if (pub.isSelected) {
															if (TextUtils.equals(pub.publishCode, "BABJ") && !TextUtils.isEmpty(data.id)) {
																OkHttpTyphoonDetailBABJ(pub.publishName, pub.publishCode, data.id, name);
															}else {
																OkHttpTyphoonDetailIdea(pub.publishName, pub.publishCode, data.tId, name);
															}
														}
													}
												}
												if (TextUtils.isEmpty(typhoonName)) {
													tvTyphoonName.setText(getString(R.string.no_typhoon));
												}else {
													tvTyphoonName.setText(typhoonName);
												}
												tvTyphoonName.setVisibility(View.VISIBLE);
											}

											if (startList.size() <= 0) {// ??????????????????
												ivTyphoonPlay.setVisibility(View.GONE);
												ivTyphoonRange.setVisibility(View.GONE);
											}else if (startList.size() == 1) {
//												ivTyphoonPlay.setVisibility(View.VISIBLE);
												ivTyphoonRange.setVisibility(View.VISIBLE);
											}else {
												ivTyphoonPlay.setVisibility(View.GONE);
												ivTyphoonRange.setVisibility(View.VISIBLE);
											}

											if (nameAdapter != null) {
												nameAdapter.notifyDataSetChanged();
											}
											cancelDialog();
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

	private void initNameListView() {
		ListView nameListView = findViewById(R.id.nameListView);
		nameAdapter = new ShawnTyphoonNameAdapter(mContext, nameList);
		nameListView.setAdapter(nameAdapter);
		nameListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				TyphoonDto dto = nameList.get(arg2);
				if (dto.isSelected) {
					return;
				}
				for (TyphoonDto data : nameList) {
					if (TextUtils.equals(data.id, dto.id)) {
						data.isSelected = true;
					}else {
						data.isSelected = false;
					}
				}
				if (nameAdapter != null) {
					nameAdapter.notifyDataSetChanged();
				}

				startList.clear();
				pointsMap.clear();
//				ivTyphoonPlay.setVisibility(View.VISIBLE);
				if (TextUtils.equals(dto.enName, "nameless")) {
					tvTyphoonName.setText(dto.enName);
				}else {
					tvTyphoonName.setText(dto.code+" "+dto.name+" "+dto.enName);
				}

				clearAllPoints(null);
				isShowInfoWindow = true;
				for (TyphoonDto pub : publishList) {
					if (pub.isSelected) {
						if (dto.isSelected) {
							if (TextUtils.equals(pub.publishCode, "BABJ") && !TextUtils.isEmpty(dto.id)) {
								OkHttpTyphoonDetailBABJ(pub.publishName, pub.publishCode, dto.id, tvTyphoonName.getText().toString());
							}else {
								OkHttpTyphoonDetailIdea(pub.publishName, pub.publishCode, dto.tId, tvTyphoonName.getText().toString());
							}
						}
					}
				}

			}
		});
	}

	/**
	 * ??????????????????
	 */
	private void OkHttpTyphoonDetailBABJ(final String publishName, final String publishCode, final String typhoonId, final String typhoonName) {
		showDialog();
		final String url = "http://decision-admin.tianqi.cn/Home/other/gettyphoon/view/"+typhoonId;
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
												List<TyphoonDto> points = new ArrayList<>();//????????????
												List<TyphoonDto> forePoints = new ArrayList<>();//???????????????
												JSONArray array = obj.getJSONArray("typhoon");
												JSONArray itemArray = array.getJSONArray(8);
												for (int j = 0; j < itemArray.length(); j++) {
													JSONArray itemArray2 = itemArray.getJSONArray(j);
													TyphoonDto dto = new TyphoonDto();
													if (!TextUtils.isEmpty(typhoonName)) {
														dto.name = typhoonName;
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
																item = "???";
															}else if (TextUtils.equals(item, "S")) {
																item = "???";
															}else if (TextUtils.equals(item, "W")) {
																item = "???";
															}else if (TextUtils.equals(item, "E")) {
																item = "???";
															}
															windDir = windDir+item;
														}
														dto.wind_dir = windDir;
													}

													String type = itemArray2.getString(3);
													if (TextUtils.equals(type, "TD")) {//????????????
														type = "1";
													}else if (TextUtils.equals(type, "TS")) {//????????????
														type = "2";
													}else if (TextUtils.equals(type, "STS")) {//???????????????
														type = "3";
													}else if (TextUtils.equals(type, "TY")) {//??????
														type = "4";
													}else if (TextUtils.equals(type, "STY")) {//?????????
														type = "5";
													}else if (TextUtils.equals(type, "SuperTY")) {//????????????
														type = "6";
													}
													dto.type = type;
													dto.isFactPoint = true;

													JSONArray array10 = itemArray2.getJSONArray(10);
													for (int m = 0; m < array10.length(); m++) {
														JSONArray itemArray10 = array10.getJSONArray(m);
														if (m == 0) {
															dto.radius_7 = itemArray10.getString(1)+","+itemArray10.getString(2)+","+itemArray10.getString(3)+","+itemArray10.getString(4);
														}else if (m == 1) {
															dto.radius_10 = itemArray10.getString(1)+","+itemArray10.getString(2)+","+itemArray10.getString(3)+","+itemArray10.getString(4);
														}else if (m == 2) {
															dto.radius_12 = itemArray10.getString(1)+","+itemArray10.getString(2)+","+itemArray10.getString(3)+","+itemArray10.getString(4);
														}
													}
													points.add(dto);

													if (!itemArray2.get(11).equals("null") && !itemArray2.get(11).equals(null)) {
														JSONObject obj11 = itemArray2.getJSONObject(11);
														JSONArray array11 = obj11.getJSONArray("BABJ");
														if (array11.length() > 0) {
															forePoints.clear();
														}
														for (int n = 0; n < array11.length(); n++) {
															JSONArray itemArray11 = array11.getJSONArray(n);
															for (int i = 0; i < itemArray11.length(); i++) {
																TyphoonDto data = new TyphoonDto();
																if (!TextUtils.isEmpty(typhoonName)) {
																	data.name = typhoonName;
																}
																data.lng = itemArray11.getDouble(2);
																data.lat = itemArray11.getDouble(3);
																data.pressure = itemArray11.getString(4);
																data.max_wind_speed = itemArray11.getString(5);

																long t2 = itemArray11.getLong(0)*3600*1000;
																long ttt = longTime+t2;
																data.time = sdf2.format(new Date(ttt));

																String babjType = itemArray11.getString(7);
																if (TextUtils.equals(babjType, "TD")) {//????????????
																	babjType = "1";
																}else if (TextUtils.equals(babjType, "TS")) {//????????????
																	babjType = "2";
																}else if (TextUtils.equals(babjType, "STS")) {//???????????????
																	babjType = "3";
																}else if (TextUtils.equals(babjType, "TY")) {//??????
																	babjType = "4";
																}else if (TextUtils.equals(babjType, "STY")) {//?????????
																	babjType = "5";
																}else if (TextUtils.equals(babjType, "SuperTY")) {//????????????
																	babjType = "6";
																}
																data.type = babjType;
																data.isFactPoint = false;

																forePoints.add(data);
															}
														}
													}
												}

												points.addAll(forePoints);
												pointsMap.put(typhoonId, points);
												cancelDialog();

												try {
													int size = startList.size();
													long sleep;
													if (size == 1) {
														sleep = 100;
													}else if (size == 2) {
														sleep = 300;
													}else if (size == 3) {
														sleep = 400;
													}else {
														sleep = 500;
													}
													Thread.sleep(sleep);

													drawTyphoon(publishName, publishCode+typhoonId, false, points);
												} catch (InterruptedException e) {
													e.printStackTrace();
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
	 * ??????????????????
	 */
	private void OkHttpTyphoonDetailIdea(final String publishName, final String publishCode, final String typhoonId, final String typhoonName) {
		showDialog();
		final String url = String.format("http://61.142.114.104:8080/zstyphoon/lhdata/zstf?type=1&tsid=%s&fcid=%s", typhoonId, publishCode);
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
						if (TextUtils.isEmpty(result) || TextUtils.equals(result, "{}")) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									cancelDialog();
									if (!TextUtils.isEmpty(typhoonName)) {
										Toast.makeText(mContext, "??????"+publishName+typhoonName+"??????", Toast.LENGTH_SHORT).show();
									}
								}
							});
							return;
						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								try {
									List<TyphoonDto> allPoints = new ArrayList<>();
									JSONArray array = new JSONArray(result);
									if (array.length() <= 0) {
										cancelDialog();
										if (!TextUtils.isEmpty(typhoonName)) {
											Toast.makeText(mContext, "??????"+publishName+typhoonName+"??????", Toast.LENGTH_SHORT).show();
										}
										return;
									}

									for (int i = 0; i < array.length(); i++) {
										TyphoonDto dto = new TyphoonDto();
										JSONObject itemObj = array.getJSONObject(i);
										if (!TextUtils.isEmpty(typhoonName)) {
											dto.name = typhoonName;
										}
										if (!itemObj.isNull("DDATETIME") && !TextUtils.isEmpty(itemObj.getString("DDATETIME"))) {
											String time = itemObj.getString("DDATETIME");
											if (!TextUtils.isEmpty(time)) {
												try {
													dto.time = sdf3.format(sdf8.parse(time));
												} catch (ParseException e) {
													e.printStackTrace();
												}
											}
										}
										if (!itemObj.isNull("LEADTIME")) {
											int LEADTIME = itemObj.getInt("LEADTIME");
											if (!TextUtils.isEmpty(dto.time)) {
												try {
													long time = sdf3.parse(dto.time).getTime()+1000*60*60*LEADTIME;
													dto.time = sdf3.format(new Date(time));
												} catch (ParseException e) {
													e.printStackTrace();
												}
											}
										}
										if (!itemObj.isNull("LONGITUDE") && !TextUtils.isEmpty(itemObj.getString("LONGITUDE"))) {
											dto.lng = itemObj.getDouble("LONGITUDE");
										}
										if (!itemObj.isNull("LATITUDE") && !TextUtils.isEmpty(itemObj.getString("LATITUDE"))) {
											dto.lat = itemObj.getDouble("LATITUDE");
										}
										if (!itemObj.isNull("PRESSURE") && !TextUtils.isEmpty(itemObj.getString("PRESSURE"))) {
											dto.pressure = itemObj.getString("PRESSURE");
										}
										if (!itemObj.isNull("WINDSPEED") && !TextUtils.isEmpty(itemObj.getString("WINDSPEED"))) {
											dto.max_wind_speed = itemObj.getString("WINDSPEED");
										}
										if (!itemObj.isNull("SPEED") && !TextUtils.isEmpty(itemObj.getString("SPEED"))) {
											dto.move_speed = itemObj.getString("SPEED");
										}
										if (!itemObj.isNull("DIRECTION") && !TextUtils.isEmpty(itemObj.getString("DIRECTION"))) {
											float fx = (float) itemObj.getDouble("DIRECTION");
											dto.wind_dir = CommonUtil.getWindDirection(fx);
										}
										if (!itemObj.isNull("TCRANK") && !TextUtils.isEmpty(itemObj.getString("TCRANK"))) {
											String type = itemObj.getString("TCRANK");
											if (TextUtils.equals(type, "TD")) {//????????????
												type = "1";
											}else if (TextUtils.equals(type, "TS")) {//????????????
												type = "2";
											}else if (TextUtils.equals(type, "STS")) {//???????????????
												type = "3";
											}else if (TextUtils.equals(type, "TY")) {//??????
												type = "4";
											}else if (TextUtils.equals(type, "STY")) {//?????????
												type = "5";
											}else if (TextUtils.equals(type, "SUPER TY")) {//????????????
												type = "6";
											}
											dto.type = type;
										}
										if (!itemObj.isNull("TYPE") && !TextUtils.isEmpty(itemObj.getString("TYPE"))) {
											String isFactFore = itemObj.getString("TYPE");
											if (TextUtils.equals(isFactFore, "0")) {
												dto.isFactPoint = true;
											}else {
												dto.isFactPoint = false;
											}
										}
										if (!itemObj.isNull("RD07") && !TextUtils.isEmpty(itemObj.getString("RD07"))) {
											dto.radius_7 = itemObj.getString("RD07");
										}else if (!itemObj.isNull("RR07") && !TextUtils.isEmpty(itemObj.getString("RR07"))) {
											String r = itemObj.getString("RR07");
											dto.radius_7 = r+","+r+","+r+","+r;
										}

										if (!itemObj.isNull("RD10") && !TextUtils.isEmpty(itemObj.getString("RD10"))) {
											dto.radius_10 = itemObj.getString("RD10");
										}else if (!itemObj.isNull("RR10") && !TextUtils.isEmpty(itemObj.getString("RR10"))) {
											String r = itemObj.getString("RR10");
											dto.radius_10 = r+","+r+","+r+","+r;
										}

										if (!itemObj.isNull("RD12") && !TextUtils.isEmpty(itemObj.getString("RD12"))) {
											dto.radius_12 = itemObj.getString("RD12");
										}else if (!itemObj.isNull("RD12") && !TextUtils.isEmpty(itemObj.getString("RD12"))) {
											String r = itemObj.getString("RD12");
											dto.radius_12 = r+","+r+","+r+","+r;
										}

										allPoints.add(dto);
									}

									cancelDialog();
									try {
										int size = startList.size();
										long sleep;
										if (size == 1) {
											sleep = 100;
										}else if (size == 2) {
											sleep = 300;
										}else if (size == 3) {
											sleep = 400;
										}else {
											sleep = 500;
										}
										Thread.sleep(sleep);

										drawTyphoon(publishName, publishCode+typhoonId, false, allPoints);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

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

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case DRAW_TYPHOON_COMPLETE:
					if (ivTyphoonPlay != null) {
						ivTyphoonPlay.setImageResource(R.drawable.shawn_icon_typhoon_play);
					}

					if (clickLatLng != null) {
						searchAddrByLatLng(clickLatLng.latitude, clickLatLng.longitude);
					}

//					List<TyphoonDto> mPoints = (ArrayList<TyphoonDto>)msg.obj;
//					LatLngBounds.Builder builder = LatLngBounds.builder();
//					for (int i = 0; i < mPoints.size(); i++) {
//						TyphoonDto dto = mPoints.get(i);
//						LatLng latLng = new LatLng(dto.lat, dto.lng);
//						builder.include(latLng);
//					}
//					aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
					break;
				case MSG_ROLING_TYPHOON:
					TyphoonDto data = (TyphoonDto) msg.obj;
					String name = "";
					if (!TextUtils.isEmpty(data.name)) {
						name = data.name;
					}
					String strength = "";
					if (!TextUtils.isEmpty(data.strength)) {
						strength = "("+data.strength+")";
					}
					String wind = "";
					if(!TextUtils.isEmpty(data.max_wind_speed)){
						wind = "????????????"+ WeatherUtil.getHourWindForce(Float.parseFloat(data.max_wind_speed))+" ";
					}
					float distance = AMapUtils.calculateLineDistance(locationLatLng, new LatLng(data.lat, data.lng));
					BigDecimal bd = new BigDecimal(distance/1000);
					float d = bd.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
					String length = d+"??????";
					if (!TextUtils.isEmpty(locationCity)) {
						tvTyphoonInfo.setText(name+strength+wind+"???"+locationCity+length);
					}else {
						tvTyphoonInfo.setText(name+strength+wind+length);
					}
					tvTyphoonInfo.setVisibility(View.VISIBLE);
					break;
			}
		}
	};

    /**
     * ????????????markers
     */
    private void removeRange(String tid) {
        if (!TextUtils.isEmpty(tid)) {
            //??????????????????
            if (rangeLinesMap.containsKey(tid)) {
                List<Polyline> polylines = rangeLinesMap.get(tid);
                for (Polyline polyline : polylines) {
                    if (polyline != null) {
                        polyline.remove();
                    }
                }
                polylines.clear();
                rangeLinesMap.remove(tid);
            }

            //????????????marker
            if (rangeMarkersMap.containsKey(tid)) {
                Marker marker = rangeMarkersMap.get(tid);
                if (marker != null) {
                    marker.remove();
                }
                rangeMarkersMap.remove(tid);
            }
        }else {
            //??????????????????
            for (String typhoonId : rangeLinesMap.keySet()) {
                if (rangeLinesMap.containsKey(typhoonId)) {
                    List<Polyline> polylines = rangeLinesMap.get(typhoonId);
                    for (Polyline polyline : polylines) {
                        if (polyline != null) {
                            polyline.remove();
                        }
                    }
                    polylines.clear();
                }
            }
            rangeLinesMap.clear();

            //????????????marker
            for (String typhoonId : rangeMarkersMap.keySet()) {
                if (rangeMarkersMap.containsKey(typhoonId)) {
                    Marker marker = rangeMarkersMap.get(typhoonId);
                    if (marker != null) {
                        marker.remove();
                    }
                }
            }
            rangeMarkersMap.clear();
        }

    }

    /**
     * ???????????????????????????
     */
    private void removeWindCircle() {
		for (Polygon polygon : windCirclePolygons) {
			polygon.remove();
		}
		windCirclePolygons.clear();
    }

    /**
     * ??????????????????????????????????????????????????????markers
     */
    private void removeTyphoons(String tid) {
        if (!TextUtils.isEmpty(tid)) {
            //??????????????????
            if (factLinesMap.containsKey(tid)) {
                List<Polyline> factLines = factLinesMap.get(tid);
                for (Polyline polyline : factLines) {
                    if (polyline != null) {
                        polyline.remove();
                    }
                }
                factLines.clear();
                factLinesMap.remove(tid);
            }

            //??????????????????
            if (foreLinesMap.containsKey(tid)) {
                List<Polyline> dashLines = foreLinesMap.get(tid);
                for (Polyline polyline : dashLines) {
                    if (polyline != null) {
                        polyline.remove();
                    }
                }
                dashLines.clear();
                foreLinesMap.remove(tid);
            }

            //???????????????????????????
            if (markerPointsMap.containsKey(tid)) {
                List<Marker> markers = markerPointsMap.get(tid);
                for (Marker marker : markers) {
                    if (marker != null) {
                        marker.remove();
                    }
                }
                markers.clear();
                markerPointsMap.remove(tid);
            }

            //??????????????????????????????
            if (rotateMarkersMap.containsKey(tid)) {
                Marker rotateMarker = rotateMarkersMap.get(tid);
                if (rotateMarker != null) {
                    rotateMarker.remove();
                }
                rotateMarkersMap.remove(tid);
            }

            //??????????????????????????????????????????????????????marker
            if (factTimeMarkersMap.containsKey(tid)) {
                Marker factTimeMarker = factTimeMarkersMap.get(tid);
                if (factTimeMarker != null) {
                    factTimeMarker.remove();
                }
                factTimeMarkersMap.remove(tid);
            }

            //???????????????????????????
            if (lastFactPointMap.containsKey(tid)) {
                lastFactPointMap.remove(tid);
            }
        }else {
            //??????????????????
            for (String typhoonId : factLinesMap.keySet()) {
                if (factLinesMap.containsKey(typhoonId)) {
                    List<Polyline> polylines = factLinesMap.get(typhoonId);
                    for (Polyline polyline : polylines) {
                        if (polyline != null) {
                            polyline.remove();
                        }
                    }
                    polylines.clear();
                }
            }
            factLinesMap.clear();

            //??????????????????
            for (String typhoonId : foreLinesMap.keySet()) {
                if (foreLinesMap.containsKey(typhoonId)) {
                    List<Polyline> polylines = foreLinesMap.get(typhoonId);
                    for (Polyline polyline : polylines) {
                        if (polyline != null) {
                            polyline.remove();
                        }
                    }
                    polylines.clear();
                }
            }
            foreLinesMap.clear();

            //???????????????????????????
            for (String typhoonId : markerPointsMap.keySet()) {
                if (markerPointsMap.containsKey(typhoonId)) {
                    List<Marker> markers = markerPointsMap.get(typhoonId);
                    for (Marker marker : markers) {
                        if (marker != null) {
                            marker.remove();
                        }
                    }
                    markers.clear();
                }
            }
            markerPointsMap.clear();

            //??????????????????????????????
            for (String typhoonId : rotateMarkersMap.keySet()) {
                if (rotateMarkersMap.containsKey(typhoonId)) {
                    Marker marker = rotateMarkersMap.get(typhoonId);
                    if (marker != null) {
                        marker.remove();
                    }
                }
            }
            rotateMarkersMap.clear();

            //??????????????????????????????????????????????????????marker
            for (String typhoonId : factTimeMarkersMap.keySet()) {
                if (factTimeMarkersMap.containsKey(typhoonId)) {
                    Marker marker = factTimeMarkersMap.get(typhoonId);
                    if (marker != null) {
                        marker.remove();
                    }
                }
            }
            factTimeMarkersMap.clear();

            //???????????????????????????
            lastFactPointMap.clear();
        }

    }

    /**
     * ??????????????????
     */
    private void clearAllPoints(String typhoonId) {
        removeLocationCirces();
        removeWindCircle();
        removeRange(typhoonId);
        removeTyphoons(typhoonId);
    }

	/**
	 * ????????????
	 * @param isAnimate
	 */
	private void drawTyphoon(String publishName, String typhoonId, boolean isAnimate, List<TyphoonDto> list) {
		if (list.isEmpty()) {
			return;
		}

		if (mRoadThread != null) {
			mRoadThread.cancel();
			mRoadThread = null;
		}
		mRoadThread = new RoadThread(publishName, typhoonId, list, isAnimate);
		mRoadThread.start();
	}

	/**
	 * ???????????????
	 */
	private class RoadThread extends Thread {

        private boolean cancelled;
        private boolean isAnimate;
        private List<TyphoonDto> allPoints;//????????????????????????
        private String publishName,typhoonId;

        private RoadThread(String publishName, String typhoonId, List<TyphoonDto> allPoints, boolean isAnimate) {
            this.publishName = publishName;
        	this.typhoonId = typhoonId;
            this.allPoints = allPoints;
            this.isAnimate = isAnimate;
        }

        @Override
        public void run() {
            final int len = allPoints.size();

            //???????????????
            final List<TyphoonDto> factPoints = new ArrayList<>();
            for (int j = 0; j < len; j++) {
                if (allPoints.get(j).isFactPoint) {
                    factPoints.add(allPoints.get(j));
                }
            }

            List<Polyline> factLines = new ArrayList<>();//????????????
            List<Polyline> foreLines = new ArrayList<>();//????????????
            List<Marker> markerPoints = new ArrayList<>();//???????????????
            for (int i = 0; i < len; i++) {
                if (cancelled) {
                    break;
                }

                if (i == len-1) {
                    Message msg = handler.obtainMessage(DRAW_TYPHOON_COMPLETE);
                    msg.obj = allPoints;
                    handler.sendMessage(msg);
                }

                if (isAnimate) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

				final TyphoonDto firstPoint = allPoints.get(i);//????????????
				final TyphoonDto lastPoint = i >= (len-1) ? null : allPoints.get(i+1);//???????????????
				TyphoonDto lastFactPoint = null;//?????????????????????
				if (factPoints.size() > 0) {
					lastFactPoint = factPoints.get(factPoints.size()-1);//?????????????????????
				}
                drawRoute(publishName, typhoonId, factLines, foreLines, markerPoints, firstPoint, lastPoint, lastFactPoint);
            }
            factLinesMap.put(typhoonId, factLines);
            foreLinesMap.put(typhoonId, foreLines);
            markerPointsMap.put(typhoonId, markerPoints);

        }

        private void cancel() {
            cancelled = true;
        }
	}

	private void drawRoute(String publishName, final String typhoonId, List<Polyline> factLines, List<Polyline> foreLines, List<Marker> markerPoints, TyphoonDto firstPoint, TyphoonDto lastPoint, TyphoonDto lastFactPoint) {
		if (lastPoint == null) {//???????????????
			lastPoint = firstPoint;
		}
		if (lastFactPoint == null) {
			lastFactPoint = firstPoint;
		}

		int lineColor = 0;
		if (typhoonId.contains("BABJ")) {//?????????
			lineColor = Color.RED;
		}else if (typhoonId.contains("BCGZ")) {//?????????
			lineColor = 0xffEF9A1A;
		}else if (typhoonId.contains("VHHH")) {//?????????
			lineColor = Color.YELLOW;
		}else if (typhoonId.contains("RJTD")) {//?????????
			lineColor = Color.GREEN;
		}else if (typhoonId.contains("PGTW")) {//?????????
			lineColor = Color.BLUE;
		}

        double firstLat = firstPoint.lat;
        double firstLng = firstPoint.lng;
        double lastLat = lastPoint.lat;
        double lastLng = lastPoint.lng;
        LatLng firstLatLng = new LatLng(firstLat, firstLng);
        LatLng lastLatLng = new LatLng(lastLat, lastLng);

        //?????????
        ArrayList<LatLng> latLngs = new ArrayList<>();
        if (lastPoint.isFactPoint) {//?????????
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.width(CommonUtil.dip2px(mContext, 2));
            polylineOptions.color(lineColor);
            latLngs.add(firstLatLng);
            latLngs.add(lastLatLng);
            polylineOptions.addAll(latLngs);
            Polyline factLine = aMap.addPolyline(polylineOptions);
            factLines.add(factLine);
        } else {//????????????
            double dis = Math.sqrt(Math.pow(firstLat-lastLat, 2)+ Math.pow(firstLng-lastLng, 2));
            int numPoint = (int) Math.floor(dis/0.2);
            double lng_per = (lastLng-firstLng)/numPoint;
            double lat_per = (lastLat-firstLat)/numPoint;
            for (int i = 0; i < numPoint; i++) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(lineColor);
                polylineOptions.width(CommonUtil.dip2px(mContext, 2));
                latLngs.add(new LatLng(firstLat+i*lat_per, firstLng+i*lng_per));
                if (i % 2 == 1) {
                    polylineOptions.addAll(latLngs);
                    Polyline dashLine = aMap.addPolyline(polylineOptions);
                    foreLines.add(dashLine);
                    latLngs.clear();
                }
            }
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shawn_typhoon_point_icon, null);


		if (firstPoint == lastPoint) {//?????????????????????????????????
			if (!TextUtils.isEmpty(publishName)) {
				TextView tvName = view.findViewById(R.id.tvName);
				tvName.setText(publishName);
				tvName.setVisibility(View.VISIBLE);
			}
		}


        ImageView ivPoint = view.findViewById(R.id.ivPoint);
        if (TextUtils.equals(firstPoint.type, "1")) {
            ivPoint.setImageResource(R.drawable.shawn_typhoon_level1);
        }else if (TextUtils.equals(firstPoint.type, "2")) {
            ivPoint.setImageResource(R.drawable.shawn_typhoon_level2);
        }else if (TextUtils.equals(firstPoint.type, "3")) {
            ivPoint.setImageResource(R.drawable.shawn_typhoon_level3);
        }else if (TextUtils.equals(firstPoint.type, "4")) {
            ivPoint.setImageResource(R.drawable.shawn_typhoon_level4);
        }else if (TextUtils.equals(firstPoint.type, "5")) {
            ivPoint.setImageResource(R.drawable.shawn_typhoon_level5);
        }else if (TextUtils.equals(firstPoint.type, "6")) {
            ivPoint.setImageResource(R.drawable.shawn_typhoon_level6);
        }else {//?????????
            ivPoint.setImageResource(R.drawable.shawn_typhoon_yb);
        }
        MarkerOptions options = new MarkerOptions();
        options.title(firstPoint.name+"|"+firstPoint.content(mContext)+"|"+firstPoint.radius_7+"|"+firstPoint.radius_10+"|"+firstPoint.radius_12);
        options.snippet(markerType2);
        options.anchor(0.5f, 0.5f);
        options.position(firstLatLng);
        options.icon(BitmapDescriptorFactory.fromView(view));
        Marker marker = aMap.addMarker(options);
        markerPoints.add(marker);

        if (firstPoint.isFactPoint && lastFactPoint == firstPoint) {//?????????????????????
			if(isShowInfoWindow) {
				clickMarker = marker;
				clickMarker.showInfoWindow();

				//?????????????????????????????????????????????????????????
				drawWindCircle(firstPoint.radius_7, firstPoint.radius_10, firstPoint.radius_12, firstLatLng);

				//???????????????????????????????????????
				aMap.animateCamera(CameraUpdateFactory.newLatLng(firstLatLng));
			}

            //????????????????????????
            MarkerOptions tOption = new MarkerOptions();
            tOption.position(firstLatLng);
            tOption.anchor(0.5f, 0.5f);
//            tOption.icon(BitmapDescriptorFactory.fromAsset("typhoon/typhoon_icon1.png"));
            ArrayList<BitmapDescriptor> iconList = new ArrayList<>();
            for (int i = 1; i <= 9; i++) {
                iconList.add(BitmapDescriptorFactory.fromAsset("typhoon/typhoon_icon"+i+".png"));
            }
            tOption.icons(iconList);
            tOption.period(6);
            tOption.zIndex(-10);
            Marker rotateMarker = aMap.addMarker(tOption);
            rotateMarker.setClickable(false);
            rotateMarkersMap.put(typhoonId, rotateMarker);

            //???????????????????????????????????????
            lastFactPointMap.put(typhoonId, lastFactPoint);

            ranging(typhoonId);
		}
	}

	/**
	 * ???????????????????????????
	 */
	private void drawWindCircle(String radius_7, String radius_10, String radius_12, LatLng center) {
		removeWindCircle();

		//????????????
		if (!TextUtils.isEmpty(radius_7) && !TextUtils.equals(radius_7, "null") && radius_7.contains(",")) {
			String[] radiuss = radius_7.split(",");
			List<LatLng> wind7Points = new ArrayList<>();
			getWindCirclePoints(center, radiuss[0], 0, wind7Points);
			getWindCirclePoints(center, radiuss[3], 90, wind7Points);
			getWindCirclePoints(center, radiuss[2], 180, wind7Points);
			getWindCirclePoints(center, radiuss[1], 270, wind7Points);
			if (wind7Points.size() > 0) {
				PolygonOptions polygonOptions = new PolygonOptions();
				polygonOptions.strokeWidth(3).strokeColor(Color.YELLOW).fillColor(0x20FFFF00);
				for (LatLng latLng : wind7Points) {
					polygonOptions.add(latLng);
				}
				Polygon polygon = aMap.addPolygon(polygonOptions);
				windCirclePolygons.add(polygon);
			}
		}

		//????????????
		if (!TextUtils.isEmpty(radius_10) && !TextUtils.equals(radius_10, "null") && radius_10.contains(",")) {
			String[] radiuss = radius_10.split(",");
			List<LatLng> wind10Points = new ArrayList<>();
			getWindCirclePoints(center, radiuss[0], 0, wind10Points);
			getWindCirclePoints(center, radiuss[3], 90, wind10Points);
			getWindCirclePoints(center, radiuss[2], 180, wind10Points);
			getWindCirclePoints(center, radiuss[1], 270, wind10Points);
			if (wind10Points.size() > 0) {
				PolygonOptions polygonOptions = new PolygonOptions();
				polygonOptions.strokeWidth(3).strokeColor(0xffFE9900).fillColor(0x20FF0000);
				for (LatLng latLng : wind10Points) {
					polygonOptions.add(latLng);
				}
				Polygon polygon = aMap.addPolygon(polygonOptions);
				windCirclePolygons.add(polygon);
			}
		}

		//???????????????
		if (!TextUtils.isEmpty(radius_12) && !TextUtils.equals(radius_12, "null") && radius_12.contains(",")) {
			String[] radiuss = radius_12.split(",");
			List<LatLng> wind12Points = new ArrayList<>();
			getWindCirclePoints(center, radiuss[0], 0, wind12Points);
			getWindCirclePoints(center, radiuss[3], 90, wind12Points);
			getWindCirclePoints(center, radiuss[2], 180, wind12Points);
			getWindCirclePoints(center, radiuss[1], 270, wind12Points);
			if (wind12Points.size() > 0) {
				PolygonOptions polygonOptions = new PolygonOptions();
				polygonOptions.strokeWidth(3).strokeColor(Color.RED).fillColor(0x20FF0000);
				for (LatLng latLng : wind12Points) {
					polygonOptions.add(latLng);
				}
				Polygon polygon = aMap.addPolygon(polygonOptions);
				windCirclePolygons.add(polygon);
			}
		}

	}

	/**
	 * ??????????????????????????????
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @return
	 */
	private void getWindCirclePoints(LatLng center, String radius, double startAngle, List<LatLng> points) {
		if (!TextUtils.isEmpty(radius) && !TextUtils.equals(radius, "null")) {
			double r = 6371000.79;
			int numpoints = 90;
			double phase = Math.PI/2 / numpoints;

			for (int i = 0; i <= numpoints; i++) {
				double dx = (Integer.valueOf(radius)*1000 * Math.cos((i+startAngle) * phase));
				double dy = (Integer.valueOf(radius)*1000 * Math.sin((i+startAngle) * phase));//??????1.6 ????????????
				double lng = center.longitude + dx / (r * Math.cos(center.latitude * Math.PI / 180) * Math.PI / 180);
				double lat = center.latitude + dy / (r * Math.PI / 180);
				points.add(new LatLng(lat, lng));
			}

		}
	}

    /**
     * ??????
     */
    private void ranging(String tid) {
        if (clickLatLng == null || !isRanging) {
            return;
        }

		addLocationCircles();
        if (!TextUtils.isEmpty(tid)) {
            rangingSingle(tid);
        }else {
            for (String typhoonId : lastFactPointMap.keySet()) {
                rangingSingle(typhoonId);
            }
        }
        searchAddrByLatLng(clickLatLng.latitude, clickLatLng.longitude);
    }

    /**
     * ???????????????
     * @param typhoonId
     */
    private void rangingSingle(String typhoonId) {
        double locationLat = clickLatLng.latitude;
        double locationLng = clickLatLng.longitude;
        if (lastFactPointMap.containsKey(typhoonId)) {
            TyphoonDto dto = lastFactPointMap.get(typhoonId);
            double lat = dto.lat;
            double lng = dto.lng;
            double dis = Math.sqrt(Math.pow(locationLat-lat, 2)+ Math.pow(locationLng-lng, 2));
            int numPoint = (int) Math.floor(dis/0.2);
            double lng_per = (lng-locationLng)/numPoint;
            double lat_per = (lat-locationLat)/numPoint;
            List<Polyline> polylines = new ArrayList<>();
            List<LatLng> ranges = new ArrayList<>();
            for (int i = 0; i < numPoint; i++) {
                PolylineOptions line = new PolylineOptions();
                line.color(0xff6291E1);
                line.width(CommonUtil.dip2px(mContext, 2));
                ranges.add(new LatLng(locationLat+i*lat_per, locationLng+i*lng_per));
                if (i % 2 == 1) {
                    line.addAll(ranges);
                    Polyline polyline = aMap.addPolyline(line);
                    polylines.add(polyline);
                    ranges.clear();
                }
            }
            rangeLinesMap.put(typhoonId, polylines);

            LatLng centerLatLng = new LatLng((locationLat+lat)/2, (locationLng+lng)/2);
            addRangeMarker(typhoonId, centerLatLng, clickLatLng, new LatLng(lat, lng));
        }
    }

    /**
     * ?????????????????????????????????
     */
    private void addRangeMarker(String typhoonId, LatLng latLng, LatLng startLatLng, LatLng endLatLng) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.shawn_typhoon_range_marker_icon, null);
        TextView tvName = mView.findViewById(R.id.tvName);
		float distance = AMapUtils.calculateLineDistance(startLatLng, endLatLng);
		BigDecimal bd = new BigDecimal(distance/1000);
		float d = bd.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        tvName.setText("????????????"+d+"??????");
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        options.icon(BitmapDescriptorFactory.fromView(mView));
        Marker marker = aMap.addMarker(options);
        marker.setClickable(false);
        rangeMarkersMap.put(typhoonId, marker);
    }

    /**
     * ???????????????????????????????????????
     * @param lat
     * @param lng
     */
    private void searchAddrByLatLng(double lat, double lng) {
        //latLonPoint??????????????????Latlng???????????????????????????????????????GeocodeSearch.AMAP?????????????????????????????????GPS???????????????
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(lat, lng), 200, GeocodeSearch.AMAP);
        if (geocoderSearch == null) {
			geocoderSearch = new GeocodeSearch(mContext);
		}
        geocoderSearch.getFromLocationAsyn(query);
		geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
			@Override
			public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
				if (result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null) {
					locationCity = result.getRegeocodeAddress().getCity();

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tvTyphoonInfo.removeAllViews();
							tvTyphoonInfo.setFactory(new ViewSwitcher.ViewFactory() {
								@Override
								public View makeView() {
									TextView textView = new TextView(mContext);
									textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
									textView.setTextColor(Color.WHITE);
									textView.setEllipsize(TextUtils.TruncateAt.END);
									return textView;
								}
							});

							List<TyphoonDto> lastFactPoints = new ArrayList<>();
							for (String typhoonId : lastFactPointMap.keySet()) {
								if (lastFactPointMap.containsKey(typhoonId)) {
									TyphoonDto data = lastFactPointMap.get(typhoonId);
									if (data != null) {
										lastFactPoints.add(data);
									}
								}
							}

							removeThread();
							rollingThread = new RollingThread(lastFactPoints);
							rollingThread.start();

						}
					});
				}
			}

			@Override
			public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

			}
		});
    }

	private void removeThread() {
		if (rollingThread != null) {
			rollingThread.cancel();
			rollingThread = null;
		}
	}

	private class RollingThread extends Thread {

		static final int STATE_PLAYING = 1;
		static final int STATE_CANCEL = 3;
		private int state;
		private int index;
		private List<TyphoonDto> lastFactPoints;

		private RollingThread(List<TyphoonDto> lastFactPoints) {
			this.lastFactPoints = lastFactPoints;
		}

		@Override
		public void run() {
			super.run();
			this.state = STATE_PLAYING;
			while (index < lastFactPoints.size()) {
				TyphoonDto dto = lastFactPoints.get(index);
				if (state == STATE_CANCEL) {
					break;
				}
				try {
					Message msg = handler.obtainMessage();
					msg.what = MSG_ROLING_TYPHOON;
					msg.obj = dto;
					handler.sendMessage(msg);

					sleep(4000);
					index++;
					if (index >= lastFactPoints.size()) {
						index = 0;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void cancel() {
			this.state = STATE_CANCEL;
		}
	}

    @Override
	public boolean onMarkerClick(Marker marker) {
        if (marker != null && marker != locationMarker) {
        	if (TextUtils.equals(marker.getSnippet(), markerType2)) {//?????????
				if (!TextUtils.isEmpty(marker.getTitle())) {
					String[] title = marker.getTitle().split("\\|");
					drawWindCircle(title[2], title[3], title[4], marker.getPosition());
				}
			}else if (TextUtils.equals(marker.getSnippet(), markerType1)) {//??????

			}

			clickMarker = marker;
            if (clickMarker.isInfoWindowShown()) {
            	clickMarker.hideInfoWindow();
			}else {
				clickMarker.showInfoWindow();
			}

        }
        return true;
	}

	@Override
	public void onMapClick(LatLng latLng) {
		//???????????????
        if (isRanging) {//???????????????
			clickLatLng = latLng;
            removeRange(null);
            addLocationMarker();
            ranging(null);
        }

		mapClick();
	}

    private void mapClick() {
        if (clickMarker != null && clickMarker.isInfoWindowShown()) {
            clickMarker.hideInfoWindow();
        }
    }

	@Override
	public View getInfoContents(Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mView = null;
		if (TextUtils.equals(marker.getSnippet(), markerType1)) {//??????marker
			mView = inflater.inflate(R.layout.warning_marker_icon_info, null);
			final List<WarningDto> infoList = addInfoList(marker);
			ListView mListView = mView.findViewById(R.id.listView);
			WarningAdapter mAdapter = new WarningAdapter(mContext, infoList, true);
			mListView.setAdapter(mAdapter);
			ViewGroup.LayoutParams params = mListView.getLayoutParams();
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
					mapClick();
				}
			});
		}

		return mView;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}

	private List<WarningDto> addInfoList(Marker marker) {
		List<WarningDto> infoList = new ArrayList<>();
		for (WarningDto dto : typhoonWarnings) {
			String[] latLng = marker.getTitle().split(",");
			if (TextUtils.equals(latLng[0], dto.lat+"") && TextUtils.equals(latLng[1], dto.lng+"")) {
				infoList.add(dto);
			}
		}
		return infoList;
	}

	private void intentDetail(WarningDto data) {
		Intent intentDetail = new Intent(mContext, WarningDetailActivity.class);
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		intentDetail.putExtra(CONST.COLUMN_ID, columnId);
		Bundle bundle = new Bundle();
		bundle.putParcelable("data", data);
		intentDetail.putExtras(bundle);
		startActivity(intentDetail);
	}

	private void legendAnimation(boolean flag, final RelativeLayout reLayout) {
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation;
		if (!flag) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, 1f,
					Animation.RELATIVE_TO_SELF, 0);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		reLayout.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				reLayout.clearAnimation();
			}
		});
	}

	/**
	 * ????????????????????????
	 */
	private void OkHttpRadar() {
		showDialog();
		final String url = "http://api.tianqi.cn:8070/v1/img.py";
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
										if (!obj.isNull("status")) {
											if (obj.getString("status").equals("ok")) {
												if (!obj.isNull("radar_img")) {
													JSONArray array = new JSONArray(obj.getString("radar_img"));
													radarList.clear();
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
														radarList.add(dto);
													}

													if (radarList.size() > 0) {
														startDownLoadImgs(radarList);
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
				});
			}
		}).start();

	}

	private void startDownLoadImgs(List<MinuteFallDto> list) {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		mRadarManager.loadImagesAsyn(list, new CaiyunManager.RadarListener() {
			@Override
			public void onResult(int result, List<MinuteFallDto> images) {
				if (result == CaiyunManager.RadarListener.RESULT_SUCCESSED) {
					mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);

					removeRadar();
					mRadarThread = new RadarThread(radarList);
					mRadarThread.start();
				}
			}

			@Override
			public void onProgress(String url, int progress) {
//		Message msg = new Message();
//		msg.obj = progress;
//		msg.what = HANDLER_PROGRESS;
//		mHandler.sendMessage(msg);
			}
		});
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case HANDLER_SHOW_RADAR:
					if (msg.obj != null) {
						MinuteFallDto dto = (MinuteFallDto) msg.obj;
						if (!TextUtils.isEmpty(dto.path)) {
							try {
								Bitmap bitmap = BitmapFactory.decodeFile(dto.path);
								if (bitmap != null) {
									showRadar(bitmap, dto.getP1(), dto.getP2(), dto.getP3(), dto.getP4());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					break;
				case HANDLER_LOAD_FINISHED:
					ivTyphoonRadar.setVisibility(View.VISIBLE);
					cancelDialog();
					break;
				default:
					break;
			}
		};
	};

	private void showRadar(Bitmap bitmap, double p1, double p2, double p3, double p4) {
		BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = new LatLngBounds.Builder()
				.include(new LatLng(p3, p2))
				.include(new LatLng(p1, p4))
				.build();

		if (radarOverlay == null) {
			radarOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
					.anchor(0.5f, 0.5f)
					.positionFromBounds(bounds)
					.image(fromView)
					.transparency(0.0f));
		} else {
			radarOverlay.setImage(null);
			radarOverlay.setPositionFromBounds(bounds);
			radarOverlay.setImage(fromView);
		}
		aMap.runOnDrawFrame();
	}

	private class RadarThread extends Thread {

		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<MinuteFallDto> images;
		private int state;
		private int index;
		private int count;

		private RadarThread(List<MinuteFallDto> images) {
			this.images = images;
			this.count = images.size();
			this.index = 0;
			this.state = STATE_NONE;
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
	}

	/**
	 * ?????????????????????????????????
	 */
	private void removeRadar() {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		if (radarOverlay != null) {
			radarOverlay.remove();
			radarOverlay = null;
		}
	}

	/**
	 * ??????????????????
	 */
	private void OkHttpCloudChart() {
		showDialog();
		final String url = "http://decision-admin.tianqi.cn/Home/other/getDecisionCloudImages";
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
										if (!obj.isNull("l")) {
											JSONArray array = obj.getJSONArray("l");
											if (array.length() > 0) {
												JSONObject itemObj = array.getJSONObject(0);
												String imgUrl = itemObj.getString("l2");
												if (!TextUtils.isEmpty(imgUrl)) {
													Picasso.get().load(imgUrl).into(new Target() {
														@Override
														public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
															cloudBitmap = bitmap;
															ivTyphoonCloud.setVisibility(View.VISIBLE);
															cancelDialog();
															drawCloud(bitmap);
														}
														@Override
														public void onBitmapFailed(Exception e, Drawable errorDrawable) {
														}
														@Override
														public void onPrepareLoad(Drawable placeHolderDrawable) {
														}
													});
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
	private void drawCloud(Bitmap bitmap) {
		if (bitmap == null || !isCloudOn) {
			return;
		}
		BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = new LatLngBounds.Builder()
				.include(new LatLng(-10.787277369124666, 62.8820698883665))
				.include(new LatLng(56.385845314127209, 161.69675114151386))
				.build();

		if (cloudOverlay == null) {
			cloudOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
					.anchor(0.5f, 0.5f)
					.positionFromBounds(bounds)
					.image(fromView)
					.transparency(0.2f));
		} else {
			cloudOverlay.setImage(null);
			cloudOverlay.setPositionFromBounds(bounds);
			cloudOverlay.setImage(fromView);
		}
	}

	/**
	 * ????????????
	 */
	private void removeCloud() {
		if (cloudOverlay != null) {
			cloudOverlay.remove();
			cloudOverlay = null;
		}
	}

	/**
	 * ??????????????????
	 */
	private void OkHttpWind() {
		showDialog();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.windGFS("1000")).build(), new Callback() {
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
								if (windData == null) {
									windData = new WindData();
								}
								if (!obj.isNull("gridHeight")) {
									windData.height = obj.getInt("gridHeight");
								}
								if (!obj.isNull("gridWidth")) {
									windData.width = obj.getInt("gridWidth");
								}
								if (!obj.isNull("x0")) {
									windData.x0 = obj.getDouble("x0");
								}
								if (!obj.isNull("y0")) {
									windData.y0 = obj.getDouble("y0");
								}
								if (!obj.isNull("x1")) {
									windData.x1 = obj.getDouble("x1");
								}
								if (!obj.isNull("y1")) {
									windData.y1 = obj.getDouble("y1");
								}
								if (!obj.isNull("filetime")) {
									windData.filetime = obj.getString("filetime");
								}

								if (!obj.isNull("field")) {
									windData.dataList.clear();
									JSONArray array = new JSONArray(obj.getString("field"));
									for (int i = 0; i < array.length(); i+=2) {
										WindDto dto2 = new WindDto();
										dto2.initX = (float)(array.optDouble(i));
										dto2.initY = (float)(array.optDouble(i+1));
										windData.dataList.add(dto2);
									}
								}

								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										cancelDialog();
										reloadWind();
									}
								});

							} catch (JSONException e1) {
								e1.printStackTrace();
							}
						}
					}
				});
			}
		}).start();
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		container.removeAllViews();
		container2.removeAllViews();
		tvFileTime.setVisibility(View.GONE);
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		zoom = arg0.zoom;
		Log.e("zoomzoom", zoom+"");
		if (isWindOn) {
			reloadWind();
		}
	}

	long t = new Date().getTime();

	/**
	 * ??????????????????
	 */
	private void reloadWind() {
		if (windData == null) {
			return;
		}
		t = new Date().getTime() - t;
		if (t < 1000) {
			return;
		}

		LatLng latLngStart = aMap.getProjection().fromScreenLocation(new Point(0, 0));
		LatLng latLngEnd = aMap.getProjection().fromScreenLocation(new Point(CommonUtil.widthPixels(this), CommonUtil.heightPixels(this)));
		windData.latLngStart = latLngStart;
		windData.latLngEnd = latLngEnd;
		if (waitWindView == null) {
			waitWindView = new WaitWindView(mContext);
			waitWindView.init(TyhpoonActivity.this);
			waitWindView.setData(windData, zoom);
			waitWindView.start();
			waitWindView.invalidate();
		} else {
			waitWindView.setData(windData, zoom);
		}

		container.removeAllViews();
		container.addView(waitWindView);
		ivTyphoonWind.setVisibility(View.VISIBLE);
		tvFileTime.setVisibility(View.VISIBLE);
		if (!TextUtils.isEmpty(windData.filetime)) {
			try {
				tvFileTime.setText("GFS "+sdf3.format(sdf2.parse(windData.filetime))+"????????????");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1???????????????
		//bitmap2????????????????????????view
		Bitmap bitmap2 = CommonUtil.captureView(reShare);
		Bitmap bitmap3 = CommonUtil.mergeBitmap(TyhpoonActivity.this, bitmap1, bitmap2, true);
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap2);
		Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.legend_share_portrait);
		Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
		CommonUtil.clearBitmap(bitmap3);
		CommonUtil.clearBitmap(bitmap4);
		CommonUtil.share(TyhpoonActivity.this, bitmap);
	}

	@Override
	public void onMapScreenShot(Bitmap bitmap, int i) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.ivTyphoonRadar:
				isRadarOn = !isRadarOn;
				if (isRadarOn) {//???????????????
					ivTyphoonRadar.setImageResource(R.drawable.shawn_icon_typhoon_radar_on);
					if (radarList.size() <= 0) {
						OkHttpRadar();
					}else {
						removeRadar();
						mRadarThread = new RadarThread(radarList);
						mRadarThread.start();
					}

					ivTyphoonCloud.setImageResource(R.drawable.shawn_icon_typhoon_cloud_off);
					removeCloud();
					isCloudOn = false;

				}else {//???????????????
					ivTyphoonRadar.setImageResource(R.drawable.shawn_icon_typhoon_radar_off);
					removeRadar();
				}
				break;
			case R.id.ivTyphoonCloud:
				isCloudOn = !isCloudOn;
				if (isCloudOn) {//????????????
					ivTyphoonCloud.setImageResource(R.drawable.shawn_icon_typhoon_cloud_on);
					if (cloudBitmap == null) {
						OkHttpCloudChart();
					}else {
						drawCloud(cloudBitmap);
					}

					ivTyphoonRadar.setImageResource(R.drawable.shawn_icon_typhoon_radar_off);
					removeRadar();
					isRadarOn = false;

				}else {//????????????
					ivTyphoonCloud.setImageResource(R.drawable.shawn_icon_typhoon_cloud_off);
					removeCloud();
				}
				break;
			case R.id.ivTyphoonWind:
				isWindOn = !isWindOn;
				if (isWindOn) {//????????????
					if (windData == null) {
						OkHttpWind();
					}else {
						reloadWind();
					}
					ivTyphoonWind.setImageResource(R.drawable.shawn_icon_typhoon_wind_on);
					tvFileTime.setVisibility(View.VISIBLE);
				}else {//????????????
					ivTyphoonWind.setImageResource(R.drawable.shawn_icon_typhoon_wind_off);
					tvFileTime.setVisibility(View.GONE);
					container.removeAllViews();
					container2.removeAllViews();
					tvFileTime.setVisibility(View.GONE);
				}
				break;
			case R.id.ivTyphoonWarning:
				isWarningOn = !isWarningOn;
				if (isWarningOn) {
					addTyphoonWarningMarkers();
					ivTyphoonWarning.setImageResource(R.drawable.shawn_icon_typhoon_warning_on);
				}else {
					removeTyphoonWarningMarkers();
					ivTyphoonWarning.setImageResource(R.drawable.shawn_icon_typhoon_warning_off);
				}
				break;

			case R.id.ivTyphoonRange:
				isRanging = !isRanging;
				if (isRanging) {
					ivTyphoonRange.setImageResource(R.drawable.shawn_icon_typhoon_range_on);
					addLocationMarker();
					ranging(null);
				}else {
					ivTyphoonRange.setImageResource(R.drawable.shawn_icon_typhoon_range_off);
					removeLocationCirces();
					removeRange(null);
				}
				break;
			case R.id.ivLocation:
				if (locationLatLng != null) {
					aMap.animateCamera(CameraUpdateFactory.newLatLng(locationLatLng));
				}
				break;
			case R.id.ivTyphoonPlay:
				clearAllPoints(null);
				ivTyphoonPlay.setImageResource(R.drawable.shawn_icon_typhoon_pause);
//				for (String typhoonId : pointsMap.keySet()) {
//					if (pointsMap.containsKey(typhoonId)) {
//						List<TyphoonDto> points = pointsMap.get(typhoonId);
//						drawTyphoon(typhoonId, true, points);
//					}
//				}
				break;
			case R.id.ivLegend:
			case R.id.ivCancelLegend:
				if (reLegend.getVisibility() == View.GONE) {
					legendAnimation(false, reLegend);
					reLegend.setVisibility(View.VISIBLE);
					ivLegend.setClickable(false);
					ivTyphoonList.setClickable(false);
				}else {
					legendAnimation(true, reLegend);
					reLegend.setVisibility(View.GONE);
					ivLegend.setClickable(true);
					ivTyphoonList.setClickable(true);
				}
				break;
			case R.id.ivTyphoonList:
			case R.id.ivCancelList:
				if (reTyphoonList.getVisibility() == View.GONE) {
					legendAnimation(false, reTyphoonList);
					reTyphoonList.setVisibility(View.VISIBLE);
					ivLegend.setClickable(false);
					ivTyphoonList.setClickable(false);
				}else {
					legendAnimation(true, reTyphoonList);
					reTyphoonList.setVisibility(View.GONE);
					ivLegend.setClickable(true);
					ivTyphoonList.setClickable(true);
				}
				break;
			case R.id.ivShare:
				aMap.getMapScreenShot(TyhpoonActivity.this);
				break;
			case R.id.ivGuide:
				ivGuide.setVisibility(View.GONE);
				CommonUtil.saveGuidePageState(mContext, this.getClass().getName());
				break;
		}
	}

	/**
	 * ??????????????????
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (mapView != null) {
			mapView.onResume();
		}
	}

	/**
	 * ??????????????????
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (mapView != null) {
			mapView.onPause();
		}
	}

	/**
	 * ??????????????????
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mapView != null) {
			mapView.onSaveInstanceState(outState);
		}
	}

	/**
	 * ??????????????????
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mapView != null) {
			mapView.onDestroy();
		}
		if (mRadarManager != null) {
			mRadarManager.onDestory();
		}
		removeRadar();
		removeThread();
	}

	//???????????????????????????
	private String[] allPermissions = new String[] {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	//?????????????????????
	private List<String> deniedList = new ArrayList<>();
	/**
	 * ??????????????????
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
			if (deniedList.isEmpty()) {//?????????????????????
				init();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//???list????????????
				ActivityCompat.requestPermissions(TyhpoonActivity.this, permissions, AuthorityUtil.AUTHOR_LOCATION);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_LOCATION:
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
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"?????????????????????????????????????????????????????????????????????");
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(TyhpoonActivity.this, permission)) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"?????????????????????????????????????????????????????????????????????");
							break;
						}
					}
				}
				break;
		}
	}

}
