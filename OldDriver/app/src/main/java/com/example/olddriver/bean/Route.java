package com.example.olddriver.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by lzp on 2016/11/30.
 */

public class Route extends BmobObject {
    private String user_id;
    private String to;
    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
