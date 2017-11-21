package com.china.activity;

/**
 * 预警筛选选择区域
 */

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
import com.china.adapter.WarningStatisticScreenAreaAdapter;
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

public class WarningStatisticScreenAreaActivity extends BaseActivity implements OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;//返回按钮
    private TextView tvTitle = null;
    private EditText etSearch = null;
    private String baseUrl = "http://decision.tianqi.cn/alarm12379/hisgrepcityclild.php?k=";

    //搜索城市后的结果列表
    private ListView mListView = null;
    private WarningStatisticScreenAreaAdapter cityAdapter = null;
    private List<WarningDto> cityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning_statistic_screen_area);
        mContext = this;
        initWidget();
        initListView();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(watcher);
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
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
            if (arg0.toString() == null) {
                return;
            }

            if (!TextUtils.isEmpty(arg0.toString().trim())) {
                OkHttpArea(baseUrl+arg0.toString().trim());
            }

        }
    };

    /**
     * 初始化listview
     */
    private void initListView() {
        mListView = (ListView) findViewById(R.id.listView);
        cityAdapter = new WarningStatisticScreenAreaAdapter(mContext, cityList);
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

    private void OkHttpArea(String url) {
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

                                if (nameArray.length() <= 2) {
                                    if (nameArray.length() == 1) {
                                        areaid = areaid.substring(0, 2);

                                        WarningDto dto = new WarningDto();
                                        dto.areaName = names;
                                        dto.areaId = areaid;
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
                                    }else if (nameArray.length() == 2) {
                                        areaid = areaid.substring(0, 4);

                                        WarningDto dto = new WarningDto();
                                        dto.areaName = names;
                                        dto.areaId = areaid;
                                        cityList.add(dto);
                                    }

                                }
                            }

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (cityList.size() > 0 && cityAdapter != null) {
                                    cityAdapter.notifyDataSetChanged();
                                }
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

            default:
                break;
        }
    }
}
