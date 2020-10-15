package com.china.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.R;
import com.china.common.MyApplication;
import com.china.utils.OkHttpUtil;
import com.china.wheelview.NumericWheelAdapter;
import com.china.wheelview.OnWheelScrollListener;
import com.china.wheelview.WheelView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 产品定制-定制
 */
public class ProductCustomSubmitActivity extends BaseActivity implements View.OnClickListener {

    private Context context;
    private EditText etUnit,etUse,etScope,etType,etPerson,etPhone,etContent;
    private TextView tvTime;
    private WheelView year,month,day,hour;
    private ConstraintLayout clLayout;
    private long mExitTime;//记录点击完返回按钮后的long型时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_custom_submit);
        context = this;
        initWidget();
        initWheelView();
    }

    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("气象信息需求单");
        TextView tvControl = findViewById(R.id.tvControl);
        tvControl.setText("提交");
        tvControl.setVisibility(View.VISIBLE);
        tvControl.setOnClickListener(this);
        tvTime = findViewById(R.id.tvTime);
        tvTime.setOnClickListener(this);
        etUnit = findViewById(R.id.etUnit);
        etUse = findViewById(R.id.etUse);
        etScope = findViewById(R.id.etScope);
        etType = findViewById(R.id.etType);
        etPerson = findViewById(R.id.etPerson);
        etPhone = findViewById(R.id.etPhone);
        etContent = findViewById(R.id.etContent);
        TextView tvNegtive = findViewById(R.id.tvNegtive);
        tvNegtive.setOnClickListener(this);
        TextView tvPositive = findViewById(R.id.tvPositive);
        tvPositive.setOnClickListener(this);
        clLayout = findViewById(R.id.clLayout);
        clLayout.setOnClickListener(this);
    }

    private void initWheelView() {
        Calendar c = Calendar.getInstance();
        int curYear = c.get(Calendar.YEAR);
        int curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
        int curDate = c.get(Calendar.DATE);
        int curHour = c.get(Calendar.HOUR_OF_DAY);

        year = findViewById(R.id.year);
        NumericWheelAdapter numericWheelAdapter1=new NumericWheelAdapter(this,1950, curYear);
        numericWheelAdapter1.setLabel(getString(R.string.year));
        year.setViewAdapter(numericWheelAdapter1);
        year.setCyclic(false);//是否可循环滑动
        year.addScrollingListener(scrollListener);

        month = findViewById(R.id.month);
        NumericWheelAdapter numericWheelAdapter2=new NumericWheelAdapter(this,1, 12, "%02d");
        numericWheelAdapter2.setLabel(getString(R.string.month));
        month.setViewAdapter(numericWheelAdapter2);
        month.setCyclic(false);
        month.addScrollingListener(scrollListener);

        day = findViewById(R.id.day);
        initDay(curYear,curMonth);
        day.setCyclic(false);

        hour = findViewById(R.id.hour);
        NumericWheelAdapter numericWheelAdapter3=new NumericWheelAdapter(this,1, 23, "%02d");
        numericWheelAdapter3.setLabel(getString(R.string.hour));
        hour.setViewAdapter(numericWheelAdapter3);
        hour.setCyclic(false);
        hour.addScrollingListener(scrollListener);

        year.setVisibleItems(7);
        month.setVisibleItems(7);
        day.setVisibleItems(7);
        hour.setVisibleItems(7);

        year.setCurrentItem(curYear - 1950);
        month.setCurrentItem(curMonth - 1);
        day.setCurrentItem(curDate - 1);
        hour.setCurrentItem(curHour - 1);
    }

    private OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {
        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            int n_year = year.getCurrentItem() + 1950;//年
            int n_month = month.getCurrentItem() + 1;//月
            initDay(n_year,n_month);
        }
    };

    /**
     */
    private void initDay(int arg1, int arg2) {
        NumericWheelAdapter numericWheelAdapter=new NumericWheelAdapter(this,1, getDay(arg1, arg2), "%02d");
        numericWheelAdapter.setLabel(getString(R.string.day));
        day.setViewAdapter(numericWheelAdapter);
    }

    /**
     *
     * @param year
     * @param month
     * @return
     */
    private int getDay(int year, int month) {
        int day;
        boolean flag;
        switch (year % 4) {
            case 0:
                flag = true;
                break;
            default:
                flag = false;
                break;
        }
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                day = 31;
                break;
            case 2:
                day = flag ? 29 : 28;
                break;
            default:
                day = 30;
                break;
        }
        return day;
    }

    /**
     */
    private void setTextViewValue() {
        String yearStr = String.valueOf(year.getCurrentItem()+1950);
        String monthStr = String.valueOf((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1));
        String dayStr = String.valueOf(((day.getCurrentItem()+1) < 10) ? "0" + (day.getCurrentItem()+1) : (day.getCurrentItem()+1));
        String hourStr = String.valueOf(((hour.getCurrentItem()+1) < 10) ? "0" + (hour.getCurrentItem()+1) : (hour.getCurrentItem()+1));

        tvTime.setText(yearStr+"-"+monthStr+"-"+dayStr+" "+hourStr+"时");
    }

    private void bootTimeLayoutAnimation() {
        if (clLayout.getVisibility() == View.GONE) {
            timeLayoutAnimation(true, clLayout);
            clLayout.setVisibility(View.VISIBLE);
        }else {
            timeLayoutAnimation(false, clLayout);
            clLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 时间图层动画
     * @param flag
     * @param view
     */
    private void timeLayoutAnimation(boolean flag, final View view) {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation animation;
        if (!flag) {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,1f);
        }else {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,1f,
                    Animation.RELATIVE_TO_SELF,0f);
        }
        animation.setDuration(400);
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        view.startAnimation(animationSet);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                view.clearAnimation();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "确定退出当前编辑界面？", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvTime:
            case R.id.tvNegtive:
                bootTimeLayoutAnimation();
                break;
            case R.id.tvPositive:
                setTextViewValue();
                bootTimeLayoutAnimation();
                break;
            case R.id.tvControl:
                OkHttpSubmit();
                break;
        }
    }

    /**
     * 提交
     */
    private void OkHttpSubmit() {
        if (TextUtils.isEmpty(etUnit.getText().toString())) {
            Toast.makeText(this, "请输入需求单位", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etUse.getText().toString())) {
            Toast.makeText(this, "请输入信息用途", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etScope.getText().toString())) {
            Toast.makeText(this, "请输入信息范围", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tvTime.getText().toString())) {
            Toast.makeText(this, "请选择提供时间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etType.getText().toString())) {
            Toast.makeText(this, "请输入提供方式", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etPerson.getText().toString())) {
            Toast.makeText(this, "请输入联系人", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etPhone.getText().toString())) {
            Toast.makeText(this, "请输入联系方式", Toast.LENGTH_SHORT).show();
            return;
        }
        showDialog();
        final String url = "https://decision-admin.tianqi.cn/home/work2019/decision_add_demand";
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("uid", MyApplication.UID);
        builder.add("department", etUnit.getText().toString());
        builder.add("usefor", etUse.getText().toString());
        builder.add("range", etScope.getText().toString());
        builder.add("provide_time", tvTime.getText().toString());
        builder.add("provide_email", etType.getText().toString());
        builder.add("user_name", etPerson.getText().toString());
        builder.add("user_tel", etPhone.getText().toString());
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
                                cancelDialog();
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
                                            String status = obj.getString("status");
                                            if (TextUtils.equals(status, "1")) {
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
