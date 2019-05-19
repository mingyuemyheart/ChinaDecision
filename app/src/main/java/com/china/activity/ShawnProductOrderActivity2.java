package com.china.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.fragment.ShawnProductOrderLeftFragment;
import com.china.fragment.ShawnProductOrderRightFragment;
import com.china.view.MainViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 产品定制
 */
public class ShawnProductOrderActivity2 extends ShawnBaseActivity implements OnClickListener {
	
	private TextView tv1,tv2;
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private ShawnProductOrderLeftFragment fragment1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_product_order2);
		initWidget();
		initViewPager();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvControl = findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		tvControl.setText("定制");
		tvControl.setVisibility(View.VISIBLE);
		tv1 = findViewById(R.id.tv1);
		tv1.setOnClickListener(new MyOnClickListener(0));
		tv2 = findViewById(R.id.tv2);
		tv2.setOnClickListener(new MyOnClickListener(1));
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		fragment1 = new ShawnProductOrderLeftFragment();
		fragments.add(fragment1);
		ShawnProductOrderRightFragment fragment2 = new ShawnProductOrderRightFragment();
		fragments.add(fragment2);
		
		viewPager = findViewById(R.id.viewPager);
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			@Override
			public void onPageSelected(int position) {
				if (position == 0) {
					tv1.setTextColor(Color.WHITE);
					tv1.setBackgroundResource(R.drawable.corner_left_blue);
					tv2.setTextColor(getResources().getColor(R.color.blue));
					tv2.setBackgroundResource(R.drawable.corner_right_white);
				}else if (position == 1) {
					tv1.setTextColor(getResources().getColor(R.color.blue));
					tv1.setBackgroundResource(R.drawable.corner_left_white);
					tv2.setTextColor(Color.WHITE);
					tv2.setBackgroundResource(R.drawable.corner_right_blue);
				}
			}
			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		viewPager.setAdapter(new MyPagerAdapter());
	}

	/**
	 * @ClassName: MyOnClickListener
	 * @Description: TODO头标点击监听
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:46:08
	 *
	 */
	private class MyOnClickListener implements OnClickListener {

		private int index = 0;

		private MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index);
			}
		}

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
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.tvControl:
				startActivityForResult(new Intent(this, ShawnProductOrderSubmitActivity2.class), 1001);
				break;

			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 1001:
					if (fragment1 != null) {
						fragment1.OkHttpList();
					}
					break;
			}
		}
	}

}
