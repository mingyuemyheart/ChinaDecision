package com.china.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.china.R;
import com.china.dto.WeatherDto;
import com.china.utils.CommonUtil;
import com.china.utils.WeatherUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 一周预报曲线图
 */
public class WeeklyView extends View{
	
	private Context mContext;
	private List<WeatherDto> tempList = new ArrayList<>();
	private int maxTemp,minTemp;//最高温度
	private Paint lineP,textP;//画线画笔
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.CANADA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd", Locale.CANADA);
	private int totalDivider = 0, itemDivider = 1;
	private long foreDate = 0, currentDate = 0;

	public WeeklyView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public WeeklyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public WeeklyView(Context context, AttributeSet attrs, int defStyleAttr) {
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
	 * 进行赋值
	 */
	public void setData(List<WeatherDto> dataList, long foreDate, long currentDate) {
		this.foreDate = foreDate;
		this.currentDate = currentDate;
		if (!dataList.isEmpty()) {
			tempList.addAll(dataList);
			
			maxTemp = tempList.get(0).highTemp;
			minTemp = tempList.get(0).lowTemp;
			for (int i = 0; i < tempList.size(); i++) {
				if (maxTemp <= tempList.get(i).highTemp) {
					maxTemp = tempList.get(i).highTemp;
				}
				if (minTemp >= tempList.get(i).lowTemp) {
					minTemp = tempList.get(i).lowTemp;
				}
			}

			if (maxTemp > 0 && minTemp > 0) {
				totalDivider = maxTemp-minTemp;
			}else if (maxTemp >= 0 && minTemp <= 0) {
				totalDivider = maxTemp-minTemp;
			}else if (maxTemp < 0 && minTemp < 0) {
				totalDivider = maxTemp-minTemp;
			}
			if (totalDivider <= 5) {
				itemDivider = 1;
			}else if (totalDivider > 5 && totalDivider <= 15) {
				itemDivider = 2;
			}else if (totalDivider > 15 && totalDivider <= 25) {
				itemDivider = 3;
			}else if (totalDivider > 25 && totalDivider <= 40) {
				itemDivider = 4;
			}else {
				itemDivider = 5;
			}
//			maxTemp = maxTemp+itemDivider*3/2;
//			minTemp = minTemp-itemDivider;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, 40);
		float chartH = h-CommonUtil.dip2px(mContext, 280);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 160);

		int size = tempList.size();
		
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			WeatherDto dto = tempList.get(i);

			//获取最高温度对应的坐标点信息
			dto.highX = (chartW/(size-1))*i + leftMargin;
			float highTemp = tempList.get(i).highTemp;
			dto.highY = chartH*Math.abs(maxTemp-highTemp)/totalDivider+topMargin;
			Log.e("highTemp", highTemp+"---"+dto.highY);

			//获取最低温度的对应的坐标点信息
			dto.lowX = (chartW/(size-1))*i + leftMargin;
			float lowTemp = tempList.get(i).lowTemp;
			dto.lowY = chartH*Math.abs(maxTemp-lowTemp)/totalDivider+topMargin;
			Log.e("lowTemp", lowTemp+"---"+dto.lowY);
			
			tempList.set(i, dto);
		}

		lineP.setColor(mContext.getResources().getColor(R.color.light_gray));
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.5f));
		canvas.drawLine(0, CommonUtil.dip2px(mContext, 45), w, CommonUtil.dip2px(mContext, 45), lineP);

		for (int i = 0; i < tempList.size(); i++) {
			WeatherDto dto = tempList.get(i);
			//绘制纵向分隔线
			if (i < tempList.size()-1) {
				lineP.setColor(mContext.getResources().getColor(R.color.light_gray));
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.5f));
				canvas.drawLine((tempList.get(i+1).highX-dto.highX)/2+dto.highX, 0, (tempList.get(i+1).highX-dto.highX)/2+dto.highX, h-CommonUtil.dip2px(mContext, 5), lineP);
			}
		}

		//绘制最低温度、最高温度曲线
		for (int i = 0; i < size-1; i++) {
			float x1 = tempList.get(i).lowX;
			float y1 = tempList.get(i).lowY;
			float x2 = tempList.get(i+1).lowX;
			float y2 = tempList.get(i+1).lowY;

			float wt = (x1 + x2) / 2;

			float x3 = wt;
			float y3 = y1;
			float x4 = wt;
			float y4 = y2;

			Path pathLow = new Path();
			pathLow.moveTo(x1, y1);
			pathLow.cubicTo(x3, y3, x4, y4, x2, y2);
			lineP.setColor(0xff0059ab);
			lineP.setStrokeWidth(5.0f);
			canvas.drawPath(pathLow, lineP);
		}

		for (int i = 0; i < size-1; i++) {
			float x1 = tempList.get(i).highX;
			float y1 = tempList.get(i).highY;
			float x2 = tempList.get(i+1).highX;
			float y2 = tempList.get(i+1).highY;

			float wt = (x1 + x2) / 2;

			float x3 = wt;
			float y3 = y1;
			float x4 = wt;
			float y4 = y2;

			Path pathHigh = new Path();
			pathHigh.moveTo(x1, y1);
			pathHigh.cubicTo(x3, y3, x4, y4, x2, y2);
			lineP.setColor(0xffff6d00);
			lineP.setStrokeWidth(5.0f);
			canvas.drawPath(pathHigh, lineP);
		}

		for (int i = 0; i < tempList.size(); i++) {
			WeatherDto dto = tempList.get(i);
			
			//绘制周几、日期、天气现象和天气现象图标
			textP.setColor(getResources().getColor(R.color.text_color4));
			textP.setTextSize(getResources().getDimension(R.dimen.level_5));
			String week = dto.week;
			if (currentDate > foreDate) {
				if (i == 0) {
					week = "昨天";
				}else if (i == 1) {
					week = "今天";
				}else if (i == 2) {
					week = "明天";
				}else {
					week = dto.week;
				}
			}else {
				if (i == 0) {
					week = "今天";
				}else if (i == 1) {
					week = "明天";
				}else {
					week = dto.week;
				}
			}
			float weekText = textP.measureText(week);
			canvas.drawText(week, dto.highX-weekText/2, CommonUtil.dip2px(mContext, 20), textP);
			
			try {
				String date = sdf2.format(sdf1.parse(dto.date));
				float dateText = textP.measureText(date);
				canvas.drawText(date, dto.highX-dateText/2, CommonUtil.dip2px(mContext, 35), textP);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			//绘制高温及天气现象
			float highPheText = textP.measureText(dto.highPhe);//天气现象字符串占像素宽度
			canvas.drawText(dto.highPhe, dto.highX-highPheText/2, CommonUtil.dip2px(mContext, 60), textP);
			Bitmap b = WeatherUtil.getDayBitmap(mContext, dto.highPheCode);
			Bitmap newBit = ThumbnailUtils.extractThumbnail(b, (int)(CommonUtil.dip2px(mContext, 18)), (int)(CommonUtil.dip2px(mContext, 18)));
			canvas.drawBitmap(newBit, dto.highX-newBit.getWidth()/2, CommonUtil.dip2px(mContext, 65), textP);
			String highWindDir = mContext.getString(WeatherUtil.getWindDirection(dto.highWindDir));
			String highWindForce = WeatherUtil.getDayWindForce(dto.highWindForce);
			float highWindDirWidth = textP.measureText(highWindDir);
			float highWindForceWidth = textP.measureText(highWindForce);
			canvas.drawText(highWindDir, dto.highX-highWindDirWidth/2, CommonUtil.dip2px(mContext, 100), textP);
			canvas.drawText(highWindForce, dto.highX-highWindForceWidth/2, CommonUtil.dip2px(mContext, 115), textP);

			//绘制低温及天气现象
			Bitmap lb = WeatherUtil.getNightBitmap(mContext, dto.lowPheCode);
			Bitmap newLbit = ThumbnailUtils.extractThumbnail(lb, (int)(CommonUtil.dip2px(mContext, 18)), (int)(CommonUtil.dip2px(mContext, 18)));
			canvas.drawBitmap(newLbit, dto.lowX-newLbit.getWidth()/2, h-CommonUtil.dip2px(mContext, 80), textP);
			float lowPheText = textP.measureText(dto.lowPhe);//天气现象字符串占像素宽度
			canvas.drawText(dto.lowPhe, dto.lowX-lowPheText/2, h-CommonUtil.dip2px(mContext, 45), textP);
			String lowWindDir = mContext.getString(WeatherUtil.getWindDirection(dto.lowWindDir));
			String lowWindForce = WeatherUtil.getDayWindForce(dto.lowWindForce);
			float lowWindDirWidth = textP.measureText(lowWindDir);
			float lowWindForceWidth = textP.measureText(lowWindForce);
			canvas.drawText(lowWindDir, dto.lowX-lowWindDirWidth/2, h-CommonUtil.dip2px(mContext, 25), textP);
			canvas.drawText(lowWindForce, dto.lowX-lowWindForceWidth/2, h-CommonUtil.dip2px(mContext, 10), textP);

			//绘制曲线上每个时间点上的圆点marker
			if (i != 0) {
				lineP.setColor(0xff0059ab);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 7));
				canvas.drawPoint(dto.lowX, dto.lowY, lineP);
				lineP.setColor(Color.WHITE);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 4));
				canvas.drawPoint(dto.lowX, dto.lowY, lineP);
				lineP.setColor(0xffff6d00);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 7));
				canvas.drawPoint(dto.highX, dto.highY, lineP);
				lineP.setColor(Color.WHITE);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 4));
				canvas.drawPoint(dto.highX, dto.highY, lineP);
			}else {
				lineP.setColor(0xff0059ab);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 7));
				canvas.drawPoint(dto.lowX, dto.lowY, lineP);
				lineP.setColor(0xffff6d00);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 7));
				canvas.drawPoint(dto.highX, dto.highY, lineP);
			}

			//绘制曲线上每个时间点的温度值
			textP.setColor(getResources().getColor(R.color.text_color4));
			textP.setTextSize(CommonUtil.dip2px(mContext, 12));
			float highText = textP.measureText(String.valueOf(tempList.get(i).highTemp)+mContext.getString(R.string.unit_degree));//高温字符串占像素宽度
			canvas.drawText(String.valueOf(tempList.get(i).highTemp)+mContext.getString(R.string.unit_degree), dto.highX-highText/2, dto.highY-CommonUtil.dip2px(mContext, 10), textP);
			
			textP.setColor(getResources().getColor(R.color.text_color4));
			textP.setTextSize(CommonUtil.dip2px(mContext, 12));
			float lowText = textP.measureText(String.valueOf(tempList.get(i).lowTemp)+mContext.getString(R.string.unit_degree));//低温字符串所占的像素宽度
			canvas.drawText(String.valueOf(tempList.get(i).lowTemp)+mContext.getString(R.string.unit_degree), dto.lowX-lowText/2, dto.lowY+CommonUtil.dip2px(mContext, 20), textP);
		}

	}
	
}
