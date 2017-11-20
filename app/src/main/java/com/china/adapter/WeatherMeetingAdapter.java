package com.china.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.R;
import com.china.dto.WeatherMeetingDto;

import java.util.ArrayList;
import java.util.List;

public class WeatherMeetingAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<WeatherMeetingDto> mArrayList = new ArrayList<WeatherMeetingDto>();
	
	private final class ViewHolder{
		TextView tvTitle;
		ImageView imageView;
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public WeatherMeetingAdapter(Context context, List<WeatherMeetingDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_weather_meeting, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			WeatherMeetingDto dto = mArrayList.get(position);
			mHolder.tvTitle.setText(dto.liveName);
			mHolder.tvTime.setText(dto.liveStart+" ~ "+dto.liveEnd);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return convertView;
	}

}
