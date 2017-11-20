package com.china.dto;

import java.io.Serializable;

public class DisasterReportDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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

}
