package com.china.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.StationMonitorDto;

import java.util.ArrayList;
import java.util.List;

/**
 * 实况排行
 */

public class StationMonitorRankAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<StationMonitorDto> mArrayList = new ArrayList<>();
	
	private final class ViewHolder{
		TextView tvNum;
		TextView tvName;
		TextView tvValue;
	}
	
	private ViewHolder mHolder = null;
	
	public StationMonitorRankAdapter(Context context, List<StationMonitorDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.station_monitor_rank_cell, null);
			mHolder = new ViewHolder();
			mHolder.tvNum = (TextView) convertView.findViewById(R.id.tvNum);
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.tvValue = (TextView) convertView.findViewById(R.id.tvValue);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		StationMonitorDto dto = mArrayList.get(position);
		mHolder.tvNum.setText(String.valueOf(position+1));
		mHolder.tvName.setText(dto.name+" - "+dto.stationId+" ("+dto.provinceName+")");
		mHolder.tvValue.setText(dto.value);
		
		if (position == 0 || position == 1 || position == 2) {
			mHolder.tvNum.setBackgroundResource(R.drawable.bg_rank_top3);
		}else {
			mHolder.tvNum.setBackgroundResource(R.drawable.bg_rank_bottom3);
		}
		
		return convertView;
	}

}
