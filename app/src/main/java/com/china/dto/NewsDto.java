package com.china.dto;

import java.io.Serializable;

public class NewsDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String id = null;//数组的下标0-9对应没个镇的id
	public String imgUrl = null;//图片地址
	public String title = null;//标题
	public String time = null;//时间
	public String detailUrl = null;//详情页地址
	public String showType;//显示类型
	public boolean isSelected = false;//是否被选中
}
