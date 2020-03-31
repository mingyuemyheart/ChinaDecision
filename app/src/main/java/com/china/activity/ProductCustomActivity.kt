package com.china.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.View
import android.view.View.OnClickListener
import com.china.R
import com.china.adapter.MyPagerAdapter
import com.china.fragment.ProductCustomLeftFragment
import com.china.fragment.ProductCustomRightFragment
import kotlinx.android.synthetic.main.activity_product_custom.*
import java.util.*

/**
 * 产品定制
 */
class ProductCustomActivity : BaseFragmentActivity(), OnClickListener {

	private val fragments : ArrayList<Fragment> = ArrayList()
	private var fragment1 : ProductCustomLeftFragment? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_product_custom)
		initWidget()
		initViewPager()
	}

	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvControl.setOnClickListener(this)
		tvControl.text = "定制"
		tvControl.visibility = View.VISIBLE
		tv1.setOnClickListener(MyOnClickListener(0, viewPager))
		tv2.setOnClickListener(MyOnClickListener(1, viewPager))
	}
	
	/**
	 * 初始化viewPager
	 */
	private fun initViewPager() {
		fragment1 = ProductCustomLeftFragment()
		fragments.add(fragment1!!)
		val fragment2 = ProductCustomRightFragment()
		fragments.add(fragment2)

		viewPager!!.adapter = MyPagerAdapter(supportFragmentManager, fragments)
		viewPager!!.setSlipping(true)//设置ViewPager是否可以滑动
		viewPager!!.offscreenPageLimit = fragments.size
		viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
			override fun onPageScrollStateChanged(p0: Int) {
			}
			override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
			}
			override fun onPageSelected(position: Int) {
				if (position == 0) {
					tv1.setTextColor(Color.WHITE)
					tv1.setBackgroundResource(R.drawable.corner_left_blue)
					tv2.setTextColor(ContextCompat.getColor(this@ProductCustomActivity, R.color.blue))
					tv2.setBackgroundResource(R.drawable.corner_right_white)
				}else if (position == 1) {
					tv1.setTextColor(ContextCompat.getColor(this@ProductCustomActivity, R.color.blue))
					tv1.setBackgroundResource(R.drawable.corner_left_white)
					tv2.setTextColor(Color.WHITE)
					tv2.setBackgroundResource(R.drawable.corner_right_blue)
				}
			}
		})
	}

	/**
	 * @ClassName: MyOnClickListener
	 * @Description: TODO头标点击监听
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:46:08
	 *
	 */
	private class MyOnClickListener internal constructor(i: Int, viewPager: ViewPager) : OnClickListener {
		private var index = 0
		private var viewPager : ViewPager? = null
		override fun onClick(v: View?) {
			if (viewPager != null) {
				viewPager!!.currentItem = index
			}
		}
		init {
			index = i
			this.viewPager = viewPager
		}
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.llBack -> finish()
			R.id.tvControl -> startActivityForResult(Intent(this, ProductCustomSubmitActivity::class.java), 1001)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK) {
			when (requestCode) {
				1001 -> if (fragment1 != null) {
					fragment1!!.OkHttpList()
				}
			}
		}
	}

}
