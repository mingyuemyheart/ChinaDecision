package com.china.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnProductOrderAdapter;
import com.china.common.MyApplication;
import com.china.dto.DisasterDto;
import com.china.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 产品定制-提供资料
 */
public class ShawnProductOrderRightFragment extends Fragment implements View.OnClickListener {

    private ImageView iv1,iv2;
    private boolean timeDesc = false, nameDesc = false;
    private ShawnProductOrderAdapter mAdapter;
    private List<DisasterDto> dataList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shawn_fragment_product_order_right, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initWidget(view);
        initListView(view);
    }

    private void initWidget(View view) {
        LinearLayout ll1 = view.findViewById(R.id.ll1);
        ll1.setOnClickListener(this);
        LinearLayout ll2 = view.findViewById(R.id.ll2);
        ll2.setOnClickListener(this);
        iv1 = view.findViewById(R.id.iv1);
        iv2 = view.findViewById(R.id.iv2);
    }

    private void initListView(View view) {
        ListView listView = view.findViewById(R.id.listView);
        mAdapter = new ShawnProductOrderAdapter(getActivity(), dataList);
        listView.setAdapter(mAdapter);
        OkHttpList();
    }

    private void OkHttpList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String url = "https://decision-admin.tianqi.cn/Home/work2019/decision_get_demand_doc?uid="+ MyApplication.UID;
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
                                                dataList.add(dto);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll1:
                timeDesc = !timeDesc;
                if (timeDesc) {
                    iv1.setImageResource(R.drawable.shawn_icon_rank_top);
                    Collections.sort(dataList, new Comparator<DisasterDto>() {
                        @Override
                        public int compare(DisasterDto o1, DisasterDto o2) {
                            return o1.time.compareTo(o2.time);
                        }
                    });
                }else {
                    iv1.setImageResource(R.drawable.shawn_icon_rank_bottom);
                    Collections.sort(dataList, new Comparator<DisasterDto>() {
                        @Override
                        public int compare(DisasterDto o1, DisasterDto o2) {
                            return o2.time.compareTo(o1.time);
                        }
                    });
                }
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.ll2:
                nameDesc = !nameDesc;
                if (nameDesc) {
                    iv2.setImageResource(R.drawable.shawn_icon_triangle_bottom);
                    Collections.sort(dataList, new Comparator<DisasterDto>() {
                        @Override
                        public int compare(DisasterDto o1, DisasterDto o2) {
                            return o1.title.compareTo(o2.title);
                        }
                    });
                }else {
                    iv2.setImageResource(R.drawable.shawn_icon_triangle_top);
                    Collections.sort(dataList, new Comparator<DisasterDto>() {
                        @Override
                        public int compare(DisasterDto o1, DisasterDto o2) {
                            return o2.title.compareTo(o1.title);
                        }
                    });
                }
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

}
