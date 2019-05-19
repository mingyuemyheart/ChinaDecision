package com.china.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.china.R;
import com.china.common.CONST;
import com.china.dto.StationMonitorDto;
import com.china.dto.WarningDto;
import com.china.utils.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 降水柱状图
 * @author shawn_sun
 */
public class FactRainView extends View{
	
	private Context mContext;
	private List<StationMonitorDto> tempList = new ArrayList<>();
	private float maxValue = 0;
	private float minValue = 0;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
	private SimpleDateFormat sdf4 = new SimpleDateFormat("mm", Locale.CHINA);
	private SimpleDateFormat sdf5 = new SimpleDateFormat("HH:mm", Locale.CHINA);
	private Bitmap bitmap = null;
	private List<WarningDto> warningList = new ArrayList<>();
	
	public FactRainView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public FactRainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public FactRainView(Context context, AttributeSet attrs, int defStyleAttr) {
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
		
		bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_marker_rain),
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 25)));
	}
	
	/**
	 * 对cubicView进行赋值
	 */
	public void setData(List<StationMonitorDto> dataList, List<WarningDto> warningList) {
		if (!warningList.isEmpty()) {
			this.warningList.addAll(warningList);
		}
		if (!dataList.isEmpty()) {
			tempList.addAll(dataList);

			String temp = tempList.get(0).precipitation1h;
			for (int i = 0; i < tempList.size(); i++) {
				StationMonitorDto dto = tempList.get(i);
				if (!TextUtils.isEmpty(dto.precipitation1h) && !TextUtils.equals(dto.precipitation1h, CONST.noValue)) {
					temp = dto.precipitation1h;
					break;
				}
			}
			if (!TextUtils.isEmpty(temp) && !TextUtils.equals(temp, CONST.noValue)) {
				maxValue = Float.valueOf(temp);
				minValue = Float.valueOf(temp);
				for (int i = 0; i < tempList.size(); i++) {
					StationMonitorDto dto = tempList.get(i);
					if (!TextUtils.isEmpty(dto.precipitation1h) && !TextUtils.equals(dto.precipitation1h, CONST.noValue)) {
						if (maxValue <= Float.valueOf(dto.precipitation1h)) {
							maxValue = Float.valueOf(dto.precipitation1h);
						}
						if (minValue >= Float.valueOf(dto.precipitation1h)) {
							minValue = Float.valueOf(dto.precipitation1h);
						}
					}
				}
			}
			
			if (maxValue == 0 && minValue == 0) {
				maxValue = 5;
				minValue = 0;
			}else {
				int totalDivider = (int) (Math.ceil(Math.abs(maxValue)));
				int itemDivider = 1;
				if (totalDivider <= 5) {
					itemDivider = 1;
				}else if (totalDivider <= 10) {
					itemDivider = 2;
				}else if (totalDivider <= 20) {
					itemDivider = 5;
				}else if (totalDivider <= 50) {
					itemDivider = 10;
				}else if (totalDivider <= 100) {
					itemDivider = 20;
				}else if (totalDivider <= 200) {
					itemDivider = 50;
				}else {
					itemDivider = 100;
				}
				maxValue = (float) (Math.ceil(maxValue)+itemDivider);
			}
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
		float chartW = w-CommonUtil.dip2px(mContext, 40);
		float chartH = h-CommonUtil.dip2px(mContext, 80);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 10);
		float bottomMargin = CommonUtil.dip2px(mContext, 70);
		float chartMaxH = chartH * maxValue / (Math.abs(maxValue)+Math.abs(minValue));//同时存在正负值时，正值高度
		
		int size = tempList.size();
		float columnWidth = chartW/size;
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			StationMonitorDto dto = tempList.get(i);
			dto.x = columnWidth*i + columnWidth/2 + leftMargin;
			dto.y = 0;
			
			if (!TextUtils.isEmpty(dto.precipitation1h) && !TextUtils.equals(dto.precipitation1h, CONST.noValue)) {
				float value = Float.valueOf(dto.precipitation1h);
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
		}
		
		for (int i = 0; i < size-1; i++) {
			StationMonitorDto dto = tempList.get(i);
			//绘制区域
			Path rectPath = new Path();
			rectPath.moveTo(dto.x-columnWidth/2, topMargin);
			rectPath.lineTo(dto.x+columnWidth/2, topMargin);
			rectPath.lineTo(dto.x+columnWidth/2, h-bottomMargin);
			rectPath.lineTo(dto.x-columnWidth/2, h-bottomMargin);
			rectPath.close();
			if (i == 0 || i == 1 || i == 2 || i == 3 || i == 8 || i == 9 || i == 10 || i == 11
					 || i == 16 || i == 17 || i == 18 || i == 19) {
				lineP.setColor(Color.WHITE);
			}else {
				lineP.setColor(0xfff9f9f9);
			}
			lineP.setStyle(Style.FILL);
			canvas.drawPath(rectPath, lineP);
		}
		
		for (int i = 0; i < size; i++) {
			StationMonitorDto dto = tempList.get(i);
			//绘制分割线
			Path linePath = new Path();
			linePath.moveTo(dto.x-columnWidth/2, topMargin);
			linePath.lineTo(dto.x-columnWidth/2, h-bottomMargin);
			linePath.close();
			lineP.setColor(0xfff1f1f1);
			lineP.setStyle(Style.STROKE);
			canvas.drawPath(linePath, lineP);
		}
		
		int totalDivider = (int) (Math.ceil(Math.abs(maxValue)));
		int itemDivider = 1;
		if (totalDivider <= 5) {
			itemDivider = 1;
		}else if (totalDivider <= 10) {
			itemDivider = 2;
		}else if (totalDivider <= 20) {
			itemDivider = 5;
		}else if (totalDivider <= 50) {
			itemDivider = 10;
		}else if (totalDivider <= 100) {
			itemDivider = 20;
		}else if (totalDivider <= 200) {
			itemDivider = 50;
		}else {
			itemDivider = 100;
		}
		for (int i = (int) minValue; i <= totalDivider; i+=itemDivider) {
			float dividerY = 0;
			int value = i;
			if (value >= 0) {
				dividerY = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (minValue >= 0) {
					dividerY = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}else {
				dividerY = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (maxValue < 0) {
					dividerY = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}
			lineP.setColor(0xfff1f1f1);
			canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
			textP.setColor(0xff999999);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			canvas.drawText(String.valueOf(i), CommonUtil.dip2px(mContext, 5), dividerY, textP);
		}
		
		for (int i = 0; i < size; i++) {
			StationMonitorDto dto = tempList.get(i);
			if (!TextUtils.isEmpty(dto.precipitation1h) && !TextUtils.equals(dto.precipitation1h, CONST.noValue)) {
				//绘制柱形图
				lineP.setColor(0xff0096d3);
				lineP.setStyle(Style.FILL);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
				canvas.drawRect(dto.x-columnWidth/4, dto.y, dto.x+columnWidth/4, h-bottomMargin, lineP);
				
				//绘制曲线上每个点的数据值
				if (Float.valueOf(dto.precipitation1h) != 0) {
					textP.setColor(0xff0096d3);
					textP.setTextSize(CommonUtil.dip2px(mContext, 10));
					float tempWidth = textP.measureText(dto.precipitation1h);
					canvas.drawBitmap(bitmap, dto.x-bitmap.getWidth()/2, dto.y-bitmap.getHeight()-CommonUtil.dip2px(mContext, 2.5f), textP);
					canvas.drawText(dto.precipitation1h, dto.x-tempWidth/2, dto.y-bitmap.getHeight()/2, textP);
				}
			}
			
			//绘制24小时
			textP.setColor(0xff999999);
			textP.setTextSize(CommonUtil.dip2px(mContext, 12));
			try {
				if (!TextUtils.isEmpty(dto.time)) {
					for (int j = 0; j < warningList.size(); j++) {
						WarningDto wDto = warningList.get(j);
						if (TextUtils.equals(sdf2.format(sdf1.parse(dto.time)), sdf2.format(sdf3.parse(wDto.time)))) {
							Bitmap wBitmap = null;
							if (wDto.color.equals(CONST.blue[0])) {
								wBitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+wDto.type+CONST.blue[1]+CONST.imageSuffix);
								if (wBitmap == null) {
									wBitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
								}
							}else if (wDto.color.equals(CONST.yellow[0])) {
								wBitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+wDto.type+CONST.yellow[1]+CONST.imageSuffix);
								if (wBitmap == null) {
									wBitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
								}
							}else if (wDto.color.equals(CONST.orange[0])) {
								wBitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+wDto.type+CONST.orange[1]+CONST.imageSuffix);
								if (wBitmap == null) {
									wBitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
								}
							}else if (wDto.color.equals(CONST.red[0])) {
								wBitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+wDto.type+CONST.red[1]+CONST.imageSuffix);
								if (wBitmap == null) {
									wBitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
								}
							}
							Bitmap b = zoomImage(wBitmap, CommonUtil.dip2px(mContext, 30f), CommonUtil.dip2px(mContext, 25f));
							int minute = Integer.valueOf(sdf4.format(sdf3.parse(wDto.time)));
							float warningX = minute*columnWidth/60;
							canvas.drawBitmap(b, dto.x+warningX-b.getWidth()/2, h-CommonUtil.dip2px(mContext, 40f), textP);
							float tempWidth = textP.measureText(dto.ballTemp);
							textP.setColor(0xff999999);
							textP.setTextSize(CommonUtil.dip2px(mContext, 10));
							canvas.drawText(sdf5.format(sdf3.parse(wDto.time)), dto.x+warningX-tempWidth/2, h-CommonUtil.dip2px(mContext, 5f), textP);
							textP.setColor(0xffdedede);
							canvas.drawLine(dto.x+warningX, h-CommonUtil.dip2px(mContext, 70f), dto.x+warningX, h-CommonUtil.dip2px(mContext, 40f), textP);
						}
					}
					
					String time = sdf2.format(sdf1.parse(dto.time));
					if (!TextUtils.isEmpty(time)) {
						float text = textP.measureText(time+mContext.getString(R.string.hour));
						textP.setColor(0xff999999);
						textP.setTextSize(CommonUtil.dip2px(mContext, 10));
						canvas.drawText(time+mContext.getString(R.string.hour), dto.x-text/2, h-CommonUtil.dip2px(mContext, 55f), textP);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		lineP.reset();
		textP.reset();
		
	}
	
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(bgimage, 0, 0, (int) width, (int) height, matrix, true);
	}
	
}
