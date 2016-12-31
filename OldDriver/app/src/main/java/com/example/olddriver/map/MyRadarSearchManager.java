package com.example.olddriver.map;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.radar.RadarNearbyInfo;
import com.baidu.mapapi.radar.RadarNearbyResult;
import com.baidu.mapapi.radar.RadarNearbySearchOption;
import com.baidu.mapapi.radar.RadarSearchError;
import com.baidu.mapapi.radar.RadarSearchListener;
import com.baidu.mapapi.radar.RadarSearchManager;
import com.baidu.mapapi.radar.RadarUploadInfo;
import com.baidu.mapapi.radar.RadarUploadInfoCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzp on 2016/12/8.
 * 管理雷达搜索相关的东西
 * onStart,onStop方法管理雷达的数据自动上传，降低系统资源消耗
 * onDestroy回收资源，删除用户标记
 * startRadarSearch(radius)方法启动雷达搜索
 * setOnGetRadarSearchResultListener获取雷达结果回调.
 */

public class MyRadarSearchManager {
    private int r;
    private final int MAX_SIZE = 15;
    private final int uploadDuration = 2000;
    private RadarSearchManager radarSearchManager;
    private OnGetRadarSearchResultListener listener;
    /*检索半径，单位：m*/

    public MyRadarSearchManager(){
        radarSearchManager = RadarSearchManager.getInstance();
        radarSearchManager.addNearbyInfoListener(defaultListener);
    }

    public void setOnGetRadarSearchResultListener(OnGetRadarSearchResultListener listener){
        this.listener = listener;
    }

    public void startRadarSearch(int radius){
        this.r = radius;
        /*雷达搜索的逻辑*/
        RadarSearchManager radarSearchManager = RadarSearchManager.getInstance();
        radarSearchManager.addNearbyInfoListener(new RadarSearchListener() {
            @Override
            public void onGetNearbyInfoList(RadarNearbyResult radarNearbyResult, RadarSearchError radarSearchError) {
                if(radarSearchError == RadarSearchError.RADAR_NO_ERROR){
                    ArrayList<Neighbour> dataList = new ArrayList<Neighbour>();
                    for(RadarNearbyInfo info : radarNearbyResult.infoList){
                        Neighbour neighbour = new Neighbour();
                        neighbour.distance = info.distance;
                        /*后续需要计算角度*/
                        neighbour.angle = 0;
                        try {
                            JSONObject object = new JSONObject(info.comments);
                            neighbour.neighbourId = object.getString("userId");
                            neighbour.neighbourName = object.getString("userName");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dataList.add(neighbour);
                    }
                    listener.OnGetRadarSearchResult(dataList);
                }
            }

            @Override
            public void onGetUploadState(RadarSearchError radarSearchError) {

            }

            @Override
            public void onGetClearInfoState(RadarSearchError radarSearchError) {

            }
        });


        //构造请求参数，其中centerPt是自己的位置坐标
        LatLng latLng = new LatLng(LocationService.lastLocation.getLatitude(), LocationService.lastLocation.getLongitude());
        RadarNearbySearchOption option = new RadarNearbySearchOption().centerPt(latLng).pageNum(MAX_SIZE).radius(r);
        //发起查询请求
        radarSearchManager.nearbyInfoRequest(option);
    }

    interface OnGetRadarSearchResultListener{
        void OnGetRadarSearchResult(List<Neighbour> dataList);
    }

    private RadarSearchListener defaultListener = new RadarSearchListener(){

        @Override
        public void onGetNearbyInfoList(RadarNearbyResult radarNearbyResult, RadarSearchError radarSearchError) {}

        @Override
        public void onGetUploadState(RadarSearchError radarSearchError) {

        }

        @Override
        public void onGetClearInfoState(RadarSearchError radarSearchError) {

        }
    };

    public void onStart(){
        radarSearchManager.startUploadAuto(new RadarUploadInfoCallback() {
            @Override
            public RadarUploadInfo onUploadInfoCallback() {
                //构造自动上传数据
                RadarUploadInfo info = new RadarUploadInfo();
                info.pt = new LatLng(LocationService.lastLocation.getLatitude(),
                        LocationService.lastLocation.getLongitude());

                //构造json传进comments
                //info.comments

                return info;
            }
        }, uploadDuration);
    }

    public void onStop(){
        radarSearchManager.stopUploadAuto();
    }

    public void onDestroy(){
        //清楚用户标记
        radarSearchManager.clearUserInfo();
        //移除监听
        radarSearchManager.removeNearbyInfoListener(defaultListener);
        //释放资源
        radarSearchManager.destroy();
        radarSearchManager = null;
    }

    public static class Neighbour{
        public String neighbourId;
        public String neighbourName;
        public int angle;
        public int distance;
    }
}
