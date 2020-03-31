package com.china.activity

import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.webkit.*
import android.webkit.WebSettings.LayoutAlgorithm
import com.china.R
import com.china.common.CONST
import com.china.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.shawn_layout_title.*
import java.util.*

/**
 * 应用推荐、周报统计
 */
class WebviewActivity : ShawnBaseActivity(), OnClickListener {
	
	private var dataUrl : String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_webview)
		initWidget()
		initWebView()
	}

	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		llBack.setOnClickListener(this)
		ivShare.setOnClickListener(this)
		
		val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
		if (!TextUtils.isEmpty(title)) {
			tvTitle.text = title
		}
	}
	
	/**
	 * 初始化webview
	 */
	private fun initWebView() {
		dataUrl = intent.getStringExtra(CONST.WEB_URL)
		if (TextUtils.isEmpty(dataUrl)) {
			return
		}
		
		val webSettings = webView.settings
		//支持javascript
		webSettings.javaScriptEnabled = true
		webSettings.javaScriptCanOpenWindowsAutomatically = true
		webSettings.domStorageEnabled = true
		webSettings.setGeolocationEnabled(true)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
		}
		// 设置可以支持缩放 
		webSettings.setSupportZoom(true)
		// 设置出现缩放工具 
		webSettings.builtInZoomControls = true
		webSettings.displayZoomControls = false
		//扩大比例的缩放
		webSettings.useWideViewPort = true
		//自适应屏幕
		webSettings.layoutAlgorithm = LayoutAlgorithm.SINGLE_COLUMN
		webSettings.loadWithOverviewMode = true
		loadUrl()

		webView.webChromeClient = object : WebChromeClient() {
			override fun onReceivedTitle(view: WebView?, title: String?) {
				super.onReceivedTitle(view, title)
			}

			override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
				callback!!.invoke(origin, true, false)
			}
		}

		webView.webViewClient = object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView?, itemUrl: String?): Boolean {
				dataUrl = itemUrl
				loadUrl()
				return true
			}

			override fun onPageFinished(view: WebView?, url: String?) {
				super.onPageFinished(view, url)
				if (TextUtils.equals(tvTitle.text, "用户协议") || TextUtils.equals(tvTitle.text, "隐私政策")) {
					ivShare.visibility = View.GONE
				} else {
					ivShare.visibility = View.VISIBLE
				}
			}

			override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
				super.onReceivedSslError(view, handler, error)
				handler!!.proceed()
			}
		}

	}

	private fun loadUrl() {
		val extraHeaders : HashMap<String, String> = HashMap()
		extraHeaders["Referer"] = CommonUtil.getRequestHeader()
		webView.loadUrl(dataUrl, extraHeaders)
	}

	override fun onPause() {
		super.onPause()
		if (webView != null) {
			webView.reload()
		}
	}

	override fun onClick(v: View?) {
		when(v!!.id) {
			R.id.llBack -> finish()
			R.id.ivShare -> {
				if (TextUtils.equals(dataUrl, CONST.COUNTURL)) {
					CommonUtil.share(this, getString(R.string.app_name), tvTitle.text.toString(), null, CONST.COUNTURL)
				}else if (TextUtils.equals(dataUrl, CONST.RECOMMENDURL)) {
					CommonUtil.share(this, getString(R.string.app_name), tvTitle.text.toString(), null, "http://testdecision.tianqi.cn/describe/index.html")
				}
			}
		}
	}

}
