package com.china.activity;

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import com.china.R
import com.china.adapter.NewsAdapter
import com.china.common.CONST
import com.china.dto.NewsDto
import com.china.manager.MyCollectManager
import kotlinx.android.synthetic.main.activity_collection.*
import kotlinx.android.synthetic.main.layout_title.*
import java.util.*

/**
 * 我的收藏
 */
class CollectionActivity : BaseActivity(), OnClickListener {

    private var mAdapter: NewsAdapter? = null
    private val dataList: MutableList<NewsDto> = ArrayList() //存放收藏数据的list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)
        initWidget()
        initListView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
    }

    private fun getCollectData() {
        //判断是否是已收藏
        dataList.clear()
        val size = MyCollectManager.readCollect(this@CollectionActivity, dataList)
        if (size > 0) {
            if (mAdapter != null) {
                mAdapter!!.notifyDataSetChanged()
            }
            tvPrompt!!.visibility = View.GONE
        } else {
            tvPrompt!!.visibility = View.VISIBLE
        }
    }

    /**
     * 初始化listview
     */
    private fun initListView() {
        getCollectData()
        val mListView = findViewById<ListView>(R.id.listView)
        mAdapter = NewsAdapter(this, dataList)
        mListView.adapter = mAdapter
        mListView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = dataList[arg2]
            val intent = Intent(this, Webview2Activity::class.java)
            intent.putExtra("data", dto)
            intent.putExtra(CONST.ACTIVITY_NAME, dto.title)
            intent.putExtra(CONST.WEB_URL, dto.detailUrl)
            startActivityForResult(intent, 1001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1001 -> getCollectData()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
        }
    }

}
