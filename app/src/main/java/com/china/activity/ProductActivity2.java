package com.china.activity;

/**
 * 农业气象等
 */

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
import com.china.adapter.ProductAdapter;
import com.china.common.CONST;
import com.china.common.ColumnData;
import com.china.dto.NewsDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.view.RefreshLayout;
import com.china.view.RefreshLayout.OnRefreshListener;

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

public class ProductActivity2 extends BaseActivity implements OnClickListener, OnRefreshListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private GridView gridView = null;
	private ProductAdapter mAdapter = null;
	private List<ColumnData> mList = new ArrayList<>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product2);
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
		refresh();
	}

	private void refresh() {
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		mList.clear();
		String dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(dataUrl)) {
			OkHttpList(dataUrl);
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

	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);

		refresh();
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
				if (TextUtils.equals(dto.showType, CONST.NEWS)) {//天气资讯
					intent = new Intent(mContext, WeatherInfoActivity.class);
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
							intent = new Intent(mContext, WeatherInfoDetailActivity.class);
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
								ColumnData dto = new ColumnData();
								dto.name = itemObj.getString("l1");
								dto.dataUrl = itemObj.getString("l2");
								dto.icon = itemObj.getString("l4");
								mList.add(dto);
							}
						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (mList.size() > 0 && mAdapter != null) {
									mAdapter.notifyDataSetChanged();
								}
								refreshLayout.setRefreshing(false);
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
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
		}
	}
	
}
