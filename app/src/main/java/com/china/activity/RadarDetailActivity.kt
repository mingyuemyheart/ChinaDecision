package com.china.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.china.R
import com.china.adapter.RadarDetailAdapter
import com.china.dto.RadarDto
import com.china.manager.RadarManager
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import com.china.utils.SecretUrlUtil
import com.china.view.PhotoView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_radar_detail.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 天气雷达详情
 */
class RadarDetailActivity : BaseActivity(), OnClickListener, RadarManager.RadarListener {

	private val radarList: ArrayList<RadarDto>? = ArrayList()
	private var mRadarManager: RadarManager? = null
	private var mRadarThread: RadarThread? = null
	private var mAdapter: RadarDetailAdapter? = null
	private val sdf1 = SimpleDateFormat("HH:mm", Locale.CHINA)
	private val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
	private var gridviewPosition = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_radar_detail)
		initWidget()
		initGridView()
	}

	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		showDialog()
		llBack.setOnClickListener(this)
		imageView.setOnClickListener(this)
		ivPlay.setOnClickListener(this)
		seekBar.setOnSeekBarChangeListener(seekbarListener)
		mRadarManager = RadarManager(this)

		val params = imageView.layoutParams
		params.width = CommonUtil.widthPixels(this)
		params.height = CommonUtil.widthPixels(this) * 16 / 21
		imageView.layoutParams = params
		if (intent.hasExtra("radarName")) {
			val title = intent.getStringExtra("radarName")
			if (!TextUtils.isEmpty(title)) {
				tvTitle.text = title
			}
		}
		if (intent.hasExtra("radarCode")) {
			val radarCode = intent.getStringExtra("radarCode")
			if (!TextUtils.isEmpty(radarCode)) {
				okHttpRadarDetail(radarCode)
			}
		}
	}

	private val seekbarListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
		override fun onStopTrackingTouch(arg0: SeekBar) {
			if (mRadarThread != null) {
				mRadarThread!!.setCurrent(seekBar!!.progress)
				mRadarThread!!.stopTracking()
			}
		}

		override fun onStartTrackingTouch(arg0: SeekBar) {
			if (mRadarThread != null) {
				mRadarThread!!.startTracking()
			}
		}

		override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {}
	}

	/**
	 * 获取雷达图片集信息
	 */
	private fun okHttpRadarDetail(radarCode: String) {
		val url = SecretUrlUtil.radarDetail(radarCode, "product")
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
						cancelDialog()
						if (!TextUtils.isEmpty(result)) {
							try {
								val obj = JSONObject(result)
								val r2 = obj.getString("r2")
								val r3 = obj.getString("r3")
								val r5 = obj.getString("r5")
								val array = JSONArray(obj.getString("r6"))
								for (i in array.length() - 1 downTo 0) {
									val itemArray = array.getJSONArray(i)
									val r6_0 = itemArray.getString(0)
									val r6_1 = itemArray.getString(1)
									val imgUrl = "$r2$r5$r6_0.$r3"
									val dto = RadarDto()
									dto.imgUrl = imgUrl
									try {
										dto.time = sdf1.format(sdf2.parse(r6_1))
									} catch (e: ParseException) {
										e.printStackTrace()
									}
									radarList!!.add(dto)
								}
								if (radarList!!.size > 0) {
									gridviewPosition = radarList.size - 1
									loadImages(gridviewPosition)
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
	 * 初始化gridview
	 */
	private fun initGridView() {
		mAdapter = RadarDetailAdapter(this, radarList)
		gridView.adapter = mAdapter
		gridView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
			gridviewPosition = arg2
			loadImages(gridviewPosition)
		}
	}

	/**
	 * 加载图片数据
	 */
	private fun loadImages(gridviewPosition: Int) {
		if (radarList != null && radarList.size > 0) {
			for (i in radarList.indices) {
				val data = radarList[i]
				data.isSelected = i == gridviewPosition
			}
			if (mAdapter != null) {
				mAdapter!!.notifyDataSetChanged()
			}
			if (!TextUtils.isEmpty(radarList[gridviewPosition].imgUrl)) {
				Picasso.get().load(radarList[gridviewPosition].imgUrl).error(R.drawable.shawn_icon_no_pic).into(imageView)
			} else {
				imageView!!.setImageResource(R.drawable.shawn_icon_no_pic)
			}
			if (seekBar != null && radarList.size > 0) {
				seekBar!!.progress = gridviewPosition + 1
				seekBar!!.max = radarList.size
			}
			try {
				tvTime!!.text = sdf1.format(sdf2.parse(radarList[gridviewPosition].time))
			} catch (e: ParseException) {
				e.printStackTrace()
			}
			initViewPager()
		}
	}

	/**
	 * 初始化viewPager
	 */
	private fun initViewPager() {
		val imageArray = arrayOfNulls<ImageView>(radarList!!.size)
		for (i in radarList.indices) {
			val dto = radarList[i]
			if (!TextUtils.isEmpty(dto.imgUrl)) {
				val imageView = ImageView(this)
				imageView.scaleType = ImageView.ScaleType.CENTER_CROP
				Picasso.get().load(dto.imgUrl).into(imageView)
				imageArray[i] = imageView
			}
		}
		val mAdapter = MyViewPagerAdapter(imageArray)
		viewPager.adapter = mAdapter
		viewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
			override fun onPageSelected(arg0: Int) {
				tvCount.text = (arg0 + 1).toString() + "/" + radarList.size
			}
			override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
			override fun onPageScrollStateChanged(arg0: Int) {}
		})
	}

	private inner class MyViewPagerAdapter(private val mImageViews: Array<ImageView?>) : PagerAdapter() {
		override fun getCount(): Int {
			return mImageViews.size
		}

		override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
			return arg0 === arg1
		}

		override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
			container.removeView(mImageViews[position])
		}

		override fun instantiateItem(container: ViewGroup, position: Int): Any {
			val photoView = PhotoView(container.context)
			val drawable = mImageViews[position]!!.drawable
			photoView.setImageDrawable(drawable)
			container.addView(photoView, 0)
			photoView.onPhotoTapListener = OnPhotoTapListener { view, v, v1 ->
				scaleColloseAnimation(clViewPager)
				clViewPager.visibility = View.GONE
			}
			return photoView
		}
	}

	/**
	 * 放大动画
	 * @param view
	 */
	private fun scaleExpandAnimation(view: View?) {
		val animationSet = AnimationSet(true)
		val scaleAnimation = ScaleAnimation(0f, 1.0f, 0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
		scaleAnimation.interpolator = LinearInterpolator()
		scaleAnimation.duration = 300
		animationSet.addAnimation(scaleAnimation)
		val alphaAnimation = AlphaAnimation(0f, 1.0f)
		alphaAnimation.duration = 300
		animationSet.addAnimation(alphaAnimation)
		view!!.startAnimation(animationSet)
	}

	/**
	 * 缩小动画
	 * @param view
	 */
	private fun scaleColloseAnimation(view: View?) {
		val animationSet = AnimationSet(true)
		val scaleAnimation = ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
		scaleAnimation.interpolator = LinearInterpolator()
		scaleAnimation.duration = 300
		animationSet.addAnimation(scaleAnimation)
		val alphaAnimation = AlphaAnimation(1.0f, 0f)
		alphaAnimation.duration = 300
		animationSet.addAnimation(alphaAnimation)
		view!!.startAnimation(animationSet)
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		if (clViewPager!!.visibility == View.VISIBLE) {
			scaleColloseAnimation(clViewPager)
			clViewPager!!.visibility = View.GONE
			return false
		} else {
			finish()
		}
		return super.onKeyDown(keyCode, event)
	}

	private fun startDownLoadImgs(list: ArrayList<RadarDto>?) {
		if (mRadarThread != null) {
			mRadarThread!!.cancel()
			mRadarThread = null
		}
		if (list!!.isNotEmpty()) {
			mRadarManager!!.loadImagesAsyn(list, this)
		}
	}

	override fun onResult(result: Int, images: ArrayList<RadarDto>?) {
		runOnUiThread {
			cancelDialog()
			if (ivPlay != null) {
				ivPlay.setImageResource(R.drawable.shawn_icon_pause)
			}
		}
		if (result == RadarManager.RadarListener.RESULT_SUCCESSED) {
			if (mRadarThread != null) {
				mRadarThread!!.cancel()
				mRadarThread = null
			}
			if (images!!.isNotEmpty()) {
				mRadarThread = RadarThread(images)
				mRadarThread!!.start()
			}
		}
	}

	private inner class RadarThread(images: ArrayList<RadarDto>) : Thread() {

		var currentState: Int
		private var index: Int
		private val count: Int
		private var isTracking: Boolean
		val STATE_NONE = 0
		val STATE_PLAYING = 1
		val STATE_PAUSE = 2
		val STATE_CANCEL = 3
		var images: ArrayList<RadarDto>

		init {
			count = images.size
			index = 0
			currentState = STATE_NONE
			isTracking = false
			this.images = images
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
				if (mRadarThread != null) {
					mRadarThread!!.pause()
					if (ivPlay != null) {
						ivPlay.setImageResource(R.drawable.shawn_icon_play)
					}
					if (seekBar != null) {
						seekBar.progress = radarList!!.size
					}
				}
			} else {
				runOnUiThread {
					gridviewPosition = index
					val radar = images[index]
					val bitmap = BitmapFactory.decodeFile(radar.imgPath)
					if (bitmap != null) {
						imageView!!.setImageBitmap(bitmap)
					}
					val arg2 = index++
					changeProgress(radar.time, arg2, count-1)
					var i = 0
					while (i < radarList!!.size) {
						if (i == arg2) {
							radarList[arg2].isSelected = true
						} else {
							radarList[i].isSelected = false
						}
						i++
					}
					mAdapter!!.notifyDataSetChanged()
				}
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
	override fun onProgress(url: String?, progress: Int) {
	}

	private fun changeProgress(time: String, progress: Int, max: Int) {
		if (seekBar != null) {
			seekBar!!.max = max
			seekBar!!.progress = progress
		}
		tvTime!!.text = time
	}

	override fun onDestroy() {
		super.onDestroy()
		if (mRadarManager != null) {
			mRadarManager!!.onDestory()
		}
		if (mRadarThread != null) {
			mRadarThread!!.cancel()
			mRadarThread = null
		}
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.llBack -> finish()
			R.id.ivPlay -> if (mRadarThread != null && mRadarThread!!.currentState == mRadarThread!!.STATE_PLAYING) {
				mRadarThread!!.pause()
				ivPlay.setImageResource(R.drawable.shawn_icon_play)
			} else if (mRadarThread != null && mRadarThread!!.currentState == mRadarThread!!.STATE_PAUSE) {
				mRadarThread!!.play()
				ivPlay.setImageResource(R.drawable.shawn_icon_pause)
			} else if (mRadarThread == null) {
				showDialog()
				startDownLoadImgs(radarList) //开始下载
			}
			R.id.imageView -> if (clViewPager!!.visibility == View.GONE) {
				if (viewPager != null) {
					viewPager!!.currentItem = gridviewPosition
				}
				scaleExpandAnimation(clViewPager)
				clViewPager!!.visibility = View.VISIBLE
				tvCount.text = (gridviewPosition + 1).toString() + "/" + radarList!!.size
			}
		}
	}

}
