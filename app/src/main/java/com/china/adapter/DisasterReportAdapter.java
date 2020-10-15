package com.china.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.DisasterReportDto;
import com.china.utils.WeatherUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 灾情直报
 */
public class DisasterReportAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<DisasterReportDto> mArrayList;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
	
	private final class ViewHolder{
		TextView vSendername;
		TextView vCategory;
		TextView vEdittime;
		TextView vGeneralloss;
		TextView vRzdpop;
	}
	
	public DisasterReportAdapter(Context context, List<DisasterReportDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_disaster_report, null);
			mHolder = new ViewHolder();
			mHolder.vSendername = convertView.findViewById(R.id.vSendername);
			mHolder.vCategory = convertView.findViewById(R.id.vCategory);
			mHolder.vEdittime = convertView.findViewById(R.id.vEdittime);
			mHolder.vGeneralloss = convertView.findViewById(R.id.vGeneralloss);
			mHolder.vRzdpop = convertView.findViewById(R.id.vRzdpop);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		DisasterReportDto dto = mArrayList.get(position);

		mHolder.vSendername.setText(dto.vSendername);
		if (!TextUtils.isEmpty(dto.vCategory)) {
			mHolder.vCategory.setText("灾情类别："+mContext.getString(WeatherUtil.getDisasterClass(Integer.valueOf(dto.vCategory))));
		}
		if (!TextUtils.isEmpty(dto.vEdittime)) {
			try {
				mHolder.vEdittime.setText("上报时间："+sdf2.format(sdf1.parse(dto.vEdittime)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (!TextUtils.isEmpty(dto.vGeneralLoss)) {
			mHolder.vGeneralloss.setText("经济损失："+dto.vGeneralLoss+"万元");
		}else {
			mHolder.vGeneralloss.setText("经济损失："+"--");
		}
		if (!TextUtils.isEmpty(dto.vRzDpop)) {
			mHolder.vRzdpop.setText("死亡人数："+dto.vRzDpop+"人");
		}else {
			mHolder.vRzdpop.setText("死亡人数："+"--");
		}
		
		return convertView;
	}

}
