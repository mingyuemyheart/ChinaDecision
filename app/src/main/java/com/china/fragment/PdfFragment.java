package com.china.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.R;
import com.china.activity.PDFActivity;
import com.china.common.CONST;
import com.china.dto.NewsDto;

import net.tsz.afinal.FinalBitmap;

/**
 * 首页pdf文档
 */

public class PdfFragment extends Fragment implements View.OnClickListener{

    private ImageView imageView;
    private TextView tvTitle;
    private NewsDto data;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdf, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initWidget(view);
    }

    private void initWidget(View view) {
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvTitle.setOnClickListener(this);

        data = getArguments().getParcelable("data");
        if (data != null) {
            if (!TextUtils.isEmpty(data.imgUrl)) {
                FinalBitmap finalBitmap = FinalBitmap.create(getActivity());
                finalBitmap.display(imageView, data.imgUrl, null, 0);
            }else {
                imageView.setImageResource(R.drawable.iv_pdf);
            }
            if (!TextUtils.isEmpty(data.title)) {
                tvTitle.setText(data.title);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView:
            case R.id.tvTitle:
                Intent intent = new Intent(getActivity(), PDFActivity.class);
                intent.putExtra(CONST.ACTIVITY_NAME, data.title);
                intent.putExtra(CONST.WEB_URL, data.detailUrl);
                startActivity(intent);
                break;
        }
    }
}
