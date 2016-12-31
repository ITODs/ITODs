package com.example.olddriver.map;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.example.olddriver.bean.Markers;
import com.example.olddriver.map.overlay.MarkerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by lzp on 2016/11/30.
 * 管理各种障碍物marker的构造以及可见性
 * ui线程中需要同时调用本类的onResume和 onPause方法
 */

public class MarkersManager {
    private List<Markers> markerList;
    private Handler handler;
    private BaiduMap baiduMap;
    private boolean isAccidentVisiable, isWorkVisiable, isSpeedTestVisiable,isPoliceVisiable;

    private float currentZoom = 18.0f;
    public static final float MIN_ZOOM = 14.0f;
    private boolean isMarkersVisiable = true;
    /*用于刷新数据的定时任务*/
    private Timer syncTimer;
    
    
    /*三种标记物*/
    private MarkerManager policeMarkerManager;
    private MarkerManager roadblockMarkerManager;
    private MarkerManager speedTestMarkerManager;
    private MarkerManager accidentMarkerManager;

    public MarkersManager(Activity activity, final BaiduMap map, Handler handler) {
        baiduMap = map;
        this.handler = handler;

        policeMarkerManager = new MarkerManager(activity,baiduMap, MARKER_POLICE);
        roadblockMarkerManager = new MarkerManager(activity,baiduMap, MARKER_WORK);
        speedTestMarkerManager = new MarkerManager(activity,baiduMap, MARKER_SPEED_TEST);
        accidentMarkerManager = new MarkerManager(activity,baiduMap, MARKER_ACCIDENT);

        /*baiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                //检查地图比例尺，如果小于一定程度，取消所有marker的显示
                if(Math.abs(MarkersManager.this.currentZoom - mapStatus.zoom) > 0.001){
                    currentZoom = mapStatus.zoom;

                    if(currentZoom < MIN_ZOOM && isMarkersVisiable == true){
                        Log.i("mBaiduMap", "取消marker的显示");
                        isMarkersVisiable = false;
                    }

                    if(currentZoom >= MIN_ZOOM && isMarkersVisiable == false){
                        Log.i("mBaiduMap", "显示marker");
                        isMarkersVisiable = true;

                    }
                }
            }
        });*/
        getMarkerSettings();
    }


    public static final int MARKER_ACCIDENT = 0;
    public static final int MARKER_WORK = 1;
    public static final int MARKER_SPEED_TEST = 2;
    public static final int MARKER_POLICE = 3;


    /*是否启用路障标记*/
    /*是否启用测速标记*/
    /*是否启用查车标记*/
    /*是否启用车祸标记*/
    public void setVisiable(boolean b, int type){
        if(policeMarkerManager == null){
            /*数据还没有被初始化*/
            return;
        }

        switch (type){
            case MARKER_ACCIDENT:
                if(isAccidentVisiable != b){
                    isAccidentVisiable = b;
                }
                break;
            
            case MARKER_WORK:
                if(isWorkVisiable != b){
                    isWorkVisiable = b;
                }
                break;
            
            case MARKER_SPEED_TEST:
                if(isSpeedTestVisiable != b){
                    isSpeedTestVisiable = b;
                }
                break;
            
            case MARKER_POLICE:
                if(isPoliceVisiable != b){
                    isPoliceVisiable = b;
                }
                break;

            default:
                break;
        }
    }



    public static void getAllMarker(int cityId, FindListener<Markers> findListener){
        BmobQuery<Markers> query = new BmobQuery<>("Markers");
        query.addWhereEqualTo("m_city_id", cityId);
        query.setLimit(100000);
        query.findObjects(findListener);
    }


