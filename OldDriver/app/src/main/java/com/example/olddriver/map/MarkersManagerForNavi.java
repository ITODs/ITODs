package com.example.olddriver.map;

import android.app.Activity;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.example.olddriver.R;
import com.example.olddriver.bean.Markers;

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
 * Created by lzp on 2016/12/15.
 */

public class MarkersManagerForNavi {
    private List<Markers> markerList;
    private Activity activity;
    //放入地图的markerList
    private ArrayList<BNRouteGuideManager.CustomizedLayerItem> markerDataList = new ArrayList<>();

    private boolean[] isItemVisiable = new boolean[4];
    private boolean isVisiable = true;
    //顺序isAccidentVisiable, isWorkVisiable, isSpeedTestVisiable,isPoliceVisiable;

    /*用于刷新数据的定时任务*/
    private Timer syncTimer;


    public MarkersManagerForNavi(Activity activity){
        this.activity = activity;

        getNaviMarkersSettings();
    }

    public void getNaviMarkersSettings(){
        for(int i = 0; i < 4; i++)
            isItemVisiable[i] = true;
    }


    public static final int MARKER_ACCIDENT = 0;
    public static final int MARKER_WORK = 1;
    public static final int MARKER_SPEED_TEST = 2;
    public static final int MARKER_POLICE = 3;


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
                syncTimer.schedule(new MarkersManagerForNavi.SyncTask(), 0, 10000);
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

    private class SyncTask extends TimerTask {
        @Override
        public void run() {
            /*到云上同步数据*/
            Log.i("markerManager","timerTaskOnStart");

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
                        markerDataList.clear();

                        for(Markers marker : list){
                            Log.i("bmob", marker.getM_city_id() + "  type: " + marker.getM_type());
                            Log.i("latlng:", marker.getM_latitude() + "  " + marker.getM_longtitude());

                            if(isItemVisiable[marker.getM_type()]){
                                int resId = R.mipmap.add_markers;
                                switch (marker.getM_type()){
                                    case MARKER_ACCIDENT:
                                        break;
                                    case MARKER_POLICE:
                                        break;
                                    case MARKER_SPEED_TEST:
                                        break;
                                    case MARKER_WORK:
                                        break;
                                    default:
                                        continue;
                                }

                                markerDataList.add(
                                        new BNRouteGuideManager.CustomizedLayerItem(
                                                marker.getM_latitude(),
                                                marker.getM_longtitude(),
                                                BNRoutePlanNode.CoordinateType.BD09LL,
                                                activity.getResources().getDrawable(resId),
                                                BNRouteGuideManager.CustomizedLayerItem.ALIGN_CENTER));
                            }
                        }

                        markerList = list;

                        Log.i("customized",markerDataList.size() + "");
                        //加载自定义图标到地图
                        if(markerDataList.size() != 0){
                            BNRouteGuideManager.getInstance().setCustomizedLayerItems(markerDataList);
                            BNRouteGuideManager.getInstance().showCustomizedLayer(true);
                        }
                    }
                    else{
                        Log.i("bmob",e.toString());
                    }
                }
            });
        }
    }

    /*在ui线程的onResume和 onPause中调用，停止更新*/
    public void onResume(){
        /*10s同步一次marker*/
        Log.i("MarkerManagerForNavi", "onResume");
        this.syncTimer = new Timer("markersSyncTimer2");
        syncTimer.schedule(new MarkersManagerForNavi.SyncTask(),0,10000);
    }

    public void onPause(){
        syncTimer.cancel();
    }
}
