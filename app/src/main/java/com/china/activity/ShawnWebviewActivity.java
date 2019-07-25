package com.china.activity;

import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.common.CONST;
import com.china.utils.CommonUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用推荐、周报统计
 */
public class ShawnWebviewActivity extends ShawnBaseActivity implements OnClickListener{
	
	private TextView tvTitle;
	private WebView webView;
	private String dataUrl;
	private ImageView ivShare;
	private SwipeRefreshLayout refreshLayout;//下拉刷新布局

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_webview);
		initRefreshLayout();
		initWidget();
		initWebView();
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (webView != null && !TextUtils.isEmpty(dataUrl)) {
					loadUrl();
				}
			}
		});
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		ivShare = findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}
	}
	
	/**
	 * 初始化webview
	 */
	private void initWebView() {
		dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (TextUtils.isEmpty(dataUrl)) {
			return;
		}
		
		webView = findViewById(R.id.webView);
		WebSettings webSettings = webView.getSettings();
		
		//支持javascript
		webSettings.setJavaScriptEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
		// 设置可以支持缩放 
		webSettings.setSupportZoom(true); 
		// 设置出现缩放工具 
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
		//扩大比例的缩放
		webSettings.setUseWideViewPort(true);
		//自适应屏幕
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setLoadWithOverviewMode(true);
//		webView.loadUrl(url);
		loadUrl();
		
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
				dataUrl = itemUrl;
				loadUrl();
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				refreshLayout.setRefreshing(false);
				ivShare.setVisibility(View.VISIBLE);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				super.onReceivedSslError(view, handler, error);
				handler.proceed();
			}
		});
	}

	private void loadUrl() {
		Map<String, String> extraHeaders = new HashMap<>();
		extraHeaders.put("Referer", CommonUtil.getRequestHeader());
		webView.loadUrl(dataUrl, extraHeaders);
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
			if (TextUtils.equals(dataUrl, CONST.COUNTURL)) {
				CommonUtil.share(ShawnWebviewActivity.this, getString(R.string.app_name), tvTitle.getText().toString(), null, CONST.COUNTURL);
			}else if (TextUtils.equals(dataUrl, CONST.RECOMMENDURL)) {
				CommonUtil.share(ShawnWebviewActivity.this, getString(R.string.app_name), tvTitle.getText().toString(), null, CONST.RECOMMENDURL);
			}
	        break;

		default:
			break;
		}
	}
	
}
