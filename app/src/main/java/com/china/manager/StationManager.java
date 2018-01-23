package com.china.manager;

import android.text.TextUtils;

import com.china.utils.OkHttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


public class StationManager {
	
	public static String precipitation1hResult = null;
	public static String precipitation3hResult = null;
	public static String precipitation6hResult = null;
	public static String precipitation12hResult = null;
	public static String precipitation24hResult = null;
	public static String balltempResult = null;
	public static String humidityResult = null;
	public static String visibilityResult = null;
	public static String airpressureResult = null;
	public static String windspeedResult = null;

	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData1(String url) {
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
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData3H(String url) {
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
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData6H(String url) {
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

	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData12H(String url) {
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
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData24H(String url) {
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
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData2(String url) {
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
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData3(String url) {
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
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData4(String url) {
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
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData5(String url) {
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
	
	/**
	 * 获取五中天气要素数据接口
	 * @param url
	 */
	public static void asyncGetMapData6(String url) {
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
	
}
