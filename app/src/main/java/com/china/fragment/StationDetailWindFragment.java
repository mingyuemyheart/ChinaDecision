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
import com.china.view.WindView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StationDetailWindFragment extends Fragment{
	
	private TextView tvCurrentSpeed = null;//当前风速
	private TextView tvMaxSpeed = null;//最大风速
	private TextView tv1, tv2;
	private LinearLayout llContainer1 = null;
	private LinearLayout llContent = null;
	private StationMonitorDto data = null;
	private List<WarningDto> warningList = new ArrayList<>();
	private int viewWidth = 0;
	private int width = 0;
	private int height = 0;
	private float density = 0;
	private boolean isPortrait = true;//判断默认进来是否为竖屏
	private WindView windView = null;
	private TextView tvPrompt = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.station_detail_wind_fragment, null);
		return view;
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
	
	@SuppressWarnings("unchecked")
	private void initWidget(View view) {
		tvCurrentSpeed = (TextView) view.findViewById(R.id.tvCurrentSpeed);
		tvMaxSpeed = (TextView) view.findViewById(R.id.tvMaxSpeed);
		tv1 = (TextView) view.findViewById(R.id.tv1);
		tv2 = (TextView) view.findViewById(R.id.tv2);
		llContainer1 = (LinearLayout) view.findViewById(R.id.llContainer1);
		llContent = (LinearLayout) view.findViewById(R.id.llContent);
		tvPrompt = (TextView) view.findViewById(R.id.tvPrompt);
		
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		density = dm.density;
		
		data = (StationMonitorDto) getArguments().getSerializable("data");
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
			windView = new WindView(getActivity());
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
