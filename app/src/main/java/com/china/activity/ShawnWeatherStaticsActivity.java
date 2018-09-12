package com.china.activity;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.china.dto.WeatherStaticsDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.SecretUrlUtil;
import com.china.view.CircularProgressBar;
import com.tendcloud.tenddata.TCAgent;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 天气统计
 */
public class ShawnWeatherStaticsActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener,
 OnMapClickListener, OnCameraChangeListener, OnMapScreenShotListener{
	
	private Context mContext;
	private TextView tvTitle,tvName,tvBar1,tvBar2,tvBar3,tvBar4,tvBar5,tvDetail;
	private MapView mMapView;
	private AMap aMap;
	private List<WeatherStaticsDto> level1List = new ArrayList<>();
	private List<WeatherStaticsDto> level2List = new ArrayList<>();
	private List<WeatherStaticsDto> level3List = new ArrayList<>();
	private HashMap<String, WeatherStaticsDto> areaIdMap = new HashMap<>();//按区域id区分
	private Map<String, Marker> markerMap = new LinkedHashMap<>();//按区域id区分
	private float zoom = 3.7f, zoom1 = 6.5f, zoom2 = 8.5f;
	private CircularProgressBar mCircularProgressBar1,mCircularProgressBar2,mCircularProgressBar3,mCircularProgressBar4,mCircularProgressBar5;
	private RelativeLayout reDetail,reContent;
	private LatLng leftlatlng = new LatLng(-16.305714763804854,75.13831436634065);
	private LatLng rightLatlng = new LatLng(63.681687310440864,135.21788656711578);
	private final String level1 = "level1", level2 = "level2", level3 = "level3";
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	private SimpleDateFormat sdf4 = new SimpleDateFormat("MM月dd日", Locale.CHINA);
	private AVLoadingIndicatorView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_weather_statics);
		mContext = this;
		initMap(savedInstanceState);
		initWidget();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		loadingView = findViewById(R.id.loadingView);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvName = findViewById(R.id.tvName);
		tvBar1 = findViewById(R.id.tvBar1);
		tvBar2 = findViewById(R.id.tvBar2);
		tvBar3 = findViewById(R.id.tvBar3);
		tvBar4 = findViewById(R.id.tvBar4);
		tvBar5 = findViewById(R.id.tvBar5);
		tvDetail = findViewById(R.id.tvDetail);
		mCircularProgressBar1 = findViewById(R.id.bar1);
		mCircularProgressBar2 = findViewById(R.id.bar2);
		mCircularProgressBar3 = findViewById(R.id.bar3);
		mCircularProgressBar4 = findViewById(R.id.bar4);
		mCircularProgressBar5 = findViewById(R.id.bar5);
		reDetail = findViewById(R.id.reDetail);
		reContent = findViewById(R.id.reContent);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		OkHttpList();
		
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
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
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setOnCameraChangeListener(this);
	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
	}
	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		if (reDetail.getVisibility() == View.VISIBLE) {
			hideAnimation(reDetail);
		}

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);

		zoom = arg0.zoom;
		switchMarkers();
		addMarkers();
	}
	
	/**
	 * 获取天气统计数据
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
							level3List.clear();
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
				List<WeatherStaticsDto> list = new ArrayList<>();
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
					list.add(dto);

					areaIdMap.put(dto.areaId, dto);

					if (TextUtils.equals(level, level1)) {
						level1List.add(dto);
					}else if (TextUtils.equals(level, level2)) {
						level2List.add(dto);
					}else if (TextUtils.equals(level, level3)) {
						level3List.add(dto);
					}
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void switchMarkers() {
		new Thread(new Runnable() {
			@Override
			public void run() {
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
						}else if (zoom > zoom1 && zoom <= zoom2) {
							if (TextUtils.equals(level, "3")) {
								marker.remove();
							}
						}else {

						}
						if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
							//已经在可是范围内则不处理
						}else {
							marker.remove();
						}
					}
				}
			}
		}).start();
	}

	/**
	 * 添加marker
	 */
	private void addMarkers() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<WeatherStaticsDto> list = new ArrayList<>();
				if (zoom <= zoom1) {
					list.addAll(level1List);
				}else if (zoom > zoom1 && zoom <= zoom2) {
					list.addAll(level1List);
					list.addAll(level2List);
				}else {
					list.addAll(level1List);
					list.addAll(level2List);
					list.addAll(level3List);
				}

				Map<String, Marker> markers = new LinkedHashMap<>();//为防止ConcurrentModificationException异常，待循环执行完毕后，再对markerMap进行修改
				for (WeatherStaticsDto dto : list) {
					if (markerMap.containsKey(dto.areaId)) {
						Marker m = markerMap.get(dto.areaId);
						if (m != null && m.isVisible()) {
							//已经在可是区域添加过了，就不重复绘制了
						}else {
							addVisibleAreaMarker(dto, markers);
						}
					}else {
						addVisibleAreaMarker(dto, markers);
					}
				}
				markerMap.putAll(markers);
			}
		}).start();
	}

	/**
	 * 添加可视区域对应的marker
	 * @param dto
	 */
	private void addVisibleAreaMarker(WeatherStaticsDto dto, Map<String, Marker> markers) {
		if (dto.lat > leftlatlng.latitude && dto.lat < rightLatlng.latitude && dto.lng > leftlatlng.longitude && dto.lng < rightLatlng.longitude) {
			MarkerOptions options = new MarkerOptions();
			options.title(dto.areaId);
			options.snippet(dto.level);
			options.anchor(0.5f, 1.0f);
			options.position(new LatLng(dto.lat, dto.lng));
			options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.name)));
			Marker marker = aMap.addMarker(options);
			markers.put(dto.areaId, marker);
			markerExpandAnimation(marker);
		}
	}

	/**
	 * 给marker添加文字
	 * @param name 城市名称
	 * @return
	 */
	private View getTextBitmap(String name) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_statistic_marker_icon, null);
		if (view == null) {
			return null;
		}
		TextView tvName = view.findViewById(R.id.tvName);
		if (!TextUtils.isEmpty(name) && name.length() > 2) {
			name = name.substring(0, 2)+"\n"+name.substring(2, name.length());
		}
		tvName.setText(name);
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
	public void onMapClick(LatLng arg0) {
		if (reDetail.getVisibility() == View.VISIBLE) {
			hideAnimation(reDetail);
		}
	}
	
	/**
	 * 向上弹出动画
	 * @param layout
	 */
	private void showAnimation(final View layout) {
		if (layout.getVisibility() == View.VISIBLE) {
			return;
		}
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 1f, 
				TranslateAnimation.RELATIVE_TO_SELF, 0);
		animation.setDuration(300);
		layout.startAnimation(animation);
		layout.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 向下隐藏动画
	 * @param layout
	 */
	private void hideAnimation(final View layout) {
		if (layout.getVisibility() == View.GONE) {
			return;
		}
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 1f);
		animation.setDuration(300);
		layout.startAnimation(animation);
		layout.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onMarkerClick(final Marker marker) {
		if (reDetail.getVisibility() != View.VISIBLE) {
			showAnimation(reDetail);
		}
		if (marker != null) {
			if (areaIdMap.containsKey(marker.getTitle())) {
				WeatherStaticsDto dto = areaIdMap.get(marker.getTitle());
				tvName.setText(dto.name+" "+dto.stationId);
				tvDetail.setText("");
				reContent.setVisibility(View.INVISIBLE);
				loadingView.setVisibility(View.VISIBLE);
				OkHttpDetail(SecretUrlUtil.statisticDetail(dto.stationId));
			}
		}
		return true;
	}

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
										String startTime = sdf2.format(sdf3.parse(obj.getString("starttime")));
										String endTime = sdf2.format(sdf3.parse(obj.getString("endtime")));
										String highTemp = "";//高温
										String lowTemp = "";//低温
										String highWind = "";//最大风速
										String highRain = "";//最大降水量
										String lxGaowen = "";//连续高温
                                        String no_rain_lx = "";//连续没雨天数

										if (!obj.isNull("count")) {
											JSONArray array = new JSONArray(obj.getString("count"));
											JSONObject itemObj0 = array.getJSONObject(0);//温度
											JSONObject itemObj1 = array.getJSONObject(1);//降水
											JSONObject itemObj5 = array.getJSONObject(5);//风速

											//温度
											if (!itemObj0.isNull("max") && !itemObj0.isNull("min")) {
												highTemp = itemObj0.getString("max");
												if (TextUtils.equals(highTemp, "-1.0")) {
													highTemp = getString(R.string.no_statics);
												}else {
													String time = sdf4.format(sdf1.parse(itemObj0.getString("maxtime")));
													highTemp = highTemp+"℃"+"("+time+")";
												}
												lowTemp = itemObj0.getString("min");
												if (TextUtils.equals(lowTemp, "-1.0")) {
													lowTemp = getString(R.string.no_statics);
												}else {
													String time = sdf4.format(sdf1.parse(itemObj0.getString("mintime")));
													lowTemp = lowTemp+"℃"+"("+time+")";
												}
											}

											//降水
											if (!itemObj1.isNull("max")) {
												highRain = itemObj1.getString("max");
												if (TextUtils.equals(highRain, "-1.0")) {
													highRain = getString(R.string.no_statics);
												}else {
													String time = sdf4.format(sdf1.parse(itemObj1.getString("maxtime")));
													highRain = highRain+"mm"+"("+time+")";
												}
											}

											//风速
											if (!itemObj5.isNull("max")) {
												highWind = itemObj5.getString("max");
												if (TextUtils.equals(highWind, "-1.0")) {
													highWind = getString(R.string.no_statics);
												}else {
													String maxlv = itemObj5.getString("maxlv");
													String time = sdf4.format(sdf1.parse(itemObj5.getString("maxtime")));
													highWind = highWind+"m/s["+maxlv+"级]"+"("+time+")";
												}
											}

										}

										if (startTime != null && endTime != null && highTemp != null && lowTemp != null && highWind != null && highRain != null) {
											long start = sdf2.parse(startTime).getTime();
											long end = sdf2.parse(endTime).getTime();
											float dayCount = (float) ((end - start) / (1000*60*60*24)) + 1;
											if (!obj.isNull("tqxxcount")) {
												JSONArray array = new JSONArray(obj.getString("tqxxcount"));
												for (int i = 0; i < array.length(); i++) {
													JSONObject itemObj = array.getJSONObject(i);
													String name = itemObj.getString("name");
													int value = itemObj.getInt("value");
													int maxlx = itemObj.getInt("maxlx");
													int nomaxlx = 0;
													if (!itemObj.isNull("nomaxlx")) {
                                                        nomaxlx = itemObj.getInt("nomaxlx");
                                                    }

													if (i == 0) {
														if (value == -1) {
															tvBar1.setText(name + "\n" + "--");
															animate(mCircularProgressBar1, null, 0, 1000);
															mCircularProgressBar1.setProgress(0);
														}else {
															tvBar1.setText(name + "\n" + value + "天");
															animate(mCircularProgressBar1, null, -value/dayCount, 1000);
															mCircularProgressBar1.setProgress(-value/dayCount);
														}
													}else if (i == 1) {
                                                        no_rain_lx = nomaxlx+"天";

														if (value == -1) {
															tvBar2.setText(name + "\n" + "--");
															animate(mCircularProgressBar2, null, 0, 1000);
															mCircularProgressBar2.setProgress(0);
														}else {
															tvBar2.setText(name + "\n" + value + "天");
															animate(mCircularProgressBar2, null, -value/dayCount, 1000);
															mCircularProgressBar2.setProgress(-value/dayCount);
														}
													}else if (i == 2) {

													}else if (i == 3) {
														if (value == -1) {
															tvBar4.setText(name + "\n" + "--");
															animate(mCircularProgressBar4, null, 0, 1000);
															mCircularProgressBar4.setProgress(0);
														}else {
															tvBar4.setText(name + "\n" + value + "天");
															animate(mCircularProgressBar4, null, -value/dayCount, 1000);
															mCircularProgressBar4.setProgress(-value/dayCount);
														}
													}else if (i == 4) {

													}else if (i == 5) {
														if (value == -1) {
															tvBar3.setText(name + "\n" + "--");
															animate(mCircularProgressBar3, null, 0, 1000);
															mCircularProgressBar3.setProgress(0);
														}else {
															tvBar3.setText(name + "\n" + value + "天");
															animate(mCircularProgressBar3, null, -value/dayCount, 1000);
															mCircularProgressBar3.setProgress(-value/dayCount);
														}
													}else if (i == 6) {
														lxGaowen = maxlx+"天";

														if (value == -1) {
															tvBar5.setText(name + "\n" + "--");
															animate(mCircularProgressBar5, null, 0, 1000);
															mCircularProgressBar5.setProgress(0);
														}else {
															tvBar5.setText(name + "\n" + value + "天");
															animate(mCircularProgressBar5, null, -value/dayCount, 1000);
															mCircularProgressBar5.setProgress(-value/dayCount);
														}
													}
												}
											}

											StringBuffer buffer = new StringBuffer();
											buffer.append(getString(R.string.from)).append(startTime);
											buffer.append(getString(R.string.to)).append(endTime);
											buffer.append("：\n");
											buffer.append(getString(R.string.highest_temp)).append(highTemp).append("，");
											buffer.append(getString(R.string.lowest_temp)).append(lowTemp).append("，");
											buffer.append(getString(R.string.max_speed)).append(highWind).append("，");
											buffer.append(getString(R.string.max_fall)).append(highRain).append("，");
											buffer.append(getString(R.string.lx_no_fall)).append(no_rain_lx).append("，");
											buffer.append(getString(R.string.lx_gaowen)).append(lxGaowen).append("。");

											SpannableStringBuilder builder = new SpannableStringBuilder(buffer.toString());
											ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(Color.RED);
											ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(Color.RED);
											ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(Color.RED);
											ForegroundColorSpan builderSpan4 = new ForegroundColorSpan(Color.RED);
											ForegroundColorSpan builderSpan5 = new ForegroundColorSpan(Color.RED);
											ForegroundColorSpan builderSpan6 = new ForegroundColorSpan(Color.RED);

											builder.setSpan(builderSpan1, 29, 29+highTemp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
											builder.setSpan(builderSpan2, 29+highTemp.length()+6, 29+highTemp.length()+6+lowTemp.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
											builder.setSpan(builderSpan3, 29+highTemp.length()+6+lowTemp.length()+6, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
											builder.setSpan(builderSpan4, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7+highRain.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
											builder.setSpan(builderSpan5, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7+highRain.length()+10, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7+highRain.length()+10+no_rain_lx.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
											builder.setSpan(builderSpan6, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7+highRain.length()+10+no_rain_lx.length()+9, 29+highTemp.length()+6+lowTemp.length()+6+highWind.length()+7+highRain.length()+10+no_rain_lx.length()+9+lxGaowen.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
											tvDetail.setText(builder);

										}
										reContent.setVisibility(View.VISIBLE);
										loadingView.setVisibility(View.GONE);
									} catch (JSONException e) {
										e.printStackTrace();
									} catch (ParseException e) {
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
	 * 进度条动画
	 * @param progressBar
	 * @param listener
	 * @param progress
	 * @param duration
	 */
	private void animate(final CircularProgressBar progressBar, final AnimatorListener listener,final float progress, final int duration) {
		ObjectAnimator mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
		mProgressBarAnimator.setDuration(duration);
		mProgressBarAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(final Animator animation) {
			}
			@Override
			public void onAnimationEnd(final Animator animation) {
				progressBar.setProgress(progress);
			}
			@Override
			public void onAnimationRepeat(final Animator animation) {
			}
			@Override
			public void onAnimationStart(final Animator animation) {
			}
		});
		if (listener != null) {
			mProgressBarAnimator.addListener(listener);
		}
		mProgressBarAnimator.reverse();
		mProgressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				progressBar.setProgress((Float) animation.getAnimatedValue());
			}
		});
//		progressBar.setMarkerProgress(0f);
		mProgressBarAnimator.start();
	}
	
	@Override
	public void onMapScreenShot(final Bitmap bitmap1) {//bitmap1为地图截屏
		Bitmap bitmap;
		Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
		if (reDetail.getVisibility() == View.VISIBLE) {
			Bitmap bitmap2 = CommonUtil.captureView(reDetail);
			Bitmap bitmap3 = CommonUtil.mergeBitmap(ShawnWeatherStaticsActivity.this, bitmap1, bitmap2, true);
			CommonUtil.clearBitmap(bitmap2);
			bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap4, false);
			CommonUtil.clearBitmap(bitmap3);
		}else {
			bitmap = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap4, false);
		}
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap4);
		CommonUtil.share(ShawnWeatherStaticsActivity.this, bitmap);
	}
	@Override
	public void onMapScreenShot(Bitmap arg0, int arg1) {
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (reDetail.getVisibility() == View.VISIBLE) {
				hideAnimation(reDetail);
				return false;
			} else {
				setBackEmit();
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (reDetail.getVisibility() == View.VISIBLE) {
				hideAnimation(reDetail);
			} else {
				setBackEmit();
				finish();
			}
			break;
		case R.id.ivShare:
			aMap.getMapScreenShot(ShawnWeatherStaticsActivity.this);
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
