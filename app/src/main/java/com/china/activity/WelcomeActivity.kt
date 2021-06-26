package com.china.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.Toast
import com.china.R
import com.china.common.CONST
import com.china.common.ColumnData
import com.china.common.MyApplication
import com.china.dto.NewsDto
import com.china.utils.CommonUtil
import com.china.utils.OkHttpUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.dialog_policy.view.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 欢迎界面
 */
class WelcomeActivity : BaseActivity() {

	private val dataList : ArrayList<ColumnData> = ArrayList()
	private val pdfList : ArrayList<NewsDto> = ArrayList()//pdf文档类

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_welcome)

		//点击Home键后再点击APP图标，APP重启而不是回到原来界面
		if (!isTaskRoot) {
			finish()
			return
		}
		//点击Home键后再点击APP图标，APP重启而不是回到原来界面

		okHttpTheme()

		if (!policyFlag()) {
			promptDialog()
		}else {
			init()
		}
	}

	/**
	 * 温馨提示对话框
	 */
	private fun promptDialog() {
		val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val view = inflater.inflate(R.layout.dialog_policy, null)

		val dialog = Dialog(this, R.style.CustomProgressDialog)
		dialog.setContentView(view)
		dialog.show()

		view.tvProtocal.setOnClickListener {
			val intent = Intent(this, WebviewActivity::class.java)
			intent.putExtra(CONST.ACTIVITY_NAME, "用户协议")
			intent.putExtra(CONST.WEB_URL, "http://decision-admin.tianqi.cn/Public/share/chinaweather_links/yhxy.html")
			startActivity(intent)
		}
		view.tvPolicy.setOnClickListener {
			val intent = Intent(this, WebviewActivity::class.java)
			intent.putExtra(CONST.ACTIVITY_NAME, "隐私政策")
			intent.putExtra(CONST.WEB_URL, "http://decision-admin.tianqi.cn/Public/share/chinaweather_links/yszc.html")
			startActivity(intent)
		}
		view.tvNegtive.setOnClickListener {
			dialog.dismiss()
			finish()
		}
		view.tvPositive.setOnClickListener {
			dialog.dismiss()
			savePolicyFlag()
			init()
		}
	}

	private fun savePolicyFlag() {
		val sp = getSharedPreferences("policy", Context.MODE_PRIVATE)
		val editor = sp.edit()
		editor.putBoolean("isShow", true)
		editor.apply()
	}

	private fun policyFlag() : Boolean {
		val sp = getSharedPreferences("policy", Context.MODE_PRIVATE)
		return sp.getBoolean("isShow", false)
	}

	private fun init() {
		Handler().postDelayed({
			val sharedPreferences = getSharedPreferences(CONST.SHOWGUIDE, Context.MODE_PRIVATE)
			val version = sharedPreferences.getString(CONST.VERSION, "")
			if (!TextUtils.equals(version, CommonUtil.getVersion(this))) {
				startActivity(Intent(this, GuideActivity::class.java))
				finish()
			}else {
				if (!TextUtils.isEmpty(MyApplication.USERNAME) && !TextUtils.isEmpty(MyApplication.PASSWORD)) {
					okHttpLogin()
				}else {
					startActivity(Intent(this, LoginActivity::class.java))
					finish()
				}
			}
		}, 1000)
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
		builder.add("device_id", CommonUtil.getUniqueId(this))
		builder.add("platform", "android")
		builder.add("os_version", android.os.Build.VERSION.RELEASE)
		builder.add("software_version", CommonUtil.getVersion(this))
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
					val result = response.body!!.string()
					runOnUiThread {
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
											val obj = JSONObject(obje.getString("info"))
											MyApplication.UID = obj.getString("id")
											MyApplication.USERGROUP = obj.getString("usergroup")
											MyApplication.saveUserInfo(this@WelcomeActivity)

											okHttpPushToken()

											val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
											val bundle = Bundle()
											bundle.putParcelableArrayList("dataList", dataList as ArrayList<out Parcelable>)
											bundle.putParcelableArrayList("pdfList", pdfList as ArrayList<out Parcelable>)
											intent.putExtras(bundle)
											startActivity(intent)
											finish()
										}
									}else {
										//失败
										if (!obje.isNull("msg")) {
											val msg = obje.getString("msg")
											if (msg != null) {
												Toast.makeText(this@WelcomeActivity, msg, Toast.LENGTH_SHORT).show()
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

	/**
	 * 获取主题
	 */
	private fun okHttpTheme() {
		val url = "https://decision-admin.tianqi.cn/Home/work2019/decision_theme_data?appid=${CONST.APPID}"
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {}
				@Throws(IOException::class)
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						if (!TextUtils.isEmpty(result)) {
							try {
								val obj = JSONObject(result)
								if (!obj.isNull("style")) {
									MyApplication.setTheme(obj.getString("style"))
								}
								if (!obj.isNull("launch_img")) {
									Picasso.get().load(obj.getString("launch_img")).into(imageView)
								}
								if (!obj.isNull("top_img")) {
									MyApplication.setTop_img(obj.getString("top_img"))
								}
								if (!obj.isNull("top_img_url")) {
									MyApplication.setTop_img_url(obj.getString("top_img_url"))
								}
								if (!obj.isNull("top_img_title")) {
									MyApplication.setTop_img_title(obj.getString("top_img_title"))
								}
							} catch (e: JSONException) {
								e.printStackTrace()
							}
						}
					}
				}
			})
		}).start()
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			return true
		}
		return super.onKeyDown(keyCode, event)
	}

	private fun okHttpPushToken() {
		if (TextUtils.equals(MyApplication.USERGROUP, "17")) {//公众用户不推送
			return
		}
		val url = "http://decision-admin.tianqi.cn/Home/work2019/savePushToken_zgqx"
		val builder = FormBody.Builder()
		val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
		val serial = Build.SERIAL
		builder.add("uuid", androidId+serial)
		builder.add("uid", MyApplication.UID)
		builder.add("groupid", MyApplication.USERGROUP)
		builder.add("pushtoken", MyApplication.DEVICETOKEN)
		builder.add("platform", "android")
		builder.add("um_key", MyApplication.appKey)
		val body = builder.build()
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url).post(body).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					Log.e("result", result)
				}
			})
		}).start()
	}

}
