package com.china.adapter;

/**
 * 城市选择
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.CityDto;

import java.util.ArrayList;
import java.util.List;

public class CityAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<CityDto> mArrayList = new ArrayList<>();
	
	private final class ViewHolder{
		TextView tvName;
	}
	
	private ViewHolder mHolder = null;
	
	public CityAdapter(Context context, List<CityDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_city, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
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
