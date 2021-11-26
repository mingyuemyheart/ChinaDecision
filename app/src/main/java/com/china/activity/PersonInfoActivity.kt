package com.china.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.china.R
import com.china.common.MyApplication
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_person_info.*
import kotlinx.android.synthetic.main.layout_title.*

/**
 * 个人信息
 */
class PersonInfoActivity : BaseActivity(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_info)
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        tvTitle!!.text = "个人信息"
        llBack!!.setOnClickListener(this)
        llNickName!!.setOnClickListener(this)
        llMobile!!.setOnClickListener(this)
        llUnit!!.setOnClickListener(this)

        if (!TextUtils.isEmpty(MyApplication.PORTRAIT)) {
            Picasso.get().load(MyApplication.PORTRAIT).into(ivPortrait)
        }
        if (!TextUtils.isEmpty(MyApplication.USERNAME)) {
            tvUserName!!.text = MyApplication.USERNAME
        }
        if (!TextUtils.isEmpty(MyApplication.WXACCOUNT)) {
            tvWx!!.text = MyApplication.WXACCOUNT
        }
        if (!TextUtils.isEmpty(MyApplication.NICKNAME)) {
            tvNickName!!.text = MyApplication.NICKNAME
        }
        if (!TextUtils.isEmpty(MyApplication.MOBILE)) {
            tvMobile!!.text = MyApplication.MOBILE
        }
        if (!TextUtils.isEmpty(MyApplication.UNIT)) {
            tvUnit!!.text = MyApplication.UNIT
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.llNickName -> {
                val intent = Intent(this, ModifyInfoActivity::class.java)
                intent.putExtra("title", "姓名")
                intent.putExtra("content", MyApplication.NICKNAME)
                startActivityForResult(intent, 1001)
            }
            R.id.llMobile -> {
                intent = Intent(this, ModifyInfoActivity::class.java)
                intent.putExtra("title", "手机号")
                intent.putExtra("content", MyApplication.MOBILE)
                startActivityForResult(intent, 1002)
            }
            R.id.llUnit -> {
                intent = Intent(this, ModifyInfoActivity::class.java)
                intent.putExtra("title", "单位名称")
                intent.putExtra("content", MyApplication.UNIT)
                startActivityForResult(intent, 1003)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1001 -> if (!TextUtils.isEmpty(MyApplication.NICKNAME)) {
                    tvNickName!!.text = MyApplication.NICKNAME
                }
                1002 -> if (!TextUtils.isEmpty(MyApplication.MOBILE)) {
                    tvMobile!!.text = MyApplication.MOBILE
                }
                1003 -> if (!TextUtils.isEmpty(MyApplication.UNIT)) {
                    tvUnit!!.text = MyApplication.UNIT
                }
            }
        }
    }
	
}
