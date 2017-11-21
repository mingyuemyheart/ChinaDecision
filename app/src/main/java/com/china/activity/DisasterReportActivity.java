package com.china.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.DisasterReportAdapter;
import com.china.common.CONST;
import com.china.dto.DisasterReportDto;
import com.china.utils.CommonUtil;
import com.china.utils.CustomHttpClient;
import com.china.view.RefreshLayout;
import com.china.view.RefreshLayout.OnRefreshListener;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 灾情直报
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class DisasterReportActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private DisasterReportAdapter mAdapter = null;
	private List<DisasterReportDto> mList = new ArrayList<DisasterReportDto>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private String url = "http://decision-admin.tianqi.cn/infomes/data/chinaweather/zqzb.html";
	private ProgressBar progressBar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.disaster_report);
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
				queryDisaster(url);
			}
		});
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
		
		queryDisaster(url);
		
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new DisasterReportAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				DisasterReportDto dto = mList.get(arg2);
				Intent intent = new Intent(mContext, DisasterReportDetailActivity.class);
				intent.putExtra("data", dto);
				startActivity(intent);
			}
		});
	}
	
	private void queryDisaster(String url) {
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
			if (requestResult != null) {
				try {
					JSONObject obj = new JSONObject(requestResult);
					if (!obj.isNull("response")) {
						mList.clear();
						JSONObject object = obj.getJSONObject("response");
						if (!object.isNull("directList")) {
							JSONArray array = object.getJSONArray("directList");
							for (int i = 0; i < array.length(); i++) {
								JSONObject itemObj = array.getJSONObject(i);
								DisasterReportDto dto = new DisasterReportDto();
								dto.vSendername = itemObj.getString("vSendername");
								dto.vCategory = itemObj.getString("vCategory");
								dto.vEdittime = itemObj.getString("vEdittime");
								dto.vGeneralLoss = itemObj.getString("vGeneralLoss");
								dto.vRzDpop = itemObj.getString("vRzDpop");
								dto.vEditor = itemObj.getString("vEditor");
								dto.vTaPhone = itemObj.getString("vTaPhone");
								dto.vSummary = itemObj.getString("vSummary");
								dto.vInfluenceDiscri = itemObj.getString("vInfluenceDiscri");
								dto.vStartTime = itemObj.getString("vStartTime");
								dto.vEndTime = itemObj.getString("vEndTime");
								dto.dRecordId = itemObj.getString("dRecordId");
								mList.add(dto);
							}
							
							if (mList.size() > 0) {
								Collections.sort(mList, new Comparator<DisasterReportDto>() {
									@Override
									public int compare(DisasterReportDto arg0, DisasterReportDto arg1) {
										return arg1.vEdittime.compareTo(arg0.vEdittime);
									}
								});
								
								if (mAdapter != null) {
									mAdapter.notifyDataSetChanged();
								}
							}
							progressBar.setVisibility(View.GONE);
							refreshLayout.setRefreshing(false);
						}
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
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
