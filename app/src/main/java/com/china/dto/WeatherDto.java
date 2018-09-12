package com.china.dto;

import java.io.Serializable;

public class WeatherDto implements Serializable{

	private static final long serialVersionUID = 1L;

	public String cityName,cityId;
	public double lat, lng;
	public String level;
	public float minuteFall= 0;//逐分钟降水量

	//平滑曲线
	public int hourlyTemp = 0;//逐小时温度
	public String hourlyTime = null;//逐小时时间
	public int hourlyCode = 0;//天气现象编号
	public float x = 0;//x轴坐标点
	public float y = 0;//y轴坐标点
	
	//列表、趋势
	public String week = null;//周几
	public String date = null;//日期
	public String lowPhe = null;//晚上天气现象
	public int lowPheCode = -1;//晚上天气现象编号
	public int lowTemp = 0;//最低气温
	public int lowWindDir = 0;
	public int lowWindForce = 0;
	public float lowX = 0;//最低温度x轴坐标点
	public float lowY = 0;//最低温度y轴坐标点
	public String highPhe = null;//白天天气现象
	public int highPheCode = -1;//白天天气现象编号
	public int highTemp = 0;//最高气温
	public int highWindDir = 0;
	public int highWindForce = 0;
	public float highX = 0;//最高温度x轴坐标点
	public float highY = 0;//最高温度y轴坐标点
	
}
