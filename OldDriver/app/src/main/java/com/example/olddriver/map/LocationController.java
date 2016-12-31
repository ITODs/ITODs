package com.example.olddriver.map;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.olddriver.R;

/**
 * Created by lzp on 2016/11/30.
 * 封装定位和手机方向传感器的一些回调操作
 * 封装地图定位层的操作和数据更新
 */

public class LocationController extends MyOrientationClient {
    private float direction = 0.0f;
    private BaiduMap baiduMap;
    private LocationService locationService;
    private boolean isFirst = true;

    public LocationController(final Context context, BaiduMap map) {
        super(context);
        baiduMap = map;

        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);
        this.setLocationMode(MyLocationConfiguration.LocationMode.COMPASS);

        //初始化2s申请一次定位
        locationService = new LocationService(context, 2000);
        locationService.setLocationListener(new LocationService.MyLocationListener() {
            @Override
            public void getLocation(BDLocation location) {

                if(isFirst){
                    isFirst = false;
                    MyLocationData locData = new MyLocationData.Builder()
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                            .direction(direction).latitude(LocationService.lastLocation.getLatitude())
                            .longitude(LocationService.lastLocation.getLongitude()).build();
                    // 设置定位数据
                    baiduMap.setMyLocationData(locData);

                    MapStatus mMapStatus = new MapStatus.Builder()
                            .target(new LatLng(LocationService.lastLocation.getLatitude(), LocationService.lastLocation.getLongitude()))
                            .zoom(18)
                            .build();
                    // 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

                    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
                            .newMapStatus(mMapStatus);
                    // 改变地图状态
                    baiduMap.setMapStatus(mMapStatusUpdate);
                }
            }
        });


        this.setOnOrientationListener(new OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                if (Math.abs(direction - x) > 1) {
                    direction = x;

                    if(LocationService.lastLocation == null){
                        //还没有获取到定位数据，直接返回
                        return;
                    }

                    //更新map的位置状态
                    // 构造定位数据
                    MyLocationData locData = new MyLocationData.Builder()
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                            .direction(direction).latitude(LocationService.lastLocation.getLatitude())
                            .longitude(LocationService.lastLocation.getLongitude()).build();
                    // 设置定位数据
                    baiduMap.setMyLocationData(locData);
                }
            }
        });
    }


    @Override
    public void start() {
        super.start();
        locationService.startLocationService();
        Log.i("定位服务","start");
    }

    @Override
    public void stop() {
        super.stop();
        locationService.stopLocationService();
        Log.i("定位服务","stop");
    }

    public void setLocationMode(MyLocationConfiguration.LocationMode mode){
        MyLocationConfiguration config = new MyLocationConfiguration(mode, true, null);
        baiduMap.setMyLocationConfigeration(config);
    }

}
