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
import com.china.activity.ShawnPDFActivity;
import com.china.common.CONST;
import com.china.dto.NewsDto;
import com.squareup.picasso.Picasso;

/**
 * 首页pdf文档
 */
public class ShawnPdfFragment extends Fragment implements View.OnClickListener{

    private NewsDto data;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shawn_fragment_pdf, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initWidget(view);
    }

    private void initWidget(View view) {
        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setOnClickListener(this);

        data = getArguments().getParcelable("data");
        if (data != null) {
            if (!TextUtils.isEmpty(data.title)) {
                tvTitle.setText("【"+data.header+"】"+data.title);
            }
            if (!TextUtils.isEmpty(data.imgUrl)) {
                Picasso.get().load(data.imgUrl).into(imageView);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView:
            case R.id.tvTitle:
                Intent intent = new Intent(getActivity(), ShawnPDFActivity.class);
                if (data != null) {
                    intent.putExtra(CONST.ACTIVITY_NAME, data.title);
                    intent.putExtra(CONST.WEB_URL, data.detailUrl);
                }
                startActivity(intent);
                break;
        }
    }
}
