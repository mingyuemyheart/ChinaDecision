package com.china.activity;

/**
 * 欢迎界面
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.china.R;
import com.china.common.CONST;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;

public class WelcomeActivity extends BaseActivity{

	private Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mContext = this;
		checkMultiAuthority();
	}

	/**
	 * 申请多个权限
	 */
	private void checkMultiAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			commonControl();
		}else {
			AuthorityUtil.deniedList.clear();
			for (int i = 0; i < AuthorityUtil.allPermissions.length; i++) {
				if (ContextCompat.checkSelfPermission(mContext, AuthorityUtil.allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
					AuthorityUtil.deniedList.add(AuthorityUtil.allPermissions[i]);
				}
			}
			if (AuthorityUtil.deniedList.isEmpty()) {//所有权限都授予
				commonControl();
			}else {
				String[] permissions = AuthorityUtil.deniedList.toArray(new String[AuthorityUtil.deniedList.size()]);//将list转成数组
				ActivityCompat.requestPermissions(WelcomeActivity.this, permissions, AuthorityUtil.AUTHOR_MULTI);
			}
		}
	}

	private void commonControl() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				SharedPreferences sharedPreferences = getSharedPreferences(CONST.SHOWGUIDE, Context.MODE_PRIVATE);
				String version = sharedPreferences.getString(CONST.VERSION, "");
				if (!TextUtils.equals(version, CommonUtil.getVersion(mContext))) {
					startActivity(new Intent(mContext, GuideActivity.class));
				}else {
					startActivity(new Intent(mContext, LoginActivity.class));
				}
				finish();
			}
		}, 1000);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_MULTI:
				commonControl();
				break;
		}
	}

	@Override
	public boolean onKeyDown(int KeyCode, KeyEvent event){
		if (KeyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(KeyCode, event);
	}
	
}
