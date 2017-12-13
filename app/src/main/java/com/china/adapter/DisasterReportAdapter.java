package com.china.adapter;

/**
 * 灾情直报
 */

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
import java.util.ArrayList;
import java.util.List;

public class DisasterReportAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<DisasterReportDto> mArrayList = new ArrayList<>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
	
	private final class ViewHolder{
		TextView vSendername;
		TextView vCategory;
		TextView vEdittime;
		TextView vGeneralloss;
		TextView vRzdpop;
	}
	
	private ViewHolder mHolder = null;
	
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
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_disaster_report, null);
			mHolder = new ViewHolder();
			mHolder.vSendername = (TextView) convertView.findViewById(R.id.vSendername);
			mHolder.vCategory = (TextView) convertView.findViewById(R.id.vCategory);
			mHolder.vEdittime = (TextView) convertView.findViewById(R.id.vEdittime);
			mHolder.vGeneralloss = (TextView) convertView.findViewById(R.id.vGeneralloss);
			mHolder.vRzdpop = (TextView) convertView.findViewById(R.id.vRzdpop);
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
