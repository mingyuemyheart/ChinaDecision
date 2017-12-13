package com.china.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.china.utils.CustomHttpClient;
import com.china.view.RefreshLayout;
import com.china.view.RefreshLayout.OnRefreshListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 天气资讯详情
 */

public class WeatherInfoDetailActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private WebView webView = null;
	private WebSettings webSettings = null;
	private String url = null;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private RelativeLayout reBottom = null;
	private ImageView ivText = null;
	private ImageView ivCollect = null;
	private ImageView ivShareImg = null;
	private boolean isCollected = false;//是否已收藏
	private List<NewsDto> collectList = new ArrayList<>();//存放收藏数据的list
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_info_detail);
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
					Map<String, String> extraHeaders = new HashMap<>();
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
		reBottom = (RelativeLayout) findViewById(R.id.reBottom);
		ivText = (ImageView) findViewById(R.id.ivText);
		ivText.setOnClickListener(this);
		ivShareImg = (ImageView) findViewById(R.id.ivShareImg);
		ivShareImg.setOnClickListener(this);
		ivCollect = (ImageView) findViewById(R.id.ivCollect);
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
		int size = MyCollectManager.readCollect(WeatherInfoDetailActivity.this, collectList);
		if (size != 0) {
			for (int i = 0; i < collectList.size(); i++) {
				if (TextUtils.equals(url, collectList.get(i).detailUrl)) {
					ivCollect.setImageResource(R.drawable.iv_url_collect_selected);
					isCollected = true;
					break;
				}
			}
		}
	}
	
	/**
	 * 初始化webview
	 */
	@SuppressLint("SetJavaScriptEnabled") 
	private void initWebView() {
		url = getIntent().getStringExtra(CONST.WEB_URL);
		if (url == null) {
			return;
		}
		
		isCollect(url);
		
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
		Map<String, String> extraHeaders = new HashMap<>();
		extraHeaders.put("Referer", CustomHttpClient.getRequestHeader());
		webView.loadUrl(url, extraHeaders);
		
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (title != null) {
					tvTitle.setText(title);
				}
			}
		});
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String itemUrl) {
				url = itemUrl;
				Map<String, String> extraHeaders = new HashMap<>();
				extraHeaders.put("Referer", CustomHttpClient.getRequestHeader());
				webView.loadUrl(url, extraHeaders);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				refreshLayout.setRefreshing(false);
				reBottom.setVisibility(View.VISIBLE);
			}
		});
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

	@SuppressWarnings("deprecation")
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
			if (isCollected == false) {
				NewsDto dto = getIntent().getExtras().getParcelable("data");
				if (dto != null) {
					collectList.add(0, dto);
					ivCollect.setImageResource(R.drawable.iv_url_collect_selected);
					Toast.makeText(WeatherInfoDetailActivity.this, getString(R.string.collect_success), Toast.LENGTH_SHORT).show();
					isCollected = true;
				}
			}else {
				for (int i = 0; i < collectList.size(); i++) {
					if (TextUtils.equals(url, collectList.get(i).detailUrl)) {
						collectList.remove(i);
						ivCollect.setImageResource(R.drawable.iv_url_collect);
						Toast.makeText(WeatherInfoDetailActivity.this, getString(R.string.collect_cancel), Toast.LENGTH_SHORT).show();
						isCollected = false;
						break;
					}
				}
			}
			MyCollectManager.clearCollectData(WeatherInfoDetailActivity.this);
			MyCollectManager.writeCollect(WeatherInfoDetailActivity.this, collectList);
			break;
		case R.id.ivShareImg:
			Bitmap bitmap1 = CommonUtil.captureWebView(webView);
			Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.iv_share_bottom);
//			Bitmap bitmap2 = BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable-hdpi/iv_share_bottom.png"));
			Bitmap bitmap = CommonUtil.mergeBitmap(WeatherInfoDetailActivity.this, bitmap1, bitmap2, false);
			CommonUtil.clearBitmap(bitmap1);
			CommonUtil.clearBitmap(bitmap2);
			CommonUtil.share(WeatherInfoDetailActivity.this, bitmap);
	        break;

		default:
			break;
		}
	}
	
}
