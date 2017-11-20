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

public class StationMonitorSearchAdapter extends BaseAdapter{

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<StationMonitorDto> mArrayList = new ArrayList<>();

	private final class ViewHolder{
		TextView tvName;
	}

	private ViewHolder mHolder = null;

	public StationMonitorSearchAdapter(Context context, List<StationMonitorDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_station_monitor_search, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			StationMonitorDto dto = mArrayList.get(position);
			mHolder.tvName.setText(dto.provinceName + "-" +dto.cityName+"-"+dto.districtName+"-"+dto.addr+"-"+dto.stationId);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
