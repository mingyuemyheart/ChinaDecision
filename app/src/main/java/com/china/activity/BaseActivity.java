package com.china.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.china.common.MyApplication;
import com.china.manager.SystemStatusManager;
import com.china.view.MyDialog2;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;


public class BaseActivity extends Activity{
	
	private MyDialog2 mDialog = null;
	private Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setTranslucentStatus();
	}
	
	/**
	 * 设置状态栏背景状态
	 */
	@SuppressLint("InlinedApi") 
	private void setTranslucentStatus() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window win = getWindow();
			WindowManager.LayoutParams winParams = win.getAttributes();
			final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			winParams.flags |= bits;
			win.setAttributes(winParams);
		}
		SystemStatusManager tintManager = new SystemStatusManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(0);// 状态栏无背景
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
