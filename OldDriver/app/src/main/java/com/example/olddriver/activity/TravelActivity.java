package com.example.olddriver.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.olddriver.R;
import com.example.olddriver.adapter.FavouriteAdapter;
import com.example.olddriver.adapter.TravelAdapter;
import com.example.olddriver.bean.TravelBean;
import com.example.olddriver.custom.MySwipeMenuListView;
import com.example.olddriver.util.FavouriteManager;
import com.example.olddriver.util.TravelManager;

import java.util.List;

public class TravelActivity extends AppCompatActivity {

    private MySwipeMenuListView listView;
    private TravelAdapter travelAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.way_toolbar);
        toolbar.setTitle("我的行程");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        listView= (MySwipeMenuListView) findViewById(R.id.travel_list);

        TravelManager.getTravelData(new TravelManager.OnGetTravelDataListener() {
            @Override
            public void onGetTravelData(final List<TravelBean> dataList) {
                TravelActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        travelAdapter = new TravelAdapter(dataList,TravelActivity.this.getLayoutInflater());
                        listView.setAdapter(travelAdapter);
                    }
                });
            }
        });


        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.parseColor("#FF4500")));
                deleteItem.setWidth(Px2Dp(getApplicationContext(),1000));
                deleteItem.setTitle("删 除");
                deleteItem.setTitleSize(14);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        TravelManager.removeTravelNode((TravelBean) travelAdapter.getItem(position));
                        TravelManager.getTravelData(new TravelManager.OnGetTravelDataListener() {
                            @Override
                            public void onGetTravelData(final List<TravelBean> dataList) {
                                TravelActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        travelAdapter = new TravelAdapter(dataList, TravelActivity.this.getLayoutInflater());
                                        listView.setAdapter(travelAdapter);
                                    }
                                });
                            }
                        });
                        break;
                }
                return false;
            }
        });
    }
    public int Px2Dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    //titlewbar 返回键
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
