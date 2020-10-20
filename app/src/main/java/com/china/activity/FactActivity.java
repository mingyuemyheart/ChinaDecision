package com.china.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapUtils;
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
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.china.R;
import com.china.common.CONST;
import com.china.dto.MinuteFallDto;
import com.china.dto.StationMonitorDto;
import com.china.manager.CaiyunManager;
import com.china.manager.DBManager;
import com.china.manager.FactManager;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.SecretUrlUtil;
import com.china.view.StationCursorView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 站点监测
 */
public class FactActivity extends BaseActivity implements OnClickListener, AMapLocationListener, OnMarkerClickListener,
        OnMapClickListener, OnGeocodeSearchListener, OnCameraChangeListener, OnMapScreenShotListener,DistrictSearch.OnDistrictSearchListener {

    private Context mContext;
    private MapView mMapView;
    private AMap aMap;
    private TextView tvTemp2,tvRain2,tvHumidity2,tvVisibility2,tvPressure2,tvWindSpeed2,tvLayerName,tvProName;
    private TextView tvTitle,tvName,tvStationId,tvTemp,tvDistance,tvJiangshui,tvShidu,tvWind,tvVisible,tvPressrue;
    private List<StationMonitorDto> stationList = new ArrayList<>();
    private SimpleDateFormat sdf1 = new SimpleDateFormat("HH时", Locale.CHINA);
    private long pastTime = 60 * 60 * 1000;
    private RelativeLayout reContent,reShare;
    private float zoom = 3.5f;
    private Marker clickMarker;
    private String layerName = "全国",stationName,stationId;
    private List<Marker> markerList = new ArrayList<>();//10个站点的marker
    private LatLng locationLatLng = new LatLng(39.904030, 116.407526);
    private GeocodeSearch geocoderSearch;
    private List<Text> texts = new ArrayList<>();//等值线
    private ImageView ivLegend,ivProName,ivGuide,ivCursor,ivTyphoonRadar,ivTyphoonCloud;
    private int value = 1;//默认为降水
    private LinearLayout llRain,llTemp,llCursor;
    private TextView tv1, tv2, tv3, tv4, tv5, tv21, tv22, tv23, tv24;
    private final String LOADTYPE1 = "default",LOADTYPE2 = "move",LOADTYPE3 = "click";//默认加载图层数据方式,地图缩放、移动等,天气6要素按钮点击切换
    private LatLng leftlatlng = new LatLng(-16.305714763804854,75.13831436634065);
    private LatLng rightLatlng = new LatLng(63.681687310440864,135.21788656711578);
    private final int typeRain1 = 1,typeRain3 = 13,typeRain6 = 16,typeRain12 = 112,typeRain24 = 124,typeTemp1 = 21,typeTemp24Max = 22,
            typeTemp24Min = 23,typeTemp24Change = 24,typeHumidity = 3,typeVisible = 4,typePressure = 5,typeWind = 6;
    private List<Polyline> boundLines = new ArrayList<>();//省份边界
    private GroundOverlay layerOverlay;
    private MyBroadCastReceiver mReceiver;

    //云图雷达图
    private boolean isRadarOn = false,isCloudOn = false;
    private CaiyunManager mRadarManager;
    private List<MinuteFallDto> radarList = new ArrayList<>();
    private RadarThread mRadarThread;
    private static final int HANDLER_SHOW_RADAR = 1;
    private static final int HANDLER_LOAD_FINISHED = 3;
    private GroundOverlay radarOverlay,cloudOverlay;
    private Bitmap cloudBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_fact);
        mContext = this;
        initBroadCast();
        initMap(savedInstanceState);
        initWidget();
    }
    
    private void initBroadCast() {
        mReceiver = new MyBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FactManager.BROAD_RAIN1_COMPLETE);
        intentFilter.addAction(FactManager.BROAD_RAIN1_LEGEND_COMPLETE);
        registerReceiver(mReceiver, intentFilter);
    }
    
    private class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), FactManager.BROAD_RAIN1_COMPLETE)) {
                drawNationDataToMap(value, LOADTYPE1);
            }else if (TextUtils.equals(intent.getAction(), FactManager.BROAD_RAIN1_LEGEND_COMPLETE)) {
                if (!TextUtils.isEmpty(FactManager.precipitation1hLegend)) {
                    Picasso.get().load(FactManager.precipitation1hLegend).into(ivLegend);
                }
            }
        }
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
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                FactManager factManager = new FactManager(mContext);
            }
        });
    }

    @Override
    public void onCameraChange(CameraPosition arg0) {
        closeDetailWindow();
    }

    @Override
    public void onCameraChangeFinish(CameraPosition arg0) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Point leftPoint = new Point(0, dm.heightPixels);
        Point rightPoint = new Point(dm.widthPixels, 0);
        leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
        rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);
        zoom = arg0.zoom;

        moveCursor();

        //绘制图层
        delayedHandler.removeMessages(1001);
        Message msg = delayedHandler.obtainMessage();
        msg.what = 1001;
        delayedHandler.sendMessageDelayed(msg, 1500);
    }

    @SuppressLint("HandlerLeak")
    private Handler delayedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1000://标尺
                    if (llCursor != null) {
                        llCursor.setVisibility(View.GONE);
                    }
                    break;
                case 1001://地图缩放延时时间
                    if (tvProName.getVisibility() == View.VISIBLE) {
                        drawProvinceDataToMap(value, LOADTYPE2);
                    } else {
                        drawNationDataToMap(value, LOADTYPE2);
                    }
                    break;
            }
        }
    };

    /**
     * 绘制全国区域
     * @param value 判断降水、气温、气压等类型
     */
    private void drawNationDataToMap(int value, String loadType) {
        String imgUrl = "";
        String result = "";
        String type = "";
        switch (value) {
            case typeRain1:
                imgUrl = FactManager.precipitation1hImg;
                result = FactManager.precipitation1hResult;
                type = getString(R.string.layer_rain1);
                break;
            case typeRain3:
                imgUrl = FactManager.precipitation3hImg;
                result = FactManager.precipitation3hResult;
                type = getString(R.string.layer_rain3);
                break;
            case typeRain6:
                imgUrl = FactManager.precipitation6hImg;
                result = FactManager.precipitation6hResult;
                type = getString(R.string.layer_rain6);
                break;
            case typeRain12:
                imgUrl = FactManager.precipitation12hImg;
                result = FactManager.precipitation12hResult;
                type = getString(R.string.layer_rain12);
                break;
            case typeRain24:
                imgUrl = FactManager.precipitation24hImg;
                result = FactManager.precipitation24hResult;
                type = getString(R.string.layer_rain24);
                break;
            case typeTemp1:
                imgUrl = FactManager.balltempImg;
                result = FactManager.balltempResult;
                type = getString(R.string.layer_temp21);
                break;
            case typeTemp24Max:
                imgUrl = FactManager.balltempMaxImg;
                result = FactManager.balltempMaxResult;
                type = getString(R.string.layer_temp22);
                break;
            case typeTemp24Min:
                imgUrl = FactManager.balltempMinImg;
                result = FactManager.balltempMinResult;
                type = getString(R.string.layer_temp23);
                break;
            case typeTemp24Change:
                imgUrl = FactManager.balltempChangeImg;
                result = FactManager.balltempChangeResult;
                type = getString(R.string.layer_temp24);
                break;
            case typeHumidity:
                imgUrl = FactManager.humidityImg;
                result = FactManager.humidityResult;
                type = getString(R.string.layer_humidity);
                break;
            case typeVisible:
                imgUrl = FactManager.visibilityImg;
                result = FactManager.visibilityResult;
                type = getString(R.string.layer_visible);
                break;
            case typePressure:
                imgUrl = FactManager.airpressureImg;
                result = FactManager.airpressureResult;
                type = getString(R.string.layer_pressure);
                break;
            case typeWind:
                imgUrl = FactManager.windspeedImg;
                result = FactManager.windspeedResult;
                type = getString(R.string.layer_wind);
                break;
        }

        if (TextUtils.equals(loadType, LOADTYPE1)) {//默认只加载图层数据、图层名称
            drawWeatherLayerName(result, type);
            drawNewWeatherLayer(imgUrl);
        } else if (TextUtils.equals(loadType, LOADTYPE2)) {//监听地图缩放、移动等
            if (zoom < 10.0f) {//保持图层一直在
                removeStationMarkers();
                drawNewWeatherLayer(imgUrl);
                if (zoom < 8.0f) {
                    removeValueLine();
                } else if (zoom >= 8.0f && zoom < 10.0f) {//绘制等值线
                    if (texts.size() == 0) {
                        drawValueLine(result);
                    }
                }
            } else {//清除图层、等值线，绘制站点
                removeNewWeatherLayer();
                removeValueLine();

                queryNationViewStationInfo(stationList, loadType);//绘制站点
            }
        } else if (TextUtils.equals(loadType, LOADTYPE3)) {//监听天气6要素切换点击事件
            removeNewWeatherLayer();
            removeValueLine();
            removeStationMarkers();

            drawWeatherLayerName(result, type);

            if (zoom < 10.0f) {//绘制新选择的图层数据
                drawNewWeatherLayer(imgUrl);
                if (zoom < 8.0f) {
                    removeValueLine();
                } else if (zoom >= 8.0f && zoom < 10.0f) {//绘制等值线
                    if (texts.size() == 0) {
                        drawValueLine(result);
                    }
                }
            } else {
                queryNationViewStationInfo(stationList, loadType);//绘制站点
            }
        }

    }

    /**
     * 绘制某个省份区域
     *
     * @param value 判断降水、气温、气压等类型
     */
    private void drawProvinceDataToMap(int value, String loadType) {
        String result = "";
        String type = "";
        switch (value) {
            case typeRain1:
                result = FactManager.precipitation1hResult;
                type = getString(R.string.layer_rain1);
                break;
            case typeRain3:
                result = FactManager.precipitation3hResult;
                type = getString(R.string.layer_rain3);
                break;
            case typeRain6:
                result = FactManager.precipitation6hResult;
                type = getString(R.string.layer_rain6);
                break;
            case typeRain12:
                result = FactManager.precipitation12hResult;
                type = getString(R.string.layer_rain12);
                break;
            case typeRain24:
                result = FactManager.precipitation24hResult;
                type = getString(R.string.layer_rain24);
                break;
            case typeTemp1:
                result = FactManager.balltempResult;
                type = getString(R.string.layer_temp21);
                break;
            case typeTemp24Max:
                result = FactManager.balltempMaxResult;
                type = getString(R.string.layer_temp22);
                break;
            case typeTemp24Min:
                result = FactManager.balltempMinResult;
                type = getString(R.string.layer_temp23);
                break;
            case typeTemp24Change:
                result = FactManager.balltempChangeResult;
                type = getString(R.string.layer_temp24);
                break;
            case typeHumidity:
                result = FactManager.humidityResult;
                type = getString(R.string.layer_humidity);
                break;
            case typeVisible:
                result = FactManager.visibilityResult;
                type = getString(R.string.layer_visible);
                break;
            case typePressure:
                result = FactManager.airpressureResult;
                type = getString(R.string.layer_pressure);
                break;
            case typeWind:
                result = FactManager.windspeedResult;
                type = getString(R.string.layer_wind);
                break;
        }

        if (TextUtils.equals(loadType, LOADTYPE1)) {//默认只加载图层数据、图层名称

        } else if (TextUtils.equals(loadType, LOADTYPE2)) {//监听地图缩放、移动等
            removeStationMarkers();

            queryProStationInfo(stationList, tvProName.getText().toString(), loadType);//绘制省份站点
        } else if (TextUtils.equals(loadType, LOADTYPE3)) {//监听天气6要素切换点击事件
            removeNewWeatherLayer();
            removeValueLine();
            removeStationMarkers();

            drawWeatherLayerName(result, type);
            queryProStationInfo(stationList, tvProName.getText().toString(), loadType);//绘制省份站点
        }

    }

    /**
     * 绘制图层名称
     * @param result
     * @param type
     */
    private void drawWeatherLayerName(final String result, final String type) {
        if (TextUtils.isEmpty(result) || TextUtils.isEmpty(type)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (!obj.isNull("t")) {
                        long time1 = obj.getLong("t") - pastTime;
                        long time2 = obj.getLong("t");
                        String time = "(" + sdf1.format(time1) + "-" + sdf1.format(time2) + ")";
                        final String name = type + time;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String unit = "";
                                if (name.contains("降水")) {
                                    unit = "[单位:mm]";
                                } else if (name.contains("气温")) {
                                    unit = "[单位:℃]";
                                } else if (name.contains("湿度")) {
                                    unit = "[单位:%]";
                                } else if (name.contains("风速")) {
                                    unit = "[单位:级]";
                                } else if (name.contains("能见度")) {
                                    unit = "[单位:km]";
                                } else if (name.contains("气压")) {
                                    unit = "[单位:hPa]";
                                }
                                tvLayerName.setText(layerName + name + unit);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 绘制新的图层
     */
    private void drawNewWeatherLayer(final String imgUrl) {
        if (TextUtils.isEmpty(imgUrl)) {
            return;
        }
        Picasso.get().load(imgUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(new LatLng(3.83703, 73.502355))
                        .include(new LatLng(53.563624, 135.09567))
                        .build();
                if (layerOverlay == null) {
                    layerOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
                            .anchor(0.5f, 0.5f)
                            .positionFromBounds(bounds)
                            .image(fromView)
                            .zIndex(1001)
                            .transparency(0.25f));
                } else {
                    layerOverlay.setImage(null);
                    layerOverlay.setPositionFromBounds(bounds);
                    layerOverlay.setImage(fromView);
                }
                aMap.runOnDrawFrame();
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }

    private void removeNewWeatherLayer() {
        if (layerOverlay != null) {
            layerOverlay.remove();
            layerOverlay = null;
        }
    }

    /**
     * 绘制等值线
     * @param result
     */
    private void drawValueLine(final String result) {
        if (TextUtils.isEmpty(result)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject obj = new JSONObject(result);
                    JSONArray array = obj.getJSONArray("l");
                    int length = array.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject itemObj = array.getJSONObject(i);

                        double centerLat = 0;
                        double centerLng = 0;
                        if (!itemObj.isNull("p")) {
                            String p = itemObj.getString("p");
                            if (!TextUtils.isEmpty(p)) {
                                String[] points = p.split(";");
                                String[] latLng = points[points.length/2].split(",");
                                centerLat = Double.valueOf(latLng[1]);
                                centerLng = Double.valueOf(latLng[0]);
                            }
                        }
                        if (!itemObj.isNull("v")) {
                            double v = itemObj.getDouble("v");
                            TextOptions options = new TextOptions();
                            options.position(new LatLng(centerLat, centerLng));
                            options.fontColor(Color.BLACK);
                            options.fontSize(20);
                            options.text(v + "");
                            options.backgroundColor(Color.TRANSPARENT);
                            Text text = aMap.addText(options);
                            texts.add(text);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 清除等值线
     */
    private void removeValueLine() {
        for (int i = 0; i < texts.size(); i++) {
            texts.get(i).remove();
        }
        texts.clear();
    }

    /**
     * 清除省份边界
     */
    private void removeProvinceBounds() {
        for (int i = 0; i < boundLines.size(); i++) {
            boundLines.get(i).remove();
        }
        boundLines.clear();
    }

    /**
     * 绘制省份边界
     */
    private void drawProvinceBounds(String keywords) {
        removeProvinceBounds();
        if (TextUtils.isEmpty(keywords)) {
            return;
        }
        DistrictSearch search = new DistrictSearch(mContext);
        DistrictSearchQuery query = new DistrictSearchQuery();
        query.setKeywords(keywords);//传入关键字
        query.setShowBoundary(true);//是否返回边界值
        search.setQuery(query);
        search.setOnDistrictSearchListener(this);//绑定监听器
        search.searchDistrictAsyn();//开始搜索
    }

    @Override
    public void onDistrictSearched(DistrictResult districtResult) {
        if (districtResult == null || districtResult.getDistrict() == null) {
            return;
        }

        final DistrictItem item = districtResult.getDistrict().get(0);
        if (item == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] polyStr = item.districtBoundary();
                if (polyStr == null || polyStr.length == 0) {
                    return;
                }

                for (String str : polyStr) {
                    String[] lat = str.split(";");
                    PolylineOptions polylineOptions = new PolylineOptions();
                    for (String latstr : lat) {
                        String[] lats = latstr.split(",");
                        double latitude = Double.parseDouble(lats[1]);
                        double longitude = Double.parseDouble(lats[0]);
                        polylineOptions.add(new LatLng(latitude, longitude));
                    }
                    polylineOptions.width(8).color(0xff8698ae);
                    Polyline polyline = aMap.addPolyline(polylineOptions);
                    boundLines.add(polyline);
                }
            }
        }).start();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = findViewById(R.id.tvTitle);
        tvName = findViewById(R.id.tvName);
        tvStationId = findViewById(R.id.tvStationId);
        tvTemp = findViewById(R.id.tvTemp);
        tvDistance = findViewById(R.id.tvDistance);
        tvJiangshui = findViewById(R.id.tvJiangshui);
        tvWind = findViewById(R.id.tvWind);
        tvShidu = findViewById(R.id.tvShidu);
        tvVisible = findViewById(R.id.tvVisible);
        tvPressrue = findViewById(R.id.tvPressrue);
        TextView tvCheckStation = findViewById(R.id.tvCheckStation);
        tvCheckStation.setOnClickListener(this);
        reContent = findViewById(R.id.reContent);
        reContent.setOnClickListener(this);
        ImageView ivDelete = findViewById(R.id.ivDelete);
        ivDelete.setOnClickListener(this);
        ImageView ivLocation = findViewById(R.id.ivLocation);
        ivLocation.setOnClickListener(this);
        tvLayerName = findViewById(R.id.tvLayerName);
        ivLegend = findViewById(R.id.ivLegend);
        ImageView ivLegendPrompt = findViewById(R.id.ivLegendPrompt);
        ivLegendPrompt.setOnClickListener(this);
        llRain = findViewById(R.id.llRain);
        llTemp = findViewById(R.id.llTemp);
        reShare = findViewById(R.id.reShare);
        ImageView ivShare = findViewById(R.id.ivShare);
        ivShare.setOnClickListener(this);
        ivShare.setVisibility(View.VISIBLE);
        tv1 = findViewById(R.id.tv1);
        tv1.setOnClickListener(this);
        tv2 = findViewById(R.id.tv2);
        tv2.setOnClickListener(this);
        tv3 = findViewById(R.id.tv3);
        tv3.setOnClickListener(this);
        tv4 = findViewById(R.id.tv4);
        tv4.setOnClickListener(this);
        tv5 = findViewById(R.id.tv5);
        tv5.setOnClickListener(this);
        tv21 = findViewById(R.id.tv21);
        tv21.setOnClickListener(this);
        tv22 = findViewById(R.id.tv22);
        tv22.setOnClickListener(this);
        tv23 = findViewById(R.id.tv23);
        tv23.setOnClickListener(this);
        tv24 = findViewById(R.id.tv24);
        tv24.setOnClickListener(this);
        ImageView ivRank = findViewById(R.id.ivRank);
        ivRank.setOnClickListener(this);
        tvProName = findViewById(R.id.tvProName);
        tvProName.setOnClickListener(this);
        ivProName = findViewById(R.id.ivProName);
        ivProName.setOnClickListener(this);
        llCursor = findViewById(R.id.llCursor);
        ivCursor = findViewById(R.id.ivCursor);
        ImageView ivAdd = findViewById(R.id.ivAdd);
        ivAdd.setOnClickListener(this);
        ImageView ivMinuse = findViewById(R.id.ivMinuse);
        ivMinuse.setOnClickListener(this);
        ivGuide = findViewById(R.id.ivGuide);
        ivGuide.setOnClickListener(this);
        tvTemp2 = findViewById(R.id.tvTemp2);
        tvTemp2.setOnClickListener(this);
        tvRain2 = findViewById(R.id.tvRain2);
        tvRain2.setOnClickListener(this);
        tvHumidity2 = findViewById(R.id.tvHumidity2);
        tvHumidity2.setOnClickListener(this);
        tvVisibility2 = findViewById(R.id.tvVisibility2);
        tvVisibility2.setOnClickListener(this);
        tvPressure2 = findViewById(R.id.tvPressure2);
        tvPressure2.setOnClickListener(this);
        tvWindSpeed2 = findViewById(R.id.tvWindSpeed2);
        tvWindSpeed2.setOnClickListener(this);
        ImageView ivArea = findViewById(R.id.ivArea);
        ivArea.setOnClickListener(this);
        ivTyphoonRadar = findViewById(R.id.ivTyphoonRadar);
        ivTyphoonRadar.setOnClickListener(this);
        ivTyphoonCloud = findViewById(R.id.ivTyphoonCloud);
        ivTyphoonCloud.setOnClickListener(this);

        mRadarManager = new CaiyunManager(mContext);
        CommonUtil.showGuidePage(mContext, this.getClass().getName(), ivGuide);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams((int)CommonUtil.dip2px(mContext, 70), LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.leftMargin = width/6/2-(int)CommonUtil.dip2px(mContext, 25);
        llRain.setLayoutParams(params1);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)CommonUtil.dip2px(mContext, 90), LinearLayout.LayoutParams.WRAP_CONTENT);
        params2.leftMargin = width/6+width/6/2-(int)CommonUtil.dip2px(mContext, 40);
        llTemp.setLayoutParams(params2);

        String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
        if (title != null) {
            tvTitle.setText(title);
        }

        llCursor.removeAllViews();
        StationCursorView cursorView = new StationCursorView(mContext);
        llCursor.addView(cursorView, (int) CommonUtil.dip2px(mContext, 30), (int) CommonUtil.dip2px(mContext, 160));

        geocoderSearch = new GeocodeSearch(mContext);
        geocoderSearch.setOnGeocodeSearchListener(this);

        if (CommonUtil.isLocationOpen(mContext)) {
            startLocation();
        }else {
            addLocationMarker(locationLatLng);
        }
    }

    int scrollY;
    /**
     * 设置游标
     */
    private void moveCursor() {
        int top = (int)CommonUtil.dip2px(mContext, 20);
        int add = (int)CommonUtil.dip2px(mContext, 5);
        if (zoom < 6.0f) {
            setScrollBarPosition(scrollY, top*1+add);
            scrollY = top*1+add;
        } else if (zoom >= 6.0 && zoom < 8.0f)  {
            setScrollBarPosition(scrollY, top*2+add);
            scrollY = top*2+add;
        } else if (zoom >= 8.0 && zoom < 10.0f)  {
            setScrollBarPosition(scrollY, top*3+add);
            scrollY = top*3+add;
        } else  {
            setScrollBarPosition(scrollY, top*4+add);
            scrollY = top*4+add;
        }
        aMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    /**
     * 设置游标位置动画
     */
    private void setScrollBarPosition(float fromYDelta, float toYDelta) {
        TranslateAnimation anim = new TranslateAnimation(0,0,fromYDelta,toYDelta);
        anim.setDuration(500);
        anim.setFillAfter(true);
        ivCursor.startAnimation(anim);
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
            addLocationMarker(locationLatLng);
        }
    }

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
        Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.icon_map_location),
                (int)(CommonUtil.dip2px(mContext, 21)), (int)(CommonUtil.dip2px(mContext, 32)));
        if (bitmap != null) {
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_location));
        }
        if (clickMarker != null) {
            clickMarker.remove();
        }
        clickMarker = aMap.addMarker(options);
        clickMarker.setClickable(false);
    }

    /**
     * 查询全国符合可视化区域站点信息
     */
    private void queryNationViewStationInfo(List<StationMonitorDto> list, String loadType) {
        if (leftlatlng == null || rightLatlng == null) {
            return;
        }
        DBManager dbManager = new DBManager(mContext);
        dbManager.openDateBase();
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
        try {
            if (database != null && database.isOpen()) {
                Cursor cursor = null;
                if (zoom >= 10.0f && zoom < 12.0f) {
                    cursor = database.rawQuery(
                            "select * from " + DBManager.TABLE_NAME1
                                    + " where cast(SID as decimal) < 100000"
                                    + " and lat > " + leftlatlng.latitude + " and lat < " + rightLatlng.latitude
                                    + " and lon > " + leftlatlng.longitude + " and lon < " + rightLatlng.longitude
                            , null);
                } else if (zoom >= 12.0f) {
                    cursor = database.rawQuery(
                            "select * from " + DBManager.TABLE_NAME1
                                    + " where lat > " + leftlatlng.latitude + " and lat < " + rightLatlng.latitude
                                    + " and lon > " + leftlatlng.longitude + " and lon < " + rightLatlng.longitude
                            , null);
                }
                list.clear();
                String ids = "";
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    StationMonitorDto dto = new StationMonitorDto();
                    dto.stationId = cursor.getString(cursor.getColumnIndex("SID"));
                    dto.lat = cursor.getDouble(cursor.getColumnIndex("LAT"));
                    dto.lng = cursor.getDouble(cursor.getColumnIndex("LON"));

                    String pro = cursor.getString(cursor.getColumnIndex("PRO"));
                    String city = cursor.getString(cursor.getColumnIndex("CITY"));
                    String dis = cursor.getString(cursor.getColumnIndex("DIST"));
                    String addr = cursor.getString(cursor.getColumnIndex("ADDR"));
                    dto.provinceName = pro;
                    dto.cityName = city;
                    dto.districtName = dis;
                    dto.addr = addr;

                    ids += dto.stationId + ",";

                    if (addr.contains(pro) && addr.contains(city) && addr.contains(dis)) {
                        dto.name = addr;
                    } else if (addr.contains(city) && addr.contains(dis)) {
                        dto.name = pro + addr;
                    } else if (addr.contains(dis)) {
                        dto.name = pro + city + addr;
                    } else {
                        if (pro.contains(city) || dis.contains(city)) {
                            dto.name = pro + dis + addr;
                        } else {
                            dto.name = pro + city + dis + addr;
                        }
                    }

                    list.add(dto);
                }
                cursor.close();
                dbManager.closeDatabase();

                if (!TextUtils.isEmpty(ids)) {
                    OkHttpStationsInfo(ids.substring(0, ids.length() - 1), loadType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 查询某个省份所有站点信息
     *
     * @param list
     * @param provinceName
     */
    private void queryProStationInfo(List<StationMonitorDto> list, String provinceName, String loadType) {
        if (TextUtils.isEmpty(provinceName)) {
            return;
        }
        DBManager dbManager = new DBManager(mContext);
        dbManager.openDateBase();
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
        try {
            if (database != null && database.isOpen()) {
                Cursor cursor = null;
                if (zoom <= 10.0f) {
                    cursor = database.rawQuery(
                            "select * from " + DBManager.TABLE_NAME1
                                    + " where CAST(SID as decimal) < 100000 and PRO like " + "\"%" + provinceName + "%\""
                            , null);
                } else {
                    cursor = database.rawQuery(
                            "select * from " + DBManager.TABLE_NAME1
                                    + " where PRO like " + "\"%" + provinceName + "%\""
                            , null);
                }
                list.clear();
                String ids = "";
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    StationMonitorDto dto = new StationMonitorDto();
                    dto.stationId = cursor.getString(cursor.getColumnIndex("SID"));
                    dto.lat = cursor.getDouble(cursor.getColumnIndex("LAT"));
                    dto.lng = cursor.getDouble(cursor.getColumnIndex("LON"));

                    String pro = cursor.getString(cursor.getColumnIndex("PRO"));
                    String city = cursor.getString(cursor.getColumnIndex("CITY"));
                    String dis = cursor.getString(cursor.getColumnIndex("DIST"));
                    String addr = cursor.getString(cursor.getColumnIndex("ADDR"));
                    dto.provinceName = pro;
                    dto.cityName = city;
                    dto.districtName = dis;
                    dto.addr = addr;

                    ids += dto.stationId + ",";

                    if (addr.contains(pro) && addr.contains(city) && addr.contains(dis)) {
                        dto.name = addr;
                    } else if (addr.contains(city) && addr.contains(dis)) {
                        dto.name = pro + addr;
                    } else if (addr.contains(dis)) {
                        dto.name = pro + city + addr;
                    } else {
                        if (pro.contains(city) || dis.contains(city)) {
                            dto.name = pro + dis + addr;
                        } else {
                            dto.name = pro + city + dis + addr;
                        }
                    }

                    list.add(dto);
                }
                cursor.close();
                dbManager.closeDatabase();

                if (!TextUtils.isEmpty(ids)) {
                    OkHttpStationsInfo(ids.substring(0, ids.length() - 1), loadType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取站点信息
     * @param stationIds
     */
    private void OkHttpStationsInfo(String stationIds, final String loadType) {
        final String[] ids = stationIds.split(",");
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        formBodyBuilder.add("ids", stationIds);
        final RequestBody requestBody = formBodyBuilder.build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.stationsInfo(ids[0])).post(requestBody).build(), new Callback() {
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
                                JSONArray array = new JSONArray(result);
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    StationMonitorDto dto = stationList.get(i);

                                    if (!obj.isNull("datatime")) {
                                        dto.time = obj.getString("datatime");
                                    }

                                    float distance = AMapUtils.calculateLineDistance(locationLatLng, new LatLng(Double.valueOf(dto.lat), Double.valueOf(dto.lng)))/1000;
                                    float d = new BigDecimal(distance/1000).setScale(1, BigDecimal.ROUND_FLOOR).floatValue();
                                    dto.distance = d+"";

                                    if (!obj.isNull("airpressure")) {
                                        String value = obj.getString("airpressure");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0")) {
                                                    dto.airPressure = "0";
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.airPressure = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.airPressure = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.airPressure = CONST.noValue;
                                        }
                                    } else {
                                        dto.airPressure = CONST.noValue;
                                    }

                                    if (!obj.isNull("balltemp")) {
                                        String value = obj.getString("balltemp");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0")) {
                                                    dto.ballTemp = "0";
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.ballTemp = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.ballTemp = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.ballTemp = CONST.noValue;
                                        }
                                    } else {
                                        dto.ballTemp = CONST.noValue;
                                    }

                                    if (!obj.isNull("MaxWd")) {
                                        String value = obj.getString("MaxWd");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0")) {
                                                    dto.balltempMax = "0";
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.balltempMax = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.balltempMax = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.balltempMax = CONST.noValue;
                                        }
                                    } else {
                                        dto.balltempMax = CONST.noValue;
                                    }

                                    if (!obj.isNull("MinWd")) {
                                        String value = obj.getString("MinWd");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0")) {
                                                    dto.balltempMin = "0";
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.balltempMin = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.balltempMin = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.balltempMin = CONST.noValue;
                                        }
                                    } else {
                                        dto.balltempMin = CONST.noValue;
                                    }

                                    if (!obj.isNull("WDX")) {
                                        String value = obj.getString("WDX");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0")) {
                                                    dto.balltempChange = "0";
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.balltempChange = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.balltempChange = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.balltempChange = CONST.noValue;
                                        }
                                    } else {
                                        dto.balltempChange = CONST.noValue;
                                    }

                                    if (!obj.isNull("humidity")) {
                                        String value = obj.getString("humidity");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0")) {
                                                    dto.humidity = "0";
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.humidity = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.humidity = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.humidity = CONST.noValue;
                                        }
                                    } else {
                                        dto.humidity = CONST.noValue;
                                    }

                                    if (!obj.isNull("precipitation1h")) {
                                        String value = obj.getString("precipitation1h");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0") || value.equals("0.0") || value.equals("0")) {
                                                    dto.precipitation1h = CONST.noValue;
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.precipitation1h = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.precipitation1h = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.precipitation1h = CONST.noValue;
                                        }
                                    } else {
                                        dto.precipitation1h = CONST.noValue;
                                    }

                                    if (!obj.isNull("rainfall3")) {
                                        String value = obj.getString("rainfall3");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0") || value.equals("0.0") || value.equals("0")) {
                                                    dto.precipitation3h = CONST.noValue;
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.precipitation3h = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.precipitation3h = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.precipitation3h = CONST.noValue;
                                        }
                                    } else {
                                        dto.precipitation3h = CONST.noValue;
                                    }

                                    if (!obj.isNull("rainfall6")) {
                                        String value = obj.getString("rainfall6");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0") || value.equals("0.0") || value.equals("0")) {
                                                    dto.precipitation6h = CONST.noValue;
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.precipitation6h = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.precipitation6h = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.precipitation6h = CONST.noValue;
                                        }
                                    } else {
                                        dto.precipitation6h = CONST.noValue;
                                    }

                                    if (!obj.isNull("rainfall12")) {
                                        String value = obj.getString("rainfall12");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0") || value.equals("0.0") || value.equals("0")) {
                                                    dto.precipitation12h = CONST.noValue;
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.precipitation12h = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.precipitation12h = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.precipitation12h = CONST.noValue;
                                        }
                                    } else {
                                        dto.precipitation12h = CONST.noValue;
                                    }

                                    if (!obj.isNull("rainfall24")) {
                                        String value = obj.getString("rainfall24");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0") || value.equals("0.0") || value.equals("0")) {
                                                    dto.precipitation24h = CONST.noValue;
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.precipitation24h = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.precipitation24h = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.precipitation24h = CONST.noValue;
                                        }
                                    } else {
                                        dto.precipitation24h = CONST.noValue;
                                    }

                                    if (!obj.isNull("winddir")) {
                                        String dir = null;
                                        String value = obj.getString("winddir");
                                        dto.wdir = Float.parseFloat(value);
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0")) {
                                                    dir = "0";
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dir = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dir = value;
                                                    }
                                                }
                                            }

                                            float fx = Float.valueOf(dir);
                                            String wind_dir = null;
                                            if (fx == 0 || fx == 360) {
                                                wind_dir = "北";
                                            } else if (fx > 0 && fx < 90) {
                                                wind_dir = "东北";
                                            } else if (fx == 90) {
                                                wind_dir = "东";
                                            } else if (fx > 90 && fx < 180) {
                                                wind_dir = "东南";
                                            } else if (fx == 180) {
                                                wind_dir = "南";
                                            } else if (fx > 180 && fx < 270) {
                                                wind_dir = "西南";
                                            } else if (fx == 270) {
                                                wind_dir = "西";
                                            } else if (fx > 270) {
                                                wind_dir = "西北";
                                            }
                                            dto.windDir = wind_dir;
                                        } else {
                                            dto.windDir = CONST.noValue;
                                        }
                                    } else {
                                        dto.windDir = CONST.noValue;
                                    }

                                    if (!obj.isNull("windspeed")) {
                                        String value = obj.getString("windspeed");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0")) {
                                                    dto.windSpeed = "0";
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.windSpeed = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.windSpeed = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.windSpeed = CONST.noValue;
                                        }
                                    } else {
                                        dto.windSpeed = CONST.noValue;
                                    }

                                    if (!obj.isNull("pointtemp")) {
                                        String value = obj.getString("pointtemp");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0")) {
                                                    dto.pointTemp = "0";
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        dto.pointTemp = value.substring(0, value.indexOf("."));
                                                    } else {
                                                        dto.pointTemp = value;
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.pointTemp = CONST.noValue;
                                        }
                                    } else {
                                        dto.pointTemp = CONST.noValue;
                                    }

                                    if (!obj.isNull("visibility")) {
                                        String value = obj.getString("visibility");
                                        if (!TextUtils.isEmpty(value)) {
                                            if (value.length() >= 2 && value.contains(".")) {
                                                if (value.equals(".0")) {
                                                    dto.visibility = "0";
                                                } else {
                                                    if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                                                        float f = Float.valueOf(value) / 1000;
                                                        BigDecimal b = new BigDecimal(f);
                                                        float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                                                        dto.visibility = String.valueOf(f1).substring(0, String.valueOf(f1).indexOf("."));
                                                    } else {
                                                        float f = Float.valueOf(value) / 1000;
                                                        BigDecimal b = new BigDecimal(f);
                                                        float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                                                        dto.visibility = String.valueOf(f1);
                                                    }
                                                }
                                            }
                                        } else {
                                            dto.visibility = CONST.noValue;
                                        }
                                    } else {
                                        dto.visibility = CONST.noValue;
                                    }

                                }

                                addMarker(stationList, value, loadType);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 移除选择点的marker
     * 移除map上已经存在的站点marker
     */
    private void removeStationMarkers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < markerList.size(); i++) {
                    markerList.get(i).remove();
                }
                markerList.clear();
            }
        }).start();
    }

    /**
     * 添加marker
     *
     * @param list
     * @param value
     */
    private void addMarker(final List<StationMonitorDto> list, final int value, final String loadType) {
        removeStationMarkers();
        if (list.size() <= 0) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final LatLngBounds.Builder bounds = LatLngBounds.builder();
                for (StationMonitorDto dto : list) {
                    double lat = dto.lat;
                    double lng = dto.lng;
                    MarkerOptions options = new MarkerOptions();
                    options.title(dto.stationId);
                    options.snippet(dto.name);
                    options.anchor(0.5f, 0.5f);
                    options.position(new LatLng(lat, lng));
                    View markerView = getTextBitmap(value, dto);
                    if (markerView != null && lat != 0 && lng != 0) {
                        options.icon(BitmapDescriptorFactory.fromView(markerView));
                        Marker m = aMap.addMarker(options);
                        markerList.add(m);

                        bounds.include(new LatLng(lat, lng));
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (tvProName.getVisibility() == View.VISIBLE) {
                            if (TextUtils.equals(loadType, LOADTYPE3)) {
                                drawProvinceBounds(tvProName.getText().toString());
                                aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
                            }
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 给marker添加文字
     * @return
     */
    private View getTextBitmap(int value, StationMonitorDto dto) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shawn_fact_marker_info, null);
        if (value == typeWind) {
            view = inflater.inflate(R.layout.shawn_fact_marker_info_wind, null);
        }
        if (view == null) {
            return null;
        }
        ImageView ivMarker = view.findViewById(R.id.ivMarker);
        TextView tvValue = view.findViewById(R.id.tvValue);
        ImageView ivWind = view.findViewById(R.id.ivWind);

        String number = "";
        switch (value) {
            case typeRain1:
                number = dto.precipitation1h;
                break;
            case typeRain3:
                number = dto.precipitation3h;
                break;
            case typeRain6:
                number = dto.precipitation6h;
                break;
            case typeRain12:
                number = dto.precipitation12h;
                break;
            case typeRain24:
                number = dto.precipitation24h;
                break;
            case typeTemp1:
                number = dto.ballTemp;
                break;
            case typeTemp24Max:
                number = dto.balltempMax;
                break;
            case typeTemp24Min:
                number = dto.balltempMin;
                break;
            case typeTemp24Change:
                number = dto.balltempChange;
                break;
            case typeHumidity:
                number = dto.humidity;
                break;
            case typeVisible:
                number = dto.visibility;
                break;
            case typePressure:
                number = dto.airPressure;
                break;
            case typeWind:
                number = dto.windSpeed;
                break;
        }

        if (TextUtils.isEmpty(number) || TextUtils.equals(number, CONST.noValue)) {
            return null;
        }

        GradientDrawable gradientDrawable = (GradientDrawable) ivMarker.getBackground();
        gradientDrawable.setColor(FactManager.pointColor(value, number));
        tvValue.setText(number);

        if (value == typeTemp24Change) {//变温
            float changeTemp = Float.parseFloat(number);
            if (changeTemp >= 0) {
                ivWind.setImageResource(R.drawable.iv_high_temp_logo);
            }else {
                ivWind.setImageResource(R.drawable.iv_low_temp_logo);
            }
            ivWind.setVisibility(View.VISIBLE);
        } else if (value == typeWind) {//风向风速
            if (dto.wdir != -1) {
                float windSpeed = Float.parseFloat(number);
                Bitmap b = CommonUtil.getWindMarker(mContext, windSpeed);
                if (b != null) {
                    Matrix matrix = new Matrix();
                    matrix.postScale(1, 1);
                    matrix.postRotate(dto.wdir);
                    Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    if (bitmap != null) {
                        ivWind.setImageBitmap(bitmap);
                        ivWind.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        return view;
    }

    @Override
    public void onMapClick(LatLng arg0) {

    }

    /**
     * 通过经纬度获取地理位置信息
     *
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
                String pro = result.getRegeocodeAddress().getProvince();
                String city = result.getRegeocodeAddress().getCity();
                String dis = result.getRegeocodeAddress().getDistrict();
                String addr = result.getRegeocodeAddress().getStreetNumber().getStreet();
                if (pro.contains(city) || dis.contains(city)) {
                    tvName.setText(addr + getString(R.string.monitor_station));
                    stationName = tvName.getText().toString();
                } else {
                    tvName.setText(addr + getString(R.string.monitor_station));
                    stationName = tvName.getText().toString();
                }
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker == null) {
            return false;
        }

        if (!TextUtils.isEmpty(marker.getTitle())) {
            for (int i = 0; i < stationList.size(); i++) {
                StationMonitorDto data = stationList.get(i);
                if (TextUtils.equals(marker.getTitle(), data.stationId)) {
                    String snippet = marker.getSnippet();
                    if (snippet.contains(getString(R.string.not_available))) {//把“暂无”监测站更换为高德地图通过经纬度获取名称
                        searchAddrByLatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                    } else {
                        tvName.setText(snippet + getString(R.string.monitor_station));
                        stationName = tvName.getText().toString();
                    }
                    stationId = marker.getTitle();
                    tvStationId.setText(getString(R.string.station_id) + ": " + stationId);
                    tvDistance.setText(getString(R.string.distance_station) + data.distance + getString(R.string.unit_km));

                    if (TextUtils.equals(data.ballTemp, CONST.noValue)) {
                        tvTemp.setText("气温 "+CONST.noValue);
                    } else {
                        tvTemp.setText("气温 "+data.ballTemp+getString(R.string.unit_degree));
                    }

                    if (TextUtils.equals(data.precipitation1h, CONST.noValue)) {
                        tvJiangshui.setText("过去1h降水量 "+CONST.noValue);
                    } else {
                        tvJiangshui.setText("过去1h降水量 "+data.precipitation1h + getString(R.string.unit_mm));
                    }

                    if (TextUtils.equals(data.humidity, CONST.noValue)) {
                        tvShidu.setText("相对湿度 "+CONST.noValue);
                    } else {
                        tvShidu.setText("相对湿度 "+data.humidity + getString(R.string.unit_percent));
                    }

                    if (TextUtils.equals(data.windDir, CONST.noValue) && TextUtils.equals(data.windDir, CONST.noValue)) {
                        tvWind.setText("风速风向 "+CONST.noValue);
                    } else {
                        tvWind.setText("风速风向 "+data.windDir + " " + data.windSpeed + getString(R.string.unit_speed));
                    }

                    if (TextUtils.equals(data.visibility, CONST.noValue)) {
                        tvVisible.setText("能见度 "+CONST.noValue);
                    } else {
                        tvVisible.setText("能见度 "+data.visibility + getString(R.string.unit_km));
                    }

                    if (TextUtils.equals(data.airPressure, CONST.noValue)) {
                        tvPressrue.setText("气压 "+CONST.noValue);
                    } else {
                        tvPressrue.setText("气压 "+data.airPressure + getString(R.string.unit_hPa));
                    }
                    break;
                }
            }

            Animation animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 1f,
                    Animation.RELATIVE_TO_SELF, 0);
            animation.setDuration(300);
            if (reContent.getVisibility() == View.GONE) {
                reContent.setAnimation(animation);
                reContent.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    /**
     * 关闭详情窗口
     */
    private void closeDetailWindow() {
        Animation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1f);
        animation.setDuration(300);
        if (reContent.getVisibility() == View.VISIBLE) {
            reContent.setAnimation(animation);
            reContent.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapScreenShot(final Bitmap bitmap1) {
        Bitmap bitmap2 = CommonUtil.captureView(reShare);
        Bitmap bitmap3 = CommonUtil.mergeBitmap(FactActivity.this, bitmap1, bitmap2, true);
        CommonUtil.clearBitmap(bitmap1);
        CommonUtil.clearBitmap(bitmap2);
        Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.legend_share_portrait);
        Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
        CommonUtil.clearBitmap(bitmap3);
        CommonUtil.clearBitmap(bitmap4);
        CommonUtil.share(FactActivity.this, bitmap);
    }

    @Override
    public void onMapScreenShot(Bitmap arg0, int arg1) {
    }

    /**
     * 获取分钟级降水图
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
     * 清除雷达拼图，取消线程
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
     * 获取云图数据
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
     * 绘制卫星拼图
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
                    .transparency(0.25f));
        } else {
            cloudOverlay.setImage(null);
            cloudOverlay.setPositionFromBounds(bounds);
            cloudOverlay.setImage(fromView);
        }
    }

    /**
     * 清除云图
     */
    private void removeCloud() {
        if (cloudOverlay != null) {
            cloudOverlay.remove();
            cloudOverlay = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.ivShare:
                aMap.getMapScreenShot(FactActivity.this);
                break;
            case R.id.tv1:
                if (llRain.getVisibility() == View.INVISIBLE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.INVISIBLE);
                }
                tv1.setTextColor(0xff2d5a9d);
                tv2.setTextColor(getResources().getColor(R.color.text_color4));
                tv3.setTextColor(getResources().getColor(R.color.text_color4));
                tv4.setTextColor(getResources().getColor(R.color.text_color4));
                tv5.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setTextColor(0xff2d5a9d);
                tvRain2.setBackgroundResource(R.drawable.bg_layer_button);
                tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
                tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.precipitation1hLegend)) {
                    Picasso.get().load(FactManager.precipitation1hLegend).into(ivLegend);
                }
                pastTime = 60 * 60 * 1000;
                value = 1;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tv2:
                if (llRain.getVisibility() == View.INVISIBLE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.INVISIBLE);
                }

                tv1.setTextColor(getResources().getColor(R.color.text_color4));
                tv2.setTextColor(0xff2d5a9d);
                tv3.setTextColor(getResources().getColor(R.color.text_color4));
                tv4.setTextColor(getResources().getColor(R.color.text_color4));
                tv5.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setTextColor(0xff2d5a9d);
                tvRain2.setBackgroundResource(R.drawable.bg_layer_button);
                tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
                tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.precipitation3hLegend)) {
                    Picasso.get().load(FactManager.precipitation3hLegend).into(ivLegend);
                }
                pastTime = 3 * 60 * 60 * 1000;
                value = 13;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tv3:
                if (llRain.getVisibility() == View.INVISIBLE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.INVISIBLE);
                }

                tv1.setTextColor(getResources().getColor(R.color.text_color4));
                tv2.setTextColor(getResources().getColor(R.color.text_color4));
                tv3.setTextColor(0xff2d5a9d);
                tv4.setTextColor(getResources().getColor(R.color.text_color4));
                tv5.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setTextColor(0xff2d5a9d);
                tvRain2.setBackgroundResource(R.drawable.bg_layer_button);
                tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
                tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.precipitation6hLegend)) {
                    Picasso.get().load(FactManager.precipitation6hLegend).into(ivLegend);
                }
                pastTime = 6 * 60 * 60 * 1000;
                value = 16;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tv4:
                if (llRain.getVisibility() == View.INVISIBLE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.INVISIBLE);
                }

                tv1.setTextColor(getResources().getColor(R.color.text_color4));
                tv2.setTextColor(getResources().getColor(R.color.text_color4));
                tv3.setTextColor(getResources().getColor(R.color.text_color4));
                tv4.setTextColor(0xff2d5a9d);
                tv5.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setTextColor(0xff2d5a9d);
                tvRain2.setBackgroundResource(R.drawable.bg_layer_button);
                tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
                tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.precipitation12hLegend)) {
                    Picasso.get().load(FactManager.precipitation12hLegend).into(ivLegend);
                }
                pastTime = 12 * 60 * 60 * 1000;
                value = 112;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tv5:
                if (llRain.getVisibility() == View.INVISIBLE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.INVISIBLE);
                }

                tv1.setTextColor(getResources().getColor(R.color.text_color4));
                tv2.setTextColor(getResources().getColor(R.color.text_color4));
                tv3.setTextColor(getResources().getColor(R.color.text_color4));
                tv4.setTextColor(getResources().getColor(R.color.text_color4));
                tv5.setTextColor(0xff2d5a9d);
                tvRain2.setTextColor(0xff2d5a9d);
                tvRain2.setBackgroundResource(R.drawable.bg_layer_button);
                tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
                tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.precipitation24hLegend)) {
                    Picasso.get().load(FactManager.precipitation24hLegend).into(ivLegend);
                }
                pastTime = 24 * 60 * 60 * 1000;
                value = 124;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tvRain2:
                if (llRain.getVisibility() == View.INVISIBLE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.INVISIBLE);
                }
                llTemp.setVisibility(View.INVISIBLE);

                tvRain2.setTextColor(0xff2d5a9d);
                tvRain2.setBackgroundResource(R.drawable.bg_layer_button);
                tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
                tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));
                break;
            case R.id.tv21:
                if (llTemp.getVisibility() == View.INVISIBLE) {
                    llTemp.setVisibility(View.VISIBLE);
                } else {
                    llTemp.setVisibility(View.INVISIBLE);
                }

                tv21.setTextColor(0xff2d5a9d);
                tv22.setTextColor(getResources().getColor(R.color.text_color4));
                tv23.setTextColor(getResources().getColor(R.color.text_color4));
                tv24.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvTemp2.setTextColor(0xff2d5a9d);
                tvTemp2.setBackgroundResource(R.drawable.bg_layer_button);
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.balltempLegend)) {
                    Picasso.get().load(FactManager.balltempLegend).into(ivLegend);
                }
                pastTime = 60 * 60 * 1000;
                value = 21;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tv22:
                if (llTemp.getVisibility() == View.INVISIBLE) {
                    llTemp.setVisibility(View.VISIBLE);
                } else {
                    llTemp.setVisibility(View.INVISIBLE);
                }

                tv21.setTextColor(getResources().getColor(R.color.text_color4));
                tv22.setTextColor(0xff2d5a9d);
                tv23.setTextColor(getResources().getColor(R.color.text_color4));
                tv24.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvTemp2.setTextColor(0xff2d5a9d);
                tvTemp2.setBackgroundResource(R.drawable.bg_layer_button);
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.balltempMaxLegend)) {
                    Picasso.get().load(FactManager.balltempMaxLegend).into(ivLegend);
                }
                pastTime = 24 * 60 * 60 * 1000;
                value = 22;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tv23:
                if (llTemp.getVisibility() == View.INVISIBLE) {
                    llTemp.setVisibility(View.VISIBLE);
                } else {
                    llTemp.setVisibility(View.INVISIBLE);
                }

                tv21.setTextColor(getResources().getColor(R.color.text_color4));
                tv22.setTextColor(getResources().getColor(R.color.text_color4));
                tv23.setTextColor(0xff2d5a9d);
                tv24.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvTemp2.setTextColor(0xff2d5a9d);
                tvTemp2.setBackgroundResource(R.drawable.bg_layer_button);
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.balltempMinLegend)) {
                    Picasso.get().load(FactManager.balltempMinLegend).into(ivLegend);
                }
                pastTime = 24 * 60 * 60 * 1000;
                value = 23;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tv24:
                if (llTemp.getVisibility() == View.INVISIBLE) {
                    llTemp.setVisibility(View.VISIBLE);
                } else {
                    llTemp.setVisibility(View.INVISIBLE);
                }

                tv21.setTextColor(getResources().getColor(R.color.text_color4));
                tv22.setTextColor(getResources().getColor(R.color.text_color4));
                tv23.setTextColor(getResources().getColor(R.color.text_color4));
                tv24.setTextColor(0xff2d5a9d);
                tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvTemp2.setTextColor(0xff2d5a9d);
                tvTemp2.setBackgroundResource(R.drawable.bg_layer_button);
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.balltempChangeLegend)) {
                    Picasso.get().load(FactManager.balltempChangeLegend).into(ivLegend);
                }
                pastTime = 24 * 60 * 60 * 1000;
                value = 24;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tvTemp2:
                if (llTemp.getVisibility() == View.INVISIBLE) {
                    llTemp.setVisibility(View.VISIBLE);
                } else {
                    llTemp.setVisibility(View.INVISIBLE);
                }
                llRain.setVisibility(View.INVISIBLE);

                tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvTemp2.setTextColor(0xff2d5a9d);
                tvTemp2.setBackgroundResource(R.drawable.bg_layer_button);
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));
                break;
            case R.id.tvHumidity2:
                llRain.setVisibility(View.INVISIBLE);
                llTemp.setVisibility(View.INVISIBLE);

                tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
                tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvHumidity2.setTextColor(0xff2d5a9d);
                tvHumidity2.setBackgroundResource(R.drawable.bg_layer_button);
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.humidityLegend)) {
                    Picasso.get().load(FactManager.humidityLegend).into(ivLegend);
                }
                pastTime = 60 * 60 * 1000;
                value = 3;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tvVisibility2:
                llRain.setVisibility(View.INVISIBLE);
                llTemp.setVisibility(View.INVISIBLE);

                tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
                tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(0xff2d5a9d);
                tvVisibility2.setBackgroundResource(R.drawable.bg_layer_button);
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.visibilityLegend)) {
                    Picasso.get().load(FactManager.visibilityLegend).into(ivLegend);
                }
                pastTime = 60 * 60 * 1000;
                value = 4;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tvPressure2:
                llRain.setVisibility(View.INVISIBLE);
                llTemp.setVisibility(View.INVISIBLE);

                tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
                tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(0xff2d5a9d);
                tvPressure2.setBackgroundResource(R.drawable.bg_layer_button);
                tvWindSpeed2.setTextColor(getResources().getColor(R.color.text_color4));
                tvWindSpeed2.setBackgroundColor(getResources().getColor(R.color.transparent));

                if (!TextUtils.isEmpty(FactManager.airpressureLegend)) {
                    Picasso.get().load(FactManager.airpressureLegend).into(ivLegend);
                }
                pastTime = 60 * 60 * 1000;
                value = 5;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tvWindSpeed2:
                llRain.setVisibility(View.INVISIBLE);
                llTemp.setVisibility(View.INVISIBLE);

                tvRain2.setTextColor(getResources().getColor(R.color.text_color4));
                tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvTemp2.setTextColor(getResources().getColor(R.color.text_color4));
                tvTemp2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvHumidity2.setTextColor(getResources().getColor(R.color.text_color4));
                tvHumidity2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvVisibility2.setTextColor(getResources().getColor(R.color.text_color4));
                tvVisibility2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvPressure2.setTextColor(getResources().getColor(R.color.text_color4));
                tvPressure2.setBackgroundColor(getResources().getColor(R.color.transparent));
                tvWindSpeed2.setTextColor(0xff2d5a9d);
                tvWindSpeed2.setBackgroundResource(R.drawable.bg_layer_button);

                if (!TextUtils.isEmpty(FactManager.windspeedLegend)) {
                    Picasso.get().load(FactManager.windspeedLegend).into(ivLegend);
                }
                pastTime = 60 * 60 * 1000;
                value = 6;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawNationDataToMap(value, LOADTYPE3);
                        }
                    }, 100);
                }
                break;
            case R.id.tvCheckStation:
                Intent intent = new Intent(mContext, FactRankDetailActivity.class);
                intent.putExtra(CONST.ACTIVITY_NAME, stationName);
                intent.putExtra("stationId", stationId);
                intent.putExtra("interface", "oneDay");
                startActivity(intent);
                break;
            case R.id.ivArea:
                reContent.setVisibility(View.GONE);

                Intent intentMap = new Intent(mContext, FactAreaSearchActivity.class);
                intentMap.putExtra(CONST.ACTIVITY_NAME, getString(R.string.selecte_area));
                startActivityForResult(intentMap, 0);
                break;
            case R.id.tvProName:
            case R.id.ivProName:
                tvProName.setVisibility(View.GONE);
                tvProName.setText("");
                ivProName.setVisibility(View.GONE);
                removeStationMarkers();
                removeProvinceBounds();
                zoom = 3.5f;
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
                break;
            case R.id.ivDelete:
                closeDetailWindow();
                break;
            case R.id.ivLocation:
                if (zoom < 10.f) {
                    zoom = 10.0f;
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, zoom));
                } else {
                    zoom = 3.5f;
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, zoom));
                }
                break;
            case R.id.ivLegendPrompt:
                if (ivLegend.getVisibility() == View.VISIBLE) {
                    ivLegend.setVisibility(View.INVISIBLE);
                } else {
                    ivLegend.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.ivRank:
                startActivity(new Intent(mContext, FactRankActivity.class));
                break;
            case R.id.ivGuide:
                ivGuide.setVisibility(View.GONE);
                CommonUtil.saveGuidePageState(mContext, this.getClass().getName());
                break;
            case R.id.ivAdd:
                if (zoom < 6.0f) {
                    zoom = 6.0f;
                } else if (zoom >= 6.0 && zoom < 8.0f)  {
                    zoom = 8.0f;
                } else if (zoom >= 8.0 && zoom < 10.0f)  {
                    zoom = 10.0f;
                } else  {

                }
                moveCursor();
                break;
            case R.id.ivMinuse:
                if (zoom < 6.0f) {

                } else if (zoom >= 6.0 && zoom < 8.0f)  {
                    zoom = 3.5f;
                } else if (zoom >= 8.0 && zoom < 10.0f)  {
                    zoom = 7.0f;
                } else  {
                    zoom = 9.0f;
                }
                moveCursor();
                break;
            case R.id.ivTyphoonRadar:
                isRadarOn = !isRadarOn;
                if (isRadarOn) {//添加雷达图
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

                }else {//删除雷达图
                    ivTyphoonRadar.setImageResource(R.drawable.shawn_icon_typhoon_radar_off);
                    removeRadar();
                }
                break;
            case R.id.ivTyphoonCloud:
                isCloudOn = !isCloudOn;
                if (isCloudOn) {//添加云图
                    ivTyphoonCloud.setImageResource(R.drawable.shawn_icon_typhoon_cloud_on);
                    if (cloudBitmap == null) {
                        OkHttpCloudChart();
                    }else {
                        drawCloud(cloudBitmap);
                    }

                    ivTyphoonRadar.setImageResource(R.drawable.shawn_icon_typhoon_radar_off);
                    removeRadar();
                    isRadarOn = false;

                }else {//删除云图
                    ivTyphoonCloud.setImageResource(R.drawable.shawn_icon_typhoon_cloud_off);
                    removeCloud();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String provinceName = bundle.getString("provinceName");
                        if (!TextUtils.isEmpty(provinceName)) {
                            if (!TextUtils.isEmpty(tvLayerName.getText().toString())) {
                                tvLayerName.setText(tvLayerName.getText().toString().replace(layerName, provinceName));
                                layerName = provinceName;
                            }
                            tvProName.setText(provinceName);
                            tvProName.setVisibility(View.VISIBLE);
                            ivProName.setVisibility(View.VISIBLE);
                            drawProvinceDataToMap(value, LOADTYPE3);
                        }
                    }
                    break;

                default:
                    break;
            }
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
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (mRadarManager != null) {
            mRadarManager.onDestory();
        }
        removeRadar();
    }

}
