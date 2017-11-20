package com.china.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.DisasterDto;

import java.util.ArrayList;
import java.util.List;

public class DisasterAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<DisasterDto> mArrayList = new ArrayList<DisasterDto>();
	
	private final class ViewHolder{
		TextView tvTitle;
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public DisasterAdapter(Context context, List<DisasterDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.disaster_cell, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		DisasterDto dto = mArrayList.get(position);
		mHolder.tvTitle.setText(dto.title);
		mHolder.tvTime.setText(dto.time);
		
		return convertView;
	}

}
