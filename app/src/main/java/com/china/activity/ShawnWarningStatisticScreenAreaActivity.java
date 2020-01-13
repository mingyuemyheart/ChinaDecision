package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnWarningStatisticScreenAreaAdapter;
import com.china.dto.WarningDto;
import com.china.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 预警筛选选择区域
 */
public class ShawnWarningStatisticScreenAreaActivity extends ShawnBaseActivity implements OnClickListener {

    private Context mContext;
    private EditText etSearch;

    //搜索城市后的结果列表
    private ShawnWarningStatisticScreenAreaAdapter cityAdapter = null;
    private List<WarningDto> cityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_warning_statistic_screen_area);
        mContext = this;
        initWidget();
        initListView();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(watcher);
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("选择区域");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                etSearch.setFocusable(true);
                etSearch.setFocusableInTouchMode(true);
                etSearch.requestFocus();
                InputMethodManager inputManager = (InputMethodManager) etSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(etSearch, 0);
            }
        }, 300);
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void afterTextChanged(Editable arg0) {
            cityList.clear();
            cityAdapter.notifyDataSetChanged();

            if (!TextUtils.isEmpty(arg0.toString().trim())) {
                OkHttpArea("http://testdecision.tianqi.cn/alarm12379/hisgrepcityclild.php?k="+arg0.toString().trim());
            }

        }
    };

    /**
     * 初始化listview
     */
    private void initListView() {
        ListView mListView = findViewById(R.id.listView);
        cityAdapter = new ShawnWarningStatisticScreenAreaAdapter(mContext, cityList);
        mListView.setAdapter(cityAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                WarningDto data = cityList.get(arg2);
                Intent intent = new Intent();
                intent.putExtra("areaName", data.areaName);
                intent.putExtra("areaId", data.areaId);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void OkHttpArea(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
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
                                        cityList.clear();
                                        JSONArray array = new JSONArray(result);
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject itemObj = array.getJSONObject(i);

                                            String areaid = "";
                                            if (!itemObj.isNull("areaid")) {
                                                areaid = itemObj.getString("areaid");
                                            }

                                            String names = "";
                                            if (!itemObj.isNull("names")) {
                                                JSONArray nameArray = itemObj.getJSONArray("names");
                                                for (int j = 0; j < nameArray.length(); j++) {
                                                    if (TextUtils.isEmpty(names)) {
                                                        names = names+nameArray.getString(j);
                                                    }else {
                                                        names = names+"-"+nameArray.getString(j);
                                                    }
                                                }

                                                WarningDto dto = new WarningDto();
                                                dto.areaName = names;
                                                if (nameArray.length() == 1) {
                                                    dto.areaId = areaid.substring(0, 2);
                                                }else if (nameArray.length() == 2) {
                                                    dto.areaId = areaid.substring(0, 4);
                                                }else if (nameArray.length() == 3) {
                                                    dto.areaId = areaid;
                                                }
                                                cityList.add(dto);

                                                if (!itemObj.isNull("child")) {
                                                    JSONArray childArray = itemObj.getJSONArray("child");
                                                    for (int j = 0; j < childArray.length(); j++) {
                                                        JSONObject childObj = childArray.getJSONObject(j);
                                                        dto = new WarningDto();
                                                        if (!childObj.isNull("name")) {
                                                            dto.areaName = names+"-" + childObj.getString("name");
                                                        }
                                                        if (!childObj.isNull("areaid")) {
                                                            dto.areaId = childObj.getString("areaid").substring(0,4);
                                                        }
                                                        cityList.add(dto);
                                                    }
                                                }

                                            }

                                        }

                                        if (cityAdapter != null) {
                                            cityAdapter.notifyDataSetChanged();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;

            default:
                break;
        }
    }
}
