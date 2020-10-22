package com.china.activity

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.media.ThumbnailUtils
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.*
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.ScaleAnimation
import com.china.R
import com.china.adapter.WarningAdapter
import com.china.adapter.WarningStatisticAdapter
import com.china.common.CONST
import com.china.common.ColumnData
import com.china.dto.TyphoonDto
import com.china.dto.WarningDto
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import com.china.utils.SecretUrlUtil
import com.china.view.ArcMenu
import com.china.view.ArcMenu.OnMenuItemClickListener
import kotlinx.android.synthetic.main.activity_warning.*
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
 * 预警
 */
class WarningActivity : BaseActivity(), OnClickListener, AMapLocationListener, OnMapClickListener,
        OnMarkerClickListener, InfoWindowAdapter, OnCameraChangeListener, OnMapScreenShotListener {

    private var locationLatLng: LatLng? = LatLng(39.904030, 116.407526)
    private var aMap: AMap? = null
    private var zoom = 3.7f
    private var zoom1 = 6.0f
    private var zoom2 = 8.0f
    private var blue = true
    private var yellow = true
    private var orange = true
    private var red = true
    private val warningList: MutableList<WarningDto> = ArrayList()
    private val nationList: MutableList<WarningDto?> = ArrayList()
    private val markerMap: MutableMap<String, Marker> = LinkedHashMap() //按html区分
    private var isExpandMap = false //是否放大地图
    private var leftlatlng = LatLng(-16.305714763804854, 75.13831436634065)
    private var rightLatlng = LatLng(63.681687310440864, 135.21788656711578)
    private var locationMarker: Marker? = null
    private var selectMarker: Marker? = null
    private var isShowPrompt = true
    private val sdf1 = SimpleDateFormat("yyyy", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyyMMddHH", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("MM月dd日HH时", Locale.CHINA)
    private val sdf5 = SimpleDateFormat("dd日HH时", Locale.CHINA)
    private var columnId = "" //栏目id

    //预警统计列表
    private var statisticAdapter: WarningStatisticAdapter? = null
    private val statisticList: MutableList<WarningDto> = ArrayList()
    private val centrolList: MutableList<WarningDto?> = ArrayList()

    //国家级预警图层
    private var layerTime12: String? = null
    private var layerTime24: String? = null
    private val nationMap = HashMap<String, String>()
    private val flags: MutableList<Boolean> = ArrayList()
    private val warningType1 = "fog"
    private var warningType2 = "baoyu"
    private var warningType3 = "shachen"
    private var warningType4 = "daxue"
    private var warningType5 = "gaowen"
    private val warningType7 = "lengkongqi"
    private var warningType8 = "lengkongqi"
    private var warningType9 = "wind"
    private var warningType10 = "thunderstorm"
    private val polyline11: MutableList<Polyline> = ArrayList()
    private val polyline12: MutableList<Polyline> = ArrayList()
    private val polygons13: MutableList<Polygon> = ArrayList()
    private val polyline21: MutableList<Polyline> = ArrayList()
    private val polyline22: MutableList<Polyline> = ArrayList()
    private val polygons23: MutableList<Polygon> = ArrayList()
    private val polyline31: MutableList<Polyline> = ArrayList()
    private val polyline32: MutableList<Polyline> = ArrayList()
    private val polygons33: MutableList<Polygon> = ArrayList()
    private val polyline41: MutableList<Polyline> = ArrayList()
    private val polyline42: MutableList<Polyline> = ArrayList()
    private val polygons43: MutableList<Polygon> = ArrayList()
    private val polyline51: MutableList<Polyline> = ArrayList()
    private val polyline52: MutableList<Polyline> = ArrayList()
    private val polygons53: MutableList<Polygon> = ArrayList()
    private val polyline71: MutableList<Polyline> = ArrayList()
    private val polyline72: MutableList<Polyline> = ArrayList()
    private val polygons73: MutableList<Polygon> = ArrayList()
    private val polyline81: MutableList<Polyline> = ArrayList()
    private val polyline82: MutableList<Polyline> = ArrayList()
    private val polygons83: MutableList<Polygon> = ArrayList()
    private val polyline91: MutableList<Polyline> = ArrayList()
    private val polyline92: MutableList<Polyline> = ArrayList()
    private val polygons93: MutableList<Polygon> = ArrayList()
    private val polyline101: MutableList<Polyline> = ArrayList()
    private val polyline102: MutableList<Polyline> = ArrayList()
    private val polygons103: MutableList<Polygon> = ArrayList()
    private val typhoonList: MutableList<TyphoonDto> = ArrayList()
    private val typhoonMarkers: MutableList<Marker> = ArrayList() //台风markers

    private val markerType1 = "WARNING"
    private var markerType2 = "TYPHOON" //marker类型
    private var mReceiver: MyBroadCastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning)
        initBroadCast()
        initAmap(savedInstanceState)
        initWidget()
        initListView()
    }

    /**
     * 初始化高德地图
     */
    private fun initAmap(bundle: Bundle?) {
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.926628, 105.178100), zoom))
        aMap!!.uiSettings.isMyLocationButtonEnabled = false // 设置默认定位按钮是否显示
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMapClickListener(this)
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setInfoWindowAdapter(this)
        aMap!!.setOnCameraChangeListener(this)
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivShare.setOnClickListener(this)
        ivShare.visibility = View.VISIBLE
        arcMenu.onMenuItemClickListener = arcMenuListener
        ivLocation.setOnClickListener(this)
        ivRefresh.setOnClickListener(this)
        ivList.setOnClickListener(this)
        ivStatistic.setOnClickListener(this)
        ivNews.setOnClickListener(this)
        reWarningStatistic.setOnClickListener(this)
        ivArrow.setOnClickListener(this)
        tvNation.setOnClickListener(this)
        ivGuide.setOnClickListener(this)
        ivCentral.setOnClickListener(this)
        ivIntrol.setOnClickListener(this)
        tvCentrolCount.setOnClickListener(this)
        iv1.setOnClickListener(this)
        iv2.setOnClickListener(this)
        iv3.setOnClickListener(this)
        iv4.setOnClickListener(this)
        iv5.setOnClickListener(this)
        iv6.setOnClickListener(this)
        iv7.setOnClickListener(this)
        iv8.setOnClickListener(this)
        iv9.setOnClickListener(this)
        iv10.setOnClickListener(this)
        for (i in 0..9) {
            flags.add(false)
        }
        CommonUtil.showGuidePage(this, this.javaClass.name, ivGuide)
        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (title != null) {
            tvTitle.text = title
        }
        refresh()
    }

    private fun refresh() {
        showDialog()
        Thread(Runnable {
            startLocation()
            okHttpWarning()
            OkHttpTyphoonList()
        }).start()
    }

    /**
     * 开始定位
     */
    private fun startLocation() {
        val mLocationOption = AMapLocationClientOption() //初始化定位参数
        val mLocationClient = AMapLocationClient(this) //初始化定位
        mLocationOption.locationMode = AMapLocationMode.Hight_Accuracy //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.isNeedAddress = true //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.isOnceLocation = true //设置是否只定位一次,默认为false
        mLocationOption.isMockEnable = false //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.interval = 2000 //设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption) //给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this)
        mLocationClient.startLocation() //启动定位
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            locationLatLng = LatLng(amapLocation.latitude, amapLocation.longitude)
        }
        addLocationMarker()
    }

    /**
     * 添加定位标记
     */
    private fun addLocationMarker() {
        if (locationLatLng == null) {
            return
        }
        val options = MarkerOptions()
        options.position(locationLatLng)
        options.anchor(0.5f, 1.0f)
        val bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(resources, R.drawable.icon_map_location),
                CommonUtil.dip2px(this, 21f).toInt(), CommonUtil.dip2px(this, 32f).toInt())
        if (bitmap != null) {
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        } else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_location))
        }
        if (locationMarker != null) {
            locationMarker!!.remove()
        }
        locationMarker = aMap!!.addMarker(options)
        locationMarker!!.isClickable = false
    }

    /**
     * 获取预警信息
     */
    private fun okHttpWarning() {
        val url = "https://decision-admin.tianqi.cn/Home/work2019/getwarns"
        OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    return
                }
                val result = response.body!!.string()
                runOnUiThread(Runnable {
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            warningList.clear()
                            nationList.clear()
                            centrolList.clear()
                            val `object` = JSONObject(result)
                            if (!`object`.isNull("data")) {
                                val jsonArray = `object`.getJSONArray("data")
                                for (i in 0 until jsonArray.length()) {
                                    val tempArray = jsonArray.getJSONArray(i)
                                    val dto = WarningDto()
                                    dto.html = tempArray.getString(1)
                                    val array = dto.html.split("-").toTypedArray()
                                    val item0 = array[0]
                                    val item1 = array[1]
                                    val item2 = array[2]
                                    dto.item0 = item0
                                    dto.provinceId = item0.substring(0, 2)
                                    dto.type = item2.substring(0, 5)
                                    dto.color = item2.substring(5, 7)
                                    dto.time = item1
                                    dto.lng = tempArray.getDouble(2)
                                    dto.lat = tempArray.getDouble(3)
                                    dto.name = tempArray.getString(0)
                                    if (!dto.name.contains("解除")) {
                                        warningList.add(dto)
                                    }
                                    if (!TextUtils.isEmpty(item0)) {
                                        if (!dto.name.contains("解除")) {
                                            if (TextUtils.equals(item0, "000000")) {
                                                nationList.add(dto)
                                                nationMap[dto.type] = dto.type
                                            }
                                            if (dto.name.startsWith("中央气象台")) {
                                                centrolList.add(dto)
                                            }
                                        }
                                    }
                                }
                                if (centrolList.size > 0) {
                                    ivCentral.visibility = View.VISIBLE
                                    tvCentrolCount.text = centrolList.size.toString() + ""
                                    tvCentrolCount.visibility = View.VISIBLE
                                } else {
                                    ivCentral.visibility = View.GONE
                                    tvCentrolCount.text = ""
                                    tvCentrolCount.visibility = View.GONE
                                }
                                addWarningMarkers()
                                try {
                                    val count = warningList.size.toString() + ""
                                    if (TextUtils.equals(count, "0")) {
                                        var time = ""
                                        if (!`object`.isNull("time")) {
                                            val t = `object`.getLong("time")
                                            time = sdf3.format(Date(t * 1000))
                                        }
                                        tvWarningStatistic!!.text = time + ", " + "当前生效预警" + count + "条"
                                        ivList!!.visibility = View.GONE
                                        ivStatistic.visibility = View.GONE
                                        ivNews.visibility = View.GONE
                                        arcMenu!!.visibility = View.GONE
                                        reWarningStatistic!!.visibility = View.VISIBLE
                                        cancelDialog()
                                        return@Runnable
                                    }
                                    var time = ""
                                    if (!`object`.isNull("time")) {
                                        val t = `object`.getLong("time")
                                        time = sdf3.format(Date(t * 1000))
                                    }
                                    val str1 = "$time, 当前生效预警"
                                    val str2 = "条"
                                    val warningInfo = str1 + count + str2
                                    val builder = SpannableStringBuilder(warningInfo)
                                    val builderSpan1 = ForegroundColorSpan(ContextCompat.getColor(this@WarningActivity, R.color.text_color3))
                                    val builderSpan2 = ForegroundColorSpan(ContextCompat.getColor(this@WarningActivity, R.color.red))
                                    val builderSpan3 = ForegroundColorSpan(ContextCompat.getColor(this@WarningActivity, R.color.text_color3))
                                    builder.setSpan(builderSpan1, 0, str1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan2, str1.length, str1.length + count.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan3, str1.length + count.length, str1.length + count.length + str2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    tvWarningStatistic!!.text = builder
                                    ivList!!.visibility = View.VISIBLE
                                    ivStatistic.visibility = View.VISIBLE
                                    ivNews.visibility = View.VISIBLE
                                    arcMenu!!.visibility = View.VISIBLE
                                    reWarningStatistic!!.visibility = View.VISIBLE
                                    cancelDialog()
                                    if (nationList.size > 0) {
                                        tvNation.text = "国家级预警" + nationList.size + "条"
                                        tvNation.visibility = View.VISIBLE
                                        handlerNationWarning()
                                    } else {
                                        tvNation.visibility = View.GONE
                                    }

                                    //计算统计列表信息
                                    var rnation = 0
                                    var rpro = 0
                                    var rcity = 0
                                    var rdis = 0
                                    var onation = 0
                                    var opro = 0
                                    var ocity = 0
                                    var odis = 0
                                    var ynation = 0
                                    var ypro = 0
                                    var ycity = 0
                                    var ydis = 0
                                    var bnation = 0
                                    var bpro = 0
                                    var bcity = 0
                                    var bdis = 0
                                    var wnation = 0
                                    var wpro = 0
                                    var wcity = 0
                                    var wdis = 0
                                    for (i in warningList.indices) {
                                        val dto = warningList[i]
                                        if (TextUtils.equals(dto.color, "04")) {
                                            if (TextUtils.equals(dto.item0, "000000")) {
                                                rnation += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 4, dto.item0.length), "0000")) {
                                                rpro += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 2, dto.item0.length), "00")) {
                                                rcity += 1
                                            } else {
                                                rdis += 1
                                            }
                                        } else if (TextUtils.equals(dto.color, "03")) {
                                            if (TextUtils.equals(dto.item0, "000000")) {
                                                onation += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 4, dto.item0.length), "0000")) {
                                                opro += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 2, dto.item0.length), "00")) {
                                                ocity += 1
                                            } else {
                                                odis += 1
                                            }
                                        } else if (TextUtils.equals(dto.color, "02")) {
                                            if (TextUtils.equals(dto.item0, "000000")) {
                                                ynation += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 4, dto.item0.length), "0000")) {
                                                ypro += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 2, dto.item0.length), "00")) {
                                                ycity += 1
                                            } else {
                                                ydis += 1
                                            }
                                        } else if (TextUtils.equals(dto.color, "01")) {
                                            if (TextUtils.equals(dto.item0, "000000")) {
                                                bnation += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 4, dto.item0.length), "0000")) {
                                                bpro += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 2, dto.item0.length), "00")) {
                                                bcity += 1
                                            } else {
                                                bdis += 1
                                            }
                                        } else if (TextUtils.equals(dto.color, "05")) {
                                            if (TextUtils.equals(dto.item0, "000000")) {
                                                wnation += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 4, dto.item0.length), "0000")) {
                                                wpro += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 2, dto.item0.length), "00")) {
                                                wcity += 1
                                            } else {
                                                wdis += 1
                                            }
                                        }
                                    }
                                    statisticList.clear()
                                    var wDto = WarningDto()
                                    wDto.colorName = "预警" + warningList.size
                                    wDto.nationCount = "国家级" + (rnation + onation + ynation + bnation + wnation)
                                    wDto.proCount = "省级" + (rpro + opro + ypro + bpro + wpro)
                                    wDto.cityCount = "市级" + (rcity + ocity + ycity + bcity + wcity)
                                    wDto.disCount = "县级" + (rdis + odis + ydis + bdis + wdis)
                                    statisticList.add(wDto)
                                    wDto = WarningDto()
                                    wDto.colorName = "红" + (rnation + rpro + rcity + rdis)
                                    wDto.nationCount = rnation.toString() + ""
                                    wDto.proCount = rpro.toString() + ""
                                    wDto.cityCount = rcity.toString() + ""
                                    wDto.disCount = rdis.toString() + ""
                                    statisticList.add(wDto)
                                    wDto = WarningDto()
                                    wDto.colorName = "橙" + (onation + opro + ocity + odis)
                                    wDto.nationCount = onation.toString() + ""
                                    wDto.proCount = opro.toString() + ""
                                    wDto.cityCount = ocity.toString() + ""
                                    wDto.disCount = odis.toString() + ""
                                    statisticList.add(wDto)
                                    wDto = WarningDto()
                                    wDto.colorName = "黄" + (ynation + ypro + ycity + ydis)
                                    wDto.nationCount = ynation.toString() + ""
                                    wDto.proCount = ypro.toString() + ""
                                    wDto.cityCount = ycity.toString() + ""
                                    wDto.disCount = ydis.toString() + ""
                                    statisticList.add(wDto)
                                    wDto = WarningDto()
                                    wDto.colorName = "蓝" + (bnation + bpro + bcity + bdis)
                                    wDto.nationCount = bnation.toString() + ""
                                    wDto.proCount = bpro.toString() + ""
                                    wDto.cityCount = bcity.toString() + ""
                                    wDto.disCount = bdis.toString() + ""
                                    statisticList.add(wDto)
                                    wDto = WarningDto()
                                    wDto.colorName = "未知" + (wnation + wpro + wcity + wdis)
                                    wDto.nationCount = wnation.toString() + ""
                                    wDto.proCount = wpro.toString() + ""
                                    wDto.cityCount = wcity.toString() + ""
                                    wDto.disCount = wdis.toString() + ""
                                    statisticList.add(wDto)
                                    if (statisticAdapter != null) {
                                        statisticAdapter!!.notifyDataSetChanged()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                })
            }
        })
    }

    /**
     * 处理国家级预警
     */
    private fun handlerNationWarning() {
        if (nationMap.containsKey("11B17")) { //雾
            iv1!!.visibility = View.VISIBLE
        }
        if (nationMap.containsKey("11B03")) { //暴雨
            iv2.visibility = View.VISIBLE
        }
        if (nationMap.containsKey("11B07")) { //沙尘
            iv3.visibility = View.VISIBLE
        }
        if (nationMap.containsKey("11B04")) { //暴雪
            iv4.visibility = View.VISIBLE
        }
        if (nationMap.containsKey("11B09")) { //高温
            iv5.visibility = View.VISIBLE
        }
        if (nationMap.containsKey("11B06")) { //冷空气
            iv7.visibility = View.VISIBLE
        }
        if (nationMap.containsKey("11B05")) { //寒潮
            iv8.visibility = View.VISIBLE
        }
        if (nationMap.containsKey("11B23")) { //海上大风
            iv9.visibility = View.VISIBLE
        }
        if (nationMap.containsKey("11B31")) { //强对流
            iv10.visibility = View.VISIBLE
        }
    }

    /**
     * 在地图上添加marker
     */
    private fun addWarningMarkers() {
        Thread(Runnable {
            val markers: MutableMap<String, Marker> = LinkedHashMap() //为防止ConcurrentModificationException异常，待循环执行完毕后，再对markerMap进行修改
            for (dto in warningList) {
                if (markerMap.containsKey(dto.html)) {
                    val m = markerMap[dto.html]
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
    private fun addVisibleAreaMarker(dto: WarningDto, markers: MutableMap<String, Marker>) {
        if (TextUtils.equals(dto.item0, "000000")) { //国家级预警不绘制
            return
        }
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (dto.lat > leftlatlng.latitude && dto.lat < rightLatlng.latitude && dto.lng > leftlatlng.longitude && dto.lng < rightLatlng.longitude) {
            val optionsTemp = MarkerOptions()
            optionsTemp.title(dto.lat.toString() + "," + dto.lng + "," + dto.item0 + "," + dto.color)
            optionsTemp.snippet(markerType1)
            optionsTemp.anchor(0.5f, 0.5f)
            optionsTemp.position(LatLng(dto.lat, dto.lng))
            val mView = inflater.inflate(R.layout.warning_marker_icon, null)
            val ivMarker = mView.findViewById<ImageView>(R.id.ivMarker)
            var bitmap: Bitmap? = null
            if (dto.color == CONST.blue[0]) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + dto.type + CONST.blue[1] + CONST.imageSuffix)
            } else if (dto.color == CONST.yellow[0]) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + dto.type + CONST.yellow[1] + CONST.imageSuffix)
            } else if (dto.color == CONST.orange[0]) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + dto.type + CONST.orange[1] + CONST.imageSuffix)
            } else if (dto.color == CONST.red[0]) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + dto.type + CONST.red[1] + CONST.imageSuffix)
            }
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + "default" + CONST.imageSuffix)
            }
            ivMarker.setImageBitmap(bitmap)
            optionsTemp.icon(BitmapDescriptorFactory.fromView(mView))
            var marker: Marker? = null
            if (zoom <= zoom1) {
                if (dto.item0.endsWith("0000")) {
                    if (TextUtils.equals(dto.color, "01") && blue) {
                        marker = aMap!!.addMarker(optionsTemp)
                    } else if (TextUtils.equals(dto.color, "02") && yellow) {
                        marker = aMap!!.addMarker(optionsTemp)
                    } else if (TextUtils.equals(dto.color, "03") && orange) {
                        marker = aMap!!.addMarker(optionsTemp)
                    } else if (TextUtils.equals(dto.color, "04") && red) {
                        marker = aMap!!.addMarker(optionsTemp)
                    }
                }
            } else if (zoom > zoom1 && zoom <= zoom2) {
                if (dto.item0.endsWith("00")) {
                    if (TextUtils.equals(dto.color, "01") && blue) {
                        marker = aMap!!.addMarker(optionsTemp)
                    } else if (TextUtils.equals(dto.color, "02") && yellow) {
                        marker = aMap!!.addMarker(optionsTemp)
                    } else if (TextUtils.equals(dto.color, "03") && orange) {
                        marker = aMap!!.addMarker(optionsTemp)
                    } else if (TextUtils.equals(dto.color, "04") && red) {
                        marker = aMap!!.addMarker(optionsTemp)
                    }
                }
            } else {
                if (TextUtils.equals(dto.color, "01") && blue) {
                    marker = aMap!!.addMarker(optionsTemp)
                } else if (TextUtils.equals(dto.color, "02") && yellow) {
                    marker = aMap!!.addMarker(optionsTemp)
                } else if (TextUtils.equals(dto.color, "03") && orange) {
                    marker = aMap!!.addMarker(optionsTemp)
                } else if (TextUtils.equals(dto.color, "04") && red) {
                    marker = aMap!!.addMarker(optionsTemp)
                }
            }
            if (marker != null) {
                markers[dto.html] = marker
                markerExpandAnimation(marker)
            }
        }
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

    /**
     * 绘制预警markers
     */
    private fun switchWarningMarkers() {
        Thread(Runnable {
            for (html in markerMap.keys) {
                if (!TextUtils.isEmpty(html) && markerMap.containsKey(html)) {
                    val marker = markerMap[html]
                    val title = marker!!.title.split(",").toTypedArray()
                    val lat = marker.position.latitude
                    val lng = marker.position.longitude
                    val item0 = title[2]
                    val color = title[3]
                    if (zoom <= zoom1) {
                        if (!item0.endsWith("0000")) {
                            marker.remove()
                        }
                    } else if (zoom > zoom1 && zoom <= zoom2) {
                        if (!item0.endsWith("00")) {
                            marker.remove()
                        }
                    } else {
                    }
                    if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
                        //已经在可是范围内则不处理
                        if (TextUtils.equals(color, "01") && blue) {
                        } else if (TextUtils.equals(color, "02") && yellow) {
                        } else if (TextUtils.equals(color, "03") && orange) {
                        } else if (TextUtils.equals(color, "04") && red) {
                        } else {
                            marker.remove()
                        }
                    } else {
                        marker.remove()
                    }
                }
            }
        }).start()
    }

    /**
     * 筛选红橙黄蓝
     * @param color
     */
    private fun screenWarningMarkers(color: String) {
        Thread(Runnable {
            for (html in markerMap.keys) {
                if (!TextUtils.isEmpty(html) && markerMap.containsKey(html)) {
                    val marker = markerMap[html]
                    val title = marker!!.title.split(",").toTypedArray()
                    val item0 = title[2]
                    val warningColor = title[3]
                    if (zoom <= zoom1) {
                        if (!item0.endsWith("0000") || TextUtils.equals(warningColor, color)) {
                            marker.remove()
                        }
                    } else if (zoom > zoom1 && zoom <= zoom2) {
                        if (item0.endsWith("00") && TextUtils.equals(warningColor, color)) {
                            marker.remove()
                        }
                    } else {
                    }
                }
            }
        }).start()
    }

    override fun onMapClick(arg0: LatLng?) {
        if (selectMarker != null) {
            selectMarker!!.hideInfoWindow()
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null && marker !== locationMarker) {
            selectMarker = marker
            if (selectMarker!!.isInfoWindowShown) {
                selectMarker!!.hideInfoWindow()
            } else {
                selectMarker!!.showInfoWindow()
            }
        }
        return true
    }

    override fun getInfoContents(marker: Marker): View? {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var mView: View? = null
        if (TextUtils.equals(marker.snippet, markerType1)) { //预警marker
            mView = inflater.inflate(R.layout.warning_marker_icon_info, null)
            val infoList = addInfoList(marker)
            val mListView = mView.findViewById<ListView>(R.id.listView)
            val mAdapter = WarningAdapter(this, infoList, true)
            mListView.adapter = mAdapter
            val params = mListView.layoutParams
            if (infoList.size == 1) {
                params.height = CommonUtil.dip2px(this, 50f).toInt()
            } else if (infoList.size == 2) {
                params.height = CommonUtil.dip2px(this, 100f).toInt()
            } else if (infoList.size > 2) {
                params.height = CommonUtil.dip2px(this, 150f).toInt()
            }
            mListView.layoutParams = params
            mListView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 -> intentDetail(infoList[arg2]) }
        } else if (TextUtils.equals(marker.snippet, markerType2)) {
            mView = inflater.inflate(R.layout.shawn_typhoon_marker_icon_info, null)
            val tvName = mView.findViewById<TextView>(R.id.tvName)
            val tvInfo = mView.findViewById<TextView>(R.id.tvInfo)
            val ivDelete = mView.findViewById<ImageView>(R.id.ivDelete)
            if (!TextUtils.isEmpty(marker.title)) {
                val str = marker.title.split("\\|").toTypedArray()
                if (!TextUtils.isEmpty(str[0])) {
                    tvName.text = str[0]
                }
                if (!TextUtils.isEmpty(str[1])) {
                    tvInfo.text = str[1]
                }
            }
            ivDelete.setOnClickListener { marker.hideInfoWindow() }
        }
        return mView
    }

    private fun addInfoList(marker: Marker): List<WarningDto> {
        val infoList: MutableList<WarningDto> = ArrayList()
        for (dto in warningList) {
            val latLng = marker.title.split(",").toTypedArray()
            if (TextUtils.equals(latLng[0], dto.lat.toString() + "") && TextUtils.equals(latLng[1], dto.lng.toString() + "")) {
                infoList.add(dto)
            }
        }
        return infoList
    }

    private fun intentDetail(data: WarningDto) {
        val intentDetail = Intent(this, WarningDetailActivity::class.java)
        intentDetail.putExtra(CONST.COLUMN_ID, columnId)
        val bundle = Bundle()
        bundle.putParcelable("data", data)
        intentDetail.putExtras(bundle)
        startActivity(intentDetail)
    }

    override fun getInfoWindow(arg0: Marker?): View? {
        return null
    }

    private val arcMenuListener = OnMenuItemClickListener { view, pos ->
        if (pos == 0) {
            blue = !blue
            if (!blue) {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_blue_press)
            } else {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_blue)
            }
            screenWarningMarkers("01")
        } else if (pos == 1) {
            yellow = !yellow
            if (!yellow) {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_yellow_press)
            } else {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_yellow)
            }
            screenWarningMarkers("02")
        } else if (pos == 2) {
            orange = !orange
            if (!orange) {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_orange_press)
            } else {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_orange)
            }
            screenWarningMarkers("03")
        } else if (pos == 3) {
            red = !red
            if (!red) {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_red_press)
            } else {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_red)
            }
            screenWarningMarkers("04")
        }
    }

    override fun onCameraChange(arg0: CameraPosition?) {}
    override fun onCameraChangeFinish(arg0: CameraPosition) {
        zoom = arg0.zoom
        val leftPoint = Point(0, CommonUtil.heightPixels(this))
        val rightPoint = Point(CommonUtil.widthPixels(this), 0)
        leftlatlng = aMap!!.projection.fromScreenLocation(leftPoint)
        rightLatlng = aMap!!.projection.fromScreenLocation(rightPoint)
        switchWarningMarkers()
        addWarningMarkers()
    }

    /**
     * 初始化预警统计列表
     */
    private fun initListView() {
        statisticAdapter = WarningStatisticAdapter(this, statisticList)
        listView.adapter = statisticAdapter
        listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 -> clickPromptWarning() }
    }

    /**
     * 隐藏或显示ListView的动画
     */
    fun hideOrShowListViewAnimator(view: View?, startValue: Int, endValue: Int) {
        //1.设置属性的初始值和结束值
        val mAnimator = ValueAnimator.ofInt(0, 100)
        //2.为目标对象的属性变化设置监听器
        mAnimator.addUpdateListener { animation ->
            val animatorValue = animation.animatedValue as Int
            val fraction = animatorValue / 100f
            val mEvaluator = IntEvaluator()
            //3.使用IntEvaluator计算属性值并赋值给ListView的高
            view!!.layoutParams.height = mEvaluator.evaluate(fraction, startValue, endValue)
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

    private fun clickPromptWarning() {
        val height = CommonUtil.getListViewHeightBasedOnChildren(listView)
        isShowPrompt = !isShowPrompt
        if (!isShowPrompt) {
            ivArrow.setImageResource(R.drawable.shawn_icon_arrow_up_black)
            hideOrShowListViewAnimator(listView, 1, height)
        } else {
            ivArrow.setImageResource(R.drawable.shawn_icon_arrow_down_black)
            hideOrShowListViewAnimator(listView, height, 1)
        }
    }

    override fun onMapScreenShot(bitmap1: Bitmap?) { //bitmap1为地图截屏
        val bitmap2 = CommonUtil.captureView(clShare)
        val bitmap3 = CommonUtil.mergeBitmap(this@WarningActivity, bitmap1, bitmap2, true)
        CommonUtil.clearBitmap(bitmap1)
        CommonUtil.clearBitmap(bitmap2)
        val bitmap4 = BitmapFactory.decodeResource(resources, R.drawable.legend_share_portrait)
        val bitmap = CommonUtil.mergeBitmap(this, bitmap3, bitmap4, false)
        CommonUtil.clearBitmap(bitmap3)
        CommonUtil.clearBitmap(bitmap4)
        CommonUtil.share(this@WarningActivity, bitmap)
    }

    override fun onMapScreenShot(arg0: Bitmap?, arg1: Int) {}

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.ivGuide -> {
                ivGuide.visibility = View.GONE
                CommonUtil.saveGuidePageState(this, this.javaClass.name)
            }
            R.id.reWarningStatistic, R.id.ivArrow -> clickPromptWarning()
            R.id.ivLocation -> {
                isExpandMap = !isExpandMap
                if (isExpandMap) {
                    aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 10.0f))
                } else {
                    aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.926628, 105.178100), 3.5f))
                }
            }
            R.id.ivRefresh -> refresh()
            R.id.ivList -> {
                val intent = Intent(this, WarningListActivity::class.java)
                intent.putExtra(CONST.COLUMN_ID, columnId)
                intent.putExtra("isVisible", true)
                startActivity(intent)
            }
            R.id.ivStatistic -> startActivity(Intent(this, WarningStatisticActivity::class.java))
            R.id.ivNews -> {
                intent = Intent(this, PdfTitleActivity::class.java)
                val list = ArrayList<ColumnData>()
                var cd = ColumnData()
                cd.columnId = columnId
                cd.name = "月报"
                cd.dataUrl = "http://decision-admin.tianqi.cn/home/work2019/getWarnNews_new?type=1"
                list.add(cd)
                cd = ColumnData()
                cd.columnId = columnId
                cd.name = "大数据报告"
                cd.dataUrl = "http://decision-admin.tianqi.cn/home/work2019/getWarnNews_new?type=2"
                list.add(cd)
                cd = ColumnData()
                cd.columnId = columnId
                cd.name = "预警报告"
                cd.dataUrl = "http://decision-admin.tianqi.cn/home/work2019/getWarnNews_new?type=3"
                list.add(cd)
                val bundle = Bundle()
                bundle.putParcelableArrayList("dataList", list)
                intent.putExtras(bundle)
                intent.putExtra(CONST.COLUMN_ID, columnId)
                intent.putExtra(CONST.ACTIVITY_NAME, "预警报告")
                startActivity(intent)
            }
            R.id.tvNation -> {
                intent = Intent(this, WarningHeaderActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelableArrayList("warningList", nationList as ArrayList<out Parcelable?>)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            R.id.ivCentral, R.id.tvCentrolCount -> {
                intent = Intent(this, WarningHeaderActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelableArrayList("warningList", centrolList as ArrayList<out Parcelable?>)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            R.id.ivIntrol -> {
                intent = Intent(this, WebviewActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, "预警中心简介")
                intent.putExtra(CONST.WEB_URL, "http://warn-wx.tianqi.cn/Index/desc.html")
                startActivity(intent)
            }
            R.id.ivShare -> aMap!!.getMapScreenShot(this@WarningActivity)
            R.id.iv1 -> if (!flags[0]) {
                drawWarningLayer(SecretUrlUtil.warningLayer(warningType1), warningType1)
                flags[0] = true
                iv1!!.setImageResource(R.drawable.shawn_icon_warning_fog_open)
                ivLegend1.visibility = View.VISIBLE
            } else {
                removeWarningLayer(warningType1)
                flags[0] = false
                iv1!!.setImageResource(R.drawable.shawn_icon_warning_fog_close)
                ivLegend1.visibility = View.GONE
                tvName1!!.visibility = View.GONE
            }
            R.id.iv2 -> if (!flags[1]) {
                drawWarningLayer(SecretUrlUtil.warningLayer(warningType2), warningType2)
                flags[1] = true
                iv2.setImageResource(R.drawable.shawn_icon_warning_rain_open)
                ivLegend2.visibility = View.VISIBLE
            } else {
                removeWarningLayer(warningType2)
                flags[1] = false
                iv2.setImageResource(R.drawable.shawn_icon_warning_rain_close)
                ivLegend2.visibility = View.GONE
                tvName2!!.visibility = View.GONE
            }
            R.id.iv3 -> if (!flags[2]) {
                drawWarningLayer(SecretUrlUtil.warningLayer(warningType3), warningType3)
                flags[2] = true
                iv3.setImageResource(R.drawable.shawn_icon_warning_sand_open)
                ivLegend3.visibility = View.VISIBLE
            } else {
                removeWarningLayer(warningType3)
                flags[2] = false
                iv3.setImageResource(R.drawable.shawn_icon_warning_sand_close)
                ivLegend3.visibility = View.GONE
                tvName3!!.visibility = View.GONE
            }
            R.id.iv4 -> if (!flags[3]) {
                drawWarningLayer(SecretUrlUtil.warningLayer(warningType4), warningType4)
                flags[3] = true
                iv4.setImageResource(R.drawable.shawn_icon_warning_snow_open)
                ivLegend4.visibility = View.VISIBLE
            } else {
                removeWarningLayer(warningType4)
                flags[3] = false
                iv4.setImageResource(R.drawable.shawn_icon_warning_snow_close)
                ivLegend4.visibility = View.GONE
                tvName4!!.visibility = View.GONE
            }
            R.id.iv5 -> if (!flags[4]) {
                drawWarningLayer(SecretUrlUtil.warningLayer(warningType5), warningType5)
                flags[4] = true
                iv5.setImageResource(R.drawable.shawn_icon_warning_temp_open)
                ivLegend5.visibility = View.VISIBLE
            } else {
                removeWarningLayer(warningType5)
                flags[4] = false
                iv5.setImageResource(R.drawable.shawn_icon_warning_temp_close)
                ivLegend5.visibility = View.GONE
                tvName5!!.visibility = View.GONE
            }
            R.id.iv6 -> if (!flags[5]) {
                var i = 0
                while (i < typhoonMarkers.size) {
                    typhoonMarkers[i].isVisible = true
                    i++
                }
                flags[5] = true
                iv6.setImageResource(R.drawable.shawn_icon_warning_typhoon_open)
            } else {
                var i = 0
                while (i < typhoonMarkers.size) {
                    typhoonMarkers[i].isVisible = false
                    i++
                }
                flags[5] = false
                iv6.setImageResource(R.drawable.shawn_icon_warning_typhoon_close)
            }
            R.id.iv7 -> if (!flags[6]) {
                drawWarningLayer(SecretUrlUtil.warningLayer(warningType7), warningType7)
                flags[6] = true
                iv7.setImageResource(R.drawable.shawn_icon_warning_wind_open)
                ivLegend7.visibility = View.VISIBLE
            } else {
                removeWarningLayer(warningType7)
                flags[6] = false
                iv7.setImageResource(R.drawable.shawn_icon_warning_wind_open)
                ivLegend7.visibility = View.GONE
                tvName7!!.visibility = View.GONE
            }
            R.id.iv8 -> if (!flags[7]) {
                drawWarningLayer(SecretUrlUtil.warningLayer(warningType8), warningType8)
                flags[7] = true
                iv8.setImageResource(R.drawable.shawn_icon_warning_hanchao_open)
                ivLegend8.visibility = View.VISIBLE
            } else {
                removeWarningLayer(warningType8)
                flags[7] = false
                iv8.setImageResource(R.drawable.shawn_icon_warning_hanchao_close)
                ivLegend8.visibility = View.GONE
                tvName8!!.visibility = View.GONE
            }
            R.id.iv9 -> if (!flags[8]) {
                drawWarningLayer(SecretUrlUtil.warningLayer(warningType9), warningType9)
                flags[8] = true
                iv9.setImageResource(R.drawable.shawn_icon_warning_wind_open)
                ivLegend9.visibility = View.VISIBLE
            } else {
                removeWarningLayer(warningType9)
                flags[8] = false
                iv9.setImageResource(R.drawable.shawn_icon_warning_wind_close)
                ivLegend9.visibility = View.GONE
                tvName9!!.visibility = View.GONE
            }
            R.id.iv10 -> if (!flags[9]) {
                drawWarningLayer(SecretUrlUtil.warningLayer(warningType10), warningType10)
                flags[9] = true
                iv10.setImageResource(R.drawable.shawn_icon_warning_qiangduiliu_open)
                ivLegend10.visibility = View.VISIBLE
            } else {
                removeWarningLayer(warningType10)
                flags[9] = false
                iv10.setImageResource(R.drawable.shawn_icon_warning_qiangduiliu_close)
                ivLegend10.visibility = View.GONE
                tvName10!!.visibility = View.GONE
            }
        }
    }

    private fun initBroadCast() {
        mReceiver = MyBroadCastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ArcMenu.BROADCASTCLICK)
        registerReceiver(mReceiver, intentFilter)
    }

    private inner class MyBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ArcMenu.BROADCASTCLICK) {
                val bundle = intent.extras
                val status = bundle.getBoolean("STATUS")
                if (!status) {
                    narrowAnimation(llLegend)
                } else {
                    enlargeAnimation(llLegend)
                }
            }
        }
    }

    /**
     * 缩小动画
     */
    private fun narrowAnimation(view: View?) {
        if (view == null) {
            return
        }
        val animation = android.view.animation.ScaleAnimation(
                1.0f, 0.7f, 1.0f, 0.7f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f
        )
        animation.duration = 200
        animation.fillAfter = true
        view.startAnimation(animation)
    }

    /**
     * 放大动画
     */
    private fun enlargeAnimation(view: View?) {
        if (view == null) {
            return
        }
        val animation = android.view.animation.ScaleAnimation(
                0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f
        )
        animation.duration = 200
        animation.fillAfter = true
        view.startAnimation(animation)
    }

    /**
     * 清除预警图层
     */
    private fun removeWarningLayer(type: String) {
        if (TextUtils.equals(type, warningType1)) {
            for (i in polyline11.indices) {
                polyline11[i].remove()
            }
            polyline11.clear()
            for (i in polyline12.indices) {
                polyline12[i].remove()
            }
            polyline12.clear()
            for (i in polygons13.indices) {
                polygons13[i].remove()
            }
            polygons13.clear()
        } else if (TextUtils.equals(type, warningType2)) {
            for (i in polyline21.indices) {
                polyline21[i].remove()
            }
            polyline21.clear()
            for (i in polyline22.indices) {
                polyline22[i].remove()
            }
            polyline22.clear()
            for (i in polygons23.indices) {
                polygons23[i].remove()
            }
            polygons23.clear()
        } else if (TextUtils.equals(type, warningType3)) {
            for (i in polyline31.indices) {
                polyline31[i].remove()
            }
            polyline31.clear()
            for (i in polyline32.indices) {
                polyline32[i].remove()
            }
            polyline32.clear()
            for (i in polygons33.indices) {
                polygons33[i].remove()
            }
            polygons33.clear()
        } else if (TextUtils.equals(type, warningType4)) {
            for (i in polyline41.indices) {
                polyline41[i].remove()
            }
            polyline41.clear()
            for (i in polyline42.indices) {
                polyline42[i].remove()
            }
            polyline42.clear()
            for (i in polygons43.indices) {
                polygons43[i].remove()
            }
            polygons43.clear()
        } else if (TextUtils.equals(type, warningType5)) {
            for (i in polyline51.indices) {
                polyline51[i].remove()
            }
            polyline51.clear()
            for (i in polyline52.indices) {
                polyline52[i].remove()
            }
            polyline52.clear()
            for (i in polygons53.indices) {
                polygons53[i].remove()
            }
            polygons53.clear()
        } else if (TextUtils.equals(type, warningType7)) {
            for (i in polyline71.indices) {
                polyline71[i].remove()
            }
            polyline71.clear()
            for (i in polyline72.indices) {
                polyline72[i].remove()
            }
            polyline72.clear()
            for (i in polygons73.indices) {
                polygons73[i].remove()
            }
            polygons73.clear()
        } else if (TextUtils.equals(type, warningType8)) {
            for (i in polyline81.indices) {
                polyline81[i].remove()
            }
            polyline81.clear()
            for (i in polyline82.indices) {
                polyline82[i].remove()
            }
            polyline82.clear()
            for (i in polygons83.indices) {
                polygons83[i].remove()
            }
            polygons83.clear()
        } else if (TextUtils.equals(type, warningType9)) {
            for (i in polyline91.indices) {
                polyline91[i].remove()
            }
            polyline91.clear()
            for (i in polyline92.indices) {
                polyline92[i].remove()
            }
            polyline92.clear()
            for (i in polygons93.indices) {
                polygons93[i].remove()
            }
            polygons93.clear()
        } else if (TextUtils.equals(type, warningType10)) {
            for (i in polyline101.indices) {
                polyline101[i].remove()
            }
            polyline101.clear()
            for (i in polyline102.indices) {
                polyline102[i].remove()
            }
            polyline102.clear()
            for (i in polygons103.indices) {
                polygons103[i].remove()
            }
            polygons103.clear()
        }
    }

    /**
     * 绘制预警图层
     * @param url
     */
    private fun drawWarningLayer(url: String, type: String) {
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            val obj = JSONObject(result)
                            if (!obj.isNull("micaps14_$type")) {
                                val dataUrl = obj.getString("micaps14_$type")
                                OkHttpSpecialLayer(dataUrl, type)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }).start()
    }

    private fun OkHttpSpecialLayer(url: String, type: String) {
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
                            if (!obj.isNull("mtime")) {
                                val time = obj.getLong("mtime")
                                val time12 = time + 1000 * 60 * 60 * 12
                                val time24 = time + 1000 * 60 * 60 * 24
                                layerTime12 = sdf4.format(time) + " - " + sdf5.format(time12)
                                layerTime24 = sdf4.format(time) + " - " + sdf5.format(time24)
                            }
                            tvName1!!.text = "全国大雾区域预报$layerTime24"
                            tvName2!!.text = "全国强降雨落区预报$layerTime24"
                            //									tvName3.setText("全国强降雨落区预报" + enter + layerTime24);
                            //									tvName4.setText("全国强降雨落区预报" + enter + layerTime24);
                            tvName5!!.text = "全国高温区域预报$layerTime12"
                            //									tvName7.setText("全国强降雨落区预报" + enter + layerTime24);
                            //									tvName8.setText("全国强降雨落区预报" + enter + layerTime24);
                            //									tvName9.setText("全国强降雨落区预报" + enter + layerTime24);
                            //									tvName10.setText("全国强降雨落区预报" + enter + layerTime12);
                            if (TextUtils.equals(type, warningType1)) {
                                tvName1!!.visibility = View.VISIBLE
                            } else if (TextUtils.equals(type, warningType2)) {
                                tvName2!!.visibility = View.VISIBLE
                            } else if (TextUtils.equals(type, warningType5)) {
                                tvName5!!.visibility = View.VISIBLE
                            }
                            //									else if (TextUtils.equals(type, warningType7)) {
                            //									tvName7.setVisibility(View.VISIBLE);
                            //								}else if (TextUtils.equals(type, warningType8)) {
                            //									tvName8.setVisibility(View.VISIBLE);
                            //								}else if (TextUtils.equals(type, warningType9)) {
                            //									tvName9.setVisibility(View.VISIBLE);
                            //								}else if (TextUtils.equals(type, warningType10)) {
                            //									tvName10.setVisibility(View.VISIBLE);
                            //								}
                            if (!obj.isNull("lines")) {
                                val lines = obj.getJSONArray("lines")
                                for (i in 0 until lines.length()) {
                                    val itemObj = lines.getJSONObject(i)
                                    if (!itemObj.isNull("point")) {
                                        val points = itemObj.getJSONArray("point")
                                        val polylineOption = PolylineOptions()
                                        polylineOption.width(6f).color(-0xbf9441)
                                        for (j in 0 until points.length()) {
                                            val point = points.getJSONObject(j)
                                            val lat = point.getDouble("y")
                                            val lng = point.getDouble("x")
                                            polylineOption.add(LatLng(lat, lng))
                                        }
                                        val p = aMap!!.addPolyline(polylineOption)
                                        if (TextUtils.equals(type, warningType1)) {
                                            polyline11.add(p)
                                        } else if (TextUtils.equals(type, warningType2)) {
                                            polyline21.add(p)
                                        } else if (TextUtils.equals(type, warningType3)) {
                                            polyline31.add(p)
                                        } else if (TextUtils.equals(type, warningType4)) {
                                            polyline41.add(p)
                                        } else if (TextUtils.equals(type, warningType5)) {
                                            polyline51.add(p)
                                        } else if (TextUtils.equals(type, warningType7)) {
                                            polyline71.add(p)
                                        } else if (TextUtils.equals(type, warningType8)) {
                                            polyline81.add(p)
                                        } else if (TextUtils.equals(type, warningType9)) {
                                            polyline91.add(p)
                                        } else if (TextUtils.equals(type, warningType10)) {
                                            polyline101.add(p)
                                        }
                                    }
                                    //							if (!itemObj.isNull("flags")) {
                                    //								JSONObject flags = itemObj.getJSONObject("flags");
                                    //								String text = "";
                                    //								if (!flags.isNull("text")) {
                                    //									text = flags.getString("text");
                                    //								}
                                    //								if (!flags.isNull("items")) {
                                    //									JSONArray items = flags.getJSONArray("items");
                                    //									JSONObject item = items.getJSONObject(0);
                                    //									double lat = item.getDouble("y");
                                    //									double lng = item.getDouble("x");
                                    //									TextOptions to = new TextOptions();
                                    //									to.position(new LatLng(lat, lng));
                                    //									to.text(text);
                                    //									to.fontColor(Color.BLACK);
                                    //									to.fontSize(30);
                                    //									to.backgroundColor(Color.TRANSPARENT);
                                    //									Text t = aMap.addText(to);
                                    //									textList1.add(t);
                                    //								}
                                    //							}
                                }
                            }
                            if (!obj.isNull("line_symbols")) {
                                val line_symbols = obj.getJSONArray("line_symbols")
                                for (i in 0 until line_symbols.length()) {
                                    val itemObj = line_symbols.getJSONObject(i)
                                    if (!itemObj.isNull("items")) {
                                        val items = itemObj.getJSONArray("items")
                                        val polylineOption = PolylineOptions()
                                        polylineOption.width(6f).color(-0xbf9441)
                                        for (j in 0 until items.length()) {
                                            val item = items.getJSONObject(j)
                                            val lat = item.getDouble("y")
                                            val lng = item.getDouble("x")
                                            polylineOption.add(LatLng(lat, lng))
                                        }
                                        val p = aMap!!.addPolyline(polylineOption)
                                        if (TextUtils.equals(type, warningType1)) {
                                            polyline12.add(p)
                                        } else if (TextUtils.equals(type, warningType2)) {
                                            polyline22.add(p)
                                        } else if (TextUtils.equals(type, warningType3)) {
                                            polyline32.add(p)
                                        } else if (TextUtils.equals(type, warningType4)) {
                                            polyline42.add(p)
                                        } else if (TextUtils.equals(type, warningType5)) {
                                            polyline52.add(p)
                                        } else if (TextUtils.equals(type, warningType7)) {
                                            polyline72.add(p)
                                        } else if (TextUtils.equals(type, warningType8)) {
                                            polyline82.add(p)
                                        } else if (TextUtils.equals(type, warningType9)) {
                                            polyline92.add(p)
                                        } else if (TextUtils.equals(type, warningType10)) {
                                            polyline102.add(p)
                                        }
                                    }
                                }
                            }
                            //								if (!obj.isNull("symbols")) {
                            //									JSONArray symbols = obj.getJSONArray("symbols");
                            //									for (int i = 0; i < symbols.length(); i++) {
                            //										JSONObject itemObj = symbols.getJSONObject(i);
                            //										String text = "";
                            //										int color = Color.BLACK;
                            //										if (!itemObj.isNull("type")) {
                            //											String type = itemObj.getString("type");
                            //											if (TextUtils.equals(type, "60")) {
                            //												text = "H";
                            //												color = Color.RED;
                            //											}else if (TextUtils.equals(type, "61")) {
                            //												text = "L";
                            //												color = Color.BLUE;
                            //											}else if (TextUtils.equals(type, "37")) {
                            //												text = "台";
                            //												color = Color.GREEN;
                            //											}
                            //										}
                            //										double lat = itemObj.getDouble("y");
                            //										double lng = itemObj.getDouble("x");
                            //										TextOptions to = new TextOptions();
                            //										to.position(new LatLng(lat, lng));
                            //										to.text(text);
                            //										to.fontColor(color);
                            //										to.fontSize(60);
                            //										to.backgroundColor(Color.TRANSPARENT);
                            //										Text t = aMap.addText(to);
                            //										textList2.add(t);
                            //									}
                            //								}
                            if (!obj.isNull("areas")) {
                                val array = obj.getJSONArray("areas")
                                for (i in 0 until array.length()) {
                                    val itemObj = array.getJSONObject(i)
                                    var color = itemObj.getString("c")
                                    if (color.contains("#")) {
                                        color = color.replace("#", "")
                                    }
                                    val r = color.substring(0, 2).toInt(16)
                                    val g = color.substring(2, 4).toInt(16)
                                    val b = color.substring(4, 6).toInt(16)
                                    if (!itemObj.isNull("items")) {
                                        val items = itemObj.getJSONArray("items")
                                        val polygonOption = PolygonOptions()
                                        polygonOption.strokeColor(Color.rgb(r, g, b)).fillColor(Color.rgb(r, g, b))
                                        for (j in 0 until items.length()) {
                                            val item = items.getJSONObject(j)
                                            val lat = item.getDouble("y")
                                            val lng = item.getDouble("x")
                                            polygonOption.add(LatLng(lat, lng))
                                        }
                                        val p = aMap!!.addPolygon(polygonOption)
                                        if (TextUtils.equals(type, warningType1)) {
                                            polygons13.add(p)
                                        } else if (TextUtils.equals(type, warningType2)) {
                                            polygons23.add(p)
                                        } else if (TextUtils.equals(type, warningType3)) {
                                            polygons33.add(p)
                                        } else if (TextUtils.equals(type, warningType4)) {
                                            polygons43.add(p)
                                        } else if (TextUtils.equals(type, warningType5)) {
                                            polygons53.add(p)
                                        } else if (TextUtils.equals(type, warningType7)) {
                                            polygons73.add(p)
                                        } else if (TextUtils.equals(type, warningType8)) {
                                            polygons83.add(p)
                                        } else if (TextUtils.equals(type, warningType9)) {
                                            polygons93.add(p)
                                        } else if (TextUtils.equals(type, warningType10)) {
                                            polygons103.add(p)
                                        }
                                    }
                                    //							if (!itemObj.isNull("symbols")) {
                                    //								JSONObject symbols = itemObj.getJSONObject("symbols");
                                    //								String text = symbols.getString("text");
                                    //								JSONArray items = symbols.getJSONArray("items");
                                    //								if (items.length() > 0) {
                                    //									JSONObject o = items.getJSONObject(0);
                                    //									double lat = o.getDouble("y");
                                    //									double lng = o.getDouble("x");
                                    //									TextOptions to = new TextOptions();
                                    //									to.position(new LatLng(lat, lng));
                                    //									to.text(text);
                                    //									to.fontColor(Color.BLACK);
                                    //									to.fontSize(30);
                                    //									to.backgroundColor(Color.TRANSPARENT);
                                    //									Text t = aMap.addText(to);
                                    //									textList3.add(t);
                                    //								}
                                    //							}
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }

    /**
     * 获取当年的台风列表信息
     */
    private fun OkHttpTyphoonList() {
        val currentYear = Integer.valueOf(sdf1.format(Date()))
        val url = "http://decision-admin.tianqi.cn/Home/other/gettyphoon/list/$currentYear"
        OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    return
                }
                val requestResult = response.body!!.string()
                runOnUiThread(Runnable {
                    if (!TextUtils.isEmpty(requestResult)) {
                        val c = "("
                        val c2 = "})"
                        val result = requestResult.substring(requestResult.indexOf(c) + c.length, requestResult.indexOf(c2) + 1)
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("typhoonList")) {
                                    typhoonList.clear()
                                    val array = obj.getJSONArray("typhoonList")
                                    for (i in 0 until array.length()) {
                                        val itemArray = array.getJSONArray(i)
                                        val dto = TyphoonDto()
                                        dto.id = itemArray.getString(0)
                                        dto.enName = itemArray.getString(1)
                                        dto.name = itemArray.getString(2)
                                        dto.code = itemArray.getString(4)
                                        dto.status = itemArray.getString(7)
                                        //把活跃台风过滤出来存放
                                        if (TextUtils.equals(dto.status, "start")) {
                                            typhoonList.add(dto)
                                            if (TextUtils.isEmpty(dto.id)) {
                                                return@Runnable
                                            }
                                            var name: String
                                            name = if (TextUtils.equals(dto.enName, "nameless")) {
                                                dto.code + " " + dto.enName
                                            } else {
                                                dto.code + " " + dto.name + " " + dto.enName
                                            }
                                            OkHttpTyphoonDetail("http://decision-admin.tianqi.cn/Home/other/gettyphoon/view/" + dto.id, name)
                                        }
                                    }
                                    if (typhoonList.size > 0) {
                                        iv6.visibility = View.VISIBLE
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                })
            }
        })
    }

    /**
     * 获取台风详情
     */
    private fun OkHttpTyphoonDetail(url: String, name: String) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    return
                }
                val requestResult = response.body!!.string()
                runOnUiThread {
                    if (!TextUtils.isEmpty(requestResult)) {
                        val c = "("
                        val result = requestResult.substring(requestResult.indexOf(c) + c.length, requestResult.indexOf(")"))
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("typhoon")) {
                                    val array = obj.getJSONArray("typhoon")
                                    val itemArray = array.getJSONArray(8)
                                    if (itemArray.length() > 0) {
                                        val itemArray2 = itemArray.getJSONArray(itemArray.length() - 1)
                                        val dto = TyphoonDto()
                                        if (!TextUtils.isEmpty(name)) {
                                            dto.name = name
                                        }
                                        val longTime = itemArray2.getLong(2)
                                        dto.time = sdf2.format(Date(longTime))
                                        dto.lng = itemArray2.getDouble(4)
                                        dto.lat = itemArray2.getDouble(5)
                                        dto.pressure = itemArray2.getString(6)
                                        dto.max_wind_speed = itemArray2.getString(7)
                                        dto.move_speed = itemArray2.getString(9)
                                        val fx_string = itemArray2.getString(8)
                                        if (!TextUtils.isEmpty(fx_string)) {
                                            var windDir = ""
                                            for (i in 0 until fx_string.length) {
                                                var item = fx_string.substring(i, i + 1)
                                                if (TextUtils.equals(item, "N")) {
                                                    item = "北"
                                                } else if (TextUtils.equals(item, "S")) {
                                                    item = "南"
                                                } else if (TextUtils.equals(item, "W")) {
                                                    item = "西"
                                                } else if (TextUtils.equals(item, "E")) {
                                                    item = "东"
                                                }
                                                windDir = windDir + item
                                            }
                                            dto.wind_dir = windDir
                                        }
                                        var type = itemArray2.getString(3)
                                        if (TextUtils.equals(type, "TD")) { //热带低压
                                            type = "1"
                                        } else if (TextUtils.equals(type, "TS")) { //热带风暴
                                            type = "2"
                                        } else if (TextUtils.equals(type, "STS")) { //强热带风暴
                                            type = "3"
                                        } else if (TextUtils.equals(type, "TY")) { //台风
                                            type = "4"
                                        } else if (TextUtils.equals(type, "STY")) { //强台风
                                            type = "5"
                                        } else if (TextUtils.equals(type, "SuperTY")) { //超强台风
                                            type = "6"
                                        }
                                        dto.type = type
                                        dto.isFactPoint = true
                                        val array10 = itemArray2.getJSONArray(10)
                                        for (m in 0 until array10.length()) {
                                            val itemArray10 = array10.getJSONArray(m)
                                            if (m == 0) {
                                                dto.radius_7 = itemArray10.getString(1)
                                            } else if (m == 1) {
                                                dto.radius_10 = itemArray10.getString(1)
                                            }
                                        }
                                        val tOption = MarkerOptions()
                                        tOption.title(name + "|" + dto.content(this@WarningActivity))
                                        tOption.snippet(markerType2)
                                        tOption.position(LatLng(dto.lat, dto.lng))
                                        tOption.anchor(0.5f, 0.5f)
                                        val iconList = ArrayList<BitmapDescriptor>()
                                        for (i in 1..9) {
                                            iconList.add(BitmapDescriptorFactory.fromAsset("typhoon/typhoon_icon$i.png"))
                                        }
                                        tOption.icons(iconList)
                                        tOption.period(6)
                                        val marker = aMap!!.addMarker(tOption)
                                        marker.isVisible = flags[5]
                                        typhoonMarkers.add(marker)
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        })
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
        if (mReceiver != null) {
            unregisterReceiver(mReceiver)
        }
    }

}
