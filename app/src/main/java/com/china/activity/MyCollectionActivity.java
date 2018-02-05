package com.china.activity;

/**
 * 我的收藏
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.china.manager.MyCollectManager;
import com.china.utils.CommonUtil;
import com.tendcloud.tenddata.TCAgent;

import java.util.ArrayList;
import java.util.List;

public class MyCollectionActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle, tvPrompt;
	private ListView mListView = null;
	private NewsAdapter mAdapter = null;
	private List<NewsDto> dataList = new ArrayList<>();//存放收藏数据的list
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selection);
		mContext = this;
		initWidget();
		initListView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvPrompt = (TextView) findViewById(R.id.tvPrompt);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	private void getCollectData() {
		//判断是否是已收藏
		dataList.clear();
		int size = MyCollectManager.readCollect(MyCollectionActivity.this, dataList);
		if (size > 0) {
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
			tvPrompt.setVisibility(View.GONE);
		}else {
			tvPrompt.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		getCollectData();
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new NewsAdapter(mContext, dataList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				NewsDto dto = dataList.get(arg2);
				Intent intent = new Intent(mContext, WeatherInfoDetailActivity.class);
				intent.putExtra("data", dto);
				intent.putExtra(CONST.WEB_URL, dto.detailUrl);
				startActivityForResult(intent, 0);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 0:
					getCollectData();
					break;
			}
		}
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
