package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 产品订阅
 */
public class ProductOrderActivity extends BaseActivity implements View.OnClickListener {

    private Context context;
    private TextView tvMail,tvReceiveTime,tvStartTime,tvLoseTime;
    private WheelView year,month,day,hour;
    private RelativeLayout reLayout;
    private int TIMETYPE1 = 1,TIMETYPE2 = 2,TIMETYPE3 = 3;//接收时间、开始时间、失效时间
    private int timeType = TIMETYPE1;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private String receiveTime = "", startTime = "", loseTime = "";
    private TextView tvOrder1,tvOrder2,tvOrder3,tvOrder4;
    private boolean isOrder1 = false,isOrder2 = false,isOrder3 = false,isOrder4 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_product_order);
        context = this;
        initWidget();
        initWheelView();
    }

    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("产品订阅");
        TextView tvControl = findViewById(R.id.tvControl);
        tvControl.setText("提交");
        tvControl.setVisibility(View.VISIBLE);
        tvControl.setOnClickListener(this);
        LinearLayout llMail = findViewById(R.id.llMail);
        llMail.setOnClickListener(this);
        LinearLayout llReceiveTime = findViewById(R.id.llReceiveTime);
        llReceiveTime.setOnClickListener(this);
        LinearLayout llStartTime = findViewById(R.id.llStartTime);
        llStartTime.setOnClickListener(this);
        LinearLayout llLoseTime = findViewById(R.id.llLoseTime);
        llLoseTime.setOnClickListener(this);
        tvMail = findViewById(R.id.tvMail);
        tvReceiveTime = findViewById(R.id.tvReceiveTime);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvLoseTime = findViewById(R.id.tvLoseTime);
        TextView tvNegtive = findViewById(R.id.tvNegtive);
        tvNegtive.setOnClickListener(this);
        TextView tvPositive = findViewById(R.id.tvPositive);
        tvPositive.setOnClickListener(this);
        reLayout = findViewById(R.id.reLayout);
        reLayout.setOnClickListener(this);
        tvOrder1 = findViewById(R.id.tvOrder1);
        tvOrder1.setOnClickListener(this);
        tvOrder2 = findViewById(R.id.tvOrder2);
        tvOrder2.setOnClickListener(this);
        tvOrder3 = findViewById(R.id.tvOrder3);
        tvOrder3.setOnClickListener(this);
        tvOrder4 = findViewById(R.id.tvOrder4);
        tvOrder4.setOnClickListener(this);
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

        if (timeType == TIMETYPE1) {
            receiveTime = hourStr;
            tvReceiveTime.setText(receiveTime+"时");
        }else if (timeType == TIMETYPE2) {
            startTime = yearStr+"-"+monthStr+"-"+dayStr;
            tvStartTime.setText(startTime);
        }else if (timeType == TIMETYPE3) {
            loseTime = yearStr+"-"+monthStr+"-"+dayStr;
        }

        try {
            if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(loseTime)) {
                long start = sdf1.parse(startTime).getTime();
                long lose = sdf1.parse(loseTime).getTime();
                if (start >= lose) {
                    Toast.makeText(this, "开始时间不能大于或等于失效时间", Toast.LENGTH_SHORT).show();
                }else {
                    tvLoseTime.setText(loseTime);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void bootTimeLayoutAnimation() {
        if (reLayout.getVisibility() == View.GONE) {
            timeLayoutAnimation(true, reLayout);
            reLayout.setVisibility(View.VISIBLE);
            if (timeType == TIMETYPE1) {
                year.setVisibility(View.GONE);
                month.setVisibility(View.GONE);
                day.setVisibility(View.GONE);
                hour.setVisibility(View.VISIBLE);
            }else {
                year.setVisibility(View.VISIBLE);
                month.setVisibility(View.VISIBLE);
                day.setVisibility(View.VISIBLE);
                hour.setVisibility(View.GONE);
            }
        }else {
            timeLayoutAnimation(false, reLayout);
            reLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 时间图层动画
     * @param flag
     * @param view
     */
    private void timeLayoutAnimation(boolean flag, final RelativeLayout view) {
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.llMail:
                startActivityForResult(new Intent(this, ModifyMailActivity.class), 1001);
                break;
            case R.id.llReceiveTime:
                timeType = TIMETYPE1;
                bootTimeLayoutAnimation();
                break;
            case R.id.llStartTime:
                timeType = TIMETYPE2;
                bootTimeLayoutAnimation();
                break;
            case R.id.llLoseTime:
                timeType = TIMETYPE3;
                bootTimeLayoutAnimation();
                break;
            case R.id.tvNegtive:
                bootTimeLayoutAnimation();
                break;
            case R.id.tvPositive:
                setTextViewValue();
                bootTimeLayoutAnimation();
                break;
            case R.id.tvOrder1:
                isOrder1 = !isOrder1;
                if (isOrder1) {
                    tvOrder1.setBackgroundResource(R.drawable.shawn_btn_order_blue);
                    tvOrder1.setTextColor(Color.WHITE);
                }else {
                    tvOrder1.setBackgroundResource(R.drawable.shawn_btn_order_gray);
                    tvOrder1.setTextColor(getResources().getColor(R.color.text_color4));
                }
                break;
            case R.id.tvOrder2:
                isOrder2 = !isOrder2;
                if (isOrder2) {
                    tvOrder2.setBackgroundResource(R.drawable.shawn_btn_order_blue);
                    tvOrder2.setTextColor(Color.WHITE);
                }else {
                    tvOrder2.setBackgroundResource(R.drawable.shawn_btn_order_gray);
                    tvOrder2.setTextColor(getResources().getColor(R.color.text_color4));
                }
                break;
            case R.id.tvOrder3:
                isOrder3 = !isOrder3;
                if (isOrder3) {
                    tvOrder3.setBackgroundResource(R.drawable.shawn_btn_order_blue);
                    tvOrder3.setTextColor(Color.WHITE);
                }else {
                    tvOrder3.setBackgroundResource(R.drawable.shawn_btn_order_gray);
                    tvOrder3.setTextColor(getResources().getColor(R.color.text_color4));
                }
                break;
            case R.id.tvOrder4:
                isOrder4 = !isOrder4;
                if (isOrder4) {
                    tvOrder4.setBackgroundResource(R.drawable.shawn_btn_order_blue);
                    tvOrder4.setTextColor(Color.WHITE);
                }else {
                    tvOrder4.setBackgroundResource(R.drawable.shawn_btn_order_gray);
                    tvOrder4.setTextColor(getResources().getColor(R.color.text_color4));
                }
                break;
            case R.id.tvControl:
                OkHttpSubmit();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1001:
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            String email = bundle.getString("email");
                            if (!TextUtils.isEmpty(email)) {
                                tvMail.setText(email);
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 提交
     */
    private void OkHttpSubmit() {
        if (TextUtils.isEmpty(tvMail.getText().toString())) {
            Toast.makeText(this, "请填写接收邮箱", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tvReceiveTime.getText().toString())) {
            Toast.makeText(this, "请选择接收时间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tvStartTime.getText().toString())) {
            Toast.makeText(this, "请选择开始时间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(tvLoseTime.getText().toString())) {
            Toast.makeText(this, "请选择失效时间", Toast.LENGTH_SHORT).show();
            return;
        }
        String file_flags = "";
        if (isOrder1) {
            file_flags += "1,";
        }
        if (isOrder2) {
            file_flags += "2,";
        }
        if (isOrder3) {
            file_flags += "3,";
        }
        if (isOrder4) {
            file_flags += "4,";
        }
        if (!TextUtils.isEmpty(file_flags) && file_flags.endsWith(",")) {
            file_flags = file_flags.substring(0, file_flags.length()-1);
        }
        if (TextUtils.isEmpty(file_flags)) {
            Toast.makeText(this, "请选择订阅内容", Toast.LENGTH_SHORT).show();
            return;
        }
        final String url = "https://decision-admin.tianqi.cn/home/other/decision_add_schedule_task";
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("add_name", MyApplication.USERNAME);
        builder.add("receive_email", tvMail.getText().toString());
        builder.add("send_hours", receiveTime);
        builder.add("start_time", startTime);
        builder.add("end_time", loseTime);
        builder.add("file_flags", file_flags);
        builder.add("content", "android提交");
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
