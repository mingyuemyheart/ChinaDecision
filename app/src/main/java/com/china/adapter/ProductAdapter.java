package com.china.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.common.ColumnData;
import com.china.utils.CommonUtil;
import com.china.view.RoundTransform;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 实况监测等模块
 */
public class ProductAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<ColumnData> mArrayList;

	private final class ViewHolder{
		TextView tvName;
		ImageView icon;
	}
	
	public ProductAdapter(Context context, List<ColumnData> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_product, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			mHolder.icon = convertView.findViewById(R.id.icon);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		ColumnData dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.name)) {
			mHolder.tvName.setText(dto.name);
		}

		if (!TextUtils.isEmpty(dto.icon)) {
			Picasso.get().load(dto.icon).transform(new RoundTransform((int)(CommonUtil.dip2px(mContext, 5)))).into(mHolder.icon);
		}else {
			mHolder.icon.setImageResource(R.drawable.icon_seat_bitmap);
		}
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mHolder.icon.getLayoutParams();
		params.width = (CommonUtil.widthPixels(mContext)-(int)(CommonUtil.dip2px(mContext, 30)))/2;
		params.height = (CommonUtil.widthPixels(mContext)-(int)(CommonUtil.dip2px(mContext, 30)))/2*3/4;
		mHolder.icon.setLayoutParams(params);

		mHolder.icon.setBackgroundResource(R.drawable.shawn_bg_corner_border);
		mHolder.icon.setPadding(3,3,3,3);

		return convertView;
	}

}
