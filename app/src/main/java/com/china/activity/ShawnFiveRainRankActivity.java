package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.china.R;
import com.china.adapter.ShawnFactRankAdapter;
import com.china.common.CONST;
import com.china.dto.StationMonitorDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 5天降水量统计
 */
public class ShawnFiveRainRankActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;
	private TextView tvArea,tvTime;
	private LinearLayout llPrompt;
	private String startTime,endTime,provinceName = "",areaName = "全国";
	private ListView mListView;
	private ShawnFactRankAdapter mAdapter;
	private List<StationMonitorDto> dataList = new ArrayList<>();
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy年MM月dd日HH时", Locale.CHINA);
	private AVLoadingIndicatorView loadingView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_five_rain_rank);
		mContext = this;
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		loadingView = findViewById(R.id.loadingView);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("五天降水量排行");
		tvArea = findViewById(R.id.tvArea);
		tvTime = findViewById(R.id.tvTime);
		llPrompt = findViewById(R.id.llPrompt);
		ImageView ivMapSearch = findViewById(R.id.ivMapSearch);
		ivMapSearch.setOnClickListener(this);
		ivMapSearch.setVisibility(View.VISIBLE);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivShare.setVisibility(View.VISIBLE);

		endTime = sdf3.format(new Date());
		startTime = sdf3.format(new Date().getTime()-1000*60*60*24*5);
		tvArea.setText("全国");
		OkHttpList();
	}

	private void initListView() {
		mListView = findViewById(R.id.listView);
		mAdapter = new ShawnFactRankAdapter(mContext, dataList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				StationMonitorDto dto = dataList.get(arg2);
				Intent intent = new Intent(mContext, ShawnFactRankDetailActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
				intent.putExtra("stationId", dto.stationId);
				intent.putExtra("interface", "newOneDay");
				startActivity(intent);
			}
		});
	}
	
	private void OkHttpList() {
		loadingView.setVisibility(View.VISIBLE);
		final String url = String.format("http://decision-171.tianqi.cn/weather/rgwst/NearStation?starttime=%s&endtime=%s&province=%s&map=all&num=30", startTime,endTime,provinceName);
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
										JSONArray array = new JSONArray(result);
										JSONObject obj5 = array.getJSONObject(5);
										if (!obj5.isNull("rainfallmax")) {
											dataList.clear();
											JSONArray itemArray = obj5.getJSONArray("rainfallmax");
											int length = itemArray.length();
											if (length > 30) {
												length = 30;
											}
											for (int i = 0; i < length; i++) {
												JSONObject itemObj = itemArray.getJSONObject(i);
												StationMonitorDto dto = new StationMonitorDto();
												dto.provinceName = itemObj.getString("province");
												dto.name = itemObj.getString("city");
												dto.precipitation1h = itemObj.getString("rainfall");
												dto.value = dto.precipitation1h+getString(R.string.unit_mm);
												dto.stationId = itemObj.getString("stationid");
												dataList.add(dto);
											}
										}
										JSONObject obj6 = array.getJSONObject(6);
										if (!obj6.isNull("starttime")) {
											startTime = obj6.getString("starttime");
										}
										if (!obj6.isNull("endtime")) {
											endTime = obj6.getString("endtime");
										}
										try {
											tvTime.setText(sdf4.format(sdf3.parse(startTime))+" - "+sdf4.format(sdf3.parse(endTime)));
										} catch (ParseException e) {
											e.printStackTrace();
										}
										if (mAdapter != null) {
											mAdapter.notifyDataSetChanged();
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
								loadingView.setVisibility(View.GONE);
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
		case R.id.ivMapSearch:
			Intent intent = new Intent(mContext, ShawnFiveRainRankSearchActivity.class);
			if (TextUtils.equals(provinceName,"")) {
				intent.putExtra("provinceName", "全国");
			}else {
				intent.putExtra("provinceName", provinceName);
			}
			intent.putExtra("areaName", areaName);
			startActivityForResult(intent, 1001);
			break;
		case R.id.ivShare:
			Bitmap bitmap1 = CommonUtil.captureView(llPrompt);
			Bitmap bitmap2 = CommonUtil.captureView(mListView);
			Bitmap bitmap3 = CommonUtil.mergeBitmap(ShawnFiveRainRankActivity.this, bitmap1, bitmap2, false);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap2);
			Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
			Bitmap bitmap = CommonUtil.mergeBitmap(ShawnFiveRainRankActivity.this, bitmap3, bitmap4, false);
			CommonUtil.clearBitmap(bitmap3);
			CommonUtil.clearBitmap(bitmap4);
			CommonUtil.share(ShawnFiveRainRankActivity.this, bitmap);
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
			case 1001:
				Bundle bundle = data.getExtras();
				provinceName = bundle.getString("provinceName");
                areaName = bundle.getString("areaName");
				tvArea.setText(provinceName);
				if (TextUtils.equals(provinceName, "全国")) {
					provinceName = "";
                    areaName = "全国";
				}
				OkHttpList();
				break;

			default:
				break;
			}
		}
	}

}
