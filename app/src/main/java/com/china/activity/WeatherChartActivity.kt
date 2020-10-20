package com.china.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.maps.model.TextOptions
import com.china.R
import com.china.common.CONST
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_weather_chart.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 天气图分析
 */
class WeatherChartActivity : BaseActivity(), View.OnClickListener, AMap.OnMapScreenShotListener {

    private var aMap : AMap? = null
    private var isShowSwitch = true
    private var type = 1
    private var result1 : String? = null
    private var result2 : String? = null
    private var result3 : String? = null
    private val sdf1 = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_chart)
        initMap(savedInstanceState)
        initWidget()
    }

    /**
     * 初始化地图
     */
    private fun initMap(bundle : Bundle?) {
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.926628, 105.178100), 3.7f))
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
    }

    private fun refresh() {
        showDialog()
        var url : String? = null
        when (type) {
            1 -> {
                url = "https://scapi.tianqi.cn/weather/xstu?test=ncg&type=1&hm=h000"
            }
            2 -> {
                url = "https://scapi.tianqi.cn/weather/xstu?test=ncg&type=1&hm=h850"
            }
            3 -> {
                url = "https://scapi.tianqi.cn/weather/xstu?test=ncg&type=1&hm=h500"
            }
        }
        okHttpData(url)
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivShare.setOnClickListener(this)
        ivChart.setOnClickListener(this)
        ivSwitch.setOnClickListener(this)
        tv1.setOnClickListener(this)
        tv2.setOnClickListener(this)
        tv3.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        refresh()
    }

    private fun okHttpData(url : String?) {
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url!!).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    when (type) {
                        1 -> {
                            result1 = result
                        }
                        2 -> {
                            result2 = result
                        }
                        3 -> {
                            result3 = result
                        }
                    }
                    parseData(result)
                }
            })
        }).start()
    }

    private fun parseData(result : String?) {
        aMap!!.clear()
        if (!TextUtils.isEmpty(result)) {
            try {
                val obj = JSONObject(result)
                if (!obj.isNull("list")) {
                    val array = obj.getJSONArray("list")
                    val dataUrl = array.getString(0)
                    Thread(Runnable {
                        OkHttpUtil.enqueue(Request.Builder().url(dataUrl).build(), object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                            }
                            override fun onResponse(call: Call, response: Response) {
                                if (!response.isSuccessful) {
                                    return
                                }
                                val result = response.body!!.string()
                                runOnUiThread {
                                    cancelDialog()
                                    if (!TextUtils.isEmpty(result)) {
                                        try {
                                            val obj = JSONObject(result)

                                            if (!obj.isNull("mtime")) {
                                                try {
                                                    val mTime = obj.getLong("mtime")
                                                    tvTime.text = sdf1.format(Date(mTime))+"更新"
                                                    tvTime.visibility = View.VISIBLE
                                                } catch (e : JSONException) {
                                                    e.printStackTrace()
                                                }
                                            }
                                            ivShare.visibility = View.VISIBLE
                                            clShare.visibility = View.VISIBLE

                                            if (!obj.isNull("lines")) {
                                                val lines = obj.getJSONArray("lines")
                                                for (i in 0 until lines.length()) {
                                                    val itemObj = lines.getJSONObject(i)
                                                    if (!itemObj.isNull("point")) {
                                                        val points = itemObj.getJSONArray("point")
                                                        val polylineOption = PolylineOptions()
                                                        polylineOption.width(6.0f).color(0xff406bbf.toInt())
                                                        for (j in 0 until points.length()) {
                                                            val point = points.getJSONObject(j)
                                                            val lat = point.getDouble("y")
                                                            val lng = point.getDouble("x")
                                                            polylineOption.add(LatLng(lat, lng))
                                                        }
                                                        aMap!!.addPolyline(polylineOption)
                                                    }
                                                    if (!itemObj.isNull("flags")) {
                                                        val flags = itemObj.getJSONObject("flags")
                                                        var text = ""
                                                        if (!flags.isNull("text")) {
                                                            text = flags.getString("text")
                                                        }
                                                        if (!flags.isNull("items")) {
                                                            val items = flags.getJSONArray("items")
                                                            val item = items.getJSONObject(0)
                                                            val lat = item.getDouble("y")
                                                            val lng = item.getDouble("x")
                                                            val to = TextOptions()
                                                            to.position(LatLng(lat, lng))
                                                            to.text(text)
                                                            to.fontColor(Color.BLACK)
                                                            to.fontSize(30)
                                                            to.backgroundColor(Color.TRANSPARENT)
                                                            aMap!!.addText(to)
                                                        }
                                                    }
                                                }
                                            }
                                            if (!obj.isNull("line_symbols")) {
                                                val lineSymbols = obj.getJSONArray("line_symbols")
                                                for (i in 0 until lineSymbols.length()) {
                                                    val itemObj = lineSymbols.getJSONObject(i)
                                                    if (!itemObj.isNull("items")) {
                                                        val items = itemObj.getJSONArray("items")
                                                        val polylineOption = PolylineOptions()
                                                        polylineOption.width(6.0f).color(0xff406bbf.toInt())
                                                        for (j in 0 until items.length()) {
                                                            val item = items.getJSONObject(j)
                                                            val lat = item.getDouble("y")
                                                            val lng = item.getDouble("x")
                                                            polylineOption.add(LatLng(lat, lng))
                                                        }
                                                        aMap!!.addPolyline(polylineOption)
                                                    }
                                                }
                                            }
                                            if (!obj.isNull("symbols")) {
                                                val symbols = obj.getJSONArray("symbols")
                                                for (i in 0 until symbols.length()) {
                                                    val itemObj = symbols.getJSONObject(i)
                                                    var text = ""
                                                    var color = Color.BLACK
                                                    if (!itemObj.isNull("type")) {
                                                        val type = itemObj.getString("type")
                                                        when {
                                                            TextUtils.equals(type, "60") -> {
                                                                text = "H"
                                                                color = Color.RED
                                                            }
                                                            TextUtils.equals(type, "61") -> {
                                                                text = "L"
                                                                color = Color.BLUE
                                                            }
                                                            TextUtils.equals(type, "37") -> {
                                                                text = "台"
                                                                color = Color.GREEN
                                                            }
                                                        }
                                                    }
                                                    val lat = itemObj.getDouble("y")
                                                    val lng = itemObj.getDouble("x")
                                                    val to = TextOptions()
                                                    to.position(LatLng(lat, lng))
                                                    to.text(text)
                                                    to.fontColor(color)
                                                    to.fontSize(60)
                                                    to.backgroundColor(Color.TRANSPARENT)
                                                    aMap!!.addText(to)
                                                }
                                            }
                                        } catch (e : JSONException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        })
                    }).start()
                }
            } catch (e : JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun onMapScreenShot(bitmap1: Bitmap?) {
        val bitmap2 = CommonUtil.captureView(clShare)
        val bitmap3 = CommonUtil.mergeBitmap(this, bitmap1, bitmap2, true)
        CommonUtil.clearBitmap(bitmap1)
        CommonUtil.clearBitmap(bitmap2)
        val bitmap4 = BitmapFactory.decodeResource(resources, R.drawable.legend_share_portrait)
        val bitmap = CommonUtil.mergeBitmap(this, bitmap3, bitmap4, false)
        CommonUtil.clearBitmap(bitmap3)
        CommonUtil.clearBitmap(bitmap4)
        CommonUtil.share(this, bitmap)
    }
    override fun onMapScreenShot(p0: Bitmap?, p1: Int) {
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.llBack -> finish()
            R.id.ivShare -> aMap!!.getMapScreenShot(this)
            R.id.ivSwitch -> {
                isShowSwitch = !isShowSwitch
                if (isShowSwitch) {
                    llSwitch.visibility = View.VISIBLE
                }else {
                    llSwitch.visibility = View.GONE
                }
            }
            R.id.tv1 -> {
                type = 1
                tv1.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                tv2.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tv3.setTextColor(ContextCompat.getColor(this, R.color.text_color4))

                if (!TextUtils.isEmpty(result1)) {
                    parseData(result1)
                }else {
                    refresh()
                }
            }
            R.id.tv2 -> {
                type = 2
                tv1.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tv2.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                tv3.setTextColor(ContextCompat.getColor(this, R.color.text_color4))

                if (!TextUtils.isEmpty(result2)) {
                    parseData(result2)
                }else {
                    refresh()
                }
            }
            R.id.tv3 -> {
                type = 3
                tv1.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tv2.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tv3.setTextColor(ContextCompat.getColor(this, R.color.text_color3))

                if (!TextUtils.isEmpty(result3)) {
                    parseData(result3)
                }else {
                    refresh()
                }
            }
            R.id.ivChart -> {
                if (ivLegend.visibility == View.VISIBLE) {
                    ivLegend.visibility = View.INVISIBLE
                }else {
                    ivLegend.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView.onPause()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (mapView != null) {
            mapView.onSaveInstanceState(outState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null) {
            mapView.onDestroy()
        }
    }

}
