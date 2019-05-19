package com.china.fragment;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.common.CONST;
import com.china.dto.StationMonitorDto;
import com.china.dto.WarningDto;
import com.china.utils.CommonUtil;
import com.china.view.ShawnWindView;

import java.util.ArrayList;
import java.util.List;

/**
 * 实况站点详情-风速风向
 */
public class ShawnFactDetailWindFragment extends Fragment{
	
	private LinearLayout llContainer1,llContent;
	private List<WarningDto> warningList = new ArrayList<>();
	private int viewWidth,width,height;
	private float density = 0;
	private boolean isPortrait = true;//判断默认进来是否为竖屏
	private ShawnWindView windView = null;
	private TextView tvPrompt = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.shawn_fragment_fact_detail_wind, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (isPortrait) {//默认竖屏进来
			if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
				viewWidth = width;
				showPortrait();
			}else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				viewWidth = height;
				showLandscape();
			}
		}else {//默认横屏进来
			if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
				viewWidth = height;
				showPortrait();
			}else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				viewWidth = width;
				showLandscape();
			}
		}
	}
	
	private void initWidget(View view) {
		TextView tvCurrentSpeed = view.findViewById(R.id.tvCurrentSpeed);
		TextView tvMaxSpeed = view.findViewById(R.id.tvMaxSpeed);
		TextView tv1 = view.findViewById(R.id.tv1);
		TextView tv2 = view.findViewById(R.id.tv2);
		llContainer1 = view.findViewById(R.id.llContainer1);
		llContent = view.findViewById(R.id.llContent);
		tvPrompt = view.findViewById(R.id.tvPrompt);
		
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		density = dm.density;

		StationMonitorDto data = getArguments().getParcelable("data");
		if (data != null) {
			if (!TextUtils.equals(data.currentWindSpeed, CONST.noValue)) {
				tvCurrentSpeed.setText(data.currentWindSpeed);
				tv1.setText(getString(R.string.unit_speed));
			}else {
				tvCurrentSpeed.setText(CONST.noValue);
			}
			
			if (!TextUtils.equals(data.statisMaxSpeed, CONST.noValue)) {
				tvMaxSpeed.setText(data.statisMaxSpeed);
				tv2.setText(getString(R.string.unit_speed));
			}else {
				tvMaxSpeed.setText(CONST.noValue);
			}
			
			warningList.clear();
			warningList.addAll(getArguments().<WarningDto>getParcelableArrayList("warningList"));
			windView = new ShawnWindView(getActivity());
			windView.setData(data.dataList, warningList);
			
			if (width < height) {
				isPortrait = true;
				viewWidth = width;
				showPortrait();
			}else {
				isPortrait = false;
				viewWidth = width;
				showLandscape();
			}
		}
	}
	
	private void showPortrait() {
		tvPrompt.setVisibility(View.GONE);
		llContent.setVisibility(View.VISIBLE);
		llContainer1.removeAllViews();
		llContainer1.addView(windView, (int)(CommonUtil.dip2px(getActivity(), viewWidth)), LinearLayout.LayoutParams.MATCH_PARENT);
	}
	
	private void showLandscape() {
		tvPrompt.setVisibility(View.VISIBLE);
		llContent.setVisibility(View.GONE);
		llContainer1.removeAllViews();
		llContainer1.addView(windView, (int)(CommonUtil.dip2px(getActivity(), viewWidth/density)), LinearLayout.LayoutParams.MATCH_PARENT);
	}
	
}
