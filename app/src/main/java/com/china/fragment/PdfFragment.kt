package com.china.fragment

import android.app.Fragment
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.china.R
import com.china.activity.PDFActivity
import com.china.activity.WebviewActivity
import com.china.common.CONST
import com.china.common.MyApplication
import com.china.dto.NewsDto
import com.china.utils.CommonUtil
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_pdf.*

/**
 * 首页pdf文档
 */
class PdfFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pdf, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
    }

    private fun initWidget() {
        val data : NewsDto = arguments.getParcelable("data")
        if (!TextUtils.isEmpty(data.title)) {
            tvTitle.text = data.title
        }

        if (TextUtils.equals("1", MyApplication.getAppTheme())) {
            ivLouder.setImageBitmap(CommonUtil.grayScaleImage(BitmapFactory.decodeResource(resources, R.drawable.shawn_icon_month_news)))
        }
        if (!TextUtils.isEmpty(data.imgUrl)) {
            if (TextUtils.equals("1", MyApplication.getAppTheme())) {
                Picasso.get().load(data.imgUrl).into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: LoadedFrom?) {
                        imageView.setImageBitmap(CommonUtil.grayScaleImage(bitmap))
                    }
                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
            } else {
                Picasso.get().load(data.imgUrl).into(imageView)
            }
        }

        llContent.setOnClickListener {
            val intent = if (TextUtils.equals(data.showType, CONST.URL)) {
                Intent(activity, WebviewActivity::class.java)
            } else {
                Intent(activity, PDFActivity::class.java)
            }
            intent.putExtra(CONST.ACTIVITY_NAME, data.title)
            intent.putExtra(CONST.WEB_URL, data.detailUrl)
            startActivity(intent)
        }
    }

}
