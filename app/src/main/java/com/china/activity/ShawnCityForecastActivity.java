package com.china.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
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
import com.china.dto.WeatherDto;
import com.china.dto.WeatherStaticsDto;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.SecretUrlUtil;
import com.china.utils.WeatherUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 城市天气预报
 */
public class ShawnCityForecastActivity extends ShawnBaseActivity implements OnClickListener, OnMarkerClickListener,
        OnMapClickListener, OnCameraChangeListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener, OnMapScreenShotListener{

    private Context mContext;
    private MapView mMapView;
    private AMap aMap;
    private List<WeatherStaticsDto> level1List = new ArrayList<>();
    private List<WeatherStaticsDto> level2List = new ArrayList<>();
    private Map<String, Marker> markerMap = new LinkedHashMap<>();//按区域id区分
    private final String level1 = "level1", level2 = "level2", level3 = "level3";
    private float zoom = 3.7f, zoom1 = 7.0f;
    private LatLng leftlatlng = new LatLng(-16.305714763804854,75.13831436634065);
    private LatLng rightLatlng = new LatLng(63.681687310440864,135.21788656711578);
    private Marker clickMarker;
    private TextView tvTime;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日 HH时", Locale.CHINA);
    private SimpleDateFormat sdf3 = new SimpleDateFormat("HH", Locale.CHINA);
    private Bundle savedInstanceState;
    private AVLoadingIndicatorView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_city_forecast);
        this.savedInstanceState = savedInstanceState;
        mContext = this;
        checkAuthority();
    }

    private void init() {
        initMap(savedInstanceState);
        initWidget();
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
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnMapClickListener(this);
        aMap.setOnCameraChangeListener(this);
        aMap.setInfoWindowAdapter(this);
        aMap.setOnInfoWindowClickListener(this);
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        loadingView = findViewById(R.id.loadingView);
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        ImageView ivShare = findViewById(R.id.ivShare);
        ivShare.setOnClickListener(this);
        ivShare.setVisibility(View.VISIBLE);
        ImageView ivMapSearch = findViewById(R.id.ivMapSearch);
        ivMapSearch.setOnClickListener(this);
        ivMapSearch.setVisibility(View.VISIBLE);
        tvTime = findViewById(R.id.tvTime);

        String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }

        OkHttpList();
    }

    /**
     * 获取所有站点信息
     */
    private void OkHttpList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.statistic()).build(), new Callback() {
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
                            level1List.clear();
                            level2List.clear();
                            parseStationInfo(result, level1);
                            parseStationInfo(result, level2);
                            parseStationInfo(result, level3);
                            addMarkers();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingView.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 解析数据
     */
    private void parseStationInfo(String result, String level) {
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
                        dto.lat = itemObj.getDouble("lat");
                    }
                    if (!itemObj.isNull("lon")) {
                        dto.lng = itemObj.getDouble("lon");
                    }

                    if (TextUtils.equals(level, level1)) {
                        level1List.add(dto);
                    }else if (TextUtils.equals(level, level2)) {
                        level2List.add(dto);
                    }else if (TextUtils.equals(level, level3)) {//把四个直辖市的区划入地市级
                        if (dto.areaId.contains("10101") || dto.areaId.contains("10102") || dto.areaId.contains("10103") || dto.areaId.contains("10104")) {
                            dto.level = "2";
                            level2List.add(dto);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void switchMarkers() {
        for (String areaId : markerMap.keySet()) {
            if (!TextUtils.isEmpty(areaId) && markerMap.containsKey(areaId)) {
                Marker marker = markerMap.get(areaId);
                String level = marker.getSnippet();
                double lat = marker.getPosition().latitude;
                double lng = marker.getPosition().longitude;
                if (zoom <= zoom1) {
                    if (TextUtils.equals(level, "2") || TextUtils.equals(level, "3")) {
                        marker.remove();
                    }
                }
                if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
                    //已经在可是范围内则不处理
                }else {
                    marker.remove();
                }
            }
        }
    }

    /**
     * 添加marker
     */
    private void addMarkers() {
        List<WeatherStaticsDto> list = new ArrayList<>();
        if (zoom <= zoom1) {
            list.addAll(level1List);
        }else {
            list.addAll(level1List);
            list.addAll(level2List);
        }

        for (WeatherStaticsDto dto : list) {
            if (markerMap.containsKey(dto.areaId)) {
                Marker m = markerMap.get(dto.areaId);
                if (m != null && m.isVisible()) {
                    //已经在可是区域添加过了，就不重复绘制了
                }else {
                    addVisibleAreaMarker(dto);
                }
            }else {
                addVisibleAreaMarker(dto);
            }
        }
    }

    /**
     * 添加可视区域对应的marker
     * @param dto
     */
    private void addVisibleAreaMarker(WeatherStaticsDto dto) {
        if (dto.lat > leftlatlng.latitude && dto.lat < rightLatlng.latitude && dto.lng > leftlatlng.longitude && dto.lng < rightLatlng.longitude) {
            WeatherDto weatherDto = new WeatherDto();
            weatherDto.cityId = dto.areaId;
            weatherDto.cityName = dto.name;
            weatherDto.lat = dto.lat;
            weatherDto.lng = dto.lng;
            weatherDto.level = dto.level;
            getWeathersInfo(weatherDto);
        }
    }

    /**
     * 获取站点天气信息
     */
    private void getWeathersInfo(final WeatherDto dto) {
        if (TextUtils.isEmpty(dto.cityId)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(String.format("https://videoshfcx.tianqi.cn/dav_tqwy/ty_weather/data/%s.html", dto.cityId)).build(), new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    }
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
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

                                        //15天预报
                                        if (!obj.isNull("f")) {
                                            JSONObject f = obj.getJSONObject("f");

                                            final String f0 = f.getString("f0");
                                            if (!TextUtils.isEmpty(f0)) {
                                                try {
                                                    tvTime.setText(sdf2.format(sdf1.parse(f0)));
                                                    tvTime.setVisibility(View.VISIBLE);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            if (!f.isNull("f1")) {
                                                JSONArray f1 = f.getJSONArray("f1");
                                                JSONObject weeklyObj = f1.getJSONObject(0);
                                                //晚上
                                                dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
                                                dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
                                                dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"));
                                                dto.lowWindDir = Integer.valueOf(weeklyObj.getString("ff"));
                                                dto.lowWindForce = Integer.valueOf(weeklyObj.getString("fh"));

                                                //白天
                                                dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
                                                dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
                                                dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"));
                                                dto.highWindDir = Integer.valueOf(weeklyObj.getString("fe"));
                                                dto.highWindForce = Integer.valueOf(weeklyObj.getString("fg"));

                                                MarkerOptions options = new MarkerOptions();
                                                options.title(dto.cityName+","+dto.highPheCode+","+dto.highTemp+","+dto.highWindDir+","+dto.highWindForce
                                                        +","+dto.lowPheCode+","+dto.lowTemp+","+dto.lowWindDir+","+dto.lowWindForce+","+dto.cityId);
                                                options.snippet(dto.level);
                                                options.anchor(0.5f, 1.0f);
                                                options.position(new LatLng(dto.lat, dto.lng));
                                                int currentHour = Integer.valueOf(sdf3.format(new Date()));
                                                if (currentHour >= 5 && currentHour < 18) {
                                                    options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.highPheCode)));
                                                }else {
                                                    options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.lowPheCode)));
                                                }
                                                Marker marker = aMap.addMarker(options);
                                                markerMap.put(dto.cityId, marker);
                                                markerExpandAnimation(marker);

                                            }
                                        }
                                    }catch (JSONException e) {
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
     * 给marker添加文字
     * @return
     */
    private View getTextBitmap(int weatherCode) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shawn_fore_marker_icon, null);
        if (view == null) {
            return null;
        }
        ImageView ivMarker = view.findViewById(R.id.ivMarker);
        ivMarker.setImageBitmap(WeatherUtil.getDayBitmap(mContext, weatherCode));
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

        zoom = arg0.zoom;

        handler.removeMessages(1001);
        Message msg = handler.obtainMessage();
        msg.what = 1001;
        handler.sendMessageDelayed(msg, 500);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    switchMarkers();
                    addMarkers();
                    break;
            }
        }
    };

    @Override
    public void onMapClick(LatLng arg0) {
        if (clickMarker != null && clickMarker.isInfoWindowShown()) {
            clickMarker.hideInfoWindow();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != null) {
            clickMarker = marker;
            if (clickMarker.isInfoWindowShown()) {
                clickMarker.hideInfoWindow();
            }else {
                marker.showInfoWindow();
            }
        }
        return true;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shawn_fore_marker_icon_info, null);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvInfo = view.findViewById(R.id.tvInfo);
        String[] result = marker.getTitle().split(",");
        tvName.setText(result[0]);
        String phe = getString(WeatherUtil.getWeatherId(Integer.valueOf(result[1]))) + "~" + getString(WeatherUtil.getWeatherId(Integer.valueOf(result[5])));
        if (TextUtils.equals(result[1], result[5])) {
            phe = getString(WeatherUtil.getWeatherId(Integer.valueOf(result[1])));
        }
        String temp = result[2]+"~"+result[6]+"℃";
        String windDir = getString(WeatherUtil.getWindDirection(Integer.valueOf(result[3])));
        String windForce = WeatherUtil.getFactWindForce(Integer.valueOf(result[4]));
        tvInfo.setText(phe+"\n"+temp+"\n"+windDir+windForce);
        return view;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String[] result = marker.getTitle().split(",");
        Intent intent = new Intent(mContext, ShawnForecastActivity.class);
        intent.putExtra("cityName", result[0]);
        intent.putExtra("cityId", result[9]);
        startActivity(intent);
    }

    @Override
    public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
        Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
        Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap4, false);
        CommonUtil.clearBitmap(bitmap1);
        CommonUtil.clearBitmap(bitmap4);
        CommonUtil.share(this, bitmap);
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
            case R.id.ivShare:
                aMap.getMapScreenShot(this);
                break;
            case R.id.ivMapSearch:
                startActivity(new Intent(mContext, CityActivity.class));
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
            Manifest.permission.CALL_PHONE,
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
                ActivityCompat.requestPermissions(ShawnCityForecastActivity.this, permissions, AuthorityUtil.AUTHOR_LOCATION);
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
                        AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用电话权限、存储权限，是否前往设置？");
                    }
                }else {
                    for (String permission : permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(ShawnCityForecastActivity.this, permission)) {
                            AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用电话权限、存储权限，是否前往设置？");
                            break;
                        }
                    }
                }
                break;
        }
    }

}
