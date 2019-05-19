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

import java.util.List;

/**
 * 产品定制-需求单，提供材料
 * @author shawn_sun
 */
public class ShawnProductOrderAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private List<DisasterDto> mArrayList;

	private final class ViewHolder{
		TextView tvTitle,tvContent,tvTime;
		ImageView imageView;
	}

	public ShawnProductOrderAdapter(Context context, List<DisasterDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_product_order, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = convertView.findViewById(R.id.tvTitle);
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			mHolder.tvContent = convertView.findViewById(R.id.tvContent);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		try {
			DisasterDto dto = mArrayList.get(position);
			if (!TextUtils.isEmpty(dto.title)) {
				mHolder.tvTitle.setText(dto.title);
			}
			if (!TextUtils.isEmpty(dto.content)) {
				mHolder.tvContent.setText(dto.content);
			}
			if (!TextUtils.isEmpty(dto.time)) {
				mHolder.tvTime.setText("提交时间："+dto.time);
			}
			if (TextUtils.equals(dto.status, "0")) {
				mHolder.imageView.setImageResource(R.drawable.shawn_icon_dsp);
			}else if (TextUtils.equals(dto.status, "2")) {
				mHolder.imageView.setImageResource(R.drawable.shawn_icon_ysp);
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return convertView;
	}

}
