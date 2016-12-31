package com.example.olddriver.map.overlay;

import android.app.Activity;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.example.olddriver.R;
import com.example.olddriver.bean.Markers;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by lzp on 2016/11/30.
 * 管理一组marker的生命周期及其可见性
 */

public class MarkerManager extends OverlayManager {
    private List<Markers> markersList = null;
    private Activity activity;

    /*marker图标*/
    private BitmapDescriptor descriptor;

    public static final int MARKER_ACCIDENT = 0;
    public static final int MARKER_WORK = 1;
    public static final int MARKER_SPEED_TEST = 2;
    public static final int MARKER_POLICE = 3;

    /**
     * 通过一个BaiduMap 对象构造
     *
     * @param baiduMap
     */
    public MarkerManager(Activity activity,BaiduMap baiduMap, int type) {
        super(baiduMap);

        this.activity = activity;

        int resId = 0;

        switch (type){
            case MARKER_ACCIDENT:
                resId = R.mipmap.traffic_accident_for_map;
                break;

            case MARKER_WORK:
                resId = R.mipmap.traffic_work_for_map;
                break;

            case MARKER_SPEED_TEST:
                resId = R.mipmap.traffic_photo_for_map;
                break;

            case MARKER_POLICE:
                resId = R.mipmap.traffic_police_for_map;
                break;

            default:
                break;
        }
        descriptor = BitmapDescriptorFactory.fromResource(resId);
    }

    @Override
    public List<OverlayOptions> getOverlayOptions() {
        if(markersList == null || markersList.size() == 0)
            return null;

        //MarkerOptions()
        /*根据markersList构造一组markerOptions返回*/
        ArrayList<OverlayOptions> optionsList = new ArrayList<>();
        MarkerOptions options = null;
        for(Markers marker : markersList){
            options = new MarkerOptions().
                    position(new LatLng(marker.getM_latitude(), marker.getM_longtitude())).
                    icon(descriptor);

            Log.i("markerOptions", options.toString());

            optionsList.add(options);
        }

        return optionsList;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng latLng = marker.getPosition();
        mBaiduMap.hideInfoWindow();
        InfoWindow infoWindow = null;
        //构造infoWindow

        mBaiduMap.showInfoWindow(infoWindow);
        Log.d("markerManager","点击Marker");
        return true;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }

    public void setMarkersList(List<Markers> list){
        markersList = list;
    }
}
