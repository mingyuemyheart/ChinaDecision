package com.china.fragment;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.china.R;
import com.china.common.CONST;
import com.china.dto.WarningDto;
import com.china.manager.DBManager;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;

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
public class ShawnWarningDetailFragment extends Fragment{
	
	private ImageView imageView;//预警图标
	private TextView tvName,tvTime,tvIntro,tvGuide;
	private ScrollView scrollView;
	private WarningDto data;
	private SwipeRefreshLayout refreshLayout;//下拉刷新布局
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.shawn_fragment_warning_detail, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		initWidget(view);
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = view.findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
	
	private void refresh() {
		data = getArguments().getParcelable("data");
		if (data != null && !TextUtils.isEmpty(data.html)) {
			OkHttpWarningDetail(data.html);
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		scrollView = view.findViewById(R.id.scrollView);
		imageView = view.findViewById(R.id.imageView);
		tvName = view.findViewById(R.id.tvName);
		tvTime = view.findViewById(R.id.tvTime);
		tvIntro = view.findViewById(R.id.tvIntro);
		tvGuide = view.findViewById(R.id.tvGuide);
		
		refresh();
	}
	
	/**
	 * 获取预警详情
	 */
	private void OkHttpWarningDetail(final String html) {
		final String url = "https://decision-admin.tianqi.cn/Home/work2019/getDetailWarn/identifier/"+html;
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
						if (getActivity() == null || !isAdded()) {
							return;
						}
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (!object.isNull("sendTime")) {
											tvTime.setText("发布时间："+object.getString("sendTime"));
										}

										if (!object.isNull("description")) {
											tvIntro.setText(object.getString("description"));
										}

										if (!object.isNull("headline")) {
											String headline = object.getString("headline");
											if (!TextUtils.isEmpty(headline)) {
												tvName.setText(headline.replace("发布", "发布\n"));
											}
										}

										String color = object.getString("severityCode");
										String type = object.getString("eventType");
										Bitmap bitmap = null;
										if (color.equals(CONST.blue[0])) {
											bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+type+CONST.blue[1]+CONST.imageSuffix);
										}else if (color.equals(CONST.yellow[0])) {
											bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+type+CONST.yellow[1]+CONST.imageSuffix);
										}else if (color.equals(CONST.orange[0])) {
											bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+type+CONST.orange[1]+CONST.imageSuffix);
										}else if (color.equals(CONST.red[0])) {
											bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+type+CONST.red[1]+CONST.imageSuffix);
										}
										if (bitmap == null) {
											bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.imageSuffix);
										}
										imageView.setImageBitmap(bitmap);

										initDBManager();
										scrollView.setVisibility(View.VISIBLE);
										refreshLayout.setRefreshing(false);
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

	/**
	 * 初始化数据库
	 */
	private void initDBManager() {
		DBManager dbManager = new DBManager(getActivity());
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + data.type+data.color + "\"",null);
		String content = null;
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			content = cursor.getString(cursor.getColumnIndex("WarningGuide"));
		}
		cursor.close();
		if (!TextUtils.isEmpty(content)) {
			tvGuide.setText("预警指南：\n"+content);
			tvGuide.setVisibility(View.VISIBLE);
		}else {
			tvGuide.setVisibility(View.GONE);
		}
	}

}
