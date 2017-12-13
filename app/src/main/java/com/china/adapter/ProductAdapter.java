package com.china.adapter;

/**
 * 实况监测等模块
 */

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.common.ColumnData;

import net.tsz.afinal.FinalBitmap;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<ColumnData> mArrayList = new ArrayList<>();
	private int width = 0;
	private float density = 0;
	
	private final class ViewHolder{
		TextView tvName;
		ImageView icon;
	}
	
	private ViewHolder mHolder = null;
	
	public ProductAdapter(Context context, List<ColumnData> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		density = dm.density;
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
			convertView = mInflater.inflate(R.layout.adapter_product, null);
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
			finalBitmap.display(mHolder.icon, dto.icon, null, (int)(5*density));
		}else {
			mHolder.icon.setImageResource(R.drawable.iv_default_news);
		}
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mHolder.icon.getLayoutParams();
		params.width = (width-(int)(30*density))/2;
		params.height = (width-(int)(30*density))/2*3/4;
		mHolder.icon.setLayoutParams(params);

		mHolder.icon.setBackgroundResource(R.drawable.corner_border);
		mHolder.icon.setPadding(3,3,3,3);

		return convertView;
	}

}
