package com.china.activity

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import com.china.R
import com.china.adapter.ShawnCityAdapter
import com.china.adapter.ShawnCityHotAdapter
import com.china.common.CONST
import com.china.dto.CityDto
import com.china.manager.DBManager
import com.china.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_city.*
import kotlinx.android.synthetic.main.shawn_layout_title.*
import java.util.*

/**
 * 城市查询
 */
class CityActivity : ShawnBaseActivity(), OnClickListener {

    //搜索城市后的结果列表
    private var searchAdapter : ShawnCityAdapter? = null
    private val searchList : ArrayList<CityDto> = ArrayList()

    //全国热门
    private val hotList : ArrayList<CityDto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city)
        initWidget()
        initListView()
        initGridView()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        etSearch.addTextChangedListener(watcher)
        tvNational.setOnClickListener(this)
        llBack.setOnClickListener(this)
        tvTitle.text = "城市查询"

        val columnId = intent.getStringExtra(CONST.COLUMN_ID)
        CommonUtil.submitClickCount(columnId, "城市查询")
    }

    private val watcher = object : TextWatcher {
        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun afterTextChanged(arg0: Editable) {
            searchList.clear()
            if (arg0.toString().trim() == "") {
                if (listView != null) {
                    listView.visibility = View.GONE
                }
                llNation.visibility = View.VISIBLE
                if (gridView != null) {
                    gridView.visibility = View.VISIBLE
                }
            } else {
                if (listView != null) {
                    listView.visibility = View.VISIBLE
                }
                llNation.visibility = View.GONE
                if (gridView != null) {
                    gridView.visibility = View.GONE
                }
                getCityInfo(arg0.toString().trim())
            }
        }
    }

    /**
     * 迁移到天气详情界面
     */
    private fun intentWeatherDetail(data : CityDto) {
        val bundle = Bundle()
        bundle.putParcelable("data", data)
        var i : Intent? = null
        if (intent.hasExtra("reserveCity")) {
            i = Intent()
            i.putExtras(bundle)
            setResult(RESULT_OK, i)
            finish()
        }else {
            i = Intent(this, ShawnForecastActivity::class.java)
            i.putExtra("cityName", data.areaName)
            i.putExtra("cityId", data.cityId)
            startActivity(i)
        }
    }

    /**
     * 初始化listview
     */
    private fun initListView() {
        searchAdapter = ShawnCityAdapter(this, searchList)
        listView.adapter = searchAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            intentWeatherDetail(searchList[position])
        }
    }

    /**
     * 初始化全国热门
     */
    private fun initGridView() {
        hotList.clear()
        val stations = resources.getStringArray(R.array.nation_hotCity)
        for (i in 0 until stations.size) {
            val value = stations[i].split(",")
            val dto = CityDto()
            dto.lng = value[3].toDouble()
            dto.lat = value[2].toDouble()
            dto.cityId = value[0]
            dto.areaName = value[1]
            hotList.add(dto)
        }

        val nAdapter = ShawnCityHotAdapter(this, hotList)
        gridView.adapter = nAdapter
        gridView.setOnItemClickListener { parent, view, position, id ->
            intentWeatherDetail(hotList[position])
        }
    }

    /**
     * 获取城市信息
     */
    private fun getCityInfo(keyword : String) {
        searchList.clear()
        val dbManager = DBManager(this)
        dbManager.openDateBase()
        dbManager.closeDatabase()
        val database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null)
        val cursor = database.rawQuery("select * from "+DBManager.TABLE_NAME3+" where pro like "+"\"%"+keyword+"%\""+" or city like "+"\"%"+keyword+"%\""+" or dis like "+"\"%"+keyword+"%\"",null)
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            val dto = CityDto()
            dto.provinceName = cursor.getString(cursor.getColumnIndex("pro"))
            dto.cityName = cursor.getString(cursor.getColumnIndex("city"))
            dto.areaName = cursor.getString(cursor.getColumnIndex("dis"))
            dto.cityId = cursor.getString(cursor.getColumnIndex("cid"))
            dto.warningId = cursor.getString(cursor.getColumnIndex("wid"))
            searchList.add(dto)
        }
        cursor.close()

        if (searchAdapter != null) {
            searchAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.llBack -> finish()
        }
    }

}
