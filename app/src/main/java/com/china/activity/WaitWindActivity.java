package com.china.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
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
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.china.R;
import com.china.common.CONST;
import com.china.dto.TyphoonDto;
import com.china.dto.WindData;
import com.china.dto.WindDto;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.SecretUrlUtil;
import com.china.view.WaitWindView2;
import com.china.view.WindForeView;
import com.tendcloud.tenddata.TCAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 等风来
 */
public class WaitWindActivity extends BaseActivity implements OnClickListener, AMap.OnMapScreenShotListener, OnCameraChangeListener, AMap.OnMapClickListener,
        AMap.OnMarkerClickListener, GeocodeSearch.OnGeocodeSearchListener, AMapLocationListener {

    private Context mContext;
    private LinearLayout llBack,llHeight,llContainer1;
    private TextView tvTitle,tvFileTime,tvHeight200,tvHeight500,tvHeight1000,tvLocation;
    private ImageView ivHeight,ivSwitch,ivLocation,ivClose,ivShare;
    private RelativeLayout reDetail,reShare;
    private MapView mapView;
    private AMap aMap;
    private float zoom = 3.7f;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH");
    private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy年MM月dd日HH时");
    private RelativeLayout container;
    public RelativeLayout container2;
    private int width = 0, height = 0;
    private WaitWindView2 waitWindView;
    private boolean isGfs = true;//默认为风场新数据
    private WindData windDataGFS,windDataT639;
    private GeocodeSearch geocoderSearch;
    private AMapLocationClientOption mLocationOption;//声明mLocationOption对象
    private AMapLocationClient mLocationClient;//声明AMapLocationClient类对象
    private Marker locationMarker,selectMarker;
    private double locationLat = 35.926628, locationLng = 105.178100;
    private String dataHeight = "1000";
    private List<TyphoonDto> typhoonList = new ArrayList<>();
    private List<Marker> typhoonMarkers = new ArrayList<>();//台风markers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_wind);
        mContext = this;
        initAmap(savedInstanceState);
        checkAuthority();
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
            initWidget();
        }else {
            deniedList.clear();
            for (int i = 0; i < allPermissions.length; i++) {
                if (ContextCompat.checkSelfPermission(mContext, allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(allPermissions[i]);
                }
            }
            if (deniedList.isEmpty()) {//所有权限都授予
                initWidget();
            }else {
                String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
                ActivityCompat.requestPermissions(WaitWindActivity.this, permissions, AuthorityUtil.AUTHOR_LOCATION);
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
                        initWidget();
                    }else {//只要有一个没有授权，就提示进入设置界面设置
                        AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、存储权限，是否前往设置？");
                    }
                }else {
                    for (int i = 0; i < permissions.length; i++) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(WaitWindActivity.this, permissions[i])) {
                            AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、存储权限，是否前往设置？");
                            break;
                        }
                    }
                }
                break;
        }
    }

    private void initWidget() {
        showDialog();

        llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = findViewById(R.id.tvTitle);
        ivShare = findViewById(R.id.ivShare);
        ivShare.setOnClickListener(this);
        ivShare.setVisibility(View.VISIBLE);
        reShare = findViewById(R.id.reShare);
        tvFileTime = findViewById(R.id.tvFileTime);
        container = findViewById(R.id.container);
        container2 = findViewById(R.id.container2);
        ivSwitch = findViewById(R.id.ivSwitch);
        ivSwitch.setOnClickListener(this);
        ivLocation = findViewById(R.id.ivLocation);
        ivLocation.setOnClickListener(this);
        ivHeight = findViewById(R.id.ivHeight);
        ivHeight.setOnClickListener(this);
        llHeight = findViewById(R.id.llHeight);
        tvHeight200 = findViewById(R.id.tvHeight200);
        tvHeight200.setOnClickListener(this);
        tvHeight500 = findViewById(R.id.tvHeight500);
        tvHeight500.setOnClickListener(this);
        tvHeight1000 = findViewById(R.id.tvHeight1000);
        tvHeight1000.setOnClickListener(this);
        reDetail = findViewById(R.id.reDetail);
        tvLocation = findViewById(R.id.tvLocation);
        llContainer1 = findViewById(R.id.llContainer1);
        ivClose = findViewById(R.id.ivClose);
        ivClose.setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

        String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
        if (title != null) {
            tvTitle.setText(title);
        }

        startLocation();
        OkHttpGFS();

//        int currentYear = Integer.valueOf(sdf1.format(new Date()));
//        OkHttpTyphoonList("http://decision-admin.tianqi.cn/Home/extra/gettyphoon/list/"+currentYear);

        String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
        CommonUtil.submitClickCount(columnId, title);
    }

    private void initAmap(Bundle bundle) {
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(bundle);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);

        geocoderSearch = new GeocodeSearch(mContext);
        geocoderSearch.setOnGeocodeSearchListener(this);
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
            addLocationMarker(new LatLng(locationLat, locationLng));
        }
    }

    @Override
    public void onMapClick(LatLng arg0) {
        if (selectMarker != null) {
            selectMarker.hideInfoWindow();
        }
        addLocationMarker(arg0);
    }

    private void addLocationMarker(final LatLng latLng) {
        if (latLng == null) {
            return;
        }
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        options.anchor(0.5f, 1.0f);
        Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_map_click_map),
                (int)(CommonUtil.dip2px(mContext, 21)), (int)(CommonUtil.dip2px(mContext, 32)));
        if (bitmap != null) {
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iv_map_click_map));
        }
        if (locationMarker != null) {
            locationMarker.remove();
        }
        locationMarker = aMap.addMarker(options);
        locationMarker.showInfoWindow();

        //latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latLng.latitude, latLng.longitude), 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                OkHttpDetail(SecretUrlUtil.windDetail(latLng.longitude, latLng.latitude));
            }
        }, 1000);

    }

    @Override
    public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
    }
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (result != null && result.getRegeocodeAddress().getFormatAddress() != null) {
            tvLocation.setText(result.getRegeocodeAddress().getFormatAddress());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != locationMarker) {
            selectMarker = marker;
            marker.showInfoWindow();
        }
        return true;
    }

    /**
     * 获取某点的风速信息
     */
    private void OkHttpDetail(final String url) {
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
                                        if (!obj.isNull("forecast")) {
                                            List<WindDto> windList = new ArrayList<>();
                                            JSONArray array = obj.getJSONArray("forecast");
                                            for (int i = 0; i < array.length(); i++) {
                                                JSONObject itemObj = array.getJSONObject(i);
                                                WindDto dto = new WindDto();
                                                if (!itemObj.isNull("speed")) {
                                                    dto.speed = itemObj.getString("speed");
                                                }
                                                if (!itemObj.isNull("date")) {
                                                    dto.date = itemObj.getString("date");
                                                }
                                                windList.add(dto);
                                            }

                                            llContainer1.removeAllViews();
                                            WindForeView cubicView = new WindForeView(mContext);
                                            cubicView.setData(windList);
                                            llContainer1.addView(cubicView, width, height/3);
                                            legendAnimation(false, reDetail);
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

    private void OkHttpGFS() {
        if (windDataGFS != null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.windGFS(dataHeight)).build(), new Callback() {
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
                                if (windDataGFS == null) {
                                    windDataGFS = new WindData();
                                }
                                if (!obj.isNull("gridHeight")) {
                                    windDataGFS.height = obj.getInt("gridHeight");
                                }
                                if (!obj.isNull("gridWidth")) {
                                    windDataGFS.width = obj.getInt("gridWidth");
                                }
                                if (!obj.isNull("x0")) {
                                    windDataGFS.x0 = obj.getDouble("x0");
                                }
                                if (!obj.isNull("y0")) {
                                    windDataGFS.y0 = obj.getDouble("y0");
                                }
                                if (!obj.isNull("x1")) {
                                    windDataGFS.x1 = obj.getDouble("x1");
                                }
                                if (!obj.isNull("y1")) {
                                    windDataGFS.y1 = obj.getDouble("y1");
                                }
                                if (!obj.isNull("filetime")) {
                                    windDataGFS.filetime = obj.getString("filetime");
                                }

                                if (!obj.isNull("field")) {
                                    windDataGFS.dataList.clear();
                                    JSONArray array = new JSONArray(obj.getString("field"));
                                    for (int i = 0; i < array.length(); i += 2) {
                                        WindDto dto2 = new WindDto();
                                        dto2.initX = (float) (array.optDouble(i));
                                        dto2.initY = (float) (array.optDouble(i + 1));
                                        windDataGFS.dataList.add(dto2);
                                    }
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        cancelDialog();
                                        reloadWind(true);
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

    private void OkHttpT639() {
        if (windDataT639 != null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.windT639(dataHeight, "0")).build(), new Callback() {
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
                                if (windDataT639 == null) {
                                    windDataT639 = new WindData();
                                }
                                if (!obj.isNull("gridHeight")) {
                                    windDataT639.height = obj.getInt("gridHeight");
                                }
                                if (!obj.isNull("gridWidth")) {
                                    windDataT639.width = obj.getInt("gridWidth");
                                }
                                if (!obj.isNull("x0")) {
                                    windDataT639.x0 = obj.getDouble("x0");
                                }
                                if (!obj.isNull("y0")) {
                                    windDataT639.y0 = obj.getDouble("y0");
                                }
                                if (!obj.isNull("x1")) {
                                    windDataT639.x1 = obj.getDouble("x1");
                                }
                                if (!obj.isNull("y1")) {
                                    windDataT639.y1 = obj.getDouble("y1");
                                }
                                if (!obj.isNull("filetime")) {
                                    windDataT639.filetime = obj.getString("filetime");
                                }

                                if (!obj.isNull("field")) {
                                    windDataT639.dataList.clear();
                                    JSONArray array = new JSONArray(obj.getString("field"));
                                    for (int i = 0; i < array.length(); i += 2) {
                                        WindDto dto2 = new WindDto();
                                        dto2.initX = (float) (array.optDouble(i));
                                        dto2.initY = (float) (array.optDouble(i + 1));
                                        windDataT639.dataList.add(dto2);
                                    }
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        cancelDialog();
                                        reloadWind(false);
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
        if (isGfs) {
            reloadWind(true);
        }else {
            reloadWind(false);
        }
    }

    long t = new Date().getTime();

    /**
     * 重新加载风场
     */
    private void reloadWind(boolean isGfs) {
        t = new Date().getTime() - t;
        if (t < 1000) {
            return;
        }

        LatLng latLngStart = aMap.getProjection().fromScreenLocation(new Point(0, 0));
        LatLng latLngEnd = aMap.getProjection().fromScreenLocation(new Point(width, height));
        Log.e("latLng", latLngStart.latitude+","+latLngStart.longitude+"\n"+latLngEnd.latitude+","+latLngEnd.longitude);
        if (isGfs) {
            windDataGFS.latLngStart = latLngStart;
            windDataGFS.latLngEnd = latLngEnd;
        }else {
            windDataT639.latLngStart = latLngStart;
            windDataT639.latLngEnd = latLngEnd;
        }
        if (waitWindView == null) {
            waitWindView = new WaitWindView2(mContext);
            waitWindView.init(WaitWindActivity.this);
            if (isGfs) {
                waitWindView.setData(windDataGFS);
            }else {
                waitWindView.setData(windDataT639);
            }
            waitWindView.start();
            waitWindView.invalidate();
        }else {
            if (isGfs) {
                waitWindView.setData(windDataGFS);
            }else {
                waitWindView.setData(windDataT639);
            }
        }

        container2.removeAllViews();
        container.removeAllViews();
        container.addView(waitWindView);
        tvFileTime.setVisibility(View.VISIBLE);
        String time;
        if (isGfs) {
            time = windDataGFS.filetime;
            if (!TextUtils.isEmpty(time)) {
                try {
                    tvFileTime.setText("GFS "+sdf3.format(sdf2.parse(time)) + "风场预报");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }else {
            time = windDataT639.filetime;
            if (!TextUtils.isEmpty(time)) {
                try {
                    tvFileTime.setText("T639 "+sdf3.format(sdf2.parse(time)) + "风场预报");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void legendAnimation(final boolean flag, final View view) {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation animation;
        if (flag == false) {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, -1f,
                    Animation.RELATIVE_TO_SELF, 0);
        }else {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,-1.0f);
        }
        animation.setDuration(400);
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        view.startAnimation(animationSet);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                view.clearAnimation();
                if (!flag) {
                    view.setVisibility(View.VISIBLE);
                }else {
                    view.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
        Bitmap bitmap2 = CommonUtil.captureView(reShare);
        Bitmap bitmap3 = CommonUtil.mergeBitmap(WaitWindActivity.this, bitmap1, bitmap2, true);
        CommonUtil.clearBitmap(bitmap1);
        CommonUtil.clearBitmap(bitmap2);
        Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
        Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
        CommonUtil.clearBitmap(bitmap3);
        CommonUtil.clearBitmap(bitmap4);
        CommonUtil.share(WaitWindActivity.this, bitmap);
    }

    @Override
    public void onMapScreenShot(Bitmap bitmap, int i) {

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
                aMap.getMapScreenShot(WaitWindActivity.this);
                break;
            case R.id.ivSwitch:
                if (isGfs) {
                    ivSwitch.setImageResource(R.drawable.icon_switch_data);
                    windDataT639 = null;
                    OkHttpT639();
                }else {
                    ivSwitch.setImageResource(R.drawable.icon_switch_dataoff);
                    windDataGFS = null;
                    OkHttpGFS();
                }
                isGfs = !isGfs;
                break;
            case R.id.ivLocation:
                if (zoom >= 12.f) {
                    ivLocation.setImageResource(R.drawable.icon_locationoff);
                    zoom = 3.7f;
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), zoom));
                }else {
                    ivLocation.setImageResource(R.drawable.icon_location);
                    zoom = 12.0f;
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), zoom));
                }
                break;
            case R.id.ivHeight:
                if (llHeight.getVisibility() == View.VISIBLE) {
                    llHeight.setVisibility(View.GONE);
                    ivHeight.setImageResource(R.drawable.icon_wind_heightoff);
                }else {
                    llHeight.setVisibility(View.VISIBLE);
                    ivHeight.setImageResource(R.drawable.icon_wind_height);
                }
                break;
            case R.id.tvHeight200:
                tvHeight200.setTextColor(getResources().getColor(R.color.blue));
                tvHeight500.setTextColor(getResources().getColor(R.color.text_color4));
                tvHeight1000.setTextColor(getResources().getColor(R.color.text_color4));
                tvHeight200.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                tvHeight500.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                tvHeight1000.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                dataHeight = "200";
                if (isGfs) {
                    windDataGFS = null;
                    OkHttpGFS();
                }else {
                    windDataT639 = null;
                    OkHttpT639();
                }
                break;
            case R.id.tvHeight500:
                tvHeight200.setTextColor(getResources().getColor(R.color.text_color4));
                tvHeight500.setTextColor(getResources().getColor(R.color.blue));
                tvHeight1000.setTextColor(getResources().getColor(R.color.text_color4));
                tvHeight200.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                tvHeight500.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                tvHeight1000.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                dataHeight = "500";
                if (isGfs) {
                    windDataGFS = null;
                    OkHttpGFS();
                }else {
                    windDataT639 = null;
                    OkHttpT639();
                }
                break;
            case R.id.tvHeight1000:
                tvHeight200.setTextColor(getResources().getColor(R.color.text_color4));
                tvHeight500.setTextColor(getResources().getColor(R.color.text_color4));
                tvHeight1000.setTextColor(getResources().getColor(R.color.blue));
                tvHeight200.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                tvHeight500.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                tvHeight1000.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                dataHeight = "1000";
                if (isGfs) {
                    windDataGFS = null;
                    OkHttpGFS();
                }else {
                    windDataT639 = null;
                    OkHttpT639();
                }
                break;
            case R.id.ivClose:
                if (reDetail.getVisibility() == View.GONE) {
                    legendAnimation(false, reDetail);
                }else {
                    legendAnimation(true, reDetail);
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
    }

    /**
     * 获取当年的台风列表信息
     */
    private void OkHttpTyphoonList(final String url) {
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
                                                        String name;
                                                        if (TextUtils.equals(dto.enName, "nameless")) {
                                                            name = dto.code + " " + dto.enName;
                                                        }else {
                                                            name = dto.code + " " + dto.name + " " + dto.enName;
                                                        }
                                                        OkHttpTyphoonDetail("http://decision-admin.tianqi.cn/Home/extra/gettyphoon/view/"+dto.id, name);
                                                    }
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
                                                    String time = sdf2.format(new Date(longTime));
                                                    dto.time = time;
//									String time = itemArray2.getString(1);
                                                    String str_year = time.substring(0, 4);
                                                    if(!TextUtils.isEmpty(str_year)){
                                                        dto.year = Integer.parseInt(str_year);
                                                    }
                                                    String str_month = time.substring(4, 6);
                                                    if(!TextUtils.isEmpty(str_month)){
                                                        dto.month = Integer.parseInt(str_month);
                                                    }
                                                    String str_day = time.substring(6, 8);
                                                    if(!TextUtils.isEmpty(str_day)){
                                                        dto.day = Integer.parseInt(str_day);
                                                    }
                                                    String str_hour = time.substring(8, 10);
                                                    if(!TextUtils.isEmpty(str_hour)){
                                                        dto.hour = Integer.parseInt(str_hour);
                                                    }

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
                                                            dto.en_radius_7 = itemArray10.getString(1);
                                                            dto.es_radius_7 = itemArray10.getString(2);
                                                            dto.wn_radius_7 = itemArray10.getString(3);
                                                            dto.ws_radius_7 = itemArray10.getString(4);
                                                        }else if (m == 1) {
                                                            dto.radius_10 = itemArray10.getString(1);
                                                            dto.en_radius_10 = itemArray10.getString(1);
                                                            dto.es_radius_10 = itemArray10.getString(2);
                                                            dto.wn_radius_10 = itemArray10.getString(3);
                                                            dto.ws_radius_10 = itemArray10.getString(4);
                                                        }
                                                    }
//									points.add(dto);

                                                    MarkerOptions tOption = new MarkerOptions();
                                                    tOption.title(name+"|"+dto.content(mContext));
                                                    tOption.position(new LatLng(dto.lat, dto.lng));
                                                    tOption.anchor(0.5f, 0.5f);
                                                    ArrayList<BitmapDescriptor> iconList = new ArrayList<>();
                                                    for (int i = 1; i <= 9; i++) {
                                                        iconList.add(BitmapDescriptorFactory.fromAsset("typhoon/typhoon_icon"+i+".png"));
                                                    }
                                                    tOption.icons(iconList);
                                                    tOption.period(2);
                                                    Marker marker = aMap.addMarker(tOption);
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

}
