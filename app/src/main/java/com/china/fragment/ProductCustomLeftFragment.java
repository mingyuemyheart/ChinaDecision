package com.china.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnProductOrderAdapter;
import com.china.common.MyApplication;
import com.china.dto.DisasterDto;
import com.china.utils.CommonUtil;
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
 * 产品定制-需求单
 */
public class ProductCustomLeftFragment extends Fragment {

    private LinearLayout llContainer, llContainer1;
    private ShawnProductOrderAdapter mAdapter;
    private List<DisasterDto> dataList = new ArrayList<>();
    private List<DisasterDto> dataList1 = new ArrayList<>();
    private List<DisasterDto> dataList2 = new ArrayList<>();
    private List<DisasterDto> dataList3 = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_custom_left, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initWidget(view);
        initListView(view);
    }

    private void initWidget(View view) {
        llContainer = view.findViewById(R.id.llContainer);
        llContainer1 = view.findViewById(R.id.llContainer1);
        llContainer.removeAllViews();
        llContainer1.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.weight = 1;
        params1.setMargins((int) CommonUtil.dip2px(getActivity(), 20), 0, (int)CommonUtil.dip2px(getActivity(), 20), 0);
        for (int i = 0; i < 3; i++) {
            TextView tvName = new TextView(getActivity());
            String name;
            if (i == 0) {
                name = "全部";
            }else if (i == 1) {
                name = "待审批";
            }else {
                name = "已审批";
            }
            tvName.setText(name);
            tvName.setPadding(0, 20, 0, 20);
            tvName.setGravity(Gravity.CENTER);
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            tvName.setTextColor(getResources().getColor(R.color.text_color3));
            tvName.setLayoutParams(params);
            tvName.setTag(name);
            llContainer.addView(tvName);

            TextView tvBar = new TextView(getActivity());
            tvBar.setGravity(Gravity.CENTER);
            tvBar.setLayoutParams(params1);
            if (i == 0) {
                tvBar.setBackgroundColor(getResources().getColor(R.color.blue));
            }else {
                tvBar.setBackgroundColor(Color.TRANSPARENT);
            }
            llContainer1.addView(tvBar);

            tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = v.getTag()+"";
                    for (int j = 0; j < llContainer.getChildCount(); j++) {
                        TextView tvName = (TextView) llContainer.getChildAt(j);
                        TextView tvBar = (TextView) llContainer1.getChildAt(j);
                        if (TextUtils.equals(name, tvName.getTag()+"")) {
                            tvBar.setBackgroundColor(getResources().getColor(R.color.blue));
                        }else {
                            tvBar.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }

                    dataList.clear();
                    if (TextUtils.equals(name, "待审批")) {//待审批
                        dataList.addAll(dataList2);
                    }else if (TextUtils.equals(name, "已审批")) {//已审批
                        dataList.addAll(dataList3);
                    }else {//全部
                        dataList.addAll(dataList1);
                    }
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void initListView(View view) {
        ListView listView = view.findViewById(R.id.listView);
        mAdapter = new ShawnProductOrderAdapter(getActivity(), dataList);
        listView.setAdapter(mAdapter);
        OkHttpList();
    }

    public void OkHttpList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url = "https://decision-admin.tianqi.cn/Home/work2019/decision_get_demand?uid="+ MyApplication.UID;
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dataList.clear();
                                dataList1.clear();
                                dataList2.clear();
                                dataList3.clear();
                                if (!TextUtils.isEmpty(result)) {
                                    try {
                                        JSONObject obj = new JSONObject(result);
                                        if (!obj.isNull("data")) {
                                            JSONArray array = obj.getJSONArray("data");
                                            for (int i = 0; i < array.length(); i++) {
                                                JSONObject itemObj = array.getJSONObject(i);
                                                DisasterDto dto = new DisasterDto();
                                                if (!itemObj.isNull("department")) {
                                                    dto.title = itemObj.getString("department");
                                                }
                                                if (!itemObj.isNull("usefor")) {
                                                    dto.content = itemObj.getString("usefor");
                                                }
                                                if (!itemObj.isNull("add_time")) {
                                                    dto.time = itemObj.getString("add_time");
                                                }
                                                if (!itemObj.isNull("status")) {
                                                    dto.status = itemObj.getString("status");
                                                }
                                                dataList.add(dto);
                                                dataList1.add(dto);
                                                if (TextUtils.equals(dto.status, "0")) {//待审批
                                                    dataList2.add(dto);
                                                }else {//已审批
                                                    dataList3.add(dto);
                                                }
                                            }
                                            if (mAdapter != null) {
                                                mAdapter.notifyDataSetChanged();
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
