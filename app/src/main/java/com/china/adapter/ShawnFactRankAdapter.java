package com.china.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.StationMonitorDto;

import java.util.List;

/**
 * 实况排行
 */
public class ShawnFactRankAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<StationMonitorDto> mArrayList;

    private final class ViewHolder {
        TextView tvNum, tvName, tvValue;
    }

    public ShawnFactRankAdapter(Context context, List<StationMonitorDto> mArrayList) {
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
            convertView = mInflater.inflate(R.layout.shawn_adapter_fact_rank, null);
            mHolder = new ViewHolder();
            mHolder.tvNum = convertView.findViewById(R.id.tvNum);
            mHolder.tvName = convertView.findViewById(R.id.tvName);
            mHolder.tvValue = convertView.findViewById(R.id.tvValue);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        StationMonitorDto dto = mArrayList.get(position);
        mHolder.tvNum.setText(String.valueOf(position + 1));
        mHolder.tvName.setText(dto.name + " - " + dto.stationId + " (" + dto.provinceName + ")");
        mHolder.tvValue.setText(dto.value);

        if (position == 0 || position == 1 || position == 2) {
            mHolder.tvNum.setBackgroundResource(R.drawable.shawn_fact_bg_rank_top3);
        } else {
            mHolder.tvNum.setBackgroundResource(R.drawable.shawn_fact_bg_rank_bottom3);
        }

        return convertView;
    }

}
