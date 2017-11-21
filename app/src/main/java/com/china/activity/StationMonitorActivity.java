package com.china.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
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
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
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
import com.china.common.MyApplication;
import com.china.dto.StationMonitorDto;
import com.china.manager.DBManager;
import com.china.manager.RainManager;
import com.china.manager.StationManager;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.view.StationCursorView;

import net.tsz.afinal.FinalBitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 站点监测
 *
 * @author shawn_sun
 */

@SuppressLint("SimpleDateFormat")
public class StationMonitorActivity extends BaseActivity implements OnClickListener, AMapLocationListener, OnMarkerClickListener,
        InfoWindowAdapter, OnMapClickListener, OnGeocodeSearchListener, OnCameraChangeListener, OnMapScreenShotListener,
        AMap.OnMapLoadedListener, DistrictSearch.OnDistrictSearchListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private MapView mMapView = null;
    private AMap aMap = null;
    private List<StationMonitorDto> stationList = new ArrayList<StationMonitorDto>();
    private TextView tvName = null;
    private TextView tvStationId = null;
    private TextView tvTemp = null;
    private TextView tvDistance = null;
    private TextView tvJiangshui = null;
    private TextView tvShidu = null;
    private TextView tvWind = null;
    private TextView tvLoudian = null;
    private TextView tvVisible = null;
    private TextView tvPressrue = null;
    private TextView tvCheckStation = null;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("HH时");
    private long pastTime = 60 * 60 * 1000;
    private RelativeLayout reContent = null;
    private ImageView ivDelete = null;
    public final static String SANX_DATA_99 = "sanx_data_99";//加密秘钥名称
    public final static String APPID = "f63d329270a44900";//机密需要用到的AppId
    private float zoom = 3.5f;
    private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
    private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
    private String stationName = null;
    private String stationId = null;
    private TextView tvTemp2 = null;
    private TextView tvRain2 = null;
    private TextView tvHumidity2 = null;
    private TextView tvVisibility2 = null;
    private TextView tvPressure2 = null;
    private TextView tvWindSpeed2 = null;
    private List<Marker> markerList = new ArrayList<Marker>();//10个站点的marker
    private ImageView ivMapSearch = null;//省市县筛选站点
    private double locationLat = 0;
    private double locationLng = 0;
    private GeocodeSearch geocoderSearch = null;
    private String precipitation1hLegend = null;
    private String precipitation3hLegend = null;
    private String precipitation6hLegend = null;
    private String precipitation12hLegend = null;
    private String precipitation24hLegend = null;
    private String balltempLegend = null;
    private String humidityLegend = null;
    private String visibilityLegend = null;
    private String windspeedLegend = null;
    private String airpressureLegend = null;
    private String precipitation1hJson = null;
    private String precipitation3hJson = null;
    private String precipitation6hJson = null;
    private String precipitation12hJson = null;
    private String precipitation24hJson = null;
    private String balltempJson = null;
    private String humidityJson = null;
    private String visibilityJson = null;
    private String windspeedJson = null;
    private String airpressureJson = null;
    private List<Polygon> polygons = new ArrayList<Polygon>();
    private List<Text> texts = new ArrayList<Text>();//等值线
    private ImageView ivLocation = null;//定位按钮
    private TextView tvLayerName = null;//图层名称
    private String layerName = "全国";
    private int value = 1;//默认为降水
    private ImageView ivLegend = null;
    private ImageView ivLegendPrompt = null;
    private HorizontalScrollView hScrollView = null;
    private LinearLayout llRain = null;
    private TextView tv1, tv2, tv3, tv4, tv5;
    private ImageView ivRank = null;//进入排序界面
    private RelativeLayout reShare = null;
    private ImageView ivShare = null;
    private LinearLayout llScrollView = null;
    private TextView tvProName = null;
    private ImageView ivProName = null;
    private final String LOADTYPE1 = "default";//默认加载图层数据方式
    private final String LOADTYPE2 = "move";//地图缩放、移动等
    private final String LOADTYPE3 = "click";//天气6要素按钮点击切换
    private LatLng leftlatlng = null, rightLatlng = null;//可视区域左上右下对应的经纬度
    private List<String> precipitation1hColor = new ArrayList<>();
    private List<String> precipitation3hColor = new ArrayList<>();
    private List<String> precipitation6hColor = new ArrayList<>();
    private List<String> precipitation12hColor = new ArrayList<>();
    private List<String> precipitation24hColor = new ArrayList<>();
    private List<String> balltempColor = new ArrayList<>();
    private List<String> humidityColor = new ArrayList<>();
    private List<String> visibilityColor = new ArrayList<>();
    private List<String> windspeedColor = new ArrayList<>();
    private List<String> airpressureColor = new ArrayList<>();
    private List<Polyline> boundLines = new ArrayList<>();//省份边界
    private LinearLayout llCursor = null;//游标
    private StationCursorView cursorView = null;
    private ImageView ivGuide = null;//引导页

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_monitor);
        mContext = this;
        initMap(savedInstanceState);
        initWidget();
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
        aMap.setInfoWindowAdapter(this);
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMapLoadedListener(this);
    }

    @Override
    public void onMapLoaded() {
        asyncGetMapUrl("http://decision-171.tianqi.cn/weather/rgwst/JsonCatalogue?map=china&type=1");//获取中国地区五中天气现象数据
        asyncGetMapLegend("http://decision-171.tianqi.cn/weather/rgwst/jsontuli");//获取图例
    }

    /**
     * 获取五种天气要素的json地址
     *
     * @param url
     */
    private void asyncGetMapUrl(final String url) {
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
                        if (result != null) {
                            try {
                                JSONArray array = new JSONArray(result);
                                if (array.length() == 0) {
                                    hScrollView.setVisibility(View.GONE);
                                    return;
                                }
                                JSONObject obj = array.getJSONObject(0);
                                if (!obj.isNull("precipitation1h")) {
                                    precipitation1hJson = obj.getString("precipitation1h");
                                }
                                if (!obj.isNull("rainfall3")) {
                                    precipitation3hJson = obj.getString("rainfall3");
                                }
                                if (!obj.isNull("rainfall6")) {
                                    precipitation6hJson = obj.getString("rainfall6");
                                }
                                if (!obj.isNull("rainfall12")) {
                                    precipitation12hJson = obj.getString("rainfall12");
                                }
                                if (!obj.isNull("rainfall24")) {
                                    precipitation24hJson = obj.getString("rainfall24");
                                }
                                if (!obj.isNull("balltemp")) {
                                    balltempJson = obj.getString("balltemp");
                                }
                                if (!obj.isNull("humidity")) {
                                    humidityJson = obj.getString("humidity");
                                }
                                if (!obj.isNull("windspeed")) {
                                    windspeedJson = obj.getString("windspeed");
                                }
                                if (!obj.isNull("visibility")) {
                                    visibilityJson = obj.getString("visibility");
                                }
                                if (!obj.isNull("airpressure")) {
                                    airpressureJson = obj.getString("airpressure");
                                }
                                if (!TextUtils.isEmpty(precipitation1hJson)) {
                                    asyncGetMapData(precipitation1hJson);
                                }
                                if (!TextUtils.isEmpty(precipitation3hJson)) {
                                    StationManager.asyncGetMapData3H(precipitation3hJson);
                                }
                                if (!TextUtils.isEmpty(precipitation6hJson)) {
                                    StationManager.asyncGetMapData6H(precipitation6hJson);
                                }
                                if (!TextUtils.isEmpty(precipitation12hJson)) {
                                    StationManager.asyncGetMapData12H(precipitation12hJson);
                                }
                                if (!TextUtils.isEmpty(precipitation24hJson)) {
                                    StationManager.asyncGetMapData24H(precipitation24hJson);
                                }
                                if (!TextUtils.isEmpty(balltempJson)) {
                                    StationManager.asyncGetMapData2(balltempJson);
                                }
                                if (!TextUtils.isEmpty(humidityJson)) {
                                    StationManager.asyncGetMapData3(humidityJson);
                                }
                                if (!TextUtils.isEmpty(visibilityJson)) {
                                    StationManager.asyncGetMapData4(visibilityJson);
                                }
                                if (!TextUtils.isEmpty(airpressureJson)) {
                                    StationManager.asyncGetMapData5(airpressureJson);
                                }
                                if (!TextUtils.isEmpty(windspeedJson)) {
                                    StationManager.asyncGetMapData6(windspeedJson);
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

    /**
     * 获取图例
     *
     * @param url
     */
    private void asyncGetMapLegend(final String url) {
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
                        if (result != null) {
                            try {
                                JSONObject obj = new JSONObject(result);
                                if (!obj.isNull("jc_1xsjs")) {
                                    precipitation1hLegend = obj.getString("jc_1xsjs");
                                }
                                if (!TextUtils.isEmpty(precipitation1hLegend)) {
                                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                                    finalBitmap.display(ivLegend, precipitation1hLegend, null, 0);
                                }
                                if (!obj.isNull("jc_3xsjs")) {
                                    precipitation3hLegend = obj.getString("jc_3xsjs");
                                }
                                if (!obj.isNull("jc_6xsjs")) {
                                    precipitation6hLegend = obj.getString("jc_6xsjs");
                                }
                                if (!obj.isNull("jc_12xsjs")) {
                                    precipitation12hLegend = obj.getString("jc_12xsjs");
                                }
                                if (!obj.isNull("jc_24xsjs")) {
                                    precipitation24hLegend = obj.getString("jc_24xsjs");
                                }
                                if (!obj.isNull("jc_wdtl")) {
                                    balltempLegend = obj.getString("jc_wdtl");
                                }
                                if (!obj.isNull("jc_xdsdtl")) {
                                    humidityLegend = obj.getString("jc_xdsdtl");
                                }
                                if (!obj.isNull("jc_fltl")) {
                                    windspeedLegend = obj.getString("jc_fltl");
                                }
                                if (!obj.isNull("jc_njdtl")) {
                                    visibilityLegend = obj.getString("jc_njdtl");
                                }
                                if (!obj.isNull("jc_qytl")) {
                                    airpressureLegend = obj.getString("jc_qytl");
                                }

                                if (!obj.isNull("jb_1xsjs")) {
                                    precipitation1hColor.clear();
                                    JSONArray array = obj.getJSONArray("jb_1xsjs");
                                    for (int i = 0; i < array.length(); i++) {
                                        precipitation1hColor.add(array.getString(i));
                                    }
                                }
                                if (!obj.isNull("jb_3xsjs")) {
                                    precipitation3hColor.clear();
                                    JSONArray array = obj.getJSONArray("jb_3xsjs");
                                    for (int i = 0; i < array.length(); i++) {
                                        precipitation3hColor.add(array.getString(i));
                                    }
                                }
                                if (!obj.isNull("jb_6xsjs")) {
                                    precipitation6hColor.clear();
                                    JSONArray array = obj.getJSONArray("jb_6xsjs");
                                    for (int i = 0; i < array.length(); i++) {
                                        precipitation6hColor.add(array.getString(i));
                                    }
                                }
                                if (!obj.isNull("jb_12xsjs")) {
                                    precipitation12hColor.clear();
                                    JSONArray array = obj.getJSONArray("jb_12xsjs");
                                    for (int i = 0; i < array.length(); i++) {
                                        precipitation12hColor.add(array.getString(i));
                                    }
                                }
                                if (!obj.isNull("jb_24xsjs")) {
                                    precipitation24hColor.clear();
                                    JSONArray array = obj.getJSONArray("jb_24xsjs");
                                    for (int i = 0; i < array.length(); i++) {
                                        precipitation24hColor.add(array.getString(i));
                                    }
                                }
                                if (!obj.isNull("jb_fltl")) {
                                    windspeedColor.clear();
                                    JSONArray array = obj.getJSONArray("jb_fltl");
                                    for (int i = 0; i < array.length(); i++) {
                                        windspeedColor.add(array.getString(i));
                                    }
                                }
                                if (!obj.isNull("jb_njdtl")) {
                                    visibilityColor.clear();
                                    JSONArray array = obj.getJSONArray("jb_njdtl");
                                    for (int i = 0; i < array.length(); i++) {
                                        visibilityColor.add(array.getString(i));
                                    }
                                }
                                if (!obj.isNull("jb_qytl")) {
                                    airpressureColor.clear();
                                    JSONArray array = obj.getJSONArray("jb_qytl");
                                    for (int i = 0; i < array.length(); i++) {
                                        airpressureColor.add(array.getString(i));
                                    }
                                }
                                if (!obj.isNull("jb_wdtl")) {
                                    balltempColor.clear();
                                    JSONArray array = obj.getJSONArray("jb_wdtl");
                                    for (int i = 0; i < array.length(); i++) {
                                        balltempColor.add(array.getString(i));
                                    }
                                }
                                if (!obj.isNull("jb_xdsdtl")) {
                                    humidityColor.clear();
                                    JSONArray array = obj.getJSONArray("jb_xdsdtl");
                                    for (int i = 0; i < array.length(); i++) {
                                        humidityColor.add(array.getString(i));
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

    /**
     * 获取五中天气要素数据接口
     *
     * @param url
     */
    private void asyncGetMapData(String url) {
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
                    StationManager.precipitation1hResult = result;
                    drawNationDataToMap(value, LOADTYPE1);
                }
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

        setMapEmit(arg0.zoom, arg0.target.latitude, arg0.target.longitude);

        zoom = arg0.zoom;
        if (cursorView != null) {
            if (llCursor != null) {
                llCursor.setVisibility(View.VISIBLE);
            }
            cursorView.refreshCursor(zoom);
            delayedHandler.removeMessages(1000);
            Message msg = delayedHandler.obtainMessage();
            msg.what = 1000;
            delayedHandler.sendMessageDelayed(msg, 3000);
        }

        if (tvProName.getVisibility() == View.VISIBLE) {
            drawProvinceDataToMap(value, LOADTYPE2);
        } else {
            drawNationDataToMap(value, LOADTYPE2);
        }
    }

    private Handler delayedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1000:
                    if (llCursor != null) {
                        llCursor.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    /**
     * 清除图层
     */
    private void removeWeatherLayer() {
        for (int i = 0; i < polygons.size(); i++) {
            polygons.get(i).remove();
        }
        polygons.clear();
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
     * 绘制全国区域
     *
     * @param value 判断降水、气温、气压等类型
     */
    private void drawNationDataToMap(int value, String loadType) {
        String result = "";
        String type = "";
        if (value == 1) {
            result = StationManager.precipitation1hResult;
            type = getString(R.string.layer_rain1);
        } else if (value == 13) {
            result = StationManager.precipitation3hResult;
            type = getString(R.string.layer_rain3);
        } else if (value == 16) {
            result = StationManager.precipitation6hResult;
            type = getString(R.string.layer_rain6);
        } else if (value == 112) {
            result = StationManager.precipitation12hResult;
            type = getString(R.string.layer_rain12);
        } else if (value == 124) {
            result = StationManager.precipitation24hResult;
            type = getString(R.string.layer_rain24);
        } else if (value == 2) {
            result = StationManager.balltempResult;
            type = getString(R.string.layer_temp);
        } else if (value == 3) {
            result = StationManager.humidityResult;
            type = getString(R.string.layer_humidity);
        } else if (value == 4) {
            result = StationManager.visibilityResult;
            type = getString(R.string.layer_visible);
        } else if (value == 5) {
            result = StationManager.airpressureResult;
            type = getString(R.string.layer_pressure);
        } else if (value == 6) {
            result = StationManager.windspeedResult;
            type = getString(R.string.layer_wind);
        }

        if (TextUtils.equals(loadType, LOADTYPE1)) {//默认只加载图层数据、图层名称
            drawWeatherLayerName(result, type);
            drawWeatherLayer(result);
        } else if (TextUtils.equals(loadType, LOADTYPE2)) {//监听地图缩放、移动等
            if (zoom < 10.0f) {//保持图层一直在
                removeStationMarkers();

                if (polygons.size() == 0) {
                    drawWeatherLayer(result);
                }
                if (zoom < 8.0f) {
                    removeValueLine();
                } else if (zoom >= 8.0f && zoom < 10.0f) {//绘制等值线
                    if (texts.size() == 0) {
                        drawValueLine(result);
                    }
                }
            } else {//清除图层、等值线，绘制站点
                removeWeatherLayer();
                removeValueLine();

                queryNationViewStationInfo(stationList, loadType);//绘制站点
            }
        } else if (TextUtils.equals(loadType, LOADTYPE3)) {//监听天气6要素切换点击事件
            removeWeatherLayer();
            removeValueLine();
            removeStationMarkers();

            drawWeatherLayerName(result, type);

            if (zoom < 10.0f) {//绘制新选择的图层数据
                if (polygons.size() == 0) {
                    drawWeatherLayer(result);
                }
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
        if (value == 1) {
            result = StationManager.precipitation1hResult;
            type = getString(R.string.layer_rain1);
        } else if (value == 13) {
            result = StationManager.precipitation3hResult;
            type = getString(R.string.layer_rain3);
        } else if (value == 16) {
            result = StationManager.precipitation6hResult;
            type = getString(R.string.layer_rain6);
        } else if (value == 112) {
            result = StationManager.precipitation12hResult;
            type = getString(R.string.layer_rain12);
        } else if (value == 124) {
            result = StationManager.precipitation24hResult;
            type = getString(R.string.layer_rain24);
        } else if (value == 2) {
            result = StationManager.balltempResult;
            type = getString(R.string.layer_temp);
        } else if (value == 3) {
            result = StationManager.humidityResult;
            type = getString(R.string.layer_humidity);
        } else if (value == 4) {
            result = StationManager.visibilityResult;
            type = getString(R.string.layer_visible);
        } else if (value == 5) {
            result = StationManager.airpressureResult;
            type = getString(R.string.layer_pressure);
        } else if (value == 6) {
            result = StationManager.windspeedResult;
            type = getString(R.string.layer_wind);
        }

        if (TextUtils.equals(loadType, LOADTYPE1)) {//默认只加载图层数据、图层名称

        } else if (TextUtils.equals(loadType, LOADTYPE2)) {//监听地图缩放、移动等
            removeStationMarkers();

            queryProStationInfo(stationList, tvProName.getText().toString(), loadType);//绘制省份站点
        } else if (TextUtils.equals(loadType, LOADTYPE3)) {//监听天气6要素切换点击事件
            removeWeatherLayer();
            removeValueLine();
            removeStationMarkers();

            drawWeatherLayerName(result, type);
            queryProStationInfo(stationList, tvProName.getText().toString(), loadType);//绘制省份站点
        }

    }

    /**
     * 绘制图层名称
     *
     * @param result
     * @param type
     */
    private void drawWeatherLayerName(String result, String type) {
        if (TextUtils.isEmpty(result) || TextUtils.isEmpty(type)) {
            return;
        }
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

    /**
     * 绘制图层
     *
     * @param result
     */
    private void drawWeatherLayer(String result) {
        if (TextUtils.isEmpty(result)) {
            return;
        }
        try {
            JSONObject obj = new JSONObject(result);
            JSONArray array = obj.getJSONArray("l");
            int length = array.length();
            for (int i = 0; i < length; i++) {
                JSONObject itemObj = array.getJSONObject(i);
                JSONArray c = itemObj.getJSONArray("c");
                int r = c.getInt(0);
                int g = c.getInt(1);
                int b = c.getInt(2);
                int a = c.getInt(3) * (int)(255 * 0.7f);

                if (!itemObj.isNull("p")) {
                    String p = itemObj.getString("p");
                    if (!TextUtils.isEmpty(p)) {
                        String[] points = p.split(";");
                        PolygonOptions polylineOption = new PolygonOptions();
                        polylineOption.fillColor(Color.argb(a, r, g, b));
                        polylineOption.strokeColor(Color.TRANSPARENT);
                        for (int j = 0; j < points.length; j++) {
                            String[] latLng = points[j].split(",");
                            double lat = Double.valueOf(latLng[1]);
                            double lng = Double.valueOf(latLng[0]);
                            polylineOption.add(new LatLng(lat, lng));
                        }
                        Polygon polygon = aMap.addPolygon(polylineOption);
                        polygons.add(polygon);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 绘制等值线
     *
     * @param result
     */
    private void drawValueLine(String result) {
        if (TextUtils.isEmpty(result)) {
            return;
        }
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
                        for (int j = 0; j < points.length; j++) {
                            String[] latLng = points[j].split(",");
                            double lat = Double.valueOf(latLng[1]);
                            double lng = Double.valueOf(latLng[0]);
                            if (j == points.length / 2) {
                                centerLat = lat;
                                centerLng = lng;
                            }
                        }
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
//		query.setKeywordsLevel(DistrictSearchQuery.KEYWORDS_CITY);
        query.setShowBoundary(true);//是否返回边界值
        search.setQuery(query);
        search.setOnDistrictSearchListener(this);//绑定监听器
        search.searchDistrictAnsy();//开始搜索
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

        new Thread() {
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
        }.start();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        columnId = getIntent().getStringExtra(CONST.COLUMN_ID);//栏目id

        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvName = (TextView) findViewById(R.id.tvName);
        tvStationId = (TextView) findViewById(R.id.tvStationId);
        tvTemp = (TextView) findViewById(R.id.tvTemp);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvJiangshui = (TextView) findViewById(R.id.tvJiangshui);
        tvWind = (TextView) findViewById(R.id.tvWind);
        tvShidu = (TextView) findViewById(R.id.tvShidu);
        tvLoudian = (TextView) findViewById(R.id.tvLoudian);
        tvVisible = (TextView) findViewById(R.id.tvVisible);
        tvPressrue = (TextView) findViewById(R.id.tvPressrue);
        tvCheckStation = (TextView) findViewById(R.id.tvCheckStation);
        tvCheckStation.setOnClickListener(this);
        reContent = (RelativeLayout) findViewById(R.id.reContent);
        reContent.setOnClickListener(this);
        ivDelete = (ImageView) findViewById(R.id.ivDelete);
        ivDelete.setOnClickListener(this);
        ivLocation = (ImageView) findViewById(R.id.ivLocation);
        ivLocation.setOnClickListener(this);
        tvLayerName = (TextView) findViewById(R.id.tvLayerName);
        ivLegend = (ImageView) findViewById(R.id.ivLegend);
        ivLegendPrompt = (ImageView) findViewById(R.id.ivLegendPrompt);
        ivLegendPrompt.setOnClickListener(this);
        hScrollView = (HorizontalScrollView) findViewById(R.id.hScrollView);
        llRain = (LinearLayout) findViewById(R.id.llRain);
        reShare = (RelativeLayout) findViewById(R.id.reShare);
        ivShare = (ImageView) findViewById(R.id.ivShare);
        ivShare.setOnClickListener(this);
        llScrollView = (LinearLayout) findViewById(R.id.llScrollView);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv1.setOnClickListener(this);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv2.setOnClickListener(this);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv3.setOnClickListener(this);
        tv4 = (TextView) findViewById(R.id.tv4);
        tv4.setOnClickListener(this);
        tv5 = (TextView) findViewById(R.id.tv5);
        tv5.setOnClickListener(this);
        ivRank = (ImageView) findViewById(R.id.ivRank);
        ivRank.setOnClickListener(this);
        tvProName = (TextView) findViewById(R.id.tvProName);
        tvProName.setOnClickListener(this);
        ivProName = (ImageView) findViewById(R.id.ivProName);
        ivProName.setOnClickListener(this);
        llCursor = (LinearLayout) findViewById(R.id.llCursor);
        ivGuide = (ImageView) findViewById(R.id.ivGuide);
        ivGuide.setOnClickListener(this);
        CommonUtil.showGuidePage(mContext, this.getClass().getName(), ivGuide);

        tvTemp2 = (TextView) findViewById(R.id.tvTemp2);
        tvTemp2.setOnClickListener(this);
        tvRain2 = (TextView) findViewById(R.id.tvRain2);
        tvRain2.setOnClickListener(this);
        tvHumidity2 = (TextView) findViewById(R.id.tvHumidity2);
        tvHumidity2.setOnClickListener(this);
        tvVisibility2 = (TextView) findViewById(R.id.tvVisibility2);
        tvVisibility2.setOnClickListener(this);
        tvPressure2 = (TextView) findViewById(R.id.tvPressure2);
        tvPressure2.setOnClickListener(this);
        tvWindSpeed2 = (TextView) findViewById(R.id.tvWindSpeed2);
        tvWindSpeed2.setOnClickListener(this);
        ivMapSearch = (ImageView) findViewById(R.id.ivMapSearch);
        ivMapSearch.setOnClickListener(this);

        String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
        if (title != null) {
            tvTitle.setText(title);
        }

        llCursor.removeAllViews();
        cursorView = new StationCursorView(mContext);
        llCursor.addView(cursorView, (int) CommonUtil.dip2px(mContext, 30), (int) CommonUtil.dip2px(mContext, 160));

        geocoderSearch = new GeocodeSearch(mContext);
        geocoderSearch.setOnGeocodeSearchListener(this);

        startLocation();

        String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
        CommonUtil.submitClickCount(columnId, title);
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
            Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_map_location),
                    (int) (CommonUtil.dip2px(mContext, 15)), (int) (CommonUtil.dip2px(mContext, 15)));
            if (bitmap != null) {
                options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            } else {
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iv_map_location));
            }
            options.position(latLng);
            Marker locationMarker = aMap.addMarker(options);
            locationMarker.setClickable(false);
        }
    }

    /**
     * 查询全国符合可视化区域站点信息
     *
     * @param list
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
                    dto.lat = cursor.getString(cursor.getColumnIndex("LAT"));
                    dto.lng = cursor.getString(cursor.getColumnIndex("LON"));

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
                cursor = null;
                dbManager.closeDatabase();

                if (!TextUtils.isEmpty(ids)) {
                    asyncTaskContentPost(ids.substring(0, ids.length() - 1), loadType);
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
                    dto.lat = cursor.getString(cursor.getColumnIndex("LAT"));
                    dto.lng = cursor.getString(cursor.getColumnIndex("LON"));

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
                cursor = null;
                dbManager.closeDatabase();

                if (!TextUtils.isEmpty(ids)) {
                    asyncTaskContentPost(ids.substring(0, ids.length() - 1), loadType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 加密请求字符串
     *
     * @return
     */
    private String getStationContentUrlPost(String stationIds) {
        String URL = "http://decision-171.tianqi.cn/weather/rgwst/NewestDataNew";
        String sysdate = RainManager.getDate(Calendar.getInstance(), "yyyyMMddHHmmss");//系统时间
        StringBuffer buffer = new StringBuffer();
        buffer.append(URL);
        buffer.append("?");
        buffer.append("stationids=").append(stationIds);
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

    /**
     * 获取最近10个站点的信息
     *
     * @param stationIds
     */
    private void asyncTaskContentPost(String stationIds, final String loadType) {
        String[] ids = stationIds.split(",");
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        formBodyBuilder.add("ids", stationIds);
        RequestBody requestBody = formBodyBuilder.build();
        OkHttpUtil.enqueue(new Request.Builder().url(getStationContentUrlPost(ids[0])).post(requestBody).build(), new Callback() {
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

                            dto.distance = getDistance(locationLng, locationLat, Double.valueOf(dto.lng), Double.valueOf(dto.lat));

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

                            if (!obj.isNull("winddir")) {
                                String dir = null;
                                String value = obj.getString("winddir");
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
                                        wind_dir = mContext.getString(R.string.chart_north);
                                    } else if (fx > 0 && fx < 90) {
                                        wind_dir = mContext.getString(R.string.chart_north_east);
                                    } else if (fx == 90) {
                                        wind_dir = mContext.getString(R.string.chart_east);
                                    } else if (fx > 90 && fx < 180) {
                                        wind_dir = mContext.getString(R.string.chart_south_east);
                                    } else if (fx == 180) {
                                        wind_dir = mContext.getString(R.string.chart_south);
                                    } else if (fx > 180 && fx < 270) {
                                        wind_dir = mContext.getString(R.string.chart_south_west);
                                    } else if (fx == 270) {
                                        wind_dir = mContext.getString(R.string.chart_west);
                                    } else if (fx > 270) {
                                        wind_dir = mContext.getString(R.string.chart_north_west);
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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addMarker(stationList, value, loadType);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 移除选择点的marker
     * 移除map上已经存在的站点marker
     */
    private void removeStationMarkers() {
        for (int i = 0; i < markerList.size(); i++) {
            markerList.get(i).remove();
        }
        markerList.clear();
    }

    /**
     * 添加marker
     *
     * @param list
     * @param value
     */
    private void addMarker(final List<StationMonitorDto> list, int value, String loadType) {
        removeStationMarkers();
        if (list.isEmpty() || list.size() == 0) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            StationMonitorDto dto = list.get(i);
            double lat = 0;
            double lng = 0;
            if (!TextUtils.isEmpty(dto.lat)) {
                lat = Double.valueOf(dto.lat);
            }
            if (!TextUtils.isEmpty(dto.lng)) {
                lng = Double.valueOf(dto.lng);
            }
            MarkerOptions options = new MarkerOptions();
            options.title(dto.stationId);
            options.snippet(dto.name);
            options.anchor(0.5f, 0.5f);
            options.position(new LatLng(lat, lng));
            View markerView = null;
            if (value == 1) {
                markerView = getTextBitmap(value, dto.precipitation1h);
            } else if (value == 13) {
                markerView = getTextBitmap(value, dto.precipitation3h);
            } else if (value == 16) {
                markerView = getTextBitmap(value, dto.precipitation6h);
            } else if (value == 112) {
                markerView = getTextBitmap(value, dto.precipitation12h);
            } else if (value == 124) {
                markerView = getTextBitmap(value, dto.precipitation24h);
            } else if (value == 2) {
                markerView = getTextBitmap(value, dto.ballTemp);
            } else if (value == 3) {
                markerView = getTextBitmap(value, dto.humidity);
            } else if (value == 4) {
                markerView = getTextBitmap(value, dto.visibility);
            } else if (value == 5) {
                markerView = getTextBitmap(value, dto.airPressure);
            } else if (value == 6) {
                markerView = getTextBitmap(value, dto.windSpeed);
            }
            if (markerView != null && lat != 0 && lng != 0) {
                options.icon(BitmapDescriptorFactory.fromView(markerView));
                Marker m = aMap.addMarker(options);
                markerList.add(m);
            }
        }

        if (tvProName.getVisibility() == View.VISIBLE) {
            if (TextUtils.equals(loadType, LOADTYPE3)) {
                drawProvinceBounds(tvProName.getText().toString());
                setProvinceBounds();
            }
        }

    }

    /**
     * 设置省份边界
     */
    private void setProvinceBounds() {
        if (markerList.size() > 0) {
            double leftLat = markerList.get(0).getPosition().latitude;
            double leftLng = markerList.get(0).getPosition().longitude;
            double rightLat = markerList.get(0).getPosition().latitude;
            double rightLng = markerList.get(0).getPosition().longitude;
            for (int i = 0; i < markerList.size(); i++) {
                if (leftLat >= markerList.get(i).getPosition().latitude) {
                    leftLat = markerList.get(i).getPosition().latitude;
                }
                if (leftLng >= markerList.get(i).getPosition().longitude) {
                    leftLng = markerList.get(i).getPosition().longitude;
                }
                if (rightLat <= markerList.get(i).getPosition().latitude) {
                    rightLat = markerList.get(i).getPosition().latitude;
                }
                if (rightLng <= markerList.get(i).getPosition().longitude) {
                    rightLng = markerList.get(i).getPosition().longitude;
                }
            }

            final LatLng left = new LatLng(leftLat, leftLng);
            final LatLng right = new LatLng(rightLat, rightLng);
            //延时1秒开始地图动画
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(left)
                                .include(right).build();
                        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }, 500);
        }
    }

    /**
     * 给marker添加文字
     *
     * @param number
     * @return
     */
    private View getTextBitmap(int value, String number) {
        if (TextUtils.isEmpty(number) || TextUtils.equals(number, CONST.noValue)) {
            return null;
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.station_monitor_item, null);
        if (view == null) {
            return null;
        }
        ImageView ivMarker = (ImageView) view.findViewById(R.id.ivMarker);
        GradientDrawable gradientDrawable = (GradientDrawable) ivMarker.getBackground();
        gradientDrawable.setColor(pointColor(value, number));
        TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
        tvValue.setText(number);
        return view;
    }

    private int pointColor(int value, String number) {
        String color = "#a5f38d";
        if (value == 1) {
            for (int i = 0; i < precipitation1hColor.size(); i++) {
                String[] colorStr = precipitation1hColor.get(i).split(",");
                if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
                    color = colorStr[2];
                }
            }
        } else if (value == 13) {
            for (int i = 0; i < precipitation3hColor.size(); i++) {
                String[] colorStr = precipitation3hColor.get(i).split(",");
                if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
                    color = colorStr[2];
                }
            }
        } else if (value == 16) {
            for (int i = 0; i < precipitation6hColor.size(); i++) {
                String[] colorStr = precipitation6hColor.get(i).split(",");
                if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
                    color = colorStr[2];
                }
            }
        } else if (value == 112) {
            for (int i = 0; i < precipitation12hColor.size(); i++) {
                String[] colorStr = precipitation12hColor.get(i).split(",");
                if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
                    color = colorStr[2];
                }
            }
        } else if (value == 124) {
            for (int i = 0; i < precipitation24hColor.size(); i++) {
                String[] colorStr = precipitation24hColor.get(i).split(",");
                if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
                    color = colorStr[2];
                }
            }
        } else if (value == 2) {
            for (int i = 0; i < balltempColor.size(); i++) {
                String[] colorStr = balltempColor.get(i).split(",");
                if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
                    color = colorStr[2];
                }
            }
        } else if (value == 3) {
            for (int i = 0; i < humidityColor.size(); i++) {
                String[] colorStr = humidityColor.get(i).split(",");
                if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
                    color = colorStr[2];
                }
            }
        } else if (value == 4) {
            for (int i = 0; i < visibilityColor.size(); i++) {
                String[] colorStr = visibilityColor.get(i).split(",");
                if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
                    color = colorStr[2];
                }
            }
        } else if (value == 5) {
            for (int i = 0; i < airpressureColor.size(); i++) {
                String[] colorStr = airpressureColor.get(i).split(",");
                if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
                    color = colorStr[2];
                }
            }
        } else if (value == 6) {
            for (int i = 0; i < windspeedColor.size(); i++) {
                String[] colorStr = windspeedColor.get(i).split(",");
                if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
                    color = colorStr[2];
                }
            }
        }
        return Color.parseColor(color);
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
        // TODO Auto-generated method stub
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
                        tvTemp.setText(CONST.noValue);
                    } else {
                        tvTemp.setText(data.ballTemp);
                    }

                    if (TextUtils.equals(data.precipitation1h, CONST.noValue)) {
                        tvJiangshui.setText(CONST.noValue);
                    } else {
                        tvJiangshui.setText(data.precipitation1h + getString(R.string.unit_mm));
                    }

                    if (TextUtils.equals(data.humidity, CONST.noValue)) {
                        tvShidu.setText(CONST.noValue);
                    } else {
                        tvShidu.setText(data.humidity + getString(R.string.unit_percent));
                    }

                    if (TextUtils.equals(data.windDir, CONST.noValue) && TextUtils.equals(data.windDir, CONST.noValue)) {
                        tvWind.setText(CONST.noValue);
                    } else {
                        tvWind.setText(data.windDir + " " + data.windSpeed + getString(R.string.unit_speed));
                    }

                    if (TextUtils.equals(data.pointTemp, CONST.noValue)) {
                        tvLoudian.setText(CONST.noValue);
                    } else {
                        tvLoudian.setText(data.pointTemp + getString(R.string.unit_degree));
                    }

                    if (TextUtils.equals(data.visibility, CONST.noValue)) {
                        tvVisible.setText(CONST.noValue);
                    } else {
                        tvVisible.setText(data.visibility + getString(R.string.unit_km));
                    }

                    if (TextUtils.equals(data.airPressure, CONST.noValue)) {
                        tvPressrue.setText(CONST.noValue);
                    } else {
                        tvPressrue.setText(data.airPressure + getString(R.string.unit_hPa));
                    }

                    setPointEmit(stationId, stationName);

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

    @Override
    public View getInfoContents(Marker arg0) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.empty_marker_view, null);
        return view;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 关闭详情窗口
     */
    private void closeDetailWindow() {
        setHidePointEmit();
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

    /**
     * 计算两点之间距离
     *
     * @param longitude1
     * @param latitude1
     * @param longitude2
     * @param latitude2
     * @return
     */
    public static String getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double EARTH_RADIUS = 6378137;
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(Lat1) * Math.cos(Lat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        BigDecimal bd = new BigDecimal(s / 1000);
        double d = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        String distance = d + "";

        String value = distance;
        if (value.length() >= 2 && value.contains(".")) {
            if (value.equals(".0")) {
                distance = "0";
            } else {
                if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
                    distance = value.substring(0, value.indexOf("."));
                } else {
                    distance = value;
                }
            }
        }

        return distance;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    @Override
    public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
        Bitmap bitmap2 = CommonUtil.captureView(reShare);
        Bitmap bitmap3 = CommonUtil.mergeBitmap(StationMonitorActivity.this, bitmap1, bitmap2, true);
        CommonUtil.clearBitmap(bitmap1);
        CommonUtil.clearBitmap(bitmap2);
        Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
//		Bitmap bitmap4 = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable-hdpi/iv_share_bottom.png"));
        Bitmap bitmap5 = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
        CommonUtil.clearBitmap(bitmap3);
        CommonUtil.clearBitmap(bitmap4);
        Bitmap bitmap6 = CommonUtil.captureView(llScrollView);
        Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap6, bitmap5, false);
        CommonUtil.clearBitmap(bitmap5);
        CommonUtil.clearBitmap(bitmap6);
        CommonUtil.share(StationMonitorActivity.this, bitmap);
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
            case R.id.ivShare:
                aMap.getMapScreenShot(StationMonitorActivity.this);
                break;
            case R.id.tv1:
                setElementEmit("1", "0");
                if (llRain.getVisibility() == View.GONE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.GONE);
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

                if (!TextUtils.isEmpty(precipitation1hLegend)) {
                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                    finalBitmap.display(ivLegend, precipitation1hLegend, null, 0);
                }
                pastTime = 60 * 60 * 1000;
                value = 1;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    drawNationDataToMap(value, LOADTYPE3);
                }
                break;
            case R.id.tv2:
                setElementEmit("1", "1");
                if (llRain.getVisibility() == View.GONE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.GONE);
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

                if (!TextUtils.isEmpty(precipitation3hLegend)) {
                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                    finalBitmap.display(ivLegend, precipitation3hLegend, null, 0);
                }
                pastTime = 3 * 60 * 60 * 1000;
                value = 13;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    drawNationDataToMap(value, LOADTYPE3);
                }
                break;
            case R.id.tv3:
                setElementEmit("1", "2");
                if (llRain.getVisibility() == View.GONE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.GONE);
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

                if (!TextUtils.isEmpty(precipitation6hLegend)) {
                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                    finalBitmap.display(ivLegend, precipitation6hLegend, null, 0);
                }
                pastTime = 6 * 60 * 60 * 1000;
                value = 16;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    drawNationDataToMap(value, LOADTYPE3);
                }
                break;
            case R.id.tv4:
                setElementEmit("1", "3");
                if (llRain.getVisibility() == View.GONE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.GONE);
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

                if (!TextUtils.isEmpty(precipitation12hLegend)) {
                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                    finalBitmap.display(ivLegend, precipitation12hLegend, null, 0);
                }
                pastTime = 12 * 60 * 60 * 1000;
                value = 112;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    drawNationDataToMap(value, LOADTYPE3);
                }
                break;
            case R.id.tv5:
                setElementEmit("1", "4");
                if (llRain.getVisibility() == View.GONE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.GONE);
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

                if (!TextUtils.isEmpty(precipitation24hLegend)) {
                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                    finalBitmap.display(ivLegend, precipitation24hLegend, null, 0);
                }
                pastTime = 24 * 60 * 60 * 1000;
                value = 124;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    drawNationDataToMap(value, LOADTYPE3);
                }
                break;
            case R.id.tvRain2:
                if (llRain.getVisibility() == View.GONE) {
                    llRain.setVisibility(View.VISIBLE);
                } else {
                    llRain.setVisibility(View.GONE);
                }

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
            case R.id.tvTemp2:
                setElementEmit("0", "");
                llRain.setVisibility(View.GONE);

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

                if (!TextUtils.isEmpty(balltempLegend)) {
                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                    finalBitmap.display(ivLegend, balltempLegend, null, 0);
                }
                pastTime = 60 * 60 * 1000;
                value = 2;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    drawNationDataToMap(value, LOADTYPE3);
                }
                break;
            case R.id.tvHumidity2:
                setElementEmit("3", "");
                llRain.setVisibility(View.GONE);

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

                if (!TextUtils.isEmpty(humidityLegend)) {
                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                    finalBitmap.display(ivLegend, humidityLegend, null, 0);
                }
                pastTime = 60 * 60 * 1000;
                value = 3;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    drawNationDataToMap(value, LOADTYPE3);
                }
                break;
            case R.id.tvVisibility2:
                setElementEmit("4", "");
                llRain.setVisibility(View.GONE);

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

                if (!TextUtils.isEmpty(visibilityLegend)) {
                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                    finalBitmap.display(ivLegend, visibilityLegend, null, 0);
                }
                pastTime = 60 * 60 * 1000;
                value = 4;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    drawNationDataToMap(value, LOADTYPE3);
                }
                break;
            case R.id.tvPressure2:
                setElementEmit("5", "");
                llRain.setVisibility(View.GONE);

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

                if (!TextUtils.isEmpty(airpressureLegend)) {
                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                    finalBitmap.display(ivLegend, airpressureLegend, null, 0);
                }
                pastTime = 60 * 60 * 1000;
                value = 5;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    drawNationDataToMap(value, LOADTYPE3);
                }
                break;
            case R.id.tvWindSpeed2:
                setElementEmit("2", "");
                llRain.setVisibility(View.GONE);

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

                if (!TextUtils.isEmpty(windspeedLegend)) {
                    FinalBitmap finalBitmap = FinalBitmap.create(mContext);
                    finalBitmap.display(ivLegend, windspeedLegend, null, 0);
                }
                pastTime = 60 * 60 * 1000;
                value = 6;
                if (tvProName.getVisibility() == View.VISIBLE) {
                    drawProvinceDataToMap(value, LOADTYPE3);
                } else {
                    drawNationDataToMap(value, LOADTYPE3);
                }
                break;
            case R.id.tvCheckStation:
                Intent intent = new Intent(mContext, StationMonitorDetailActivity.class);
                intent.putExtra(CONST.ACTIVITY_NAME, stationName);
                intent.putExtra("stationId", stationId);
                intent.putExtra("interface", "oneDay");
                startActivity(intent);
                break;
            case R.id.ivMapSearch:
                reContent.setVisibility(View.GONE);

                Intent intentMap = new Intent(mContext, StationMonitorSearchActivity.class);
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
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), zoom));
                } else {
                    zoom = 3.5f;
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
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
                startActivity(new Intent(mContext, StationMonitorRankActivity.class));
                break;
            case R.id.ivGuide:
                ivGuide.setVisibility(View.GONE);
                CommonUtil.saveGuidePageState(mContext, this.getClass().getName());
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
    }

    private String columnId = "";//栏目id

    /**
     * 六要素点击发送指令
     * @param id 六要素id
     * @param sid 降水小时区分
     */
    private void setElementEmit(String id, String sid) {
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
     * 点击站点发送指令
     * @param stationId
     * @param stationName
     */
    private void setPointEmit(String stationId, String stationName) {
        try {
            if (socket == null) {
                socket = MyApplication.getSocket();
            }
            if (socket != null && socket.connected()) {
                JSONObject obj = new JSONObject();
                obj.put("computerInfo", MyApplication.computerInfo);
                JSONObject commond = new JSONObject();
                commond.put("id", stationId);
                JSONObject message = new JSONObject();
                message.put("data", stationName);
                commond.put("message", message);
                obj.put("commond", commond);
                socket.emit(columnId, obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏站点详情发送指令
     */
    private void setHidePointEmit() {
        try {
            if (socket == null) {
                socket = MyApplication.getSocket();
            }
            if (socket != null && socket.connected()) {
                JSONObject obj = new JSONObject();
                obj.put("computerInfo", MyApplication.computerInfo);
                JSONObject commond = new JSONObject();
                commond.put("message", "hideMask");
                obj.put("commond", commond);
                socket.emit(columnId, obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
