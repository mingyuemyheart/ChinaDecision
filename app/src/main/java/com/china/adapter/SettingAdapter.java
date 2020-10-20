package com.china.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.china.R;
import com.china.common.CONST;
import com.china.common.MyApplication;
import com.china.dto.ShawnSettingDto;

import java.util.List;

/**
 * 侧滑页面
 */
public class SettingAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<ShawnSettingDto> mArrayList;

	private final class ViewHolder{
		TextView tvName,tvValue;
		ImageView imageView,ivArrow;
		Switch swFact;
	}

	public SettingAdapter(Context context, List<ShawnSettingDto> mArrayList) {
		mContext = context;
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
			convertView = mInflater.inflate(R.layout.adapter_setting, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			mHolder.tvValue = convertView.findViewById(R.id.tvValue);
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.ivArrow = convertView.findViewById(R.id.ivArrow);
			mHolder.swFact = convertView.findViewById(R.id.swFact);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		ShawnSettingDto dto = mArrayList.get(position);

		if (dto.getType() == 12) {//格点实况
			mHolder.swFact.setChecked(MyApplication.FACTENABLE);
			mHolder.swFact.setVisibility(View.VISIBLE);
			mHolder.ivArrow.setVisibility(View.INVISIBLE);
			mHolder.swFact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
					MyApplication.FACTENABLE = b;
					MyApplication.saveUserInfo(mContext);
					sendBroadCast();
				}
			});
		}else {
			mHolder.swFact.setVisibility(View.INVISIBLE);
			mHolder.ivArrow.setVisibility(View.VISIBLE);
		}

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

	private void sendBroadCast() {
		Intent intent = new Intent();
		intent.setAction(CONST.BROADCAST_FACT);
		mContext.sendBroadcast(intent);
	}

}
