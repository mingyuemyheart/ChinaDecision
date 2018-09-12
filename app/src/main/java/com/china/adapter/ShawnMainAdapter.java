package com.china.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.R;
import com.china.common.ColumnData;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 主界面
 */
public class ShawnMainAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<ColumnData> mArrayList;
	public int height = 0;
	
	private final class ViewHolder{
		TextView tvName;
		ImageView icon;
	}
	
	public ShawnMainAdapter(Context context, List<ColumnData> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_main, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			mHolder.icon = convertView.findViewById(R.id.icon);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		try {
			ColumnData dto = mArrayList.get(position);

			if (!TextUtils.isEmpty(dto.name)) {
				mHolder.tvName.setText(dto.name);
			}

			if (!TextUtils.isEmpty(dto.icon)) {
				Picasso.get().load(dto.icon).error(R.drawable.shawn_icon_seat_bitmap).into(mHolder.icon);
			}else {
				mHolder.icon.setImageResource(R.drawable.shawn_icon_seat_bitmap);
			}

			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			params.height = height/3;
			convertView.setLayoutParams(params);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return convertView;
	}

}
