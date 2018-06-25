package com.china.view;

/**
 * 实况监测游标
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;

import com.china.R;
import com.china.utils.CommonUtil;

public class StationCursorView extends View{

	private Context mContext;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;
	private Bitmap bitmapBound = null;//上下边界图标
	private Bitmap bitmapDegree = null;//刻度
	private Bitmap bitmapCursor = null;//游标
	private float minZoom = 3.0f, maxZoom = 19.0f;//搞得地图缩放范围
	private float rectZoom = 6.0f;//面，对应zoom
	private float lineZoom = 8.0f;//线，对应zoom
	private float pointZoom = 10.0f;//点，对应zoom
	private float disZoom = 12.0f;//区域，对应zoom
	private float zoom = 0;//地图缩放值

	public StationCursorView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public StationCursorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public StationCursorView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}
	
	private void init() {
		lineP = new Paint();
		lineP.setStyle(Style.STROKE);
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		lineP.setColor(0xff959fa5);
		lineP.setStrokeWidth(5);

		textP = new Paint();
		textP.setColor(Color.BLACK);
		textP.setTextSize(getResources().getDimension(R.dimen.level_6));
		textP.setTypeface(Typeface.DEFAULT_BOLD);

		bitmapBound = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.skjc_pic_guo),
				(int)(CommonUtil.dip2px(mContext, 10)), (int)(CommonUtil.dip2px(mContext, 5)));
		bitmapDegree = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.skjc_pic_dian),
				(int)(CommonUtil.dip2px(mContext, 5)), (int)(CommonUtil.dip2px(mContext, 5)));
		bitmapCursor = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.skjc_pic_zx),
				(int)(CommonUtil.dip2px(mContext, 15)), (int)(CommonUtil.dip2px(mContext, 5)));
	}

	/**
	 * 改变游标位置
	 * @param zoom
	 */
	public void refreshCursor(float zoom) {
		this.zoom = zoom;
		postInvalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();

		float itemHeight = h/(maxZoom-minZoom);//每一个刻度对应的高度
		canvas.drawLine(bitmapBound.getWidth()/2, 0, bitmapBound.getWidth()/2, h, lineP);//绘制纵轴线
		canvas.drawBitmap(bitmapBound, 0, 0, lineP);//绘制上边界
		canvas.drawBitmap(bitmapDegree, bitmapBound.getWidth()/2-bitmapDegree.getWidth()/2, itemHeight*(rectZoom-minZoom), lineP);//绘制面刻度
		canvas.drawBitmap(bitmapDegree, bitmapBound.getWidth()/2-bitmapDegree.getWidth()/2, itemHeight*(lineZoom-minZoom), lineP);//绘制线刻度
		canvas.drawBitmap(bitmapDegree, bitmapBound.getWidth()/2-bitmapDegree.getWidth()/2, itemHeight*(pointZoom-minZoom), lineP);//绘制点刻度
		canvas.drawBitmap(bitmapDegree, bitmapBound.getWidth()/2-bitmapDegree.getWidth()/2, itemHeight*(disZoom-minZoom), lineP);//绘制区域刻度
		canvas.drawBitmap(bitmapBound, 0, h-CommonUtil.dip2px(mContext, 5), lineP);//绘制上边界

		canvas.drawText("国", bitmapBound.getWidth()+CommonUtil.dip2px(mContext, 5), textP.getTextSize(), textP);
		canvas.drawText("面", bitmapBound.getWidth()+CommonUtil.dip2px(mContext, 5), itemHeight*(rectZoom-minZoom)+textP.getTextSize(), textP);
		canvas.drawText("线", bitmapBound.getWidth()+CommonUtil.dip2px(mContext, 5), itemHeight*(lineZoom-minZoom)+textP.getTextSize(), textP);
		canvas.drawText("点", bitmapBound.getWidth()+CommonUtil.dip2px(mContext, 5), itemHeight*(pointZoom-minZoom)+textP.getTextSize(), textP);
		canvas.drawText("区", bitmapBound.getWidth()+CommonUtil.dip2px(mContext, 5), itemHeight*(disZoom-minZoom)+textP.getTextSize(), textP);
		canvas.drawText("域", bitmapBound.getWidth()+CommonUtil.dip2px(mContext, 5), itemHeight*(disZoom-minZoom)+textP.getTextSize()*2, textP);
		canvas.drawText("站", bitmapBound.getWidth()+CommonUtil.dip2px(mContext, 5), itemHeight*(disZoom-minZoom)+textP.getTextSize()*3, textP);
		canvas.drawText("街", bitmapBound.getWidth()+CommonUtil.dip2px(mContext, 5), h-textP.getTextSize()-5, textP);
		canvas.drawText("道", bitmapBound.getWidth()+CommonUtil.dip2px(mContext, 5), h-5, textP);

		canvas.drawBitmap(bitmapCursor, bitmapBound.getWidth()/2-lineP.getStrokeWidth(), itemHeight*(zoom-minZoom), lineP);//绘制默认游标位置

	}
	
}
