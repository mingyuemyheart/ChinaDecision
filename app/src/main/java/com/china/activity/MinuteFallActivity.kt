package com.china.activity

import android.Manifest
import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnMapClickListener
import com.amap.api.maps.AMap.OnMapScreenShotListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.china.R
import com.china.common.CONST
import com.china.dto.MinuteFallDto
import com.china.dto.WeatherDto
import com.china.manager.CaiyunManager
import com.china.manager.CaiyunManager.RadarListener
import com.china.utils.AuthorityUtil
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import com.china.view.MinuteFallView
import kotlinx.android.synthetic.main.activity_minute_fall.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 分钟级降水估测
 */
class MinuteFallActivity : BaseActivity(), OnClickListener, AMapLocationListener,
        OnMapClickListener, AMap.OnMarkerClickListener, OnGeocodeSearchListener, OnMapScreenShotListener {

    private var aMap: AMap? = null
    private var zoom = 3.7f
    private val dataList: ArrayList<MinuteFallDto> = ArrayList()
    private var mOverlay: GroundOverlay? = null
    private var mRadarManager: CaiyunManager? = null
    private var mRadarThread: RadarThread? = null
    private var clickMarker: Marker? = null
    private var geocoderSearch: GeocodeSearch? = null
    private var locationLatLng = LatLng(39.904030, 116.407526)
    private var isShowDetail = false
    private val radarMarkers: ArrayList<Marker> = ArrayList()
    private var isShowRadar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minute_fall)
        initMap(savedInstanceState)
        checkAuthority()
    }

    private fun init() {
        initWidget()
    }

    private fun initMap(bundle: Bundle?) {
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.926628, 105.178100), zoom))
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMapClickListener(this)
        aMap!!.setOnMarkerClickListener(this)
    }

    private fun initWidget() {
        showDialog()
        llBack.setOnClickListener(this)
        ivShare.setOnClickListener(this)
        ivShare.visibility = View.VISIBLE
        ivRank.setOnClickListener(this)
        ivSwitch.setOnClickListener(this)
        ivRadar.setOnClickListener(this)
        ivLocation.setOnClickListener(this)
        clChart.setOnClickListener(this)
        ivPlay.setOnClickListener(this)
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mRadarThread!!.startTracking()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (mRadarThread != null) {
                    mRadarThread!!.setCurrent(seekBar!!.progress)
                    mRadarThread!!.stopTracking()
                }
            }
        })

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }

        geocoderSearch = GeocodeSearch(this)
        geocoderSearch!!.setOnGeocodeSearchListener(this)
        mRadarManager = CaiyunManager(this)

        okHttpCaiyun()
        if (CommonUtil.isLocationOpen(this)) {
            startLocation()
        } else {
            addLocationMarker(locationLatLng)
        }
    }

    /**
     * 开始定位
     */
    private fun startLocation() {
        val mLocationOption = AMapLocationClientOption()//初始化定位参数
        val mLocationClient = AMapLocationClient(this)//初始化定位
        mLocationOption.locationMode = AMapLocationMode.Hight_Accuracy//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.isNeedAddress = true//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.isOnceLocation = true//设置是否只定位一次,默认为false
        mLocationOption.isMockEnable = false//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.interval = 2000//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption)//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this)
        mLocationClient.startLocation()//启动定位
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            locationLatLng = LatLng(amapLocation.latitude, amapLocation.longitude)
            addLocationMarker(locationLatLng)
        }
    }

    /**
     * 添加定位标记
     */
    private fun addLocationMarker(latLng: LatLng) {
        if (latLng == null) {
            return
        }
        val options = MarkerOptions()
        options.position(latLng)
        options.anchor(0.5f, 1.0f)
        val bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(resources, R.drawable.icon_map_location),
                CommonUtil.dip2px(this, 21.0f).toInt(), CommonUtil.dip2px(this, 32.0f).toInt())
        if (bitmap != null) {
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        } else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_location))
        }
        if (clickMarker != null) {
            clickMarker!!.remove()
        }
        clickMarker = aMap!!.addMarker(options)
        clickMarker!!.isClickable = false

        okHttpMinute(latLng.longitude, latLng.latitude)
        searchAddrByLatLng(latLng.latitude, latLng.longitude)
    }

    private fun okHttpMinute(lng: Double, lat: Double) {
        val url = "http://api.caiyunapp.com/v2/HyTVV5YAkoxlQ3Zd/$lng,$lat/forecast"
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
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obje = JSONObject(result)
                                if (!obje.isNull("result")) {
                                    val obj = obje.getJSONObject("result")
                                    if (!obj.isNull("minutely")) {
                                        val objMin = obj.getJSONObject("minutely")
                                        if (!objMin.isNull("description")) {
                                            val rain = objMin.getString("description")
                                            if (!TextUtils.isEmpty(rain)) {
                                                tvRain.text = rain.replace("小彩云", "")
                                                tvRain.visibility = View.VISIBLE
                                            } else {
                                                tvRain.visibility = View.GONE
                                            }
                                        }
                                        if (!objMin.isNull("precipitation_2h")) {
                                            val array = objMin.getJSONArray("precipitation_2h")
                                            val minuteList: ArrayList<WeatherDto> = ArrayList()
                                            for (i in 0 until array.length()) {
                                                val dto = WeatherDto()
                                                dto.minuteFall = array.getDouble(i).toFloat()
                                                minuteList.add(dto)
                                            }

                                            val minuteFallView = MinuteFallView(this@MinuteFallActivity)
                                            minuteFallView.setData(minuteList, tvRain.text.toString())
                                            llContainer.removeAllViews()
                                            llContainer.addView(minuteFallView, CommonUtil.widthPixels(this@MinuteFallActivity), CommonUtil.dip2px(this@MinuteFallActivity, 120.0f).toInt())
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
        }).start()
    }

    override fun onMapClick(latLng: LatLng?) {
        tvAddr.text = ""
        tvRain.text = ""
        addLocationMarker(latLng!!)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null && marker != clickMarker) {
            val intent = Intent(this, RadarDetailActivity::class.java)
            intent.putExtra("radarName", marker.title)
            intent.putExtra("radarCode", marker.snippet)
            startActivity(intent)
        }
        return true
    }

    /**
     * 通过经纬度获取地理位置信息
     * @param lat
     * @param lng
     */
    private fun searchAddrByLatLng(lat: Double, lng: Double) {
        //latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
        val query = RegeocodeQuery(LatLonPoint(lat, lng), 200.0f, GeocodeSearch.AMAP)
        geocoderSearch!!.getFromLocationAsyn(query)
    }

    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
    }

    override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result?.regeocodeAddress != null && result.regeocodeAddress.formatAddress != null) {
                var addr: String? = null
                addr = if (result.regeocodeAddress.city.contains(result.regeocodeAddress.province)) {
                    result.regeocodeAddress.city + result.regeocodeAddress.district
                } else {
                    result.regeocodeAddress.province + result.regeocodeAddress.city + result.regeocodeAddress.district
                }
                tvAddr.text = addr
            }
        }
    }

    /**
     * 获取彩云数据
     */
    private fun okHttpCaiyun() {
        Thread(Runnable {
            val url = "http://api.tianqi.cn:8070/v1/img.py"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            val obj = JSONObject(result)
                            if (!obj.isNull("status")) {
                                if (obj.getString("status") == "ok") {
                                    if (!obj.isNull("radar_img")) {
                                        val array = JSONArray(obj.getString("radar_img"))
                                        for (i in 0 until array.length()) {
                                            val array0 = array.getJSONArray(i)
                                            val dto = MinuteFallDto()
                                            dto.imgUrl = array0.optString(0)
                                            dto.time = array0.optLong(1)
                                            val itemArray = array0.getJSONArray(2)
                                            dto.p1 = itemArray.optDouble(0)
                                            dto.p2 = itemArray.optDouble(1)
                                            dto.p3 = itemArray.optDouble(2)
                                            dto.p4 = itemArray.optDouble(3)
                                            dataList.add(dto)
                                        }
                                        if (dataList.size > 0) {
                                            startDownLoadImgs(dataList)
                                        }
                                    }
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }).start()
    }

    private fun startDownLoadImgs(list: List<MinuteFallDto>) {
        if (mRadarThread != null) {
            mRadarThread!!.cancel()
            mRadarThread = null
        }
        mRadarManager!!.loadImagesAsyn(list, object : RadarListener {
            override fun onResult(result: Int, images: List<MinuteFallDto>) {
				runOnUiThread {
					cancelDialog()
					llSeekBar.visibility = View.VISIBLE
				}
                if (result == RadarListener.RESULT_SUCCESSED) {
                    val radar = images[images.size - 1]
                    drawRadar(radar, 100, 100)
                }
            }
            override fun onProgress(url: String, progress: Int) {
            }
        })
    }

    private fun drawRadar(dto: MinuteFallDto, progress: Int, max: Int) {
        runOnUiThread {
            if (dto.getPath() != null) {
                val bitmap = BitmapFactory.decodeFile(dto.getPath())
                if (bitmap != null) {
                    showRadar(bitmap, dto.p1, dto.p2, dto.p3, dto.p4)
                }
            }
            changeProgress(dto.time, progress, max)
        }
    }

    private fun showRadar(bitmap: Bitmap, p1: Double, p2: Double, p3: Double, p4: Double) {
        val fromView = BitmapDescriptorFactory.fromBitmap(bitmap)
        val bounds = LatLngBounds.Builder()
                .include(LatLng(p3, p2))
                .include(LatLng(p1, p4))
                .build()
        if (mOverlay == null) {
            mOverlay = aMap!!.addGroundOverlay(GroundOverlayOptions()
                    .anchor(0.5f, 0.5f)
                    .positionFromBounds(bounds)
                    .image(fromView)
                    .transparency(0.0f))
        } else {
            mOverlay!!.setImage(null)
            mOverlay!!.setPositionFromBounds(bounds)
            mOverlay!!.setImage(fromView)
        }
        aMap!!.runOnDrawFrame()
    }

    private inner class RadarThread(images: ArrayList<MinuteFallDto>) : Thread() {

        var currentState: Int
        private var index: Int
        private val count: Int
        private var isTracking: Boolean
        private val STATE_NONE = 0
        val STATE_PLAYING = 1
        val STATE_PAUSE = 2
        private val STATE_CANCEL = 3
        private var images: ArrayList<MinuteFallDto>? = null

        init {
            this.images = images
            count = images.size
            index = 0
            currentState = STATE_NONE
            isTracking = false
        }

        override fun run() {
            super.run()
            currentState = STATE_PLAYING
            while (true) {
                if (currentState == STATE_CANCEL) {
                    break
                }
                if (currentState == STATE_PAUSE) {
                    continue
                }
                if (isTracking) {
                    continue
                }
                sendRadar()
                try {
                    sleep(200)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        private fun sendRadar() {
            if (index >= count || index < 0) {
                index = 0
            } else {
                val radar: MinuteFallDto = images!![index]
                drawRadar(radar, index++, count - 1)
            }
        }

        fun cancel() {
            currentState = STATE_CANCEL
        }

        fun pause() {
            currentState = STATE_PAUSE
        }

        fun play() {
            currentState = STATE_PLAYING
        }

        fun setCurrent(index: Int) {
            this.index = index
        }

        fun startTracking() {
            isTracking = true
        }

        fun stopTracking() {
            isTracking = false
            if (currentState == STATE_PAUSE) {
                sendRadar()
            }
        }
    }

    private fun changeProgress(time: Long, progress: Int, max: Int) {
        if (seekBar != null) {
            seekBar.max = max
            seekBar.progress = progress
        }
        val sdf = SimpleDateFormat("HH:mm", Locale.CHINA)
        val value = time.toString() + "000"
        val date = Date(java.lang.Long.valueOf(value))
        tvTime.text = sdf.format(date)
    }

    /**
     * 隐藏或显示ListView的动画
     */
    private fun hideOrShowListViewAnimator(view: View, startValue: Int, endValue: Int) {
        //1.设置属性的初始值和结束值
        val mAnimator = ValueAnimator.ofInt(0, 100)
        //2.为目标对象的属性变化设置监听器
        mAnimator.addUpdateListener { animation ->
            val animatorValue = animation.animatedValue as Int
            val fraction = animatorValue / 100f
            val mEvaluator = IntEvaluator()
            //3.使用IntEvaluator计算属性值并赋值给ListView的高
            view.layoutParams.height = mEvaluator.evaluate(fraction, startValue, endValue)
            Log.e("height", view.layoutParams.height.toString())
            view.requestLayout()
        }
        //4.为ValueAnimator设置LinearInterpolator
        mAnimator.interpolator = LinearInterpolator()
        //5.设置动画的持续时间
        mAnimator.duration = 200
        //6.为ValueAnimator设置目标对象并开始执行动画
        mAnimator.setTarget(view)
        mAnimator.start()
    }

    private fun clickRainChart() {
        val height = CommonUtil.dip2px(this, 120f).toInt()
        isShowDetail = !isShowDetail
        if (isShowDetail) {
            ivArrow.setImageResource(R.drawable.shawn_icon_animation_up)
            hideOrShowListViewAnimator(llContainer, 0, height)
            llContainer.visibility = View.VISIBLE
        } else {
            ivArrow.setImageResource(R.drawable.shawn_icon_animation_down)
            hideOrShowListViewAnimator(llContainer, height, 0)
        }
    }

    /**
     * 切换雷达站点显示、隐藏
     */
    private fun switchRadars() {
        isShowRadar = !isShowRadar
        if (isShowRadar) {
            ivRadar.setImageResource(R.drawable.shawn_icon_minute_radar_on)
            if (radarMarkers.size <= 0) {
                val radar = CommonUtil.getFromAssets(this, "json/nation_radars.json")
                if (TextUtils.isEmpty(radar)) {
                    return
                }
                Thread(Runnable {
                    try {
                        val array = JSONArray(radar)
                        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        for (i in 0 until array.length()) {
                            val itemObj = array.getJSONObject(i)
                            val lat = itemObj.getDouble("lat")
                            val lng = itemObj.getDouble("lon")
                            val radarName = itemObj.getString("name")
                            val radarCode = itemObj.getString("id")
                            val options = MarkerOptions()
                            options.title(radarName)
                            options.snippet(radarCode)
                            options.anchor(0.5f, 0.5f)
                            options.position(LatLng(lat, lng))
                            val mView = inflater.inflate(R.layout.shawn_radar_marker_icon, null)
                            val ivMarker = mView.findViewById<ImageView>(R.id.ivMarker)
                            ivMarker.setImageResource(R.drawable.shawn_icon_map_radar)
                            options.icon(BitmapDescriptorFactory.fromView(mView))
                            val marker = aMap!!.addMarker(options)
                            marker.isVisible = true
                            radarMarkers.add(marker)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }).start()
            } else {
                for (marker in radarMarkers) {
                    marker.isVisible = true
                }
            }
        } else {
            ivRadar.setImageResource(R.drawable.shawn_icon_minute_radar_off)
            for (marker in radarMarkers) {
                marker.isVisible = false
            }
        }
    }

    override fun onMapScreenShot(bitmap1: Bitmap?) { //bitmap1为地图截屏
        if (llContainer.visibility == View.VISIBLE) {
            val bitmap2 = CommonUtil.captureView(clRain)
            val bitmap3 = CommonUtil.captureView(llContainer)
            val bitmap4 = CommonUtil.mergeBitmap(this, bitmap2, bitmap3, false)
            CommonUtil.clearBitmap(bitmap2)
            CommonUtil.clearBitmap(bitmap3)
            val bitmap5 = CommonUtil.mergeBitmap(this, bitmap1, bitmap4, true)
            CommonUtil.clearBitmap(bitmap1)
            CommonUtil.clearBitmap(bitmap4)
            val bitmap6 = CommonUtil.captureView(clShare)
            val bitmap7 = CommonUtil.mergeBitmap(this, bitmap5, bitmap6, true)
            CommonUtil.clearBitmap(bitmap5)
            CommonUtil.clearBitmap(bitmap6)
            val bitmap8 = BitmapFactory.decodeResource(resources, R.drawable.legend_share_portrait)
            val bitmap = CommonUtil.mergeBitmap(this, bitmap7, bitmap8, false)
            CommonUtil.clearBitmap(bitmap7)
            CommonUtil.clearBitmap(bitmap8)
            CommonUtil.share(this, bitmap)
        } else {
            val bitmap2 = CommonUtil.captureView(clChart)
            val bitmap3 = CommonUtil.mergeBitmap(this, bitmap1, bitmap2, true)
            CommonUtil.clearBitmap(bitmap1)
            CommonUtil.clearBitmap(bitmap2)
            val bitmap4 = CommonUtil.captureView(clShare)
            val bitmap5 = CommonUtil.mergeBitmap(this, bitmap3, bitmap4, true)
            CommonUtil.clearBitmap(bitmap3)
            CommonUtil.clearBitmap(bitmap4)
            val bitmap6 = BitmapFactory.decodeResource(resources, R.drawable.legend_share_portrait)
            val bitmap = CommonUtil.mergeBitmap(this, bitmap5, bitmap6, false)
            CommonUtil.clearBitmap(bitmap5)
            CommonUtil.clearBitmap(bitmap6)
            CommonUtil.share(this, bitmap)
        }
    }

    override fun onMapScreenShot(arg0: Bitmap?, arg1: Int) {}

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> {
                finish()
            }
            R.id.ivRadar -> switchRadars()
            R.id.ivSwitch -> if (aMap!!.mapType == AMap.MAP_TYPE_NORMAL) {
                aMap!!.mapType = AMap.MAP_TYPE_SATELLITE
                ivSwitch.setImageResource(R.drawable.icon_switch_map_on)
            } else {
                aMap!!.mapType = AMap.MAP_TYPE_NORMAL
                ivSwitch.setImageResource(R.drawable.icon_switch_map_off)
            }
            R.id.ivLocation -> {
                if (zoom < 10f) {
                    zoom = 10.0f
                    ivLocation.setImageResource(R.drawable.icon_location_on)
                } else {
                    zoom = 3.7f
                    ivLocation.setImageResource(R.drawable.icon_location_off)
                }
                aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, zoom))
                addLocationMarker(locationLatLng)
            }
            R.id.ivRank -> if (ivLegend.visibility == View.VISIBLE) {
                ivLegend.visibility = View.INVISIBLE
                ivRank.setImageResource(R.drawable.icon_legend_off)
            } else {
                ivLegend.visibility = View.VISIBLE
                ivRank.setImageResource(R.drawable.icon_legend_on)
            }
            R.id.ivPlay -> if (mRadarThread != null && mRadarThread!!.currentState == mRadarThread!!.STATE_PLAYING) {
                mRadarThread!!.pause()
                ivPlay.setImageResource(R.drawable.icon_play)
            } else if (mRadarThread != null && mRadarThread!!.currentState == mRadarThread!!.STATE_PAUSE) {
                mRadarThread!!.play()
                ivPlay.setImageResource(R.drawable.icon_pause)
            } else if (mRadarThread == null) {
                ivPlay.setImageResource(R.drawable.icon_pause)
                if (mRadarThread != null) {
                    mRadarThread!!.cancel()
                    mRadarThread = null
                }
                mRadarThread = RadarThread(dataList)
                mRadarThread!!.start()
            }
            R.id.ivShare -> aMap!!.getMapScreenShot(this)
            R.id.clChart, R.id.ivArrow -> clickRainChart()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView.onResume()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView.onPause()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (mapView != null) {
            mapView.onSaveInstanceState(outState)
        }
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null) {
            mapView.onDestroy()
        }
        if (mRadarManager != null) {
            mRadarManager!!.onDestory()
        }
        if (mRadarThread != null) {
            mRadarThread!!.cancel()
            mRadarThread = null
        }
    }

    //需要申请的所有权限
    private val allPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

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
                    AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用您的位置权限、存储权限，是否前往设置？")
                }
            } else {
                for (permission in permissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission!!)) {
                        AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用您的位置权限、存储权限，是否前往设置？")
                        break
                    }
                }
            }
        }
    }

}
