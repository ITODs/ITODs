package com.example.olddriver.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by lzp on 2016/11/30.
 */

public class UserBean extends BmobUser {
    private String wallet_id;
    private double u_longtitude;
    private double u_latitude;
    private String u_location;
    private int u_city_id;
    private String head_path;
    private String true_name;
    private int type;
    private String city_name;
    public static final int TYPE_TRAVELER = 1;
    public static final int TYPE_REGISTED_USER = 0;

    public int getType() {
        return type;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static UserBean currentUser = null;

    public String getHead_path() {
        return head_path;
    }

    public void setHead_path(String head_path) {
        this.head_path = head_path;
    }

    public String getTrue_name() {
        return true_name;
    }

    public void setTrue_name(String true_name) {
        this.true_name = true_name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    private String sex;
    public int getU_city_id() {
        return u_city_id;
    }

    public void setU_city_id(int u_city_id) {
        this.u_city_id = u_city_id;
    }

    public double getU_latitude() {
        return u_latitude;
    }

    public void setU_latitude(double u_latitude) {
        this.u_latitude = u_latitude;
    }

    public String getU_location() {
        return u_location;
    }

    public void setU_location(String u_location) {
        this.u_location = u_location;
    }

    public double getU_longtitude() {
        return u_longtitude;
    }

    public void setU_longtitude(double u_longtitude) {
        this.u_longtitude = u_longtitude;
    }

    public String getWallet_id() {
        return wallet_id;
    }

    public void setWallet_id(String wallet_id) {
        this.wallet_id = wallet_id;
    }
}
