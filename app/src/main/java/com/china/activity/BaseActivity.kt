package com.china.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import com.china.common.CONST
import com.china.utils.CommonUtil
import com.china.view.LoadingDialog

open class BaseActivity : Activity() {

    private var mDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 23) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        CommonUtil.submitClickCount(intent.getStringExtra(CONST.COLUMN_ID), intent.getStringExtra(CONST.ACTIVITY_NAME))
    }

    protected fun showDialog() {
        if (mDialog == null) {
            mDialog = LoadingDialog(this)
        }
        mDialog!!.show()
    }

    protected fun cancelDialog() {
        if (mDialog != null) {
            mDialog!!.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelDialog() //解决activity已经销毁，而还在调用dialog
    }

}
