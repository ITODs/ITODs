package com.example.olddriver.bean;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.example.olddriver.util.DataFormater;

import java.util.HashMap;

/**
 * Created by lzp on 2016/12/13.
 */

public class SearchResult {
    //临时保存一个搜索结果，方便跳转使用{因为里面某对象没有实现序列化接口。。}
    public static SearchResult lastSearchResult = null;

    private String key = "";
    private String addr = "";
    private HashMap<DrivingRoutePlanOption.DrivingPolicy,DrivingRouteLine> routeLineDataMap;
    private String distance = "";
    private String taxiPrice = "";

    public LatLng getTargetLatLng() {
        return targetLatLng;
    }

    public void setTargetLatLng(LatLng targetLatLng) {
        this.targetLatLng = targetLatLng;
    }

    private LatLng targetLatLng = null;

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public HashMap<DrivingRoutePlanOption.DrivingPolicy, DrivingRouteLine> getRouteLineDataMap() {
        return routeLineDataMap;
    }

    public void setRouteLineDataMap(HashMap<DrivingRoutePlanOption.DrivingPolicy, DrivingRouteLine> routeLineDataMap) {
        this.routeLineDataMap = routeLineDataMap;

        //从路径规划结果中获取路程数据
        if(routeLineDataMap != null){
            String distance = "";
            DrivingRouteLine line = null;
            if((line = routeLineDataMap.get(DrivingRoutePlanOption.DrivingPolicy.ECAR_TIME_FIRST)) != null){
                distance = DataFormater.formatDistance(line.getDistance());
            }else if((line = routeLineDataMap.get(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST)) != null){
                distance = DataFormater.formatDistance(line.getDistance());
            }else if((line = routeLineDataMap.get(DrivingRoutePlanOption.DrivingPolicy.ECAR_FEE_FIRST)) != null){
                distance = DataFormater.formatDistance(line.getDistance());
            }

            this.distance = distance;
        }
    }

    public String getTaxiPrice() {
        return taxiPrice;
    }

    public void setTaxiPrice(String taxiPrice) {
        this.taxiPrice = taxiPrice;
    }
}
