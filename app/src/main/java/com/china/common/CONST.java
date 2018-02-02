package com.china.common;

import android.os.Environment;

import java.io.File;

public class CONST {

	public static String APPID = "19";//贵州客户端对应服务器的appid
	public static String noValue = "--";
	public static int MSG_100 = 100;
	public static int MSG_101 = 101;
	public static String filePath = Environment.getExternalStorageDirectory() + File.separator + "screenshot.png";
	public static final String imageSuffix = ".png";//图标后缀名
	public static String SHOWGUIDE = "show_guide";
	public static String VERSION = "version";
	
	public static final String WAIT_WIND = "http://www.welife100.com/Wap/Fengc/index";//等风地址
	public static final String CLOUD_URL = "http://decision.tianqi.cn/data/page/imgs.html?http://decision.tianqi.cn/data/product/JC_YT_DL_WXZXCSYT.html";//云图地址
	
	//showType类型，区分本地类或者图文
	public static final String LOCAL = "local";
	public static final String NEWS = "news";
	public static final String NEWSPLUS = "news+";
	public static final String PRODUCT = "product";
	public static final String URL = "url";
	public static final String PDF = "pdf";
	
	//intent传值的标示
	public static final String INTENT_APPID = "intent_appid";
	public static final String WEB_URL = "web_Url";//网页地址的标示
	public static final String COLUMN_ID = "column_id";//栏目id
	public static final String ACTIVITY_NAME = "activity_name";//界面名称
	
	//下拉刷新progresBar四种颜色
	public static final int color1 = android.R.color.holo_blue_bright;
	public static final int color2 = android.R.color.holo_blue_light;
	public static final int color3 = android.R.color.holo_blue_bright;
	public static final int color4 = android.R.color.holo_blue_light;
	
	//本地保存用户信息参数
	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		public static final String uId = "uId";
		public static final String userName = "uName";
		public static final String passWord = "pwd";
		public static final String userGroup = "userGroup";
	}
	
	public static String UID = null;//用户id
	public static String USERNAME = null;//用户名
	public static String PASSWORD = null;//用户密码
	public static String USERGROUP = null;//用户组
	
	public static String COUNTURL = null;//周报统计链接
	public static String RECOMMENDURL = null;//应用推荐链接
	
	//预警颜色对应规则
	public static String[] blue = {"01", "_blue"};
	public static String[] yellow = {"02", "_yellow"};
	public static String[] orange = {"03", "_orange"};
	public static String[] red = {"04", "_red"};
	
	//贵州接口
	public static String GUIZHOU_BASE = "http://decision-admin.tianqi.cn/home";
	public static String GUIZHOU_LOGIN = GUIZHOU_BASE+"/Work/login";//登录
	public static String GUIZHOU_FEEDBACK = GUIZHOU_BASE+"/Work/request";//意见反馈
}
