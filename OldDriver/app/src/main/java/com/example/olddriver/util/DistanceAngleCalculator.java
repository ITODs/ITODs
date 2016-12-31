package com.example.olddriver.util;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

/**
 * Created by lzp on 2016/12/8.
 */

public class DistanceAngleCalculator {
    //返回直线距离 单位 ；米
    public static int calculateDistance(LatLng myLatLng, LatLng target){
        return (int)DistanceUtil.getDistance(myLatLng,target);
    }

    public static class DistanceAndAngle{
        public int distance;
        public int angle;
    }
}
