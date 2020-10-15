package com.china.activity

import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.china.R
import com.china.common.CONST
import com.china.dto.WarningDto
import com.china.manager.DBManager
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_warning_detail.*
import kotlinx.android.synthetic.main.layout_title.*
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
class WarningDetailActivity : BaseActivity(), OnClickListener {
	
	private var data : WarningDto? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_warning_detail)
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
		if (intent.hasExtra("data")) {
			data = intent.extras.getParcelable("data")
			if (data != null && !TextUtils.isEmpty(data!!.html)) {
				okHttpWarningDetail(data!!.html)
			}
		}
	}
	
	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvTitle.text = "预警详情"
		ivShare.setOnClickListener(this)

		refresh()
	}
	
	/**
	 * 获取预警详情
	 */
	private fun okHttpWarningDetail(html : String) {
		Thread(Runnable {
			val url = "https://decision-admin.tianqi.cn/Home/work2019/getDetailWarn/identifier/$html"
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
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
									CONST.blue[0] -> bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+type+CONST.blue[1]+CONST.imageSuffix)
									CONST.yellow[0] -> bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+type+CONST.yellow[1]+CONST.imageSuffix)
									CONST.orange[0] -> bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+type+CONST.orange[1]+CONST.imageSuffix)
									CONST.red[0] -> bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+type+CONST.red[1]+CONST.imageSuffix)
								}
								if (bitmap == null) {
									bitmap = CommonUtil.getImageFromAssetsFile(this@WarningDetailActivity,"warning/"+"default"+CONST.imageSuffix)
								}
								imageView.setImageBitmap(bitmap)

								initDBManager()
								ivShare.visibility = View.VISIBLE
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
		val dbManager = DBManager(this)
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

	override fun onClick(v: View?) {
		when(v!!.id) {
			R.id.llBack -> finish()
			R.id.ivShare -> {
				val bitmap1 = CommonUtil.captureScrollView(scrollView)
				val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.shawn_legend_share_portrait)
				val bitmap = CommonUtil.mergeBitmap(this, bitmap1, bitmap2, false)
				CommonUtil.clearBitmap(bitmap1)
				CommonUtil.clearBitmap(bitmap2)
				CommonUtil.share(this@WarningDetailActivity, bitmap)
			}
		}
	}

}
