package com.projec.itods;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.getbase.floatingactionbutton.FloatingActionsMenu;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private FloatingActionButton friend;
    private FloatingActionsMenu marker;
    private com.getbase.floatingactionbutton.FloatingActionButton police,accident,work,photo;
    private com.getbase.floatingactionbutton.FloatingActionButton syn,rev;

    private ImageView item_out;
    private PopupWindow mPopupItem;
    private View itemView;
    public boolean stateItem=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        OTIDsPopupWindow();

        OTIDsTitle();
        //地图代码
        OTIDsMap();
        //FloatingActionButton代码
        OTIDsFABtn();
        OTIDsItem();

    }

    private void OTIDsItem() {
        item_out= (ImageView)itemView.findViewById(R.id.item_out);
        item_out.setOnClickListener(this);

    }



    private void OTIDsPopupWindow() {
        //弹窗布局
        itemView = getLayoutInflater().inflate(R.layout.popupwindow_item, null);
        mPopupItem = new PopupWindow(itemView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT, true);
        mPopupItem.setTouchable(true);
        mPopupItem.setOutsideTouchable(true);
        mPopupItem.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //设置焦点
        mPopupItem.getContentView().setFocusableInTouchMode(true);
        mPopupItem.getContentView().setFocusable(true);
        mPopupItem.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mPopupItem != null && mPopupItem.isShowing()) {
                        mPopupItem.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });

    }

    private void OTIDsTitle() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_user);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    private void OTIDsFABtn() {
        friend = (FloatingActionButton) findViewById(R.id.friend_button);
        marker= (FloatingActionsMenu) findViewById(R.id.markers_button);
        police=(com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.marker_police);
        accident=(com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.marker_accident);
        work=(com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.marker_work);
        photo=(com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.marker_photo);

        syn=(com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.type_syn);
        rev=(com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.type_rev);

        friend.setOnClickListener(this);
//        marker.setOnClickListener(this);

        police.setOnClickListener(this);
        accident.setOnClickListener(this);
        work.setOnClickListener(this);
        photo.setOnClickListener(this);

        syn.setOnClickListener(this);
        rev.setOnClickListener(this);
    }

    private void OTIDsMap() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        //隐藏+-键
        mMapView.removeViewAt(2);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启路况图
        mBaiduMap.setTrafficEnabled(true);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15));
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(stateItem==true)
            mPopupItem.showAtLocation(this.findViewById(R.id.layout_main), Gravity.CENTER, 0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.item_out:
                stateItem=false;
                mPopupItem.dismiss();
                break;
            case R.id.friend_button:
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                break;
            case R.id.marker_police:
                hideFABMenu();
                break;
            case R.id.marker_accident:
                hideFABMenu();
                break;
            case R.id.marker_work:
                hideFABMenu();
                break;
            case R.id.marker_photo:
                hideFABMenu();
                break;

            case R.id.type_syn:
                showFABMenu();
                marker.collapse();
                break;
            case R.id.type_rev:
                showFABMenu();
                //关闭重启
                marker.collapse();
                break;
            default:
                break;
        }
    }

    private void hideFABMenu() {
        police.setVisibility(View.GONE);
        accident.setVisibility(View.GONE);
        work.setVisibility(View.GONE);
        photo.setVisibility(View.GONE);
        syn.setVisibility(View.VISIBLE);
        rev.setVisibility(View.VISIBLE);
    }
    private void showFABMenu() {
        police.setVisibility(View.VISIBLE);
        accident.setVisibility(View.VISIBLE);
        work.setVisibility(View.VISIBLE);
        photo.setVisibility(View.VISIBLE);
        syn.setVisibility(View.GONE);
        rev.setVisibility(View.GONE);
    }

    //创建菜单栏准备
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            Intent intent=new Intent(MainActivity.this,SearchActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
            if (mPopupItem != null && !mPopupItem.isShowing()) {
                mPopupItem.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM, 0, 0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
