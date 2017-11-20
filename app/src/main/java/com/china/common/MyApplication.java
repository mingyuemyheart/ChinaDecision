package com.china.common;

import android.app.Application;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;

import java.net.URISyntaxException;

public class MyApplication extends Application{

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
	}
	{
		//umeng分享的平台注册
		PlatformConfig.setWeixin("wx1fa67f698f7053ad", "f3fc51dcb4518eb80bff808acb10c409");
		PlatformConfig.setQQZone("1105876438", "tH05WZYOjbInVhQq");
		PlatformConfig.setDing("dingoaqfmkgk4d9lo7gbmq");
		Config.DEBUG = false;
	}

}
