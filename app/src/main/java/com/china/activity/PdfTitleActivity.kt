package com.china.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import com.china.R
import com.china.common.CONST
import com.china.common.ColumnData
import com.china.fragment.PdfListFragment
import com.china.fragment.WebviewFragment
import com.china.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_pdf_title.*
import kotlinx.android.synthetic.main.layout_title.*

/**
 * 带有标签页的pdf文档界面
 * @author shawn_sun
 *
 */
class PdfTitleActivity : BaseFragmentActivity(), OnClickListener {

	private val fragments : ArrayList<Fragment> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_pdf_title)
		initWidget()
		initViewPager()
	}

	private fun initWidget() {
		llBack.setOnClickListener(this)
		hScrollView1.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))

		val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
		if (!TextUtils.isEmpty(title)) {
			tvTitle.text = title
		}
	}
	
	/**
	 * 初始化viewPager
	 */
	private fun initViewPager() {
		val columnList : ArrayList<ColumnData> = intent.getParcelableArrayListExtra("dataList")
		val columnSize = columnList.size
		if (columnSize <= 1) {
			llContainer.visibility = View.GONE
			llContainer1.visibility = View.GONE
		}
		llContainer.removeAllViews()
		llContainer1.removeAllViews()
		val width = CommonUtil.widthPixels(this)
		for (i in 0 until columnSize) {
			val dto = columnList[i]

			val tvName = TextView(this)
			tvName.gravity = Gravity.CENTER
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f)
			tvName.setPadding(0, CommonUtil.dip2px(this, 5.0f).toInt(), 0, CommonUtil.dip2px(this, 5.0f).toInt())
			tvName.setOnClickListener {
				if (viewPager != null) {
					viewPager.setCurrentItem(i, true)
				}
			}
			tvName.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
			if (!TextUtils.isEmpty(dto.name)) {
				tvName.text = dto.name
			}

			val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			if (columnSize <= 4) {
				params.width = width/columnSize
			} else {
				params.width = width/4
			}
			tvName.layoutParams = params
			llContainer.addView(tvName, i)

			val tvBar = TextView(this)
			tvBar.gravity = Gravity.CENTER
			tvBar.setOnClickListener {
				if (viewPager != null) {
					viewPager.setCurrentItem(i, true)
				}
			}
			if (i == 0) {
				tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))
			}else {
				tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
			}
			val params1 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			if (columnSize <= 4) {
				params1.width = width/columnSize
			} else {
				params1.width = width/4
			}
			params1.height = CommonUtil.dip2px(this, 2.0f).toInt()
			tvBar.layoutParams = params1
			llContainer1.addView(tvBar, i)

			var fragment: Fragment? = null
			when(dto.showType) {
				CONST.URL -> fragment = WebviewFragment()
				else -> fragment = PdfListFragment()
			}
			val bundle = Bundle()
			bundle.putString(CONST.WEB_URL, dto.dataUrl)
			bundle.putString(CONST.COLUMN_ID, dto.id)
			fragment.arguments = bundle
			fragments.add(fragment)
		}

		viewPager.offscreenPageLimit = fragments.size
		viewPager.adapter = MyPagerAdapter(supportFragmentManager, fragments)
		viewPager.setOnPageChangeListener(object : OnPageChangeListener {
			override fun onPageScrollStateChanged(p0: Int) {
			}
			override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
			}
			override fun onPageSelected(arg0: Int) {
				if (llContainer != null) {
					if (llContainer.childCount > 4) {
						hScrollView1.smoothScrollTo(width / 4 * arg0, 0)
					}
				}

				if (llContainer1 != null) {
					for (i in 0 until llContainer1.childCount) {
						val tvBar = llContainer1.getChildAt(i) as TextView
						if (i == arg0) {
							tvBar.setBackgroundColor(ContextCompat.getColor(this@PdfTitleActivity, R.color.blue))
						} else {
							tvBar.setBackgroundColor(ContextCompat.getColor(this@PdfTitleActivity, R.color.transparent))
						}
					}
				}
			}
		})
	}

	/**
	 * @ClassName: MyPagerAdapter
	 * @Description: TODO填充ViewPager的数据适配器
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:37:47
	 *
	 */
	class MyPagerAdapter(fm: FragmentManager, fs : ArrayList<Fragment>) : FragmentStatePagerAdapter(fm) {

		private val fragments : ArrayList<Fragment> = fs

		init {
			notifyDataSetChanged()
		}

		override fun getCount(): Int {
			return fragments.size
		}

		override fun getItem(arg0: Int): Fragment {
			return fragments[arg0]
		}

		override fun getItemPosition(`object`: Any): Int {
			return PagerAdapter.POSITION_NONE
		}
	}

	override fun onClick(view: View?) {
		when(view!!.id) {
			R.id.llBack -> finish()
		}
	}

}
