package com.china.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.TyphoonDto;

import java.util.List;

/**
 * 台风列表，按照不同发布单位区分
 */
public class ShawnTyphoonPublishAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<TyphoonDto> mArrayList;

	private final class ViewHolder{
		TextView tvName;
	}
	
	public ShawnTyphoonPublishAdapter(Context context, List<TyphoonDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_typhoon_publish, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		TyphoonDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.publishName)) {
			mHolder.tvName.setText(dto.publishName);
		}

		if (dto.isSelected) {
			convertView.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
		}else {
			convertView.setBackgroundColor(Color.TRANSPARENT);
		}

		return convertView;
	}

}
