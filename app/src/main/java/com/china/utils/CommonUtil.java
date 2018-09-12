package com.china.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import com.china.R;
import com.china.common.CONST;
import com.china.common.MyApplication;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class CommonUtil {

	/** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static float dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return dpValue * scale;
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static float px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return pxValue / scale;
    } 
    
    /**
     * 获取listview高度
     * @param listView
     */
    public static int getListViewHeightBasedOnChildren(ListView listView) {
    	int height = 0;
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return height;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0); 
			totalHeight += listItem.getMeasuredHeight();
		}
		height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		return height;
	}
    
	/**
	 * 日期转星期
	 *
	 * @param datetime
	 * @return
	 */
	public static String dateToWeek(String datetime) {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
		String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		Calendar cal = Calendar.getInstance(); // 获得一个日历
		Date datet = null;
		try {
			datet = f.parse(datetime);
			cal.setTime(datet);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
		if (w < 0)
			w = 0;
		return weekDays[w];
	}
	
	/**
	 * 从Assets中读取图片
	 */
	public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
		Bitmap image = null;
		AssetManager am = context.getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	/**
	 * 获取圆角图片
	 * @param bitmap
	 * @param corner
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int corner) {
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(rectF, corner, corner, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

			final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			canvas.drawBitmap(bitmap, src, rect, paint);
			bitmap.recycle();
			return output;
		} catch (Exception e) {
			return bitmap;
		}
	}
	
	/**
	 * 隐藏虚拟键盘
	 * @param editText 输入框
	 * @param context 上下文
	 */
	public static void hideInputSoft(EditText editText, Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	/**
	 * 显示虚拟键盘
	 * @param context 上下文
	 */
	public static void showInputSoft(Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	/**
	 * 转换图片成圆形
	 * 
	 * @param bitmap
	 *            传入Bitmap对象
	 * @return
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}
	
	/**
     * 获取网落图片资源 
     * @param url
     * @return
     */
	public static Bitmap getHttpBitmap(String url) {
		URL myFileURL;
		Bitmap bitmap = null;
		try {
			myFileURL = new URL(url);
			// 获得连接
			HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
			// 设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
			conn.setConnectTimeout(6000);
			// 连接设置获得数据流
			conn.setDoInput(true);
			// 不使用缓存
			conn.setUseCaches(false);
			// 这句可有可无，没有影响
			conn.connect();
			// 得到数据流
			InputStream is = conn.getInputStream();
			// 解析得到图片
			bitmap = BitmapFactory.decodeStream(is);
			// 关闭数据流
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/**
	 * 转换图片成六边形
	 * @return
	 */
	public static Bitmap getHexagonShape(Bitmap bitmap) {
		int targetWidth = bitmap.getWidth();
		int targetHeight = bitmap.getHeight();
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);

		float radius = targetHeight / 2;
		float triangleHeight = (float) (Math.sqrt(3) * radius / 2);
		float centerX = targetWidth / 2;
		float centerY = targetHeight / 2;
		
		Canvas canvas = new Canvas(targetBitmap);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG));
		Path path = new Path();
		path.moveTo(centerX, centerY + radius);
		path.lineTo(centerX - triangleHeight, centerY + radius / 2);
		path.lineTo(centerX - triangleHeight, centerY - radius / 2);
		path.lineTo(centerX, centerY - radius);
		path.lineTo(centerX + triangleHeight, centerY - radius / 2);
		path.lineTo(centerX + triangleHeight, centerY + radius / 2);
		path.moveTo(centerX, centerY + radius);
		canvas.clipPath(path);
		canvas.drawBitmap(bitmap, new Rect(0, 0, targetWidth, targetHeight), new Rect(0, 0, targetWidth, targetHeight), null);
		return targetBitmap;
	}

	/**
	 * 把本地的drawable转换成六边形图片
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}
	
	/**
	 * 读取assets下文件
	 * @param fileName
	 * @return
	 */
	public static String getFromAssets(Context context, String fileName) {
		String Result = "";
		try {
			InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			while ((line = bufReader.readLine()) != null)
				Result += line;
			return Result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Result;
	}
	
	/**  
	 * 截取webView快照(webView加载的整个内容的大小)  
	 * @param webView  
	 * @return  
	 */  
	public static Bitmap captureWebView(WebView webView){
	    Picture snapShot = webView.capturePicture();  
	    Bitmap bitmap = Bitmap.createBitmap(snapShot.getWidth(),snapShot.getHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);  
	    snapShot.draw(canvas);  
	    clearCanvas(canvas);
	    return bitmap;  
	}  
	
	/**
	 * 截取scrollView
	 * @param scrollView
	 * @return
	 */
	public static Bitmap captureScrollView(ScrollView scrollView) {  
        int height = 0;  
        // 获取scrollview实际高度  
        for (int i = 0; i < scrollView.getChildCount(); i++) {  
        	height += scrollView.getChildAt(i).getHeight();  
        	scrollView.getChildAt(i).setBackgroundColor(0xffffff);  
        }  
        // 创建对应大小的bitmap  
        Bitmap bitmap = Bitmap.createBitmap(scrollView.getWidth(), height, Config.ARGB_8888);  
        Canvas canvas = new Canvas(bitmap);  
        scrollView.draw(canvas);  
        clearCanvas(canvas);
        return bitmap;  
    }  
	
	/**
	 * 截取listview
	 * @param listView
	 * @return
	 */
    public static Bitmap captureListView(ListView listView){
        ListAdapter listAdapter = listView.getAdapter();
        int count = listAdapter.getCount();
        if (count > 30) {
        	count = 30;
		}
        List<View> childViews = new ArrayList<>(count);
        int totalHeight = 0;
        for(int i = 0; i < count; i++){
        	View itemView = listAdapter.getView(i, null, listView);
        	itemView.measure(0, 0); 
			childViews.add(itemView);
			totalHeight += itemView.getMeasuredHeight();
        }
        Bitmap bitmap = Bitmap.createBitmap(listView.getWidth(), totalHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int yPos = 0;
        //把每个ItemView生成图片，并画到背景画布上
        for(int j = 0; j < childViews.size(); j++){
            View itemView = childViews.get(j);
            int childHeight = itemView.getMeasuredHeight();
            itemView.layout(0, 0, listView.getWidth(), childHeight);
            itemView.buildDrawingCache();
            Bitmap itemBitmap = itemView.getDrawingCache();
            if(itemBitmap!=null){
                canvas.drawBitmap(itemBitmap, 0, yPos, null);
            }
            yPos = childHeight +yPos;
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        clearCanvas(canvas);
        return bitmap;
    }
    
    /**
     * 截屏自定义view
     * @param view
     * @return
     */
    public static Bitmap captureMyView(View view) {
		if (view == null) {
			return null;
		}
		int width = view.getWidth();
		int height = view.getHeight();
		if (width <= 0 || height <= 0) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		bitmap = view.getDrawingCache();
		clearCanvas(canvas);
		return bitmap;
	}
    
	/**
     * 截屏,可是区域
     * @return
     */
	public static Bitmap captureView(View view) {
		if (view == null) {
			return null;
		}
		int width = view.getWidth();
		int height = view.getHeight();
		if (width <= 0 || height <= 0) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		clearCanvas(canvas);
		return bitmap;
	}
	
	/**
	 * 合成图片
	 * @param bitmap1
	 * @param bitmap2
	 * @param isCover 判断是否为覆盖合成
	 * @return
	 */
	public static Bitmap mergeBitmap(Context context, Bitmap bitmap1, Bitmap bitmap2, boolean isCover) {
    	if (bitmap1 == null || bitmap2 == null) {
			return null;
		}
    	
    	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        bitmap1 = Bitmap.createScaledBitmap(bitmap1, width, width*bitmap1.getHeight()/bitmap1.getWidth(), true);
    	bitmap2 = Bitmap.createScaledBitmap(bitmap2, width, width*bitmap2.getHeight()/bitmap2.getWidth(), true);
    	
    	Bitmap bitmap = null;
        Canvas canvas = null;
        if (isCover) {
        	int height = bitmap1.getHeight();
        	if (bitmap1.getHeight() > bitmap2.getHeight()) {
				height = bitmap1.getHeight();
			}else {
				height = bitmap2.getHeight();
			}
        	bitmap = Bitmap.createBitmap(bitmap1.getWidth(), height, Config.ARGB_8888);
        	canvas = new Canvas(bitmap);
        	canvas.drawBitmap(bitmap1, 0, 0 , null);
        	canvas.drawBitmap(bitmap2, 0, 0, null);
		}else {
			bitmap = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight()+bitmap2.getHeight(), Config.ARGB_8888);
			canvas = new Canvas(bitmap);
        	canvas.drawBitmap(bitmap1, 0, 0 , null);
        	canvas.drawBitmap(bitmap2, 0, bitmap1.getHeight(), null);
		}
        clearCanvas(canvas);
        return bitmap;
    }
    
    public static void clearBitmap(Bitmap bitmap) {
		if (bitmap != null) {
//			if (!bitmap.isRecycled()) {
//				bitmap.recycle();
//			}
			bitmap = null;
			System.gc();
		}
	}
    
    public static void clearCanvas(Canvas canvas) {
    	if (canvas != null) {
			canvas = null;
		}
    }
    
    /**
     * 分享功能
     * @param activity
     */
    public static void share(final Activity activity, final Bitmap bitmap) {
    	ShareAction panelAction = new ShareAction(activity);
		panelAction.setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.QQ,SHARE_MEDIA.DINGTALK,SHARE_MEDIA.EMAIL,SHARE_MEDIA.SMS);
		panelAction.setShareboardclickCallback(new ShareBoardlistener() {
			@Override
			public void onclick(SnsPlatform arg0, SHARE_MEDIA arg1) {
				ShareAction shareAction = new ShareAction(activity);
				shareAction.setPlatform(arg1);
				if (bitmap != null) {
					shareAction.withMedia(new UMImage(activity, bitmap));
				}
				shareAction.share();
			}
		});
        panelAction.open();
    }
    
    /**
     * 分享功能
     * @param activity
     */
    public static void share(final Activity activity, final String title, final String content, final String imgUrl, final String url) {
    	ShareAction panelAction = new ShareAction(activity);
		panelAction.setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.QQ,SHARE_MEDIA.DINGTALK,SHARE_MEDIA.EMAIL,SHARE_MEDIA.SMS);
		panelAction.setShareboardclickCallback(new ShareBoardlistener() {
			@Override
			public void onclick(SnsPlatform arg0, SHARE_MEDIA arg1) {
				ShareAction sAction = new ShareAction(activity);
				sAction.setPlatform(arg1);
	        	UMWeb web = new UMWeb(url);
	            web.setTitle(title);//标题
	            if (!TextUtils.isEmpty(imgUrl)) {
	            	web.setThumb(new UMImage(activity, imgUrl));  //缩略图
				}else {
					web.setThumb(new UMImage(activity, R.drawable.shawn_icon_round_icon));
				}
	            web.setDescription(content);
	            sAction.withMedia(web);
		        sAction.share();
			}
		});
        panelAction.open();
    }
    
    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * @param context
     * @return true 表示开启
     */ 
    public static final boolean isLocationOpen(final Context context) { 
        LocationManager locationManager  = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快） 
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位） 
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); 
        if (gps || network) { 
            return true; 
        } 
        return false; 
    }
    
    /**
	 * 提交点击次数
	 */
	public static void submitClickCount(final String columnId, final String name) {
		if (TextUtils.isEmpty(columnId) || TextUtils.isEmpty(name)) {
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
		String addtime = sdf.format(new Date());
		final String clickUrl = String.format("http://decision-admin.tianqi.cn/Home/Count/clickCount?addtime=%s&appid=%s&eventid=menuClick_%s&eventname=%s&userid=%s&username=%s",
				addtime, CONST.APPID, columnId, name, MyApplication.UID, MyApplication.USERNAME);
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(clickUrl).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
					}
				});
			}
		}).start();
	}

	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String getVersion(Context context) {
	    try {
	        PackageManager manager = context.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
	        return info.versionName;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "";
	    }
	}

	/**
	 * 是否显示引导页
	 * @param context
	 * @param className
	 * @param ivGuide
	 */
	public static void showGuidePage(Context context, String className, ImageView ivGuide) {
		SharedPreferences sp = context.getSharedPreferences(className, Context.MODE_PRIVATE);
		boolean isShowGuid = sp.getBoolean("isShowGuide", true);
		if (isShowGuid) {
			ivGuide.setVisibility(View.VISIBLE);
		}else {
			ivGuide.setVisibility(View.GONE);
		}
	}

	public static void saveGuidePageState(Context context, String className) {
		SharedPreferences sp = context.getSharedPreferences(className, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("isShowGuide", false);
		editor.commit();
	}

	/**
	 * 写入文件内容
	 * @param activity
	 * @param fileName
	 * @param content
	 */
	public static void writeFile(Activity activity, String fileName, String content) {
		try {
            /* 根据用户提供的文件名，以及文件的应用模式，打开一个输出流.文件不存系统会为你创建一个的，
             * 至于为什么这个地方还有FileNotFoundException抛出，我也比较纳闷。在Context中是这样定义的
             *   public abstract FileOutputStream openFileOutput(String name, int mode)
             *   throws FileNotFoundException;
             * openFileOutput(String name, int mode);
             * 第一个参数，代表文件名称，注意这里的文件名称不能包括任何的/或者/这种分隔符，只能是文件名
             *          该文件会被保存在/data/data/应用名称/files/chenzheng_java.txt
             * 第二个参数，代表文件的操作模式
             *          MODE_PRIVATE 私有（只能创建它的应用访问） 重复写入时会文件覆盖
             *          MODE_APPEND  私有   重复写入时会在文件的末尾进行追加，而不是覆盖掉原来的文件
             *          MODE_WORLD_READABLE 公用  可读
             *          MODE_WORLD_WRITEABLE 公用 可读写
             *  */
			FileOutputStream outputStream = activity.openFileOutput(fileName+".txt", Activity.MODE_PRIVATE);
			outputStream.write(content.getBytes());
			outputStream.flush();
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取文件内容
	 * @param activity
	 * @param fileName
	 * @return
	 */
	public static String readFile(Activity activity, String fileName) {
		try {
			FileInputStream inputStream = activity.openFileInput(fileName+".txt");
			byte[] bytes = new byte[1024];
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			while (inputStream.read(bytes) != -1) {
				arrayOutputStream.write(bytes, 0, bytes.length);
			}
			inputStream.close();
			arrayOutputStream.close();
			String result = new String(arrayOutputStream.toByteArray());
			return result;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * SDCard写入文件
	 * @param file
	 * @param content
	 */
	public static void writeExternalFile(File file, String content) {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(content.getBytes());
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * SDCard读取文件
	 * @param file
	 * @return
	 */
	public static String readExternalFile(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			StringBuffer result = new StringBuffer();
			while (br.ready()) {
				result.append((char)br.read());
			}
			br.close();
			return result.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取http://decision.tianqi.cn域名的请求头
	 * @return
	 */
	public static String getRequestHeader() {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd00");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd06");
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd12");
		SimpleDateFormat sdf4 = new SimpleDateFormat("yyyyMMdd18");
		SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMddHH");
		long time1 = 0, time2 = 0, time3 = 0, time4 = 0;
		long currentTime = 0;
		try {
			time1 = sdf5.parse(sdf1.format(new Date())).getTime();
			time2 = sdf5.parse(sdf2.format(new Date())).getTime();
			time3 = sdf5.parse(sdf3.format(new Date())).getTime();
			time4 = sdf5.parse(sdf4.format(new Date())).getTime();
			currentTime = new Date().getTime();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String date = null;
		if (currentTime >= time1 && currentTime < time2) {
			date = sdf1.format(new Date());
		}else if (currentTime >= time2 && currentTime < time3) {
			date = sdf2.format(new Date());
		}else if (currentTime >= time3 && currentTime < time4) {
			date = sdf3.format(new Date());
		}else if (currentTime >= time4) {
			date = sdf4.format(new Date());
		}
		String publicKey = "http://decision.tianqi.cn/?date="+date;//公钥
		String privateKye = "url_private_key_789";//私钥
		String result = "";
		try{
			byte[] rawHmac = null;
			byte[] keyBytes = privateKye.getBytes("UTF-8");
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			rawHmac = mac.doFinal(publicKey.getBytes("UTF-8"));
			result = Base64.encodeToString(rawHmac, Base64.DEFAULT);
//			result = URLEncoder.encode(result, "UTF-8");
			result = "http://decision.tianqi.cn/"+result;
		}catch(Exception e){
			Log.e("SceneException", e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 获取手机唯一标识
	 * @param context
	 * @return
	 */
	public static String getUniqueId(Context context){
		String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		String serialNo = android.os.Build.SERIAL;
		String id = androidId + serialNo;
		try {
			return toMD5(id);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return id;
		}
	}

	/**
	 * 字符串转md5
	 * @param text
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private static String toMD5(String text) throws NoSuchAlgorithmException {
		//获取摘要器 MessageDigest
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		//通过摘要器对字符串的二进制字节数组进行hash计算
		byte[] digest = messageDigest.digest(text.getBytes());

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			//循环每个字符 将计算结果转化为正整数;
			int digestInt = digest[i] & 0xff;
			//将10进制转化为较短的16进制
			String hexString = Integer.toHexString(digestInt);
			//转化结果如果是个位数会省略0,因此判断并补0
			if (hexString.length() < 2) {
				sb.append(0);
			}
			//将循环结果添加到缓冲区
			sb.append(hexString);
		}
		//返回整个结果
		return sb.toString();
	}

    /**
     * 根据当前时间获取日期
     * @param i (+1为后一天，-1为前一天，0表示当天)
     * @return
     */
    public static String getDate(String time, int i) {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm");
        try {
            Date date = sdf2.parse(time);
            c.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        c.add(Calendar.DAY_OF_MONTH, i);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
        String date = sdf1.format(c.getTime());
        return date;
    }

    /**
     * 根据当前时间获取星期几
     * @param i (+1为后一天，-1为前一天，0表示当天)
     * @return
     */
    public static String getWeek(int i) {
        String week = "";

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_WEEK, i);

        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                week = "周日";
                break;
            case Calendar.MONDAY:
                week = "周一";
                break;
            case Calendar.TUESDAY:
                week = "周二";
                break;
            case Calendar.WEDNESDAY:
                week = "周三";
                break;
            case Calendar.THURSDAY:
                week = "周四";
                break;
            case Calendar.FRIDAY:
                week = "周五";
                break;
            case Calendar.SATURDAY:
                week = "周六";
                break;
        }

        return week;
    }

	/**
	 * 根据风速获取风向标
	 * @param context
	 * @param speed
	 * @return
	 */
	public static Bitmap getWindMarker(Context context, double speed) {
		Bitmap bitmap = null;
		if (speed <= 0.2) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
		}else if (speed > 0.2 && speed <= 1.5) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
		}else if (speed > 1.5 && speed <= 3.3) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
		}else if (speed > 3.3 && speed <= 5.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind34);
		}else if (speed > 5.4 && speed <= 7.9) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind34);
		}else if (speed > 7.9 && speed <= 10.7) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind56);
		}else if (speed > 10.7 && speed <= 13.8) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind56);
		}else if (speed > 13.8 && speed <= 17.1) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind78);
		}else if (speed > 17.1 && speed <= 20.7) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind78);
		}else if (speed > 20.7 && speed <= 24.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}else if (speed > 24.4 && speed <= 28.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}else if (speed > 28.4 && speed <= 32.6) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}else if (speed > 32.6 && speed < 99999.0) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}
		return bitmap;
	}

	/**
	 * 根据风速获取风向标
	 * @param context
	 * @param speed
	 * @return
	 */
	public static Bitmap getStrongWindMarker(Context context, double speed) {
		Bitmap bitmap = null;
		if (speed > 17.1 && speed <= 20.7) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fzj_wind_78);
		}else if (speed > 20.7 && speed <= 24.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fzj_wind_89);
		}else if (speed > 24.4 && speed <= 28.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fzj_wind_910);
		}else if (speed > 28.4 && speed <= 32.6) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fzj_wind_1011);
		}else if (speed > 32.6 && speed < 99999.0) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fzj_wind_1112);
		}
		return bitmap;
	}

	/**
	 * 获取状态栏高度
	 * @param context
	 * @return
	 */
	public static int statusBarHeight(Context context) {
		int statusBarHeight = -1;//状态栏高度
		//获取status_bar_height资源的ID
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			//根据资源ID获取响应的尺寸值
			statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
		}
		return statusBarHeight;
	}

	/**
	 * 获取底部导航栏高度
	 * @param context
	 * @return
	 */
	public static int navigationBarHeight(Context context) {
		int navigationBarHeight = -1;//状态栏高度
		//获取status_bar_height资源的ID
		int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
			//根据资源ID获取响应的尺寸值
			navigationBarHeight = context.getResources().getDimensionPixelSize(resourceId);
		}
		return navigationBarHeight;
	}
    
}
