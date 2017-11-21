package com.china.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.china.R;
import com.china.activity.WarningStatisticActivity;
import com.china.dto.WarningDto;

import java.util.List;

/**
 * 预警统计
 */

public class WarningStatisticGroupAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<WarningDto> groupList;
    private List<List<WarningDto>> childList;
    private LayoutInflater mInflater = null;
    private ExpandableListView listView = null;

    public WarningStatisticGroupAdapter(Context context, List<WarningDto> groupList, List<List<WarningDto>> childList, ExpandableListView listView){
        mContext = context;
        this.groupList = groupList;
        this.childList = childList;
        this.listView = listView;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private GroupHolder groupHolder = null;
    class GroupHolder{
        TextView tvAreaName;
        LinearLayout llAll;
        TextView tvShortName;
        ImageView ivArrow;
        TextView tvCount;
        TextView tvRed;
        TextView tvOrange;
        TextView tvYellow;
        TextView tvBlue;
    }

    private ChildHolder childHolder = null;
    class ChildHolder{
        TextView tvAreaName;
        TextView tvShortName;
        TextView tvCount;
        TextView tvRed;
        TextView tvOrange;
        TextView tvYellow;
        TextView tvBlue;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.adapter_warning_statistic_group, null);
            groupHolder = new GroupHolder();
            groupHolder.tvAreaName = (TextView) convertView.findViewById(R.id.tvAreaName);
            groupHolder.llAll = (LinearLayout) convertView.findViewById(R.id.llAll);
            groupHolder.tvShortName = (TextView) convertView.findViewById(R.id.tvShortName);
            groupHolder.ivArrow = (ImageView) convertView.findViewById(R.id.ivArrow);
            groupHolder.tvCount = (TextView) convertView.findViewById(R.id.tvCount);
            groupHolder.tvRed = (TextView) convertView.findViewById(R.id.tvRed);
            groupHolder.tvOrange = (TextView) convertView.findViewById(R.id.tvOrange);
            groupHolder.tvYellow = (TextView) convertView.findViewById(R.id.tvYellow);
            groupHolder.tvBlue = (TextView) convertView.findViewById(R.id.tvBlue);
            convertView.setTag(groupHolder);
        }else{
            groupHolder = (GroupHolder) convertView.getTag();
        }

        //判断是否已经打开列表
        if(isExpanded){
            groupHolder.ivArrow.setImageResource(R.drawable.statistic_arrow_top);
        }else{
            groupHolder.ivArrow.setImageResource(R.drawable.statistic_arrow_bottom);
        }

        final WarningDto dto = groupList.get(groupPosition);
        if (!TextUtils.isEmpty(dto.areaName)) {
//            String areaName = dto.areaName;
//            if (areaName.contains("省")) {
//                areaName = areaName.replace("省", "");
//            }
//            if (areaName.startsWith("内蒙古")) {
//                areaName = "内蒙古";
//            }else if (areaName.startsWith("广西")) {
//                areaName = "广西";
//            }else if (areaName.startsWith("西藏")) {
//                areaName = "西藏";
//            }else if (areaName.startsWith("宁夏")) {
//                areaName = "宁夏";
//            }else if (areaName.startsWith("新疆")) {
//                areaName = "新疆";
//            }
//            groupHolder.tvAreaName.setText(areaName);
            groupHolder.tvAreaName.setText(dto.areaName);
        }else {
            groupHolder.tvAreaName.setText("总计");
        }
        if (!TextUtils.equals(dto.areaKey, "all") && !dto.areaKey.contains("00") && dto.areaKey.length() != 6) {
            if (!TextUtils.isEmpty(dto.areaName)) {
                SpannableString ss = new SpannableString(dto.areaName);
                ss.setSpan(new UnderlineSpan(), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                groupHolder.tvAreaName.setText(ss);
            }
        }
        if (!TextUtils.isEmpty(dto.shortName)) {
            groupHolder.tvShortName.setText(dto.shortName);
        }else {
            groupHolder.tvShortName.setText("全部");
        }
        groupHolder.tvCount.setText(dto.warningCount);
        groupHolder.tvRed.setText(dto.redCount);
        groupHolder.tvOrange.setText(dto.orangeCount);
        groupHolder.tvYellow.setText(dto.yellowCount);
        groupHolder.tvBlue.setText(dto.blueCount);

        groupHolder.tvAreaName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.equals(dto.areaKey, "all") && !dto.areaKey.contains("00") && dto.areaKey.length() != 6) {
                    Intent intent = new Intent(mContext, WarningStatisticActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("data", dto);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            }
        });

        groupHolder.llAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroup(groupPosition);
                }else {
                    listView.expandGroup(groupPosition, true);
                }
            }
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.adapter_warning_statistic_child, null);
            childHolder = new ChildHolder();
            childHolder.tvAreaName = (TextView) convertView.findViewById(R.id.tvAreaName);
            childHolder.tvShortName = (TextView) convertView.findViewById(R.id.tvShortName);
            childHolder.tvCount = (TextView) convertView.findViewById(R.id.tvCount);
            childHolder.tvRed = (TextView) convertView.findViewById(R.id.tvRed);
            childHolder.tvOrange = (TextView) convertView.findViewById(R.id.tvOrange);
            childHolder.tvYellow = (TextView) convertView.findViewById(R.id.tvYellow);
            childHolder.tvBlue = (TextView) convertView.findViewById(R.id.tvBlue);
            convertView.setTag(childHolder);
        }else{
            childHolder = (ChildHolder) convertView.getTag();
        }

        WarningDto dto = childList.get(groupPosition).get(childPosition);
        if (!TextUtils.isEmpty(dto.shortName)) {
//            String shortName = dto.shortName;
//            if (shortName.contains("事件")) {
//                shortName = shortName.replaceAll("事件", "");
//            }
            childHolder.tvShortName.setText(dto.shortName);
        }
        childHolder.tvCount.setText(dto.warningCount);
        childHolder.tvRed.setText(dto.redCount);
        childHolder.tvOrange.setText(dto.orangeCount);
        childHolder.tvYellow.setText(dto.yellowCount);
        childHolder.tvBlue.setText(dto.blueCount);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
