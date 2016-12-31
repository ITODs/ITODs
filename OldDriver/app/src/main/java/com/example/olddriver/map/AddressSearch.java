package com.example.olddriver.map;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

/**
 * Created by 苏颂贤 on 2016/12/13.
 */

public class AddressSearch {
    private GeoCoder mSearch;
    public AddressSearch(){mSearch=null;}
    public interface OnGetAddressListener{
        void onGetAddress(String address);
    }
    public void setOnGetAddressListener(final OnGetAddressListener listener){
        OnGetGeoCoderResultListener geoListener = new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                listener.onGetAddress(reverseGeoCodeResult.getAddress());

                mSearch.destroy();
            }
        };

        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(geoListener);
    }

    public void getAdress(LatLng lating){
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(lating));
    }

}
