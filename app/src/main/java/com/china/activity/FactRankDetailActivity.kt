package com.china.activity

import android.app.Fragment
import android.app.FragmentTransaction
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.china.R
import com.china.common.CONST
import com.china.dto.StationMonitorDto
import com.china.dto.WarningDto
import com.china.fragment.*
import com.china.manager.DBManager
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import com.china.utils.SecretUrlUtil
import kotlinx.android.synthetic.main.activity_fact_rank_detail.*
import kotlinx.android.synthetic.main.layout_title.*
import kotlinx.android.synthetic.main.layout_title.reTitle
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.util.*

/**
 * 实况监测详情
 */
class FactRankDetailActivity : BaseActivity(), OnClickListener{

    private val fragments: MutableList<Fragment?> = ArrayList()
    private val warningList: MutableList<WarningDto?> = ArrayList() //该站点对应的预警信息

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fact_rank_detail)
        showDialog()
        initWidget()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            showPortrait()
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showLandscape()
        }
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        ll1.setOnClickListener(MyOnClickListener(0))
        ll2.setOnClickListener(MyOnClickListener(1))
        ll3.setOnClickListener(MyOnClickListener(2))
        ll4.setOnClickListener(MyOnClickListener(3))
        ll5.setOnClickListener(MyOnClickListener(4))
        ll6.setOnClickListener(MyOnClickListener(5))
        llMain.setOnClickListener(this)
        ivShare.setOnClickListener(this)
        if (CommonUtil.widthPixels(this) < CommonUtil.heightPixels(this)) {
            showPortrait()
        } else {
            showLandscape()
        }
        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        val stationId = intent.getStringExtra("stationId")
        if (!TextUtils.isEmpty(stationId)) {
            tvTitle.text = "$title - $stationId"
            val warningId = queryWarningIdByStationId(stationId)
            if (!TextUtils.isEmpty(warningId)) {
                okHttpWarning("https://decision-admin.tianqi.cn/Home/work2019/getwarns?order=0&areaid=$warningId")
            }
            okHttpDetail(stationId, intent.getStringExtra("interface"))
        }
    }

    private fun showPortrait() {
        llBack!!.visibility = View.VISIBLE
        ivShare!!.visibility = View.GONE
        llMain!!.visibility = View.VISIBLE
    }

    private fun showLandscape() {
        llBack!!.visibility = View.GONE
        ivShare!!.visibility = View.VISIBLE
        llMain!!.visibility = View.GONE
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager(data: StationMonitorDto) {
        var fragment: Fragment? = null
        for (i in 0..5) {
            if (i == 0) {
                fragment = ShawnFactDetailRainFragment()
            } else if (i == 1) {
                fragment = ShawnFactDetailTempFragment()
            } else if (i == 2) {
                fragment = ShawnFactDetailHumidityFragmetn()
            } else if (i == 3) {
                fragment = ShawnFactDetailWindFragment()
            } else if (i == 4) {
                fragment = ShawnFactDetailVisibleFragment()
            } else if (i == 5) {
                fragment = ShawnFactDetailPressureFragment()
            }
            val bundle = Bundle()
            bundle.putParcelable("data", data)
            bundle.putParcelableArrayList("warningList", warningList as ArrayList<out Parcelable?>)
            fragment!!.arguments = bundle
            fragments.add(fragment)
        }
        viewPager.setSlipping(true) //设置ViewPager是否可以滑动
        viewPager.offscreenPageLimit = fragments.size
        viewPager.setOnPageChangeListener(MyOnPageChangeListener())
        viewPager.adapter = MyPagerAdapter()
    }

    private inner class MyOnPageChangeListener : OnPageChangeListener {
        override fun onPageSelected(arg0: Int) {
            when (arg0) {
                0 -> {
                    ll1.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.white))
                    ll2.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll3.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll4.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll5.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll6.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    iv1.setImageResource(R.drawable.iv_jiangshui_selected)
                    iv2.setImageResource(R.drawable.iv_wendu)
                    iv3.setImageResource(R.drawable.iv_shidu)
                    iv4.setImageResource(R.drawable.iv_wind)
                    iv5.setImageResource(R.drawable.iv_visible)
                    iv6.setImageResource(R.drawable.iv_qiya)
                    tv1.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.sure_color))
                    tv2.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv3.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv4.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv5.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv6.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                }
                1 -> {
                    ll1.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll2.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.white))
                    ll3.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll4.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll5.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll6.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    iv1.setImageResource(R.drawable.iv_jiangshui)
                    iv2.setImageResource(R.drawable.iv_wendu_selected)
                    iv3.setImageResource(R.drawable.iv_shidu)
                    iv4.setImageResource(R.drawable.iv_wind)
                    iv5.setImageResource(R.drawable.iv_visible)
                    iv6.setImageResource(R.drawable.iv_qiya)
                    tv1.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv2.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.sure_color))
                    tv3.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv4.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv5.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv6.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                }
                2 -> {
                    ll1.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll2.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll3.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.white))
                    ll4.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll5.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll6.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    iv1.setImageResource(R.drawable.iv_jiangshui)
                    iv2.setImageResource(R.drawable.iv_wendu)
                    iv3.setImageResource(R.drawable.iv_shidu_selected)
                    iv4.setImageResource(R.drawable.iv_wind)
                    iv5.setImageResource(R.drawable.iv_visible)
                    iv6.setImageResource(R.drawable.iv_qiya)
                    tv1.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv2.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv3.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.sure_color))
                    tv4.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv5.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv6.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                }
                3 -> {
                    ll1.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll2.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll3.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll4.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.white))
                    ll5.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll6.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    iv1.setImageResource(R.drawable.iv_jiangshui)
                    iv2.setImageResource(R.drawable.iv_wendu)
                    iv3.setImageResource(R.drawable.iv_shidu)
                    iv4.setImageResource(R.drawable.iv_wind_selected)
                    iv5.setImageResource(R.drawable.iv_visible)
                    iv6.setImageResource(R.drawable.iv_qiya)
                    tv1.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv2.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv3.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv4.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.sure_color))
                    tv5.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv6.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                }
                4 -> {
                    ll1.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll2.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll3.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll4.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll5.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.white))
                    ll6.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    iv1.setImageResource(R.drawable.iv_jiangshui)
                    iv2.setImageResource(R.drawable.iv_wendu)
                    iv3.setImageResource(R.drawable.iv_shidu)
                    iv4.setImageResource(R.drawable.iv_wind)
                    iv5.setImageResource(R.drawable.iv_visible_selected)
                    iv6.setImageResource(R.drawable.iv_qiya)
                    tv1.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv2.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv3.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv4.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv5.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.sure_color))
                    tv6.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                }
                5 -> {
                    ll1.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll2.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll3.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll4.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll5.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.station_color1))
                    ll6.setBackgroundColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.white))
                    iv1.setImageResource(R.drawable.iv_jiangshui)
                    iv2.setImageResource(R.drawable.iv_wendu)
                    iv3.setImageResource(R.drawable.iv_shidu)
                    iv4.setImageResource(R.drawable.iv_wind)
                    iv5.setImageResource(R.drawable.iv_visible)
                    iv6.setImageResource(R.drawable.iv_qiya_selected)
                    tv1.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv2.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv3.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv4.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv5.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.text_color4))
                    tv6.setTextColor(ContextCompat.getColor(this@FactRankDetailActivity, R.color.sure_color))
                }
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * @ClassName: MyOnClickListener
     * @Description: TODO头标点击监听
     * @author Panyy
     * @date 2013 2013年11月6日 下午2:46:08
     */
    private inner class MyOnClickListener(i: Int) : OnClickListener {
        private var index = 0
        override fun onClick(v: View) {
            if (viewPager != null) {
                viewPager.currentItem = index
            }
        }

        init {
            index = i
        }
    }

    /**
     * @ClassName: MyPagerAdapter
     * @Description: TODO填充ViewPager的数据适配器
     * @author Panyy
     * @date 2013 2013年11月6日 下午2:37:47
     */
    private inner class MyPagerAdapter : PagerAdapter() {
        override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
            return arg0 === arg1
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun destroyItem(container: View, position: Int, `object`: Any) {
            (container as ViewPager).removeView(fragments[position]!!.view)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment: Fragment? = fragments[position]
            if (!fragment!!.isAdded) { // 如果fragment还没有added
                val ft: FragmentTransaction = fragmentManager.beginTransaction()
                ft.add(fragment, fragment.javaClass.simpleName)
                ft.commit()
                /**
                 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
                 * 会在进程的主线程中,用异步的方式来执行。
                 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
                 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
                 */
                fragmentManager.executePendingTransactions()
            }
            if (fragment.view.parent == null) {
                container.addView(fragment.view) // 为viewpager增加布局
            }
            return fragment.view
        }
    }

    /**
     * 根据站点id查询预警id
     * @param stationId
     */
    private fun queryWarningIdByStationId(stationId: String): String? {
        var warningId: String? = null
        val dbManager = DBManager(this)
        dbManager.openDateBase()
        val database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null)
        try {
            if (database != null && database.isOpen) {
                val cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME1 + " where SID = " + stationId, null)
                for (i in 0 until cursor.count) {
                    cursor.moveToPosition(i)
                    warningId = cursor.getString(cursor.getColumnIndex("WARNID"))
                }
                cursor.close()
                dbManager.closeDatabase()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return warningId
    }

    /**
     * 获取预警信息
     */
    private fun okHttpWarning(url: String) {
        Thread(Runnable {
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
                            val `object` = JSONObject(result)
                            if (!`object`.isNull("data")) {
                                warningList.clear()
                                val jsonArray = `object`.getJSONArray("data")
                                for (i in 0 until jsonArray.length()) {
                                    val tempArray = jsonArray.getJSONArray(i)
                                    val dto = WarningDto()
                                    dto.html = tempArray.getString(1)
                                    val array = dto.html.split("-").toTypedArray()
                                    val item0 = array[0]
                                    val item1 = array[1]
                                    val item2 = array[2]
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

    /**
     * 实况监测详情
     */
    private fun okHttpDetail(stationids: String, interfaceType: String) {
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(SecretUrlUtil.stationDetail(stationids, interfaceType)).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            val array = JSONArray(result)
                            val dto = StationMonitorDto()
                            val obj = array.getJSONObject(0)
                            if (!obj.isNull("present")) {
                                val presentObj = obj.getJSONObject("present")
                                if (!presentObj.isNull("atballtemp")) {
                                    val value = presentObj.getString("atballtemp")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.currentTemp = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.currentTemp = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.currentTemp = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.currentTemp = CONST.noValue
                                    }
                                } else {
                                    dto.currentTemp = CONST.noValue
                                }
                                if (!presentObj.isNull("atprecipitation1h")) {
                                    val value = presentObj.getString("atprecipitation1h")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.current1hRain = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.current1hRain = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.current1hRain = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.current1hRain = CONST.noValue
                                    }
                                } else {
                                    dto.current1hRain = CONST.noValue
                                }
                                if (!presentObj.isNull("athumidity")) {
                                    val value = presentObj.getString("athumidity")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.currentHumidity = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.currentHumidity = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.currentHumidity = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.currentHumidity = CONST.noValue
                                    }
                                } else {
                                    dto.currentHumidity = CONST.noValue
                                }
                                if (!presentObj.isNull("atwindspeed")) {
                                    val value = presentObj.getString("atwindspeed")
                                    if (!TextUtils.isDigitsOnly(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.currentWindSpeed = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.currentWindSpeed = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.currentWindSpeed = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.currentWindSpeed = CONST.noValue
                                    }
                                } else {
                                    dto.currentWindSpeed = CONST.noValue
                                }
                                if (!presentObj.isNull("atairpressure")) {
                                    val value = presentObj.getString("atairpressure")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.currentPressure = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.currentPressure = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.currentPressure = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.currentPressure = CONST.noValue
                                    }
                                } else {
                                    dto.currentPressure = CONST.noValue
                                }
                                if (!presentObj.isNull("atvisibility")) {
                                    val value = presentObj.getString("atvisibility")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.currentVisible = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    val f = value.toDouble() / 1000f
                                                    val b = BigDecimal(f)
                                                    val f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).toFloat()
                                                    dto.currentVisible = f1.toString().substring(0, f1.toString().indexOf("."))
                                                } else {
                                                    val f = value.toDouble() / 1000f
                                                    val b = BigDecimal(f)
                                                    val f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).toFloat()
                                                    dto.currentVisible = f1.toString()
                                                }
                                            }
                                        }
                                    } else {
                                        dto.currentVisible = CONST.noValue
                                    }
                                } else {
                                    dto.currentVisible = CONST.noValue
                                }
                            }
                            if (!obj.isNull("statistics")) {
                                val statisticsObj = obj.getJSONObject("statistics")
                                if (!statisticsObj.isNull("maxtemperature")) {
                                    val value = statisticsObj.getString("maxtemperature")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statisHighTemp = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statisHighTemp = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statisHighTemp = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statisHighTemp = CONST.noValue
                                    }
                                } else {
                                    dto.statisHighTemp = CONST.noValue
                                }
                                if (!statisticsObj.isNull("mintemperature")) {
                                    val value = statisticsObj.getString("mintemperature")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statisLowTemp = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statisLowTemp = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statisLowTemp = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statisLowTemp = CONST.noValue
                                    }
                                } else {
                                    dto.statisLowTemp = CONST.noValue
                                }
                                if (!statisticsObj.isNull("mean")) {
                                    val value = statisticsObj.getString("mean")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statisAverTemp = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statisAverTemp = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statisAverTemp = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statisAverTemp = CONST.noValue
                                    }
                                } else {
                                    dto.statisAverTemp = CONST.noValue
                                }
                                if (!statisticsObj.isNull("rainfall3")) {
                                    val value = statisticsObj.getString("rainfall3")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statis3hRain = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statis3hRain = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statis3hRain = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statis3hRain = CONST.noValue
                                    }
                                } else {
                                    dto.statis3hRain = CONST.noValue
                                }
                                if (!statisticsObj.isNull("rainfall6")) {
                                    val value = statisticsObj.getString("rainfall6")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statis6hRain = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statis6hRain = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statis6hRain = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statis6hRain = CONST.noValue
                                    }
                                } else {
                                    dto.statis6hRain = CONST.noValue
                                }
                                if (!statisticsObj.isNull("rainfall12")) {
                                    val value = statisticsObj.getString("rainfall12")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statis12hRain = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statis12hRain = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statis12hRain = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statis12hRain = CONST.noValue
                                    }
                                } else {
                                    dto.statis12hRain = CONST.noValue
                                }
                                if (!statisticsObj.isNull("rainfall24")) {
                                    val value = statisticsObj.getString("rainfall24")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statis24hRain = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statis24hRain = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statis24hRain = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statis24hRain = CONST.noValue
                                    }
                                } else {
                                    dto.statis24hRain = CONST.noValue
                                }
                                if (!statisticsObj.isNull("maxhumidity")) {
                                    val value = statisticsObj.getString("maxhumidity")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statisMaxHumidity = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statisMaxHumidity = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statisMaxHumidity = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statisMaxHumidity = CONST.noValue
                                    }
                                } else {
                                    dto.statisMaxHumidity = CONST.noValue
                                }
                                if (!statisticsObj.isNull("minhumidity")) {
                                    val value = statisticsObj.getString("minhumidity")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statisMinHumidity = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statisMinHumidity = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statisMinHumidity = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statisMinHumidity = CONST.noValue
                                    }
                                } else {
                                    dto.statisMinHumidity = CONST.noValue
                                }
                                if (!statisticsObj.isNull("maxwindspeed")) {
                                    val value = statisticsObj.getString("maxwindspeed")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statisMaxSpeed = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statisMaxSpeed = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statisMaxSpeed = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statisMaxSpeed = CONST.noValue
                                    }
                                } else {
                                    dto.statisMaxSpeed = CONST.noValue
                                }
                                if (!statisticsObj.isNull("maxpressure")) {
                                    val value = statisticsObj.getString("maxpressure")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statisMaxPressure = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statisMaxPressure = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statisMaxPressure = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statisMaxPressure = CONST.noValue
                                    }
                                } else {
                                    dto.statisMaxPressure = CONST.noValue
                                }
                                if (!statisticsObj.isNull("minpressure")) {
                                    val value = statisticsObj.getString("minpressure")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statisMinPressure = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    dto.statisMinPressure = value.substring(0, value.indexOf("."))
                                                } else {
                                                    dto.statisMinPressure = value
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statisMinPressure = CONST.noValue
                                    }
                                } else {
                                    dto.statisMinPressure = CONST.noValue
                                }
                                if (!statisticsObj.isNull("minvisibility")) {
                                    val value = statisticsObj.getString("minvisibility")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                dto.statisMinVisible = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    val f = value.toDouble() / 1000f
                                                    val b = BigDecimal(f)
                                                    val f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).toFloat()
                                                    dto.statisMinVisible = f1.toString().substring(0, f1.toString().indexOf("."))
                                                } else {
                                                    val f = value.toDouble() / 1000f
                                                    val b = BigDecimal(f)
                                                    val f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).toFloat()
                                                    dto.statisMinVisible = f1.toString()
                                                }
                                            }
                                        }
                                    } else {
                                        dto.statisMinVisible = CONST.noValue
                                    }
                                } else {
                                    dto.statisMinVisible = CONST.noValue
                                }
                            }
                            val itemArray = obj.getJSONArray("24H")
                            val tempList: MutableList<StationMonitorDto> = ArrayList()
                            for (i in 0 until itemArray.length()) {
                                val itemObj = itemArray.getJSONObject(i)
                                val data = StationMonitorDto()
                                if (!itemObj.isNull("datatime")) {
                                    data.time = itemObj.getString("datatime")
                                }
                                if (!itemObj.isNull("balltemp")) {
                                    val value = itemObj.getString("balltemp")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                data.ballTemp = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    data.ballTemp = value.substring(0, value.indexOf("."))
                                                } else {
                                                    data.ballTemp = value
                                                }
                                            }
                                        }
                                    } else {
                                        data.ballTemp = CONST.noValue
                                    }
                                } else {
                                    data.ballTemp = CONST.noValue
                                }
                                if (!itemObj.isNull("precipitation1h")) {
                                    val value = itemObj.getString("precipitation1h")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                data.precipitation1h = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    data.precipitation1h = value.substring(0, value.indexOf("."))
                                                } else {
                                                    data.precipitation1h = value
                                                }
                                            }
                                        }
                                    } else {
                                        data.precipitation1h = CONST.noValue
                                    }
                                } else {
                                    data.precipitation1h = CONST.noValue
                                }
                                if (!itemObj.isNull("humidity")) {
                                    val value = itemObj.getString("humidity")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                data.humidity = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    data.humidity = value.substring(0, value.indexOf("."))
                                                } else {
                                                    data.humidity = value
                                                }
                                            }
                                        }
                                    } else {
                                        data.humidity = CONST.noValue
                                    }
                                } else {
                                    data.humidity = CONST.noValue
                                }
                                if (!itemObj.isNull("windspeed")) {
                                    val value = itemObj.getString("windspeed")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                data.windSpeed = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    data.windSpeed = value.substring(0, value.indexOf("."))
                                                } else {
                                                    data.windSpeed = value
                                                }
                                            }
                                        }
                                    } else {
                                        data.windSpeed = CONST.noValue
                                    }
                                } else {
                                    data.windSpeed = CONST.noValue
                                }
                                if (!itemObj.isNull("winddir")) {
                                    val value = itemObj.getString("winddir")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                data.windDir = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    data.windDir = value.substring(0, value.indexOf("."))
                                                } else {
                                                    data.windDir = value
                                                }
                                            }
                                        }
                                    } else {
                                        data.windDir = CONST.noValue
                                    }
                                } else {
                                    data.windDir = CONST.noValue
                                }
                                if (!itemObj.isNull("airpressure")) {
                                    val value = itemObj.getString("airpressure")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                data.airPressure = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    data.airPressure = value.substring(0, value.indexOf("."))
                                                } else {
                                                    data.airPressure = value
                                                }
                                            }
                                        }
                                    } else {
                                        data.airPressure = CONST.noValue
                                    }
                                } else {
                                    data.airPressure = CONST.noValue
                                }
                                if (!itemObj.isNull("visibility")) {
                                    val value = itemObj.getString("visibility")
                                    if (!TextUtils.isEmpty(value)) {
                                        if (value.length >= 2 && value.contains(".")) {
                                            if (value == ".0") {
                                                data.visibility = "0"
                                            } else {
                                                if (TextUtils.equals(value.substring(value.length - 2, value.length), ".0")) {
                                                    val f = value.toDouble() / 1000f
                                                    val b = BigDecimal(f)
                                                    val f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).toFloat()
                                                    data.visibility = f1.toString().substring(0, f1.toString().indexOf("."))
                                                } else {
                                                    val f = value.toDouble() / 1000f
                                                    val b = BigDecimal(f)
                                                    val f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).toFloat()
                                                    data.visibility = f1.toString()
                                                }
                                            }
                                        }
                                    } else {
                                        data.visibility = CONST.noValue
                                    }
                                } else {
                                    data.visibility = CONST.noValue
                                }
                                tempList.add(data)
                            }
                            dto.dataList.addAll(tempList)
                            runOnUiThread {
                                initViewPager(dto)
                                cancelDialog()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }).start()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.ivShare -> {
                val bitmap1 = CommonUtil.captureView(reTitle)
                val bitmap2 = CommonUtil.captureMyView(viewPager)
                val bitmap3 = CommonUtil.mergeBitmap(this@FactRankDetailActivity, bitmap1, bitmap2, false)
                CommonUtil.clearBitmap(bitmap1)
                CommonUtil.clearBitmap(bitmap2)
                val bitmap4 = BitmapFactory.decodeResource(resources, R.drawable.legend_share_landscape)
                val bitmap = CommonUtil.mergeBitmap(this@FactRankDetailActivity, bitmap3, bitmap4, false)
                CommonUtil.clearBitmap(bitmap3)
                CommonUtil.clearBitmap(bitmap4)
                CommonUtil.share(this@FactRankDetailActivity, bitmap)
            }
        }
    }
	
}
