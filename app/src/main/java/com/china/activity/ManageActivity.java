package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ManageAdapter;
import com.china.common.ColumnData;
import com.china.common.MyApplication;
import com.china.stickygridheaders.StickyGridHeadersGridView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 模块管理
 */
public class ManageActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private List<ColumnData> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        mContext = this;
        initWidget();
        initGridView();
    }

    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("模块管理");
        TextView tvControl = findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("保存");
        tvControl.setVisibility(View.VISIBLE);
    }

    /**
     * 初始化工务段
     */
    private int section = 1;
    private void initGridView() {
        if (!getIntent().hasExtra("dataList")) {
            return;
        }
        List<ColumnData> list = getIntent().getExtras().getParcelableArrayList("dataList");
        if (list == null || list.size() <= 0) {
            return;
        }

        String columnIds = MyApplication.getColumnIds(this);
        dataList.clear();
        for (int i = 0; i < list.size(); i++) {
            ColumnData dto = list.get(i);
            if (dto.child.size() <= 0) {
                dto.headerName = dto.name;
                if (!TextUtils.isEmpty(columnIds)) {
                    if (!TextUtils.isEmpty(dto.columnId)) {
                        if (columnIds.contains(dto.columnId)) {//已经有保存的栏目
                            dto.isSelected = true;
                        }else {
                            dto.isSelected = false;
                        }
                    }else {
                        dto.isSelected = true;
                    }
                }else {
                    dto.isSelected = true;
                }
                dataList.add(dto);
            }else {
                for (int j = 0; j < dto.child.size(); j++) {
                    ColumnData data = dto.child.get(j);
                    data.headerName = dto.name;
                    data.icon = dto.icon;
                    if (!TextUtils.isEmpty(columnIds)) {
                        if (!TextUtils.isEmpty(data.columnId)) {
                            if (columnIds.contains(data.columnId)) {//已经有保存的栏目
                                data.isSelected = true;
                            }else {
                                data.isSelected = false;
                            }
                        }else {
                            data.isSelected = true;
                        }
                    }else {
                        data.isSelected = true;
                    }
                    dataList.add(data);
                }
            }
        }

        Map<String, Integer> sectionMap = new LinkedHashMap<>();
        for (int i = 0; i < dataList.size(); i++) {
            ColumnData dto = dataList.get(i);
            if (!sectionMap.containsKey(dto.headerName)) {
                dto.section = section;
                sectionMap.put(dto.headerName, section);
                section++;
            }else {
                dto.section = sectionMap.get(dto.headerName);
            }
            dataList.set(i, dto);
        }

        StickyGridHeadersGridView pGridView = findViewById(R.id.pGridView);
        final ManageAdapter pAdapter = new ManageAdapter(mContext, dataList);
        pGridView.setAdapter(pAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvControl:
                String columnIds = "";
                for (int i = 0; i < dataList.size(); i++) {
                    ColumnData dto = dataList.get(i);
                    if (dto.isSelected) {
                        columnIds = columnIds+dto.columnId+","+dto.groupColumnId+",";
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("columnIds", columnIds);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

}
