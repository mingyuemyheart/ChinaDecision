package com.china.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.R;
import com.china.adapter.ShawnReserveCityAdapter;
import com.china.dto.CityDto;
import com.china.dto.WarningDto;
import com.china.manager.DBManager;
import com.china.swipemenulistview.SwipeMenu;
import com.china.swipemenulistview.SwipeMenuCreator;
import com.china.swipemenulistview.SwipeMenuItem;
import com.china.swipemenulistview.SwipeMenuListView;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.utils.WeatherUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 城市预定
 */
public class ShawnReserveCityActivity extends BaseActivity implements View.OnClickListener {
    
    private Context mContext;
    private ShawnReserveCityAdapter mAdapter;
    private List<CityDto> cityList = new ArrayList<>();
    private TextView tvPrompt;
    private List<WarningDto> warningList = new ArrayList<>();//预警列表
    private AVLoadingIndicatorView loadingView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_reserve_city);
        mContext = this;
        checkAuthority();
    }

    private void init() {
        initWidget();
        initListView();
    }
    
    private void initWidget() {
        loadingView = findViewById(R.id.loadingView);
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("城市订阅");
        tvPrompt = findViewById(R.id.tvPrompt);
        TextView tvControl = findViewById(R.id.tvControl);
        tvControl.setText("添加城市");
        tvControl.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tvControl.getPaint().setAntiAlias(true);
        tvControl.setOnClickListener(this);
        tvControl.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpWarning();
            }
        }).start();
    }

    /**
     * 获取预警信息
     */
    private void OkHttpWarning() {
        final String url = "http://decision-admin.tianqi.cn/Home/extra/getwarns?order=0";
        OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                queryWeatherInfos();
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
                        if (!object.isNull("data")) {
                            warningList.clear();
                            JSONArray jsonArray = object.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONArray tempArray = jsonArray.getJSONArray(i);
                                WarningDto dto = new WarningDto();
                                dto.html = tempArray.getString(1);
                                String[] array = dto.html.split("-");
                                String item0 = array[0];
                                String item1 = array[1];
                                String item2 = array[2];

                                dto.item0 = item0;
                                dto.provinceId = item0.substring(0, 2);
                                dto.type = item2.substring(0, 5);
                                dto.color = item2.substring(5, 7);
                                dto.time = item1;
                                dto.lng = tempArray.getDouble(2);
                                dto.lat = tempArray.getDouble(3);
                                dto.name = tempArray.getString(0);

                                if (!dto.name.contains("解除")) {
                                    warningList.add(dto);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                queryWeatherInfos();
            }
        });
    }

    /**
     * 查询天气信息
     */
    private void queryWeatherInfos() {
        String cityName = getIntent().getStringExtra("cityName");
        String cityId = getIntent().getStringExtra("cityId");
        String warningId = queryWarningIdByCityId(cityId);

        SharedPreferences sp = getSharedPreferences("RESERVE_CITY", Context.MODE_PRIVATE);
        String cityInfo = sp.getString("cityInfo", "");
        final List<String> cityIds = new ArrayList<>();
        cityIds.add(cityId);
        cityList.clear();
        CityDto dto = new CityDto();
        dto.cityId = cityId;
        dto.cityName = cityName;
        dto.warningId = warningId;
        cityList.add(dto);
        if (!TextUtils.isEmpty(cityInfo)) {
            String[] array = cityInfo.split(";");
            for (String value : array) {
                String[] itemArray = value.split(",");
                dto = new CityDto();
                dto.cityId = itemArray[0];
                dto.cityName = itemArray[1];
                dto.warningId = itemArray[2];
                cityList.add(dto);
                cityIds.add(itemArray[0]);
            }
        }

        getWeatherInfos(cityIds);
    }

    /**
     * 获取预警id
     */
    private String queryWarningIdByCityId(String cityId) {
        DBManager dbManager = new DBManager(mContext);
        dbManager.openDateBase();
        dbManager.closeDatabase();
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
        Cursor cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME3 + " where cid = " + "\"" + cityId + "\"",null);
        String warningId = null;
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            warningId = cursor.getString(cursor.getColumnIndex("wid"));
        }
        cursor.close();
        return warningId;
    }

    /**
     * 初始化listview
     */
    private void initListView() {
        SwipeMenuListView mListView = findViewById(R.id.listView);
        mAdapter = new ShawnReserveCityAdapter(mContext, cityList);
        mListView.setAdapter(mAdapter);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                switch (menu.getViewType()) {
                    case ShawnReserveCityAdapter.notShowRemove:
                        break;
                    case ShawnReserveCityAdapter.showRemove:
                        createMenu1(menu);
                        break;
                }
            }
            private void createMenu1(SwipeMenu menu) {
                SwipeMenuItem item1 = new SwipeMenuItem(mContext);
                item1.setBackground(new ColorDrawable(Color.RED));
                item1.setWidth((int) CommonUtil.dip2px(mContext, 70));
                item1.setTitle("删除");
                item1.setTitleColor(getResources().getColor(R.color.white));
                item1.setTitleSize(14);
                menu.addMenuItem(item1);
            }
        };
        mListView.setMenuCreator(creator);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (menu.getViewType()) {
                    case ShawnReserveCityAdapter.notShowRemove:
                        break;
                    case ShawnReserveCityAdapter.showRemove:
                        cityList.remove(position);
                        if (cityList.size() > 1) {
                            tvPrompt.setVisibility(View.VISIBLE);
                        }else {
                            tvPrompt.setVisibility(View.GONE);
                        }
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                }
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                CityDto data = cityList.get(arg2);
                Intent intent = new Intent(mContext, ShawnForecastActivity.class);
                intent.putExtra("cityName", data.cityName);
                intent.putExtra("cityId", data.cityId);
                startActivity(intent);
            }
        });
    }

    /**
     * 获取定位城市和保存本地城市信息
     */
    private void getWeatherInfos(final List<String> cityIds) {
        WeatherAPI.getWeathers2(mContext, cityIds, Constants.Language.ZH_CN, new AsyncResponseHandler() {
            @Override
            public void onComplete(final List<Weather> contentList) {
                super.onComplete(contentList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < contentList.size(); i++) {
                            Weather content = contentList.get(i);
                            if (content != null) {
                                try {
                                    JSONObject obj = new JSONObject(content.toString());

                                    //实况信息
                                    if (!obj.isNull("l")) {
                                        CityDto dto = cityList.get(i);

                                        JSONObject l = obj.getJSONObject("l");
                                        if (!l.isNull("l5")) {
                                            String weatherCode = WeatherUtil.lastValue(l.getString("l5"));
                                            if (!TextUtils.isEmpty(weatherCode)) {
                                                dto.highPheCode = Integer.parseInt(weatherCode);
                                            }
                                        }

                                        if (!l.isNull("l1")) {
                                            String factTemp = WeatherUtil.lastValue(l.getString("l1"));
                                            if (!TextUtils.isEmpty(factTemp)) {
                                                dto.highTemp = factTemp;
                                            }
                                        }

                                        List<WarningDto> list = new ArrayList<>();
                                        for (WarningDto data : warningList) {
                                            if (TextUtils.equals(data.item0, dto.warningId)) {
                                                list.add(data);
                                            }
                                        }
                                        dto.warningList.addAll(list);

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                        if (cityList.size() > 1) {
                            tvPrompt.setVisibility(View.VISIBLE);
                        }else {
                            tvPrompt.setVisibility(View.GONE);
                        }
                        loadingView.setVisibility(View.GONE);
                    }
                });
            }
            @Override
            public void onError(Throwable error, String content) {
                super.onError(error, content);
            }
        });
    }

    /**
     * 获取天气信息
     */
    private void getWeatherInfo(final CityDto dto) {
        loadingView.setVisibility(View.VISIBLE);
        WeatherAPI.getWeather2(mContext, dto.cityId, Constants.Language.ZH_CN, new AsyncResponseHandler() {
            @Override
            public void onComplete(final Weather content) {
                super.onComplete(content);
                if (content != null) {
                    try {
                        JSONObject obj = new JSONObject(content.toString());

                        //实况信息
                        if (!obj.isNull("l")) {
                            JSONObject l = obj.getJSONObject("l");
                            if (!l.isNull("l5")) {
                                String weatherCode = WeatherUtil.lastValue(l.getString("l5"));
                                if (!TextUtils.isEmpty(weatherCode)) {
                                    dto.highPheCode = Integer.parseInt(weatherCode);
                                }
                            }

                            if (!l.isNull("l1")) {
                                String factTemp = WeatherUtil.lastValue(l.getString("l1"));
                                if (!TextUtils.isEmpty(factTemp)) {
                                    dto.highTemp = factTemp;
                                }
                            }

                            List<WarningDto> list = new ArrayList<>();
                            for (WarningDto data : warningList) {
                                if (TextUtils.equals(data.item0, dto.warningId)) {
                                    list.add(data);
                                }
                            }
                            dto.warningList.addAll(list);

                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                loadingView.setVisibility(View.GONE);
            }
            @Override
            public void onError(Throwable error, String content) {
                super.onError(error, content);
            }
        });
    }

    /**
     * 保存订阅城市信息
     */
    private void saveCityInfo() {
        //保存预定城市信息
        String cityInfo = "";
        for (int i = 1; i < cityList.size(); i++) {//从1开始是为了过滤掉定位城市
            cityInfo += (cityList.get(i).cityId+","+cityList.get(i).cityName+","+cityList.get(i).warningId+";");
        }
        SharedPreferences sharedPreferences = getSharedPreferences("RESERVE_CITY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cityInfo", cityInfo);
        editor.apply();
        Log.e("cityInfo", cityInfo);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        saveCityInfo();
        finish();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                saveCityInfo();
                finish();
                break;
            case R.id.tvControl:
                if (cityList.size() >= 10) {
                    Toast.makeText(mContext, "最多只能关注10个城市", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(mContext, ShawnCityActivity.class);
                intent.putExtra("reserveCity", "reserveCity");
                startActivityForResult(intent, 1001);
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
                        CityDto dto = data.getExtras().getParcelable("data");
                        if (dto != null) {
                            boolean isDuplicate = false;//是否重复
                            for (int i = 0; i < cityList.size(); i++) {
                                if (TextUtils.equals(cityList.get(i).cityId, dto.cityId)) {//防止重复添加同一个城市
                                    isDuplicate = true;
                                    break;
                                }
                            }
                            if (isDuplicate) {
                                Toast.makeText(mContext, "该城市已关注", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            dto.warningId = queryWarningIdByCityId(dto.cityId);
                            if (!TextUtils.isEmpty(dto.cityName)) {
                                if (dto.cityName.contains(dto.areaName)) {
                                    dto.cityName = dto.areaName;
                                }else {
                                    dto.cityName = dto.areaName+" "+"("+dto.cityName+")";
                                }
                            }else {
                                dto.cityName = dto.areaName;
                            }
                            cityList.add(dto);

                            getWeatherInfo(dto);
                        }
                    }
                    break;
            }
        }
    }

    //需要申请的所有权限
    private String[] allPermissions = new String[] {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //拒绝的权限集合
    private List<String> deniedList = new ArrayList<>();
    /**
     * 申请定位权限
     */
    private void checkAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            init();
        }else {
            deniedList.clear();
            for (String permission : allPermissions) {
                if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(permission);
                }
            }
            if (deniedList.isEmpty()) {//所有权限都授予
                init();
            }else {
                String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
                ActivityCompat.requestPermissions(ShawnReserveCityActivity.this, permissions, AuthorityUtil.AUTHOR_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AuthorityUtil.AUTHOR_LOCATION:
                if (grantResults.length > 0) {
                    boolean isAllGranted = true;//是否全部授权
                    for (int gResult : grantResults) {
                        if (gResult != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false;
                            break;
                        }
                    }
                    if (isAllGranted) {//所有权限都授予
                        init();
                    }else {//只要有一个没有授权，就提示进入设置界面设置
                        AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的存储权限，是否前往设置？");
                    }
                }else {
                    for (String permission : permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(ShawnReserveCityActivity.this, permission)) {
                            AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的存储权限，是否前往设置？");
                            break;
                        }
                    }
                }
                break;
        }
    }

}
