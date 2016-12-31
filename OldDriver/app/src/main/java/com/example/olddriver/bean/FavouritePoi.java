package com.example.olddriver.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by 苏颂贤 on 2016/12/18.
 */

public class FavouritePoi extends BmobObject {
    private double m_longtitude;
    private double m_latitude;
    private String m_tag;
    private String m_address;
    private String m_title;
    private String userID;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public double getM_longtitude() {
        return m_longtitude;
    }

    public void setM_longtitude(double m_longtitude) {
        this.m_longtitude = m_longtitude;
    }

    public double getM_latitude() {
        return m_latitude;
    }

    public void setM_latitude(double m_latitude) {
        this.m_latitude = m_latitude;
    }

    public String getM_tag() {
        return m_tag;
    }

    public void setM_tag(String m_tag) {
        this.m_tag = m_tag;
    }

    public String getM_address() {
        return m_address;
    }

    public void setM_address(String m_address) {
        this.m_address = m_address;
    }

    public String getM_title() {
        return m_title;
    }

    public void setM_title(String m_title) {
        this.m_title = m_title;
    }

    @Override
    public boolean equals(Object o) {
        FavouritePoi poi = (FavouritePoi)o;
        if(poi.getM_latitude() == this.getM_latitude() &&
                poi.getM_longtitude() == this.getM_longtitude())
            return true;
        else
            return false;
    }


}
