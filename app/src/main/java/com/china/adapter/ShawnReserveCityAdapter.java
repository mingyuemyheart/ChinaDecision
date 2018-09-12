package com.china.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.R;
import com.china.activity.ShawnHeadWarningActivity;
import com.china.activity.ShawnWarningDetailActivity;
import com.china.common.CONST;
import com.china.dto.CityDto;
import com.china.dto.WarningDto;
import com.china.utils.CommonUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 城市预定
 */
public class ShawnReserveCityAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<CityDto> mArrayList;
	private int hour;
	public static final int notShowRemove = 0,showRemove = 1;

	public ShawnReserveCityAdapter(Context context, List<CityDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		SimpleDateFormat sdf1 = new SimpleDateFormat("HH", Locale.CHINA);
		hour = Integer.parseInt(sdf1.format(new Date()));
	}

	private final class ViewHolder{
		ImageView ivWarning, ivPhe, ivLocation;
		TextView tvCity, tvCount, tvTemp;
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
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return notShowRemove;
		}else {
			return showRemove;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.shawn_adapter_reserve_city, null);
			mHolder = new ViewHolder();
			mHolder.ivWarning = convertView.findViewById(R.id.ivWarning);
			mHolder.ivPhe = convertView.findViewById(R.id.ivPhe);
			mHolder.ivLocation = convertView.findViewById(R.id.ivLocation);
			mHolder.tvCity = convertView.findViewById(R.id.tvCity);
			mHolder.tvCount = convertView.findViewById(R.id.tvCount);
			mHolder.tvTemp = convertView.findViewById(R.id.tvTemp);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		final CityDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.cityName)) {
			mHolder.tvCity.setText(dto.cityName);
		}

		Drawable drawable;
		if (hour >= 5 && hour < 18) {
			drawable = mContext.getResources().getDrawable(R.drawable.phenomenon_drawable);
		}else {
			drawable = mContext.getResources().getDrawable(R.drawable.phenomenon_drawable_night);
		}
		drawable.setLevel(dto.highPheCode);
		mHolder.ivPhe.setBackground(drawable);

		if (!TextUtils.isEmpty(dto.highTemp)) {
			mHolder.tvTemp.setText(dto.highTemp+"℃");
		}

		if (position == 0) {
			mHolder.ivLocation.setVisibility(View.VISIBLE);
		}else {
			mHolder.ivLocation.setVisibility(View.INVISIBLE);
		}

		int size = dto.warningList.size();
		if (size == 1) {
			final WarningDto data = dto.warningList.get(0);
			Bitmap bitmap = null;
			if (data.color.equals(CONST.blue[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+data.type+CONST.blue[1]+CONST.imageSuffix);
			}else if (data.color.equals(CONST.yellow[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+data.type+CONST.yellow[1]+CONST.imageSuffix);
			}else if (data.color.equals(CONST.orange[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+data.type+CONST.orange[1]+CONST.imageSuffix);
			}else if (data.color.equals(CONST.red[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+data.type+CONST.red[1]+CONST.imageSuffix);
			}
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+CONST.imageSuffix);
			}
			mHolder.ivWarning.setVisibility(View.VISIBLE);
			mHolder.ivWarning.setImageBitmap(bitmap);
			mHolder.tvCount.setVisibility(View.GONE);
			mHolder.tvCount.setText("");
			mHolder.ivWarning.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intentDetail = new Intent(mContext, ShawnWarningDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", data);
					intentDetail.putExtras(bundle);
					mContext.startActivity(intentDetail);
				}
			});
		}else if (size > 1) {
			mHolder.ivWarning.setVisibility(View.GONE);
			mHolder.tvCount.setVisibility(View.VISIBLE);
			mHolder.tvCount.setText(size+"条预警");
			mHolder.tvCount.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, ShawnHeadWarningActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) dto.warningList);
					intent.putExtras(bundle);
					mContext.startActivity(intent);
				}
			});
		}else {//没有预警
			mHolder.ivWarning.setVisibility(View.GONE);
			mHolder.tvCount.setVisibility(View.GONE);
		}

		return convertView;
	}

}
