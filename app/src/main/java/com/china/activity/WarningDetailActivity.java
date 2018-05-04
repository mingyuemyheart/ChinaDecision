package com.china.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.china.R;
import com.china.common.CONST;
import com.china.common.MyApplication;
import com.china.dto.WarningDto;
import com.china.manager.DBManager;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.view.RefreshLayout;
import com.china.view.RefreshLayout.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 预警详情
 */

public class WarningDetailActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView imageView = null;//预警图标
	private TextView tvName = null;//预警名称
	private TextView tvTime = null;//预警时间
	private TextView tvIntro = null;//预警介绍
	private TextView tvGuide = null;//防御指南
	private String url = "http://decision.tianqi.cn/alarm12379/content2/";//详情页面url
	private WarningDto data = null;
	private ScrollView scrollView = null;
	private ImageView ivShare = null;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warning_detail);
		mContext = this;
		initRefreshLayout();
		initWidget();
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.PULL_FROM_START);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
	
	private void refresh() {
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			try {
				OkHttpWarningDetail(url+data.html);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		columnId = getIntent().getStringExtra(CONST.COLUMN_ID);//栏目id

		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.warning_detail));
		imageView = (ImageView) findViewById(R.id.imageView);
		tvName = (TextView) findViewById(R.id.tvName);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvIntro = (TextView) findViewById(R.id.tvIntro);
		tvGuide = (TextView) findViewById(R.id.tvGuide);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		
		refresh();
	}
	
	/**
	 * 初始化数据库
	 */
	private void initDBManager() {
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = null;
		cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + data.type+data.color + "\"",null);
		String content = null;
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			content = cursor.getString(cursor.getColumnIndex("WarningGuide"));
		}
		if (!TextUtils.isEmpty(content)) {
			tvGuide.setText(getString(R.string.warning_guide)+content);
			tvGuide.setVisibility(View.VISIBLE);
		}else {
			tvGuide.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 获取预警详情
	 */
	private void OkHttpWarningDetail(final String url) {
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
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("sendTime")) {
												tvTime.setText(object.getString("sendTime"));
											}

											if (!object.isNull("description")) {
												tvIntro.setText(object.getString("description"));
											}

											String name = object.getString("headline");
											if (!TextUtils.isEmpty(name)) {
												tvName.setText(name.replace(getString(R.string.publish), getString(R.string.publish)+"\n"));
											}

											Bitmap bitmap = null;
											if (object.getString("severityCode").equals(CONST.blue[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.blue[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
												}
											}else if (object.getString("severityCode").equals(CONST.yellow[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.yellow[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
												}
											}else if (object.getString("severityCode").equals(CONST.orange[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.orange[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
												}
											}else if (object.getString("severityCode").equals(CONST.red[0])) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+object.getString("eventType")+CONST.red[1]+CONST.imageSuffix);
												if (bitmap == null) {
													bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
												}
											}
											if (bitmap == null) {
												bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.imageSuffix);
											}
											imageView.setImageBitmap(bitmap);

											initDBManager();
											scrollView.setVisibility(View.VISIBLE);
											ivShare.setVisibility(View.VISIBLE);
											refreshLayout.setRefreshing(false);
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setNormalEmit("10", "");
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			setNormalEmit("10", "");
			finish();
			break;
		case R.id.ivShare:
			Bitmap bitmap1 = CommonUtil.captureScrollView(scrollView);
			Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
//			Bitmap bitmap2 = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable-hdpi/iv_share_bottom.png"));
			Bitmap bitmap = CommonUtil.mergeBitmap(WarningDetailActivity.this, bitmap1, bitmap2, false);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap2);
			CommonUtil.share(WarningDetailActivity.this, bitmap);
			break;

		default:
			break;
		}
	}

	private String columnId = "";//栏目id

	/**
	 * 普通发送指令，
	 * @param id
	 * @param sid
	 */
	private void setNormalEmit(String id, String sid) {
		try {
			if (socket == null) {
				socket = MyApplication.getSocket();
			}
			if (socket != null && socket.connected()) {
				JSONObject obj = new JSONObject();
				obj.put("computerInfo", MyApplication.computerInfo);
				JSONObject commond = new JSONObject();
				commond.put("id", id);
				JSONObject message = new JSONObject();
				if (!TextUtils.isEmpty(sid)) {
					message.put("sid", sid);
				}
				commond.put("message", message);
				obj.put("commond", commond);
				socket.emit(columnId, obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
