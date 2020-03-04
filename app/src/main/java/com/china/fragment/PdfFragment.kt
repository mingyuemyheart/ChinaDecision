package com.china.fragment

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.china.R
import com.china.activity.PDFActivity
import com.china.activity.WebviewActivity
import com.china.common.CONST
import com.china.dto.NewsDto
import com.squareup.picasso.Picasso
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
        if (!TextUtils.isEmpty(data.imgUrl)) {
            Picasso.get().load(data.imgUrl).into(imageView)
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
