package com.china.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsDto implements Parcelable{

	public String id = null;//数组的下标0-9对应没个镇的id
	public String imgUrl = null;//图片地址
	public String title = null;//标题
	public String time = null;//时间
	public String detailUrl = null;//详情页地址
	public String showType;//显示类型
	public boolean isSelected = false;//是否被选中
	public String header;

	public NewsDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.id);
		dest.writeString(this.imgUrl);
		dest.writeString(this.title);
		dest.writeString(this.time);
		dest.writeString(this.detailUrl);
		dest.writeString(this.showType);
		dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
		dest.writeString(this.header);
	}

	protected NewsDto(Parcel in) {
		this.id = in.readString();
		this.imgUrl = in.readString();
		this.title = in.readString();
		this.time = in.readString();
		this.detailUrl = in.readString();
		this.showType = in.readString();
		this.isSelected = in.readByte() != 0;
		this.header = in.readString();
	}

	public static final Creator<NewsDto> CREATOR = new Creator<NewsDto>() {
		@Override
		public NewsDto createFromParcel(Parcel source) {
			return new NewsDto(source);
		}

		@Override
		public NewsDto[] newArray(int size) {
			return new NewsDto[size];
		}
	};
}
