package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.china.R;
import com.china.adapter.FiveRainAdapter;
import com.china.common.CONST;
import com.china.dto.StationMonitorDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 五天降水统计
 */
public class FiveRainActivity extends BaseActivity implements OnClickListener, AMapLocationListener, OnMapScreenShotListener {

    private Context mContext;
    private MapView mMapView;
    private AMap aMap;
    private TextView tvLayerName;
    private ListView listView;
    private FiveRainAdapter mAdapter;
    private List<StationMonitorDto> dataList = new ArrayList<>();
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日HH时", Locale.CHINA);
    private ConstraintLayout clShare;
    private float zoom = 3.5f;
    private Marker clickMarker;
    private LatLng locationLatLng = new LatLng(39.904030, 116.407526);
    private ImageView ivLegend,ivTime;
    private GroundOverlay layerOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_rain);
        mContext = this;
        showDialog();
        initWidget();
        initMap(savedInstanceState);
        initListView();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvLayerName = findViewById(R.id.tvLayerName);
        ivTime = findViewById(R.id.ivTime);
        ivTime.setOnClickListener(this);
        ImageView ivRank = findViewById(R.id.ivRank);
        ivRank.setOnClickListener(this);
        ImageView ivLocation = findViewById(R.id.ivLocation);
        ivLocation.setOnClickListener(this);
        ivLegend = findViewById(R.id.ivLegend);
        ImageView ivLegendPrompt = findViewById(R.id.ivLegendPrompt);
        ivLegendPrompt.setOnClickListener(this);
        clShare = findViewById(R.id.clShare);
        ImageView ivShare = findViewById(R.id.ivShare);
        ivShare.setOnClickListener(this);
        ivShare.setVisibility(View.VISIBLE);

        String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
        if (title != null) {
            tvTitle.setText(title);
        }

        if (CommonUtil.isLocationOpen(mContext)) {
            startLocation();
        }else {
            addLocationMarker(locationLatLng);
        }
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
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                OkHttpLayer();
                OkHttpLegend();
            }
        });
    }

    private void initListView() {
        listView = findViewById(R.id.listView);
        mAdapter = new FiveRainAdapter(this, dataList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StationMonitorDto dto = dataList.get(position);
                drawWeatherLayer(dto);
                bootTimeLayoutAnimation(listView);
            }
        });
    }

    private void OkHttpLayer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url = "https://app.tianqi.cn/tile_map/getchina5dayraincimisslayer";
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
                                cancelDialog();
                                if (!TextUtils.isEmpty(result)) {
                                    try {
                                        dataList.clear();
                                        JSONObject obj = new JSONObject(result);
                                        double leftLat = 0,leftLng = 0,rightLat = 0,rightLng = 0;
                                        if (!obj.isNull("minlat")) {
                                            leftLat = obj.getDouble("minlat");
                                        }
                                        if (!obj.isNull("minlon")) {
                                            leftLng = obj.getDouble("minlon");
                                        }
                                        if (!obj.isNull("maxlat")) {
                                            rightLat = obj.getDouble("maxlat");
                                        }
                                        if (!obj.isNull("maxlon")) {
                                            rightLng = obj.getDouble("maxlon");
                                        }
                                        if (!obj.isNull("list")) {
                                            JSONArray array = obj.getJSONArray("list");
                                            for (int i = 0; i < array.length(); i++) {
                                                StationMonitorDto dto = new StationMonitorDto();
                                                JSONObject itemObj = array.getJSONObject(i);
                                                if (!itemObj.isNull("imgurl")) {
                                                    dto.imgUrl = itemObj.getString("imgurl");
                                                }
                                                if (!itemObj.isNull("starttime")) {
                                                    dto.startTime = itemObj.getString("starttime");
                                                }
                                                if (!itemObj.isNull("endtime")) {
                                                    dto.endTime = itemObj.getString("endtime");
                                                }
                                                dto.leftLat = leftLat;
                                                dto.leftLng = leftLng;
                                                dto.rightLat = rightLat;
                                                dto.rightLng = rightLng;
                                                dataList.add(dto);

                                                if (i == 0) {
                                                    drawWeatherLayer(dto);
                                                }
                                            }
                                            if (mAdapter != null) {
                                                mAdapter.notifyDataSetChanged();
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
     * 绘制新的图层
     */
    private void drawWeatherLayer(final StationMonitorDto dto) {
        removeNewWeatherLayer();
        if (!TextUtils.isEmpty(dto.startTime) && !TextUtils.isEmpty(dto.endTime)) {
            try {
                tvLayerName.setText(sdf2.format(sdf1.parse(dto.startTime)) + " - " + sdf2.format(sdf1.parse(dto.endTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (TextUtils.isEmpty(dto.imgUrl)) {
            return;
        }
        Picasso.get().load(dto.imgUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(new LatLng(dto.leftLat, dto.leftLng))
                        .include(new LatLng(dto.rightLat, dto.rightLng))
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
     * 获取图例
     */
    private void OkHttpLegend() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url = "http://decision-admin.tianqi.cn/Home/extra/decision_skjctuli";
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
                                        if (!obj.isNull("jc_24xsjs")) {
                                            String legend = obj.getString("jc_24xsjs");
                                            if (!TextUtils.isEmpty(legend)) {
                                                Picasso.get().load(legend).into(ivLegend);
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
     * 时间图层动画
     * @param flag
     * @param view
     */
    private void timeLayoutAnimation(boolean flag, final View view) {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation animation;
        if (!flag) {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,1f);
        }else {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,1f,
                    Animation.RELATIVE_TO_SELF,0f);
        }
        animation.setDuration(300);
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
            }
        });
    }

    private void bootTimeLayoutAnimation(View view) {
        if (view == null) {
            return;
        }
        if (view.getVisibility() == View.GONE) {
            timeLayoutAnimation(true, view);
            view.setVisibility(View.VISIBLE);
            ivTime.setImageResource(R.drawable.icon_calendar_press);
        }else {
            timeLayoutAnimation(false, view);
            view.setVisibility(View.GONE);
            ivTime.setImageResource(R.drawable.icon_calendar);
        }
    }

    @Override
    public void onMapScreenShot(final Bitmap bitmap1) {
        Bitmap bitmap2 = CommonUtil.captureView(clShare);
        Bitmap bitmap3 = CommonUtil.mergeBitmap(FiveRainActivity.this, bitmap1, bitmap2, true);
        CommonUtil.clearBitmap(bitmap1);
        CommonUtil.clearBitmap(bitmap2);
        Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.legend_share_portrait);
        Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
        CommonUtil.clearBitmap(bitmap3);
        CommonUtil.clearBitmap(bitmap4);
        CommonUtil.share(FiveRainActivity.this, bitmap);
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
            case R.id.ivShare:
                aMap.getMapScreenShot(FiveRainActivity.this);
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
                startActivity(new Intent(mContext, FiveRainRankActivity.class));
                break;
            case R.id.ivTime:
                bootTimeLayoutAnimation(listView);
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
