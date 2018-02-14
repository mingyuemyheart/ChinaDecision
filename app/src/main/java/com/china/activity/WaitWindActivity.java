package com.china.activity;

/**
 * 等风来
 */

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.china.dto.WindData;
import com.china.dto.WindDto;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.SecretUrlUtil;
import com.china.utils.WeatherUtil;
import com.china.view.WaitWindView2;
import com.tendcloud.tenddata.TCAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class WaitWindActivity extends BaseActivity implements OnClickListener, AMap.OnMapScreenShotListener, OnCameraChangeListener, AMap.OnMapClickListener,
        GeocodeSearch.OnGeocodeSearchListener, AMapLocationListener, AMap.InfoWindowAdapter {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private ImageView ivShare = null;
    private RelativeLayout reShare = null;
    private MapView mapView = null;
    private AMap aMap = null;
    private float zoom = 3.7f;
    private TextView tvFileTime = null;
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH");
    private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy年MM月dd日HH时");
    private RelativeLayout container = null;
    public RelativeLayout container2 = null;
    private int width = 0, height = 0;
    private WaitWindView2 waitWindView = null;
    private ImageView ivSwitch = null;
    private boolean isGfs = true;//默认为风场新数据
    private WindData windData2 = null;//gfs
    private WindData windData1 = null;//t639
    private Bundle bundle = null;
    private GeocodeSearch geocoderSearch = null;
    private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
    private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
    private Marker locationMarker = null;

    //marker infowindow
    private TextView tvPosition = null;
    private TextView tvDes = null;
    private TextView tvWind = null;
    private TextView tvAqi = null;
    private ProgressBar progressBar = null;
    private LinearLayout llMarkView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_wind);
        bundle = savedInstanceState;
        mContext = this;
        checkAuthority();
    }

    private void init() {
        showDialog();
        initAmap(bundle);
        initWidget();
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
                        init();
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
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        ivShare = (ImageView) findViewById(R.id.ivShare);
        ivShare.setOnClickListener(this);
        ivShare.setVisibility(View.VISIBLE);
        reShare = (RelativeLayout) findViewById(R.id.reShare);
        tvFileTime = (TextView) findViewById(R.id.tvFileTime);
        container = (RelativeLayout) findViewById(R.id.container);
        container2 = (RelativeLayout) findViewById(R.id.container2);
        ivSwitch = (ImageView) findViewById(R.id.ivSwitch);
        ivSwitch.setOnClickListener(this);

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

        String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
        CommonUtil.submitClickCount(columnId, title);
    }

    private void initAmap(Bundle bundle) {
        mapView = (MapView) findViewById(R.id.mapView);
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
        aMap.setInfoWindowAdapter(this);

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
            if (amapLocation.getLongitude() != 0 && amapLocation.getLatitude() != 0) {
                addLocationMarker(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
            }
        }
    }

    @Override
    public void onMapClick(LatLng arg0) {
        addLocationMarker(arg0);
    }

    private void addLocationMarker(final LatLng latLng) {
        if (latLng == null) {
            return;
        }
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        options.anchor(0.5f, 0.5f);
        Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_map_location),
                (int)(CommonUtil.dip2px(mContext, 15)), (int)(CommonUtil.dip2px(mContext, 15)));
        if (bitmap != null) {
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iv_map_location));
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
            tvPosition.setText(result.getRegeocodeAddress().getFormatAddress());
        }
    }

    @Override
    public View getInfoContents(Marker arg0) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.marker_info_waitwind, null);
        tvPosition = (TextView) view.findViewById(R.id.tvPosition);
        tvDes = (TextView) view.findViewById(R.id.tvDes);
        tvWind = (TextView) view.findViewById(R.id.tvWind);
        tvAqi = (TextView) view.findViewById(R.id.tvAqi);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        llMarkView = (LinearLayout) view.findViewById(R.id.llMarkView);
        return view;
    }

    /**
     * 获取某点的风速信息
     */
    private void OkHttpDetail(final String url) {
        progressBar.setVisibility(View.VISIBLE);
        llMarkView.setVisibility(View.INVISIBLE);
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
                                        if (!obj.isNull("air")) {
                                            JSONObject airObj = new JSONObject(obj.getString("air"));
                                            if (!airObj.isNull("k")) {
                                                JSONObject kObj = new JSONObject(airObj.getString("k"));
                                                if (!kObj.isNull("k3")) {
                                                    if (tvAqi != null) {
                                                        String aqi = WeatherUtil.lastValue(kObj.getString("k3"));
                                                        if (!TextUtils.isEmpty(aqi)) {
                                                            tvAqi.setText(aqi + " " + WeatherUtil.getAqi(mContext, Integer.valueOf(aqi)));
                                                            tvAqi.setBackgroundResource(getCornerBackground(Integer.valueOf(aqi)));
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (!obj.isNull("discript")) {
                                            JSONObject disObj = new JSONObject(obj.getString("discript"));
                                            String dis1 = disObj.getString("1");
                                            String dis2 = disObj.getString("2");
                                            String dis3 = disObj.getString("3");
                                            if (dis1 != null && dis3 != null) {
                                                tvDes.setText(dis1+"，"+dis2+"，"+dis3);
                                            }
                                        }

                                        if (!obj.isNull("forecast")) {
                                            JSONArray foreArray = obj.getJSONArray("forecast");
                                            if (foreArray.length() > 0) {
                                                JSONObject foreObj = foreArray.getJSONObject(0);

                                                String speed = "", force = "", dir = "";
                                                if (!foreObj.isNull("speed")) {
                                                    speed = foreObj.getString("speed");
                                                    if (!TextUtils.isEmpty(speed)) {
                                                        BigDecimal bd = new BigDecimal(Float.valueOf(speed));
                                                        float value = bd.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                                                        speed = value+"";
                                                    }
                                                }
                                                if (!foreObj.isNull("level")) {
                                                    force = foreObj.getString("level");
                                                }
                                                if (!foreObj.isNull("dirdes")) {
                                                    dir = foreObj.getString("dirdes");
                                                }

                                                tvWind.setText(speed+"m/s"+" "+force+"级"+"\n"+dir);
                                            }
                                        }

                                        llMarkView.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
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

    /**
     * 根据aqi数据获取相对应的背景图标
     * @param value
     * @return
     */
    private int getCornerBackground(int value) {
        int drawable = -1;
        if (value >= 0 && value <= 50) {
            drawable = R.drawable.corner_aqi_one;
        }else if (value >= 51 && value < 100) {
            drawable = R.drawable.corner_aqi_two;
        }else if (value >= 101 && value < 150) {
            drawable = R.drawable.corner_aqi_three;
        }else if (value >= 151 && value < 200) {
            drawable = R.drawable.corner_aqi_four;
        }else if (value >= 201 && value < 300) {
            drawable = R.drawable.corner_aqi_five;
        }else if (value >= 301) {
            drawable = R.drawable.corner_aqi_six;
        }
        return drawable;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    private void OkHttpGFS() {
        if (windData2 != null) {
            return;
        }
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
                                if (windData2 == null) {
                                    windData2 = new WindData();
                                }
                                if (obj != null) {
                                    if (!obj.isNull("gridHeight")) {
                                        windData2.height = obj.getInt("gridHeight");
                                    }
                                    if (!obj.isNull("gridWidth")) {
                                        windData2.width = obj.getInt("gridWidth");
                                    }
                                    if (!obj.isNull("x0")) {
                                        windData2.x0 = obj.getDouble("x0");
                                    }
                                    if (!obj.isNull("y0")) {
                                        windData2.y0 = obj.getDouble("y0");
                                    }
                                    if (!obj.isNull("x1")) {
                                        windData2.x1 = obj.getDouble("x1");
                                    }
                                    if (!obj.isNull("y1")) {
                                        windData2.y1 = obj.getDouble("y1");
                                    }
                                    if (!obj.isNull("filetime")) {
                                        windData2.filetime = obj.getString("filetime");
                                    }

                                    if (!obj.isNull("field")) {
                                        windData2.dataList.clear();
                                        JSONArray array = new JSONArray(obj.getString("field"));
                                        for (int i = 0; i < array.length(); i += 2) {
                                            WindDto dto2 = new WindDto();
                                            dto2.initX = (float) (array.optDouble(i));
                                            dto2.initY = (float) (array.optDouble(i + 1));
                                            windData2.dataList.add(dto2);
                                        }
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            cancelDialog();
                                            reloadWind(true);
                                        }
                                    });

                                }
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
        if (windData1 != null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.windT639("1000", "0")).build(), new Callback() {
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
                                if (windData1 == null) {
                                    windData1 = new WindData();
                                }
                                if (obj != null) {
                                    if (!obj.isNull("gridHeight")) {
                                        windData1.height = obj.getInt("gridHeight");
                                    }
                                    if (!obj.isNull("gridWidth")) {
                                        windData1.width = obj.getInt("gridWidth");
                                    }
                                    if (!obj.isNull("x0")) {
                                        windData1.x0 = obj.getDouble("x0");
                                    }
                                    if (!obj.isNull("y0")) {
                                        windData1.y0 = obj.getDouble("y0");
                                    }
                                    if (!obj.isNull("x1")) {
                                        windData1.x1 = obj.getDouble("x1");
                                    }
                                    if (!obj.isNull("y1")) {
                                        windData1.y1 = obj.getDouble("y1");
                                    }
                                    if (!obj.isNull("filetime")) {
                                        windData1.filetime = obj.getString("filetime");
                                    }

                                    if (!obj.isNull("field")) {
                                        windData1.dataList.clear();
                                        JSONArray array = new JSONArray(obj.getString("field"));
                                        for (int i = 0; i < array.length(); i += 2) {
                                            WindDto dto2 = new WindDto();
                                            dto2.initX = (float) (array.optDouble(i));
                                            dto2.initY = (float) (array.optDouble(i + 1));
                                            windData1.dataList.add(dto2);
                                        }
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            cancelDialog();
                                            reloadWind(false);
                                        }
                                    });

                                }
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
        if (isGfs) {
            windData2.latLngStart = latLngStart;
            windData2.latLngEnd = latLngEnd;
        }else {
            windData1.latLngStart = latLngStart;
            windData1.latLngEnd = latLngEnd;
        }
        if (waitWindView == null) {
            waitWindView = new WaitWindView2(mContext);
            waitWindView.init(WaitWindActivity.this);
            if (isGfs) {
                waitWindView.setData(windData2);
            }else {
                waitWindView.setData(windData1);
            }
            waitWindView.start();
            waitWindView.invalidate();
        }else {
            if (isGfs) {
                waitWindView.setData(windData2);
            }else {
                waitWindView.setData(windData1);
            }
        }

        container2.removeAllViews();
        container.removeAllViews();
        container.addView(waitWindView);
        tvFileTime.setVisibility(View.VISIBLE);
        String time = "";
        if (isGfs) {
            time = windData2.filetime;
            if (!TextUtils.isEmpty(time)) {
                try {
                    tvFileTime.setText("GFS "+sdf3.format(sdf2.parse(time)) + "风场预报");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }else {
            time = windData1.filetime;
            if (!TextUtils.isEmpty(time)) {
                try {
                    tvFileTime.setText("T639 "+sdf3.format(sdf2.parse(time)) + "风场预报");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
        //bitmap2为覆盖再地图上的view
        Bitmap bitmap2 = CommonUtil.captureView(reShare);
        //bitmap3为bitmap1+bitmap2覆盖叠加在一起的view
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
                    if (windData1 == null) {
                        OkHttpT639();
                    }else {
                        reloadWind(false);
                    }
                    isGfs = false;
                }else {
                    if (windData2 == null) {
                        OkHttpGFS();
                    }else {
                        reloadWind(true);
                    }
                    isGfs = true;
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

}
