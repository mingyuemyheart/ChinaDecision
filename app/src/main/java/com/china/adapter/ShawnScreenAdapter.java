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
import com.china.common.ColumnData;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 屏屏联动
 */
public class ShawnScreenAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<ColumnData> mArrayList;

	private final class ViewHolder{
		TextView tvName;
		ImageView icon;
	}

	public ShawnScreenAdapter(Context context, List<ColumnData> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_screen, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			mHolder.icon = convertView.findViewById(R.id.icon);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		ColumnData dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.name)) {
			mHolder.tvName.setText(dto.name);
		}
		
		if (!TextUtils.isEmpty(dto.icon)) {
			Picasso.get().load(dto.icon).error(R.drawable.shawn_icon_seat_bitmap).into(mHolder.icon);
		}else {
			mHolder.icon.setImageResource(R.drawable.shawn_icon_seat_bitmap);
		}

		return convertView;
	}

}
