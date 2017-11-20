package com.china.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.R;
import com.china.common.CONST;
import com.china.dto.WarningDto;
import com.china.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 预警统计列表
 */

public class WarningStatisticListGroupAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<WarningDto> groupList;
    private List<List<WarningDto>> childList;
    private LayoutInflater mInflater = null;
    private List<WarningDto> typeList = new ArrayList<>();

    public WarningStatisticListGroupAdapter(Context context, List<WarningDto> groupList, List<List<WarningDto>> childList){
        mContext = context;
        this.groupList = groupList;
        this.childList = childList;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        typeList.clear();
        String[] array1 = mContext.getResources().getStringArray(R.array.warningType);
        for (int i = 0; i < array1.length; i++) {
            String[] value = array1[i].split(",");
            WarningDto dto = new WarningDto();
            dto.name = value[1];
            dto.type = value[0];
            typeList.add(dto);
        }
    }

    private GroupHolder groupHolder = null;
    class GroupHolder{
        ImageView imageView;
        TextView tvName;
        TextView tvTime1;
        TextView tvTime2;
    }

    private ChildHolder childHolder = null;
    class ChildHolder{
        TextView tvTime1;
        TextView tvTime2;
        TextView tvColor;
        TextView tvType;
        TextView tvContent;
        TextView tvUnit;
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
            convertView = mInflater.inflate(R.layout.adapter_warning_statistic_list_group, null);
            groupHolder = new GroupHolder();
            groupHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            groupHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            groupHolder.tvTime1 = (TextView) convertView.findViewById(R.id.tvTime1);
            groupHolder.tvTime2 = (TextView) convertView.findViewById(R.id.tvTime2);
            convertView.setTag(groupHolder);
        }else{
            groupHolder = (GroupHolder) convertView.getTag();
        }

        WarningDto dto = groupList.get(groupPosition);

        Bitmap bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+"_"+dto.color.toLowerCase()+CONST.imageSuffix);
        if (bitmap == null) {
            bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+"_"+dto.color.toLowerCase()+CONST.imageSuffix);
        }
        groupHolder.imageView.setImageBitmap(bitmap);

        if (!TextUtils.isEmpty(dto.name)) {
            groupHolder.tvName.setText(dto.name);
        }
        if (!TextUtils.isEmpty(dto.time)) {
            groupHolder.tvTime1.setText("发布时间："+dto.time);
        }
        if (!TextUtils.isEmpty(dto.time2)) {
            groupHolder.tvTime2.setText("解除时间："+dto.time2);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.adapter_warning_statistic_list_child, null);
            childHolder = new ChildHolder();
            childHolder.tvTime1 = (TextView) convertView.findViewById(R.id.tvTime1);
            childHolder.tvTime2 = (TextView) convertView.findViewById(R.id.tvTime2);
            childHolder.tvColor = (TextView) convertView.findViewById(R.id.tvColor);
            childHolder.tvType = (TextView) convertView.findViewById(R.id.tvType);
            childHolder.tvContent = (TextView) convertView.findViewById(R.id.tvContent);
            childHolder.tvUnit = (TextView) convertView.findViewById(R.id.tvUnit);
            convertView.setTag(childHolder);
        }else{
            childHolder = (ChildHolder) convertView.getTag();
        }

        WarningDto dto = childList.get(groupPosition).get(childPosition);
        if (!TextUtils.isEmpty(dto.time)) {
            childHolder.tvTime1.setText(dto.time);
        }
        if (!TextUtils.isEmpty(dto.time2)) {
            childHolder.tvTime2.setText(dto.time2);
        }
        if (!TextUtils.isEmpty(dto.color)) {
            String color = null;
            if (TextUtils.equals(dto.color, "Red")) {
                color = "红色";
            }else if (TextUtils.equals(dto.color, "Orange")) {
                color = "橙色";
            }else if (TextUtils.equals(dto.color, "Yellow")) {
                color = "黄色";
            }else if (TextUtils.equals(dto.color, "Blue")) {
                color = "蓝色";
            }
            if (!TextUtils.isEmpty(color)) {
                childHolder.tvColor.setText(color);
            }
        }

        String type = null;
        if (!TextUtils.isEmpty(dto.type)) {
            for (int i = 0; i < typeList.size(); i++) {
                if (TextUtils.equals(typeList.get(i).type, dto.type)) {
                    type = typeList.get(i).name;
                    break;
                }
            }
        }
        if (!TextUtils.isEmpty(type)) {
            childHolder.tvType.setText(type);
        }else {
            childHolder.tvType.setText("未知类型");
        }

        if (!TextUtils.isEmpty(dto.content)) {
            childHolder.tvContent.setText(dto.content);
        }
        if (!TextUtils.isEmpty(dto.name)) {
            String heading = null;
            if (dto.name.contains("发布")) {
                heading = dto.name.split("发布")[0];
            }else if (dto.name.contains("解除")) {
                heading = dto.name.split("解除")[0];
            }
            if (!TextUtils.isEmpty(heading)) {
                childHolder.tvUnit.setText(heading);
            }
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
