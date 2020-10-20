package com.china.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import com.china.R
import com.china.adapter.MyPagerAdapter
import com.china.dto.WarningDto
import com.china.fragment.WarningDetailFragment
import com.china.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_warning_header.*
import kotlinx.android.synthetic.main.layout_title.*
import java.util.*

/**
 * 左右切换的预警界面
 */
class WarningHeaderActivity : BaseFragmentActivity(), OnClickListener {
	
	private val fragments : ArrayList<Fragment> = ArrayList()
	private val warnList : ArrayList<WarningDto> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_warning_header)
		initWidget()
		initViewPager()
	}

	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvTitle.text = "预警详情"
		ivShare.setOnClickListener(this)
		ivShare.visibility = View.VISIBLE
	}
	
	/**
	 * 初始化viewPager
	 */
	private fun initViewPager() {
		warnList.clear()
		warnList.addAll(intent.extras.getParcelableArrayList("warningList"))

		val ivTips = arrayOfNulls<ImageView>(warnList.size)
		viewGroup.removeAllViews()
		for (i in 0 until warnList.size) {
			val dto = warnList[i]
			val fragment = WarningDetailFragment()
			val bundle = Bundle()
			bundle.putParcelable("data", dto)
			fragment.arguments = bundle
			fragments.add(fragment)

			val imageView = ImageView(this)
			imageView.layoutParams = LayoutParams(5, 5)
			ivTips[i] = imageView
			if(i == 0){
				ivTips[i]!!.setBackgroundResource(R.drawable.point_black)
			}else{
				ivTips[i]!!.setBackgroundResource(R.drawable.point_gray)
			}
			val layoutParams = LinearLayout.LayoutParams(LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
			layoutParams.leftMargin = 10
			layoutParams.rightMargin = 10
			viewGroup.addView(imageView, layoutParams)
		}

		if (warnList.size <= 1) {
			viewGroup.visibility = View.GONE
		}

		viewPager!!.adapter = MyPagerAdapter(supportFragmentManager, fragments)
		viewPager!!.setSlipping(true)//设置ViewPager是否可以滑动
		viewPager!!.offscreenPageLimit = fragments.size
		viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
			override fun onPageScrollStateChanged(p0: Int) {
			}
			override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
			}
			override fun onPageSelected(p0: Int) {
				for (i in 0 until warnList.size) {
					if(i == p0){
						ivTips[i]!!.setBackgroundResource(R.drawable.point_black)
					}else{
						ivTips[i]!!.setBackgroundResource(R.drawable.point_gray)
					}
				}
			}
		})
	}
	
	override fun onClick(v: View?) {
		when(v!!.id) {
			R.id.llBack -> finish()
			R.id.ivShare -> {
				val bitmap1 = CommonUtil.captureView(viewPager)
				val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.legend_share_portrait)
				val bitmap = CommonUtil.mergeBitmap(this, bitmap1, bitmap2, false)
				CommonUtil.clearBitmap(bitmap1)
				CommonUtil.clearBitmap(bitmap2)
				CommonUtil.share(this@WarningHeaderActivity, bitmap)
			}
		}
	}

}
