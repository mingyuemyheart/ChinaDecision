package com.china.manager;

import android.text.TextUtils;

import com.china.utils.OkHttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


public class StationManager {
	
	public static String precipitation1hResult,precipitation3hResult,precipitation6hResult,precipitation12hResult,precipitation24hResult;
	public static String balltempResult, balltempMaxResult, balltempMinResult, balltempChangeResult;
	public static String humidityResult = null;
	public static String visibilityResult = null;
	public static String airpressureResult = null;
	public static String windspeedResult = null;

	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData1(final String url) {
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
							precipitation1hResult = result;
						}
					}
				});
			}
		}).start();
	}
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData3H(final String url) {
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
							precipitation3hResult = result;
						}
					}
				});
			}
		}).start();
	}
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData6H(final String url) {
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
							precipitation6hResult = result;
						}
					}
				});
			}
		}).start();
	}

	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData12H(final String url) {
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
							precipitation12hResult = result;
						}
					}
				});
			}
		}).start();
	}
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData24H(final String url) {
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
							precipitation24hResult = result;
						}
					}
				});
			}
		}).start();
	}
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData21(final String url) {
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
							balltempResult = result;
						}
					}
				});
			}
		}).start();
	}

	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData22(final String url) {
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
							balltempMaxResult = result;
						}
					}
				});
			}
		}).start();
	}

	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData23(final String url) {
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
							balltempMinResult = result;
						}
					}
				});
			}
		}).start();
	}

	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData24(final String url) {
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
							balltempChangeResult = result;
						}
					}
				});
			}
		}).start();
	}
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData3(final String url) {
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
							humidityResult = result;
						}
					}
				});
			}
		}).start();
	}
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData4(final String url) {
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
							visibilityResult = result;
						}
					}
				});
			}
		}).start();
	}
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData5(final String url) {
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
							airpressureResult = result;
						}
					}
				});
			}
		}).start();
	}
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData6(final String url) {
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
							windspeedResult = result;
						}
					}
				});
			}
		}).start();
	}
	
}
