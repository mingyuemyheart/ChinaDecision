package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnProductAdapter;
import com.china.common.CONST;
import com.china.common.ColumnData;
import com.china.common.MyApplication;
import com.china.dto.NewsDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.tendcloud.tenddata.TCAgent;
import com.wang.avi.AVLoadingIndicatorView;

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
 * 农业气象等
 */
public class ShawnProductActivity2 extends ShawnBaseActivity implements OnClickListener {
	
	private Context mContext;
	private TextView tvTitle;
	private ShawnProductAdapter mAdapter;
	private List<ColumnData> dataList = new ArrayList<>();
	private AVLoadingIndicatorView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_product2);
		mContext = this;
		initWidget();
		initListView();
	}

	private void initWidget() {
		loadingView = findViewById(R.id.loadingView);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		dataList.clear();
		String dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(dataUrl)) {
			loadingView.setVisibility(View.VISIBLE);
			OkHttpList(dataUrl);
		}else {
			ColumnData data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				tvTitle.setText(data.name);
				dataList.clear();
				String columnIds = MyApplication.getColumnIds(this);
				if (!TextUtils.isEmpty(columnIds)) {
					for (int i = 0; i < data.child.size(); i++) {
						ColumnData dto = data.child.get(i);
						if (columnIds.contains(dto.columnId)) {//已经有保存的栏目
							dataList.add(dto);
						}
					}
				}else {
					dataList.addAll(data.child);
				}
			}
		}
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		GridView gridView = findViewById(R.id.gridView);
		mAdapter = new ShawnProductAdapter(mContext, dataList);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = dataList.get(arg2);
				Intent intent;
				if (TextUtils.equals(dto.showType, CONST.NEWS)) {//天气资讯
					intent = new Intent(mContext, ShawnWeatherInfoActivity.class);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else {
					if (!TextUtils.isEmpty(dto.dataUrl)) {
						if (dto.dataUrl.contains(".pdf") || dto.dataUrl.contains(".PDF")) {//pdf格式
							intent = new Intent(mContext, ShawnPDFActivity.class);
							intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
							intent.putExtra(CONST.WEB_URL, dto.dataUrl);
							startActivity(intent);
						}else {//网页、图片
							intent = new Intent(mContext, ShawnNewsDetailActivity.class);
							NewsDto data = new NewsDto();
							data.title = dto.name;
							data.detailUrl = dto.dataUrl;
							data.imgUrl = dto.icon;
							Bundle bundle = new Bundle();
							bundle.putParcelable("data", data);
							intent.putExtras(bundle);

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
												ColumnData dto = new ColumnData();
												dto.name = itemObj.getString("l1");
												dto.dataUrl = itemObj.getString("l2");
												dto.icon = itemObj.getString("l4");
												dataList.add(dto);
											}
										}
										if (mAdapter != null) {
											mAdapter.notifyDataSetChanged();
										}
									} catch (JSONException e1) {
										e1.printStackTrace();
									}
								}
								loadingView.setVisibility(View.GONE);
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
