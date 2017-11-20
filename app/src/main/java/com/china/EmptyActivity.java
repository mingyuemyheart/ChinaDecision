package com.china;

/**
 * 用于屏屏联动，进入空界面
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

public class EmptyActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_empty);
		mContext = this;
		initWidget();
	}

	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);//栏目id
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
