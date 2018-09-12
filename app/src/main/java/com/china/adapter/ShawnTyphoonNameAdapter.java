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
import com.china.dto.TyphoonDto;

import java.util.List;

/**
 * 台风路径-列表
 */
public class ShawnTyphoonNameAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<TyphoonDto> mArrayList;

	private final class ViewHolder{
		ImageView ivStatus;
		TextView tvName;
	}
	
	public ShawnTyphoonNameAdapter(Context context, List<TyphoonDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_typhoon_name, null);
			mHolder = new ViewHolder();
			mHolder.ivStatus = convertView.findViewById(R.id.ivStatus);
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		TyphoonDto dto = mArrayList.get(position);

		if (TextUtils.isEmpty(dto.name) || TextUtils.equals(dto.name, "null")) {
			mHolder.tvName.setText(dto.code + " " + dto.enName);
		}else {
			mHolder.tvName.setText(dto.code + " " + dto.name + " " + dto.enName);
		}
		
		if (!dto.isSelected) {
			mHolder.ivStatus.setImageResource(R.drawable.shawn_bg_checkbox);
		}else {
			mHolder.ivStatus.setImageResource(R.drawable.shawn_bg_checkbox_selected);
		}

		return convertView;
	}

}
