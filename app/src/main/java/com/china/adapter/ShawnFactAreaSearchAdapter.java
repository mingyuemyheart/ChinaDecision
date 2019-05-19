package com.china.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.StationMonitorDto;

import java.util.List;

/**
 * 实况-城市选择
 * @author shawn_sun
 */
public class ShawnFactAreaSearchAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private List<StationMonitorDto> mArrayList;

	private final class ViewHolder{
		TextView tvName;
	}

	public ShawnFactAreaSearchAdapter(Context context, List<StationMonitorDto> mArrayList) {
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.shawn_adapter_fact_area_search, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
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
