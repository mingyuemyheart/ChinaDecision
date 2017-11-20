package com.china.adapter;

/**
 * 屏屏联动
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
import com.china.common.ColumnData;

import net.tsz.afinal.FinalBitmap;

import java.util.ArrayList;
import java.util.List;

public class ScreenAdapter extends BaseAdapter{

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<ColumnData> mArrayList = new ArrayList<ColumnData>();

	private final class ViewHolder{
		TextView tvName;
		ImageView icon;
	}

	private ViewHolder mHolder = null;

	public ScreenAdapter(Context context, List<ColumnData> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_screen, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		ColumnData dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.name)) {
			mHolder.tvName.setText(dto.name);
		}
		
		if (!TextUtils.isEmpty(dto.icon)) {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.icon, dto.icon, null, 0);
		}else {
			mHolder.icon.setImageResource(R.drawable.iv_default_news);
		}

		return convertView;
	}

}
