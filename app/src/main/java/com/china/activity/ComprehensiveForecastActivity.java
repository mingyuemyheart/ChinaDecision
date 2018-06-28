package com.china.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.china.R;
import com.china.common.CONST;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.tendcloud.tenddata.TCAgent;

import net.tsz.afinal.FinalBitmap;

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
 * 综合预报
 * @author shawn_sun
 *
 */

public class ComprehensiveForecastActivity extends BaseActivity implements OnClickListener, AMapLocationListener, OnMapScreenShotListener, AMap.OnCameraChangeListener {
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle,tvName,tvTime;
	private MapView mMapView = null;
	private AMap aMap = null;
	private ImageView ivShare,ivLegendPrompt,ivLocation,ivLegend,ivJiangshui,ivHighTemp,ivLowTemp,ivWuran,ivShachen,ivGaowen,ivFog,ivDizhi,ivHaze,ivQiangduiliu,ivDafeng,ivMore;
	private RelativeLayout reShare,reMore;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy.MM.dd HH:00");
	private long start, end;
	private float zoom = 3.5f;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private double locationLat = 35.926628, locationLng = 105.178100;
	private List<Polygon> polygons = new ArrayList<>();//图层数据

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comprehensive_forecast);
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
		ivLegendPrompt = (ImageView) findViewById(R.id.ivLegendPrompt);
		ivLegendPrompt.setOnClickListener(this);
		ivLegend = (ImageView) findViewById(R.id.ivLegend);
		ivLocation = (ImageView) findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvName = (TextView) findViewById(R.id.tvName);
		tvTime = (TextView) findViewById(R.id.tvTime);
		reShare = (RelativeLayout) findViewById(R.id.reShare);
		reMore = (RelativeLayout) findViewById(R.id.reMore);
		ivJiangshui = (ImageView) findViewById(R.id.ivJiangshui);
		ivJiangshui.setOnClickListener(this);
		ivHighTemp = (ImageView) findViewById(R.id.ivHighTemp);
		ivHighTemp.setOnClickListener(this);
		ivLowTemp = (ImageView) findViewById(R.id.ivLowTemp);
		ivLowTemp.setOnClickListener(this);
		ivWuran = (ImageView) findViewById(R.id.ivWuran);
		ivWuran.setOnClickListener(this);
		ivShachen = (ImageView) findViewById(R.id.ivShachen);
		ivShachen.setOnClickListener(this);
		ivGaowen = (ImageView) findViewById(R.id.ivGaowen);
		ivGaowen.setOnClickListener(this);
		ivFog = (ImageView) findViewById(R.id.ivFog);
		ivFog.setOnClickListener(this);
		ivDizhi = (ImageView) findViewById(R.id.ivDizhi);
		ivDizhi.setOnClickListener(this);
		ivHaze = (ImageView) findViewById(R.id.ivHaze);
		ivHaze.setOnClickListener(this);
		ivQiangduiliu = (ImageView) findViewById(R.id.ivQiangduiliu);
		ivQiangduiliu.setOnClickListener(this);
		ivDafeng = (ImageView) findViewById(R.id.ivDafeng);
		ivDafeng.setOnClickListener(this);
		ivMore = (ImageView) findViewById(R.id.ivMore);
		ivMore.setOnClickListener(this);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		startLocation();

		OkHttpList("http://decision-admin.tianqi.cn/Home/extra/decision_zhyblayers");
		
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
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
			LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
			MarkerOptions options = new MarkerOptions();
			options.anchor(0.5f, 0.5f);
			Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_map_location),
					(int) (CommonUtil.dip2px(mContext, 15)), (int) (CommonUtil.dip2px(mContext, 15)));
			if (bitmap != null) {
				options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
			} else {
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iv_map_location));
			}
			options.position(latLng);
			Marker locationMarker = aMap.addMarker(options);
			locationMarker.setClickable(false);
		}
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
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnCameraChangeListener(this);
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		zoom = arg0.zoom;
	}

	private void OkHttpList(final String url) {
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
                                        if (!TextUtils.isEmpty(result)) {
                                            JSONObject obj = new JSONObject(result);

                                            if (!obj.isNull("fc_rain")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_rain");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
                                                String name = itemObj.getString("name");
                                                String validhour = itemObj.getString("validhour");
                                                ivJiangshui.setTag(dataurl+","+img+","+name+","+validhour);
                                                OkHttpDetail(ivJiangshui, true);
                                            }else {
                                                ivJiangshui.setAlpha(0.5f);
                                            }

                                            if (!obj.isNull("fc_max")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_max");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
												String name = itemObj.getString("name");
												String validhour = itemObj.getString("validhour");
                                                ivHighTemp.setTag(dataurl+","+img+","+name+","+validhour);
                                                OkHttpHighLowTemp(ivHighTemp, false);
                                            }else {
                                                ivHighTemp.setAlpha(0.5f);
                                            }

                                            if (!obj.isNull("fc_min")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_min");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
												String name = itemObj.getString("name");
												String validhour = itemObj.getString("validhour");
                                                ivLowTemp.setTag(dataurl+","+img+","+name+","+validhour);
												OkHttpHighLowTemp(ivLowTemp, false);
                                            }else {
                                                ivLowTemp.setAlpha(0.5f);
                                            }

                                            if (!obj.isNull("fc_kqzl")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_kqzl");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
												String name = itemObj.getString("name");
												String validhour = itemObj.getString("validhour");
                                                ivWuran.setTag(dataurl+","+img+","+name+","+validhour);
                                                OkHttpDetail(ivWuran, false);
                                            }else {
                                                ivWuran.setAlpha(0.5f);
                                            }

                                            if (!obj.isNull("fc_sc")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_sc");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
												String name = itemObj.getString("name");
												String validhour = itemObj.getString("validhour");
                                                ivShachen.setTag(dataurl+","+img+","+name+","+validhour);
                                                OkHttpDetail(ivShachen, false);
                                            }else {
                                                ivShachen.setAlpha(0.5f);
                                            }

                                            if (!obj.isNull("fc_gw")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_gw");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
												String name = itemObj.getString("name");
												String validhour = itemObj.getString("validhour");
                                                ivGaowen.setTag(dataurl+","+img+","+name+","+validhour);
                                                OkHttpDetail(ivGaowen, false);
                                            }else {
                                                ivGaowen.setAlpha(0.5f);
                                            }

                                            if (!obj.isNull("fc_fog")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_fog");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
												String name = itemObj.getString("name");
												String validhour = itemObj.getString("validhour");
                                                ivFog.setTag(dataurl+","+img+","+name+","+validhour);
                                                OkHttpDetail(ivFog, false);
                                            }else {
                                                ivFog.setAlpha(0.5f);
                                            }

                                            if (!obj.isNull("fc_dz")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_dz");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
												String name = itemObj.getString("name");
												String validhour = itemObj.getString("validhour");
                                                ivDizhi.setTag(dataurl+","+img+","+name+","+validhour);
                                                OkHttpDetail(ivDizhi, false);
                                            }else {
                                                ivDizhi.setAlpha(0.5f);
                                            }

                                            if (!obj.isNull("fc_haze")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_haze");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
												String name = itemObj.getString("name");
												String validhour = itemObj.getString("validhour");
                                                ivHaze.setTag(dataurl+","+img+","+name+","+validhour);
                                                OkHttpDetail(ivHaze, false);
                                            }else {
                                                ivHaze.setAlpha(0.5f);
                                            }

                                            if (!obj.isNull("fc_spc")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_spc");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
												String name = itemObj.getString("name");
												String validhour = itemObj.getString("validhour");
                                                ivQiangduiliu.setTag(dataurl+","+img+","+name+","+validhour);
                                                OkHttpDetail(ivQiangduiliu, false);
                                            }else {
                                                ivQiangduiliu.setAlpha(0.5f);
                                            }

                                            if (!obj.isNull("fc_dfjw")) {
                                                JSONObject itemObj = obj.getJSONObject("fc_dfjw");
                                                String dataurl = itemObj.getString("dataurl");
                                                String img = itemObj.getString("img");
												String name = itemObj.getString("name");
												String validhour = itemObj.getString("validhour");
                                                ivDafeng.setTag(dataurl+","+img+","+name+","+validhour);
                                                OkHttpDetail(ivDafeng, false);
                                            }else {
                                                ivDafeng.setAlpha(0.5f);
                                            }

                                        }
                                        cancelDialog();
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

	private void clearPolygons() {
        for (int i = 0; i < polygons.size(); i++) {
            polygons.get(i).remove();
        }
        polygons.clear();
    }

	/**
	 * 绘制单要素图层
	 * @param draw 是否绘制图层
	 */
	private void OkHttpDetail(final ImageView imageView, final boolean draw) {
		final String[] tags = String.valueOf(imageView.getTag()).split(",");
		if (tags.length <= 0 || TextUtils.isEmpty(tags[0])) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					imageView.setAlpha(0.5f);
				}
			});
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(tags[0]).build(), new Callback() {
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
										JSONArray arr = new JSONArray(result);
										if (arr.length() > 0) {
											JSONObject obj = arr.getJSONObject(0);

											if (!obj.isNull("time")) {
												start = obj.getLong("time");
												if (!TextUtils.isEmpty(tags[3])) {
													int hour = Integer.parseInt(tags[3]);
													end = start + 1000*60*60*hour;
													tvTime.setText(sdf1.format(start)+" - "+sdf1.format(end));
												}
											}

											if (!obj.isNull("areas")) {
												JSONArray array = obj.getJSONArray("areas");
												if (array.length() <= 0) {
                                                    imageView.setAlpha(0.5f);
												    return;
                                                }
                                                if (!draw) {
													return;
												}
												clearPolygons();
												if (!TextUtils.isEmpty(tags[2])) {
													tvName.setText(tags[2]);
												}

												if (!TextUtils.isEmpty(tags[1])) {
													FinalBitmap finalBitmap = FinalBitmap.create(mContext);
													finalBitmap.display(ivLegend, tags[1], null, 0);
												}
												for (int i = 0; i < array.length(); i++) {
													JSONObject itemObj = array.getJSONObject(i);
													String color = itemObj.getString("c");
													if (color.contains("#")) {
														color = color.replace("#", "");
													}
													int r = Integer.parseInt(color.substring(0,2), 16);
													int g = Integer.parseInt(color.substring(2,4), 16);
													int b = Integer.parseInt(color.substring(4,6), 16);
													if (!itemObj.isNull("items")) {
														JSONArray items = itemObj.getJSONArray("items");
														PolygonOptions polygonOption = new PolygonOptions();
														polygonOption.strokeColor(Color.rgb(r, g, b)).fillColor(Color.rgb(r, g, b));
														for (int j = 0; j < items.length(); j++) {
															JSONObject item = items.getJSONObject(j);
															double lat = item.getDouble("y");
															double lng = item.getDouble("x");
															polygonOption.add(new LatLng(lat, lng));
														}
														Polygon polygon = aMap.addPolygon(polygonOption);
                                                        polygons.add(polygon);
													}
												}
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
	 * 绘制单要素图层
	 */
	private void OkHttpHighLowTemp(final ImageView imageView, final boolean draw) {
		final String[] tags = String .valueOf(imageView.getTag()).split(",");
		if (tags.length <= 0 || TextUtils.isEmpty(tags[0])) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					imageView.setAlpha(0.5f);
				}
			});
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(tags[0]).build(), new Callback() {
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
								if (!draw) {
									return;
								}
								clearPolygons();
								try {
									JSONObject obj = new JSONObject(result);

									if (!obj.isNull("t")) {
										start = obj.getLong("t");
										if (!TextUtils.isEmpty(tags[3])) {
											int hour = Integer.parseInt(tags[3]);
											end = start + 1000*60*60*hour;
											tvTime.setText(sdf1.format(start)+" - "+sdf1.format(end));
										}
									}

									if (!obj.isNull("l")) {
										JSONArray array = obj.getJSONArray("l");
										if (array.length() <= 0) {
											return;
										}

										if (!TextUtils.isEmpty(tags[2])) {
											tvName.setText(tags[2]);
										}
										if (!TextUtils.isEmpty(tags[3])) {
											int hour = Integer.parseInt(tags[3]);
											end = start + 1000*60*60*hour;
											tvTime.setText(sdf1.format(start)+" - "+sdf1.format(end));
										}
										if (!TextUtils.isEmpty(tags[1])) {
											FinalBitmap finalBitmap = FinalBitmap.create(mContext);
											finalBitmap.display(ivLegend, tags[1], null, 0);
										}
										for (int i = 0; i < array.length(); i++) {
											JSONObject itemObj = array.getJSONObject(i);
											JSONArray c = itemObj.getJSONArray("c");
											int r = c.getInt(0);
											int g = c.getInt(1);
											int b = c.getInt(2);
											int a = c.getInt(3) * (int)(255 * 0.7f);

											if (!itemObj.isNull("p")) {
												String p = itemObj.getString("p");
												if (!TextUtils.isEmpty(p)) {
													String[] points = p.split(";");
													PolygonOptions polygonOption = new PolygonOptions();
													polygonOption.fillColor(Color.argb(a, r, g, b));
													polygonOption.strokeColor(Color.TRANSPARENT);
													for (int j = 0; j < points.length; j++) {
														String[] latLng = points[j].split(",");
														double lat = Double.valueOf(latLng[1]);
														double lng = Double.valueOf(latLng[0]);
														polygonOption.add(new LatLng(lat, lng));
													}
													Polygon polygon = aMap.addPolygon(polygonOption);
													polygons.add(polygon);
												}
											}
										}
									}

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
		Bitmap bitmap;
		Bitmap bitmap8 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
		Bitmap bitmap2 = CommonUtil.captureView(reShare);
		Bitmap bitmap3 = CommonUtil.mergeBitmap(mContext, bitmap1, bitmap2, true);
		CommonUtil.clearBitmap(bitmap1);
		CommonUtil.clearBitmap(bitmap2);
		bitmap = CommonUtil.mergeBitmap(mContext, bitmap3, bitmap8, false);
		CommonUtil.clearBitmap(bitmap3);
		CommonUtil.clearBitmap(bitmap8);
		CommonUtil.share(ComprehensiveForecastActivity.this, bitmap);
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
				aMap.getMapScreenShot(ComprehensiveForecastActivity.this);
				break;
			case R.id.ivLegendPrompt:
				if (ivLegend.getVisibility() == View.VISIBLE) {
					ivLegend.setVisibility(View.INVISIBLE);
				}else {
					ivLegend.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.ivMore:
				if (reMore.getVisibility() == View.VISIBLE) {
					reMore.setVisibility(View.GONE);
				}else {
					reMore.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.ivLocation:
				if (zoom >= 12.f) {
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), 3.5f));
				}else {
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), 12.0f));
				}
				break;
			case R.id.ivHighTemp:
				ivHighTemp.setImageResource(R.drawable.com_hightemp_press);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui);
				ivWuran.setImageResource(R.drawable.com_wuran);
				ivShachen.setImageResource(R.drawable.com_shachen);
				ivGaowen.setImageResource(R.drawable.com_gaowen);
				ivFog.setImageResource(R.drawable.com_fog);
				ivDafeng.setImageResource(R.drawable.com_dafeng);
				ivDizhi.setImageResource(R.drawable.com_dizhi);
				ivHaze.setImageResource(R.drawable.com_haze);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu);

				OkHttpHighLowTemp(ivHighTemp, true);
				break;
			case R.id.ivLowTemp:
				ivHighTemp.setImageResource(R.drawable.com_hightemp);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp_press);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui);
				ivWuran.setImageResource(R.drawable.com_wuran);
				ivShachen.setImageResource(R.drawable.com_shachen);
				ivGaowen.setImageResource(R.drawable.com_gaowen);
				ivFog.setImageResource(R.drawable.com_fog);
				ivDafeng.setImageResource(R.drawable.com_dafeng);
				ivDizhi.setImageResource(R.drawable.com_dizhi);
				ivHaze.setImageResource(R.drawable.com_haze);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu);

				OkHttpHighLowTemp(ivLowTemp, true);
				break;
			case R.id.ivJiangshui:
				ivHighTemp.setImageResource(R.drawable.com_hightemp);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui_press);
				ivWuran.setImageResource(R.drawable.com_wuran);
				ivShachen.setImageResource(R.drawable.com_shachen);
				ivGaowen.setImageResource(R.drawable.com_gaowen);
				ivFog.setImageResource(R.drawable.com_fog);
				ivDafeng.setImageResource(R.drawable.com_dafeng);
				ivDizhi.setImageResource(R.drawable.com_dizhi);
				ivHaze.setImageResource(R.drawable.com_haze);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu);

				OkHttpDetail(ivJiangshui, true);
				break;
			case R.id.ivWuran:
				ivHighTemp.setImageResource(R.drawable.com_hightemp);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui);
				ivWuran.setImageResource(R.drawable.com_wuran_press);
				ivShachen.setImageResource(R.drawable.com_shachen);
				ivGaowen.setImageResource(R.drawable.com_gaowen);
				ivFog.setImageResource(R.drawable.com_fog);
				ivDafeng.setImageResource(R.drawable.com_dafeng);
				ivDizhi.setImageResource(R.drawable.com_dizhi);
				ivHaze.setImageResource(R.drawable.com_haze);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu);

				OkHttpDetail(ivWuran, true);
				break;
			case R.id.ivShachen:
				ivHighTemp.setImageResource(R.drawable.com_hightemp);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui);
				ivWuran.setImageResource(R.drawable.com_wuran);
				ivShachen.setImageResource(R.drawable.com_shachen_press);
				ivGaowen.setImageResource(R.drawable.com_gaowen);
				ivFog.setImageResource(R.drawable.com_fog);
				ivDafeng.setImageResource(R.drawable.com_dafeng);
				ivDizhi.setImageResource(R.drawable.com_dizhi);
				ivHaze.setImageResource(R.drawable.com_haze);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu);

				OkHttpDetail(ivShachen, true);
				break;
			case R.id.ivGaowen:
				ivHighTemp.setImageResource(R.drawable.com_hightemp);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui);
				ivWuran.setImageResource(R.drawable.com_wuran);
				ivShachen.setImageResource(R.drawable.com_shachen);
				ivGaowen.setImageResource(R.drawable.com_gaowen_press);
				ivFog.setImageResource(R.drawable.com_fog);
				ivDafeng.setImageResource(R.drawable.com_dafeng);
				ivDizhi.setImageResource(R.drawable.com_dizhi);
				ivHaze.setImageResource(R.drawable.com_haze);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu);

				OkHttpDetail(ivGaowen, true);
				break;
			case R.id.ivFog:
				ivHighTemp.setImageResource(R.drawable.com_hightemp);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui);
				ivWuran.setImageResource(R.drawable.com_wuran);
				ivShachen.setImageResource(R.drawable.com_shachen);
				ivGaowen.setImageResource(R.drawable.com_gaowen);
				ivFog.setImageResource(R.drawable.com_fog_press);
				ivDafeng.setImageResource(R.drawable.com_dafeng);
				ivDizhi.setImageResource(R.drawable.com_dizhi);
				ivHaze.setImageResource(R.drawable.com_haze);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu);

				OkHttpDetail(ivFog, true);
				break;
			case R.id.ivDafeng:
				ivHighTemp.setImageResource(R.drawable.com_hightemp);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui);
				ivWuran.setImageResource(R.drawable.com_wuran);
				ivShachen.setImageResource(R.drawable.com_shachen);
				ivGaowen.setImageResource(R.drawable.com_gaowen);
				ivFog.setImageResource(R.drawable.com_fog);
				ivDafeng.setImageResource(R.drawable.com_dafeng_press);
				ivDizhi.setImageResource(R.drawable.com_dizhi);
				ivHaze.setImageResource(R.drawable.com_haze);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu);

				OkHttpDetail(ivDafeng, true);
				break;
			case R.id.ivDizhi:
				ivHighTemp.setImageResource(R.drawable.com_hightemp);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui);
				ivWuran.setImageResource(R.drawable.com_wuran);
				ivShachen.setImageResource(R.drawable.com_shachen);
				ivGaowen.setImageResource(R.drawable.com_gaowen);
				ivFog.setImageResource(R.drawable.com_fog);
				ivDafeng.setImageResource(R.drawable.com_dafeng);
				ivDizhi.setImageResource(R.drawable.com_dizhi_press);
				ivHaze.setImageResource(R.drawable.com_haze);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu);

				OkHttpDetail(ivDizhi, true);
				break;
			case R.id.ivHaze:
				ivHighTemp.setImageResource(R.drawable.com_hightemp);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui);
				ivWuran.setImageResource(R.drawable.com_wuran);
				ivShachen.setImageResource(R.drawable.com_shachen);
				ivGaowen.setImageResource(R.drawable.com_gaowen);
				ivFog.setImageResource(R.drawable.com_fog);
				ivDafeng.setImageResource(R.drawable.com_dafeng);
				ivDizhi.setImageResource(R.drawable.com_dizhi);
				ivHaze.setImageResource(R.drawable.com_haze_press);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu);

				OkHttpDetail(ivHaze, true);
				break;
			case R.id.ivQiangduiliu:
				ivHighTemp.setImageResource(R.drawable.com_hightemp);
				ivLowTemp.setImageResource(R.drawable.com_lowtemp);
				ivJiangshui.setImageResource(R.drawable.com_jiangshui);
				ivWuran.setImageResource(R.drawable.com_wuran);
				ivShachen.setImageResource(R.drawable.com_shachen);
				ivGaowen.setImageResource(R.drawable.com_gaowen);
				ivFog.setImageResource(R.drawable.com_fog);
				ivDafeng.setImageResource(R.drawable.com_dafeng);
				ivDizhi.setImageResource(R.drawable.com_dizhi);
				ivHaze.setImageResource(R.drawable.com_haze);
				ivQiangduiliu.setImageResource(R.drawable.com_qiangduiliu_press);

				OkHttpDetail(ivQiangduiliu, true);
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
