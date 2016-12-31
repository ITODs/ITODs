package com.example.olddriver.bean;

import com.baidu.mapapi.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by BinZ on 2016/12/12.
 */

public class LocationList implements Serializable{
    private String location_title;
    private String location_address;
    private String city;
    private LatLng targetLatLng;
    private String district;
    private String type = "";

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LatLng getTargetLatLng() {
        return targetLatLng;
    }

    public void setTargetLatLng(LatLng targetLatLng) {
        this.targetLatLng = targetLatLng;
    }


    public String getLocation_address() {
        return location_address;
    }

    public void setLocation_address(String location_address) {
        this.location_address = location_address;
    }

    public String getLocation_title() {
        return location_title;
    }

    public void setLocation_title(String location_title) {
        this.location_title = location_title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        HashMap<String,String> data = new HashMap<>();
        data.put("title",location_title);
        data.put("address",location_address);
        data.put("city",city);
        data.put("lat",targetLatLng.latitude+"");
        data.put("lng",targetLatLng.longitude+"");
        data.put("district",district);
        data.put("type",type);
        out.writeObject(data);
    }
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException{
        HashMap<String,String> data = (HashMap<String, String>) in.readObject();
        location_title = data.get("title");
        location_address = data.get("address");
        city = data.get("city");
        try{
            targetLatLng = new LatLng(Double.valueOf(data.get("lat")),Double.valueOf(data.get("lng")));
        }catch (NumberFormatException e){
            targetLatLng = null;
            e.printStackTrace();
        }

        district = data.get("district");
        type = data.get("type");
    }
}
