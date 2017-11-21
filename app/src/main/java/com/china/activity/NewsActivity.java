package com.china.activity;

/**
 * 热点新闻
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.china.adapter.NewsAdapter;
import com.china.common.CONST;
import com.china.dto.NewsDto;
import com.china.utils.CommonUtil;
import com.china.utils.CustomHttpClient;
import com.china.view.RefreshLayout;
import com.china.view.RefreshLayout.OnLoadListener;
import com.china.view.RefreshLayout.OnRefreshListener;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends BaseActivity implements OnClickListener, OnRefreshListener, OnLoadListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private NewsAdapter mAdapter = null;
	private List<NewsDto> mList = new ArrayList<NewsDto>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private String dataUrl = null;
	private String showType = null;
	private ProgressBar progressBar = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news);
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
				asyncQuery(dataUrl, false);
			}
		}
	}
	
	private void refresh() {
		dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(dataUrl)) {
			asyncQuery(dataUrl, true);
		}
	}
	
	private void initWidget() {
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		tvTitle.setText(title);

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
					intent = new Intent(mContext, UrlActivity.class);
					intent.putExtra("data", dto);
					intent.putExtra(CONST.WEB_URL, dto.detailUrl);
					startActivity(intent);
				}
			}
		});
	}
	
	private void asyncQuery(String url, boolean isRefresh) {
		HttpAsyncTask task = new HttpAsyncTask(isRefresh);
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		private boolean isRefresh = false;
		
		public HttpAsyncTask(boolean isRefresh) {
			this.isRefresh = isRefresh;
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			if (refreshLayout.isRefreshing()) {
				refreshLayout.setRefreshing(false);
			}
			if (refreshLayout.isLoading()) {
				refreshLayout.setLoading(false);
			}
			if (requestResult != null) {
				try {
					JSONObject obj = new JSONObject(requestResult);
					if (!obj.isNull("l")) {
						if (isRefresh) {
							mList.clear();
						}
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
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
					progressBar.setVisibility(View.GONE);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
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
		if (v.getId() == R.id.llBack) {
			setBackEmit();
			finish();
		}
	}
	
}
