package com.example.olddriver.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.olddriver.R;
import com.example.olddriver.bean.FavouritePoi;

import java.util.List;

/**
 * Created by 苏颂贤 on 2016/12/18.
 */

public class FavouriteAdapter extends BaseAdapter {
    private List<FavouritePoi> favouritePois;
    private LayoutInflater inflater;

    public FavouriteAdapter(List<FavouritePoi> favouritePois, LayoutInflater inflater) {
        this.favouritePois = favouritePois;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return favouritePois == null? 0 :favouritePois.size();
    }

    @Override
    public Object getItem(int i) {
        return favouritePois.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = null;
        if (convertView != null)
            view = convertView;
        else
            view = inflater.inflate(R.layout.favourite_list_item,null);
        FavouritePoi poi = (FavouritePoi) getItem(i);
        Log.i("mazz",""+poi.getM_longtitude());
        TextView tv_tag = (TextView) view.findViewById(R.id.tv_tag);
        TextView tv_save_time = (TextView) view.findViewById(R.id.tv_save_time);
        TextView tv_addr = (TextView) view.findViewById(R.id.tv_addr);
        if(poi.getM_tag().equals(""))
            tv_tag.setText("我的收藏");
        else
            tv_tag.setText(poi.getM_tag());
        tv_save_time.setText(poi.getCreatedAt());
        tv_addr.setText(poi.getM_address());
        return view;
    }
}
