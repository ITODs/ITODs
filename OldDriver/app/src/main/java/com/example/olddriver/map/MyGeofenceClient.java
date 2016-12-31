package com.example.olddriver.map;
import com.baidu.mapapi.model.LatLng;
import com.example.olddriver.bean.Markers;
import com.example.olddriver.util.DistanceAngleCalculator;

import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by lzp on 2016/12/8.
 */

public class MyGeofenceClient {
    //默认两百米弹出提醒
    private int PERIMETER = 500;
    private static int DEFAULT_PERIOD = 2000;
    private List<Markers> dataList = null;
    private Timer geofenceTimer;
    private EnterGeofenceListener listener = null;

    //历史纪录set 已经报告过的marker不再重复报告
    private HashSet<String> historySet = new HashSet<>();

    private class GeofenceTask extends TimerTask{
        @Override
        public void run() {
            if(dataList != null){
                //算一下各个marker 和 自己位置距离，角度
                LatLng myLatLng = new LatLng(LocationService.lastLocation.getLatitude(), LocationService.lastLocation.getLongitude());
                for(Markers marker : dataList){

                    if(historySet.contains(marker.getObjectId()))
                        continue;

                    LatLng target = new LatLng(marker.getM_latitude(),marker.getM_longtitude());
                    int distance = DistanceAngleCalculator.calculateDistance(myLatLng,target);
                    if(distance < PERIMETER && listener != null){
                        listener.enterGeofence(marker , distance);

                        historySet.add(marker.getObjectId());
                    }
                }
            }else{
                if(LocationService.lastLocation != null){
                    int cityId = Integer.valueOf(LocationService.lastLocation.getCityCode());
                    getData(cityId);
                }
            }
        }
    }

    public void setEnterGeofenceListener(EnterGeofenceListener listener){
        this.listener = listener;
    }

    public interface EnterGeofenceListener{
        void enterGeofence(Markers marker, int distance);
    }

    public MyGeofenceClient(){}

    public void onCreate(){
        if(LocationService.lastLocation != null){
            int cityId = Integer.valueOf(LocationService.lastLocation.getCityCode());
            getData(cityId);
        }
    }

    public void onStart(){
        geofenceTimer = new Timer("geofenceTimer");
        geofenceTimer.schedule(new GeofenceTask(),0,DEFAULT_PERIOD);
    }

    public void onStop(){
        if(geofenceTimer != null)
            geofenceTimer.cancel();
    }

    private void getData(int cityId){
        BmobQuery<Markers> query = new BmobQuery<>("Markers");
        query.addWhereEqualTo("m_city_id", cityId);
        query.setLimit(100000);
        query.findObjects(new FindListener<Markers>() {
            @Override
            public void done(List<Markers> list, BmobException e) {
                if(e == null){
                    dataList = list;
                }
            }
        });
    }
}
