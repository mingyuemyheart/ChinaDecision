package com.china.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.R;
import com.china.dto.WeatherDto;
import com.china.utils.CommonUtil;
import com.china.utils.WeatherUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class WeeklyForecastAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<WeatherDto> mArrayList = new ArrayList<WeatherDto>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");
	
	private final class ViewHolder{
		TextView tvWeek;
		TextView tvDate;
		TextView tvHighPhe;
		ImageView ivHighPhe;
		TextView tvHighTemp;
		ImageView ivHighWind;
		TextView tvHighWind;
		TextView tvLowPhe;
		ImageView ivLowPhe;
		TextView tvLowTemp;
		ImageView ivLowWind;
		TextView tvLowWind;
	}
	
	private ViewHolder mHolder = null;
	
	public WeeklyForecastAdapter(Context context, List<WeatherDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_weekly_forecast, null);
			mHolder = new ViewHolder();
			mHolder.tvWeek = (TextView) convertView.findViewById(R.id.tvWeek);
			mHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
			mHolder.tvHighPhe = (TextView) convertView.findViewById(R.id.tvHighPhe);
			mHolder.ivHighPhe = (ImageView) convertView.findViewById(R.id.ivHighPhe);
			mHolder.tvHighTemp = (TextView) convertView.findViewById(R.id.tvHighTemp);
			mHolder.ivHighWind = (ImageView) convertView.findViewById(R.id.ivHighWind);
			mHolder.tvHighWind = (TextView) convertView.findViewById(R.id.tvHighWind);
			mHolder.tvLowPhe = (TextView) convertView.findViewById(R.id.tvLowPhe);
			mHolder.ivLowPhe = (ImageView) convertView.findViewById(R.id.ivLowPhe);
			mHolder.tvLowTemp = (TextView) convertView.findViewById(R.id.tvLowTemp);
			mHolder.ivLowWind = (ImageView) convertView.findViewById(R.id.ivLowWind);
			mHolder.tvLowWind = (TextView) convertView.findViewById(R.id.tvLowWind);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WeatherDto dto = mArrayList.get(position);
		if (position == 0) {
			mHolder.tvWeek.setText(mContext.getString(R.string.today));
		}else {
			String week = dto.week;
			mHolder.tvWeek.setText(mContext.getString(R.string.week)+week.substring(week.length()-1, week.length()));
		}
		try {
			mHolder.tvDate.setText(sdf2.format(sdf1.parse(dto.date)));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (dto.highTemp >= 35) {
			mHolder.ivHighPhe.setBackgroundResource(R.drawable.bg_weather_temp);
			mHolder.ivHighPhe.setImageBitmap(WeatherUtil.getDayBitmapWhite(mContext, dto.highPheCode));
		}else {
			if (dto.highPhe.contains("雨") || dto.highPhe.contains("雪")) {
				mHolder.ivHighPhe.setBackgroundResource(R.drawable.bg_weather_wind);
				mHolder.ivHighPhe.setImageBitmap(WeatherUtil.getDayBitmapWhite(mContext, dto.highPheCode));
			}else if (dto.highPhe.contains("雾") || dto.highPhe.contains("霾")) {
				mHolder.ivHighPhe.setBackgroundResource(R.drawable.bg_weather_fog);
				mHolder.ivHighPhe.setImageBitmap(WeatherUtil.getDayBitmapWhite(mContext, dto.highPheCode));
			}else if (dto.highPhe.contains("沙") || dto.highPhe.contains("尘")) {
				mHolder.ivHighPhe.setBackgroundResource(R.drawable.bg_weather_storm);
				mHolder.ivHighPhe.setImageBitmap(WeatherUtil.getDayBitmapWhite(mContext, dto.highPheCode));
			}else {
				mHolder.ivHighPhe.setBackgroundResource(R.drawable.bg_weather_default);
				mHolder.ivHighPhe.setImageBitmap(WeatherUtil.getDayBitmap(mContext, dto.highPheCode));
			}
		}
		mHolder.tvHighPhe.setText(dto.highPhe);
		mHolder.tvHighTemp.setText(dto.highTemp+"℃");
		mHolder.tvHighWind.setText(mContext.getString(WeatherUtil.getWindDirection(dto.highWindDir))
		+WeatherUtil.getDayWindForce(dto.highWindForce));
		if (dto.highWindForce >= 4) {
			mHolder.ivHighWind.setBackgroundResource(R.drawable.bg_weather_wind);
			mHolder.ivHighWind.setImageBitmap(WeatherUtil.getWindBitmapWhite(mContext, dto.highWindForce));
		}else {
			mHolder.ivHighWind.setBackgroundResource(R.drawable.bg_weather_default);
			mHolder.ivHighWind.setImageBitmap(WeatherUtil.getWindBitmap(mContext, dto.highWindForce));
		}
		mHolder.ivHighWind.setRotation(WeatherUtil.getWindDegree(dto.highWindDir));


		if (dto.lowTemp >= 35) {
			mHolder.ivLowPhe.setBackgroundResource(R.drawable.bg_weather_temp);
			mHolder.ivLowPhe.setImageBitmap(WeatherUtil.getNightBitmapWhite(mContext, dto.lowPheCode));
		}else {
			if (dto.lowPhe.contains("雨") || dto.lowPhe.contains("雪")) {
				mHolder.ivLowPhe.setBackgroundResource(R.drawable.bg_weather_wind);
				mHolder.ivLowPhe.setImageBitmap(WeatherUtil.getNightBitmapWhite(mContext, dto.lowPheCode));
			}else if (dto.lowPhe.contains("雾") || dto.lowPhe.contains("霾")) {
				mHolder.ivLowPhe.setBackgroundResource(R.drawable.bg_weather_fog);
				mHolder.ivLowPhe.setImageBitmap(WeatherUtil.getNightBitmapWhite(mContext, dto.lowPheCode));
			}else if (dto.lowPhe.contains("沙") || dto.lowPhe.contains("尘")) {
				mHolder.ivLowPhe.setBackgroundResource(R.drawable.bg_weather_storm);
				mHolder.ivLowPhe.setImageBitmap(WeatherUtil.getNightBitmapWhite(mContext, dto.lowPheCode));
			}else {
				mHolder.ivLowPhe.setBackgroundResource(R.drawable.bg_weather_default);
				mHolder.ivLowPhe.setImageBitmap(WeatherUtil.getNightBitmap(mContext, dto.lowPheCode));
			}
		}
		mHolder.tvLowPhe.setText(dto.lowPhe);
		mHolder.tvLowTemp.setText(dto.lowTemp+"℃");
		mHolder.tvLowWind.setText(mContext.getString(WeatherUtil.getWindDirection(dto.lowWindDir))
		+WeatherUtil.getDayWindForce(dto.lowWindForce));
		if (dto.lowWindForce >= 4) {
			mHolder.ivLowWind.setBackgroundResource(R.drawable.bg_weather_wind);
			mHolder.ivLowWind.setImageBitmap(WeatherUtil.getWindBitmapWhite(mContext, dto.lowWindForce));
		}else {
			mHolder.ivLowWind.setBackgroundResource(R.drawable.bg_weather_default);
			mHolder.ivLowWind.setImageBitmap(WeatherUtil.getWindBitmap(mContext, dto.lowWindForce));
		}
		mHolder.ivLowWind.setRotation(WeatherUtil.getWindDegree(dto.lowWindDir));

		return convertView;
	}

}
