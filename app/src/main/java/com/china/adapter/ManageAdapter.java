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
import com.china.common.ColumnData;
import com.china.stickygridheaders.StickyGridHeadersSimpleAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 模块管理
 */
public class ManageAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<ColumnData> mArrayList;

	public ManageAdapter(Context context, List<ColumnData> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	private class HeaderViewHolder {
		ImageView imageView,ivSelected;
		TextView tvHeader;
	}

	@Override
	public long getHeaderId(int position) {
		return mArrayList.get(position).section;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		final HeaderViewHolder mHolder;
		if (convertView == null) {
			mHolder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.adapter_manage_header, null);
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.ivSelected = convertView.findViewById(R.id.ivSelected);
			mHolder.tvHeader = convertView.findViewById(R.id.tvHeader);
			convertView.setTag(mHolder);
		} else {
			mHolder = (HeaderViewHolder) convertView.getTag();
		}

		final ColumnData dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.icon)) {
			Picasso.get().load(dto.icon).error(R.drawable.icon_seat_bitmap).into(mHolder.imageView);
		}else {
			mHolder.imageView.setImageResource(R.drawable.icon_seat_bitmap);
		}

		if (dto.isSelected) {
			mHolder.ivSelected.setVisibility(View.VISIBLE);
		}else {
			mHolder.ivSelected.setVisibility(View.INVISIBLE);
		}

		mHolder.tvHeader.setText(dto.headerName);
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dto.isSelected = !dto.isSelected;
				if (dto.isSelected) {
					mHolder.ivSelected.setVisibility(View.VISIBLE);
				}else {
					mHolder.ivSelected.setVisibility(View.INVISIBLE);
				}
				for (ColumnData data : mArrayList) {
					if (TextUtils.equals(data.headerName, dto.headerName)) {
						if (dto.isSelected) {
							data.isSelected = true;
						}else {
							data.isSelected = false;
						}
					}
				}
				notifyDataSetChanged();
			}
		});

		return convertView;
	}


	private class ChildViewHolder {
		TextView tvItemName;
		ImageView ivSelected;
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return mArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ChildViewHolder mHolder;
		if (convertView == null) {
			mHolder = new ChildViewHolder();
			convertView = mInflater.inflate(R.layout.adapter_manage_content, null);
			mHolder.ivSelected = convertView.findViewById(R.id.ivSelected);
			mHolder.tvItemName = convertView.findViewById(R.id.tvItemName);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ChildViewHolder) convertView.getTag();
		}

		final ColumnData dto = mArrayList.get(position);

		mHolder.tvItemName.setText(dto.name);

		if (dto.isSelected) {
			mHolder.ivSelected.setVisibility(View.VISIBLE);
		}else {
			mHolder.ivSelected.setVisibility(View.INVISIBLE);
		}

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dto.isSelected = !dto.isSelected;
				if (dto.isSelected) {
					mHolder.ivSelected.setVisibility(View.VISIBLE);
				}else {
					mHolder.ivSelected.setVisibility(View.INVISIBLE);
				}
				notifyDataSetChanged();
			}
		});

		return convertView;
	}

}
