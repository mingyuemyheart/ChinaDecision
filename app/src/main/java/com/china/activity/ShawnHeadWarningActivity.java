package com.china.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.common.MyApplication;
import com.china.dto.WarningDto;
import com.china.fragment.ShawnWarningDetailFragment;
import com.china.utils.CommonUtil;
import com.china.view.MainViewPager;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 左右切换的预警界面
 */
public class ShawnHeadWarningActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private List<WarningDto> warnList = new ArrayList<>();
	private ImageView[] ivTips;//装载点的数组
	private ViewGroup viewGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_head_warning);
		mContext = this;
		initWidget();
		initViewPager();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("预警详情");
		viewGroup = findViewById(R.id.viewGroup);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		warnList.clear();
		warnList.addAll(getIntent().getExtras().<WarningDto>getParcelableArrayList("warningList"));
		
		for (int i = 0; i < warnList.size(); i++) {
			WarningDto dto = warnList.get(i);
			Fragment fragment = new ShawnWarningDetailFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable("data", dto);
			fragment.setArguments(bundle);
			fragments.add(fragment);
		}
		
		ivTips = new ImageView[warnList.size()];
		viewGroup.removeAllViews();
		for (int i = 0; i < warnList.size(); i++) {
			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new LayoutParams(5, 5));  
			ivTips[i] = imageView;  
			if(i == 0){  
				ivTips[i].setBackgroundResource(R.drawable.point_black);
			}else{  
				ivTips[i].setBackgroundResource(R.drawable.point_gray);
			}  
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			viewGroup.addView(imageView, layoutParams);  
		}
		
		if (warnList.size() <= 1) {
			viewGroup.setVisibility(View.GONE);
		}
		
		viewPager = findViewById(R.id.viewPager);
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			@Override
			public void onPageSelected(int position) {
				for (int i = 0; i < warnList.size(); i++) {
					if(i == position){
						ivTips[i].setBackgroundResource(R.drawable.point_black);
					}else{
						ivTips[i].setBackgroundResource(R.drawable.point_gray);
					}
				}
			}
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		viewPager.setAdapter(new MyPagerAdapter());
	}
	
	private class MyPagerAdapter extends PagerAdapter {
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(fragments.get(position).getView());
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = fragments.get(position);
			if (!fragment.isAdded()) { // 如果fragment还没有added
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(fragment, fragment.getClass().getSimpleName());
				ft.commit();
				/**
				 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
				 * 会在进程的主线程中,用异步的方式来执行。
				 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
				 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
				 */
				getFragmentManager().executePendingTransactions();
			}

			if (fragment.getView().getParent() == null) {
				container.addView(fragment.getView()); // 为viewpager增加布局
			}
			return fragment.getView();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setNormalEmit("3", "");
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			setNormalEmit("3", "");
			finish();
			break;
		case R.id.ivShare:
			Bitmap bitmap1 = CommonUtil.captureView(viewPager);
			Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
			Bitmap bitmap = CommonUtil.mergeBitmap(ShawnHeadWarningActivity.this, bitmap1, bitmap2, false);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap2);
			CommonUtil.share(ShawnHeadWarningActivity.this, bitmap);
			break;

		default:
			break;
		}
	};

	private Socket socket = null;

	/**
	 * 普通发送指令，
	 * @param id
	 * @param sid
	 */
	private void setNormalEmit(String id, String sid) {
		try {
			if (socket == null) {
				socket = MyApplication.getSocket();
			}
			if (socket != null && socket.connected()) {
				JSONObject obj = new JSONObject();
				obj.put("computerInfo", MyApplication.computerInfo);
				JSONObject commond = new JSONObject();
				commond.put("id", id);
				JSONObject message = new JSONObject();
				if (!TextUtils.isEmpty(sid)) {
					message.put("sid", sid);
				}
				commond.put("message", message);
				obj.put("commond", commond);
				socket.emit("178", obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
