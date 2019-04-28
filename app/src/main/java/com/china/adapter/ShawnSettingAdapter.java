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
import com.china.dto.ShawnSettingDto;

import java.util.List;

/**
 * 侧滑页面
 */
public class ShawnSettingAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private List<ShawnSettingDto> mArrayList;

	private final class ViewHolder{
		TextView tvName,tvValue;
		ImageView imageView;
	}

	public ShawnSettingAdapter(Context context, List<ShawnSettingDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_setting, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			mHolder.tvValue = convertView.findViewById(R.id.tvValue);
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		ShawnSettingDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.getName())) {
			mHolder.tvName.setText(dto.getName());
		}
		if (!TextUtils.isEmpty(dto.getValue())) {
			mHolder.tvValue.setText(dto.getValue());
			mHolder.tvValue.setVisibility(View.VISIBLE);
		}else {
			mHolder.tvValue.setVisibility(View.INVISIBLE);
		}
		mHolder.imageView.setImageResource(dto.getDrawable());

		return convertView;
	}

}
