package com.china.activity;

/**
 * 欢迎界面
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.china.R;
import com.china.common.CONST;
import com.china.utils.CommonUtil;

public class WelcomeActivity extends BaseActivity{

	private Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mContext = this;
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
	public boolean onKeyDown(int KeyCode, KeyEvent event){
		if (KeyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(KeyCode, event);
	}
	
}
