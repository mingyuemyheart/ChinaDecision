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

import java.util.List;

/**
 * 城市查询-热门城市
 */
public class ShawnCityHotAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<CityDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvName;
	}
	
	public ShawnCityHotAdapter(Context context, List<CityDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_city_hot, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		CityDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.areaName)) {
			mHolder.tvName.setText(dto.areaName);
		}

		return convertView;
	}

}
