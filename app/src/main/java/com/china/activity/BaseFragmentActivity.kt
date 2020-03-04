package com.china.activity

import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View

open class BaseFragmentActivity : FragmentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (Build.VERSION.SDK_INT >= 23) {
			window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
		}
	}

}
