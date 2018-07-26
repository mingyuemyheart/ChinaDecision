package com.china.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.StreamFactListAdapter;
import com.china.common.CONST;
import com.china.dto.StreamFactDto;
import com.china.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 强对流天气实况列表
 * @author shawn_sun
 *
 */
public class StreamFactListFragment extends Fragment implements View.OnClickListener {
	
	private ListView listView;
	private StreamFactListAdapter mAdapter;
	private List<StreamFactDto> dataList = new ArrayList<>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH");
	private TextView tvName,tvStationName,tvProvince,tvStationId,tvValue,tvUnit,tvPrompt;
	private SwipeRefreshLayout refreshLayout;//下拉刷新布局
	private ImageView ivRank;
	private LinearLayout llRank;
	private boolean isDesc = true;//是否为降序排序

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_stream_fact_list, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		initWidget(view);
		initListView(view);
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = view.findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 400);
		refreshLayout.post(new Runnable() {
			@Override
			public void run() {
				refreshLayout.setRefreshing(true);
			}
		});
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}

	private void refresh() {
		String name = getArguments().getString(CONST.ACTIVITY_NAME);
		String end = sdf1.format(new Date())+"时";
		String start = sdf1.format(new Date().getTime()-1000*60*60)+"时";
		tvName.setText("全国1小时"+name+"实况"+"("+start+"-"+end+")");

		dataList.clear();
		OkHttpList();
	}

	private void initWidget(View view) {
		tvName = view.findViewById(R.id.tvName);
		tvStationName = view.findViewById(R.id.tvStationName);
		tvProvince = view.findViewById(R.id.tvProvince);
		tvStationId = view.findViewById(R.id.tvStationId);
		tvValue = view.findViewById(R.id.tvValue);
		tvUnit = view.findViewById(R.id.tvUnit);
		tvPrompt = view.findViewById(R.id.tvPrompt);
		ivRank = view.findViewById(R.id.ivRank);
		llRank = view.findViewById(R.id.llRank);
		llRank.setOnClickListener(this);

		refresh();
	}

	private void initListView(View view) {
		listView = view.findViewById(R.id.listView);
		mAdapter = new StreamFactListAdapter(getActivity(), dataList);
		listView.setAdapter(mAdapter);
	}

	/**
	 * 获取数据
	 */
	private void OkHttpList() {
		final String url = String.format("http://scapi.weather.com.cn/weather/getServerWeather?time=%s&test=ncg", sdf2.format(new Date()));
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);

										String columnName = tvName.getText().toString();
										if (columnName.contains("降水")) {
											tvValue.setText("降水量");
											tvUnit.setText("("+getString(R.string.unit_mm)+")");
											if (!obj.isNull("PRE")) {
												JSONObject object = obj.getJSONObject("PRE");
												if (!object.isNull("data")) {
													dataList.clear();
													JSONArray array = object.getJSONArray("data");
													for (int i = 0; i < array.length(); i++) {
														StreamFactDto dto = new StreamFactDto();
														JSONObject itemObj = array.getJSONObject(i);
														if (!itemObj.isNull("Lat")) {
															dto.lat = itemObj.getDouble("Lat");
														}
														if (!itemObj.isNull("Lon")) {
															dto.lng = itemObj.getDouble("Lon");
														}
														if (!itemObj.isNull("Station_ID_C")) {
															dto.stationId = itemObj.getString("Station_ID_C");
														}
														if (!itemObj.isNull("Station_Name")) {
															dto.stationName = itemObj.getString("Station_Name");
														}
														if (!itemObj.isNull("Province")) {
															dto.province = itemObj.getString("Province");
														}
														if (!itemObj.isNull("City")) {
															dto.city = itemObj.getString("City");
														}
														if (!itemObj.isNull("Cnty")) {
															dto.dis = itemObj.getString("Cnty");
														}
														if (!itemObj.isNull("PRE_1h")) {
															dto.pre1h = itemObj.getString("PRE_1h");
														}
														if (!dto.pre1h.contains("99999")) {
															double pre1h = Double.parseDouble(dto.pre1h);
															if (pre1h <= 300) {//过滤掉300mm以上
																dataList.add(dto);
															}
														}
													}
												}
											}
										}else if (columnName.contains("大风")) {
											tvValue.setText("风速");
											tvUnit.setText("("+getString(R.string.unit_speed)+")");
											if (!obj.isNull("WIN")) {
												JSONObject object = obj.getJSONObject("WIN");
												if (!object.isNull("data")) {
													dataList.clear();
													JSONArray array = object.getJSONArray("data");
													for (int i = 0; i < array.length(); i++) {
														StreamFactDto dto = new StreamFactDto();
														JSONObject itemObj = array.getJSONObject(i);
														if (!itemObj.isNull("Lat")) {
															dto.lat = itemObj.getDouble("Lat");
														}
														if (!itemObj.isNull("Lon")) {
															dto.lng = itemObj.getDouble("Lon");
														}
														if (!itemObj.isNull("Station_ID_C")) {
															dto.stationId = itemObj.getString("Station_ID_C");
														}
														if (!itemObj.isNull("Station_Name")) {
															dto.stationName = itemObj.getString("Station_Name");
														}
														if (!itemObj.isNull("Province")) {
															dto.province = itemObj.getString("Province");
														}
														if (!itemObj.isNull("City")) {
															dto.city = itemObj.getString("City");
														}
														if (!itemObj.isNull("Cnty")) {
															dto.dis = itemObj.getString("Cnty");
														}
														if (!itemObj.isNull("WIN_S_Max")) {
															dto.windS = itemObj.getString("WIN_S_Max");
														}
														if (!itemObj.isNull("WIN_D_S_Max")) {
															dto.windD = itemObj.getString("WIN_D_S_Max");
														}
														if (!dto.windS.contains("99999")) {
															double windS = Double.parseDouble(dto.windS);
															if (windS > 17 && windS < 60) {//过滤掉17m/s以下、60m/s以上
																dataList.add(dto);
															}
														}
													}
												}
											}
										}else if (columnName.contains("冰雹")) {
											tvValue.setText("冰雹直径");
											tvUnit.setText("("+getString(R.string.unit_mm)+")");
											if (!obj.isNull("HAIL")) {
												JSONObject object = obj.getJSONObject("HAIL");
												if (!object.isNull("data")) {
													dataList.clear();
													JSONArray array = object.getJSONArray("data");
													for (int i = 0; i < array.length(); i++) {
														StreamFactDto dto = new StreamFactDto();
														JSONObject itemObj = array.getJSONObject(i);
														if (!itemObj.isNull("Lat")) {
															dto.lat = itemObj.getDouble("Lat");
														}
														if (!itemObj.isNull("Lon")) {
															dto.lng = itemObj.getDouble("Lon");
														}
														if (!itemObj.isNull("Station_ID_C")) {
															dto.stationId = itemObj.getString("Station_ID_C");
														}
														if (!itemObj.isNull("Station_Name")) {
															dto.stationName = itemObj.getString("Station_Name");
														}
														if (!itemObj.isNull("Province")) {
															dto.province = itemObj.getString("Province");
														}
														if (!itemObj.isNull("City")) {
															dto.city = itemObj.getString("City");
														}
														if (!itemObj.isNull("Cnty")) {
															dto.dis = itemObj.getString("Cnty");
														}
														if (!itemObj.isNull("HAIL_Diam_Max")) {
															dto.hail = itemObj.getString("HAIL_Diam_Max");
														}
														if (!dto.hail.contains("99999")) {
															dataList.add(dto);
														}
													}
												}
											}
										}else {
											tvValue.setText("闪电强度");
											tvUnit.setText("(10KA)");
											if (!obj.isNull("Lit")) {
												JSONObject object = obj.getJSONObject("Lit");
												dataList.clear();
												if (!object.isNull("data_1")) {
													JSONArray array = object.getJSONArray("data_1");
													for (int i = 0; i < array.length(); i++) {
														StreamFactDto dto = new StreamFactDto();
														JSONObject itemObj = array.getJSONObject(i);
														if (!itemObj.isNull("Lat")) {
															dto.lat = itemObj.getDouble("Lat");
														}
														if (!itemObj.isNull("Lon")) {
															dto.lng = itemObj.getDouble("Lon");
														}
														if (!itemObj.isNull("Station_ID_C")) {
															dto.stationId = itemObj.getString("Station_ID_C");
														}
														if (!itemObj.isNull("Station_Name")) {
															dto.stationName = itemObj.getString("Station_Name");
														}
														if (!itemObj.isNull("Lit_Prov")) {
															dto.province = itemObj.getString("Lit_Prov");
														}
														if (!itemObj.isNull("Lit_City")) {
															dto.city = itemObj.getString("Lit_City");
														}
														if (!itemObj.isNull("Lit_Cnty")) {
															dto.dis = itemObj.getString("Lit_Cnty");
														}
														if (!itemObj.isNull("Lit_Current")) {
															dto.lighting = itemObj.getString("Lit_Current");
														}
														dto.lightingType = 1;
														if (!dto.lighting.contains("99999")) {
															dataList.add(dto);
														}
													}
												}
												if (!object.isNull("data_2")) {
													JSONArray array = object.getJSONArray("data_2");
													for (int i = 0; i < array.length(); i++) {
														StreamFactDto dto = new StreamFactDto();
														JSONObject itemObj = array.getJSONObject(i);
														if (!itemObj.isNull("Lat")) {
															dto.lat = itemObj.getDouble("Lat");
														}
														if (!itemObj.isNull("Lon")) {
															dto.lng = itemObj.getDouble("Lon");
														}
														if (!itemObj.isNull("Station_ID_C")) {
															dto.stationId = itemObj.getString("Station_ID_C");
														}
														if (!itemObj.isNull("Station_Name")) {
															dto.stationName = itemObj.getString("Station_Name");
														}
														if (!itemObj.isNull("Lit_Prov")) {
															dto.province = itemObj.getString("Lit_Prov");
														}
														if (!itemObj.isNull("Lit_City")) {
															dto.city = itemObj.getString("Lit_City");
														}
														if (!itemObj.isNull("Lit_Cnty")) {
															dto.dis = itemObj.getString("Lit_Cnty");
														}
														if (!itemObj.isNull("Lit_Current")) {
															dto.lighting = itemObj.getString("Lit_Current");
														}
														dto.lightingType = 2;
														if (!dto.lighting.contains("99999")) {
															dataList.add(dto);
														}
													}
												}
												if (!object.isNull("data_3")) {
													JSONArray array = object.getJSONArray("data_3");
													for (int i = 0; i < array.length(); i++) {
														StreamFactDto dto = new StreamFactDto();
														JSONObject itemObj = array.getJSONObject(i);
														if (!itemObj.isNull("Lat")) {
															dto.lat = itemObj.getDouble("Lat");
														}
														if (!itemObj.isNull("Lon")) {
															dto.lng = itemObj.getDouble("Lon");
														}
														if (!itemObj.isNull("Station_ID_C")) {
															dto.stationId = itemObj.getString("Station_ID_C");
														}
														if (!itemObj.isNull("Station_Name")) {
															dto.stationName = itemObj.getString("Station_Name");
														}
														if (!itemObj.isNull("Lit_Prov")) {
															dto.province = itemObj.getString("Lit_Prov");
														}
														if (!itemObj.isNull("Lit_City")) {
															dto.city = itemObj.getString("Lit_City");
														}
														if (!itemObj.isNull("Lit_Cnty")) {
															dto.dis = itemObj.getString("Lit_Cnty");
														}
														if (!itemObj.isNull("Lit_Current")) {
															dto.lighting = itemObj.getString("Lit_Current");
														}
														dto.lightingType = 3;
														if (!dto.lighting.contains("99999")) {
															dataList.add(dto);
														}
													}
												}
												if (!object.isNull("data_4")) {
													JSONArray array = object.getJSONArray("data_4");
													for (int i = 0; i < array.length(); i++) {
														StreamFactDto dto = new StreamFactDto();
														JSONObject itemObj = array.getJSONObject(i);
														if (!itemObj.isNull("Lat")) {
															dto.lat = itemObj.getDouble("Lat");
														}
														if (!itemObj.isNull("Lon")) {
															dto.lng = itemObj.getDouble("Lon");
														}
														if (!itemObj.isNull("Station_ID_C")) {
															dto.stationId = itemObj.getString("Station_ID_C");
														}
														if (!itemObj.isNull("Station_Name")) {
															dto.stationName = itemObj.getString("Station_Name");
														}
														if (!itemObj.isNull("Lit_Prov")) {
															dto.province = itemObj.getString("Lit_Prov");
														}
														if (!itemObj.isNull("Lit_City")) {
															dto.city = itemObj.getString("Lit_City");
														}
														if (!itemObj.isNull("Lit_Cnty")) {
															dto.dis = itemObj.getString("Lit_Cnty");
														}
														if (!itemObj.isNull("Lit_Current")) {
															dto.lighting = itemObj.getString("Lit_Current");
														}
														dto.lightingType = 4;
														if (!dto.lighting.contains("99999")) {
															dataList.add(dto);
														}
													}
												}
												if (!object.isNull("data_5")) {
													JSONArray array = object.getJSONArray("data_5");
													for (int i = 0; i < array.length(); i++) {
														StreamFactDto dto = new StreamFactDto();
														JSONObject itemObj = array.getJSONObject(i);
														if (!itemObj.isNull("Lat")) {
															dto.lat = itemObj.getDouble("Lat");
														}
														if (!itemObj.isNull("Lon")) {
															dto.lng = itemObj.getDouble("Lon");
														}
														if (!itemObj.isNull("Station_ID_C")) {
															dto.stationId = itemObj.getString("Station_ID_C");
														}
														if (!itemObj.isNull("Station_Name")) {
															dto.stationName = itemObj.getString("Station_Name");
														}
														if (!itemObj.isNull("Lit_Prov")) {
															dto.province = itemObj.getString("Lit_Prov");
														}
														if (!itemObj.isNull("Lit_City")) {
															dto.city = itemObj.getString("Lit_City");
														}
														if (!itemObj.isNull("Lit_Cnty")) {
															dto.dis = itemObj.getString("Lit_Cnty");
														}
														if (!itemObj.isNull("Lit_Current")) {
															dto.lighting = itemObj.getString("Lit_Current");
														}
														dto.lightingType = 5;
														if (!dto.lighting.contains("99999")) {
															dataList.add(dto);
														}
													}
												}
												if (!object.isNull("data_6")) {
													JSONArray array = object.getJSONArray("data_6");
													for (int i = 0; i < array.length(); i++) {
														StreamFactDto dto = new StreamFactDto();
														JSONObject itemObj = array.getJSONObject(i);
														if (!itemObj.isNull("Lat")) {
															dto.lat = itemObj.getDouble("Lat");
														}
														if (!itemObj.isNull("Lon")) {
															dto.lng = itemObj.getDouble("Lon");
														}
														if (!itemObj.isNull("Station_ID_C")) {
															dto.stationId = itemObj.getString("Station_ID_C");
														}
														if (!itemObj.isNull("Station_Name")) {
															dto.stationName = itemObj.getString("Station_Name");
														}
														if (!itemObj.isNull("Lit_Prov")) {
															dto.province = itemObj.getString("Lit_Prov");
														}
														if (!itemObj.isNull("Lit_City")) {
															dto.city = itemObj.getString("Lit_City");
														}
														if (!itemObj.isNull("Lit_Cnty")) {
															dto.dis = itemObj.getString("Lit_Cnty");
														}
														if (!itemObj.isNull("Lit_Current")) {
															dto.lighting = itemObj.getString("Lit_Current");
														}
														dto.lightingType = 6;
														if (!dto.lighting.contains("99999")) {
															dataList.add(dto);
														}
													}
												}

											}
										}

										if (mAdapter != null) {
											mAdapter.columnName = columnName;
											mAdapter.notifyDataSetChanged();
										}
										if (dataList.size() <= 0) {
											tvPrompt.setVisibility(View.VISIBLE);
										}
										refreshLayout.setRefreshing(false);

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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llRank:
				String columnName = tvName.getText().toString();
				if (isDesc) {
					ivRank.setImageResource(R.drawable.icon_range_up);
					if (columnName.contains("降水")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.pre1h) || TextUtils.isEmpty(arg1.pre1h)) {
									return 0;
								}else {
									return Double.valueOf(arg0.pre1h).compareTo(Double.valueOf(arg1.pre1h));
								}
							}
						});
					}else if (columnName.contains("大风")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.windS) || TextUtils.isEmpty(arg1.windS)) {
									return 0;
								}else {
									return Double.valueOf(arg0.windS).compareTo(Double.valueOf(arg1.windS));
								}
							}
						});
					}else if (columnName.contains("冰雹")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.hail) || TextUtils.isEmpty(arg1.hail)) {
									return 0;
								}else {
									return Double.valueOf(arg0.hail).compareTo(Double.valueOf(arg1.hail));
								}
							}
						});
					}else if (columnName.contains("闪电")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.lighting) || TextUtils.isEmpty(arg1.lighting)) {
									return 0;
								}else {
									return Double.valueOf(arg0.lighting).compareTo(Double.valueOf(arg1.lighting));
								}
							}
						});
					}
				}else {
					ivRank.setImageResource(R.drawable.icon_range_down);
					if (columnName.contains("降水")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.pre1h) || TextUtils.isEmpty(arg1.pre1h)) {
									return 0;
								}else {
									return Double.valueOf(arg1.pre1h).compareTo(Double.valueOf(arg0.pre1h));
								}
							}
						});
					}else if (columnName.contains("大风")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.windS) || TextUtils.isEmpty(arg1.windS)) {
									return 0;
								}else {
									return Double.valueOf(arg1.windS).compareTo(Double.valueOf(arg0.windS));
								}
							}
						});
					}else if (columnName.contains("冰雹")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.hail) || TextUtils.isEmpty(arg1.hail)) {
									return 0;
								}else {
									return Double.valueOf(arg1.hail).compareTo(Double.valueOf(arg0.hail));
								}
							}
						});
					}else if (columnName.contains("闪电")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.lighting) || TextUtils.isEmpty(arg1.lighting)) {
									return 0;
								}else {
									return Double.valueOf(arg1.lighting).compareTo(Double.valueOf(arg0.lighting));
								}
							}
						});
					}
				}
				isDesc = !isDesc;

				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}

				break;
		}
	}

}
