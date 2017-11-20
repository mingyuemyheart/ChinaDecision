package com.china.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ColumnData implements Parcelable{

	public String columnId;//栏目id
	public String id;//频道id,区分频道标示
	public String name;//频道名称
	public String level;//1为显示，0为不显示
	public String showType;//分为local、news
	public String icon;//未选中图片地址
	public String icon2;//选中图片地址
	public String desc;//描述信息
	public String dataUrl;//如果存在，则是网页数据
	public String newsType;//阅读量
	public String newsCount;//文章数
	public ArrayList<ColumnData> child = new ArrayList<ColumnData>();//儿子辈
	public String localTag;
	public String bannerUrl;
	public String imgUrl;
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeString(columnId);
		arg0.writeString(id);
		arg0.writeString(name);
		arg0.writeString(level);
		arg0.writeString(showType);
		arg0.writeString(icon);
		arg0.writeString(icon2);
		arg0.writeString(desc);
		arg0.writeString(dataUrl);
		arg0.writeString(newsType);
		arg0.writeString(newsCount);
		arg0.writeString(localTag);
		arg0.writeString(bannerUrl);
		arg0.writeString(imgUrl);
		arg0.writeList(child);
	}
	
	public static final Creator<ColumnData> CREATOR = new Creator<ColumnData>() {
		@Override
		public ColumnData[] newArray(int arg0) {
			return new ColumnData[arg0];
		}
		
		@Override
		public ColumnData createFromParcel(Parcel arg0) {
			ColumnData columnData = new ColumnData();
			columnData.columnId = arg0.readString();
			columnData.id = arg0.readString();
			columnData.name = arg0.readString();
			columnData.level = arg0.readString();
			columnData.showType = arg0.readString();
			columnData.icon = arg0.readString();
			columnData.icon2 = arg0.readString();
			columnData.desc = arg0.readString();
			columnData.dataUrl = arg0.readString();
			columnData.newsType = arg0.readString();
			columnData.newsCount = arg0.readString();
			columnData.localTag = arg0.readString();
			columnData.bannerUrl = arg0.readString();
			columnData.imgUrl = arg0.readString();
			arg0.readList(columnData.child, ColumnData.class.getClassLoader());
			return columnData;
		}
	};
	
}
