package com.example.olddriver.util;

import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.example.olddriver.bean.LocationList;
import com.example.olddriver.bean.TravelBean;
import com.example.olddriver.map.LocationService;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by lzp on 2016/12/20.
 */

public class TravelManager {

    public static void getTravelData(final OnGetTravelDataListener listener){
        if (BmobUser.getCurrentUser() == null) {
            Log.i("travelBean","没登陆，数据拉取失败");
            return;
        }
        BmobQuery<TravelBean> query = new BmobQuery<>("TravelBean");
        query.addWhereEqualTo("user_id", BmobUser.getCurrentUser().getObjectId());
        query.setLimit(100000);
        query.findObjects(new FindListener<TravelBean>() {
            @Override
            public void done(List<TravelBean> list, BmobException e) {
                if (e == null){
                    listener.onGetTravelData(list);
                    Log.i("travelBean","获取成功");
                }else{
                    Log.i("travelBean","获取失败");
                }
            }
        });
    }

    public interface OnGetTravelDataListener{
        void onGetTravelData(List<TravelBean> dataList);
    }

    public static void addTravelNode(LocationList data){
        final TravelBean travelBean = new TravelBean();
        //检查数据的完整性
        if(data.getLocation_title() == null ||
                data.getLocation_address() == null ||
                data.getTargetLatLng() == null ||
                BmobUser.getCurrentUser() == null){

            Log.i("travelBean", "数据不完整，保存失败");
            return;
        }

        travelBean.setUser_id(BmobUser.getCurrentUser().getObjectId());
        travelBean.setS_city(LocationService.lastLocation.getCity());
        travelBean.setS_location_address(LocationService.lastLocation.getAddrStr());
        travelBean.setS_targetLatLng(new LatLng(LocationService.lastLocation.getLatitude(),
                LocationService.lastLocation.getLongitude()));
        if(LocationService.lastLocation.getPoiList() != null
                && LocationService.lastLocation.getPoiList().get(0) != null){
            travelBean.setS_location_title(LocationService.lastLocation.getPoiList().get(0).getName());
        }
        if(travelBean.getS_location_title() == null || travelBean.getS_location_title().equals("")){
            travelBean.setS_location_title(LocationService.lastLocation.getAddrStr());
        }
        travelBean.setE_city(data.getCity());
        travelBean.setE_location_address(data.getLocation_address());
        travelBean.setE_location_title(data.getLocation_title());
        travelBean.setE_targetLatLng(data.getTargetLatLng());

        travelBean.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e == null){
                    Log.i("travelBean", "保存成功");
                }else{
                    Log.i("travelBean", "保存失败");
                }
            }
        });

    }
    public static void removeTravelNode(TravelBean bean){
        bean.delete(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    Log.i("TravelBean", "删除成功");
                }else{
                    Log.i("TravelBean", "删除失败  " + e.toString());
                }
            }
        });
    }

}
