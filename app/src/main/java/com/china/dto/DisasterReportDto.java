package com.china.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class DisasterReportDto implements Parcelable{

	public String vSendername;//灾情发布气象机构省市县中文名
	public String vCategory;//灾害类型
	public String vEdittime;//上报时间
	public String vGeneralLoss;//经济损失
	public String vRzDpop;//死亡人数
	public String vEditor;//上报人
	public String vTaPhone;//电话
	public String vSummary;//过程概述
	public String vInfluenceDiscri;//灾害影响描述
	public String vStartTime;//开始时间
	public String vEndTime;//结束时间
	public String dRecordId;//直播信息编号

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.vSendername);
		dest.writeString(this.vCategory);
		dest.writeString(this.vEdittime);
		dest.writeString(this.vGeneralLoss);
		dest.writeString(this.vRzDpop);
		dest.writeString(this.vEditor);
		dest.writeString(this.vTaPhone);
		dest.writeString(this.vSummary);
		dest.writeString(this.vInfluenceDiscri);
		dest.writeString(this.vStartTime);
		dest.writeString(this.vEndTime);
		dest.writeString(this.dRecordId);
	}

	public DisasterReportDto() {
	}

	protected DisasterReportDto(Parcel in) {
		this.vSendername = in.readString();
		this.vCategory = in.readString();
		this.vEdittime = in.readString();
		this.vGeneralLoss = in.readString();
		this.vRzDpop = in.readString();
		this.vEditor = in.readString();
		this.vTaPhone = in.readString();
		this.vSummary = in.readString();
		this.vInfluenceDiscri = in.readString();
		this.vStartTime = in.readString();
		this.vEndTime = in.readString();
		this.dRecordId = in.readString();
	}

	public static final Creator<DisasterReportDto> CREATOR = new Creator<DisasterReportDto>() {
		@Override
		public DisasterReportDto createFromParcel(Parcel source) {
			return new DisasterReportDto(source);
		}

		@Override
		public DisasterReportDto[] newArray(int size) {
			return new DisasterReportDto[size];
		}
	};
}
