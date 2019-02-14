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
 * 城市查询-搜索列表
 */
public class ShawnCityAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<CityDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvName;
	}
	
	public ShawnCityAdapter(Context context, List<CityDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_city, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			CityDto dto = mArrayList.get(position);
			if (TextUtils.equals(dto.cityName, dto.areaName)) {
				mHolder.tvName.setText(dto.provinceName + " - " +dto.cityName);
			}else {
				mHolder.tvName.setText(dto.provinceName +" - "+dto.cityName + " - " + dto.areaName);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
