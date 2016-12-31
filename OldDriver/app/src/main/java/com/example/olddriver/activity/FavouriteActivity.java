package com.example.olddriver.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.olddriver.R;
import com.example.olddriver.adapter.FavouriteAdapter;
import com.example.olddriver.bean.FavouritePoi;
import com.example.olddriver.custom.MySwipeMenuListView;
import com.example.olddriver.util.FavouriteManager;
import com.example.olddriver.util.FinalValue;

import java.util.List;

public class FavouriteActivity extends AppCompatActivity {

    private PopupWindow mPopupWindow;
    private MySwipeMenuListView listView;
    private View.OnClickListener favouriteListener;
    private View.OnClickListener customerListener;
    private FavouriteAdapter favouriteAdapter;
    private EditText editText_tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        toolbar.setTitle("个人收藏点");
        toolbar.setTitleTextColor(Color.parseColor("#1E90FF"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        listView= (MySwipeMenuListView) findViewById(R.id.favorite_list);
//        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.add_favourite);
//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        FavouriteManager.getFavouriteData(new FavouriteManager.OnGetFavouriteListListener() {
            @Override
            public void onGetFavouriteList(final List<FavouritePoi> dataList) {
                FavouriteActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        favouriteAdapter = new FavouriteAdapter(dataList,FavouriteActivity.this.getLayoutInflater());
                        listView.setAdapter(favouriteAdapter);
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FavouritePoi poi = (FavouritePoi)listView.getAdapter().getItem(i);
                Bundle bundle = new Bundle();
                bundle.putSerializable("favouritePoi",poi);
                Intent intent = new Intent(FavouriteActivity.this, SearchActivity.class);
                intent.putExtras(bundle);
                FavouriteActivity.this.startActivityForResult(intent,0);
            }
        });
        customerListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.cancel:
                        mPopupWindow.dismiss();
                        break;
                    case R.id.save:
                        String tag = editText_tag.getText().toString().trim();
                        if(!tag.equals(""))
                            FavouriteManager.setTag(tag);
                        mPopupWindow.dismiss();
                        favouriteAdapter = new FavouriteAdapter(FavouriteManager.getPoiList(),FavouriteActivity.this.getLayoutInflater());
                        listView.setAdapter(favouriteAdapter);
                        break;
                }
            }
        };

        favouriteListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.home:
                        FavouriteManager.setTag("我的家");
                        mPopupWindow.dismiss();
                        favouriteAdapter = new FavouriteAdapter(FavouriteManager.getPoiList(),FavouriteActivity.this.getLayoutInflater());
                        listView.setAdapter(favouriteAdapter);
                        break;
                    case R.id.company:
                        FavouriteManager.setTag("我的公司");
                        mPopupWindow.dismiss();
                        favouriteAdapter = new FavouriteAdapter(FavouriteManager.getPoiList(),FavouriteActivity.this.getLayoutInflater());
                        listView.setAdapter(favouriteAdapter);
                        break;
                    case R.id.customer:
                        mPopupWindow.dismiss();
                        View popupView = getLayoutInflater().inflate(R.layout.popupwindow_favourite_customer, null);
                        popupView.findViewById(R.id.cancel).setOnClickListener(customerListener);
                        popupView.findViewById(R.id.save).setOnClickListener(customerListener);
                        editText_tag = (EditText) popupView.findViewById(R.id.editText_tag);
                        mPopupWindow = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT, true);
                        mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
                        mPopupWindow.showAtLocation(findViewById(R.id.activity_favourite), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
                        break;
                    default:
                        break;
                }
            }
        };
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem settings = new SwipeMenuItem(getApplicationContext());
                settings.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                settings.setWidth(Px2Dp(getApplicationContext(),720));
                // set item title
                settings.setTitle("设为");
                // set item title fontsize
                settings.setTitleSize(16);
                // set item title font color
                settings.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(settings);

                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setWidth(Px2Dp(getApplicationContext(),720));
                deleteItem.setTitle("删除");
                deleteItem.setTitleSize(16);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };

// set creator
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        View popupView = getLayoutInflater().inflate(R.layout.popupwindow_favourite, null);
                        popupView.findViewById(R.id.home).setOnClickListener(favouriteListener);
                        popupView.findViewById(R.id.company).setOnClickListener(favouriteListener);
                        popupView.findViewById(R.id.customer).setOnClickListener(favouriteListener);
                        FavouriteManager.setSelectedPoi(position);
                        mPopupWindow = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT, true);
                        mPopupWindow.setTouchable(true);
                        mPopupWindow.setOutsideTouchable(true);
                        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
                        //设置焦点
                        mPopupWindow.getContentView().setFocusableInTouchMode(true);
                        mPopupWindow.getContentView().setFocusable(true);
                        //设置键盘监听
                        mPopupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                                    if (mPopupWindow != null && mPopupWindow.isShowing()) {
                                        mPopupWindow.dismiss();
                                    }
                                    return true;
                                }
                                return false;
                            }
                        });
                        mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
                        mPopupWindow.showAtLocation(findViewById(R.id.activity_favourite), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
                        break;
                    case 1:
                        FavouriteManager.remove(position);
                        favouriteAdapter = new FavouriteAdapter(FavouriteManager.getPoiList(),FavouriteActivity.this.getLayoutInflater());
                        listView.setAdapter(favouriteAdapter);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
            if (mPopupWindow != null && !mPopupWindow.isShowing()) {
                mPopupWindow.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == FinalValue.SUCCESS){
            this.setResult(FinalValue.SUCCESS);
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
