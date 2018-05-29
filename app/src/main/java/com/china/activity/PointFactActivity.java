package com.china.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.china.R;
import com.china.common.CONST;
import com.china.dto.AirPolutionDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.tendcloud.tenddata.TCAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 格点实况
 * @author shawn_sun
 *
 */

public class PointFactActivity extends BaseActivity implements OnClickListener, OnCameraChangeListener, OnMapScreenShotListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView ivShare = null;
	private MapView mMapView = null;
	private AMap aMap = null;
	private RelativeLayout reLegend = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_fact);
		mContext = this;
		showDialog();
		initMap(savedInstanceState);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		reLegend = (RelativeLayout) findViewById(R.id.reLegend);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
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
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), 3.7f));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnCameraChangeListener(this);
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		Log.e("zoom", arg0.zoom+"");
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point startPoint = new Point(0, 0);
		Point endPoint = new Point(dm.widthPixels, dm.heightPixels);
		final LatLng start = aMap.getProjection().fromScreenLocation(startPoint);
		final LatLng end = aMap.getProjection().fromScreenLocation(endPoint);

		int zoom = (int)(arg0.zoom);
		if (zoom <= 4) {
			zoom = 4;
		}
		if (zoom >= 19) {
			zoom = 20;
		}

		String url = String.format("http://scapi.weather.com.cn/weather/getqggdsk?zoom=%s&statlonlat=%s,%s&endlonlat=%s,%s&test=ncg",
				zoom, start.longitude, start.latitude, end.longitude, end.latitude);
		handler.removeMessages(1000);
		Message msg = handler.obtainMessage(1000);
		msg.obj = url;
		handler.sendMessageDelayed(msg, 1000);
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1000:
					aMap.clear();
					OkHttpList(msg.obj+"");
					break;
			}
		}
	};

	private void OkHttpList(final String url) {
		showDialog();
		Log.e("url", url);
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
								try {
									List<AirPolutionDto> dataList = new ArrayList<>();
									JSONArray array = new JSONArray(result);
									for (int i = 0; i < array.length(); i++) {
										JSONObject itemObj = array.getJSONObject(i);
										AirPolutionDto dto = new AirPolutionDto();
										dto.latitude = itemObj.getString("LAT");
										dto.longitude = itemObj.getString("LON");
										dto.aqi = itemObj.getString("TEM");
										dataList.add(dto);
									}

									for (AirPolutionDto dto : dataList) {
										double lat = Double.valueOf(dto.latitude);
										double lng = Double.valueOf(dto.longitude);
										TextOptions options = new TextOptions();
										options.position(new LatLng(lat, lng));
										options.backgroundColor(Color.TRANSPARENT);
										options.fontColor(Color.BLUE);
										options.fontSize(25);
										options.text(dto.aqi);
										Text text = aMap.addText(options);
//										lastTexts.add(text);
									}

									cancelDialog();
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

	@Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		Bitmap bitmap = null;
		Bitmap bitmap8 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
		Bitmap bitmap2 = CommonUtil.captureView(reLegend);
		Bitmap bitmap3 = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap2, true);
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap2);
		bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap8, false);
		CommonUtil.clearBitmap(bitmap3);
		CommonUtil.clearBitmap(bitmap8);
		CommonUtil.share(PointFactActivity.this, bitmap);
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
			aMap.getMapScreenShot(PointFactActivity.this);
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
