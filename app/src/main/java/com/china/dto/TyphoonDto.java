package com.china.dto;

import android.content.Context;
import android.text.TextUtils;

import com.china.R;
import com.china.utils.WeatherUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class TyphoonDto {

	public int yearly;//哪年的台风
	public String name;//台风名称
	public String id;//台风id
	public String code;//台风code
	public String enName;//台风应为名称
	public String status;//台风状态,stop、start
	public double lat,lng;
	public String pressure;//气压
	public String max_wind_speed;//最大风速
	public String move_speed;//移动速度
	public String wind_dir;//移动方向
	public String type;//台风类型
	public String radius_7;
	public String radius_10;
	public String time;
	public boolean isFactPoint = true;//true为实况点，false为预报点
	public String strength;//台风强度
	public boolean isSelected;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日 HH时", Locale.CHINA);
	
	public String content(Context context) {
		StringBuffer buffer = new StringBuffer();
		if (!TextUtils.isEmpty(time)) {
			try {
				buffer.append("时间：").append(sdf2.format(sdf1.parse(time))).append("\n");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if(!TextUtils.isEmpty(max_wind_speed)){
			if (!TextUtils.isEmpty(type)) {
				if (TextUtils.equals(type, "1")) {
					strength = context.getString(R.string.typhoon_level1);
				}else if (TextUtils.equals(type, "2")) {
					strength = context.getString(R.string.typhoon_level2);
				}else if (TextUtils.equals(type, "3")) {
					strength = context.getString(R.string.typhoon_level3);
				}else if (TextUtils.equals(type, "4")) {
					strength = context.getString(R.string.typhoon_level4);
				}else if (TextUtils.equals(type, "5")) {
					strength = context.getString(R.string.typhoon_level5);
				}else if (TextUtils.equals(type, "6")) {
					strength = context.getString(R.string.typhoon_level6);
				}
				buffer.append("中心风力："+ WeatherUtil.getHourWindForce(Float.parseFloat(max_wind_speed))+"("+strength+")"+"\n");
			}
			buffer.append("最大风速：").append(max_wind_speed).append("米/秒");
		}
		if(!TextUtils.isEmpty(pressure)){
			buffer.append("\n").append("中心气压：").append(pressure).append("hPa");
		}
		if(!TextUtils.isEmpty(wind_dir)){
			buffer.append("\n").append("移动方向：").append(wind_dir);
		}
		if(!TextUtils.isEmpty(move_speed)){
			buffer.append("\n").append("移动速度：").append(move_speed).append("公里/小时");
		}
		if(!TextUtils.isEmpty(radius_7)){
			buffer.append("\n").append("7级风圈半径：").append(radius_7).append("公里");
		}
		if(!TextUtils.isEmpty(radius_10)){
			buffer.append("\n").append("10级风圈半径：").append(radius_10).append("公里");
		}

		return buffer.toString();
	}
	
	public int icon() {
		int power = TextUtils.isEmpty(type) ? -1: Integer.parseInt(type);
		if(power == 1){
			return R.drawable.shawn_typhoon_level1;
		}else if(power == 2){
			return R.drawable.shawn_typhoon_level2;
		}else if(power == 3){
			return R.drawable.shawn_typhoon_level3;
		}else if(power == 4){
			return R.drawable.shawn_typhoon_level4;
		}else if(power == 5){
			return R.drawable.shawn_typhoon_level5;
		}else if(power == 6){
			return R.drawable.shawn_typhoon_level6;
		}
		return R.drawable.shawn_typhoon_yb;
	}
	
}
