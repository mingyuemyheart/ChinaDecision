package com.china.adapter;

/**
 * 灾情专报
 */

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

import net.tsz.afinal.FinalBitmap;

import java.util.ArrayList;
import java.util.List;

public class DisasterSpecialAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<DisasterDto> mArrayList = new ArrayList<>();
	
	private final class ViewHolder{
		TextView tvTitle;
		TextView tvTime;
		ImageView imageView;
	}
	
	private ViewHolder mHolder = null;
	
	public DisasterSpecialAdapter(Context context, List<DisasterDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_disaster_special, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
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
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.imgUrl, null, 0);
		}else {
			mHolder.imageView.setImageResource(R.drawable.iv_pdf);
		}

		return convertView;
	}

}
