package com.china;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.common.CONST;
import com.china.utils.CommonUtil;
import com.china.utils.CustomHttpClient;
import com.china.view.RefreshLayout;
import com.china.view.RefreshLayout.OnRefreshListener;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用推荐
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class Url2Activity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private WebView webView = null;
	private WebSettings webSettings = null;
	private String url = null;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private ImageView ivShare = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_url2);
		initRefreshLayout();
		initWidget();
		initWebView();
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
				if (webView != null && !TextUtils.isEmpty(url)) {
					//添加请求头
					Map<String, String> extraHeaders = new HashMap<String, String>();
					extraHeaders.put("Referer", CustomHttpClient.getRequestHeader());
					webView.loadUrl(url, extraHeaders);
				}
			}
		});
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}
	}
	
	/**
	 * 初始化webview
	 */
	@SuppressLint("SetJavaScriptEnabled") 
	private void initWebView() {
		url = getIntent().getStringExtra(CONST.WEB_URL);
		if (TextUtils.isEmpty(url)) {
			return;
		}
		
		webView = (WebView) findViewById(R.id.webView);
		webSettings = webView.getSettings();
		
		//支持javascript
		webSettings.setJavaScriptEnabled(true); 
		// 设置可以支持缩放 
		webSettings.setSupportZoom(true); 
		// 设置出现缩放工具 
		webSettings.setBuiltInZoomControls(true);
		//扩大比例的缩放
		webSettings.setUseWideViewPort(true);
		//自适应屏幕
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setLoadWithOverviewMode(true);
//		webView.loadUrl(url);
		
		//添加请求头
		Map<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders.put("Referer", CustomHttpClient.getRequestHeader());
		webView.loadUrl(url, extraHeaders);
		
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
//				if (title != null) {
//					tvTitle.setText(title);
//				}
			}
		});
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String itemUrl) {
				url = itemUrl;
				Map<String, String> extraHeaders = new HashMap<String, String>();
				extraHeaders.put("Referer", CustomHttpClient.getRequestHeader());
				webView.loadUrl(url, extraHeaders);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				refreshLayout.setRefreshing(false);
				ivShare.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (webView != null) {
			webView.reload();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivShare:
			if (TextUtils.equals(url, CONST.COUNTURL)) {
//				Bitmap bitmap1 = CommonUtil.captureMyView(webView);
////				Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
//				Bitmap bitmap2 = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable-hdpi/iv_share_bottom.png"));
//				Bitmap bitmap = CommonUtil.mergeBitmap(Url2Activity.this, bitmap1, bitmap2, false);
//				CommonUtil.clearBitmap(bitmap1);
//				CommonUtil.clearBitmap(bitmap2);
//				CommonUtil.share(Url2Activity.this, bitmap);
				CommonUtil.share(Url2Activity.this, getString(R.string.app_name), tvTitle.getText().toString(), null, CONST.COUNTURL);
			}else if (TextUtils.equals(url, CONST.RECOMMENDURL)) {
				CommonUtil.share(Url2Activity.this, getString(R.string.app_name), tvTitle.getText().toString(), null, CONST.RECOMMENDURL);
			}
	        break;

		default:
			break;
		}
	}
	
}
