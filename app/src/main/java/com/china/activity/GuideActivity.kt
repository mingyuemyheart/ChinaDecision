package com.china.activity;

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.china.R
import com.china.adapter.MyPagerAdapter
import com.china.fragment.GuideFragment
import kotlinx.android.synthetic.main.activity_guide.*
import java.util.*

/**
 * 引导页
 */
class GuideActivity : FragmentActivity() {
	
	private val fragments : ArrayList<Fragment> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_guide)
		initViewPager()
	}

	/**
	 * 初始化viewPager
	 */
	private fun initViewPager() {
		for (i in 0 .. 2) {
			val fragment = GuideFragment()
			val bundle = Bundle()
			bundle.putInt("index", i)
			fragment.arguments = bundle
			fragments.add(fragment)
		}
		viewPager!!.adapter = MyPagerAdapter(supportFragmentManager, fragments)
		viewPager!!.setSlipping(true)//设置ViewPager是否可以滑动
		viewPager!!.offscreenPageLimit = fragments.size
	}

}
