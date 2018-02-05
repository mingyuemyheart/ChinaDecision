package com.china.activity;

/**
 * 天气资讯
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.NewsAdapter;
import com.china.common.CONST;
import com.china.dto.NewsDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.view.RefreshLayout;
import com.china.view.RefreshLayout.OnLoadListener;
import com.china.view.RefreshLayout.OnRefreshListener;
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

public class WeatherInfoActivity extends BaseActivity implements OnClickListener, OnRefreshListener, OnLoadListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private NewsAdapter mAdapter = null;
	private List<NewsDto> mList = new ArrayList<>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private String dataUrl = null;
	private String showType = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_info_activity);
		mContext = this;
		showDialog();
		initRefreshLayout();
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.BOTH);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setOnRefreshListener(this);
		refreshLayout.setOnLoadListener(this);
	}
	
	@Override
	public void onRefresh() {
		if (refreshLayout.isLoading()) {
			refreshLayout.setRefreshing(false);
			return;
		}else {
			mList.clear();
			refresh();
		}
	}
	
	@Override
	public void onLoad() {
		if (refreshLayout.isRefreshing()) {
			refreshLayout.setLoading(false);
			return;
		}else {
			if (!TextUtils.isEmpty(dataUrl)) {
				OkHttpList(dataUrl);
			}
		}
	}
	
	private void refresh() {
		dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(dataUrl)) {
			OkHttpList(dataUrl);
		}
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);

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
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new NewsAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				NewsDto dto = mList.get(arg2);
				Intent intent;
				if (TextUtils.equals(showType, CONST.PDF)) {
					intent = new Intent(mContext, PDFActivity.class);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.title);
					intent.putExtra(CONST.WEB_URL, dto.detailUrl);
					startActivity(intent);
				}else {
					intent = new Intent(mContext, WeatherInfoDetailActivity.class);
					intent.putExtra("data", dto);
					intent.putExtra(CONST.WEB_URL, dto.detailUrl);
					startActivity(intent);
				}
			}
		});
	}
	
	private void OkHttpList(String url) {
		OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
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
								mList.add(dto);
							}
						}
						if (!obj.isNull("prev")) {
							dataUrl = obj.getString("prev");
						}
						if (!obj.isNull("type")) {
							showType = obj.getString("type");
						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (mAdapter != null) {
									mAdapter.notifyDataSetChanged();
								}
								cancelDialog();
								if (refreshLayout.isRefreshing()) {
									refreshLayout.setRefreshing(false);
								}
								if (refreshLayout.isLoading()) {
									refreshLayout.setLoading(false);
								}
							}
						});
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
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
