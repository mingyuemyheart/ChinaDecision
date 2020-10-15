package com.china.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.common.MyApplication;
import com.china.utils.AuthorityUtil;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * 屏屏联动
 * @author shawn_sun
 */
public class ConnectionActivity extends BaseActivity implements OnClickListener{

	private Context mContext = null;
	private Socket socket = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_connection);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("连接电脑");
		TextView tvWifi = findViewById(R.id.tvWifi);
		TextView tvScane = findViewById(R.id.tvScane);
		tvScane.setOnClickListener(this);
		TextView tvPrompt = findViewById(R.id.tvPrompt);
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

	/**
	 * 申请电话权限
	 */
	private void checkCameraAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			startActivityForResult(new Intent(mContext, ZXingActivity.class), 1000);
		}else {
			if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(ConnectionActivity.this, new String[]{Manifest.permission.CAMERA}, AuthorityUtil.AUTHOR_CAMERA);
			}else {
				startActivityForResult(new Intent(mContext, ZXingActivity.class), 1000);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_CAMERA:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					startActivityForResult(new Intent(mContext, ZXingActivity.class), 1000);
				}else {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(ConnectionActivity.this, Manifest.permission.CAMERA)) {
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用相机权限，是否前往设置？");
					}
				}
				break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvScane:
			checkCameraAuthority();
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
