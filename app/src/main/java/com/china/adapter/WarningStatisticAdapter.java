package com.china.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.WarningDto;

import java.util.List;

/**
 * 预警统计
 * @author shawn_sun
 *
 */

public class WarningStatisticAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<WarningDto> mArrayList = null;
	
	private final class ViewHolder {
		TextView tvWarning;
		TextView tvNation;
		TextView tvPro;
		TextView tvCity;
		TextView tvDis;
	}
	
	private ViewHolder mHolder = null;
	
	public WarningStatisticAdapter(Context context, List<WarningDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_warning_statistic, null);
			mHolder = new ViewHolder();
			mHolder.tvWarning = (TextView) convertView.findViewById(R.id.tvWarning);
			mHolder.tvNation = (TextView) convertView.findViewById(R.id.tvNation);
			mHolder.tvPro = (TextView) convertView.findViewById(R.id.tvPro);
			mHolder.tvCity = (TextView) convertView.findViewById(R.id.tvCity);
			mHolder.tvDis = (TextView) convertView.findViewById(R.id.tvDis);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);
		mHolder.tvWarning.setText(dto.colorName);
		mHolder.tvNation.setText(dto.nationCount);
		mHolder.tvPro.setText(dto.proCount);
		mHolder.tvCity.setText(dto.cityCount);
		mHolder.tvDis.setText(dto.disCount);
		
		if (dto.colorName.contains("红")) {
			mHolder.tvWarning.setTextColor(Color.WHITE);
			mHolder.tvWarning.setBackgroundColor(0xfffe2624);
			mHolder.tvNation.setBackgroundColor(0x10fe2624);
			mHolder.tvPro.setBackgroundColor(0x10fe2624);
			mHolder.tvCity.setBackgroundColor(0x10fe2624);
			mHolder.tvDis.setBackgroundColor(0x10fe2624);
		}else if (dto.colorName.contains("橙")) {
			mHolder.tvWarning.setTextColor(Color.WHITE);
			mHolder.tvWarning.setBackgroundColor(0xfffea228);
			mHolder.tvNation.setBackgroundColor(0x10fea228);
			mHolder.tvPro.setBackgroundColor(0x10fea228);
			mHolder.tvCity.setBackgroundColor(0x10fea228);
			mHolder.tvDis.setBackgroundColor(0x10fea228);
		}else if (dto.colorName.contains("黄")) {
			mHolder.tvWarning.setTextColor(Color.WHITE);
			mHolder.tvWarning.setBackgroundColor(0xffecdf04);
			mHolder.tvNation.setBackgroundColor(0x10ecdf04);
			mHolder.tvPro.setBackgroundColor(0x10ecdf04);
			mHolder.tvCity.setBackgroundColor(0x10ecdf04);
			mHolder.tvDis.setBackgroundColor(0x10ecdf04);
		}else if (dto.colorName.contains("蓝")) {
			mHolder.tvWarning.setTextColor(Color.WHITE);
			mHolder.tvWarning.setBackgroundColor(0xff2f82db);
			mHolder.tvNation.setBackgroundColor(0x102f82db);
			mHolder.tvPro.setBackgroundColor(0x102f82db);
			mHolder.tvCity.setBackgroundColor(0x102f82db);
			mHolder.tvDis.setBackgroundColor(0x102f82db);
		}else {
			mHolder.tvWarning.setTextColor(mContext.getResources().getColor(R.color.text_color4));
			mHolder.tvWarning.setBackgroundColor(0x80f0eff5);
			mHolder.tvNation.setBackgroundColor(0x80f0eff5);
			mHolder.tvPro.setBackgroundColor(0x80f0eff5);
			mHolder.tvCity.setBackgroundColor(0x80f0eff5);
			mHolder.tvDis.setBackgroundColor(0x80f0eff5);
		}
		
		return convertView;
	}

}
