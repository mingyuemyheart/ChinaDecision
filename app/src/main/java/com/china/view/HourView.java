package com.china.view;

/**
 * 24小时实况温度曲线图
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.china.MainActivity;
import com.china.common.CONST;
import com.china.dto.WeatherDto;
import com.china.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class HourView extends View{
	
	private Context mContext = null;
	private List<WeatherDto> tempList = new ArrayList<WeatherDto>();
	private int maxValue = 0;
	private int minValue = 0;
	private int min = 0;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private float width = 0;
	private MainActivity activity = null;
	
	public HourView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public HourView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public HourView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}
	
	private void init() {
		lineP = new Paint();
		lineP.setStyle(Style.STROKE);
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		
		textP = new Paint();
		textP.setAntiAlias(true);
	}
	
	/**
	 * 对cubicView进行赋值
	 */
	public void setData(List<WeatherDto> dataList, float width, MainActivity activity) {
		this.activity = activity;
		this.width = width;
		if (!dataList.isEmpty()) {
			min  = dataList.get(0).hourlyTemp;
			for (int i = 0; i < dataList.size(); i++) {
				if (min >= dataList.get(i).hourlyTemp) {
					min = dataList.get(i).hourlyTemp;
				}
			}
			for (int i = 0; i < dataList.size(); i++) {
				int hourlyTemp = dataList.get(i).hourlyTemp;
				dataList.get(i).hourlyTemp = (hourlyTemp - min);
			}
			tempList.addAll(dataList);
			
			maxValue = tempList.get(0).hourlyTemp;
			minValue = tempList.get(0).hourlyTemp;
			for (int i = 0; i < tempList.size(); i++) {
				if (maxValue <= tempList.get(i).hourlyTemp) {
					maxValue = tempList.get(i).hourlyTemp;
				}
				if (minValue >= tempList.get(i).hourlyTemp) {
					minValue = tempList.get(i).hourlyTemp;
				}
			}
			
			maxValue = maxValue+1;
			minValue = minValue-3;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (tempList.isEmpty()) {
			return;
		}
		
		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, width/2);
//		float chartW = w-CommonUtil.dip2px(mContext, 80);
		float chartH = h-CommonUtil.dip2px(mContext, 35);
		float leftMargin = CommonUtil.dip2px(mContext, width/4);
		float rightMargin = CommonUtil.dip2px(mContext, width/4);
//		float leftMargin = CommonUtil.dip2px(mContext, 40);
//		float rightMargin = CommonUtil.dip2px(mContext, 40);
		float topMargin = CommonUtil.dip2px(mContext, 35);
		float bottomMargin = CommonUtil.dip2px(mContext, 0);
		float chartMaxH = chartH * maxValue / (Math.abs(maxValue)+Math.abs(minValue));//同时存在正负值时，正值高度
		
		int size = tempList.size();
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			WeatherDto dto = tempList.get(i);
			dto.x = (chartW/(size-1))*i + leftMargin;
			
			float value = tempList.get(i).hourlyTemp;
			if (value >= 0) {
				dto.y = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (minValue >= 0) {
					dto.y = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}else {
				dto.y = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (maxValue < 0) {
					dto.y = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}
			tempList.set(i, dto);
		}
		
		Message msg = new Message();
		msg.what = CONST.MSG_100;
		msg.arg1 = min;
		msg.obj = tempList;
		activity.handler.sendMessage(msg);
		
		//绘制区域
		for (int i = 0; i < size-1; i++) {
			float x1 = tempList.get(i).x;
			float y1 = tempList.get(i).y;
			float x2 = tempList.get(i+1).x;
			float y2 = tempList.get(i+1).y;
			
			float wt = (x1 + x2) / 2;
			
			float x3 = wt;
			float y3 = y1;
			float x4 = wt;
			float y4 = y2;

			Path rectPath = new Path();
			rectPath.moveTo(x1, y1);
			rectPath.cubicTo(x3, y3, x4, y4, x2, y2);
			rectPath.lineTo(x2, h-bottomMargin);
			rectPath.lineTo(x1, h-bottomMargin);
			rectPath.close();
			lineP.setColor(0x30ffffff);
			lineP.setStyle(Style.FILL);
			canvas.drawPath(rectPath, lineP);
		}
		
	}

}
