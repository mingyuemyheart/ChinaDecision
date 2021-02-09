package com.china.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.china.R
import com.china.common.CONST
import com.china.common.MyApplication
import com.china.utils.AuthorityUtil
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import com.china.view.MyRatingBar
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.android.synthetic.main.activity_pdfview.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.URLEncoder
import java.util.*
import kotlin.math.floor

/**
 * PDF预览界面
 */
class PDFActivity : BaseActivity(), OnClickListener, MyRatingBar.OnStarChangeListener {

    private var dataUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfview)
        checkAuthority()
    }

    private fun init() {
        initWidget()
        initPDFView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivShare.setOnClickListener(this)
        ivShare.visibility = View.VISIBLE
        ratingBar.onStarChangeListener = this
        tvFeedback.setOnClickListener(this)

        if (intent.hasExtra(CONST.ACTIVITY_NAME)) {
            title = intent.getStringExtra(CONST.ACTIVITY_NAME)
            if (!TextUtils.isEmpty(title)) {
                tvTitle.text = title
            }
        }
    }

    // 根据Unicode编码完美的判断中文汉字和符号
    private fun isChinese(c: Char): Boolean {
        val ub = Character.UnicodeBlock.of(c)
        return ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B || ub === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS || ub === Character.UnicodeBlock.GENERAL_PUNCTUATION
    }

    // 完整的判断中文汉字和符号
    private fun isChinese(name: String): String {
        var strName = name
        val ch = strName.toCharArray()
        for (c in ch) {
            if (isChinese(c)) {
                try {
                    strName = strName.replace(c.toString() + "", URLEncoder.encode(c.toString() + "", "UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        }
        return strName
    }

    private fun initPDFView() {
        if (!intent.hasExtra(CONST.WEB_URL)) {
            return
        }
        dataUrl = intent.getStringExtra(CONST.WEB_URL)
        dataUrl = if (TextUtils.isEmpty(dataUrl)) {
            return
        } else {
            isChinese(dataUrl)
        }
        okHttpFile(dataUrl)
    }

    private fun okHttpFile(url: String) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    var `is`: InputStream? = null
                    var fos: FileOutputStream? = null
                    try {
                        `is` = response.body!!.byteStream() //获取输入流
                        val total = response.body!!.contentLength().toFloat() //获取文件大小
                        if (`is` != null) {
                            val files = File("${getExternalFilesDir(null)}/ChinaWeather")
                            if (!files.exists()) {
                                files.mkdirs()
                            }
                            val filePath = "${files.absolutePath}/1.pdf"
                            fos = FileOutputStream(filePath)
                            val buf = ByteArray(1024)
                            var ch = -1
                            var process = 0
                            while (`is`.read(buf).also { ch = it } != -1) {
                                fos.write(buf, 0, ch)
                                process += ch
                                val percent = floor((process / total * 100).toDouble()).toInt()
                                val msg = handler.obtainMessage(1001)
                                msg.what = 1001
                                msg.obj = filePath
                                msg.arg1 = percent
                                handler.sendMessage(msg)
                            }
                        }
                        fos!!.flush()
                        fos.close() // 下载完成
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        `is`?.close()
                        fos?.close()
                    }
                }
            })
        }).start()
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 1001) {
                if (tvPercent == null || pdfView == null) {
                    return
                }
                val percent = msg.arg1
                tvPercent.text = "$percent${getString(R.string.unit_percent)}"
                if (percent >= 100) {
                    tvPercent!!.visibility = View.GONE
                    val filePath = msg.obj.toString() + ""
                    if (!TextUtils.isEmpty(filePath)) {
                        val file = File(msg.obj.toString() + "")
                        if (file.exists()) {
                            pdfView!!.fromFile(file)
                                    .defaultPage(0)
                                    .scrollHandle(DefaultScrollHandle(this@PDFActivity))
                                    .onPageChange { page, pageCount ->
                                        ratingBar!!.visibility = View.VISIBLE
                                        tvFeedback.visibility = View.VISIBLE
                                    }
                                    .load()
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.tvFeedback -> {
                val intent = Intent(this, ServiceFeedbackActivity::class.java)
                intent.putExtra(CONST.WEB_URL, dataUrl)
                startActivityForResult(intent, 1001)
            }
            R.id.ivShare -> {
                var time: String? = ""
                if (intent.hasExtra(CONST.DATA_TIME)) {
                    time = intent.getStringExtra(CONST.DATA_TIME)
                }
                var imgUrl: String? = ""
                if (intent.hasExtra(CONST.IMG_URL)) {
                    imgUrl = intent.getStringExtra(CONST.IMG_URL)
                }
                var url = ""
                if (!TextUtils.isEmpty(dataUrl) && dataUrl.endsWith(".pdf")) {
                    url = dataUrl.replace(".pdf", ".doc")
                }
                CommonUtil.share(this, tvTitle.text.toString(), time, imgUrl, url)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1001 -> {
                    if (ratingBar != null) {
                        ratingBar!!.visibility = View.GONE
                    }
                    if (tvFeedback != null) {
                        tvFeedback.visibility = View.GONE
                    }
                    finish()
                }
            }
        }
    }

    //需要申请的所有权限
    private val allPermissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    //拒绝的权限集合
    private val deniedList: MutableList<String> = ArrayList()

    /**
     * 申请定位权限
     */
    private fun checkAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            init()
        } else {
            deniedList.clear()
            for (permission in allPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(permission)
                }
            }
            if (deniedList.isEmpty()) { //所有权限都授予
                init()
            } else {
                val permissions = deniedList.toTypedArray() //将list转成数组
                ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AuthorityUtil.AUTHOR_LOCATION -> if (grantResults.isNotEmpty()) {
                var isAllGranted = true //是否全部授权
                for (gResult in grantResults) {
                    if (gResult != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false
                        break
                    }
                }
                if (isAllGranted) { //所有权限都授予
                    init()
                } else { //只要有一个没有授权，就提示进入设置界面设置
                    AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用存储权限，是否前往设置？")
                }
            } else {
                for (permission in permissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this@PDFActivity, permission!!)) {
                        AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用存储权限，是否前往设置？")
                        break
                    }
                }
            }
        }
    }

    override fun OnStarChanged(selectedNumber: Float, position: Int) {
        okHttpSubmit(selectedNumber)
    }

    /**
     * 反馈
     */
    private fun okHttpSubmit(selectedNumber: Float) {
        val url = "http://decision-admin.tianqi.cn/home/Evaluate/doinsert"
        val builder = FormBody.Builder()
        builder.add("uid", MyApplication.UID)
        builder.add("url", dataUrl)
        builder.add("whole", selectedNumber.toString())
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
                                        Toast.makeText(this@PDFActivity, msg, Toast.LENGTH_SHORT).show()
                                        Handler().postDelayed({
                                            if (ratingBar != null) {
                                                ratingBar!!.visibility = View.GONE
                                            }
                                            if (tvFeedback != null) {
                                                tvFeedback.visibility = View.GONE
                                            }
                                        }, 1500)
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
