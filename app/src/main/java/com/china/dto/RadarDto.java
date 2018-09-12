package com.china.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class RadarDto implements Parcelable {

	public String radarName;//雷达名称
	public String radarCode;//雷达站号
	public String imgUrl;//图片名称
	public String imgPath;//保存在缓存路径
	public String time;//发布时间
	public boolean isSelected;
	public double lat,lng;

	public RadarDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.radarName);
		dest.writeString(this.radarCode);
		dest.writeString(this.imgUrl);
		dest.writeString(this.imgPath);
		dest.writeString(this.time);
		dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
		dest.writeDouble(this.lat);
		dest.writeDouble(this.lng);
	}

	protected RadarDto(Parcel in) {
		this.radarName = in.readString();
		this.radarCode = in.readString();
		this.imgUrl = in.readString();
		this.imgPath = in.readString();
		this.time = in.readString();
		this.isSelected = in.readByte() != 0;
		this.lat = in.readDouble();
		this.lng = in.readDouble();
	}

	public static final Creator<RadarDto> CREATOR = new Creator<RadarDto>() {
		@Override
		public RadarDto createFromParcel(Parcel source) {
			return new RadarDto(source);
		}

		@Override
		public RadarDto[] newArray(int size) {
			return new RadarDto[size];
		}
	};
}
