package com.china.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class StationMonitorDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int section;
	public String stationId;//站点号
	public String name;//站点名称
	public String time;
	public String lat;
	public String lng;
	public String ballTemp;//干球温度
	public String airPressure;//气压
	public String humidity;//湿度
	public String precipitation1h;//1h降水量
	public String precipitation3h;//3h降水量
	public String precipitation6h;//6h降水量
	public String precipitation12h;//12h降水量
	public String precipitation24h;//24h降水量
	public String windDir;//风向
	public String windSpeed;//风速
	public String distance;//距离
	public String pointTemp;//露点温度
	public String visibility;//能见度
	public String value;

	public String partition;
	public String provinceName;
	public String cityName;
	public String districtName;
	public String addr;
	public List<String> areaList = new ArrayList<>();//华北、华东、华中、华南、东北、西北、西南
	
	public String currentTemp;//当前温度
	public String current1hRain;//当前1h降水量
	public String currentHumidity;//当前湿度
	public String currentWindSpeed;//当前风速
	public String currentPressure;//当前气压
	public String currentVisible;//当前能见度
	
	public String statisHighTemp;//24h最高气温
	public String statisLowTemp;//24h最低气温
	public String statisAverTemp;//24h平巨额气温
	public String statis3hRain;//3h降水
	public String statis6hRain;//6h降水
	public String statis12hRain;//12h降水
	public String statis24hRain;//24h降水
	public String statisMaxHumidity;//24h最大湿度
	public String statisMinHumidity;//24h最小湿度
	public String statisMaxSpeed;//24h最大风速
	public String statisMaxPressure;//24h最大气压
	public String statisMinPressure;//24h最小气压
	public String statisMinVisible;//24h最小能见度
	public List<StationMonitorDto> dataList = new ArrayList<StationMonitorDto>();//24h数据list
	public float x = 0;//x轴坐标点
	public float y = 0;//y轴坐标点
}
