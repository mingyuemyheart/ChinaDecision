package com.china.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.china.common.MyApplication;
import com.china.view.MyDialog2;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class ShawnBaseActivity extends Activity {
	
	private MyDialog2 mDialog = null;
	private Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		if (Build.VERSION.SDK_INT >= 23) {
			ShawnBaseActivity.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		}
	}
	
	public void showDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog2(mContext);
		}
		mDialog.show();
	}

	public void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cancelDialog();//解决activity已经销毁，而还在调用dialog
	}

	public Socket socket = null;

	/**
	 * 缩放地图发送指令
	 */
	public void setMapEmit(float zoom, double lat, double lng) {
		try {
			if (socket == null) {
				socket = MyApplication.getSocket();
			}
			if (socket != null && socket.connected()) {
				JSONObject obj = new JSONObject();
				obj.put("computerInfo", MyApplication.computerInfo);
				JSONObject commond = new JSONObject();
				JSONObject index = new JSONObject();
				index.put("zoom", zoom);
				index.put("lat", lat);
				index.put("lng", lng);
				commond.put("data", index);
				obj.put("commond", commond);
				socket.emit("map", obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 点击返回发送指令
	 */
	public void setBackEmit() {
		try {
			if (socket == null) {
				socket = MyApplication.getSocket();
			}
			if (socket != null && socket.connected()) {
				JSONObject obj = new JSONObject();
				obj.put("computerInfo", MyApplication.computerInfo);
				obj.put("commond", "back");
				socket.emit("back", obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
