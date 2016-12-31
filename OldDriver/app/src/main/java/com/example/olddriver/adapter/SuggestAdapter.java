package com.example.olddriver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.olddriver.R;
import com.example.olddriver.bean.LocationList;

import java.util.List;

/**
 * Created by BinZ on 2016/12/12.
 */

public class SuggestAdapter extends BaseAdapter{
    private List<LocationList> locations;
    private LayoutInflater inflater;


    public SuggestAdapter(List<LocationList> locations, Context context) {
        this.locations = locations;
        this.inflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return locations==null?0:locations.size();
    }

    @Override
    public Object getItem(int position) {
        return locations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //加载布局为一个视图
        View view=inflater.inflate(R.layout.item_suggestionlist,null);
        LocationList locationList = (LocationList) getItem(position);
        //在view视图中查找id为image_photo的控件
        TextView tv_title= (TextView) view.findViewById(R.id.suggest_title);
        TextView tv_address= (TextView) view.findViewById(R.id.suggest_address);
        tv_title.setText(locationList.getLocation_title());
        tv_address.setText(locationList.getLocation_address());
        return view;
    }
}
