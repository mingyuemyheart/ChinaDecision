package com.china.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.common.MyApplication;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


/**
 * 屏屏联动
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class ConnectionActivity extends BaseActivity implements OnClickListener{

	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvWifi = null;
	private TextView tvScane = null;
	private TextView tvPrompt = null;
	private Socket socket = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connection);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("连接电脑");
		tvWifi = (TextView) findViewById(R.id.tvWifi);
		tvScane = (TextView) findViewById(R.id.tvScane);
		tvScane.setOnClickListener(this);
		tvPrompt = (TextView) findViewById(R.id.tvPrompt);
		tvPrompt.setText("扫描二维码：快速连接+"+"\""+"中国气象"+"\""+"触屏版");

		NetworkInfo info = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
				WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				if (!TextUtils.isEmpty(wifiInfo.getSSID())) {
					String name = wifiInfo.getSSID().replace("\"", "");
					tvWifi.setText("当前网络："+name);
				}
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvScane:
			startActivityForResult(new Intent(mContext, ZXingActivity.class), 1000);
			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 1000:
					Bundle bundle = data.getExtras();
					if (bundle != null) {
						//http://61.4.184.177:8899/?computerInfo=1a6a53c2cb5b68580875c25f114a91d0
						String result = bundle.getString("result");
						if (!TextUtils.isEmpty(result)) {
							String[] array = result.split("=");
							MyApplication.computerInfo = array[1];
							try {
								socket = IO.socket(result);
								socket.connect();
								MyApplication.setSocket(socket);
								JSONObject obj = new JSONObject();
								obj.put("computerInfo", MyApplication.computerInfo);
								obj.put("commond", "hideQR");
								socket.emit("hideQR", obj);

								startActivity(new Intent(mContext, ScreenActivity.class));
							} catch (URISyntaxException e) {
								e.printStackTrace();
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
					break;

				default:
					break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (socket != null) {
			socket.disconnect();
			socket = null;
		}
	}
}