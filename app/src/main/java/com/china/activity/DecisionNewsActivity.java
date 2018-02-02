package com.china.activity;

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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.DecisionNewsAdapter;
import com.china.common.CONST;
import com.china.dto.DisasterDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.view.RefreshLayout;
import com.china.view.RefreshLayout.OnRefreshListener;
import com.tendcloud.tenddata.TCAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 决策专报
 * @author shawn_sun
 *
 */
public class DecisionNewsActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private DecisionNewsAdapter mAdapter = null;
	private List<DisasterDto> mList = new ArrayList<>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日 HH时");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy年MM月dd日");
	private ProgressBar progressBar = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decision_news);
		mContext = this;
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
		refreshLayout.setMode(RefreshLayout.Mode.PULL_FROM_START);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
	
	private void refresh() {
		String url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url) && url.contains("newGetDecistionJCKB")) {
			url = url.replace("newGetDecistionJCKB", "newGetDecistionJCKB/new/1");
			OkHttpList(url);
		}
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		refresh();
		
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new DecisionNewsAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				DisasterDto dto = mList.get(arg2);
				Intent intent = new Intent(mContext, PDFActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, dto.title);
				intent.putExtra(CONST.WEB_URL, dto.url);
				startActivity(intent);
			}
		});
	}
	
	/**
	 * 获取列表数据
	 */
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
				final String result = response.body().string();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject obj = new JSONObject(result);
								if (!obj.isNull("info")) {
									mList.clear();
									JSONArray array = obj.getJSONArray("info");
									for (int i = 0; i < array.length(); i++) {
										DisasterDto dto = new DisasterDto();
										JSONObject itemObj = array.getJSONObject(i);
										if (!itemObj.isNull("url")) {
											dto.url = itemObj.getString("url");
										}
										if (!itemObj.isNull("time")) {
											dto.time = itemObj.getString("time");
										}
										if (!itemObj.isNull("title")) {
											dto.title = itemObj.getString("title");
										}
										if (!itemObj.isNull("image")) {
											dto.imgUrl = itemObj.getString("image");
										}
										mList.add(dto);
									}

									if (mAdapter != null) {
										mAdapter.notifyDataSetChanged();
									}
									progressBar.setVisibility(View.GONE);
									refreshLayout.setRefreshing(false);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
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

		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		TCAgent.onPageStart(mContext, tvTitle.getText().toString());
	}

	@Override
	protected void onPause() {
		super.onPause();
		TCAgent.onPageEnd(mContext, tvTitle.getText().toString());
	}

}
