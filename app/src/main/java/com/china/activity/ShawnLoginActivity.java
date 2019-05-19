package com.china.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.china.R;
import com.china.common.CONST;
import com.china.common.ColumnData;
import com.china.common.MyApplication;
import com.china.dto.NewsDto;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.tendcloud.tenddata.TCAgent;
import com.tendcloud.tenddata.TDAccount;
import com.wang.avi.AVLoadingIndicatorView;

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

/**
 * 登录界面
 */
public class ShawnLoginActivity extends ShawnBaseActivity implements OnClickListener {
	
	private Context mContext = null;
	private EditText etUserName,etPwd;
	private String lat = "0", lng = "0", addr = "";
	private List<ColumnData> dataList = new ArrayList<>();
	private List<NewsDto> pdfList = new ArrayList<>();//pdf文档类
	private AVLoadingIndicatorView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_login);
		mContext = this;
		initWidget();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		loadingView = findViewById(R.id.loadingView);
		etUserName = findViewById(R.id.etUserName);
		etPwd = findViewById(R.id.etPwd);
		TextView tvLogin = findViewById(R.id.tvLogin);
		tvLogin.setOnClickListener(this);
		TextView tvForgetPwd = findViewById(R.id.tvForgetPwd);
		tvForgetPwd.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		TextView tvCommonLogin = findViewById(R.id.tvCommonLogin);
		tvCommonLogin.setOnClickListener(this);
		tvCommonLogin.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		tvCommonLogin.getPaint().setAntiAlias(true);

		startLocation();
	}

	/**
	 * 开始定位
	 */
	private void startLocation() {
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();//初始化定位参数
		AMapLocationClient mLocationClient = new AMapLocationClient(mContext);//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(new AMapLocationListener() {
			@Override
			public void onLocationChanged(AMapLocation aMapLocation) {
				if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
					lat = String.valueOf(aMapLocation.getLatitude());
					lng = String.valueOf(aMapLocation.getLongitude());
					addr = aMapLocation.getAddress();
				}
			}
		});
        mLocationClient.startLocation();//启动定位
	}

	private void doLogin() {
		if (checkInfo()) {
			OkHttpLogin();
		}
	}
	
	/**
	 * 验证用户信息
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, getResources().getString(R.string.input_username), Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, getResources().getString(R.string.input_password), Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * 登录
	 */
	private void OkHttpLogin() {
		loadingView.setVisibility(View.VISIBLE);
		final String url = "http://decision-admin.tianqi.cn/home/Work/login";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("username", etUserName.getText().toString());
		builder.add("password", etPwd.getText().toString());
		builder.add("appid", CONST.APPID);
		builder.add("device_id", CommonUtil.getUniqueId(mContext));
		builder.add("platform", "android");
		builder.add("os_version", android.os.Build.VERSION.RELEASE);
		builder.add("software_version", CommonUtil.getVersion(mContext));
		builder.add("mobile_type", android.os.Build.MODEL);
		builder.add("address", addr);
		builder.add("lat", lat);
		builder.add("lng", lng);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
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
										JSONObject object = new JSONObject(result);
										if (!object.isNull("status")) {
											int status  = object.getInt("status");
											if (status == 1) {//成功
												JSONArray array = object.getJSONArray("column");
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
														JSONArray childArray = obj.getJSONArray("child");
														for (int j = 0; j < childArray.length(); j++) {
															JSONObject childObj = childArray.getJSONObject(j);
															ColumnData dto = new ColumnData();
															dto.groupColumnId = data.columnId;
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
																JSONArray childArray2 = childObj.getJSONArray("child");
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

												if (!object.isNull("appinfo")) {
													JSONObject obj = object.getJSONObject("appinfo");
													if (!obj.isNull("counturl")) {
														CONST.COUNTURL = obj.getString("counturl");
													}
													if (!obj.isNull("recommendurl")) {
														CONST.RECOMMENDURL = obj.getString("recommendurl");
													}
													if (!obj.isNull("news")) {
														pdfList.clear();
														JSONArray newsArray = obj.getJSONArray("news");
														for (int i = 0; i < newsArray.length(); i++) {
															JSONObject itemObj = newsArray.getJSONObject(i);
															NewsDto dto = new NewsDto();
															if (!itemObj.isNull("name")) {
																dto.title = itemObj.getString("name");
															}
															if (!itemObj.isNull("url")) {
																dto.detailUrl = itemObj.getString("url");
															}
															if (!itemObj.isNull("time")) {
																dto.time = itemObj.getString("time");
															}
															if (!itemObj.isNull("flagImg")) {
																dto.imgUrl = itemObj.getString("flagImg");
															}
															if (!itemObj.isNull("header")) {
																dto.header = itemObj.getString("header");
															}
															pdfList.add(dto);
														}
													}
												}

												if (!object.isNull("info")) {
													JSONObject obj = new JSONObject(object.getString("info"));
														MyApplication.UID = obj.getString("id");
														MyApplication.USERGROUP = obj.getString("usergroup");
														MyApplication.USERNAME = etUserName.getText().toString();
														MyApplication.PASSWORD = etPwd.getText().toString();
														MyApplication.saveUserInfo(mContext);

														Intent intent = new Intent(mContext, ShawnMainActivity.class);
														Bundle bundle = new Bundle();
														bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) dataList);
														bundle.putParcelableArrayList("pdfList", (ArrayList<? extends Parcelable>) pdfList);
														intent.putExtras(bundle);
														startActivity(intent);
														finish();

														//统计登录事件
														TCAgent.onLogin(MyApplication.UID, TDAccount.AccountType.REGISTERED, MyApplication.USERNAME);
												}
											}else {
												//失败
												if (!object.isNull("msg")) {
													String msg = object.getString("msg");
													if (msg != null) {
														Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
													}
												}
											}
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
		case R.id.tvLogin:
			doLogin();
			break;
		case R.id.tvCommonLogin:
			etUserName.setText("中国气象");
			etPwd.setText("121");
			etUserName.setSelection(etUserName.getText().toString().length());
			etPwd.setSelection(etPwd.getText().toString().length());
			doLogin();
			break;

		default:
			break;
		}
	}
	
}
