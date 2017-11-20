package com.china.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.StationMonitorDto;

import java.util.HashMap;
import java.util.List;

public class StationMonitorRankSearchAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<StationMonitorDto> mArrayList = null;
	public HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();
	
	private final class ViewHolder {
		TextView tvName;//预警信息名称
	}
	
	private ViewHolder mHolder = null;
	
	public StationMonitorRankSearchAdapter(Context context, List<StationMonitorDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for (int i = 0; i < mArrayList.size(); i++) {
			if (i == 0) {
				isSelected.put(i, true);
			}else {
				isSelected.put(i, false);
			}
		}
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
			convertView = mInflater.inflate(R.layout.station_monitor_rank_search_item, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		StationMonitorDto dto = mArrayList.get(position);
		mHolder.tvName.setText(dto.provinceName);
		if (isSelected.get(position) == true) {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.white));
			mHolder.tvName.setBackgroundResource(R.drawable.bg_warning_selected);
		}else {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.text_color4));
			mHolder.tvName.setBackgroundColor(mContext.getResources().getColor(R.color.white));
		}
		
		return convertView;
	}

}
