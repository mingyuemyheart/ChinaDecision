package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnDisasterReportAdapter;
import com.china.common.CONST;
import com.china.dto.DisasterReportDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.tendcloud.tenddata.TCAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 灾情直报
 * @author shawn_sun
 */
public class DisasterReportActivity extends ShawnBaseActivity implements OnClickListener {
	
	private Context mContext = null;
	private TextView tvTitle = null;
	private ShawnDisasterReportAdapter mAdapter = null;
	private List<DisasterReportDto> dataList = new ArrayList<>();
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_titlebar_listview);
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
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		OkHttpDisaster();
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);

		refresh();
	}
	
	private void initListView() {
		ListView mListView = findViewById(R.id.listView);
		mAdapter = new ShawnDisasterReportAdapter(mContext, dataList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				DisasterReportDto dto = dataList.get(arg2);
				Intent intent = new Intent(mContext, DisasterReportDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	/**
	 * 获取灾情直报
	 */
	private void OkHttpDisaster() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = "http://decision-admin.tianqi.cn/infomes/data/chinaweather/zqzb.html";
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
										if (!obj.isNull("response")) {
											dataList.clear();
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
													dataList.add(dto);
												}

												if (dataList.size() > 0) {
													Collections.sort(dataList, new Comparator<DisasterReportDto>() {
														@Override
														public int compare(DisasterReportDto arg0, DisasterReportDto arg1) {
															return arg1.vEdittime.compareTo(arg0.vEdittime);
														}
													});

													if (mAdapter != null) {
														mAdapter.notifyDataSetChanged();
													}
												}
												refreshLayout.setRefreshing(false);
											}
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
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
