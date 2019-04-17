package com.china.activity;

import android.annotation.SuppressLint;
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
import com.tendcloud.tenddata.TCAgent;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 社会化观测
 */
public class ShawnSocietyObserveActivity extends ShawnBaseActivity implements OnClickListener, OnMarkerClickListener,
OnMapClickListener, InfoWindowAdapter, OnMapScreenShotListener{
	
	private Context mContext;
	private TextView tvTitle;
	private MapView mapView;
	private AMap aMap;
	private List<SocietyDto> dataList = new ArrayList<>();
	private Marker clickMarker;
	private RelativeLayout reSeekBar;
	private ImageView ivPlay;
	private SeekBar seekBar;
	private SocietyThread mThread;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);
	private static final int HANDLER_SETMARKER_TOTOP = 1;
	private List<Marker> markerList = new ArrayList<>();
	private AVLoadingIndicatorView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_society_observe);
		mContext = this;
		initAmap(savedInstanceState);
		initWidget();
	}

	private void initAmap(Bundle bundle) {
		mapView = findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), 3.7f));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setInfoWindowAdapter(this);
	}
	
	private void initWidget() {
		loadingView = findViewById(R.id.loadingView);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		ivPlay = findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		TextView tvStartTime = findViewById(R.id.tvStartTime);
		TextView tvEndTime = findViewById(R.id.tvEndTime);
		reSeekBar = findViewById(R.id.reSeekBar);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		long end = new Date().getTime();
		long start = end - 4*60*60*1000;
		tvStartTime.setText(sdf.format(new Date(start)));
		tvEndTime.setText(sdf.format(new Date(end)));
		String url = String.format("http://decision-admin.tianqi.cn/Home/extra/getcy_user_feedback?start=%s&end=%s&bounds=70,10,140,50&zoom=-999&source=all", start, end);
		OkHttpData(url);
		
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
	
	private void OkHttpData(final String url) {
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
						String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject obj = new JSONObject(result);
								if (!obj.isNull("data")) {
									dataList.clear();
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
										dataList.add(dto);
									}
								}

								addMarkers();

								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (dataList.size() > 0) {
											reSeekBar.setVisibility(View.VISIBLE);
										}
										loadingView.setVisibility(View.GONE);
									}
								});

							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		}).start();
	}
	
	private void addMarkers() {
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				int size = dataList.size();
				if (size > 1000) {
					size = 1000;
				}
				for (int i = 0; i < size; i++) {
					SocietyDto dto = dataList.get(i);
					MarkerOptions options = new MarkerOptions();
					options.anchor(0.5f, 0.5f);
					options.position(new LatLng(dto.lat, dto.lng));
					View view = inflater.inflate(R.layout.shawn_society_marker_icon, null);
					ImageView ivMarker = view.findViewById(R.id.ivMarker);
					if (dto.main == 0) {
						options.title(getString(R.string.main0));
						ivMarker.setImageResource(R.drawable.shawn_icon_society0);
					}else if (dto.main == 1) {
						options.title(getString(R.string.main1));
						ivMarker.setImageResource(R.drawable.shawn_icon_society1);
					}else if (dto.main == 2) {
						options.title(getString(R.string.main2));
						ivMarker.setImageResource(R.drawable.shawn_icon_society2);
					}else if (dto.main == 3) {
						options.title(getString(R.string.main3));
						ivMarker.setImageResource(R.drawable.shawn_icon_society3);
					}else if (dto.main == 4) {
						options.title(getString(R.string.main4));
						ivMarker.setImageResource(R.drawable.shawn_icon_society4);
					}else if (dto.main == 5) {
						options.title(getString(R.string.main5));
						ivMarker.setImageResource(R.drawable.shawn_icon_society5);
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
			}
		}).start();
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
	public boolean onMarkerClick(Marker marker) {
		if (marker != null) {
			clickMarker = marker;
			if (clickMarker.isInfoWindowShown()) {
				clickMarker.hideInfoWindow();
			}else {
				clickMarker.showInfoWindow();
			}
		}
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
		View view = inflater.inflate(R.layout.shawn_society_marker_icon_info, null);
		TextView tvPhe = view.findViewById(R.id.tvPhe);
		TextView tvInfo = view.findViewById(R.id.tvInfo);
		tvPhe.setText(arg0.getTitle());
		tvInfo.setText(arg0.getSnippet());
		return view;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_SETMARKER_TOTOP: 
				if (msg.obj != null) {
					long time = (Long) msg.obj;
					for (int i = 0; i < markerList.size(); i++) {
						if (time == dataList.get(i).time) {
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
		private boolean isTracking;

		private SocietyThread(List<SocietyDto> list) {
			this.list = list;
			this.count = list.size();
			this.index = 0;
			this.state = STATE_NONE;
			this.isTracking = false;
		}
		
		private int getCurrentState() {
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
		Bitmap bitmap;
		Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
		if (reSeekBar.getVisibility() == View.VISIBLE) {
			Bitmap bitmap2 = CommonUtil.captureView(reSeekBar);
			Bitmap bitmap3 = CommonUtil.mergeBitmap(ShawnSocietyObserveActivity.this, bitmap1, bitmap2, true);
			CommonUtil.clearBitmap(bitmap2);
			bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
			CommonUtil.clearBitmap(bitmap3);
		}else {
			bitmap = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap4, false);
		}
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap4);
		CommonUtil.share(ShawnSocietyObserveActivity.this, bitmap);
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
		case R.id.ivPlay:
			if (mThread == null) {
				mThread = new SocietyThread(dataList);
				mThread.start();
				ivPlay.setImageResource(R.drawable.shawn_icon_pause);
			}else {
				if (mThread.getCurrentState() == SocietyThread.STATE_PLAYING) {
					mThread.pause();
					ivPlay.setImageResource(R.drawable.shawn_icon_play);
				} else if (mThread.getCurrentState() == SocietyThread.STATE_PAUSE) {
					mThread.play();
					ivPlay.setImageResource(R.drawable.shawn_icon_pause);
				}
			}
			break;
		case R.id.ivShare:
			aMap.getMapScreenShot(ShawnSocietyObserveActivity.this);
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
