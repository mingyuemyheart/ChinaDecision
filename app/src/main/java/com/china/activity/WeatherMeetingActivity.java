package com.china.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.common.CONST;
import com.china.dto.WeatherMeetingDto;
import com.china.fragment.WeatherMeetingFragment;
import com.china.utils.CommonUtil;
import com.china.view.MainViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 天气会商
 * @author shawn_sun
 */
public class WeatherMeetingActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = this;
	private LinearLayout llContainer = null;
	private LinearLayout llContainer1 = null;
	private MainViewPager viewPager = null;
	private List<Fragment> fragments = new ArrayList<>();
	private List<WeatherMeetingDto> dataList = new ArrayList<>();

	private String videoUrl1 = "http://10.0.86.110/rest/QxjRestService/getVideoList";
	private String videoUrl2 = "http://106.120.82.240/rest/QxjRestService/getVideoList";
	private String publicIp = "106.120.82.240";//公网ip
	private List<WeatherMeetingDto> videoList = new ArrayList<>();//点播视频列表

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pdf_title);
		mContext = this;
		showDialog();
		initWidget();
	}

	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		llContainer = findViewById(R.id.llContainer);
		llContainer1 = findViewById(R.id.llContainer1);
		HorizontalScrollView hScrollView1 = findViewById(R.id.hScrollView1);
		hScrollView1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					OkHttpVideoList(videoUrl1);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						OkHttpVideoList(videoUrl2);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}).start();

	}

	/**
	 * 初始化viewPager
	 */
	private void initViewPager(List<WeatherMeetingDto> list) {
		int columnSize = list.size();
		if (columnSize <= 1) {
			llContainer.setVisibility(View.GONE);
			llContainer1.setVisibility(View.GONE);
		}
		llContainer.removeAllViews();
		llContainer1.removeAllViews();
		int width = CommonUtil.widthPixels(this);
		for (int i = 0; i < columnSize; i++) {
			WeatherMeetingDto dto = list.get(i);

			TextView tvName = new TextView(mContext);
			tvName.setGravity(Gravity.CENTER);
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			tvName.setPadding(0, (int)(CommonUtil.dip2px(mContext, 5)), 0, (int)(CommonUtil.dip2px(mContext, 5)));
			tvName.setOnClickListener(new MyOnClickListener(i));
			if (i == 0) {
				tvName.setTextColor(0xff0035A2);
			}else {
				tvName.setTextColor(Color.BLACK);
			}
			if (!TextUtils.isEmpty(dto.columnName)) {
				tvName.setText(dto.columnName);
			}
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			if (columnSize <= 4) {
				params.width = width/columnSize;
			} else {
				params.width = width/4;
			}
			tvName.setLayoutParams(params);
			llContainer.addView(tvName, i);

			TextView tvBar = new TextView(mContext);
			tvBar.setGravity(Gravity.CENTER);
			tvBar.setOnClickListener(new MyOnClickListener(i));
			if (i == 0) {
				tvBar.setBackgroundColor(0xff0035A2);
			}else {
				tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
			}
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			if (columnSize <= 4) {
				params1.width = width/columnSize;
			} else {
				params1.width = width/4;
			}
			params1.height = (int) (CommonUtil.dip2px(mContext, 2));
			tvBar.setLayoutParams(params1);
			llContainer1.addView(tvBar, i);

			WeatherMeetingFragment fragment = new WeatherMeetingFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("index", i);
			bundle.putParcelable("data", dto);
			bundle.putParcelableArrayList("videoList", (ArrayList<? extends Parcelable>) videoList);
			fragment.setArguments(bundle);
			fragments.add(fragment);
		}

		viewPager = findViewById(R.id.viewPager);
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setAdapter(new MyPagerAdapter());
	}

	public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			if (llContainer != null) {
				for (int i = 0; i < llContainer.getChildCount(); i++) {
					TextView tvName = (TextView) llContainer.getChildAt(i);
					if (i == arg0) {
						tvName.setTextColor(0xff0035A2);
					}else {
						tvName.setTextColor(Color.BLACK);
					}
				}
			}

			if (llContainer1 != null) {
				for (int i = 0; i < llContainer1.getChildCount(); i++) {
					TextView tvBar = (TextView) llContainer1.getChildAt(i);
					if (i == arg0) {
						tvBar.setBackgroundColor(0xff0035A2);
					}else {
						tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
					}
				}
			}
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/**
	 * 头标点击监听
	 * @author shawn_sun
	 */
	private class MyOnClickListener implements View.OnClickListener {

		private int index;

		private MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index, true);
			}
		}
	}

	/**
	 * @ClassName: MyPagerAdapter
	 * @Description: TODO填充ViewPager的数据适配器
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:37:47
	 *
	 */
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

	private final OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS);
	private final OkHttpClient okHttpClient = builder.build();

	//获取点播视频列表
	private void OkHttpVideoList(String url) throws IOException{
		final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

		JSONObject obj = new JSONObject();
		try {
			obj.put("coId", "10001");
			obj.put("index", "1");
			obj.put("pageOff", "50");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String content = obj.toString();

		RequestBody body = RequestBody.create(JSON, content);
		Request request = new Request.Builder().post(body).url(url).build();
		Response response = okHttpClient.newCall(request).execute();
		if (response.isSuccessful()) {
			String result = response.body().string();
			parseData(result, url);
		} else {
			throw new IOException("Unexpected code " + response);
		}
	}

	private void parseData(final String result, final String url) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (result != null) {
					try {
						JSONObject obj = new JSONObject(result);
						if (!obj.isNull("data")) {
							videoList.clear();
							JSONArray array = obj.getJSONArray("data");

							int length = array.length();
							if (length > 30) {
								length = 30;
							}
							for (int i = 0; i < length; i++) {
								JSONObject itemObj = array.getJSONObject(i);
								WeatherMeetingDto dto = new WeatherMeetingDto();
								if (!itemObj.isNull("videoImgs")) {
									dto.videoImgs = itemObj.getString("videoImgs");
								}
								if (!itemObj.isNull("videoTime")) {
									dto.videoTime = itemObj.getString("videoTime");
								}
								if (!itemObj.isNull("resAddr")) {
									if (url.contains(publicIp)) {
										String addr = itemObj.getString("resAddr");
										if (addr.contains("http://")) {
											addr = addr.substring("http://".length(), addr.length());
											addr = addr.replace(addr.substring(0, addr.indexOf("/")), publicIp);
											dto.hlsAddress = "http://"+addr;
										}
									}else {
										dto.hlsAddress = itemObj.getString("resAddr");
									}
								}
								videoList.add(dto);
							}
						}

						cancelDialog();

						WeatherMeetingDto dto = new WeatherMeetingDto();
						dto.columnName = "本周安排";
						dto.columnUrl = "http://decision-admin.tianqi.cn/Home/extra/getDecisionHsap";
						dataList.add(dto);
						dto = new WeatherMeetingDto();
						dto.columnName = "下周安排";
						dto.columnUrl = "http://decision-admin.tianqi.cn/Home/extra/getDecisionHsap";
						dataList.add(dto);

						initViewPager(dataList);

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}

}
