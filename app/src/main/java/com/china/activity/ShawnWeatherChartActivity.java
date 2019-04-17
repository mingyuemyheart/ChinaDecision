package com.china.activity;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.china.R;
import com.china.common.CONST;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.tendcloud.tenddata.TCAgent;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 天气图分析
 */
public class ShawnWeatherChartActivity extends ShawnBaseActivity implements View.OnClickListener, AMap.OnMapScreenShotListener {

    private Context mContext;
    private TextView tvTitle,tvTime;
    private ImageView ivShare,ivLegend;
    private MapView mMapView;
    private AMap aMap;
    private boolean isShowSwitch = true;
    private LinearLayout llSwitch;
    private int swithWidth = 0;
    private TextView tv1, tv2, tv3;
    private int type = 1;
    private String result1, result2, result3;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
    private RelativeLayout reShare;
    private AVLoadingIndicatorView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_weather_chart);
        mContext = this;
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
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), 3.7f));
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);

    }

    private void refresh() {
        loadingView.setVisibility(View.VISIBLE);
        String url = "";
        if (type == 1) {
            url = "https://scapi.tianqi.cn/weather/xstu?test=ncg&type=1&hm=h000";
        }else if (type == 2) {
            url = "https://scapi.tianqi.cn/weather/xstu?test=ncg&type=1&hm=h850";
        }else if (type == 3) {
            url = "https://scapi.tianqi.cn/weather/xstu?test=ncg&type=1&hm=h500";
        }
        OkHttpData(url);
    }

    private void initWidget() {
        loadingView = findViewById(R.id.loadingView);
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = findViewById(R.id.tvTitle);
        ivShare = findViewById(R.id.ivShare);
        ivShare.setOnClickListener(this);
        ImageView ivChart = findViewById(R.id.ivChart);
        ivChart.setOnClickListener(this);
        ivLegend = findViewById(R.id.ivLegend);
        ImageView ivSwitch = findViewById(R.id.ivSwitch);
        ivSwitch.setOnClickListener(this);
        llSwitch = findViewById(R.id.llSwitch);
        tv1 = findViewById(R.id.tv1);
        tv1.setOnClickListener(this);
        tv2 = findViewById(R.id.tv2);
        tv2.setOnClickListener(this);
        tv3 = findViewById(R.id.tv3);
        tv3.setOnClickListener(this);
        tvTime = findViewById(R.id.tvTime);
        reShare = findViewById(R.id.reShare);

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        llSwitch.measure(w, h);
        swithWidth = llSwitch.getMeasuredWidth();

        String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }

        refresh();
    }

    private void OkHttpData(final String url) {
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
                        if (type == 1) {
                            result1 = result;
                        }else if (type == 2) {
                            result2 = result;
                        }else if (type == 3) {
                            result3 = result;
                        }
                        parseData(result);
                    }
                });
            }
        }).start();
    }

    private void parseData(String result) {
        aMap.clear();
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject obj = new JSONObject(result);
                if (!obj.isNull("list")) {
                    JSONArray array = obj.getJSONArray("list");
                    final String dataUrl = array.getString(0);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpUtil.enqueue(new Request.Builder().url(dataUrl).build(), new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                }
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (!response.isSuccessful()) {
                                        return;
                                    }
                                    final String result = response.body().string();
                                    if (!TextUtils.isEmpty(result)) {
                                        try {
                                            final JSONObject obj = new JSONObject(result);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (!obj.isNull("mtime")) {
                                                        try {
                                                            long mTime = obj.getLong("mtime");
                                                            tvTime.setText(sdf1.format(new Date(mTime))+"更新");
                                                            tvTime.setVisibility(View.VISIBLE);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    ivShare.setVisibility(View.VISIBLE);
                                                    reShare.setVisibility(View.VISIBLE);
                                                    loadingView.setVisibility(View.GONE);
                                                }
                                            });

                                            try {
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
//                                                                            polyline1.add(p);
                                                        }
                                                        if (!itemObj.isNull("flags")) {
                                                            JSONObject flags = itemObj.getJSONObject("flags");
                                                            String text = "";
                                                            if (!flags.isNull("text")) {
                                                                text = flags.getString("text");
                                                            }
                                                            if (!flags.isNull("items")) {
                                                                JSONArray items = flags.getJSONArray("items");
                                                                JSONObject item = items.getJSONObject(0);
                                                                double lat = item.getDouble("y");
                                                                double lng = item.getDouble("x");
                                                                TextOptions to = new TextOptions();
                                                                to.position(new LatLng(lat, lng));
                                                                to.text(text);
                                                                to.fontColor(Color.BLACK);
                                                                to.fontSize(30);
                                                                to.backgroundColor(Color.TRANSPARENT);
                                                                Text t = aMap.addText(to);
//                                                                                textList1.add(t);
                                                            }
                                                        }
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
//                                                                            polyline2.add(p);
                                                        }
                                                    }
                                                }
                                                if (!obj.isNull("symbols")) {
                                                    JSONArray symbols = obj.getJSONArray("symbols");
                                                    for (int i = 0; i < symbols.length(); i++) {
                                                        JSONObject itemObj = symbols.getJSONObject(i);
                                                        String text = "";
                                                        int color = Color.BLACK;
                                                        if (!itemObj.isNull("type")) {
                                                            String type = itemObj.getString("type");
                                                            if (TextUtils.equals(type, "60")) {
                                                                text = "H";
                                                                color = Color.RED;
                                                            }else if (TextUtils.equals(type, "61")) {
                                                                text = "L";
                                                                color = Color.BLUE;
                                                            }else if (TextUtils.equals(type, "37")) {
                                                                text = "台";
                                                                color = Color.GREEN;
                                                            }
                                                        }
                                                        double lat = itemObj.getDouble("y");
                                                        double lng = itemObj.getDouble("x");
                                                        TextOptions to = new TextOptions();
                                                        to.position(new LatLng(lat, lng));
                                                        to.text(text);
                                                        to.fontColor(color);
                                                        to.fontSize(60);
                                                        to.backgroundColor(Color.TRANSPARENT);
                                                        Text t = aMap.addText(to);
//                                                                        textList2.add(t);
                                                    }
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 隐藏或显示的动画
     */
    public void switchAnimation(final View view, final int startValue,final int endValue){
        //1.设置属性的初始值和结束值
        ValueAnimator mAnimator = ValueAnimator.ofInt(0,100);
        //2.为目标对象的属性变化设置监听器
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatorValue = (Integer) animation.getAnimatedValue();
                float fraction = animatorValue/100f;
                IntEvaluator mEvaluator = new IntEvaluator();
                //3.使用IntEvaluator计算属性值并赋值给ListView的高
                view.getLayoutParams().width = mEvaluator.evaluate(fraction, startValue, endValue);
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

    @Override
    public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
        Bitmap bitmap2 = CommonUtil.captureView(reShare);
        Bitmap bitmap3 = CommonUtil.mergeBitmap(ShawnWeatherChartActivity.this, bitmap1, bitmap2, true);
        CommonUtil.clearBitmap(bitmap1);
        CommonUtil.clearBitmap(bitmap2);
        Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
        Bitmap bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
        CommonUtil.clearBitmap(bitmap3);
        CommonUtil.clearBitmap(bitmap4);
        CommonUtil.share(ShawnWeatherChartActivity.this, bitmap);
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
                aMap.getMapScreenShot(ShawnWeatherChartActivity.this);
                break;
            case R.id.ivSwitch:
                if (isShowSwitch) {
                    isShowSwitch = false;
                    switchAnimation(llSwitch, swithWidth, 0);
                }else {
                    isShowSwitch = true;
                    switchAnimation(llSwitch, 0, swithWidth);
                }
                break;
            case R.id.tv1:
                type = 1;
                tv1.setTextColor(getResources().getColor(R.color.text_color3));
                tv2.setTextColor(getResources().getColor(R.color.text_color4));
                tv3.setTextColor(getResources().getColor(R.color.text_color4));

                if (!TextUtils.isEmpty(result1)) {
                    parseData(result1);
                }else {
                    refresh();
                }
                break;
            case R.id.tv2:
                type = 2;
                tv1.setTextColor(getResources().getColor(R.color.text_color4));
                tv2.setTextColor(getResources().getColor(R.color.text_color3));
                tv3.setTextColor(getResources().getColor(R.color.text_color4));

                if (!TextUtils.isEmpty(result2)) {
                    parseData(result2);
                }else {
                    refresh();
                }
                break;
            case R.id.tv3:
                type = 3;
                tv1.setTextColor(getResources().getColor(R.color.text_color4));
                tv2.setTextColor(getResources().getColor(R.color.text_color4));
                tv3.setTextColor(getResources().getColor(R.color.text_color3));

                if (!TextUtils.isEmpty(result3)) {
                    parseData(result3);
                }else {
                    refresh();
                }
                break;
            case R.id.ivChart:
                if (ivLegend.getVisibility() == View.VISIBLE) {
                    ivLegend.setVisibility(View.INVISIBLE);
                }else {
                    ivLegend.setVisibility(View.VISIBLE);
                }
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
        if (tvTitle != null) {
            TCAgent.onPageEnd(mContext, tvTitle.getText().toString());
        }
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
