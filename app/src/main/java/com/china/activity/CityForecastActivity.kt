package com.china.activity;

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.LinearInterpolator
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.*
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.ScaleAnimation
import com.china.R
import com.china.common.CONST
import com.china.dto.WeatherDto
import com.china.dto.WeatherStaticsDto
import com.china.utils.*
import kotlinx.android.synthetic.main.activity_city_forecast.*
import kotlinx.android.synthetic.main.layout_marker.view.*
import kotlinx.android.synthetic.main.layout_marker_info.view.*
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
import kotlin.collections.LinkedHashMap

/**
 * 城市天气预报
 */
class CityForecastActivity : BaseActivity(), OnClickListener, OnMarkerClickListener,
        OnMapClickListener, OnCameraChangeListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener, OnMapScreenShotListener {

    private var aMap: AMap? = null
    private val level1List: ArrayList<WeatherStaticsDto> = ArrayList()
    private val level2List: ArrayList<WeatherStaticsDto> = ArrayList()
    private val markerMap: LinkedHashMap<String, Marker?> = LinkedHashMap() //按区域id区分
    private val level1 = "level1"
    private val level2 = "level2"
    private val level3 = "level3"
    private var zoom = 3.7f
    private var zoom1 = 7.0f
    private var leftlatlng = LatLng(-16.305714763804854, 75.13831436634065)
    private var rightLatlng = LatLng(63.681687310440864, 135.21788656711578)
    private var clickMarker: Marker? = null
    private val sdf1 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyy年MM月dd日 HH时", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("HH", Locale.CHINA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_forecast)
        initMap(savedInstanceState)
        checkAuthority()
    }

    private fun init() {
        initWidget()
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
        aMap!!.mapType = MAP_TYPE_SATELLITE
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setOnMapClickListener(this)
        aMap!!.setOnCameraChangeListener(this)
        aMap!!.setInfoWindowAdapter(this)
        aMap!!.setOnInfoWindowClickListener(this)
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        showDialog()
        llBack.setOnClickListener(this)
        ivShare.setOnClickListener(this)
        ivShare.visibility = View.VISIBLE
        ivMapSearch.setOnClickListener(this)
        ivMapSearch.visibility = View.VISIBLE

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        okHttpList()
    }

    /**
     * 获取所有站点信息
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
                    runOnUiThread { cancelDialog() }
                    val result = response.body!!.string()
                    if (!TextUtils.isEmpty(result)) {
                        level1List.clear()
                        level2List.clear()
                        parseStationInfo(result, level1)
                        parseStationInfo(result, level2)
                        parseStationInfo(result, level3)
                        addMarkers()
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
                    if (TextUtils.equals(level, level1)) {
                        level1List.add(dto)
                    } else if (TextUtils.equals(level, level2)) {
                        level2List.add(dto)
                    } else if (TextUtils.equals(level, level3)) { //把四个直辖市的区划入地市级
                        if (dto.areaId.contains("10101") || dto.areaId.contains("10102") || dto.areaId.contains("10103") || dto.areaId.contains("10104")) {
                            dto.level = "2"
                            level2List.add(dto)
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun switchMarkers() {
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
                }
                if (lat <= leftlatlng.latitude || lat >= rightLatlng.latitude || lng <= leftlatlng.longitude || lng >= rightLatlng.longitude) {
                    marker.remove()
                }
            }
        }
    }

    /**
     * 添加marker
     */
    private fun addMarkers() {
        val list: MutableList<WeatherStaticsDto> = ArrayList()
        if (zoom <= zoom1) {
            list.addAll(level1List)
        } else {
            list.addAll(level1List)
            list.addAll(level2List)
        }
        for (dto in list) {
            if (markerMap.containsKey(dto.areaId)) {
                val m = markerMap[dto.areaId]
                if (m == null || !m.isVisible) {
                    addVisibleAreaMarker(dto)
                }
            } else {
                addVisibleAreaMarker(dto)
            }
        }
    }

    /**
     * 添加可视区域对应的marker
     * @param dto
     */
    private fun addVisibleAreaMarker(dto: WeatherStaticsDto) {
        if (dto.lat > leftlatlng.latitude && dto.lat < rightLatlng.latitude && dto.lng > leftlatlng.longitude && dto.lng < rightLatlng.longitude) {
            val weatherDto = WeatherDto()
            weatherDto.cityId = dto.areaId
            weatherDto.cityName = dto.name
            weatherDto.lat = dto.lat
            weatherDto.lng = dto.lng
            weatherDto.level = dto.level
            getWeathersInfo(weatherDto)
        }
    }

    /**
     * 获取站点天气信息
     */
    private fun getWeathersInfo(dto: WeatherDto) {
        if (TextUtils.isEmpty(dto.cityId)) {
            return
        }
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url("https://videoshfcx.tianqi.cn/dav_tqwy/ty_weather/data/${dto.cityId}.html").build(), object : Callback {
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
                                //15天预报
                                if (!obj.isNull("f")) {
                                    val f = obj.getJSONObject("f")
                                    val f0 = f.getString("f0")
                                    if (!TextUtils.isEmpty(f0)) {
                                        try {
                                            tvTime!!.text = sdf2.format(sdf1.parse(f0))
                                            tvTime!!.visibility = View.VISIBLE
                                        } catch (e: ParseException) {
                                            e.printStackTrace()
                                        }
                                    }
                                    if (!f.isNull("f1")) {
                                        val f1 = f.getJSONArray("f1")
                                        val weeklyObj = f1.getJSONObject(0)
                                        //晚上
                                        dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"))
                                        dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))))
                                        dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"))
                                        dto.lowWindDir = Integer.valueOf(weeklyObj.getString("ff"))
                                        dto.lowWindForce = Integer.valueOf(weeklyObj.getString("fh"))
                                        //白天
                                        dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"))
                                        dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))))
                                        dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"))
                                        dto.highWindDir = Integer.valueOf(weeklyObj.getString("fe"))
                                        dto.highWindForce = Integer.valueOf(weeklyObj.getString("fg"))
                                        val options = MarkerOptions()
                                        options.title(dto.cityName + "," + dto.highPheCode + "," + dto.highTemp + "," + dto.highWindDir + "," + dto.highWindForce
                                                + "," + dto.lowPheCode + "," + dto.lowTemp + "," + dto.lowWindDir + "," + dto.lowWindForce + "," + dto.cityId)
                                        options.snippet(dto.level)
                                        options.anchor(0.5f, 1.0f)
                                        options.position(LatLng(dto.lat, dto.lng))
                                        val currentHour = Integer.valueOf(sdf3.format(Date()))
                                        if (currentHour in 5..17) {
                                            options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.highPheCode)))
                                        } else {
                                            options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.lowPheCode)))
                                        }
                                        val marker = aMap!!.addMarker(options)
                                        markerMap[dto.cityId] = marker
                                        markerExpandAnimation(marker)
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

    /**
     * 给marker添加文字
     * @return
     */
    private fun getTextBitmap(weatherCode: Int): View? {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_marker, null) ?: return null
        view.ivMarker.setImageBitmap(WeatherUtil.getDayBitmap(this, weatherCode))
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

    override fun onCameraChange(arg0: CameraPosition?) {}

    override fun onCameraChangeFinish(arg0: CameraPosition) {
        val leftPoint = Point(0, CommonUtil.heightPixels(this))
        val rightPoint = Point(CommonUtil.widthPixels(this), 0)
        leftlatlng = aMap!!.projection.fromScreenLocation(leftPoint)
        rightLatlng = aMap!!.projection.fromScreenLocation(rightPoint)
        zoom = arg0.zoom
        handler.removeMessages(1001)
        val msg = handler.obtainMessage()
        msg.what = 1001
        handler.sendMessageDelayed(msg, 500)
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1001 -> {
                    switchMarkers()
                    addMarkers()
                }
            }
        }
    }

    override fun onMapClick(arg0: LatLng?) {
        if (clickMarker != null && clickMarker!!.isInfoWindowShown) {
            clickMarker!!.hideInfoWindow()
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            clickMarker = marker
            if (clickMarker!!.isInfoWindowShown) {
                clickMarker!!.hideInfoWindow()
            } else {
                marker.showInfoWindow()
            }
        }
        return true
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View? {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_marker_info, null)
        val result = marker.title.split(",").toTypedArray()
        view.tvName.text = result[0]
        var phe = getString(WeatherUtil.getWeatherId(Integer.valueOf(result[1]))) + "~" + getString(WeatherUtil.getWeatherId(Integer.valueOf(result[5])))
        if (TextUtils.equals(result[1], result[5])) {
            phe = getString(WeatherUtil.getWeatherId(Integer.valueOf(result[1])))
        }
        val temp = result[2] + "~" + result[6] + "℃"
        val windDir = getString(WeatherUtil.getWindDirection(Integer.valueOf(result[3])))
        val windForce = WeatherUtil.getFactWindForce(Integer.valueOf(result[4]))
        view.tvInfo.text = phe + "\n" + temp + "\n" + windDir + windForce
        return view
    }

    override fun onInfoWindowClick(marker: Marker) {
        val result = marker.title.split(",").toTypedArray()
        val intent = Intent(this, ForecastActivity::class.java)
        intent.putExtra("cityName", result[0])
        intent.putExtra("cityId", result[9])
        startActivity(intent)
    }

    override fun onMapScreenShot(bitmap1: Bitmap?) { //bitmap1为地图截屏
        val bitmap4 = BitmapFactory.decodeResource(resources, R.drawable.shawn_legend_share_portrait)
        val bitmap = CommonUtil.mergeBitmap(this, bitmap1, bitmap4, false)
        CommonUtil.clearBitmap(bitmap1)
        CommonUtil.clearBitmap(bitmap4)
        CommonUtil.share(this, bitmap)
    }

    override fun onMapScreenShot(arg0: Bitmap?, arg1: Int) {}

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.ivShare -> aMap!!.getMapScreenShot(this)
            R.id.ivMapSearch -> startActivity(Intent(this, CityActivity::class.java))
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

    //需要申请的所有权限
    var allPermissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    //拒绝的权限集合
    var deniedList: MutableList<String> = ArrayList()

    /**
     * 申请定位权限
     */
    private fun checkAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            init()
        } else {
            deniedList.clear()
            for (permission in allPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) !== PackageManager.PERMISSION_GRANTED) {
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
                    AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用电话权限、存储权限，是否前往设置？")
                }
            } else {
                for (permission in permissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission!!)) {
                        AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用电话权限、存储权限，是否前往设置？")
                        break
                    }
                }
            }
        }
    }

}
