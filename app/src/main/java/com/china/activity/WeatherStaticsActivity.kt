package com.china.activity

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.*
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.ScaleAnimation
import com.china.R
import com.china.common.CONST
import com.china.dto.WeatherStaticsDto
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import com.china.utils.SecretUrlUtil
import com.china.view.CircularProgressBar
import kotlinx.android.synthetic.main.activity_weather_statics.*
import kotlinx.android.synthetic.main.layout_statistic_marker_icon.view.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 天气统计
 */
class WeatherStaticsActivity : BaseActivity(), OnClickListener, OnMarkerClickListener, OnMapClickListener, OnCameraChangeListener, OnMapScreenShotListener {

    private var aMap: AMap? = null
    private val level1List: MutableList<WeatherStaticsDto> = ArrayList()
    private val level2List: MutableList<WeatherStaticsDto> = ArrayList()
    private val level3List: MutableList<WeatherStaticsDto> = ArrayList()
    private val areaIdMap = HashMap<String, WeatherStaticsDto>() //按区域id区分
    private val markerMap: MutableMap<String, Marker> = LinkedHashMap() //按区域id区分
    private var zoom = 3.7f
    private var zoom1 = 6.5f
    private var zoom2 = 8.5f
    private var leftlatlng = LatLng(-16.305714763804854, 75.13831436634065)
    private var rightLatlng = LatLng(63.681687310440864, 135.21788656711578)
    private val level1 = "level1"
    private val level2 = "level2"
    private val level3 = "level3"
    private val sdf1 = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("MM月dd日", Locale.CHINA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_statics)
        initMap(savedInstanceState)
        showDialog()
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivShare.setOnClickListener(this)
        ivShare.visibility = View.VISIBLE
        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (title != null) {
            tvTitle.text = title
        }
        okHttpList()
    }

    /**
     * 初始化地图
     */
    private fun initMap(bundle: Bundle?) {
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.926628, 105.178100), zoom))
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setOnMapClickListener(this)
        aMap!!.setOnCameraChangeListener(this)
    }

    override fun onCameraChange(arg0: CameraPosition?) {}
    override fun onCameraChangeFinish(arg0: CameraPosition) {
        if (clShare!!.visibility == View.VISIBLE) {
            hideAnimation(clShare)
        }
        val leftPoint = Point(0, CommonUtil.heightPixels(this))
        val rightPoint = Point(CommonUtil.widthPixels(this), 0)
        leftlatlng = aMap!!.projection.fromScreenLocation(leftPoint)
        rightLatlng = aMap!!.projection.fromScreenLocation(rightPoint)
        zoom = arg0.zoom
        switchMarkers()
        addMarkers()
    }

    /**
     * 获取天气统计数据
     */
    private fun okHttpList() {
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(SecretUrlUtil.statistic()).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    if (!TextUtils.isEmpty(result)) {
                        level1List.clear()
                        level2List.clear()
                        level3List.clear()
                        parseStationInfo(result, level1)
                        parseStationInfo(result, level2)
                        parseStationInfo(result, level3)
                        addMarkers()
                        runOnUiThread { cancelDialog() }
                    }
                }
            })
        }).start()
    }

    /**
     * 解析数据
     */
    private fun parseStationInfo(result: String, level: String) {
        try {
            val obj = JSONObject(result)
            if (!obj.isNull(level)) {
                val array = JSONArray(obj.getString(level))
                val list: MutableList<WeatherStaticsDto> = ArrayList()
                for (i in 0 until array.length()) {
                    val dto = WeatherStaticsDto()
                    val itemObj = array.getJSONObject(i)
                    if (!itemObj.isNull("name")) {
                        dto.name = itemObj.getString("name")
                    }
                    if (!itemObj.isNull("stationid")) {
                        dto.stationId = itemObj.getString("stationid")
                    }
                    if (!itemObj.isNull("level")) {
                        dto.level = itemObj.getString("level")
                    }
                    if (!itemObj.isNull("areaid")) {
                        dto.areaId = itemObj.getString("areaid")
                    }
                    if (!itemObj.isNull("lat")) {
                        dto.lat = itemObj.getDouble("lat")
                    }
                    if (!itemObj.isNull("lon")) {
                        dto.lng = itemObj.getDouble("lon")
                    }
                    list.add(dto)
                    areaIdMap[dto.areaId] = dto
                    when {
                        TextUtils.equals(level, level1) -> {
                            level1List.add(dto)
                        }
                        TextUtils.equals(level, level2) -> {
                            level2List.add(dto)
                        }
                        TextUtils.equals(level, level3) -> {
                            level3List.add(dto)
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun switchMarkers() {
        Thread(Runnable {
            for (areaId in markerMap.keys) {
                if (!TextUtils.isEmpty(areaId) && markerMap.containsKey(areaId)) {
                    val marker = markerMap[areaId]
                    val level = marker!!.snippet
                    val lat = marker.position.latitude
                    val lng = marker.position.longitude
                    if (zoom <= zoom1) {
                        if (TextUtils.equals(level, "2") || TextUtils.equals(level, "3")) {
                            marker.remove()
                        }
                    } else if (zoom > zoom1 && zoom <= zoom2) {
                        if (TextUtils.equals(level, "3")) {
                            marker.remove()
                        }
                    } else {
                    }
                    if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
                        //已经在可是范围内则不处理
                    } else {
                        marker.remove()
                    }
                }
            }
        }).start()
    }

    /**
     * 添加marker
     */
    private fun addMarkers() {
        Thread(Runnable {
            val list: MutableList<WeatherStaticsDto> = ArrayList()
            if (zoom <= zoom1) {
                list.addAll(level1List)
            } else if (zoom > zoom1 && zoom <= zoom2) {
                list.addAll(level1List)
                list.addAll(level2List)
            } else {
                list.addAll(level1List)
                list.addAll(level2List)
                list.addAll(level3List)
            }
            val markers: MutableMap<String, Marker> = LinkedHashMap() //为防止ConcurrentModificationException异常，待循环执行完毕后，再对markerMap进行修改
            for (dto in list) {
                if (markerMap.containsKey(dto.areaId)) {
                    val m = markerMap[dto.areaId]
                    if (m != null && m.isVisible) {
                        //已经在可是区域添加过了，就不重复绘制了
                    } else {
                        addVisibleAreaMarker(dto, markers)
                    }
                } else {
                    addVisibleAreaMarker(dto, markers)
                }
            }
            markerMap.putAll(markers)
        }).start()
    }

    /**
     * 添加可视区域对应的marker
     * @param dto
     */
    private fun addVisibleAreaMarker(dto: WeatherStaticsDto, markers: MutableMap<String, Marker>) {
        if (dto.lat > leftlatlng.latitude && dto.lat < rightLatlng.latitude && dto.lng > leftlatlng.longitude && dto.lng < rightLatlng.longitude) {
            val options = MarkerOptions()
            options.title(dto.areaId)
            options.snippet(dto.level)
            options.anchor(0.5f, 1.0f)
            options.position(LatLng(dto.lat, dto.lng))
            options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.name)))
            val marker = aMap!!.addMarker(options)
            markers[dto.areaId] = marker
            markerExpandAnimation(marker)
        }
    }

    /**
     * 给marker添加文字
     * @param name 城市名称
     * @return
     */
    private fun getTextBitmap(name: String): View? {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_statistic_marker_icon, null) ?: return null
        if (!TextUtils.isEmpty(name) && name.length > 2) {
            val result = "${name.substring(0, 2)}${name.substring(2, name.length)}"
            view.tvName.text = result
        } else {
            view.tvName.text = name
        }
        return view
    }

    private fun markerExpandAnimation(marker: Marker) {
        val animation = ScaleAnimation(0f, 1f, 0f, 1f)
        animation.setInterpolator(LinearInterpolator())
        animation.setDuration(300)
        marker.setAnimation(animation)
        marker.startAnimation()
    }

    private fun markerColloseAnimation(marker: Marker) {
        val animation = ScaleAnimation(1f, 0f, 1f, 0f)
        animation.setInterpolator(LinearInterpolator())
        animation.setDuration(300)
        marker.setAnimation(animation)
        marker.startAnimation()
    }

    override fun onMapClick(arg0: LatLng?) {
        if (clShare!!.visibility == View.VISIBLE) {
            hideAnimation(clShare)
        }
    }

    /**
     * 向上弹出动画
     * @param layout
     */
    private fun showAnimation(layout: View?) {
        if (layout!!.visibility == View.VISIBLE) {
            return
        }
        val animation = TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 1f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f)
        animation.duration = 300
        layout.startAnimation(animation)
        layout.visibility = View.VISIBLE
    }

    /**
     * 向下隐藏动画
     * @param layout
     */
    private fun hideAnimation(layout: View?) {
        if (layout!!.visibility == View.GONE) {
            return
        }
        val animation = TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 1f)
        animation.duration = 300
        layout.startAnimation(animation)
        layout.visibility = View.GONE
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (clShare!!.visibility != View.VISIBLE) {
            showAnimation(clShare)
        }
        if (marker != null) {
            if (areaIdMap.containsKey(marker.title)) {
                val dto = areaIdMap[marker.title]
                tvName!!.text = dto!!.name + " " + dto.stationId
                tvDetail.text = ""
                clContent.visibility = View.INVISIBLE
                okHttpDetail(SecretUrlUtil.statisticDetail(dto.stationId))
            }
        }
        return true
    }

    private fun okHttpDetail(url: String) {
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
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
                                val startTime = sdf2.format(sdf3.parse(obj.getString("starttime")))
                                val endTime = sdf2.format(sdf3.parse(obj.getString("endtime")))
                                var highTemp: String? = "" //高温
                                var lowTemp: String? = "" //低温
                                var highWind: String? = "" //最大风速
                                var highRain: String? = "" //最大降水量
                                var lxGaowen = "" //连续高温
                                var no_rain_lx = "" //连续没雨天数
                                if (!obj.isNull("count")) {
                                    val array = JSONArray(obj.getString("count"))
                                    val itemObj0 = array.getJSONObject(0) //温度
                                    val itemObj1 = array.getJSONObject(1) //降水
                                    val itemObj5 = array.getJSONObject(5) //风速

                                    //温度
                                    if (!itemObj0.isNull("max") && !itemObj0.isNull("min")) {
                                        highTemp = itemObj0.getString("max")
                                        highTemp = if (TextUtils.equals(highTemp, "-1.0")) {
                                            getString(R.string.no_statics)
                                        } else {
                                            val time = sdf4.format(sdf1.parse(itemObj0.getString("maxtime")))
                                            "$highTemp℃($time)"
                                        }
                                        lowTemp = itemObj0.getString("min")
                                        lowTemp = if (TextUtils.equals(lowTemp, "-1.0")) {
                                            getString(R.string.no_statics)
                                        } else {
                                            val time = sdf4.format(sdf1.parse(itemObj0.getString("mintime")))
                                            "$lowTemp℃($time)"
                                        }
                                    }

                                    //降水
                                    if (!itemObj1.isNull("max")) {
                                        highRain = itemObj1.getString("max")
                                        highRain = if (TextUtils.equals(highRain, "-1.0")) {
                                            getString(R.string.no_statics)
                                        } else {
                                            val time = sdf4.format(sdf1.parse(itemObj1.getString("maxtime")))
                                            highRain + "mm" + "(" + time + ")"
                                        }
                                    }

                                    //风速
                                    if (!itemObj5.isNull("max")) {
                                        highWind = itemObj5.getString("max")
                                        highWind = if (TextUtils.equals(highWind, "-1.0")) {
                                            getString(R.string.no_statics)
                                        } else {
                                            val maxlv = itemObj5.getString("maxlv")
                                            val time = sdf4.format(sdf1.parse(itemObj5.getString("maxtime")))
                                            highWind + "m/s[" + maxlv + "级]" + "(" + time + ")"
                                        }
                                    }
                                }
                                if (startTime != null && endTime != null && highTemp != null && lowTemp != null && highWind != null && highRain != null) {
                                    val start = sdf2.parse(startTime).time
                                    val end = sdf2.parse(endTime).time
                                    val dayCount = ((end - start) / (1000 * 60 * 60 * 24)).toFloat() + 1
                                    if (!obj.isNull("tqxxcount")) {
                                        val array = JSONArray(obj.getString("tqxxcount"))
                                        for (i in 0 until array.length()) {
                                            val itemObj = array.getJSONObject(i)
                                            val name = itemObj.getString("name")
                                            val value = itemObj.getInt("value")
                                            val maxlx = itemObj.getInt("maxlx")
                                            var nomaxlx = 0
                                            if (!itemObj.isNull("nomaxlx")) {
                                                nomaxlx = itemObj.getInt("nomaxlx")
                                            }
                                            if (i == 0) {
                                                if (value == -1) {
                                                    tvBar1.text = "$name\n--"
                                                    animate(bar1, null, 0f, 1000)
                                                 bar1!!.progress = 0f
                                                } else {
                                                    tvBar1.text = """
                                          $name
                                          ${value}天
                                          """.trimIndent()
                                                    animate(bar1, null, -value / dayCount, 1000)
                                                    bar1!!.progress = -value / dayCount
                                                }
                                            } else if (i == 1) {
                                                no_rain_lx = nomaxlx.toString() + "天"
                                                if (value == -1) {
                                                    tvBar2.text = "$name\n--"
                                                    animate(bar2, null, 0f, 1000)
                                                 bar2.progress = 0f
                                                } else {
                                                    tvBar2.text = """
                                          $name
                                          ${value}天
                                          """.trimIndent()
                                                    animate(bar2, null, -value / dayCount, 1000)
                                                 bar2.progress = -value / dayCount
                                                }
                                            } else if (i == 2) {
                                            } else if (i == 3) {
                                                if (value == -1) {
                                                    tvBar4.text = "$name\n--"
                                                    animate(bar4, null, 0f, 1000)
                                                 bar4.progress = 0f
                                                } else {
                                                    tvBar4.text = """
                                          $name
                                          ${value}天
                                          """.trimIndent()
                                                    animate(bar4, null, -value / dayCount, 1000)
                                                 bar4.progress = -value / dayCount
                                                }
                                            } else if (i == 4) {
                                            } else if (i == 5) {
                                                if (value == -1) {
                                                    tvBar3.text = "$name\n--"
                                                    animate(bar3, null, 0f, 1000)
                                                 bar3.progress = 0f
                                                } else {
                                                    tvBar3.text = """
                                          $name
                                          ${value}天
                                          """.trimIndent()
                                                    animate(bar3, null, -value / dayCount, 1000)
                                                 bar3.progress = -value / dayCount
                                                }
                                            } else if (i == 6) {
                                                lxGaowen = maxlx.toString() + "天"
                                                if (value == -1) {
                                                    tvBar5.text = "$name\n--"
                                                    animate(bar5, null, 0f, 1000)
                                                 bar5.progress = 0f
                                                } else {
                                                    tvBar5.text = """
                                          $name
                                          ${value}天
                                          """.trimIndent()
                                                    animate(bar5, null, -value / dayCount, 1000)
                                                 bar5.progress = -value / dayCount
                                                }
                                            }
                                        }
                                    }
                                    val buffer = StringBuffer()
                                    buffer.append(getString(R.string.from)).append(startTime)
                                    buffer.append(getString(R.string.to)).append(endTime)
                                    buffer.append("：\n")
                                    buffer.append(getString(R.string.highest_temp)).append(highTemp).append("，")
                                    buffer.append(getString(R.string.lowest_temp)).append(lowTemp).append("，")
                                    buffer.append(getString(R.string.max_speed)).append(highWind).append("，")
                                    buffer.append(getString(R.string.max_fall)).append(highRain).append("，")
                                    buffer.append(getString(R.string.lx_no_fall)).append(no_rain_lx).append("，")
                                    buffer.append(getString(R.string.lx_gaowen)).append(lxGaowen).append("。")
                                    val builder = SpannableStringBuilder(buffer.toString())
                                    val builderSpan1 = ForegroundColorSpan(Color.RED)
                                    val builderSpan2 = ForegroundColorSpan(Color.RED)
                                    val builderSpan3 = ForegroundColorSpan(Color.RED)
                                    val builderSpan4 = ForegroundColorSpan(Color.RED)
                                    val builderSpan5 = ForegroundColorSpan(Color.RED)
                                    val builderSpan6 = ForegroundColorSpan(Color.RED)
                                    builder.setSpan(builderSpan1, 29, 29 + highTemp.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan2, 29 + highTemp.length + 6, 29 + highTemp.length + 6 + lowTemp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                                    builder.setSpan(builderSpan3, 29 + highTemp.length + 6 + lowTemp.length + 6, 29 + highTemp.length + 6 + lowTemp.length + 6 + highWind.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan4, 29 + highTemp.length + 6 + lowTemp.length + 6 + highWind.length + 7, 29 + highTemp.length + 6 + lowTemp.length + 6 + highWind.length + 7 + highRain.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan5, 29 + highTemp.length + 6 + lowTemp.length + 6 + highWind.length + 7 + highRain.length + 10, 29 + highTemp.length + 6 + lowTemp.length + 6 + highWind.length + 7 + highRain.length + 10 + no_rain_lx.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan6, 29 + highTemp.length + 6 + lowTemp.length + 6 + highWind.length + 7 + highRain.length + 10 + no_rain_lx.length + 9, 29 + highTemp.length + 6 + lowTemp.length + 6 + highWind.length + 7 + highRain.length + 10 + no_rain_lx.length + 9 + lxGaowen.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    tvDetail.text = builder
                                }
                                clContent.visibility = View.VISIBLE
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }).start()
    }

    /**
     * 进度条动画
     * @param progressBar
     * @param listener
     * @param progress
     * @param duration
     */
    private fun animate(progressBar: CircularProgressBar?, listener: AnimatorListener?, progress: Float, duration: Int) {
        val mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress)
        mProgressBarAnimator.duration = duration.toLong()
        mProgressBarAnimator.addListener(object : AnimatorListener {
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                progressBar!!.progress = progress
            }

            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationStart(animation: Animator) {}
        })
        if (listener != null) {
            mProgressBarAnimator.addListener(listener)
        }
        mProgressBarAnimator.reverse()
        mProgressBarAnimator.addUpdateListener { animation -> progressBar!!.progress = (animation.animatedValue as Float) }
        //		progressBar.setMarkerProgress(0f);
        mProgressBarAnimator.start()
    }

    override fun onMapScreenShot(bitmap1: Bitmap?) { //bitmap1为地图截屏
        val bitmap: Bitmap
        val bitmap4 = BitmapFactory.decodeResource(resources, R.drawable.legend_share_portrait)
        if (clShare!!.visibility == View.VISIBLE) {
            val bitmap2 = CommonUtil.captureView(clShare)
            val bitmap3 = CommonUtil.mergeBitmap(this, bitmap1, bitmap2, true)
            CommonUtil.clearBitmap(bitmap2)
            bitmap = CommonUtil.mergeBitmap(this, bitmap3, bitmap4, false)
            CommonUtil.clearBitmap(bitmap3)
        } else {
            bitmap = CommonUtil.mergeBitmap(this, bitmap1, bitmap4, false)
        }
        CommonUtil.clearBitmap(bitmap1)
        CommonUtil.clearBitmap(bitmap4)
        CommonUtil.share(this, bitmap)
    }

    override fun onMapScreenShot(arg0: Bitmap?, arg1: Int) {}

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (clShare!!.visibility == View.VISIBLE) {
                hideAnimation(clShare)
                return false
            } else {
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> if (clShare!!.visibility == View.VISIBLE) {
                hideAnimation(clShare)
            } else {
                finish()
            }
            R.id.ivShare -> aMap!!.getMapScreenShot(this@WeatherStaticsActivity)
        }
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView!!.onResume()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (mapView != null) {
            mapView!!.onSaveInstanceState(outState)
        }
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null) {
            mapView!!.onDestroy()
        }
    }

}