    public void addMarker(int type){
        Markers marker = new Markers();
        BDLocation location = com.example.olddriver.map.LocationService.lastLocation;

        marker.setM_type(type);
        marker.setM_longtitude(location.getLongitude());
        marker.setM_latitude(location.getLatitude());
        marker.setM_city_id(Integer.parseInt(location.getCityCode()));
        marker.setM_location(location.getAddrStr());
        //marker.setUser_id(BmobUser.getCurrentUser().getObjectId());

        marker.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                //保存结果。
                syncTimer.cancel();
                syncTimer = new Timer("syncTimer");
                syncTimer.schedule(new SyncTask(), 0, 10000);
            }
        });

    }

    public static void update(Markers marker, boolean isTrue){
        if(isTrue)
            marker.setTrue_num(marker.getTrue_num() + 1);
        else
            marker.setFalse_num(marker.getFalse_num() + 1);

        marker.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {

            }
        });
    }

    /*读取sharedpreference的用户配置*/
    private void getMarkerSettings() {
        isAccidentVisiable = true;
        isWorkVisiable = true;
        isSpeedTestVisiable = true;
        isPoliceVisiable = true;
    }

    private class SyncTask extends TimerTask{
        @Override
        public void run() {
            /*到云上同步数据*/
            final ArrayList<Markers> policeMarkerList = new ArrayList<>();
            final ArrayList<Markers> roadblockMarkerList = new ArrayList<>();
            final ArrayList<Markers> speedTestMarkerList = new ArrayList<>();
            final ArrayList<Markers> accidentMarkerList = new ArrayList<>();

            int cityId = -1;
            if(com.example.olddriver.map.LocationService.lastLocation != null){
                try{
                    cityId = Integer.parseInt(LocationService.lastLocation.getCityCode());
                }catch (Exception e){
                    e.printStackTrace();
                    return;
                }
            }
            else{
                return;
            }

            getAllMarker(cityId,new FindListener<Markers>() {
                @Override
                public void done(List<Markers> list, BmobException e) {
                    if(e == null && list.size() != 0){
                        for(Markers marker : list){
                            Log.i("bmob", marker.getM_city_id() + "  type: " + marker.getM_type());
                            Log.i("latlng:", marker.getM_latitude() + "  " + marker.getM_longtitude());

                            if(marker.getM_type() == MARKER_ACCIDENT){
                                accidentMarkerList.add(marker);
                                Log.i("list", accidentMarkerList.size() + "");
                            }else if(marker.getM_type() == MARKER_WORK){
                                roadblockMarkerList.add(marker);
                            }else if(marker.getM_type() == MARKER_SPEED_TEST){
                                speedTestMarkerList.add(marker);
                            }else if(marker.getM_type() == MARKER_POLICE){
                                policeMarkerList.add(marker);
                            }
                        }

                        /*加载数据进入各个markerManager*/
                        policeMarkerManager.setMarkersList(policeMarkerList);
                        roadblockMarkerManager.setMarkersList(roadblockMarkerList);
                        speedTestMarkerManager.setMarkersList(speedTestMarkerList);
                        accidentMarkerManager.setMarkersList(accidentMarkerList);

                        /*根据用户设置显示某些标记*/
                        if(isPoliceVisiable)
                            policeMarkerManager.addToMap();
                        if(isWorkVisiable)
                            roadblockMarkerManager.addToMap();
                        if(isSpeedTestVisiable)
                            speedTestMarkerManager.addToMap();
                        if(isAccidentVisiable)
                            accidentMarkerManager.addToMap();
                    }
                    else{
                        if(policeMarkerList == null || policeMarkerList.size() == 0){
                            policeMarkerManager.removeFromMap();
                        }

                        if(roadblockMarkerList == null || roadblockMarkerList.size() == 0){
                            roadblockMarkerManager.removeFromMap();
                        }

                        if(speedTestMarkerList == null || speedTestMarkerList.size() == 0){
                            speedTestMarkerManager.removeFromMap();
                        }

                        if(accidentMarkerList == null || accidentMarkerList.size() == 0){
                            accidentMarkerManager.removeFromMap();
                        }

                        Log.i("bmob",e.toString());
                    }
                }
            });
        }
    }

    /*在ui线程的onResume和 onPause中调用，停止更新*/
    public void onResume(){
        /*10s同步一次marker*/
        this.syncTimer = new Timer("markersSyncTimer");
        syncTimer.schedule(new SyncTask(),0,10000);
    }

    public void onPause(){
        syncTimer.cancel();
    }
}
