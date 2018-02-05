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
import android.widget.TextView;

import com.china.R;
import com.china.adapter.DisasterSpecialAdapter;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 灾情专报
 * @author shawn_sun
 *
 */
public class DisasterSpecialActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private DisasterSpecialAdapter mAdapter = null;
	private List<DisasterDto> mList = new ArrayList<>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disaster_special);
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
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		String url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url) && url.contains("newGetDecistionZXZB")) {
			url = url.replace("newGetDecistionZXZB", "newGetDecistionZXZB/new/1");
			OkHttpList(url);
		}

		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);

		refresh();
	}
	
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new DisasterSpecialAdapter(mContext, mList);
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

									if (mList.size() > 0 && mAdapter != null) {
										mAdapter.notifyDataSetChanged();
									}
									refreshLayout.setRefreshing(false);
									cancelDialog();

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
