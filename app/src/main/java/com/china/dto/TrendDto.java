package com.china.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrendDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int section;
	public String areaName;//区名称
	public String streetName;//街道名称
	public float x = 0;//x轴坐标点
	public float y = 0;//y轴坐标点
	public int temp;//温度
	public int humidity;//相对湿度
	public float rainFall;//降水量
	public float windSpeed;//风速
	public int windDir;//风向
	public int pressure;//气压
	
	public List<TrendDto> tempList = new ArrayList<TrendDto>();//温度list
	public List<TrendDto> humidityList = new ArrayList<TrendDto>();//相对湿度list
	public List<TrendDto> rainFallList = new ArrayList<TrendDto>();//降水量list
	public List<TrendDto> windSpeedList = new ArrayList<TrendDto>();//风速list
	public List<TrendDto> pressureList = new ArrayList<TrendDto>();//气压list
	 
}
