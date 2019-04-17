package com.china.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.R;
import com.china.common.CONST;
import com.china.common.MyApplication;
import com.china.utils.OkHttpUtil;
import com.china.view.MyRatingBar;

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
 * 服务反馈
 */
public class ShawnServiceFeedbackActivity extends ShawnBaseActivity implements View.OnClickListener {

    private Context context;
    private float selectedNumber1,selectedNumber2,selectedNumber3;
    private EditText etContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_service_feedback);
        context = this;
        initWidget();
    }

    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("服务反馈");
        etContent = findViewById(R.id.etContent);
        TextView tvSubmit = findViewById(R.id.tvSubmit);
        tvSubmit.setOnClickListener(this);
        MyRatingBar ratingBar1 = findViewById(R.id.ratingBar1);
        MyRatingBar ratingBar2 = findViewById(R.id.ratingBar2);
        MyRatingBar ratingBar3 = findViewById(R.id.ratingBar3);

        ratingBar1.setOnStarChangeListener(new MyRatingBar.OnStarChangeListener() {
            @Override
            public void OnStarChanged(float selectedNumber, int position) {
                selectedNumber1 = selectedNumber;
            }
        });
        ratingBar2.setOnStarChangeListener(new MyRatingBar.OnStarChangeListener() {
            @Override
            public void OnStarChanged(float selectedNumber, int position) {
                selectedNumber2 = selectedNumber;
            }
        });
        ratingBar3.setOnStarChangeListener(new MyRatingBar.OnStarChangeListener() {
            @Override
            public void OnStarChanged(float selectedNumber, int position) {
                selectedNumber3 = selectedNumber;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvSubmit:
                OkHttpSubmit();
                break;
        }
    }

    /**
     * 反馈
     */
    private void OkHttpSubmit() {
        if (getIntent().hasExtra(CONST.WEB_URL)) {
            String dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
            if (!TextUtils.isEmpty(dataUrl)) {
                final String url = "http://decision-admin.tianqi.cn/home/Evaluate/doinsert";
                FormBody.Builder builder = new FormBody.Builder();
                builder.add("uid", MyApplication.UID);
                builder.add("url", dataUrl);
                builder.add("whole", selectedNumber1+"");
                builder.add("service", selectedNumber2+"");
                builder.add("timely", selectedNumber3+"");
                builder.add("content", etContent.getText().toString());
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
                                                JSONObject obj = new JSONObject(result);
                                                if (!obj.isNull("msg")) {
                                                    String msg = obj.getString("msg");
                                                    if (!TextUtils.isEmpty(msg)) {
                                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                                if (!obj.isNull("status")) {
                                                    if (TextUtils.equals(obj.getString("status"), "1")) {//成功
                                                        setResult(RESULT_OK);
                                                        finish();
                                                    }
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
                }).start();
            }
        }
    }

}
