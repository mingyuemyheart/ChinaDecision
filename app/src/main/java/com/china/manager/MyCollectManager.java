package com.china.manager;

/**
 * 我的收藏管理器，获取、保存数据
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.china.dto.NewsDto;

import java.util.List;

public class MyCollectManager {

	public static int readCollect(Context context, List<NewsDto> list) {
		list.clear();
		SharedPreferences sp = context.getSharedPreferences("COLLECT", Context.MODE_PRIVATE);
		int size = sp.getInt("collectSize", 0);
		for (int i = 0; i < size; i++) {
			NewsDto dto = new NewsDto();
			dto.title = sp.getString("title"+i, null);
			dto.detailUrl = sp.getString("detailUrl"+i, null);
			dto.time = sp.getString("time"+i, null);
			dto.imgUrl = sp.getString("imgUrl"+i, null);
			list.add(dto);
		}
		return size;
	}
	
	public static void writeCollect(Context context, List<NewsDto> list) {
		if (list == null) {
			return;
		}
		SharedPreferences sp = context.getSharedPreferences("COLLECT", Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt("collectSize", list.size());
		for (int i = 0; i < list.size(); i++) {
			editor.remove("title"+i);
			editor.remove("detailUrl"+i);
			editor.remove("time"+i);
			editor.remove("imgUrl"+i);
			
			editor.putString("title"+i, list.get(i).title);
			editor.putString("detailUrl"+i, list.get(i).detailUrl);
			editor.putString("time"+i, list.get(i).time);
			editor.putString("imgUrl"+i, list.get(i).imgUrl);
		}
		editor.commit();
	}
	
	public static void clearCollectData(Context context) {
		SharedPreferences sp = context.getSharedPreferences("COLLECT", Context.MODE_PRIVATE);
		sp.edit().clear().commit();
	}
	
}
