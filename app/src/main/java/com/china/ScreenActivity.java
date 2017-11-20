package com.china;

/**
 * 屏屏联动
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.adapter.MainAdapter;
import com.china.adapter.ScreenAdapter;
import com.china.common.CONST;
import com.china.common.ColumnData;
import com.china.common.MyApplication;
import com.china.dto.NewsDto;
import com.china.utils.OkHttpUtil;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScreenActivity extends BaseActivity implements OnClickListener{

	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private GridView gridView = null;
	private ScreenAdapter mAdapter = null;
	private List<ColumnData> dataList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen);
		mContext = this;
		showDialog();
		initWidget();
		initGridView();
		login(CONST.GUIZHOU_LOGIN);
	}

	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("屏屏联动");
	}

	private void initGridView() {
		gridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new ScreenAdapter(mContext, dataList);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = dataList.get(arg2);
				setEmit(dto.columnId);
				Intent intent = null;
//				if (TextUtils.equals(dto.showType, CONST.PRODUCT)) {
//					if (TextUtils.isEmpty(dto.dataUrl)) {//实况监测、天气预报、专业服务、灾情信息、天气会商
//						intent = new Intent(mContext, ProductActivity.class);
//						Bundle bundle = new Bundle();
//						bundle.putParcelable("data", dto);
//						intent.putExtras(bundle);
//						startActivity(intent);
//					}else {//农业气象
//						intent = new Intent(mContext, ProductActivity2.class);
//						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
//						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
//						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
//						startActivity(intent);
//					}
//				}else
					if (TextUtils.equals(dto.showType, CONST.URL)) {
					intent = new Intent(mContext, UrlActivity.class);

					NewsDto data = new NewsDto();
					data.title = dto.name;
					data.detailUrl = dto.dataUrl;
					data.imgUrl = dto.icon;
					intent.putExtra("data", data);

					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.NEWS)) {//天气资讯
					intent = new Intent(mContext, NewsActivity.class);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.LOCAL)) {
					if (TextUtils.equals(dto.id, "-1")) {
						Toast.makeText(mContext, "频道建设中", Toast.LENGTH_SHORT).show();
					}else if (TextUtils.equals(dto.id, "1")) {//灾情信息
						intent = new Intent(mContext, DisasterActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "2")) {//预警信息
						intent = new Intent(mContext, WarningActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "3")) {//决策专报
						intent = new Intent(mContext, DecisionNewsActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "101")) {//站点检测
						intent = new Intent(mContext, StationMonitorActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "102")) {//中国大陆区域彩色云图
						intent = new Intent(mContext, UrlActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, com.china.common.CONST.CLOUD_URL);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "103")) {//台风路径
						intent = new Intent(mContext, TyphoonRouteActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "104")) {//天气统计
						intent = new Intent(mContext, StaticsActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "105")) {//社会化观测
						intent = new Intent(mContext, SocietyObserveActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "106")) {//空气污染
						intent = new Intent(mContext, AirPolutionActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "201")) {//城市天气预报
						intent = new Intent(mContext, CityForecastActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "202")) {//分钟级降水估测
						intent = new Intent(mContext, MinuteFallActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "203")) {//等风来
						intent = new Intent(mContext, WaitWindActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, com.china.common.CONST.WAIT_WIND);
						startActivity(intent);
					}else {
						intent = new Intent(mContext, EmptyActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}
				}else {
						intent = new Intent(mContext, EmptyActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}
			}
		});
	}

	/**
	 * 异步请求
	 */
	private void login(String requestUrl) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("username", "pctest");
		builder.add("password", "hfcx123456");
		builder.add("appid", CONST.APPID);
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(requestUrl).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				if (result != null) {
					try {
						JSONObject object = new JSONObject(result);
						if (object != null) {
							if (!object.isNull("status")) {
								int status  = object.getInt("status");
								if (status == 1) {//成功
									JSONArray array = new JSONArray(object.getString("column"));
									dataList.clear();
									for (int i = 0; i < array.length(); i++) {
										JSONObject obj = array.getJSONObject(i);
										ColumnData data = new ColumnData();
										if (!obj.isNull("id")) {
											data.columnId = obj.getString("id");
										}
										if (!obj.isNull("localviewid")) {
											data.id = obj.getString("localviewid");
										}
										if (!obj.isNull("name")) {
											data.name = obj.getString("name");
										}
										if (!obj.isNull("icon")) {
											data.icon = obj.getString("icon");
										}
										if (!obj.isNull("desc")) {
											data.desc = obj.getString("desc");
										}
										if (!obj.isNull("showtype")) {
											data.showType = obj.getString("showtype");
										}
										if (!obj.isNull("dataurl")) {
											data.dataUrl = obj.getString("dataurl");
										}
										if (!obj.isNull("child")) {
											JSONArray childArray = new JSONArray(obj.getString("child"));
											for (int j = 0; j < childArray.length(); j++) {
												JSONObject childObj = childArray.getJSONObject(j);
												ColumnData dto = new ColumnData();
												if (!childObj.isNull("id")) {
													dto.columnId = childObj.getString("id");
												}
												if (!childObj.isNull("localviewid")) {
													dto.id = childObj.getString("localviewid");
												}
												if (!childObj.isNull("name")) {
													dto.name = childObj.getString("name");
												}
												if (!childObj.isNull("desc")) {
													dto.desc = childObj.getString("desc");
												}
												if (!childObj.isNull("icon")) {
													dto.icon = childObj.getString("icon");
												}
												if (!childObj.isNull("showtype")) {
													dto.showType = childObj.getString("showtype");
												}
												if (!childObj.isNull("dataurl")) {
													dto.dataUrl = childObj.getString("dataurl");
												}

												if (!childObj.isNull("child")) {
													JSONArray childArray2 = new JSONArray(childObj.getString("child"));
													for (int m = 0; m < childArray2.length(); m++) {
														JSONObject childObj2 = childArray2.getJSONObject(m);
														ColumnData d = new ColumnData();
														if (!childObj2.isNull("id")) {
															d.columnId = childObj2.getString("id");
														}
														if (!childObj2.isNull("localviewid")) {
															d.id = childObj2.getString("localviewid");
														}
														if (!childObj2.isNull("name")) {
															d.name = childObj2.getString("name");
														}
														if (!childObj2.isNull("desc")) {
															d.desc = childObj2.getString("desc");
														}
														if (!childObj2.isNull("icon")) {
															d.icon = childObj2.getString("icon");
														}
														if (!childObj2.isNull("showtype")) {
															d.showType = childObj2.getString("showtype");
														}
														if (!childObj2.isNull("dataurl")) {
															d.dataUrl = childObj2.getString("dataurl");
														}
														dto.child.add(d);
													}
												}

												data.child.add(dto);
											}
										}
										dataList.add(data);
									}

									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											cancelDialog();
											if (dataList.size() > 0 && mAdapter != null) {
												mAdapter.notifyDataSetChanged();
											}
										}
									});
								}else {
									//失败

								}
							}
						}
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

	/**
	 * 六要素点击发送指令
	 * @param id 模块id
	 */
	private void setEmit(String id) {
		try {
			if (socket == null) {
				socket = MyApplication.getSocket();
			}
			if (socket.connected()) {
				JSONObject obj = new JSONObject();
				obj.put("computerInfo", MyApplication.computerInfo);
				obj.put("commond", id);
				socket.emit("selectItem", obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
