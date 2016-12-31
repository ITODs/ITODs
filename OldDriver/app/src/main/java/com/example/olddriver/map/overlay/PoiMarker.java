package com.example.olddriver.map.overlay;

import android.app.Activity;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.olddriver.R;

/**
 * Created by lzp on 2016/12/9.
 */

public class PoiMarker{
    private Marker marker;
    private BaiduMap map;
    private BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.location_navigation);

    public PoiMarker(final Activity activity, final BaiduMap map, LatLng latLng){
        this.map = map;
        MarkerOptions option = new MarkerOptions();
        option.position(latLng);
        option.title("");
        option.icon(descriptor);
        marker = (Marker) map.addOverlay(option);


        //设置地图中心点位置
        // 设置地图的缩放级别
        MapStatus mapStatus = new MapStatus.Builder().target(latLng).zoom(16).build();
        map.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }

    public void remove(){
        marker.remove();
    }

    public interface OnClickListener{
        void onClick();
    }

    public void setOnMarkerClickListener(final OnClickListener listener){
        map.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.equals(PoiMarker.this.marker)){
                    listener.onClick();
                    return true;
                }
                return false;
            }
        });
    }
}
