package com.china.activity;

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
import com.china.common.MyApplication;
import com.china.utils.OkHttpUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 意见反馈
 */
public class ShawnFeedbackActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;
	private EditText etContent;
	private AVLoadingIndicatorView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_feedback);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
        loadingView = findViewById(R.id.loadingView);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		TextView tvControl = findViewById(R.id.tvControl);
		tvControl.setText(getString(R.string.submit));
		tvControl.setVisibility(View.VISIBLE);
		tvControl.setOnClickListener(this);
		etContent = findViewById(R.id.etContent);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}
	}
	
	/**
	 * 意见反馈
	 */
	private void OkHttpFeedback() {
        loadingView.setVisibility(View.VISIBLE);
		final String url = "http://decision-admin.tianqi.cn/home/Work/request";
		FormBody.Builder builder = new FormBody.Builder();
		if (!TextUtils.isEmpty(MyApplication.UID)) {
            builder.add("uid", MyApplication.UID);
        }
		builder.add("content", etContent.getText().toString());
		builder.add("appid", CONST.APPID);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
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
									} catch (JSONException e) {
										e.printStackTrace();
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
			case R.id.tvControl:
				if (TextUtils.isEmpty(etContent.getText().toString())) {
					Toast.makeText(mContext, "请填写意见内容...", Toast.LENGTH_SHORT).show();
					return;
				}
				OkHttpFeedback();
				break;
		}
	}
}
