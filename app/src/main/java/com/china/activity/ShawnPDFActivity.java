package com.china.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.R;
import com.china.common.CONST;
import com.china.common.MyApplication;
import com.china.utils.AuthorityUtil;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.china.view.MyRatingBar;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * PDF列表界面
 */
public class ShawnPDFActivity extends ShawnBaseActivity implements OnClickListener, MyRatingBar.OnStarChangeListener {
	
	private Context mContext;
	private PDFView pdfView;
	private TextView tvPercent,tvFeedback;
	private String title = "春运气象服务专报";
	private String dataUrl = "";
	private MyRatingBar ratingBar;
	private Dialog remarkDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_pdfview);
		mContext = this;
		checkAuthority();
	}

	private void init() {
		initWidget();
		initPDFView();
		dialogRemark();
	}

	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		ImageView ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		tvPercent = findViewById(R.id.tvPercent);
		ratingBar = findViewById(R.id.ratingBar);
		ratingBar.setOnStarChangeListener(this);
		tvFeedback = findViewById(R.id.tvFeedback);
		tvFeedback.setOnClickListener(this);

		if (getIntent().hasExtra(CONST.ACTIVITY_NAME)) {
			title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
			if (!TextUtils.isEmpty(title)) {
				tvTitle.setText(title);
				if (TextUtils.equals(title, "春运气象服务专报")) {
					ivShare.setVisibility(View.VISIBLE);
				}
			}
		}
	}
	
	// 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }
 
    // 完整的判断中文汉字和符号
    private String isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (char c : ch) {
            if (isChinese(c)) {
            	try {
					strName = strName.replace(c+"", URLEncoder.encode(c+"", "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
            }
        }
		return strName;
    }
	
	private void initPDFView() {
		pdfView = findViewById(R.id.pdfView);
		if (!getIntent().hasExtra(CONST.WEB_URL)) {
			return;
		}
		dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (TextUtils.isEmpty(dataUrl)) {
			return;
		}else {
			dataUrl = isChinese(dataUrl);
		}
		OkHttpFile(dataUrl);
	}

	private void OkHttpFile(final String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
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
						InputStream is = null;
						FileOutputStream fos = null;
						try {
							is = response.body().byteStream();//获取输入流
							float total = response.body().contentLength();//获取文件大小
							if(is != null){
								File files = new File(Environment.getExternalStorageDirectory()+"/ChinaWeather");
								if (!files.exists()) {
									files.mkdirs();
								}
								String filePath = files.getAbsolutePath()+"/"+"1.pdf";
								fos = new FileOutputStream(filePath);
								byte[] buf = new byte[1024];
								int ch = -1;
								int process = 0;
								while ((ch = is.read(buf)) != -1) {
									fos.write(buf, 0, ch);
									process += ch;

									int percent = (int) Math.floor((process / total * 100));
									Log.e("percent", process+"--"+total+"--"+percent);
									Message msg = handler.obtainMessage(1001);
									msg.what = 1001;
									msg.obj = filePath;
									msg.arg1 = percent;
									handler.sendMessage(msg);
								}
							}
							fos.flush();
							fos.close();// 下载完成

						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (is != null) {
								is.close();
							}
							if (fos != null) {
								fos.close();
							}
						}

					}
				});
			}
		}).start();
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1001) {
				if (tvPercent == null || pdfView == null) {
					return;
				}
				int percent = msg.arg1;
				tvPercent.setText(percent+getString(R.string.unit_percent));
				if (percent >= 100) {
					tvPercent.setVisibility(View.GONE);
					String filePath = msg.obj+"";
					if (!TextUtils.isEmpty(filePath)) {
						File file = new File(msg.obj+"");
						if (file.exists()) {
							pdfView.fromFile(file)
									.defaultPage(0)
									.scrollHandle(new DefaultScrollHandle(ShawnPDFActivity.this))
									.onPageChange(new OnPageChangeListener() {
										@Override
										public void onPageChanged(int page, int pageCount) {
											if (!TextUtils.isEmpty(title)) {
												if (title.startsWith("两办刊物") || title.startsWith("灾害预警")) {
													ratingBar.setVisibility(View.GONE);
													tvFeedback.setVisibility(View.GONE);
												}else {
													ratingBar.setVisibility(View.VISIBLE);
													tvFeedback.setVisibility(View.VISIBLE);
												}
											}else {
												ratingBar.setVisibility(View.VISIBLE);
												tvFeedback.setVisibility(View.VISIBLE);
											}
										}
									})
									.load();
						}
					}
				}
			}
		}
	};

	/**
	 * 评价对话框
	 */
	private void dialogRemark() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.shawn_dialog_remark, null);
		TextView tvNegtive = view.findViewById(R.id.tvNegtive);
		TextView tvPositive = view.findViewById(R.id.tvPositive);

		remarkDialog = new Dialog(mContext, R.style.CustomProgressDialog);
		remarkDialog.setContentView(view);

		tvNegtive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				remarkDialog.dismiss();
				finish();
			}
		});
		tvPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				remarkDialog.dismiss();
				Intent intent = new Intent(mContext, ShawnServiceFeedbackActivity.class);
				intent.putExtra(CONST.WEB_URL, dataUrl);
				startActivityForResult(intent, 1001);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!TextUtils.isEmpty(title)) {
				if (title.startsWith("两办刊物") || title.startsWith("灾害预警")) {
					return true;
				}else {
					if (remarkDialog != null && !remarkDialog.isShowing()) {
						remarkDialog.show();
						return false;
					}
				}
			}else {
				if (remarkDialog != null && !remarkDialog.isShowing()) {
					remarkDialog.show();
					return false;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				if (!TextUtils.isEmpty(title)) {
					if (title.startsWith("两办刊物") || title.startsWith("灾害预警")) {
						finish();
					}else {
						if (remarkDialog != null && !remarkDialog.isShowing()) {
							remarkDialog.show();
							return;
						}
					}
				}else {
					if (remarkDialog != null && !remarkDialog.isShowing()) {
						remarkDialog.show();
						return;
					}
				}
				break;
			case R.id.tvFeedback:
				Intent intent = new Intent(this, ShawnServiceFeedbackActivity.class);
				intent.putExtra(CONST.WEB_URL, dataUrl);
				startActivityForResult(intent, 1001);
				break;
			case R.id.ivShare:
				String time = "";
				if (getIntent().hasExtra(CONST.DATA_TIME)) {
					time = getIntent().getStringExtra(CONST.DATA_TIME);
				}
				String imgUrl = "";
				if (getIntent().hasExtra(CONST.IMG_URL)) {
					imgUrl = getIntent().getStringExtra(CONST.IMG_URL);
				}
				String url = "";
				if (!TextUtils.isEmpty(dataUrl) && dataUrl.endsWith(".pdf")) {
					url = dataUrl.replace(".pdf", ".doc");
				}
				CommonUtil.share(this, title, time, imgUrl, url);
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
					if (ratingBar != null) {
						ratingBar.setVisibility(View.GONE);
					}
					if (tvFeedback != null) {
						tvFeedback.setVisibility(View.GONE);
					}
					finish();
					break;
			}
		}
	}

	//需要申请的所有权限
	private String[] allPermissions = new String[] {
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
				ActivityCompat.requestPermissions(ShawnPDFActivity.this, permissions, AuthorityUtil.AUTHOR_LOCATION);
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
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用存储权限，是否前往设置？");
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(ShawnPDFActivity.this, permission)) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用存储权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}

	@Override
	public void OnStarChanged(float selectedNumber, int position) {
		OkHttpSubmit(selectedNumber);
	}

	/**
	 * 反馈
	 */
	private void OkHttpSubmit(float selectedNumber) {
		final String url = "http://decision-admin.tianqi.cn/home/Evaluate/doinsert";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("uid", MyApplication.UID);
		builder.add("url", dataUrl);
		builder.add("whole", selectedNumber+"");
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
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("msg")) {
											String msg = obj.getString("msg");
											if (!TextUtils.isEmpty(msg)) {
												Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
												new Handler().postDelayed(new Runnable() {
													@Override
													public void run() {
														if (ratingBar != null) {
															ratingBar.setVisibility(View.GONE);
														}
														if (tvFeedback != null) {
															tvFeedback.setVisibility(View.GONE);
														}
													}
												}, 1500);
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
