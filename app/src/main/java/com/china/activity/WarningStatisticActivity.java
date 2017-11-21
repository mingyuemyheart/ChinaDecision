package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.WarningStatisticGroupAdapter;
import com.china.dto.WarningDto;
import com.china.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 预警统计
 */

public class WarningStatisticActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private TextView tvControl = null;
    private TextView tvTime = null;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");
    private SimpleDateFormat sdf3 = new SimpleDateFormat("MM月dd日");
    private SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy0101000000");
    private SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMdd000000");
    private SimpleDateFormat sdf6 = new SimpleDateFormat("yyyyMMddHHmmss");
    private ExpandableListView listView = null;
    private WarningStatisticGroupAdapter mAdapter = null;
    private List<WarningDto> groupList = new ArrayList<>();
    private List<List<WarningDto>> childList = new ArrayList<>();
    private String areaName = "全国";
    private String areaId = null;
    private String baseUrl = "http://decision.tianqi.cn/alarm12379/hisalarmcount.php?format=1";
    private String startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning_statistic);
        mContext = this;
        showDialog();
        initWidget();
        initListView();
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(areaName+"预警统计");
        tvControl = (TextView) findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("筛选");
        tvControl.setVisibility(View.VISIBLE);
        tvTime = (TextView) findViewById(R.id.tvTime);

        startTime = sdf4.format(new Date());
        endTime = sdf5.format(new Date());
        String url = baseUrl;
        if (getIntent().hasExtra("data")) {
            WarningDto data = getIntent().getExtras().getParcelable("data");
            areaName = data.areaName;
            tvTitle.setText(areaName+"预警统计");
            if (!TextUtils.isEmpty(data.areaKey)) {
                areaId = data.areaKey;
                url = baseUrl+"&areaid="+data.areaKey+"&starttime="+startTime+"&endtime="+endTime;
            }
        }
        OkHttpStatistic(url, true);
    }

    private void initListView() {
        listView = (ExpandableListView) findViewById(R.id.listView);
        mAdapter = new WarningStatisticGroupAdapter(mContext, groupList, childList, listView);
        listView.setAdapter(mAdapter);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                WarningDto dto = groupList.get(groupPosition);
                if (TextUtils.equals(dto.areaKey, "all")) {//总计不能点击
                    return true;
                }
                Intent intent = new Intent(mContext, WarningStatisticListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("data", dto);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }
        });
    }

    /**
     * 获取预警统计信息
     * @param url
     */
    private void OkHttpStatistic(String url, final boolean isParseTime) {
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
                        JSONObject object = new JSONObject(result);
                        if (isParseTime) {
                            if (!object.isNull("time")) {
                                final String time = object.getString("time");
                                if (!TextUtils.isEmpty(time)) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                tvTime.setText(sdf2.format(sdf1.parse(time))+" - "+sdf3.format(new Date()));
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        if (!object.isNull("data")) {
                            groupList.clear();
                            childList.clear();
                            JSONArray array = object.getJSONArray("data");
                            for (int i = 0; i < array.length(); i++) {
                                WarningDto dto = new WarningDto();
                                JSONObject obj = array.getJSONObject(i);
                                if (!obj.isNull("name")) {
                                    dto.areaName = obj.getString("name");
                                }
                                if (!obj.isNull("areaid")) {
                                    dto.areaId = obj.getString("areaid");
                                }
                                if (!obj.isNull("areaKey")) {
                                    dto.areaKey = obj.getString("areaKey");
                                }
                                if (!obj.isNull("count")) {
                                    String[] count = obj.getString("count").split("\\|");
                                    dto.warningCount = count[0];
                                    dto.redCount = count[1];
                                    dto.orangeCount = count[2];
                                    dto.yellowCount = count[3];
                                    dto.blueCount = count[4];
                                }

                                if (!obj.isNull("list")) {
                                    List<WarningDto> list = new ArrayList<>();
                                    list.clear();
                                    JSONArray listArray = obj.getJSONArray("list");
                                    for (int j = 0; j < listArray.length(); j++) {
                                        JSONObject itemObj = listArray.getJSONObject(j);
                                        WarningDto d = new WarningDto();
                                        if (!itemObj.isNull("name")) {
                                            d.shortName = itemObj.getString("name");
                                        }
                                        if (!itemObj.isNull("code")) {
                                            d.type = itemObj.getString("code");
                                        }
                                        if (!itemObj.isNull("count")) {
                                            String[] count = itemObj.getString("count").split("\\|");
                                            d.warningCount = count[0];
                                            d.redCount = count[1];
                                            d.orangeCount = count[2];
                                            d.yellowCount = count[3];
                                            d.blueCount = count[4];
                                        }
                                        list.add(d);
                                    }
                                    childList.add(list);
                                }

                                groupList.add(dto);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (groupList.size() > 0 && mAdapter != null) {
                                    mAdapter.notifyDataSetChanged();
                                }
                                cancelDialog();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
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
            case R.id.tvControl:
                Intent intent = new Intent(mContext, WarningStatisticScreenActivity.class);
                intent.putExtra("startTime", startTime);
                intent.putExtra("endTime", endTime);
                intent.putExtra("areaName", areaName);
                intent.putExtra("areaId", areaId);
                startActivityForResult(intent, 1000);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1000:
                    areaId = data.getExtras().getString("areaId");
                    if (TextUtils.isEmpty(areaId)) {
                        return;
                    }
                    areaName = data.getExtras().getString("areaName");
                    startTime = data.getExtras().getString("startTime");
                    endTime = data.getExtras().getString("endTime");
                    tvTitle.setText(areaName+"预警统计");
                    try {
                        tvTime.setText(sdf2.format(sdf6.parse(startTime))+" - "+sdf3.format(sdf6.parse(endTime)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String url = baseUrl+"&areaid="+areaId+"&starttime="+startTime+"&endtime="+endTime;
                    OkHttpStatistic(url, false);
                    break;
            }
        }
    }
}
