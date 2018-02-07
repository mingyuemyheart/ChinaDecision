package com.china.utils;

import android.util.Base64;
import android.util.Log;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密访问接口地址工具类
 */

public class SecretUrlUtil {

    private static final String APPID = "6f688d62594549a2";//机密需要用到的AppId
    private static final String CHINAWEATHER_DATA = "chinaweather_data";//加密秘钥名称

    /**
     * 获取9位城市id
     * @param lng 经度
     * @param lat 维度
     * @return
     */
    public static final String geo(double lng, double lat) {
        String URL = "http://geoload.tianqi.cn/ag9/";

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm");
        String sysdate = sdf1.format(new Date());

        StringBuffer buffer = new StringBuffer();
        buffer.append(URL);
        buffer.append("?");
        buffer.append("lon=").append(lng);
        buffer.append("&");
        buffer.append("lat=").append(lat);
        buffer.append("&");
        buffer.append("date=").append(sysdate);
        buffer.append("&");
        buffer.append("appid=").append(APPID);

        String key = getKey(CHINAWEATHER_DATA, buffer.toString());
        buffer.delete(buffer.lastIndexOf("&"), buffer.length());

        buffer.append("&");
        buffer.append("appid=").append(APPID.substring(0, 6));
        buffer.append("&");
        buffer.append("key=").append(key.substring(0, key.length() - 3));
        String result = buffer.toString();
        return result;
    }

    /**
     * 获取秘钥
     * @param key
     * @param src
     * @return
     */
    public static final String getKey(String key, String src) {
        try{
            byte[] rawHmac = null;
            byte[] keyBytes = key.getBytes("UTF-8");
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            rawHmac = mac.doFinal(src.getBytes("UTF-8"));
            String encodeStr = Base64.encodeToString(rawHmac, Base64.DEFAULT);
            String keySrc = URLEncoder.encode(encodeStr, "UTF-8");
            return keySrc;
        }catch(Exception e){
            Log.e("SceneException", e.getMessage(), e);
        }
        return null;
    }

}
