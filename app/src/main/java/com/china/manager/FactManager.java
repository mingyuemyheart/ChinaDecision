package com.china.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;

import com.china.utils.OkHttpUtil;

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
 * 实况监测管理类
 */
public class FactManager {

	private Context context;

	public static final String BROAD_RAIN1_COMPLETE = "broad_rain1_complete";//默认1小时图层加载完
	public static final String BROAD_RAIN1_LEGEND_COMPLETE = "broad_rain1_legend_complete";//默认1小时图例加载完
	
	public static String precipitation1hResult,precipitation3hResult,precipitation6hResult,precipitation12hResult,precipitation24hResult;
	public static String balltempResult, balltempMaxResult, balltempMinResult, balltempChangeResult;
	public static String humidityResult,visibilityResult,airpressureResult,windspeedResult;

	public static String precipitation1hImg,precipitation3hImg,precipitation6hImg,precipitation12hImg,precipitation24hImg;
	public static String balltempImg, balltempMaxImg, balltempMinImg, balltempChangeImg;
	public static String humidityImg,visibilityImg,airpressureImg,windspeedImg;

	public static String precipitation1hLegend,precipitation3hLegend,precipitation6hLegend,precipitation12hLegend,precipitation24hLegend;
	public static String balltempLegend, balltempMaxLegend, balltempMinLegend, balltempChangeLegend;
	public static String humidityLegend,visibilityLegend,windspeedLegend,airpressureLegend;

	private static List<String> precipitation1hColor = new ArrayList<>();
	private static List<String> precipitation3hColor = new ArrayList<>();
	private static List<String> precipitation6hColor = new ArrayList<>();
	private static List<String> precipitation12hColor = new ArrayList<>();
	private static List<String> precipitation24hColor = new ArrayList<>();
	private static List<String> balltempColor = new ArrayList<>();
	private static List<String> balltempMaxColor = new ArrayList<>();
	private static List<String> balltempMinColor = new ArrayList<>();
	private static List<String> balltempChangeColor = new ArrayList<>();
	private static List<String> humidityColor = new ArrayList<>();
	private static List<String> visibilityColor = new ArrayList<>();
	private static List<String> windspeedColor = new ArrayList<>();
	private static List<String> airpressureColor = new ArrayList<>();

