package com.china.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnRadarDetailAdapter;
import com.china.dto.RadarDto;
import com.china.manager.RadarManager;
import com.china.utils.OkHttpUtil;
import com.china.utils.SecretUrlUtil;
import com.china.view.PhotoView;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

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
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 天气雷达详情
 */
public class ShawnRadarDetailActivity extends BaseActivity implements OnClickListener, RadarManager.RadarListener {

	private Context mContext;
	private List<RadarDto> radarList = new ArrayList<>();
	private ImageView imageView,ivPlay;
	private RadarManager mRadarManager;
	private RadarThread mRadarThread;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_PROGRESS = 2;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private static final int HANDLER_PAUSE = 4;
	private SeekBar seekBar;
	private TextView tvTime,tvCount;
	private ShawnRadarDetailAdapter mAdapter;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	private ViewPager mViewPager;
	private RelativeLayout reViewPager;
	private int gridviewPosition;
	private AVLoadingIndicatorView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_radar_detail);
		mContext = this;
		initWidget();
		initGridView();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		loadingView = findViewById(R.id.loadingView);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		imageView = findViewById(R.id.imageView);
		imageView.setOnClickListener(this);
		ivPlay = findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		tvTime = findViewById(R.id.tvTime);
		tvCount = findViewById(R.id.tvCount);
		reViewPager = findViewById(R.id.reViewPager);

		mRadarManager = new RadarManager(mContext);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		ViewGroup.LayoutParams params = imageView.getLayoutParams();
		params.width = width;
		params.height = width*16/21;
		imageView.setLayoutParams(params);

		if (getIntent().hasExtra("radarName")) {
			String title = getIntent().getStringExtra("radarName");
			if (!TextUtils.isEmpty(title)) {
				tvTitle.setText(title);
			}
		}
		if (getIntent().hasExtra("radarCode")) {
			String radarCode = getIntent().getStringExtra("radarCode");
			if (!TextUtils.isEmpty(radarCode)) {
				OkHttpRadarDetail(radarCode);
			}
		}

	}

	private OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.setCurrent(seekBar.getProgress());
				mRadarThread.stopTracking();
			}
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.startTracking();
			}
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		}
	};

	/**
	 * 获取雷达图片集信息
	 */
	private void OkHttpRadarDetail(String radarCode) {
		final String url = SecretUrlUtil.radarDetail(radarCode, "product");
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
										String r2 = obj.getString("r2");
										String r3 = obj.getString("r3");
										String r5 = obj.getString("r5");
										JSONArray array = new JSONArray(obj.getString("r6"));
										for (int i = array.length()-1; i >= 0 ; i--) {
											JSONArray itemArray = array.getJSONArray(i);
											String r6_0 = itemArray.getString(0);
											String r6_1 = itemArray.getString(1);
											String imgUrl = r2 + r5 + r6_0 + "." + r3;

											RadarDto dto = new RadarDto();
											dto.imgUrl = imgUrl;
											try {
												dto.time = sdf1.format(sdf2.parse(r6_1));
											} catch (ParseException e) {
												e.printStackTrace();
											}
											radarList.add(dto);
										}

										if (radarList.size() > 0) {
											gridviewPosition = radarList.size()-1;
											loadImages(gridviewPosition);
										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								loadingView.setVisibility(View.GONE);
							}
						});
					}
				});
			}
		}).start();
	}

	/**
	 * 初始化gridview
	 */
	private void initGridView() {
		GridView mGridView = findViewById(R.id.gridView);
		mAdapter = new ShawnRadarDetailAdapter(mContext, radarList);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                gridviewPosition = arg2;
                loadImages(gridviewPosition);
			}
		});
	}

	/**
	 * 加载图片数据
	 */
	private void loadImages(int gridviewPosition) {
		if (radarList != null && radarList.size() > 0) {
			for (int i = 0; i < radarList.size(); i++) {
				RadarDto data = radarList.get(i);
				if (i == gridviewPosition) {
					data.isSelected = true;
				}else {
					data.isSelected = false;
				}
			}
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}

			if (!TextUtils.isEmpty(radarList.get(gridviewPosition).imgUrl)) {
				Picasso.get().load(radarList.get(gridviewPosition).imgUrl).error(R.drawable.shawn_icon_no_pic).into(imageView);
			}else {
				imageView.setImageResource(R.drawable.shawn_icon_no_pic);
			}

			if (seekBar != null && radarList.size() > 0) {
				seekBar.setProgress(gridviewPosition+1);
				seekBar.setMax(radarList.size());
			}
			try {
				tvTime.setText(sdf1.format(sdf2.parse(radarList.get(gridviewPosition).time)));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			initViewPager();

		}
	}

	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		ImageView[] imageArray = new ImageView[radarList.size()];
		for (int i = 0; i < radarList.size(); i++) {
			RadarDto dto = radarList.get(i);
			if (!TextUtils.isEmpty(dto.imgUrl)) {
				ImageView imageView = new ImageView(this);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				Picasso.get().load(dto.imgUrl).into(imageView);
				imageArray[i] = imageView;
			}
		}

		mViewPager = findViewById(R.id.viewPager);
		MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter(imageArray);
		mViewPager.setAdapter(myViewPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				tvCount.setText((arg0+1)+"/"+radarList.size());
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	private class MyViewPagerAdapter extends PagerAdapter {

		private ImageView[] mImageViews;

		private MyViewPagerAdapter(ImageView[] imageViews) {
			this.mImageViews = imageViews;
		}

		@Override
		public int getCount() {
			return mImageViews.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mImageViews[position]);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			Drawable drawable = mImageViews[position].getDrawable();
			photoView.setImageDrawable(drawable);
			container.addView(photoView, 0);
			photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
				@Override
				public void onPhotoTap(View view, float v, float v1) {
					scaleColloseAnimation(reViewPager);
					reViewPager.setVisibility(View.GONE);
				}
			});
			return photoView;
		}

	}

	/**
	 * 放大动画
	 * @param view
	 */
	private void scaleExpandAnimation(View view) {
		AnimationSet animationSet = new AnimationSet(true);

		ScaleAnimation scaleAnimation = new ScaleAnimation(0,1.0f,0,1.0f,
				Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
		scaleAnimation.setInterpolator(new LinearInterpolator());
		scaleAnimation.setDuration(300);
		animationSet.addAnimation(scaleAnimation);

		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1.0f);
		alphaAnimation.setDuration(300);
		animationSet.addAnimation(alphaAnimation);

		view.startAnimation(animationSet);
	}

	/**
	 * 缩小动画
	 * @param view
	 */
	private void scaleColloseAnimation(View view) {
		AnimationSet animationSet = new AnimationSet(true);

		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
				Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
		scaleAnimation.setInterpolator(new LinearInterpolator());
		scaleAnimation.setDuration(300);
		animationSet.addAnimation(scaleAnimation);

		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
		alphaAnimation.setDuration(300);
		animationSet.addAnimation(alphaAnimation);

		view.startAnimation(animationSet);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (reViewPager.getVisibility() == View.VISIBLE) {
			scaleColloseAnimation(reViewPager);
			reViewPager.setVisibility(View.GONE);
			return false;
		}else {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void startDownLoadImgs(List<RadarDto> list) {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		if (list.size() > 0) {
			mRadarManager.loadImagesAsyn(list, this);
		}
	}

	@Override
	public void onResult(int result, List<RadarDto> images) {
		mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		if (result == RadarManager.RadarListener.RESULT_SUCCESSED) {
			if (mRadarThread != null) {
				mRadarThread.cancel();
				mRadarThread = null;
			}
			if (images.size() > 0) {
				mRadarThread = new RadarThread(images);
				mRadarThread.start();
			}
		}
	}

	private class RadarThread extends Thread {

		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<RadarDto> images;
		private int state;
		private int index;
		private int count;
		private boolean isTracking;

		private RadarThread(List<RadarDto> images) {
			this.images = images;
			this.count = images.size();
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
				sendRadar();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void sendRadar() {
			if (index >= count || index < 0) {
				index = 0;

				if (mRadarThread != null) {
					mRadarThread.pause();

					Message message = mHandler.obtainMessage();
					message.what = HANDLER_PAUSE;
					mHandler.sendMessage(message);
					if (seekBar != null) {
						seekBar.setProgress(radarList.size());
					}
				}
			}else {
			    gridviewPosition = index;
				RadarDto radar = images.get(index);
				Message message = mHandler.obtainMessage();
				message.what = HANDLER_SHOW_RADAR;
				message.obj = radar;
				message.arg1 = count - 1;
				message.arg2 = index ++;
				mHandler.sendMessage(message);
			}

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
				sendRadar();
			}
		}
	}

	@Override
	public void onProgress(String url, int progress) {
		Message msg = new Message();
		msg.obj = progress;
		msg.what = HANDLER_PROGRESS;
		mHandler.sendMessage(msg);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_SHOW_RADAR:
				if (msg.obj != null) {
					RadarDto radar = (RadarDto) msg.obj;
					Bitmap bitmap = BitmapFactory.decodeFile(radar.imgPath);
					if (bitmap != null) {
						imageView.setImageBitmap(bitmap);
					}
					changeProgress(radar.time, msg.arg2, msg.arg1);

					for (int i = 0; i < radarList.size(); i++) {
						if (i == msg.arg2) {
							radarList.get(msg.arg2).isSelected = true;
						}else {
							radarList.get(i).isSelected = false;
						}
					}
					mAdapter.notifyDataSetChanged();
				}
				break;
			case HANDLER_PROGRESS:

				break;
			case HANDLER_LOAD_FINISHED:
				loadingView.setVisibility(View.GONE);
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.shawn_icon_pause);
				}
				break;
			case HANDLER_PAUSE:
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.shawn_icon_play);
				}
				break;
			default:
				break;
			}

		};
	};

	private void changeProgress(String time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		tvTime.setText(time);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mRadarManager != null) {
			mRadarManager.onDestory();
		}
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.ivPlay:
				if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
					mRadarThread.pause();
					ivPlay.setImageResource(R.drawable.shawn_icon_play);
				} else if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
					mRadarThread.play();
					ivPlay.setImageResource(R.drawable.shawn_icon_pause);
				} else if (mRadarThread == null) {
					loadingView.setVisibility(View.VISIBLE);
					startDownLoadImgs(radarList);//开始下载
				}
				break;
			case R.id.imageView:
				if (reViewPager.getVisibility() == View.GONE) {
					if (mViewPager != null) {
						mViewPager.setCurrentItem(gridviewPosition);
					}
					scaleExpandAnimation(reViewPager);
					reViewPager.setVisibility(View.VISIBLE);
					tvCount.setText((gridviewPosition+1)+"/"+radarList.size());
				}
				break;
		}

	}

}
