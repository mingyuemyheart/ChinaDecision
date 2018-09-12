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
import com.china.dto.NewsDto;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 新闻、图文类
 */
public class ShawnNewsAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<NewsDto> mArrayList;
	
	private final class ViewHolder{
		ImageView imageView;
		TextView tvTitle,tvTime;
	}
	
	public ShawnNewsAdapter(Context context, List<NewsDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_news, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.tvTitle = convertView.findViewById(R.id.tvTitle);
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		NewsDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.title)) {
			mHolder.tvTitle.setText(dto.title);
		}

		if (!TextUtils.isEmpty(dto.time)) {
			mHolder.tvTime.setText(dto.time);
			mHolder.tvTime.setVisibility(View.VISIBLE);
		}else {
			mHolder.tvTime.setVisibility(View.INVISIBLE);
		}
		
		if (!TextUtils.isEmpty(dto.imgUrl)) {
			Picasso.get().load(dto.imgUrl).error(R.drawable.shawn_icon_seat_bitmap).into(mHolder.imageView);
		}else {
			mHolder.imageView.setImageResource(R.drawable.shawn_icon_seat_bitmap);
		}
		
		return convertView;
	}

}
