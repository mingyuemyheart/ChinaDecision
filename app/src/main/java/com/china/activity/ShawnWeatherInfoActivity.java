package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnNewsAdapter;
import com.china.common.CONST;
import com.china.dto.NewsDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.tendcloud.tenddata.TCAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 天气资讯
 */
public class ShawnWeatherInfoActivity extends ShawnBaseActivity implements OnClickListener {
	
	private Context mContext;
	private TextView tvTitle;
	private ShawnNewsAdapter mAdapter;
	private List<NewsDto> dataList = new ArrayList<>();
	private SwipeRefreshLayout refreshLayout;//下拉刷新布局
	private String dataUrl,showType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_weather_info);
		mContext = this;
		initRefreshLayout();
		initWidget();
		initListView();
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
	
	private void refresh() {
		dataList.clear();
		dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(dataUrl)) {
			OkHttpList(dataUrl);
		}
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		refresh();
		
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		ListView mListView = findViewById(R.id.listView);
		mAdapter = new ShawnNewsAdapter(mContext, dataList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				NewsDto dto = dataList.get(arg2);
				Intent intent;
				if (TextUtils.equals(showType, CONST.PDF)) {
					intent = new Intent(mContext, ShawnPDFActivity.class);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.title);
					intent.putExtra(CONST.WEB_URL, dto.detailUrl);
					startActivity(intent);
				}else {
					intent = new Intent(mContext, ShawnNewsDetailActivity.class);
					intent.putExtra("data", dto);
					intent.putExtra(CONST.WEB_URL, dto.detailUrl);
					startActivity(intent);
				}
			}
		});
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
					if (!TextUtils.isEmpty(dataUrl)) {
						OkHttpList(dataUrl);
					}
				}
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
	}
	
	private void OkHttpList(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("l")) {
											JSONArray array = new JSONArray(obj.getString("l"));
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												NewsDto dto = new NewsDto();
												dto.title = itemObj.getString("l1");
												dto.detailUrl = itemObj.getString("l2");
												dto.time = itemObj.getString("l3");
												dto.imgUrl = itemObj.getString("l4");
												dataList.add(dto);
											}
										}
										if (!obj.isNull("prev")) {
											dataUrl = obj.getString("prev");
										}
										if (!obj.isNull("type")) {
											showType = obj.getString("type");
										}
										if (mAdapter != null) {
											mAdapter.notifyDataSetChanged();
										}
										if (refreshLayout.isRefreshing()) {
											refreshLayout.setRefreshing(false);
										}

									} catch (JSONException e1) {
										e1.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setBackEmit();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				setBackEmit();
				finish();
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (tvTitle != null) {
			TCAgent.onPageStart(mContext, tvTitle.getText().toString());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (tvTitle != null) {
			TCAgent.onPageEnd(mContext, tvTitle.getText().toString());
		}
	}
	
}
