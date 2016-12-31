package com.example.olddriver.util;

import com.baidu.mapapi.search.route.DrivingRouteLine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 苏颂贤 on 2016/12/5.
 * 为路径规划数据进行格式化
 */

public class DataFormater {
    public static String formatDistance(int meters){
        if(meters < 1000)
            return meters + "米";

        float kilometer = meters / 1000.0f;

        return String.format("%.1f" ,kilometer) + "公里";
    }

    public static String formatTime(DrivingRouteLine line){
        int min = line.getDuration() / 60;

        if(min == 0)
            return "一分钟内";

        if(min < 60)
            return min + "分钟";

        if((min / 60) >= 24){
            int h = min / 60;
            int d = h / 24;
            return d + "天" + h +"小时";
        }else
            return min / 60 + "小时" + min % 60 + "分";
    }

    public static String formatTime(String markedTime) throws ParseException {
        SimpleDateFormat s1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date tempDate =null;
        tempDate = s1.parse(markedTime);

        long ans = new Date().getTime() - tempDate.getTime();

        ans/=1000;

        if(ans < 60)
            return ans+"秒前";
        else{
            ans/=60;
            if(ans < 60)
                return ans+"分钟前";

            return ans/60 + "小时" + ans%60 + "分钟前";
        }

    }

}
