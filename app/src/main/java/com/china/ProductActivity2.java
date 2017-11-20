package com.china;

/**
 * 农业气象
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.china.adapter.ProductAdapter;
import com.china.common.CONST;
import com.china.common.ColumnData;
import com.china.dto.NewsDto;
import com.china.utils.CommonUtil;
import com.china.utils.CustomHttpClient;
import com.china.view.RefreshLayout;
import com.china.view.RefreshLayout.OnRefreshListener;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity2 extends BaseActivity implements OnClickListener, OnRefreshListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private GridView gridView = null;
	private ProductAdapter mAdapter = null;
	private List<ColumnData> mList = new ArrayList<ColumnData>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private String dataUrl = null;
	private ProgressBar progressBar = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.product2);
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
		refreshLayout.setOnRefreshListener(this);
	}

	@Override
	public void onRefresh() {
		mList.clear();
		dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(dataUrl)) {
			asyncQuery(dataUrl);
		}else {
			ColumnData data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				tvTitle.setText(data.name);
				mList.clear();
				mList.addAll(data.child);
			}
		}
	}

	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		tvTitle.setText(title);

		dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(dataUrl)) {
			asyncQuery(dataUrl);
		}else {
			ColumnData data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				tvTitle.setText(data.name);
				mList.clear();
				mList.addAll(data.child);
			}
		}
		
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		gridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new ProductAdapter(mContext, mList);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = mList.get(arg2);
				Intent intent;
				if (TextUtils.equals(dto.showType, CONST.NEWS)) {
					intent = new Intent(mContext, NewsActivity.class);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else {
					if (!TextUtils.isEmpty(dto.dataUrl)) {
						if (dto.dataUrl.contains(".pdf") || dto.dataUrl.contains(".PDF")) {//pdf格式
							intent = new Intent(mContext, PDFActivity.class);
							intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
							intent.putExtra(CONST.WEB_URL, dto.dataUrl);
							startActivity(intent);
						}else {//网页、图片
							intent = new Intent(mContext, UrlActivity.class);
							NewsDto data = new NewsDto();
							data.title = dto.name;
							data.detailUrl = dto.dataUrl;
							data.imgUrl = dto.icon;
							intent.putExtra("data", data);

							intent.putExtra(CONST.COLUMN_ID, dto.columnId);
							intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
							intent.putExtra(CONST.WEB_URL, dto.dataUrl);
							startActivity(intent);
						}
					}
				}

			}
		});
	}
	
	private void asyncQuery(String url) {
		HttpAsyncTask task = new HttpAsyncTask();
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
		
		public HttpAsyncTask() {
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
			progressBar.setVisibility(View.GONE);
			refreshLayout.setRefreshing(false);
			if (requestResult != null) {
				try {
					JSONObject obj = new JSONObject(requestResult);
					if (!obj.isNull("l")) {
						JSONArray array = new JSONArray(obj.getString("l"));
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);
							ColumnData dto = new ColumnData();
							dto.name = itemObj.getString("l1");
							dto.dataUrl = itemObj.getString("l2");
							dto.icon = itemObj.getString("l4");
							mList.add(dto);
						}
					}
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
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
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}
	}
	
}
