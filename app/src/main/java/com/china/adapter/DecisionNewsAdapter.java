package com.china.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.R;
import com.china.dto.DisasterDto;

import java.util.ArrayList;
import java.util.List;

public class DecisionNewsAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<DisasterDto> mArrayList = new ArrayList<DisasterDto>();
	
	private final class ViewHolder{
		TextView tvTitle;
		ImageView imageView;
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public DecisionNewsAdapter(Context context, List<DisasterDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.decision_news_cell, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			DisasterDto dto = mArrayList.get(position);
			if (dto.title != null) {
				mHolder.tvTitle.setText(dto.title);
				mHolder.tvTime.setText(dto.time);
				if (dto.title.contains(mContext.getString(R.string.decision_news_1))) {
					mHolder.imageView.setImageResource(R.drawable.iv_decision1);
				}else if (dto.title.contains(mContext.getString(R.string.decision_news_2))) {
					mHolder.imageView.setImageResource(R.drawable.iv_decision2);
				}else if (dto.title.contains(mContext.getString(R.string.decision_news_3))) {
					mHolder.imageView.setImageResource(R.drawable.iv_decision3);
				}else {
					mHolder.imageView.setImageResource(R.drawable.iv_pdf);
				}
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return convertView;
	}

}
