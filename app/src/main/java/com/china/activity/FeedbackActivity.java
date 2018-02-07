package com.china.activity;

/**
 * 意见反馈
 */

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.R;
import com.china.common.CONST;
import com.china.utils.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FeedbackActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvControl = null;
	private EditText etContent = null;
	private String appid = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setText(getString(R.string.submit));
		tvControl.setVisibility(View.VISIBLE);
		tvControl.setOnClickListener(this);
		etContent = (EditText) findViewById(R.id.etContent);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}
		appid = getIntent().getStringExtra(CONST.INTENT_APPID);
	}
	
	/**
	 * 意见反馈
	 */
	private void OkHttpFeedback(String url) {
		if (TextUtils.isEmpty(url) || TextUtils.isEmpty(CONST.UID)) {
			return;
		}
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("uid", CONST.UID);
		builder.add("content", etContent.getText().toString());
		builder.add("appid", appid);
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
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
								JSONObject object = new JSONObject(result);
								if (object != null) {
									if (!object.isNull("status")) {
										int status  = object.getInt("status");
										if (status == 1) {//成功
											Toast.makeText(mContext, getString(R.string.submit_success), Toast.LENGTH_SHORT).show();
											finish();
										}else {
											//失败
											if (!object.isNull("msg")) {
												String msg = object.getString("msg");
												if (msg != null) {
													Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
												}
											}
										}
									}
								}
								cancelDialog();
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
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.tvControl:
				if (TextUtils.isEmpty(etContent.getText().toString())) {
					Toast.makeText(mContext, "请填写意见内容...", Toast.LENGTH_SHORT).show();
					return;
				}
				showDialog();
				OkHttpFeedback(CONST.INTERFACE_FEEDBACK);
				break;
		}
	}
}
