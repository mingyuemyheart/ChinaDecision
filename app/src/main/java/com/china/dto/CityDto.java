package com.china.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class CityDto implements Parcelable{

	public String alpha = null;//首字母
	public String cityName = null;//城市名称
	public String cityId = null;//城市id
	public String spellName = null;//全拼名称
	public String provinceName = null;//省份名称
	public String areaName = null;
	public double lng = 0;//经度
	public double lat = 0;//维度

	public CityDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.alpha);
		dest.writeString(this.cityName);
		dest.writeString(this.cityId);
		dest.writeString(this.spellName);
		dest.writeString(this.provinceName);
		dest.writeString(this.areaName);
		dest.writeDouble(this.lng);
		dest.writeDouble(this.lat);
	}

	protected CityDto(Parcel in) {
		this.alpha = in.readString();
		this.cityName = in.readString();
		this.cityId = in.readString();
		this.spellName = in.readString();
		this.provinceName = in.readString();
		this.areaName = in.readString();
		this.lng = in.readDouble();
		this.lat = in.readDouble();
	}

	public static final Creator<CityDto> CREATOR = new Creator<CityDto>() {
		@Override
		public CityDto createFromParcel(Parcel source) {
			return new CityDto(source);
		}

		@Override
		public CityDto[] newArray(int size) {
			return new CityDto[size];
		}
	};
}
