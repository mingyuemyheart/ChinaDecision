package com.china.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.RadarDto;

import java.util.List;

/**
 * 天气雷达详情
 */
public class ShawnRadarDetailAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<RadarDto> mArrayList;

	private final class ViewHolder{
		TextView tvTime;
	}

	public ShawnRadarDetailAdapter(Context context, List<RadarDto> mArrayList) {
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
        ViewHolder mHolder;
	    if (convertView == null) {
			convertView = mInflater.inflate(R.layout.shawn_adapter_radar_detail, null);
			mHolder = new ViewHolder();
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		RadarDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.time)) {
            mHolder.tvTime.setText(dto.time);
        }

		if (dto.isSelected) {
			mHolder.tvTime.setTextColor(mContext.getResources().getColor(R.color.blue));
		}else {
			mHolder.tvTime.setTextColor(mContext.getResources().getColor(R.color.text_color2));
		}
		
		return convertView;
	}

}
