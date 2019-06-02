package com.china.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.china.R;
import com.china.dto.StationMonitorDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 五天降水统计
 */
public class ShawnFiveRainAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<StationMonitorDto> mArrayList;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日HH时", Locale.CHINA);

    private final class ViewHolder {
        TextView tvTime;
    }

    public ShawnFiveRainAdapter(Context context, List<StationMonitorDto> mArrayList) {
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
            convertView = mInflater.inflate(R.layout.shawn_adapter_five_rain, null);
            mHolder = new ViewHolder();
            mHolder.tvTime = convertView.findViewById(R.id.tvTime);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        StationMonitorDto dto = mArrayList.get(position);
        if (!TextUtils.isEmpty(dto.startTime) && !TextUtils.isEmpty(dto.endTime)) {
            try {
                mHolder.tvTime.setText(sdf2.format(sdf1.parse(dto.startTime)) + " - " + sdf2.format(sdf1.parse(dto.endTime)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }

}
