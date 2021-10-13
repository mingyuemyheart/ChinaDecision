package com.china.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import com.china.R
import com.china.activity.LoginActivity
import com.china.activity.MainActivity
import com.china.common.CONST
import com.china.common.ColumnData
import com.china.common.MyApplication
import com.china.dto.NewsDto
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_guide.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 引导页
 */
class GuideFragment : Fragment(), OnClickListener {

	private val dataList : ArrayList<ColumnData> = ArrayList()
	private val pdfList : ArrayList<NewsDto> = ArrayList()//pdf文档类

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_guide, null)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initWidget()
	}

	private fun initWidget() {
		when (arguments!!.getInt("index")) {
			0 -> clMain.setBackgroundResource(R.drawable.bg_guide_01)
			1 -> clMain.setBackgroundResource(R.drawable.bg_guide_02)
			2 -> {
				clMain.setBackgroundResource(R.drawable.bg_guide_03)
				clMain.setOnClickListener(this)
			}
		}
	}

	/**
	 * 登录
	 */
	private fun okHttpLogin() {
		val url = "http://decision-admin.tianqi.cn/home/Work/login"
		val builder = FormBody.Builder()
		builder.add("username", MyApplication.USERNAME)
		builder.add("password", MyApplication.PASSWORD)
		builder.add("appid", CONST.APPID)
		builder.add("device_id", CommonUtil.getUniqueId(activity))
		builder.add("platform", "android")
		builder.add("os_version", android.os.Build.VERSION.RELEASE)
		builder.add("software_version", CommonUtil.getVersion(activity))
		builder.add("mobile_type", android.os.Build.MODEL)
		builder.add("address", "")
		builder.add("lat", "0'")
		builder.add("lng", "0")
		val body = builder.build()
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					if (!isAdded) {
						return
					}
					val result = response.body!!.string()
					activity!!.runOnUiThread {
						if (!TextUtils.isEmpty(result)) {
							try {
								val obje = JSONObject(result)
								if (!obje.isNull("status")) {
									val status  = obje.getInt("status")
									if (status == 1) {//成功
										val array = obje.getJSONArray("column")
										dataList.clear()
										for (i in 0 until array.length()) {
											val obj = array.getJSONObject(i)
											val data = ColumnData()
											if (!obj.isNull("id")) {
												data.columnId = obj.getString("id")
											}
											if (!obj.isNull("localviewid")) {
												data.id = obj.getString("localviewid")
											}
											if (!obj.isNull("name")) {
												data.name = obj.getString("name")
											}
											if (!obj.isNull("icon")) {
												data.icon = obj.getString("icon")
											}
											if (!obj.isNull("desc")) {
												data.desc = obj.getString("desc")
											}
											if (!obj.isNull("showtype")) {
												data.showType = obj.getString("showtype")
											}
											if (!obj.isNull("dataurl")) {
												data.dataUrl = obj.getString("dataurl")
											}
											if (!obj.isNull("child")) {
												val childArray = obj.getJSONArray("child")
												for (j in 0 until childArray.length()) {
													val childObj = childArray.getJSONObject(j)
													val dto = ColumnData()
													dto.groupColumnId = data.columnId
													if (!childObj.isNull("id")) {
														dto.columnId = childObj.getString("id")
													}
													if (!childObj.isNull("localviewid")) {
														dto.id = childObj.getString("localviewid")
													}
													if (!childObj.isNull("name")) {
														dto.name = childObj.getString("name")
													}
													if (!childObj.isNull("desc")) {
														dto.desc = childObj.getString("desc")
													}
													if (!childObj.isNull("icon")) {
														dto.icon = childObj.getString("icon")
													}
													if (!childObj.isNull("showtype")) {
														dto.showType = childObj.getString("showtype")
													}
													if (!childObj.isNull("dataurl")) {
														dto.dataUrl = childObj.getString("dataurl")
													}
													if (!childObj.isNull("child")) {
														val childArray2 = childObj.getJSONArray("child")
														for (m in 0 until childArray2.length()) {
															val childObj2 = childArray2.getJSONObject(m)
															val d = ColumnData()
															if (!childObj2.isNull("id")) {
																d.columnId = childObj2.getString("id")
															}
															if (!childObj2.isNull("localviewid")) {
																d.id = childObj2.getString("localviewid")
															}
															if (!childObj2.isNull("name")) {
																d.name = childObj2.getString("name")
															}
															if (!childObj2.isNull("desc")) {
																d.desc = childObj2.getString("desc")
															}
															if (!childObj2.isNull("icon")) {
																d.icon = childObj2.getString("icon")
															}
															if (!childObj2.isNull("showtype")) {
																d.showType = childObj2.getString("showtype")
															}
															if (!childObj2.isNull("dataurl")) {
																d.dataUrl = childObj2.getString("dataurl")
															}
															dto.child.add(d)
														}
													}
													data.child.add(dto)
												}
											}
											dataList.add(data)
										}

										if (!obje.isNull("appinfo")) {
											val obj = obje.getJSONObject("appinfo")
											if (!obj.isNull("counturl")) {
												CONST.COUNTURL = obj.getString("counturl")
											}
											if (!obj.isNull("recommendurl")) {
												CONST.RECOMMENDURL = obj.getString("recommendurl")
											}
											if (!obj.isNull("news")) {
												pdfList.clear()
												val newsArray = obj.getJSONArray("news")
												for (i in 0 until newsArray.length()) {
													val itemObj = newsArray.getJSONObject(i)
													val dto = NewsDto()
													if (!itemObj.isNull("header")) {
														dto.header = "【"+itemObj.getString("header")+"】"
													}
													if (!itemObj.isNull("name")) {
														dto.title = dto.header+itemObj.getString("name")
													}
													if (!itemObj.isNull("url")) {
														dto.detailUrl = itemObj.getString("url")
													}
													if (!itemObj.isNull("time")) {
														dto.time = itemObj.getString("time")
													}
													if (!itemObj.isNull("flagImg")) {
														dto.imgUrl = itemObj.getString("flagImg")
													}
													pdfList.add(dto)
												}
											}
										}

										if (!obje.isNull("info")) {
											val intent = Intent(activity, MainActivity::class.java)
											val bundle = Bundle()
											bundle.putParcelableArrayList("dataList", dataList as ArrayList<out Parcelable>)
											bundle.putParcelableArrayList("pdfList", pdfList as ArrayList<out Parcelable>)
											intent.putExtras(bundle)
											startActivity(intent)
											activity!!.finish()
										}
									}else {
										//失败
										if (!obje.isNull("msg")) {
											val msg = obje.getString("msg")
											if (msg != null) {
												Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
											}
										}
									}
								}
							} catch (e : JSONException) {
								e.printStackTrace()
							}
						}
					}
				}
			})
		}).start()
	}

	override fun onClick(v: View?) {
		when(v!!.id) {
			R.id.clMain -> {
				val sharedPreferences = activity!!.getSharedPreferences(CONST.SHOWGUIDE, Context.MODE_PRIVATE)
				val editor = sharedPreferences.edit()
				editor.putString(CONST.VERSION, CommonUtil.getVersion(activity))
				editor.apply()

				if (!TextUtils.isEmpty(MyApplication.USERNAME) && !TextUtils.isEmpty(MyApplication.PASSWORD)) {
					okHttpLogin()
				}else {
					startActivity(Intent(activity, LoginActivity::class.java))
					activity!!.finish()
				}
			}
		}
	}

}
