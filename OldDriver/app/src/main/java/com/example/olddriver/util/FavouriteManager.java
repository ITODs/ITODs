package com.example.olddriver.util;

import android.util.Log;
import android.widget.Toast;

import com.example.olddriver.bean.FavouritePoi;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by 苏颂贤 on 2016/12/18.
 */

public class FavouriteManager {
    public static final String MY_HOME = "我的家";
    public static final String MY_COMPANY = "我的公司";

    private static FavouriteManager manager = null;

    public static List<FavouritePoi> getPoiList() {
        return poiList;
    }

    private static List<FavouritePoi> poiList = null;

    public FavouriteManager getInstance(){
        return manager;
    }

    public static void init(){
        manager = new FavouriteManager();
        poiList = new ArrayList<>();
        BmobQuery<FavouritePoi> query = new BmobQuery<>("FavouritePoi");
        query.addWhereEqualTo("userID", BmobUser.getCurrentUser().getObjectId());
        query.setLimit(100000);
        query.findObjects(new FindListener<FavouritePoi>() {
            @Override
            public void done(List<FavouritePoi> list, BmobException e) {
                if(e == null)
                    poiList = list;
                Log.i("mazz",list.size()+"");
            }
        });
    }

    public static void saveFavouritePoi(final FavouritePoi poi, final OnSaveResultListener listener){
        if(manager == null) {
            manager = new FavouriteManager();
            poiList = new ArrayList<>();
        }
        poi.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e == null){
                    poiList.add(poi);
                    listener.onSaveResult(true);
                }else{
                    listener.onSaveResult(false);                    }
            }
        });
    }
    public static void remove(int index){
        if(poiList != null){
            poiList.get(index).delete();
            poiList.remove(index);
        }
    }
    public static void remove(FavouritePoi poi){
        if(poiList != null){
            if(poiList.contains(poi)){
                poiList.remove(poi);
                poi.delete(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null)
                            Toast.makeText(App.getAppContext(), "删除成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    public static void setTag(final String tag){
        FavouritePoi poi = selectedPoi;
        for (FavouritePoi favouritePoi : poiList){
            if(favouritePoi.getM_tag().equals(tag)){
                Toast.makeText(App.getAppContext(),tag+"已存在，请设置其他标签", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        poi.setM_tag(tag);
        poi.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null){
                    Toast.makeText(App.getAppContext(), "设置成功", Toast.LENGTH_SHORT).show();
                    FavouriteManager.getSelectedPoi().setM_tag(tag);
                } else{
                    Toast.makeText(App.getAppContext(), "设置失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public static FavouritePoi getPoiByTag(String tag) {
        FavouritePoi poi = null;
        for (FavouritePoi favouritePoi : poiList){
            if(favouritePoi.getM_tag().equals(tag)){
                poi = favouritePoi;
                break;
            }
        }
        return poi;
    }


    private static FavouritePoi selectedPoi = null;

    public static FavouritePoi getSelectedPoi() {
        return selectedPoi;
    }

    public static void setSelectedPoi(int index) {
        FavouriteManager.selectedPoi = poiList.get(index);
    }
    public interface OnSaveResultListener{
        void onSaveResult(boolean isSuccess);
    }

    public interface OnGetFavouriteListListener{
        void onGetFavouriteList(List<FavouritePoi> dataList);
    }

    public static void getFavouriteData(final OnGetFavouriteListListener listListener){
        BmobQuery<FavouritePoi> query = new BmobQuery<>("FavouritePoi");
        query.addWhereEqualTo("userID", BmobUser.getCurrentUser().getObjectId());
        query.setLimit(100000);
        query.findObjects(new FindListener<FavouritePoi>() {
            @Override
            public void done(List<FavouritePoi> list, BmobException e) {
                if(e == null){
                    poiList = list;
                    listListener.onGetFavouriteList(list);
                    Log.i("FavouriteManager", "ok");
                }else{
                    Log.i("FavouriteManager", e.toString());
                }
            }
        });
    }
}
