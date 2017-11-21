package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.R;
import com.china.wheelview.NumericWheelAdapter;
import com.china.wheelview.OnWheelScrollListener;
import com.china.wheelview.WheelView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 预警筛选
 *
 */
public class WarningStatisticScreenActivity extends BaseActivity implements OnClickListener{

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private TextView tvStartTime = null;
    private TextView tvEndTime = null;
    private TextView tvArea = null;
    private TextView tvCheck = null;

    private WheelView year;
    private WheelView month;
    private WheelView day;
    private WheelView hour;
    private WheelView minute;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");
    private SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy0101000000");
    private SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMdd000000");
    private SimpleDateFormat sdf6 = new SimpleDateFormat("yyyyMMddHHmmss");
    private String startTime, endTime;
    private TextView tvNegtive = null;
    private TextView tvPositive = null;
    private TextView tvContent = null;
    private RelativeLayout reLayout = null;
    private boolean startOrEnd = true;//true为start
    private String areaName = null;
    private String areaId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning_statistic_screen);
        mContext = this;
        initWidget();
        initWheelView();
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("预警筛选");
        tvStartTime = (TextView) findViewById(R.id.tvStartTime);
        tvStartTime.setOnClickListener(this);
        tvEndTime = (TextView) findViewById(R.id.tvEndTime);
        tvEndTime.setOnClickListener(this);
        tvArea = (TextView) findViewById(R.id.tvArea);
        tvArea.setOnClickListener(this);
        tvCheck = (TextView) findViewById(R.id.tvCheck);
        tvCheck.setOnClickListener(this);
        tvNegtive = (TextView) findViewById(R.id.tvNegtive);
        tvNegtive.setOnClickListener(this);
        tvPositive = (TextView) findViewById(R.id.tvPositive);
        tvPositive.setOnClickListener(this);
        tvContent = (TextView) findViewById(R.id.tvContent);
        reLayout = (RelativeLayout) findViewById(R.id.reLayout);
        reLayout.setOnClickListener(this);

        try {
            startTime = getIntent().getStringExtra("startTime");
            tvStartTime.setText(sdf2.format(sdf6.parse(startTime)));
            endTime = getIntent().getStringExtra("endTime");
            tvEndTime.setText(sdf2.format(sdf6.parse(endTime)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        areaId = getIntent().getStringExtra("areaId");
        areaName = getIntent().getStringExtra("areaName");
        tvArea.setText(areaName);
    }

    private void initWheelView() {
        Calendar c = Calendar.getInstance();
        int curYear = c.get(Calendar.YEAR);
        int curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
        int curDate = c.get(Calendar.DATE);
        int curHour = c.get(Calendar.HOUR_OF_DAY);
        int curMinute = c.get(Calendar.MINUTE);

        year = (WheelView) findViewById(R.id.year);
        NumericWheelAdapter numericWheelAdapter1=new NumericWheelAdapter(this,1950, curYear);
        numericWheelAdapter1.setLabel(getString(R.string.year));
        year.setViewAdapter(numericWheelAdapter1);
        year.setCyclic(false);//是否可循环滑动
        year.addScrollingListener(scrollListener);

        month = (WheelView) findViewById(R.id.month);
        NumericWheelAdapter numericWheelAdapter2=new NumericWheelAdapter(this,1, 12, "%02d");
        numericWheelAdapter2.setLabel(getString(R.string.month));
        month.setViewAdapter(numericWheelAdapter2);
        month.setCyclic(false);
        month.addScrollingListener(scrollListener);

        day = (WheelView) findViewById(R.id.day);
        initDay(curYear,curMonth);
        day.setCyclic(false);

        hour = (WheelView) findViewById(R.id.hour);
        NumericWheelAdapter numericWheelAdapter3=new NumericWheelAdapter(this,1, 23, "%02d");
        numericWheelAdapter3.setLabel(getString(R.string.hour));
        hour.setViewAdapter(numericWheelAdapter3);
        hour.setCyclic(false);
        hour.addScrollingListener(scrollListener);

        minute = (WheelView) findViewById(R.id.minute);
        NumericWheelAdapter numericWheelAdapter4=new NumericWheelAdapter(this,1, 59, "%02d");
        numericWheelAdapter4.setLabel(getString(R.string.minute));
        minute.setViewAdapter(numericWheelAdapter4);
        minute.setCyclic(false);
        minute.addScrollingListener(scrollListener);

        year.setVisibleItems(7);
        month.setVisibleItems(7);
        day.setVisibleItems(7);
        hour.setVisibleItems(7);
        minute.setVisibleItems(7);

        year.setCurrentItem(curYear - 1950);
        month.setCurrentItem(curMonth - 1);
        day.setCurrentItem(curDate - 1);
        hour.setCurrentItem(curHour - 1);
        minute.setCurrentItem(curMinute);
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
        int day = 30;
        boolean flag = false;
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
     * 时间图层动画
     * @param flag
     * @param view
     */
    private void timeLayoutAnimation(boolean flag, final RelativeLayout view) {
        //列表动画
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation animation = null;
        if (flag == false) {
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
        animationSet.setAnimationListener(new AnimationListener() {
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

    /**
     */
    private void setTextViewValue() {
        String yearStr = String.valueOf(year.getCurrentItem()+1950);
        String monthStr = String.valueOf((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1));
        String dayStr = String.valueOf(((day.getCurrentItem()+1) < 10) ? "0" + (day.getCurrentItem()+1) : (day.getCurrentItem()+1));
        String hourStr = String.valueOf(((hour.getCurrentItem()+1) < 10) ? "0" + (hour.getCurrentItem()+1) : (hour.getCurrentItem()+1));
        String minuteStr = String.valueOf(((minute.getCurrentItem()+1) < 10) ? "0" + (minute.getCurrentItem()+1) : (minute.getCurrentItem()+1));
        String time = yearStr+"年"+monthStr+"月"+dayStr+"日";

        if (startOrEnd) {
            try {
                tvStartTime.setText(time);
                startTime = sdf5.format(sdf2.parse(time));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else {
            try {
                tvEndTime.setText(time);
                endTime = sdf5.format(sdf2.parse(time));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void bootTimeLayoutAnimation() {
        if (reLayout.getVisibility() == View.GONE) {
            timeLayoutAnimation(true, reLayout);
            reLayout.setVisibility(View.VISIBLE);
        }else {
            timeLayoutAnimation(false, reLayout);
            reLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvStartTime:
                startOrEnd = true;
                tvContent.setText(getString(R.string.select_start_time));
                bootTimeLayoutAnimation();

                int y = Integer.valueOf(startTime.substring(0, 4));
                int m = Integer.valueOf(startTime.substring(4, 6));
                int d = Integer.valueOf(startTime.substring(6, 8));
                year.setCurrentItem(y - 1950);
                month.setCurrentItem(m - 1);
                day.setCurrentItem(d - 1);
                break;
            case R.id.tvEndTime:
                startOrEnd = false;
                tvContent.setText(getString(R.string.select_end_time));
                bootTimeLayoutAnimation();

                int y2 = Integer.valueOf(endTime.substring(0, 4));
                int m2 = Integer.valueOf(endTime.substring(4, 6));
                int d2 = Integer.valueOf(endTime.substring(6, 8));
                year.setCurrentItem(y2 - 1950);
                month.setCurrentItem(m2 - 1);
                day.setCurrentItem(d2 - 1);
                break;
            case R.id.tvArea:
                startActivityForResult(new Intent(mContext, WarningStatisticScreenAreaActivity.class), 1000);
                break;
            case R.id.tvNegtive:
                bootTimeLayoutAnimation();
                break;
            case R.id.tvPositive:
                setTextViewValue();
                bootTimeLayoutAnimation();
                break;
            case R.id.tvCheck:
                try {
                    long lStart = sdf6.parse(startTime).getTime();
                    long lEnd = sdf6.parse(endTime).getTime();
                    if (lStart >= lEnd) {
                        Toast.makeText(mContext, getString(R.string.start_big_end), Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        Intent intent = new Intent();
                        intent.putExtra("startTime", startTime);
                        intent.putExtra("endTime", endTime);
                        intent.putExtra("areaName", areaName);
                        intent.putExtra("areaId", areaId);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1000:
                    areaName = data.getExtras().getString("areaName");
                    tvArea.setText(areaName);
                    areaId = data.getExtras().getString("areaId");
                    break;
            }
        }
    }
}