	public FactManager(Context context) {
		this.context = context;
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpNewLayer();
				OkHttpLegend();
			}
		}).start();
	}

	/**
	 * 获取新的图层信息
	 */
	private void OkHttpNewLayer() {
		final String url = "http://decision-admin.tianqi.cn/Home/extra/decision_new_skjclayers";
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
						JSONArray array = new JSONArray(result);
						if (array.length() <= 0) {
							return;
						}
						JSONObject obj = array.getJSONObject(0);
						if (!obj.isNull("precipitation1h")) {
							JSONObject itemObj = obj.getJSONObject("precipitation1h");
							if (!itemObj.isNull("time")) {
								precipitation1hResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								precipitation1hImg = itemObj.getString("imgurl");
								Intent intent = new Intent();
								intent.setAction(BROAD_RAIN1_COMPLETE);
								context.sendBroadcast(intent);
							}
						}
						if (!obj.isNull("rainfall3")) {
							JSONObject itemObj = obj.getJSONObject("rainfall3");
							if (!itemObj.isNull("time")) {
								precipitation3hResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								precipitation3hImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("rainfall6")) {
							JSONObject itemObj = obj.getJSONObject("rainfall6");
							if (!itemObj.isNull("time")) {
								precipitation6hResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								precipitation6hImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("rainfall12")) {
							JSONObject itemObj = obj.getJSONObject("rainfall12");
							if (!itemObj.isNull("time")) {
								precipitation12hResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								precipitation12hImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("rainfall24")) {
							JSONObject itemObj = obj.getJSONObject("rainfall24");
							if (!itemObj.isNull("time")) {
								precipitation24hResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								precipitation24hImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("balltemp")) {
							JSONObject itemObj = obj.getJSONObject("balltemp");
							if (!itemObj.isNull("time")) {
								balltempResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								balltempImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("tempmax")) {
							JSONObject itemObj = obj.getJSONObject("tempmax");
							if (!itemObj.isNull("time")) {
								balltempMaxResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								balltempMaxImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("tempmin")) {
							JSONObject itemObj = obj.getJSONObject("tempmin");
							if (!itemObj.isNull("time")) {
								balltempMinResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								balltempMinImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("tempchange")) {
							JSONObject itemObj = obj.getJSONObject("tempchange");
							if (!itemObj.isNull("time")) {
								balltempChangeResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								balltempChangeImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("humidity")) {
							JSONObject itemObj = obj.getJSONObject("humidity");
							if (!itemObj.isNull("time")) {
								humidityResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								humidityImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("windspeed")) {
							JSONObject itemObj = obj.getJSONObject("windspeed");
							if (!itemObj.isNull("time")) {
								windspeedResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								windspeedImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("visibility")) {
							JSONObject itemObj = obj.getJSONObject("visibility");
							if (!itemObj.isNull("time")) {
								visibilityResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								visibilityImg = itemObj.getString("imgurl");
							}
						}
						if (!obj.isNull("airpressure")) {
							JSONObject itemObj = obj.getJSONObject("airpressure");
							if (!itemObj.isNull("time")) {
								airpressureResult = itemObj.getString("time");
							}
							if (!itemObj.isNull("imgurl")) {
								airpressureImg = itemObj.getString("imgurl");
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 获取图例
	 */
	private void OkHttpLegend() {
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
				String result = response.body().string();
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						if (!obj.isNull("jc_1xsjs")) {
							precipitation1hLegend = obj.getString("jc_1xsjs");
							Intent intent = new Intent();
							intent.setAction(BROAD_RAIN1_LEGEND_COMPLETE);
							context.sendBroadcast(intent);
						}
						if (!obj.isNull("jc_3xsjs")) {
							precipitation3hLegend = obj.getString("jc_3xsjs");
						}
						if (!obj.isNull("jc_6xsjs")) {
							precipitation6hLegend = obj.getString("jc_6xsjs");
						}
						if (!obj.isNull("jc_12xsjs")) {
							precipitation12hLegend = obj.getString("jc_12xsjs");
						}
						if (!obj.isNull("jc_24xsjs")) {
							precipitation24hLegend = obj.getString("jc_24xsjs");
						}
						if (!obj.isNull("jc_wdtl")) {
							balltempLegend = obj.getString("jc_wdtl");
						}
						if (!obj.isNull("jc_maxqw")) {
							balltempMaxLegend = obj.getString("jc_maxqw");
						}
						if (!obj.isNull("jc_minqw")) {
							balltempMinLegend = obj.getString("jc_minqw");
						}
						if (!obj.isNull("jc_changeqw")) {
							balltempChangeLegend = obj.getString("jc_changeqw");
						}
						if (!obj.isNull("jc_xdsdtl")) {
							humidityLegend = obj.getString("jc_xdsdtl");
						}
						if (!obj.isNull("jc_fltl")) {
							windspeedLegend = obj.getString("jc_fltl");
						}
						if (!obj.isNull("jc_njdtl")) {
							visibilityLegend = obj.getString("jc_njdtl");
						}
						if (!obj.isNull("jc_qytl")) {
							airpressureLegend = obj.getString("jc_qytl");
						}

						if (!obj.isNull("jb_1xsjs")) {
							precipitation1hColor.clear();
							JSONArray array = obj.getJSONArray("jb_1xsjs");
							for (int i = 0; i < array.length(); i++) {
								precipitation1hColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_3xsjs")) {
							precipitation3hColor.clear();
							JSONArray array = obj.getJSONArray("jb_3xsjs");
							for (int i = 0; i < array.length(); i++) {
								precipitation3hColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_6xsjs")) {
							precipitation6hColor.clear();
							JSONArray array = obj.getJSONArray("jb_6xsjs");
							for (int i = 0; i < array.length(); i++) {
								precipitation6hColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_12xsjs")) {
							precipitation12hColor.clear();
							JSONArray array = obj.getJSONArray("jb_12xsjs");
							for (int i = 0; i < array.length(); i++) {
								precipitation12hColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_24xsjs")) {
							precipitation24hColor.clear();
							JSONArray array = obj.getJSONArray("jb_24xsjs");
							for (int i = 0; i < array.length(); i++) {
								precipitation24hColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_fltl")) {
							windspeedColor.clear();
							JSONArray array = obj.getJSONArray("jb_fltl");
							for (int i = 0; i < array.length(); i++) {
								windspeedColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_njdtl")) {
							visibilityColor.clear();
							JSONArray array = obj.getJSONArray("jb_njdtl");
							for (int i = 0; i < array.length(); i++) {
								visibilityColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_qytl")) {
							airpressureColor.clear();
							JSONArray array = obj.getJSONArray("jb_qytl");
							for (int i = 0; i < array.length(); i++) {
								airpressureColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_wdtl")) {
							balltempColor.clear();
							JSONArray array = obj.getJSONArray("jb_wdtl");
							for (int i = 0; i < array.length(); i++) {
								balltempColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_maxqw")) {
							balltempMaxColor.clear();
							JSONArray array = obj.getJSONArray("jb_maxqw");
							for (int i = 0; i < array.length(); i++) {
								balltempMaxColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_minqw")) {
							balltempMinColor.clear();
							JSONArray array = obj.getJSONArray("jb_minqw");
							for (int i = 0; i < array.length(); i++) {
								balltempMinColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_changeqw")) {
							balltempChangeColor.clear();
							JSONArray array = obj.getJSONArray("jb_changeqw");
							for (int i = 0; i < array.length(); i++) {
								balltempChangeColor.add(array.getString(i));
							}
						}
						if (!obj.isNull("jb_xdsdtl")) {
							humidityColor.clear();
							JSONArray array = obj.getJSONArray("jb_xdsdtl");
							for (int i = 0; i < array.length(); i++) {
								humidityColor.add(array.getString(i));
							}
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	public static int pointColor(int value, String number) {
		String color = "#a5f38d";
		if (value == 1) {
			for (int i = 0; i < precipitation1hColor.size(); i++) {
				String[] colorStr = precipitation1hColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 13) {
			for (int i = 0; i < precipitation3hColor.size(); i++) {
				String[] colorStr = precipitation3hColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 16) {
			for (int i = 0; i < precipitation6hColor.size(); i++) {
				String[] colorStr = precipitation6hColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 112) {
			for (int i = 0; i < precipitation12hColor.size(); i++) {
				String[] colorStr = precipitation12hColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 124) {
			for (int i = 0; i < precipitation24hColor.size(); i++) {
				String[] colorStr = precipitation24hColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 21) {
			for (int i = 0; i < balltempColor.size(); i++) {
				String[] colorStr = balltempColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 22) {
			for (int i = 0; i < balltempMaxColor.size(); i++) {
				String[] colorStr = balltempMaxColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 23) {
			for (int i = 0; i < balltempMinColor.size(); i++) {
				String[] colorStr = balltempMinColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 24) {
			for (int i = 0; i < balltempChangeColor.size(); i++) {
				String[] colorStr = balltempChangeColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 3) {
			for (int i = 0; i < humidityColor.size(); i++) {
				String[] colorStr = humidityColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 4) {
			for (int i = 0; i < visibilityColor.size(); i++) {
				String[] colorStr = visibilityColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 5) {
			for (int i = 0; i < airpressureColor.size(); i++) {
				String[] colorStr = airpressureColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		} else if (value == 6) {
			for (int i = 0; i < windspeedColor.size(); i++) {
				String[] colorStr = windspeedColor.get(i).split(",");
				if (Float.valueOf(number) >= Float.valueOf(colorStr[0]) && Float.valueOf(number) < Float.valueOf(colorStr[1])) {
					color = colorStr[2];
				}
			}
		}
		return Color.parseColor(color);
	}
	
}
