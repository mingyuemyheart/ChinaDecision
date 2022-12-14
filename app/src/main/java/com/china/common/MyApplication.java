package com.china.common;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.china.activity.PDFActivity;
import com.china.activity.WebviewActivity;
import com.china.utils.CrashHandler;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;
import com.umeng.socialize.PlatformConfig;

import org.json.JSONException;
import org.json.JSONObject;

public class MyApplication extends Application{

	public static String appKey = "57f06441e0f55a8930001bdb", msgSecret = "41c04531f1b17b576505f88115745fcc";
	private static PushAgent mPushAgent;
	public static String DEVICETOKEN = "";
	public static String computerInfo = "";
	private static String appTheme = "0";
	public static String getAppTheme() {
		return appTheme;
	}
	public static void setTheme(String theme) {
		appTheme = theme;
	}

	private static String top_img,top_img_url,top_img_title;

	public static String getTop_img() {
		return top_img;
	}

	public static void setTop_img(String top_img) {
		MyApplication.top_img = top_img;
	}

	public static String getTop_img_url() {
		return top_img_url;
	}

	public static void setTop_img_url(String top_img_url) {
		MyApplication.top_img_url = top_img_url;
	}

	public static String getTop_img_title() {
		return top_img_title;
	}

	public static void setTop_img_title(String top_img_title) {
		MyApplication.top_img_title = top_img_title;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());

		getUserInfo(this);
		if (!TextUtils.equals(MyApplication.USERGROUP, "17")) {//公众用户不推送
			initUmeng();
		}
	}

	//本地保存用户信息参数
	public static String USERGROUP = "";//用户组
	public static String UID = "";//用户id
	public static String USERNAME = "";
	public static String PASSWORD = "";
	public static String WXACCOUNT = "";
	public static String NICKNAME = "";
	public static String MOBILE = "";
	public static String UNIT = "";
	public static String PORTRAIT = "";
	public static String TOKEN = "";
	public static boolean FACTENABLE;//格点实况

	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		private static final String userGroup = "userGroup";
		private static final String uid = "uid";
		private static final String userName = "uName";
		private static final String passWord = "pwd";
		private static final String wxaccount = "wxaccount";
		private static final String nickname = "nickname";
		private static final String mobile = "mobile";
		private static final String unit = "unit";
		private static final String portrait = "portrait";
		private static final String token = "token";
		private static final String factEnable = "factEnable";
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
		WXACCOUNT = "";
		NICKNAME = "";
		MOBILE = "";
		UNIT = "";
		PORTRAIT = "";
		TOKEN = "";
		FACTENABLE = false;
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
		editor.putString(UserInfo.wxaccount, WXACCOUNT);
		editor.putString(UserInfo.nickname, NICKNAME);
		editor.putString(UserInfo.mobile, MOBILE);
		editor.putString(UserInfo.unit, UNIT);
		editor.putString(UserInfo.portrait, PORTRAIT);
		editor.putString(UserInfo.token, TOKEN);
		editor.putBoolean(UserInfo.factEnable, FACTENABLE);
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
		WXACCOUNT = sharedPreferences.getString(UserInfo.wxaccount, "");
		NICKNAME = sharedPreferences.getString(UserInfo.nickname, "");
		MOBILE = sharedPreferences.getString(UserInfo.mobile, "");
		UNIT = sharedPreferences.getString(UserInfo.unit, "");
		PORTRAIT = sharedPreferences.getString(UserInfo.portrait, "");
		TOKEN = sharedPreferences.getString(UserInfo.token, "");
		FACTENABLE = sharedPreferences.getBoolean(UserInfo.factEnable, false);
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

	/**
	 * 初始化umeng
	 */
	private void initUmeng() {
		//umeng分享的平台注册
		UMConfigure.init(this, appKey, "umeng", UMConfigure.DEVICE_TYPE_PHONE, msgSecret);
//		PlatformConfig.setWeixin("wx1fa67f698f7053ad", "f3fc51dcb4518eb80bff808acb10c409");
		PlatformConfig.setWeixin("wxaccddc891ca42427", "c8b92831311597b3a7511bafc2f394ef");
		PlatformConfig.setQQZone("1105876438", "tH05WZYOjbInVhQq");
		PlatformConfig.setDing("dingoaqfmkgk4d9lo7gbmq");
		UMConfigure.setLogEnabled(false);

		registerUmengPush();

		//华为推送
//		HuaWeiRegister.register(this);
//
//		//小米推送
//        MiPushRegistar.register(this, "2882303761517530805", "5461753056805");
//
//		//魅族推送
//		MeizuRegister.register(this, "127840", "f741a6884d324eb2b67941946aa0b305");
	}

	/**
	 * 注册umeng推送
	 */
	private void registerUmengPush() {
		mPushAgent = PushAgent.getInstance(this);

		//参数number可以设置为0~10之间任意整数。当参数为0时，表示不合并通知
		mPushAgent.setDisplayNotificationNumber(0);

//        //sdk开启通知声音
//        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);
//        // sdk关闭通知声音
//		mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
//        // 通知声音由服务端控制
//		mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SERVER);
//		mPushAgent.setNotificationPlayLights(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
//		mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
//
//        //此处是完全自定义处理设置
//        mPushAgent.setPushIntentServiceClass(MyPushIntentService.class);

		//注册推送服务 每次调用register都会回调该接口
		mPushAgent.register(new IUmengRegisterCallback() {
			@Override
			public void onSuccess(String deviceToken) {
				Log.e("deviceToken", deviceToken);
				DEVICETOKEN = deviceToken;
			}

			@Override
			public void onFailure(String s, String s1) {
			}
		});

		/**
		 * 自定义行为的回调处理
		 * UmengNotificationClickHandler是在BroadcastReceiver中被调用，故
		 * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
		 * */
		mPushAgent.setNotificationClickHandler(new UmengNotificationClickHandler() {
			@Override
			public void dealWithCustomAction(Context context, UMessage msg) {
				super.dealWithCustomAction(context, msg);
				if (msg.extra != null) {
					JSONObject obj = new JSONObject(msg.extra);
					try {
						if (!obj.isNull("show_type")) {
							String url = obj.getString("url");
							String showType = obj.getString("show_type");
							if (TextUtils.equals(showType, "pdf")) {
								Intent intent = new Intent(getApplicationContext(), PDFActivity.class);
								intent.putExtra(CONST.WEB_URL, url);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							} else {
								Intent intent = new Intent(getApplicationContext(), WebviewActivity.class);
								intent.putExtra(CONST.WEB_URL, url);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

}
