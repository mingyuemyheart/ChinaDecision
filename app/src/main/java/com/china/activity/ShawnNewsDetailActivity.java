package com.china.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.china.R;
import com.china.common.CONST;
import com.china.dto.NewsDto;
import com.china.manager.MyCollectManager;
import com.china.utils.CommonUtil;
import com.tendcloud.tenddata.TCAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 天气资讯详情，底部带有分享、收藏等功能
 */
public class ShawnNewsDetailActivity extends ShawnBaseActivity implements OnClickListener{

	private Context mContext;
	private TextView tvTitle;
	private WebView webView ;
	private WebSettings webSettings;
	private String dataUrl;
	private SwipeRefreshLayout refreshLayout;//下拉刷新布局
	private RelativeLayout reBottom;
	private ImageView ivCollect;
	private boolean isCollected = false;//是否已收藏
	private List<NewsDto> collectList = new ArrayList<>();//存放收藏数据的list
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//解决5.0以上webview截屏长图
			WebView.enableSlowWholeDocumentDraw();
		}
		setContentView(R.layout.shawn_activity_news_detail);
		mContext = this;
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
		refreshLayout.setRefreshing(true);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
				loadUrl();
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
		reBottom = findViewById(R.id.reBottom);
		ImageView ivText = findViewById(R.id.ivText);
		ivText.setOnClickListener(this);
		ImageView ivShareImg = findViewById(R.id.ivShareImg);
		ivShareImg.setOnClickListener(this);
		ivCollect = findViewById(R.id.ivCollect);
		ivCollect.setOnClickListener(this);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}else {
			tvTitle.setText(getString(R.string.detail));
		}
		
		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	/**
	 * 判断是否是已收藏
	 */
	private void isCollect(String url) {
		int size = MyCollectManager.readCollect(ShawnNewsDetailActivity.this, collectList);
		if (size != 0) {
			for (int i = 0; i < collectList.size(); i++) {
				if (TextUtils.equals(url, collectList.get(i).detailUrl)) {
					ivCollect.setImageResource(R.drawable.shawn_icon_collection_blue_press);
					isCollected = true;
					break;
				}
			}
		}
	}
	
	/**
	 * 初始化webview
	 */
	private void initWebView() {
		dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (dataUrl == null) {
			return;
		}
		
		isCollect(dataUrl);
		
		webView = findViewById(R.id.webView);
		webSettings = webView.getSettings();
		
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
				reBottom.setVisibility(View.VISIBLE);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				super.onReceivedSslError(view, handler, error);
				handler.proceed();
			}
		});
	}

	private void loadUrl() {
		if (webView != null && !TextUtils.isEmpty(dataUrl)) {
			Map<String, String> extraHeaders = new HashMap<>();
			extraHeaders.put("Referer", CommonUtil.getRequestHeader());
			webView.loadUrl(dataUrl, extraHeaders);
		}
	}
	
	/**
	 * 刷新我的收藏界面数据
	 */
	private void resultIntentCollect() {
		setResult(RESULT_OK);
		setBackEmit();
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView != null && webView.canGoBack()) {
				webView.goBack();
				return true;
			}else {
				resultIntentCollect();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			resultIntentCollect();
			break;
		case R.id.ivText:
			if (webSettings.getTextSize() == WebSettings.TextSize.NORMAL) {
				webSettings.setTextSize(WebSettings.TextSize.LARGER);
			}else if (webSettings.getTextSize() == WebSettings.TextSize.LARGER) {
				webSettings.setTextSize(WebSettings.TextSize.LARGEST);
			}else if (webSettings.getTextSize() == WebSettings.TextSize.LARGEST) {
				webSettings.setTextSize(WebSettings.TextSize.NORMAL);
			}
			break;
		case R.id.ivCollect:
			if (!isCollected) {
				NewsDto dto = getIntent().getExtras().getParcelable("data");
				if (dto != null) {
					collectList.add(0, dto);
					ivCollect.setImageResource(R.drawable.shawn_icon_collection_blue_press);
					Toast.makeText(this, getString(R.string.collect_success), Toast.LENGTH_SHORT).show();
					isCollected = true;
				}
			}else {
				for (int i = 0; i < collectList.size(); i++) {
					if (TextUtils.equals(dataUrl, collectList.get(i).detailUrl)) {
						collectList.remove(i);
						ivCollect.setImageResource(R.drawable.shawn_icon_collection_blue);
						Toast.makeText(this, getString(R.string.collect_cancel), Toast.LENGTH_SHORT).show();
						isCollected = false;
						break;
					}
				}
			}
			MyCollectManager.clearCollectData(this);
			MyCollectManager.writeCollect(this, collectList);
			break;
		case R.id.ivShareImg:
			Bitmap bitmap1 = CommonUtil.captureWebView(webView);
			Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.shawn_legend_share_portrait);
			Bitmap bitmap = CommonUtil.mergeBitmap(this, bitmap1, bitmap2, false);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap2);
			CommonUtil.share(this, bitmap);
	        break;

		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (tvTitle != null) {
			TCAgent.onPageStart(mContext, tvTitle.getText().toString());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (tvTitle != null) {
			TCAgent.onPageEnd(mContext, tvTitle.getText().toString());
		}
	}
	
}
