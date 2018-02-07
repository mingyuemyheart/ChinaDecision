package com.china.activity;

/**
 * 城市预报
 */

import android.Manifest;
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
import android.util.Log;
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
import com.china.manager.RainManager;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.WeatherUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class CityForecastActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener,
        OnMapClickListener, OnCameraChangeListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener,
        OnMapScreenShotListener{

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private MapView mMapView = null;
    private AMap aMap = null;
    private HashMap<String, WeatherStaticsDto> proMap = new HashMap<>();//省级
    private HashMap<String, WeatherStaticsDto> cityMap = new HashMap<>();//市级
    private HashMap<String, WeatherStaticsDto> disMap = new HashMap<>();//县级
    public final static String SANX_DATA_99 = "sanx_data_99";//加密秘钥名称
    public final static String APPID = "f63d329270a44900";//机密需要用到的AppId
    private float zoom = 3.7f;
    private boolean isClick = false;//判断是否点击
    private ImageView ivShare = null;
    private ImageView ivMapSearch = null;
    private List<Marker> markerList = new ArrayList<>();
    private LatLng leftlatlng = null;
    private LatLng rightLatlng = null;
    private Marker clickMarker = null;
    private TextView tvTime = null;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日 HH时");
    private HashMap<String, WeatherDto> weatherMap = new HashMap<>();//已请求过得城市信息
    private boolean isFirstLoading = true;
    private Bundle bundle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_forecast);
        bundle = savedInstanceState;
        mContext = this;
        checkAuthority();
    }

    private void init() {
        showDialog();
        initMap(bundle);
        initWidget();
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
            for (int i = 0; i < allPermissions.length; i++) {
                if (ContextCompat.checkSelfPermission(mContext, allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(allPermissions[i]);
                }
            }
            if (deniedList.isEmpty()) {//所有权限都授予
                init();
            }else {
                String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
                ActivityCompat.requestPermissions(CityForecastActivity.this, permissions, AuthorityUtil.AUTHOR_LOCATION);
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
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
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
                    for (int i = 0; i < permissions.length; i++) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(CityForecastActivity.this, permissions[i])) {
                            AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用电话权限、存储权限，是否前往设置？");
                            break;
                        }
                    }
                }
                break;
        }
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        ivShare = (ImageView) findViewById(R.id.ivShare);
        ivShare.setOnClickListener(this);
        ivShare.setVisibility(View.VISIBLE);
        ivMapSearch = (ImageView) findViewById(R.id.ivMapSearch);
        ivMapSearch.setOnClickListener(this);
        tvTime = (TextView) findViewById(R.id.tvTime);

        String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
        if (title != null) {
            tvTitle.setText(title);
        }

        OkHttpCityForecast(getSecretUrl());
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
     * 加密请求字符串
     * @return
     */
    private String getSecretUrl() {
        String URL = "http://scapi.weather.com.cn/weather/stationinfo";//天气统计地址
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
     * 获取数据
     * @param url
     */
    private void OkHttpCityForecast(String url) {
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
                            parseStationInfo(result, "level1", proMap);
                            parseStationInfo(result, "level2", cityMap);
                            parseStationInfo(result, "level3", disMap);

                            Iterator iterator = proMap.entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry entry = (Map.Entry) iterator.next();
                                Object key = entry.getKey();
                                getWeathersInfo(key+"");
                            }
                            cancelDialog();
                        }
                    }
                });
            }
        });
    }

    /**
     * 解析数据
     */
    private void parseStationInfo(String result, String level, HashMap<String, WeatherStaticsDto> map) {
        map.clear();
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
                        dto.latitude = itemObj.getString("lat");
                    }
                    if (!itemObj.isNull("lon")) {
                        dto.longitude = itemObj.getString("lon");
                    }
                    if (TextUtils.equals(level, "level3")) {
                        if (dto.areaId.contains("10101") || dto.areaId.contains("10102") || dto.areaId.contains("10103") || dto.areaId.contains("10104")) {
                            map.put(dto.areaId, dto);
                        }
                    }else {
                        map.put(dto.areaId, dto);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取多个站点天气信息
     */
    private void getWeathersInfo(String cityId) {
        if (TextUtils.isEmpty(cityId)) {
            return;
        }

        try {
            WeatherAPI.getWeather2(mContext, cityId, Constants.Language.ZH_CN, new AsyncResponseHandler() {
                @Override
                public void onComplete(Weather content) {
                    super.onComplete(content);
                    if (content != null) {
                        try {
                            WeatherDto dto =  new WeatherDto();
                            JSONObject city = content.getCityInfo();
                            dto.cityName = city.getString("c3");
                            dto.lat = city.getString("c14");
                            dto.lng = city.getString("c13");
                            dto.cityId = city.getString("c1");

                            JSONArray weekArray = content.getWeatherForecastInfo(1);
                            JSONObject weekObj = weekArray.getJSONObject(0);

                            dto.lowPheCode = Integer.valueOf(weekObj.getString("fb"));
                            dto.lowTemp = Integer.valueOf(weekObj.getString("fd"));
                            dto.lowWindDir = Integer.valueOf(weekObj.getString("ff"));
                            dto.lowWindForce = Integer.valueOf(weekObj.getString("fh"));

                            dto.highPheCode = Integer.valueOf(weekObj.getString("fa"));
                            dto.highTemp = Integer.valueOf(weekObj.getString("fc"));
                            dto.highWindDir = Integer.valueOf(weekObj.getString("fe"));
                            dto.highWindForce = Integer.valueOf(weekObj.getString("fg"));

                            dto.publishTime = content.getForecastTime();
                            dto.isLoaded = true;

                            if (!weatherMap.containsKey(dto.cityId)) {
                                weatherMap.put(dto.cityId, dto);
                            }

                            addMarker(dto);
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    private void addMarker(final WeatherDto dto) {
        if (TextUtils.isEmpty(tvTime.getText().toString())) {
            if (!TextUtils.isEmpty(dto.publishTime)) {
                try {
                    tvTime.setText(sdf2.format(sdf1.parse(dto.publishTime))+"预报");
                    tvTime.setVisibility(View.VISIBLE);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        double lat = Double.valueOf(dto.lat);
        double lng = Double.valueOf(dto.lng);
        if (leftlatlng == null || rightLatlng == null) {
            MarkerOptions options = new MarkerOptions();
            options.title(dto.cityId);
            options.snippet(dto.cityName+","+dto.highPheCode+","+dto.highTemp+","+dto.highWindDir+","+dto.highWindForce
            +","+dto.lowPheCode+","+dto.lowTemp+","+dto.lowWindDir+","+dto.lowWindForce);
            options.anchor(0.5f, 1.0f);
            options.position(new LatLng(lat, lng));
            options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.highPheCode)));
            Marker marker = aMap.addMarker(options);
            markerList.add(marker);
            markerExpandAnimation(marker);
        }else {
            if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
                MarkerOptions options = new MarkerOptions();
                options.title(dto.cityId);
                options.snippet(dto.cityName+","+dto.highPheCode+","+dto.highTemp+","+dto.highWindDir+","+dto.highWindForce
                        +","+dto.lowPheCode+","+dto.lowTemp+","+dto.lowWindDir+","+dto.lowWindForce);
                options.anchor(0.5f, 1.0f);
                options.position(new LatLng(lat, lng));
                options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.highPheCode)));
                Marker marker = aMap.addMarker(options);
                markerList.add(marker);
                markerExpandAnimation(marker);
                Log.e("name", dto.cityName);
            }
        }
    }

    /**
     * 给marker添加文字
     * @return
     */
    private View getTextBitmap(int weatherCode) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_marker_city_forecast, null);
        if (view == null) {
            return null;
        }
        ImageView ivMarker = (ImageView) view.findViewById(R.id.ivMarker);
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

    private void removeMarkers() {
        for (int i = 0; i < markerList.size(); i++) {
            Marker marker = markerList.get(i);
            markerColloseAnimation(marker);
            marker.remove();
        }
        markerList.clear();
    }

    @Override
    public void onMapClick(LatLng arg0) {
        if (clickMarker != null) {
            clickMarker.hideInfoWindow();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != null) {
            marker.showInfoWindow();
            clickMarker = marker;
        }
        isClick = true;
        return true;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.marker_info_city_forecast, null);
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        TextView tvInfo = (TextView) view.findViewById(R.id.tvInfo);
        String[] result = marker.getSnippet().split(",");
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
        String[] result = marker.getSnippet().split(",");
        Intent intent = new Intent(mContext, ForecastActivity.class);
        intent.putExtra("cityName", result[0]);
        intent.putExtra("cityId", marker.getTitle());
        intent.putExtra("lng", marker.getPosition().latitude);
        intent.putExtra("lat", marker.getPosition().longitude);
        startActivity(intent);
    }

    @Override
    public void onCameraChange(CameraPosition arg0) {
    }

    @Override
    public void onCameraChangeFinish(CameraPosition arg0) {
        if (isFirstLoading) {
            isFirstLoading = false;
            return;
        }

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Point leftPoint = new Point(0, dm.heightPixels);
        Point rightPoint = new Point(dm.widthPixels, 0);
        leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
        rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);

        if (zoom == arg0.zoom && isClick == true) {//如果是地图缩放级别不变，并且点击就不错处理
            isClick = false;
            return;
        }

        zoom = arg0.zoom;
        removeMarkers();

        handler.removeMessages(1001);
        Message msg = handler.obtainMessage();
        msg.what = 1001;
        msg.obj = zoom;
        handler.sendMessageDelayed(msg, 1001);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    float arg0 = (float)msg.obj;
                    moveMap(arg0);
                    break;
            }
        }
    };

    private void moveMap(float arg0) {
        if (arg0 <= 6.0f) {
            Iterator iterator = proMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object key = entry.getKey();
                if (weatherMap.containsKey(key)) {
                    addMarker(weatherMap.get(key));
                }else {
                    getWeathersInfo(key+"");
                }
            }
        }else if (arg0 > 6.0f && arg0 <= 8.0f) {
            Iterator iterator = proMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object key = entry.getKey();
                if (weatherMap.containsKey(key)) {
                    addMarker(weatherMap.get(key));
                }else {
                    getWeathersInfo(key+"");
                }
            }
            Iterator iteratorCity = cityMap.entrySet().iterator();
            while (iteratorCity.hasNext()) {
                Map.Entry entry = (Map.Entry) iteratorCity.next();
                Object key = entry.getKey();
                if (weatherMap.containsKey(key)) {
                    addMarker(weatherMap.get(key));
                }else {
                    getWeathersInfo(key+"");
                }
            }
        } else if (arg0 > 8.0f) {
            Iterator iterator = proMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object key = entry.getKey();
                if (weatherMap.containsKey(key)) {
                    addMarker(weatherMap.get(key));
                }else {
                    getWeathersInfo(key+"");
                }
            }
            Iterator iteratorCity = cityMap.entrySet().iterator();
            while (iteratorCity.hasNext()) {
                Map.Entry entry = (Map.Entry) iteratorCity.next();
                Object key = entry.getKey();
                if (weatherMap.containsKey(key)) {
                    addMarker(weatherMap.get(key));
                }else {
                    getWeathersInfo(key+"");
                }
            }
            Iterator iteratorDis = disMap.entrySet().iterator();
            while (iteratorDis.hasNext()) {
                Map.Entry entry = (Map.Entry) iteratorDis.next();
                Object key = entry.getKey();
                if (weatherMap.containsKey(key)) {
                    addMarker(weatherMap.get(key));
                }else {
                    getWeathersInfo(key+"");
                }
            }
        }
    }

    @Override
    public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
        Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
        Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap4, false);
        CommonUtil.clearBitmap(bitmap1);
        CommonUtil.clearBitmap(bitmap4);
        CommonUtil.share(CityForecastActivity.this, bitmap);
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
                aMap.getMapScreenShot(CityForecastActivity.this);
                break;
            case R.id.ivMapSearch:
                Intent intent = new Intent(mContext, CityActivity.class);
//                intent.putExtra(CONST.COLUMN_ID, dto.columnId);
                intent.putExtra(CONST.ACTIVITY_NAME, "城市查询");
                startActivity(intent);
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

}
