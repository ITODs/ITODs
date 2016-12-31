package com.example.olddriver.bean;

import com.baidu.mapapi.model.LatLng;

import cn.bmob.v3.BmobObject;

/**
 * Created by 苏颂贤 on 2016/12/20.
 */

public class TravelBean extends BmobObject{
    private String s_location_title;
    private String s_location_address;
    private String s_city;
    private LatLng s_targetLatLng;
    private String e_location_title;
    private String e_location_address;
    private String e_city;
    private LatLng e_targetLatLng;
    private String user_id;

    public String getS_location_title() {
        return s_location_title;
    }

    public void setS_location_title(String s_location_title) {
        this.s_location_title = s_location_title;
    }

    public String getS_location_address() {
        return s_location_address;
    }

    public void setS_location_address(String s_location_address) {
        this.s_location_address = s_location_address;
    }

    public String getS_city() {
        return s_city;
    }

    public void setS_city(String s_city) {
        this.s_city = s_city;
    }

    public LatLng getS_targetLatLng() {
        return s_targetLatLng;
    }

    public void setS_targetLatLng(LatLng s_targetLatLng) {
        this.s_targetLatLng = s_targetLatLng;
    }

    public String getE_location_title() {
        return e_location_title;
    }

    public void setE_location_title(String e_location_title) {
        this.e_location_title = e_location_title;
    }

    public String getE_location_address() {
        return e_location_address;
    }

    public void setE_location_address(String e_location_address) {
        this.e_location_address = e_location_address;
    }

    public String getE_city() {
        return e_city;
    }

    public void setE_city(String e_city) {
        this.e_city = e_city;
    }

    public LatLng getE_targetLatLng() {
        return e_targetLatLng;
    }

    public void setE_targetLatLng(LatLng e_targetLatLng) {
        this.e_targetLatLng = e_targetLatLng;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

}
