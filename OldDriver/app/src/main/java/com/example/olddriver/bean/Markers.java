package com.example.olddriver.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by lzp on 2016/11/30.
 */

public class Markers extends BmobObject {
    private String user_id;
    private int true_num;
    private int false_num;
    private int m_type;
    private double m_longtitude;
    private double m_latitude;
    private String m_location;
    private int m_city_id;


    public static final int MARKER_ACCIDENT = 0;
    public static final int MARKER_WORK = 1;
    public static final int MARKER_SPEED_TEST = 2;
    public static final int MARKER_POLICE = 3;



    public int getFalse_num() {
        return false_num;
    }

    public void setFalse_num(int false_num) {
        this.false_num = false_num;
    }

    public int getM_city_id() {
        return m_city_id;
    }

    public void setM_city_id(int m_city_id) {
        this.m_city_id = m_city_id;
    }

    public double getM_latitude() {
        return m_latitude;
    }

    public void setM_latitude(double m_latitude) {
        this.m_latitude = m_latitude;
    }

    public String getM_location() {
        return m_location;
    }

    public void setM_location(String m_location) {
        this.m_location = m_location;
    }

    public double getM_longtitude() {
        return m_longtitude;
    }

    public void setM_longtitude(double m_longtitude) {
        this.m_longtitude = m_longtitude;
    }

    public int getM_type() {
        return m_type;
    }

    public void setM_type(int m_type) {
        this.m_type = m_type;
    }

    public int getTrue_num() {
        return true_num;
    }

    public void setTrue_num(int true_num) {
        this.true_num = true_num;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
