package com.china.activity;

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.china.R
import com.china.common.CONST
import com.china.common.MyApplication
import com.china.utils.OkHttpUtil
import com.china.view.MyRatingBar.OnStarChangeListener
import kotlinx.android.synthetic.main.activity_service_feedback.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * 服务反馈
 */
class ServiceFeedbackActivity : BaseActivity(), View.OnClickListener {

    private var selectedNumber1 = 0f
    private var selectedNumber2 = 0f
    private var selectedNumber3 = 0f
    private var selectedNumber4 = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_feedback)
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "服务反馈"
        tvSubmit.setOnClickListener(this)
        ratingBar1.onStarChangeListener = OnStarChangeListener { selectedNumber, position -> selectedNumber1 = selectedNumber }
        ratingBar2.onStarChangeListener = OnStarChangeListener { selectedNumber, position -> selectedNumber2 = selectedNumber }
        ratingBar3.onStarChangeListener = OnStarChangeListener { selectedNumber, position -> selectedNumber3 = selectedNumber }
        ratingBar4.onStarChangeListener = OnStarChangeListener { selectedNumber, position -> selectedNumber4 = selectedNumber }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.tvSubmit -> okHttpSubmit()
        }
    }

    /**
     * 反馈
     */
    private fun okHttpSubmit() {
        if (intent.hasExtra(CONST.WEB_URL)) {
            val dataUrl = intent.getStringExtra(CONST.WEB_URL)
            if (!TextUtils.isEmpty(dataUrl)) {
                val url = "http://decision-admin.tianqi.cn/home/Evaluate/doinsert"
                val builder = FormBody.Builder()
                builder.add("uid", MyApplication.UID)
                builder.add("url", dataUrl)
                builder.add("whole", selectedNumber1.toString())
                builder.add("service", selectedNumber2.toString())
                builder.add("timely", selectedNumber3.toString())
                builder.add("practicality", selectedNumber4.toString())
                builder.add("content", etContent!!.text.toString())
                val body: RequestBody = builder.build()
                Thread(Runnable {
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
                                        val obj = JSONObject(result)
                                        if (!obj.isNull("msg")) {
                                            val msg = obj.getString("msg")
                                            if (!TextUtils.isEmpty(msg)) {
                                                Toast.makeText(this@ServiceFeedbackActivity, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        if (!obj.isNull("status")) {
                                            if (TextUtils.equals(obj.getString("status"), "1")) { //成功
                                                setResult(RESULT_OK)
                                                finish()
                                            }
                                        }
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    })
                }).start()
            }
        }
    }

}
