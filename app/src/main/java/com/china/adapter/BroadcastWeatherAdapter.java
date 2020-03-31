package com.china.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.ShawnSettingDto;

import java.util.List;

/**
 * 实况监测等模块
 */
public class BroadcastWeatherAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<ShawnSettingDto> mArrayList;

	private final class ViewHolder{
		TextView tvName;
	}

	public BroadcastWeatherAdapter(Context context, List<ShawnSettingDto> mArrayList) {
		mContext = context;
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
			convertView = mInflater.inflate(R.layout.adapter_broadcast_weather, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		ShawnSettingDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.getName())) {
			mHolder.tvName.setText(dto.getName());
		}

		if (dto.isSelected()) {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.blue));
		} else {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.text_color3));
		}

		return convertView;
	}

}
