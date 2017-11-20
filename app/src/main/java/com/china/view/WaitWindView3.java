package com.china.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WaitWindView3 extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder holder;
	private MyThread myThread;

	public WaitWindView3(Context context) {
		super(context);
		holder = this.getHolder();
		holder.addCallback(this);
	}

	public WaitWindView3(Context context, AttributeSet attrs) {
		super(context, attrs);
		holder = this.getHolder();
		holder.addCallback(this);

	}

	public WaitWindView3(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		holder = this.getHolder();
		holder.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (myThread == null) {
			myThread = new MyThread(holder);//创建一个绘图线程
			myThread.isRun = true;
		}
		myThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		myThread.isRun = false;
	}

	//线程内部类
	class MyThread extends Thread
	{
		private SurfaceHolder holder;
		public boolean isRun ;
		public  MyThread(SurfaceHolder holder)
		{
			this.holder =holder;
			isRun = true;
		}
		@Override
		public void run()
		{
			int count = 0;
			while(isRun)
			{
				Canvas c = null;
				try
				{
					synchronized (holder)
					{
						c = holder.lockCanvas();//锁定画布，一般在锁定后就可以通过其返回的画布对象Canvas，在其上面画图等操作了。
						c.drawColor(Color.BLACK);//设置画布背景颜色
						Paint p = new Paint(); //创建画笔
						p.setColor(Color.WHITE);
						Rect r = new Rect(100, 50, 300, 250);
						c.drawRect(r, p);
						c.drawText("这是第"+(count++)+"秒", 100, 310, p);
						Thread.sleep(1000);//睡眠时间为1秒
					}
				}
				catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				finally
				{
					if(c!= null)
					{
						holder.unlockCanvasAndPost(c);//结束锁定画图，并提交改变。

					}
				}
			}
		}
	}

}
