package com.china.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.china.R;
import com.china.common.CONST;
import com.china.common.MyApplication;
import com.china.dto.WeatherDto;
import com.china.utils.CommonUtil;
import com.china.utils.WeatherUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 逐小时item
 * @author shawn_sun
 *
 */
@SuppressLint("SimpleDateFormat")
public class HourItemView extends View{
	
	private Context mContext = null;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private WeatherDto dto = null;
	private int min = 0;
	private SimpleDateFormat sdf0 = new SimpleDateFormat("yyyyMMddHHmm");
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH");
	
	public HourItemView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public HourItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public HourItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}
	
	private void init() {
		lineP = new Paint();
		lineP.setStyle(Paint.Style.STROKE);
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		
		textP = new Paint();
		textP.setAntiAlias(true);
	}
	
	/**
	 * 对cubicView进行赋值
	 */
	public void setData(WeatherDto dto, int min) {
		this.dto = dto;
		this.min = min;
	}
	
	public Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == CONST.MSG_101) {
				dto = (WeatherDto) msg.obj;
				min = msg.arg1;
				postInvalidate();
			}
		};
	};
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		//绘制曲线上每个点的白点
		lineP.setColor(0x60ffffff);
		lineP.setStyle(Paint.Style.STROKE);
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 13));
		canvas.drawPoint(dto.x, dto.y, lineP);
		
		//绘制曲线上每个点的白点
		lineP.setColor(Color.WHITE);
		lineP.setStyle(Paint.Style.STROKE);
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 8));
		canvas.drawPoint(dto.x, dto.y, lineP);
		
		//绘制曲线上每个点的数据值
		textP.setColor(Color.WHITE);
		textP.setTextSize(CommonUtil.dip2px(mContext, 12));
		float tempWidth = textP.measureText(String.valueOf(dto.hourlyTemp+min)+mContext.getString(R.string.unit_degree));
		canvas.drawText(String.valueOf(dto.hourlyTemp+min)+mContext.getString(R.string.unit_degree), dto.x+(int)(CommonUtil.dip2px(mContext, 5)), dto.y-(int)(CommonUtil.dip2px(mContext, 25)), textP);
		
		try {
			long zao8 = sdf2.parse("08").getTime();
			long wan8 = sdf2.parse("20").getTime();
			long current = sdf2.parse(sdf2.format(sdf0.parse(dto.hourlyTime))).getTime();
			Bitmap lb = null;
			if (current >= zao8 && current < wan8) {
				lb = WeatherUtil.getDayBitmap(mContext, dto.hourlyCode);
			}else {
				lb = WeatherUtil.getNightBitmap(mContext, dto.hourlyCode);
			}
			Bitmap newLbit = ThumbnailUtils.extractThumbnail(lb, (int)(CommonUtil.dip2px(mContext, 20)), (int)(CommonUtil.dip2px(mContext, 20)));
			if (TextUtils.equals("1", MyApplication.getAppTheme())) {
				canvas.drawBitmap(CommonUtil.grayScaleImage(newLbit), dto.x-newLbit.getWidth(), dto.y-(int)(CommonUtil.dip2px(mContext, 40)), textP);
			} else {
				canvas.drawBitmap(newLbit, dto.x-newLbit.getWidth(), dto.y-(int)(CommonUtil.dip2px(mContext, 40)), textP);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//绘制时间
		textP.setColor(Color.WHITE);
		textP.setTextSize(CommonUtil.dip2px(mContext, 12));
		try {
			String time = sdf1.format(sdf0.parse(dto.hourlyTime));
			float timeWidth = textP.measureText(time);
			canvas.drawText(time, dto.x-timeWidth/2, dto.y-(int)(CommonUtil.dip2px(mContext, 10)), textP);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
