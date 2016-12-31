package com.example.olddriver.map;

import android.util.Log;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.olddriver.bean.LocationList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzp on 2016/11/19.
 */

/*实际测试结果：
        * Suggestionsearch实例检索一次就要重新构建，多次采用同一对象查询无效
        * Suggestionsearch的requestSuggestion方法中，city可以不指定不指定的情况下会智能处理跨市情况
        * 查询给出的结果集没有具体信息，只有坐标，城市，区和uid信息
        * 查询的结果会直接在这里set进传进来的listView*/

public class SuggestionSearch {
    public String keyWord;
    private OnGetSuggestionResultListener listener;

    public SuggestionSearch() {}

    public void search(String keyWord) {
        if (keyWord == null || keyWord.equals("")){
            listener.onGetSuggestionResult(null);
            return;
        }

        this.keyWord = keyWord;

        //在线建议查询测试
        com.baidu.mapapi.search.sug.SuggestionSearch mSuggestionSearch = com.baidu.mapapi.search.sug.SuggestionSearch.newInstance();
        final com.baidu.mapapi.search.sug.OnGetSuggestionResultListener listener =
                new com.baidu.mapapi.search.sug.OnGetSuggestionResultListener() {

            public void onGetSuggestionResult(SuggestionResult res) {
                if (res == null || res.getAllSuggestions() == null) {
                    Log.i("custom", "查询无结果");
                    SuggestionSearch.this.listener.onGetSuggestionResult(null);
                    return;
                }
                //获取在线建议检索结果
                List<SuggestionResult.SuggestionInfo> infos = res.getAllSuggestions();

                List<LocationList> dataList = new ArrayList<>();
                LocationList locationList = null;

                for (SuggestionResult.SuggestionInfo info : infos) {
                    if (info.pt == null)
                        continue;
                    //SuggestionInfo中封装了行政区，所在城市，UID ，坐标信息
                    //PoiSearch poiSearch = PoiSearch.newInstance();
                    locationList = new LocationList();
                    locationList.setCity(info.city);
                    locationList.setDistrict(info.district);
                    locationList.setLocation_address(info.city + info.district);
                    locationList.setLocation_title(info.key);
                    locationList.setTargetLatLng(info.pt);

                    dataList.add(locationList);
                    Log.i("poiSuggestionInfoKey", info.key + "   " + info.pt);
                    Log.i("poiSuggestionDistrict", info.district + "");
                    Log.i("poiSuggestionDescribe", info.describeContents() + "");
                }

                SuggestionSearch.this.listener.onGetSuggestionResult(dataList);
            }
        };

        mSuggestionSearch.setOnGetSuggestionResultListener(listener);


        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                .keyword(keyWord)
                .city(""));

        return;
    }

    //根据keyword 过滤从百度api中返回的数据
    private void dataFiltration(List<PoiInfo> dataList, String keyWord) {
        //数据过滤
    }

    public interface OnGetSuggestionResultListener{
        void onGetSuggestionResult(List<LocationList> dataList);
    }

    public void setListener(OnGetSuggestionResultListener listener){
        this.listener = listener;
    }
}
