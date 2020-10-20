package com.china.activity

import android.Manifest
import android.animation.IntEvaluator
import android.animation.ValueAnimator
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
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.*
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.ScaleAnimation
import com.china.R
import com.china.common.CONST
import com.china.dto.WeatherStaticsDto
import com.china.utils.*
import kotlinx.android.synthetic.main.activity_city_forecast.mapView
import kotlinx.android.synthetic.main.activity_city_forecast.tvTime
import kotlinx.android.synthetic.main.activity_weather_fact.*
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * 城市天气预报
 */
class WeatherFactActivity : BaseActivity(), OnClickListener, OnMarkerClickListener,
		OnMapClickListener, OnCameraChangeListener, InfoWindowAdapter, OnInfoWindowClickListener, OnMapScreenShotListener {

	private var aMap: AMap? = null
	private val dataList: ArrayList<WeatherStaticsDto> = ArrayList()
	private val markerMap: LinkedHashMap<String, Marker?> = LinkedHashMap() //按区域id区分
	private val level1 = "level1"
	private val level2 = "level2"
	private val level3 = "level3"
	private var zoom = 3.7f
	private var zoom1 = 7.0f
	private var leftlatlng = LatLng(-16.305714763804854, 75.13831436634065)
	private var rightLatlng = LatLng(63.681687310440864, 135.21788656711578)
	private var clickMarker: Marker? = null
	private val sdf2 = SimpleDateFormat("yyyy年MM月dd日 HH时", Locale.CHINA)
	private val sdf3 = SimpleDateFormat("HH", Locale.CHINA)
	private var isShowWeather = false
	private var isPressHail = true
	private var isPressIce = true
	private var isPressSand = true
	private var isPressHaze = true
	private var isPressFog = true
	private var isPressSnow = true
	private var isPressRain = true
	private var isPressCloud = true
	private var isPressSunny = true
	private var isPressOvercast = true
	private var tags = ""

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_weather_fact)
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
		aMap!!.uiSettings.isZoomControlsEnabled = false
		aMap!!.uiSettings.isRotateGesturesEnabled = false
		aMap!!.setOnMarkerClickListener(this)
		aMap!!.setOnMapClickListener(this)
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
		ivExpand.setOnClickListener(this)
		ivHail.setOnClickListener(this)
		ivHail.tag = "5"
		ivIce.setOnClickListener(this)
		ivIce.tag = "19"
		ivSand.setOnClickListener(this)
		ivSand.tag = "20,29,30,31"
		ivHaze.setOnClickListener(this)
		ivHaze.tag = "53,54,55,56"
		ivFog.setOnClickListener(this)
		ivFog.tag = "18,32,49,57,58"
		ivSnow.setOnClickListener(this)
		ivSnow.tag = "13,14,15,16,17,26,27,28,33"
		ivRain.setOnClickListener(this)
		ivRain.tag = "3,4,5,6,7,8,9,10,11,12,19,21,22,23,24,25"
		ivCloud.setOnClickListener(this)
		ivCloud.tag = "1"
		ivSunny.setOnClickListener(this)
		ivSunny.tag ="0"
		ivOvercast.setOnClickListener(this)
		ivOvercast.tag = "2"
		pressWeather()

		tvTime!!.text = sdf2.format(Date())
		tvTime!!.visibility = View.VISIBLE

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
			OkHttpUtil.enqueue(Request.Builder().url(SecretUrlUtil.weatherFact()).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {}
				@Throws(IOException::class)
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					runOnUiThread { cancelDialog() }
					val result = response.body!!.string()
					if (!TextUtils.isEmpty(result)) {
						dataList.clear()
						parseStationInfo(result, level1)
						parseStationInfo(result, level2)
						parseStationInfo(result, level3)
						addMarkers()
					}

					//放到这里监听地图缩放事件，为了解决list异常问题
					aMap!!.setOnCameraChangeListener(this@WeatherFactActivity)
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
					if (!itemObj.isNull("city")) {
						dto.name = itemObj.getString("city")
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
					if (dto.areaId.contains("10101") || dto.areaId.contains("10102") || dto.areaId.contains("10103") || dto.areaId.contains("10104")) {
						dto.level = "2"
					}
					if (!itemObj.isNull("phe")) {
						val weatherCode = itemObj.getString("phe")
						if (!TextUtils.isEmpty(weatherCode)) {
							dto.pheCode = weatherCode.toInt()
						}
					}
					dataList.add(dto)
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
				val pheCode = marker!!.title.split(",")[1]
				val level = marker!!.snippet
				val lat = marker.position.latitude
				val lng = marker.position.longitude
				if (zoom <= zoom1) {
					if (TextUtils.equals(level, "2") || TextUtils.equals(level, "3")) {
						markerColloseAnimation(marker)
					}
				}
				if (lat <= leftlatlng.latitude || lat >= rightLatlng.latitude || lng <= leftlatlng.longitude || lng >= rightLatlng.longitude) {
					markerColloseAnimation(marker)
				}

				//选择展示
				if (!TextUtils.isEmpty(tags) && tags.contains(",")) {
					val array = tags.split(",")
					var isRemove = true
					for (i in array.indices) {
						if (TextUtils.equals(array[i], pheCode)) {
							isRemove = false
						}
					}
					if (isRemove) {
						markerColloseAnimation(marker)
					}
				}
			}
		}
	}

	/**
	 * 添加marker
	 */
	private fun addMarkers() {
		for (dto in dataList) {
			if (zoom <= zoom1) {
				if (TextUtils.equals(dto.level, "1")) {
					if (markerMap.containsKey(dto.areaId)) {
						val m = markerMap[dto.areaId]
						if (m == null || !m.isVisible) {
							addVisibleAreaMarker(dto)
						}
					} else {
						addVisibleAreaMarker(dto)
					}
				}
			} else {
				if (TextUtils.equals(dto.level, "1") || TextUtils.equals(dto.level, "2")) {
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
		}
	}

	/**
	 * 添加可视区域对应的marker
	 * @param dto
	 */
	private fun addVisibleAreaMarker(dto: WeatherStaticsDto) {
		if (dto.lat > leftlatlng.latitude && dto.lat < rightLatlng.latitude && dto.lng > leftlatlng.longitude && dto.lng < rightLatlng.longitude) {
			//选择展示
			if (!TextUtils.isEmpty(tags) && tags.contains(",")) {
				val array = tags.split(",")
				for (i in array.indices) {
					if (TextUtils.equals(array[i], dto.pheCode.toString())) {
						val options = MarkerOptions()
						options.title("${dto.name},${dto.pheCode},${dto.areaId}")
						options.snippet(dto.level)
						options.anchor(0.5f, 1.0f)
						options.position(LatLng(dto.lat, dto.lng))
						options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.pheCode)))
						val marker = aMap!!.addMarker(options)
						markerMap[dto.areaId] = marker
						markerExpandAnimation(marker)
						break
					}
				}
			}
		}
	}

	/**
	 * 给marker添加文字
	 * @return
	 */
	private fun getTextBitmap(weatherCode: Int): View? {
		val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val view = inflater.inflate(R.layout.layout_marker_fact, null) ?: return null
		val currentHour = Integer.valueOf(sdf3.format(Date()))
		if (currentHour in 5..17) {
			view.ivMarker.setImageBitmap(WeatherUtil.getDayBitmap(this, weatherCode))
		} else {
			view.ivMarker.setImageBitmap(WeatherUtil.getNightBitmap(this, weatherCode))
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
		animation.setAnimationListener(object : com.amap.api.maps.model.animation.Animation.AnimationListener{
			override fun onAnimationEnd() {
				marker.remove()
			}
			override fun onAnimationStart() {
			}
		})
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
		val phe = getString(WeatherUtil.getWeatherId(Integer.valueOf(result[1])))
		view.tvInfo.text = "$phe"
		return view
	}

	override fun onInfoWindowClick(marker: Marker) {
		val result = marker.title.split(",").toTypedArray()
		val intent = Intent(this, ForecastActivity::class.java)
		intent.putExtra("cityName", result[0])
		intent.putExtra("cityId", result[2])
		startActivity(intent)
	}

	override fun onMapScreenShot(bitmap1: Bitmap?) { //bitmap1为地图截屏
		val bitmap4 = BitmapFactory.decodeResource(resources, R.drawable.legend_share_portrait)
		val bitmap = CommonUtil.mergeBitmap(this, bitmap1, bitmap4, false)
		CommonUtil.clearBitmap(bitmap1)
		CommonUtil.clearBitmap(bitmap4)
		CommonUtil.share(this, bitmap)
	}

	override fun onMapScreenShot(arg0: Bitmap?, arg1: Int) {}

	/**
	 * 按钮的旋转动画
	 * @param view
	 * @param fromDegrees
	 * @param toDegrees
	 * @param durationMillis
	 */
	private fun rotateView(view: View, fromDegrees: Float, toDegrees: Float, durationMillis: Int) {
		val rotate = RotateAnimation(
				fromDegrees, toDegrees,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f)
		rotate.duration = durationMillis.toLong()
		rotate.fillAfter = true
		view.startAnimation(rotate)
	}

	private fun expandWeather() {
		llWeather.measure(0, 0)
		val height = llWeather.measuredHeight
		if (isShowWeather) {
			valueAnimator(llWeather, 1, height)
		} else {
			valueAnimator(llWeather, height, 1)
		}
	}

	/**
	 * 数值动画
	 */
	private fun valueAnimator(view: View, startValue: Int, endValue: Int) {
		//1.设置属性的初始值和结束值
		val mAnimator = ValueAnimator.ofInt(0, 100)
		//2.为目标对象的属性变化设置监听器
		mAnimator.addUpdateListener { animation ->
			val animatorValue = animation.animatedValue as Int
			val fraction = animatorValue / 100f
			val mEvaluator = IntEvaluator()
			//3.使用IntEvaluator计算属性值并赋值给ListView的高
			view.layoutParams.height = mEvaluator.evaluate(fraction, startValue, endValue)
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

	private fun pressWeather() {
		tags = ""
		if (isPressHail) {
			tags+=ivHail.tag.toString()+","
			ivHail.setImageResource(R.drawable.wf_icon_hail_press)
		} else {
			ivHail.setImageResource(R.drawable.wf_icon_hail)
		}
		if (isPressIce) {
			tags+=ivIce.tag.toString()+","
			ivIce.setImageResource(R.drawable.wf_icon_ice_press)
		} else {
			ivIce.setImageResource(R.drawable.wf_icon_ice)
		}
		if (isPressSand) {
			tags+=ivSand.tag.toString()+","
			ivSand.setImageResource(R.drawable.wf_icon_sand_press)
		} else {
			ivSand.setImageResource(R.drawable.wf_icon_sand)
		}
		if (isPressHaze) {
			tags+=ivHaze.tag.toString()+","
			ivHaze.setImageResource(R.drawable.wf_icon_haze_press)
		} else {
			ivHaze.setImageResource(R.drawable.wf_icon_haze)
		}
		if (isPressFog) {
			tags+=ivFog.tag.toString()+","
			ivFog.setImageResource(R.drawable.wf_icon_fog_press)
		} else {
			ivFog.setImageResource(R.drawable.wf_icon_fog)
		}
		if (isPressSnow) {
			tags+=ivSnow.tag.toString()+","
			ivSnow.setImageResource(R.drawable.wf_icon_snow_press)
		} else {
			ivSnow.setImageResource(R.drawable.wf_icon_snow)
		}
		if (isPressRain) {
			tags+=ivRain.tag.toString()+","
			ivRain.setImageResource(R.drawable.wf_icon_rain_press)
		} else {
			ivRain.setImageResource(R.drawable.wf_icon_rain)
		}
		if (isPressCloud) {
			tags+=ivCloud.tag.toString()+","
			ivCloud.setImageResource(R.drawable.wf_icon_cloud_press)
		} else {
			ivCloud.setImageResource(R.drawable.wf_icon_cloud)
		}
		if (isPressSunny) {
			tags+=ivSunny.tag.toString()+","
			ivSunny.setImageResource(R.drawable.wf_icon_sunny_press)
		} else {
			ivSunny.setImageResource(R.drawable.wf_icon_sunny)
		}
		if (isPressOvercast) {
			tags+=ivOvercast.tag.toString()+","
			ivOvercast.setImageResource(R.drawable.wf_icon_overcast_press)
		} else {
			ivOvercast.setImageResource(R.drawable.wf_icon_overcast)
		}
		switchMarkers()
		addMarkers()
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.llBack -> finish()
			R.id.ivShare -> aMap!!.getMapScreenShot(this)
			R.id.ivExpand -> {
				isShowWeather = !isShowWeather
				if (isShowWeather) {
					rotateView(ivExpand, 0f, 315f, 200)
				} else {
					rotateView(ivExpand, 315f, 0f, 200)
				}
				expandWeather()
			}
			R.id.ivHail -> {
				isPressHail = !isPressHail
				pressWeather()
			}
			R.id.ivIce -> {
				isPressIce = !isPressIce
				pressWeather()
			}
			R.id.ivSand -> {
				isPressSand = !isPressSand
				pressWeather()
			}
			R.id.ivHaze -> {
				isPressHaze = !isPressHaze
				pressWeather()
			}
			R.id.ivFog -> {
				isPressFog = !isPressFog
				pressWeather()
			}
			R.id.ivSnow -> {
				isPressSnow = !isPressSnow
				pressWeather()
			}
			R.id.ivRain -> {
				isPressRain = !isPressRain
				pressWeather()
			}
			R.id.ivCloud -> {
				isPressCloud = !isPressCloud
				pressWeather()
			}
			R.id.ivSunny -> {
				isPressSunny = !isPressSunny
				pressWeather()
			}
			R.id.ivOvercast -> {
				isPressOvercast = !isPressOvercast
				pressWeather()
			}
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
			Manifest.permission.READ_PHONE_STATE,
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
