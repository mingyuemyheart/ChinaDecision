package com.china.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.R;
import com.china.dto.DisasterDto;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 灾情专报
 */
public class ShawnDisasterSpecialAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<DisasterDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvTitle,tvTime;
		ImageView imageView;
	}
	
	public ShawnDisasterSpecialAdapter(Context context, List<DisasterDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_disaster_special, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = convertView.findViewById(R.id.tvTitle);
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		DisasterDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.title)) {
			mHolder.tvTitle.setText(dto.title);
		}
		if (!TextUtils.isEmpty(dto.time)) {
			mHolder.tvTime.setText(dto.time);
		}
		if (!TextUtils.isEmpty(dto.imgUrl)) {
			Picasso.get().load(dto.imgUrl).error(R.drawable.iv_pdf).into(mHolder.imageView);
		}else {
			mHolder.imageView.setImageResource(R.drawable.iv_pdf);
		}

		return convertView;
	}

}
