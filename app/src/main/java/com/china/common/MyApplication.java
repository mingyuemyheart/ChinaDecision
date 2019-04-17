package com.china.common;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

import com.github.nkzawa.socketio.client.Socket;
import com.tendcloud.tenddata.TCAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

import java.lang.reflect.Method;
import java.util.Random;

public class MyApplication extends Application{

	public static String appKey = "57f06441e0f55a8930001bdb";
	public static boolean isShowNavigationBar = true;//是否显示导航栏
	public static String computerInfo = "";
	private static Socket mSocket;

	public static Socket getSocket() {
		return mSocket;
	}
	public static void setSocket(Socket socket) {
		mSocket = socket;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		getUserInfo(this);

		//判断底部导航栏是否显示
		if (checkDeviceHasNavigationBar(this)) {
			registerNavigationBar();
		}

		//umeng分享的平台注册
		UMConfigure.init(this, appKey, "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
		PlatformConfig.setWeixin("wx1fa67f698f7053ad", "f3fc51dcb4518eb80bff808acb10c409");
		PlatformConfig.setQQZone("1105876438", "tH05WZYOjbInVhQq");
		PlatformConfig.setDing("dingoaqfmkgk4d9lo7gbmq");
		UMConfigure.setLogEnabled(false);

		//TalkingData统计
		String[] platforms = {"HuaWei Store", "XiaoMi Store", "Tencent Store", "OPPO Store", "VIVO Store", "LeShi Store", "ATest"};
		int i = new Random().nextInt(platforms.length);
		TCAgent.init(this.getApplicationContext(), "80C44BE2E53D4D4DB0814115BBF175F6", platforms[i]);
		TCAgent.setReportUncaughtExceptions(true);
	}

	/**
	 * 获取是否存在NavigationBar
	 * @param context
	 * @return
	 */
	public static boolean checkDeviceHasNavigationBar(Context context) {
		boolean hasNavigationBar = false;
		try {
			int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
			if (id > 0) {
				hasNavigationBar = context.getResources().getBoolean(id);
			}
			Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
			Method m = systemPropertiesClass.getMethod("get", String.class);
			String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
			if ("1".equals(navBarOverride)) {
				hasNavigationBar = false;
			} else if ("0".equals(navBarOverride)) {
				hasNavigationBar = true;
			}
		} catch (Exception e) {

		}
		return hasNavigationBar;
	}

	/**
	 * 注册导航栏监听
	 */
	private void registerNavigationBar() {
		getContentResolver().registerContentObserver(Settings.Global.getUriFor("navigationbar_is_min"), true, mNavigationStatusObserver);
		int navigationBarIsMin = Settings.Global.getInt(getContentResolver(), "navigationbar_is_min", 0);
		if (navigationBarIsMin == 1) {
			//导航键隐藏了
			isShowNavigationBar = false;
		} else {
			//导航键显示了
			isShowNavigationBar = true;
		}
	}

	private ContentObserver mNavigationStatusObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			int navigationBarIsMin = Settings.Global.getInt(getContentResolver(), "navigationbar_is_min", 0);
			if (navigationBarIsMin == 1) {
				//导航键隐藏了
				isShowNavigationBar = false;
			} else {
				//导航键显示了
				isShowNavigationBar = true;
			}
			if (navigationListener != null) {
				navigationListener.showNavigation(isShowNavigationBar);
			}
		}
	};


	public interface NavigationListener {
		void showNavigation(boolean show);
	}

	private static NavigationListener navigationListener;

	public NavigationListener getNavigationListener() {
		return navigationListener;
	}

	public static void setNavigationListener(NavigationListener listener) {
		navigationListener = listener;
	}


	//本地保存用户信息参数
	public static String USERGROUP = "";//用户组
	public static String UID = "";//用户id
	public static String USERNAME = "";
	public static String PASSWORD = "";

	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		private static final String userGroup = "userGroup";
		private static final String uid = "uid";
		private static final String userName = "uName";
		private static final String passWord = "pwd";
	}

	/**
	 * 清除用户信息
	 */
	public static void clearUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();
		USERGROUP = "";
		UID = "";
		USERNAME = "";
		PASSWORD = "";
	}

	/**
	 * 保存用户信息
	 */
	public static void saveUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(UserInfo.userGroup, USERGROUP);
		editor.putString(UserInfo.uid, UID);
		editor.putString(UserInfo.userName, USERNAME);
		editor.putString(UserInfo.passWord, PASSWORD);
		editor.apply();
	}

	/**
	 * 获取用户信息
	 */
	public static void getUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		USERGROUP = sharedPreferences.getString(UserInfo.userGroup, "");
		UID = sharedPreferences.getString(UserInfo.uid, "");
		USERNAME = sharedPreferences.getString(UserInfo.userName, "");
		PASSWORD = sharedPreferences.getString(UserInfo.passWord, "");
	}

	/**
	 * 保存columnIds
	 */
	public static void saveColumnIds(Context context, String columnIds) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("COLUMNIDS", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("columnIds", columnIds);
		editor.apply();
	}

	/**
	 * 获取columnIds
	 */
	public static String getColumnIds(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("COLUMNIDS", Context.MODE_PRIVATE);
		return sharedPreferences.getString("columnIds", "");
	}

}
