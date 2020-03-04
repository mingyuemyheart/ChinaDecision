package com.china.fragment

import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.china.R
import com.china.common.CONST
import com.china.dto.WarningDto
import com.china.manager.DBManager
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_warning_detail.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * 预警详情
 */
class WarningDetailFragment : Fragment() {
	
	private var data : WarningDto? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_warning_detail, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRefreshLayout()
        initWidget()
    }

	/**
	 * 初始化下拉刷新布局
	 */
	private fun initRefreshLayout() {
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
		refreshLayout.setProgressViewEndTarget(true, 300)
		refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener {
            refresh()
        }
	}
	
	private fun refresh() {
		data = arguments!!.getParcelable("data")
		if (data != null && !TextUtils.isEmpty(data!!.html)) {
			okHttpWarningDetail(data!!.html)
		}
	}
	
	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		refresh()
	}
	
	/**
	 * 获取预警详情
	 */
	private fun okHttpWarningDetail(html : String) {
		val url = "https://decision-admin.tianqi.cn/Home/work2019/getDetailWarn/identifier/$html"
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    if (activity == null || !isAdded) {
                        return
                    }
                    activity!!.runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("sendTime")) {
                                    tvTime.text = "发布时间："+obj.getString("sendTime")
                                }

                                if (!obj.isNull("description")) {
                                    tvIntro.text = obj.getString("description")
                                }

                                if (!obj.isNull("headline")) {
                                    val headline = obj.getString("headline")
                                    if (!TextUtils.isEmpty(headline)) {
                                        tvName.text = headline.replace("发布", "发布\n")
                                    }
                                }

                                val color = obj.getString("severityCode")
                                val type = obj.getString("eventType")
                                var bitmap : Bitmap? = null
                                when (color) {
                                    CONST.blue[0] -> bitmap = CommonUtil.getImageFromAssetsFile(activity,"warning/"+type+CONST.blue[1]+CONST.imageSuffix)
                                    CONST.yellow[0] -> bitmap = CommonUtil.getImageFromAssetsFile(activity,"warning/"+type+CONST.yellow[1]+CONST.imageSuffix)
                                    CONST.orange[0] -> bitmap = CommonUtil.getImageFromAssetsFile(activity,"warning/"+type+CONST.orange[1]+CONST.imageSuffix)
                                    CONST.red[0] -> bitmap = CommonUtil.getImageFromAssetsFile(activity,"warning/"+type+CONST.red[1]+CONST.imageSuffix)
                                }
                                if (bitmap == null) {
                                    bitmap = CommonUtil.getImageFromAssetsFile(activity,"warning/"+"default"+CONST.imageSuffix)
                                }
                                imageView.setImageBitmap(bitmap)

                                initDBManager()
                                scrollView.visibility = View.VISIBLE
                                refreshLayout.isRefreshing = false
                            } catch (e : JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }).start()
	}

	/**
	 * 初始化数据库
	 */
	private fun initDBManager() {
		val dbManager = DBManager(activity)
		dbManager.openDateBase()
		dbManager.closeDatabase()
		val database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null)
		val cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + data!!.type+data!!.color + "\"",null)
		var content : String? = null
		for (i in 0 until cursor.count) {
			cursor.moveToPosition(i)
			content = cursor.getString(cursor.getColumnIndex("WarningGuide"))
		}
		cursor.close()
		if (!TextUtils.isEmpty(content)) {
			tvGuide.text = "预警指南：\n$content"
			tvGuide.visibility = View.VISIBLE
		}else {
			tvGuide.visibility = View.GONE
		}
	}

}
