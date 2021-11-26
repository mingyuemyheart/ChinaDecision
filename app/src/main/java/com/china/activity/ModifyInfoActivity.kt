package com.china.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import com.china.R
import com.china.common.MyApplication
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_modify_info.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * 修改用户信息
 */
class ModifyInfoActivity : BaseActivity(), OnClickListener {

    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_info)
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack!!.setOnClickListener(this)
        etContent!!.addTextChangedListener(watcher)
        tvControl!!.setOnClickListener(this)
        tvControl!!.visibility = View.VISIBLE
        tvControl!!.setTextColor(resources.getColor(R.color.blue))
        tvControl!!.text = "保存"
        ivClear!!.setOnClickListener(this)

        if (intent.hasExtra("title")) {
            title = intent.getStringExtra("title")
            if (title != null) {
                tvTitle!!.text = title
            }
        }
        if (intent.hasExtra("content")) {
            val content = intent.getStringExtra("content")
            if (content != null) {
                etContent!!.setText(content)
                etContent!!.setSelection(content.length)
            }
        }
    }

    /**
     * 监听etcontent内容变化
     */
    private val watcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun afterTextChanged(arg0: Editable) {
            if (!TextUtils.isEmpty(etContent!!.text.toString())) {
                ivClear!!.visibility = View.VISIBLE
            } else {
                ivClear!!.visibility = View.GONE
            }
        }
    }

    /**
     * 修改用户信息
     */
    private fun okHttpModify() {
        val builder = FormBody.Builder()
        builder.add("id", MyApplication.UID)
        when {
            TextUtils.equals(title, "姓名") -> {
                builder.add("name", etContent!!.text.toString().trim { it <= ' ' })
            }
            TextUtils.equals(title, "手机号") -> {
                builder.add("mobile", etContent!!.text.toString().trim { it <= ' ' })
            }
            TextUtils.equals(title, "单位名称") -> {
                builder.add("department", etContent!!.text.toString().trim { it <= ' ' })
            }
        }
        val body: RequestBody = builder.build()
        Thread {
            val url = "http://decision-admin.tianqi.cn/home/work2019/user_update"
            OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val `object` = JSONObject(result)
                                if (!`object`.isNull("status")) {
                                    val status = `object`.getString("status")
                                    if (TextUtils.equals(status, "1")) { //成功
                                        if (!`object`.isNull("user")) {
                                            val obj = `object`.getJSONObject("user")
                                            if (!obj.isNull("id")) {
                                                MyApplication.UID = obj.getString("id")
                                            }
                                            if (!obj.isNull("usergroup")) {
                                                MyApplication.USERGROUP = obj.getString("usergroup")
                                            }
                                            if (!obj.isNull("wx_openid")) {
                                                MyApplication.WXACCOUNT = obj.getString("wx_openid")
                                            }
                                            if (!obj.isNull("name")) {
                                                MyApplication.NICKNAME = obj.getString("name")
                                            }
                                            if (!obj.isNull("mobile")) {
                                                MyApplication.MOBILE = obj.getString("mobile")
                                            }
                                            if (!obj.isNull("department")) {
                                                MyApplication.UNIT = obj.getString("department")
                                            }
                                            if (!obj.isNull("headpic")) {
                                                MyApplication.PORTRAIT = obj.getString("headpic")
                                            }
                                            MyApplication.saveUserInfo(this@ModifyInfoActivity)
                                            setResult(RESULT_OK)
                                            finish()
                                        }
                                    } else { //失败
                                        if (!`object`.isNull("msg")) {
                                            Toast.makeText(this@ModifyInfoActivity, `object`.getString("msg"), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }.start()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.ivClear -> etContent!!.setText("")
            R.id.tvControl -> {
                if (!TextUtils.isEmpty(etContent!!.text.toString().trim { it <= ' ' })) {
                    okHttpModify()
                } else {
                    if (title != null) {
                        Toast.makeText(this, "请输入$title", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
	
}
