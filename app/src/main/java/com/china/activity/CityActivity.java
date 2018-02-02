package com.china.activity;

/**
 * 城市选择
 */

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.CityAdapter;
import com.china.adapter.CityFragmentAdapter;
import com.china.common.CONST;
import com.china.dto.CityDto;
import com.china.manager.DBManager;
import com.china.utils.CommonUtil;
import com.tendcloud.tenddata.TCAgent;

import java.util.ArrayList;
import java.util.List;

public class CityActivity extends BaseActivity implements OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;//返回按钮
    private TextView tvTitle = null;
    private EditText etSearch = null;
    private TextView tvNational = null;//全国热门
    private LinearLayout llNation = null;

    //搜索城市后的结果列表
    private ListView mListView = null;
    private CityAdapter cityAdapter = null;
    private List<CityDto> cityList = new ArrayList<>();

    //全国热门
    private GridView nGridView = null;
    private CityFragmentAdapter nAdapter = null;
    private List<CityDto> nList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        mContext = this;
        initWidget();
        initListView();
        initNGridView();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(watcher);
        tvNational = (TextView) findViewById(R.id.tvNational);
        tvNational.setOnClickListener(this);
        llNation = (LinearLayout) findViewById(R.id.llNation);
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);

        String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
        if (title != null) {
            tvTitle.setText(title);
        }

        String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
        CommonUtil.submitClickCount(columnId, title);
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
            if (arg0.toString() == null) {
                return;
            }

            cityList.clear();
            if (arg0.toString().trim().equals("")) {
                if (mListView != null) {
                    mListView.setVisibility(View.GONE);
                }
                llNation.setVisibility(View.VISIBLE);
                if (nGridView != null) {
                    nGridView.setVisibility(View.VISIBLE);
                }
            } else {
                if (mListView != null) {
                    mListView.setVisibility(View.VISIBLE);
                }
                llNation.setVisibility(View.GONE);
                if (nGridView != null) {
                    nGridView.setVisibility(View.GONE);
                }
                getCityInfo(arg0.toString().trim());
            }

        }
    };

    /**
     * 迁移到天气详情界面
     */
    private void intentWeatherDetail(CityDto data) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", data);
        Intent intent;
        if (getIntent().hasExtra("reserveCity")) {
            intent = new Intent();
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        }else {
            intent = new Intent(mContext, ForecastActivity.class);
            intent.putExtra("cityName", data.areaName);
            intent.putExtra("cityId", data.cityId);
            startActivity(intent);
        }
    }

    /**
     * 初始化listview
     */
    private void initListView() {
        mListView = (ListView) findViewById(R.id.listView);
        cityAdapter = new CityAdapter(mContext, cityList);
        mListView.setAdapter(cityAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                intentWeatherDetail(cityList.get(arg2));
            }
        });
    }

    /**
     * 初始化全国热门
     */
    private void initNGridView() {
        nList.clear();
        String[] stations = getResources().getStringArray(R.array.nation_hotCity);
        for (int i = 0; i < stations.length; i++) {
            String[] value = stations[i].split(",");
            CityDto dto = new CityDto();
            dto.lng = Double.valueOf(value[3]);
            dto.lat = Double.valueOf(value[2]);
            dto.cityId = value[0];
            dto.areaName = value[1];
            nList.add(dto);
        }

        nGridView = (GridView) findViewById(R.id.nGridView);
        nAdapter = new CityFragmentAdapter(mContext, nList);
        nGridView.setAdapter(nAdapter);
        nGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                intentWeatherDetail(nList.get(arg2));
            }
        });
    }

    /**
     * 获取城市信息
     */
    private void getCityInfo(String keyword) {
        cityList.clear();
        DBManager dbManager = new DBManager(mContext);
        dbManager.openDateBase();
        dbManager.closeDatabase();
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
        Cursor cursor = database.rawQuery("select * from "+DBManager.TABLE_NAME3+" where pro like "+"\"%"+keyword+"%\""+" or city like "+"\"%"+keyword+"%\""+" or dis like "+"\"%"+keyword+"%\"",null);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            CityDto dto = new CityDto();
            dto.provinceName = cursor.getString(cursor.getColumnIndex("pro"));
            dto.cityName = cursor.getString(cursor.getColumnIndex("city"));
            dto.areaName = cursor.getString(cursor.getColumnIndex("dis"));
            dto.cityId = cursor.getString(cursor.getColumnIndex("cid"));
            dto.warningId = cursor.getString(cursor.getColumnIndex("wid"));
            cityList.add(dto);
        }
        if (cityList.size() > 0 && cityAdapter != null) {
            cityAdapter.notifyDataSetChanged();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        TCAgent.onPageStart(mContext, tvTitle.getText().toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        TCAgent.onPageEnd(mContext, tvTitle.getText().toString());
    }

}
