package com.china.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.china.R;
import com.china.common.CONST;
import com.china.dto.SocietyDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 社会化观测
 * @author shawn_sun
 *
 */

public class SocietyObserveActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener,
OnMapClickListener, InfoWindowAdapter, OnMapScreenShotListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MapView mapView = null;
	private AMap aMap = null;
	private float zoom = 3.7f;
	private List<SocietyDto> mList = new ArrayList<>();
	private Marker clickMarker = null;
	private RelativeLayout reSeekBar = null;
	private ImageView ivPlay = null;
	private SeekBar seekBar = null;
	private TextView tvStartTime = null;
	private TextView tvEndTime = null;
	private SocietyThread mThread = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	private static final int HANDLER_SETMARKER_TOTOP = 1;
	private List<Marker> markerList = new ArrayList<>();
	private ImageView ivShare = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_society_observe);
		mContext = this;
		showDialog();
		initWidget();
		initAmap(savedInstanceState);
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ivPlay = (ImageView) findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		tvStartTime = (TextView) findViewById(R.id.tvStartTime);
		tvEndTime = (TextView) findViewById(R.id.tvEndTime);
		reSeekBar = (RelativeLayout) findViewById(R.id.reSeekBar);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	private OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mThread != null) {
				mThread.setCurrent(arg0.getProgress());
				mThread.stopTracking();
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			if (mThread != null) {
				mThread.startTracking();
			}
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		}
	};
	
	private void initAmap(Bundle bundle) {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setInfoWindowAdapter(this);
		
		long end = new Date().getTime();
		long start = end - 2*60*60*1000;
		tvStartTime.setText(sdf.format(new Date(start)));
		tvEndTime.setText(sdf.format(new Date(end)));
		String url = "http://dev2.rain.swarma.net/fcgi-bin/v1/user_feedback_admin.py?start="+start+"&end="+end+"&bounds=70,10,140,50&zoom=-999&source=all";
		OkHttpData(url);
	}
	
	private void OkHttpData(String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
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
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						if (!obj.isNull("data")) {
							mList.clear();
							JSONArray array = obj.getJSONArray("data");
							for (int i = 0; i < array.length(); i++) {
								SocietyDto dto = new SocietyDto();
								JSONObject itemObj = array.getJSONObject(i);
								dto.lat = itemObj.getDouble("lat");
								dto.lng = itemObj.getDouble("lon");
								dto.time = itemObj.getLong("time");
								dto.main = itemObj.getInt("main");
								JSONObject subinfo = itemObj.getJSONObject("subinfo");
								dto.type = subinfo.getInt("type");
								dto.level = subinfo.getInt("level");
								mList.add(dto);
							}
						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								cancelDialog();
								addMarker();
							}
						});
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
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
	
	private void addMarker() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int size = mList.size();
		if (size >= 500) {
			size = 500;
		}
		for (int i = 0; i < size; i++) {
			SocietyDto dto = mList.get(i);
			MarkerOptions options = new MarkerOptions();
			options.anchor(0.5f, 0.5f);
			options.position(new LatLng(dto.lat, dto.lng));
			View view = inflater.inflate(R.layout.society_marker, null);
			ImageView ivMarker = (ImageView) view.findViewById(R.id.ivMarker);
			if (dto.main == 0) {
				options.title(getString(R.string.main0));
				ivMarker.setImageResource(R.drawable.iv_society0);
			}else if (dto.main == 1) {
				options.title(getString(R.string.main1));
				ivMarker.setImageResource(R.drawable.iv_society1);
			}else if (dto.main == 2) {
				options.title(getString(R.string.main2));
				ivMarker.setImageResource(R.drawable.iv_society2);
			}else if (dto.main == 3) {
				options.title(getString(R.string.main3));
				ivMarker.setImageResource(R.drawable.iv_society3);
			}else if (dto.main == 4) {
				options.title(getString(R.string.main4));
				ivMarker.setImageResource(R.drawable.iv_society4);
			}else if (dto.main == 5) {
				options.title(getString(R.string.main5));
				ivMarker.setImageResource(R.drawable.iv_society5);
			}
			
			if (dto.type == 0) {
				String level = null;
				if (dto.level == 0) {
					level = getString(R.string.type0level0);
				}else if (dto.level == 1) {
					level = getString(R.string.type0level1);
				}else if (dto.level == 2) {
					level = getString(R.string.type0level2);
				}else if (dto.level == 3) {
					level = getString(R.string.type0level3);
				}else if (dto.level == 4) {
					level = getString(R.string.type0level4);
				}
				options.snippet(getString(R.string.type0)+": "+level);
			}else if (dto.type == 1) {
				String level = null;
				if (dto.level == 0) {
					level = getString(R.string.type1level0);
				}else if (dto.level == 1) {
					level = getString(R.string.type1level1);
				}else if (dto.level == 3) {
					level = getString(R.string.type1level3);
				}else if (dto.level == 5) {
					level = getString(R.string.type1level5);
				}
				options.snippet(getString(R.string.type1)+": "+level);
			}else if (dto.type == 2) {
				String level = null;
				if (dto.level == 0) {
					level = getString(R.string.type2level0);
				}else if (dto.level == 1) {
					level = getString(R.string.type2level1);
				}else if (dto.level == 2) {
					level = getString(R.string.type2level2);
				}else if (dto.level == 3) {
					level = getString(R.string.type2level3);
				}else if (dto.level == 4) {
					level = getString(R.string.type2level4);
				}
				options.snippet(getString(R.string.type2)+": "+level);
			}else if (dto.type == 3) {
				String level = null;
				if (dto.level == 1) {
					level = getString(R.string.type3level1);
				}else if (dto.level == 3) {
					level = getString(R.string.type3level3);
				}
				options.snippet(getString(R.string.type3)+": "+level);
			}else if (dto.type == 4) {
				String level = null;
				if (dto.level == 5) {
					level = getString(R.string.type4level5);
				}
				options.snippet(getString(R.string.type4)+": "+level);
			}else if (dto.type == 5) {
				String level = null;
				if (dto.level == 5) {
					level = getString(R.string.type5level5);
				}
				options.snippet(getString(R.string.type5)+": "+level);
			}else if (dto.type == 6) {
				String level = null;
				if (dto.level == 1) {
					level = getString(R.string.type6level1);
				}else if (dto.level == 3) {
					level = getString(R.string.type6level3);
				}
				options.snippet(getString(R.string.type6)+": "+level);
			}
			options.icon(BitmapDescriptorFactory.fromView(view));
			Marker m = aMap.addMarker(options);
			markerExpandAnimation(m);
			markerList.add(m);
		}
		
		reSeekBar.setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean onMarkerClick(Marker arg0) {
		clickMarker = arg0;
		if (arg0 == null) {
			return false;
		}
		arg0.showInfoWindow();
		return true;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		if (clickMarker != null) {
			clickMarker.hideInfoWindow();
		}
	}
	
	@Override
	public View getInfoContents(Marker arg0) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.marker_info_observe, null);
		TextView tvPhe = (TextView) view.findViewById(R.id.tvPhe);
		TextView tvInfo = (TextView) view.findViewById(R.id.tvInfo);
		tvPhe.setText(arg0.getTitle());
		tvInfo.setText(arg0.getSnippet());
		return view;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_SETMARKER_TOTOP: 
				if (msg.obj != null) {
					long time = (Long) msg.obj;
					for (int i = 0; i < markerList.size(); i++) {
						if (time == mList.get(i).time) {
							Marker marker = markerList.get(i);
							marker.setToTop();
							markerColloseAnimation(marker);
							marker.hideInfoWindow();
						}
					}
					changeProgress(msg.arg2, msg.arg1);
				}
				break;
			default:
				break;
			}
			
		};
	};
	
	private void changeProgress(int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
//		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
//		String value = time + "000";
//		Date date = new Date(Long.valueOf(value));
//		tvTime.setText(sdf.format(date));
	}
	
	private class SocietyThread extends Thread {
		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<SocietyDto> list;
		private int state;
		private int index;
		private int count;
		private boolean isTracking = false;
		
		public SocietyThread(List<SocietyDto> list) {
			this.list = list;
			this.count = list.size();
			this.index = 0;
			this.state = STATE_NONE;
			this.isTracking = false;
		}
		
		public int getCurrentState() {
			return state;
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
				if (isTracking) {
					continue;
				}
				playMarkers();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void playMarkers() {
			if (index >= count || index < 0) {
				index = 0;
			}
			
			long time = list.get(index).time;
			Message message = mHandler.obtainMessage();
			message.what = HANDLER_SETMARKER_TOTOP;
			message.obj = time;
			message.arg1 = count - 1;
			message.arg2 = index ++;
			mHandler.sendMessage(message);
		}
		
		public void cancel() {
			this.state = STATE_CANCEL;
		}
		public void pause() {
			this.state = STATE_PAUSE;
		}
		public void play() {
			this.state = STATE_PLAYING;
		}
		
		public void setCurrent(int index) {
			this.index = index;
		}
		
		public void startTracking() {
			isTracking = true;
		}
		
		public void stopTracking() {
			isTracking = false;
			if (this.state == STATE_PAUSE) {
				playMarkers();
			}
		}
	}
	
	@Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		Bitmap bitmap = null;
		Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
//		Bitmap bitmap4 = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable-hdpi/iv_share_bottom.png"));
		if (reSeekBar.getVisibility() == View.VISIBLE) {
			//bitmap2为覆盖再地图上的view
			Bitmap bitmap2 = CommonUtil.captureView(reSeekBar);
			//bitmap3为bitmap1+bitmap2覆盖叠加在一起的view
			Bitmap bitmap3 = CommonUtil.mergeBitmap(SocietyObserveActivity.this, bitmap1, bitmap2, true);
			CommonUtil.clearBitmap(bitmap2);
			bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
			CommonUtil.clearBitmap(bitmap3);
		}else {
			bitmap = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap4, false);
		}
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap4);
		CommonUtil.share(SocietyObserveActivity.this, bitmap);
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
		case R.id.ivPlay:
			if (mThread == null) {
				mThread = new SocietyThread(mList);
				mThread.start();
				ivPlay.setImageResource(R.drawable.iv_pause);
			}else {
				if (mThread.getCurrentState() == SocietyThread.STATE_PLAYING) {
					mThread.pause();
					ivPlay.setImageResource(R.drawable.iv_play);
				} else if (mThread.getCurrentState() == SocietyThread.STATE_PAUSE) {
					mThread.play();
					ivPlay.setImageResource(R.drawable.iv_pause);
				}
			}
			break;
		case R.id.ivShare:
			aMap.getMapScreenShot(SocietyObserveActivity.this);
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
