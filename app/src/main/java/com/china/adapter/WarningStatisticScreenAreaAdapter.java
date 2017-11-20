package com.china.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.CityDto;
import com.china.dto.WarningDto;

import java.util.ArrayList;
import java.util.List;

/**
 * 预警筛选选择区域
 */

public class WarningStatisticScreenAreaAdapter extends BaseAdapter{

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<WarningDto> mArrayList = new ArrayList<>();

	private final class ViewHolder{
		TextView tvName;
	}

	private ViewHolder mHolder = null;

	public WarningStatisticScreenAreaAdapter(Context context, List<WarningDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_warning_statistic_screen_area, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.areaName)) {
			mHolder.tvName.setText(dto.areaName);
		}

		return convertView;
	}

}
