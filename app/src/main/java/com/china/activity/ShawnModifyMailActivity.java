package com.china.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.R;

import java.util.regex.Pattern;

/**
 * 修改订阅邮箱
 */
public class ShawnModifyMailActivity extends ShawnBaseActivity implements View.OnClickListener{

    private EditText etMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_modify_mail);
        initWidget();
    }

    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("接收邮箱");
        TextView tvControl = findViewById(R.id.tvControl);
        tvControl.setText("确定");
        tvControl.setVisibility(View.VISIBLE);
        tvControl.setOnClickListener(this);
        etMail = findViewById(R.id.etMail);
    }

    /**
     * 校验邮箱
     * @param email
     * @return 校验通过返回true，否则返回false
     */
    private boolean isEmail(String email) {
        String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        return Pattern.matches(REGEX_EMAIL, email);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvControl:
                if (TextUtils.isEmpty(etMail.getText().toString()) || !isEmail(etMail.getText().toString())) {
                    Toast.makeText(this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("email", etMail.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

}
