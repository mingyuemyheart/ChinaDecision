package com.china.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import com.china.R
import com.china.adapter.BroadcastWeatherAdapter
import com.china.common.CONST
import com.china.dto.SettingDto
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_broadcast_weather.*
import kotlinx.android.synthetic.main.activity_broadcast_weather.reTitle
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 联播天气
 */
class BroadcastWeatherActivity : BaseActivity(), View.OnClickListener {

    private val dataList : ArrayList<SettingDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("MM月dd日", Locale.CHINA)
    private var configuration : Configuration? = null //方向监听器

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast_weather)
        initWidget()
        initSurfaceView()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configuration = newConfig
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            showPort()
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showLand()
        }
        setSurfaceViewLayout()
    }

    /**
     * 显示竖屏，隐藏横屏
     */
    private fun showPort() {
        reTitle!!.visibility = View.VISIBLE
        ivExpand.setImageResource(R.drawable.shawn_icon_expand)
        fullScreen(false)
    }

    /**
     * 显示横屏，隐藏竖屏
     */
    private fun showLand() {
        reTitle!!.visibility = View.GONE
        ivExpand.setImageResource(R.drawable.iv_collose)
        fullScreen(true)
    }

    private fun fullScreen(enable: Boolean) {
        if (enable) {
            val lp = window.attributes
            lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            window.attributes = lp
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        } else {
            val attr = window.attributes
            attr.flags = attr.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            window.attributes = attr
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivPlay.setOnClickListener(this)
		ivExpand.setOnClickListener(this)
        ivShare.setOnClickListener(this)
        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle!!.text = title
        }
        showPort()
        okHttpList()
    }

    private fun okHttpList() {
        showDialog()
        val url = intent.getStringExtra(CONST.WEB_URL)
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        cancelDialog()
                        dataList.clear()
                        if (!TextUtils.isEmpty(result)) {
                            val array = JSONArray(result)
                            for (i in array.length()-1 downTo 0) {
                                val dto = SettingDto()
                                dto.value = array.getString(i)
                                val dataUrl = dto.value.split("_")
                                val last = dataUrl[dataUrl.size-1]
                                val time = last.substring(0, last.length-4)
                                Log.e("lasttime", last+"----"+time)
                                dto.name = sdf2.format(sdf1.parse(time))
                                dto.isSelected = i == 0
                                dataList.add(dto)
                            }
                            initGridView()
                        }
                    }
                }
            })
        }).start()
    }

    private fun initGridView() {
        val mAdapter = BroadcastWeatherAdapter(this, dataList)
        gridView.adapter = mAdapter
        gridView.setOnItemClickListener { parent, view, position, id ->
            val dto = dataList[position]
            for (i in 0 until dataList.size) {
                val data = dataList[i]
                data.isSelected = TextUtils.equals(dataList[i].name, dto.name)
            }
            mAdapter.notifyDataSetChanged()
            releaseMediaPlayer()
            playVideo(dto.value)
        }
    }

    private fun playVideo(videoUrl : String) {
        showDialog()
        videoView.setVideoPath(videoUrl)
        videoView.start()
    }

    private fun setSurfaceViewLayout() {
        val params = ConstraintLayout.LayoutParams(CommonUtil.widthPixels(this), CommonUtil.widthPixels(this)*9/16)
        videoView!!.layoutParams = params
        imageView.layoutParams = params
    }

    /**
     * 初始化videoView
     */
    private fun initSurfaceView() {
        setSurfaceViewLayout()
        videoView.setOnClickListener(this)
        videoView.setOnPreparedListener {
            cancelDialog()
            imageView.visibility = View.GONE
        }
    }

    private fun swithVideo() {
        if (videoView!!.isPlaying) {
            videoView!!.pause()
            ivPlay!!.setImageResource(R.drawable.icon_play)
        } else {
            videoView!!.start()
            ivPlay!!.setImageResource(R.drawable.icon_pause)
        }
    }

    /**
     * 释放MediaPlayer资源
     */
    private fun releaseMediaPlayer() {
        videoView.stopPlayback()
        videoView.suspend()
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1001 -> ivPlay!!.visibility = View.GONE
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (configuration == null) {
                finish()
            } else {
                if (configuration!!.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    finish()
                } else if (configuration!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        ivPlay!!.visibility = View.GONE
        ivExpand.visibility = View.GONE
        releaseMediaPlayer()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.videoView -> {
                if (ivPlay!!.visibility == View.VISIBLE) {
                    ivPlay!!.visibility = View.GONE
                    ivExpand.visibility = View.GONE
                } else {
                    ivPlay!!.visibility = View.VISIBLE
                    ivExpand.visibility = View.VISIBLE
                    handler.removeMessages(1001)
                    val msg = handler.obtainMessage(1001)
                    msg.what = 1001
                    handler.sendMessageDelayed(msg, 5000)
                }
            }
            R.id.ivPlay -> swithVideo()
            R.id.ivExpand -> {
                if (configuration == null) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }else {
                    if (configuration!!.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }else if (configuration!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
            }
        }
    }

}