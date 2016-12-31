package com.example.olddriver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.olddriver.R;
import com.example.olddriver.bean.TravelBean;

import java.util.List;

/**
 * Created by 苏颂贤 on 2016/12/21.
 */

public class TravelAdapter extends BaseAdapter{
    private List<TravelBean> travelList;
    private LayoutInflater inflater;

    public TravelAdapter(List<TravelBean> travelList, LayoutInflater inflater) {
        this.travelList = travelList;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return travelList.size();
    }

    @Override
    public Object getItem(int i) {
        return travelList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = inflater.inflate(R.layout.travel_list_item,null);
        TextView start = (TextView) v.findViewById(R.id.travel_start);
        TextView end = (TextView) v.findViewById(R.id.travel_end);
        TextView time = (TextView) v.findViewById(R.id.travel_save_time);

        TravelBean travelBean = travelList.get(i);
        start.setText(travelBean.getS_location_title());
        end.setText(travelBean.getE_location_title());
        time.setText(travelBean.getCreatedAt());

        return v;
    }
}
