package com.example.olddriver.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;
import com.example.olddriver.R;
import com.example.olddriver.bean.FavouritePoi;
import com.example.olddriver.bean.Markers;
import com.example.olddriver.bean.SearchResult;
import com.example.olddriver.bean.UserBean;
import com.example.olddriver.map.LocationController;
import com.example.olddriver.map.LocationService;
import com.example.olddriver.map.MarkersManager;
import com.example.olddriver.map.MyGeofenceClient;
import com.example.olddriver.map.Navigator;
import com.example.olddriver.map.RouteLineController;
import com.example.olddriver.map.overlay.PoiMarker;
import com.example.olddriver.util.DataFormater;
import com.example.olddriver.util.DataStore;
import com.example.olddriver.util.FavouriteManager;
import com.example.olddriver.util.FinalValue;
import com.example.olddriver.util.LocationFolder;
import com.example.olddriver.util.MakeHead;
import com.example.olddriver.util.MyTTS;

import java.io.File;
import java.text.ParseException;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener, BaiduMap.OnMapClickListener {
    MapView mMapView = null;
    BaiduMap mBaiduMap = null;
    private int screenWidth;
    private int screenHeight;
    private int lastX;
    private int lastY;
    private  FloatingActionButton marker;
    
    private PopupWindow mPopupWindow;
    private PopupWindow mPopupPoi;
    private PopupWindow mPopupRouteLine;
    private PopupWindow mLoadingPopupWindow;
    private PopupWindow mRecommendPopupWindow;


    private Button btn_user;
    private Button btn_setting;
    private Button btn_out;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private TextView name_show;
    private ImageView head_show;
    private TextView tel_number;
    private Bitmap headpic;

    private TextView tv_search_location;


    //定位，覆盖物相关
    private MarkersManager markersManager;
    private LocationController locationController;
    private Handler handler = new Handler();
    private RouteLineController routeLineController = null;

    //导航相关
    private Navigator navigator = null;
    private Navigator.NavigatorController controller = null;

    //poi相关
    private PoiMarker poiMarker = null;
    private View saveLocation = null;
    private View showRouteLine = null;

    //recommendPopupWindow相关
    private CountDownTimer countDownTimer;
    private TextView markerInfo;
    private TextView markerAddr;
    private TextView markerTrueNum;
    private View denyBtn;
    private View verifyBtn;
    private ImageView imageView;

    //围栏相关
    private MyGeofenceClient geofenceClient = null;


    //popupWindow 相关常量
    private boolean isFirstIn = true;
    private LinearLayout search_title;
    private long mExitTime;

    //开启GPS
    private LinearLayout start_gps;
    private LinearLayout no_gps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);


        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels - 50;

        marker = (FloatingActionButton) findViewById(R.id.button_marker);
        marker.setOnClickListener(this);
        marker.setOnTouchListener(this);

        //设置各种监听
        setListener();

        //弹窗布局
        setMarkerPopupWindow();
        setPoiPopupWindow();
        setLoadingPopupWindow();
        setRouteLinePopupWindow();
        setRecommendPopupWindow();

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启路况图
        mBaiduMap.setTrafficEnabled(true);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(18));

        locationController = new LocationController(this,mBaiduMap);
        markersManager = new MarkersManager(this, mBaiduMap, handler);
        navigator = new Navigator(MainActivity.this,MainActivity.this.handler);

        //搜索框隐藏
        search_title= (LinearLayout) findViewById(R.id.search_title);
        mBaiduMap.setOnMapClickListener(this);


        checkGPS();

        //初始化异步围栏线程
        if(geofenceClient == null){
            geofenceClient = new MyGeofenceClient();
            geofenceClient.setEnterGeofenceListener(new MyGeofenceClient.EnterGeofenceListener() {
                @Override
                public void enterGeofence(final Markers marker, final int distance) {
                    Log.i("enterGeofence" , marker.getM_type() + "  " + distance);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showRecommendPopupWindow(marker, distance);
                        }
                    });
                }
            });

            geofenceClient.onCreate();
        }
    }

    private void init(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_main_drawer);
        navigationView = (NavigationView) findViewById(R.id.nv_main_navigation);  
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        View headerView = navigationView.getHeaderView(0);
        name_show= (TextView) headerView.findViewById(R.id.name_show);
        head_show =(ImageView)headerView.findViewById(R.id.head_show);
        tel_number= (TextView) headerView.findViewById(R.id.tel_num);
        btn_user= (Button) findViewById(R.id.btn_user);
        btn_setting = (Button) findViewById(R.id.settings);
        btn_out = (Button) findViewById(R.id.out);
        btn_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        btn_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BmobUser.logOut();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });

        head_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,UserActivity.class);
                startActivity(intent);
            }
        });

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        item.setChecked(true);
                        Intent intent=null;
                        switch (item.getItemId()) {
                            case R.id.collection:
                                intent = new Intent(MainActivity.this,FavouriteActivity.class);
                                startActivityForResult(intent,0);
                                break;
                            case R.id.information:
                                intent = new Intent(MainActivity.this,UserActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.travel:
                                intent = new Intent(MainActivity.this,TravelActivity.class);
                                startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void setListener() {
        //设置搜索框监听
        tv_search_location = (TextView) findViewById(R.id.search_location);
        tv_search_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //先清理地图状态
                reset();
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.activity_open, R.anim.activity_close);
            }
        });

        //设置重置按钮监听
        View view = findViewById(R.id.search_clear);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });
    }


    //这里处理SearchActivity跳转回来得到的数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == FinalValue.SUCCESS){
            //某些有趣的情况下，数据会null 直接reset一下保持软件的稳定性
            try{
                final SearchResult searchResult = SearchResult.lastSearchResult;

                tv_search_location.setText(searchResult.getKey());

                locationController.setLocationMode(MyLocationConfiguration.LocationMode.NORMAL);
                Toast.makeText(MainActivity.this, searchResult.getKey(), Toast.LENGTH_SHORT).show();

                poiMarker = new PoiMarker(MainActivity.this, mBaiduMap, searchResult.getTargetLatLng());

                //setListener
                View.OnClickListener saveOrGoListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(view.getId() == R.id.locate_save_location){
                            FavouritePoi favouritePoi = new FavouritePoi();
                            favouritePoi.setUserID(BmobUser.getCurrentUser().getObjectId());
                            favouritePoi.setM_address(searchResult.getAddr());
                            favouritePoi.setM_latitude(searchResult.getTargetLatLng().latitude);
                            favouritePoi.setM_longtitude(searchResult.getTargetLatLng().longitude);
                            favouritePoi.setM_tag("我的收藏");
                            favouritePoi.setM_title(searchResult.getKey());
                            FavouriteManager.saveFavouritePoi(favouritePoi, new FavouriteManager.OnSaveResultListener() {
                                @Override
                                public void onSaveResult(boolean isSuccess) {
                                    if (isSuccess == true){
                                        Toast.makeText(MainActivity.this,"收藏成功", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(MainActivity.this, "收藏失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else if(view.getId() == R.id.locate_route_plan){
                            //转显示路径规划的结果
                            poiMarker.remove();
                            mPopupWindow.dismiss();
                            mPopupPoi.dismiss();

                            mPopupRouteLine.setAnimationStyle(R.style.anim_menu_bottombar);
                            mPopupRouteLine.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);

                            onRouteLineDisplay(searchResult);
                        }
                    }
                };

                saveLocation.setOnClickListener(saveOrGoListener);
                showRouteLine.setOnClickListener(saveOrGoListener);


                //setData
                onPoiDataDisplay(searchResult.getKey(),searchResult.getAddr(),searchResult.getDistance());

                poiMarker.setOnMarkerClickListener(new PoiMarker.OnClickListener() {
                    @Override
                    public void onClick() {
                        //转到路径规划
                        poiMarker.remove();
                        mPopupWindow.dismiss();
                        mPopupPoi.dismiss();

                        mPopupRouteLine.setAnimationStyle(R.style.anim_menu_bottombar);
                        mPopupRouteLine.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);

                        onRouteLineDisplay(searchResult);
                    }
                });

                mPopupPoi.setAnimationStyle(R.style.anim_menu_bottombar);
                mPopupPoi.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);

            }catch (Exception e){
                e.printStackTrace();
                reset();
            }
        }
    }

    @Override
    public void onClick(View v) {
        mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
        mPopupWindow.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);

        //加载地址数据
        if(LocationService.lastLocation != null && LocationService.lastLocation.getAddrStr() != null)
            ((TextView)(mPopupWindow.getContentView().findViewById(R.id.textview_location)))
                    .setText(LocationService.lastLocation.getAddrStr());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
            if (mPopupWindow != null && !mPopupWindow.isShowing()) {
                mPopupWindow.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        int action=event.getAction();
        Log.i("xyz", "Touch:"+action);
        switch(action){
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int dx =(int)event.getRawX() - lastX;
                int dy =(int)event.getRawY() - lastY;

                int left = v.getLeft() + dx;
                int top = v.getTop() + dy;
                int right = v.getRight() + dx;
                int bottom = v.getBottom() + dy;
                if(left < 0){
                    left = 0;
                    right = left + v.getWidth();
                }
                if(right > screenWidth){
                    right = screenWidth;
                    left = right - v.getWidth();
                }
                if(top < 0){
                    top = 0;
                    bottom = top + v.getHeight();
                }
                if(bottom > screenHeight){
                    bottom = screenHeight;
                    top = bottom - v.getHeight();
                }
                v.layout(left, top, right, bottom);
                Log.i("xyz", "position:" + left +", " + top + ", " + right + ", " + bottom);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return false;
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
        for (int i = 0;i<5;i++)
            navigationView.getMenu().getItem(i).setChecked(false);
        if(BmobUser.getCurrentUser()!=null) {

            String ObjectID=BmobUser.getCurrentUser().getObjectId();
            BmobQuery<UserBean> bmobQuery = new BmobQuery<UserBean>();
            bmobQuery.getObject(ObjectID, new QueryListener<UserBean>() {
                @Override
                public void done(UserBean object,BmobException e) {
                    if(e==null){
                        UserBean.currentUser = object;
                        headpic = MakeHead.getDiskBitmap(DataStore.USER_PATH + File.separator +BmobUser.getCurrentUser().getUsername()+ File.separator
                                + BmobUser.getCurrentUser().getUsername() +".png");
                        if (headpic == null)
                            head_show.setImageResource(R.mipmap.logo);
                        else
                            head_show.setImageBitmap(headpic);
                        if (object.getTrue_name().equals(""))
                            name_show.setText("老司机");
                        else
                            name_show.setText(object.getTrue_name());


                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if(UserBean.currentUser  != null && UserBean.currentUser.getType() != UserBean.TYPE_TRAVELER){
                                    TextView tv = (TextView) (mPopupRouteLine.getContentView().findViewById(R.id.textview_if_login));
                                    tv.setText(UserBean.currentUser.getTrue_name()+"您好!");
                                    tv = (TextView) (mPopupWindow.getContentView().findViewById(R.id.textview_if_login));
                                    tv.setText(UserBean.currentUser.getTrue_name()+"您好!");
                                    tv = (TextView) (mPopupPoi.getContentView().findViewById(R.id.textview_if_login));
                                    tv.setText(UserBean.currentUser.getTrue_name()+"您好!");
                                }
                            }
                        });
                    }
                }
            });
            tel_number.setText("Tel:"+BmobUser.getCurrentUser().getUsername().toString());
        }
        mMapView.onResume();
        markersManager.onResume();
        locationController.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        markersManager.onPause();
        locationController.stop();
    }

    private void reset(){
        tv_search_location.setText("");

        //reset地图缩放级别
        MapStatus mapStatus = new MapStatus.Builder().zoom(18).build();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
        locationController.setLocationMode(MyLocationConfiguration.LocationMode.COMPASS);

        if(poiMarker != null){
            poiMarker.remove();
            poiMarker = null;
        }

        if(routeLineController != null){
            routeLineController.recycle();
            routeLineController = null;
        }

        if(mPopupPoi != null && mPopupPoi.isShowing())
            mPopupPoi.dismiss();

        if(mPopupRouteLine != null && mPopupRouteLine.isShowing())
            mPopupRouteLine.dismiss();

        if(geofenceClient != null){
            geofenceClient.onStop();
            //geofenceClient = null;
        }

        mPopupWindow.dismiss();
    }

    private void setMarkerPopupWindow() {
        View popupView = null;
        popupView = getLayoutInflater().inflate(R.layout.popupwindow_markers, null);

        View.OnClickListener markerListener = null;

        //设置添加marker按钮的监听
        markerListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"0  " + view.getId() ,Toast.LENGTH_SHORT).show();

                switch (view.getId()){
                    //marker
                    case R.id.traffic_accident:
                        Toast.makeText(MainActivity.this,"1",Toast.LENGTH_SHORT).show();
                        markersManager.addMarker(MarkersManager.MARKER_ACCIDENT);
                        break;
                    case R.id.traffic_photo:
                        Toast.makeText(MainActivity.this,"2",Toast.LENGTH_SHORT).show();
                        markersManager.addMarker(MarkersManager.MARKER_SPEED_TEST);
                        break;
                    case R.id.traffic_police:
                        Toast.makeText(MainActivity.this,"3",Toast.LENGTH_SHORT).show();
                        markersManager.addMarker(MarkersManager.MARKER_POLICE);
                        break;
                    case R.id.traffic_work:
                        Toast.makeText(MainActivity.this,"4",Toast.LENGTH_SHORT).show();
                        markersManager.addMarker(MarkersManager.MARKER_WORK);
                        break;
                }
            }
        };

        popupView.findViewById(R.id.traffic_accident).setOnClickListener(markerListener);
        popupView.findViewById(R.id.traffic_photo).setOnClickListener(markerListener);
        popupView.findViewById(R.id.traffic_police).setOnClickListener(markerListener);
        popupView.findViewById(R.id.traffic_work).setOnClickListener(markerListener);




        if(isFirstIn){
            isFirstIn = false;
            final View finalPopupView = popupView;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(LocationService.lastLocation != null && LocationService.lastLocation.getAddrStr() != null)
                        ((TextView)(finalPopupView.findViewById(R.id.textview_location)))
                                .setText(LocationService.lastLocation.getAddrStr());
                }
            }, 1000);
        }else{
            if(LocationService.lastLocation != null && LocationService.lastLocation.getAddrStr() != null)
                ((TextView)(popupView.findViewById(R.id.textview_location)))
                        .setText(LocationService.lastLocation.getAddrStr());
        }

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

    }

    private void setPoiPopupWindow() {
        View popupView = null;
        popupView = getLayoutInflater().inflate(R.layout.popupwindow_locate, null);
        saveLocation = popupView.findViewById(R.id.locate_save_location);
        showRouteLine = popupView.findViewById(R.id.locate_route_plan);

        mPopupPoi = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT, true);
        mPopupPoi.setTouchable(true);
        mPopupPoi.setFocusable(false);
        mPopupPoi.setOutsideTouchable(false);
        mPopupPoi.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //设置焦点
        mPopupPoi.getContentView().setFocusableInTouchMode(false);
        mPopupPoi.getContentView().setFocusable(false);

    }

    private void setLoadingPopupWindow(){
        //初始化导航事件控制器
        controller = new Navigator.NavigatorController() {
            @Override
            public void getRoutePlanResult(boolean isSuccess) {
                if(isSuccess && mLoadingPopupWindow.isShowing())
                    mLoadingPopupWindow.dismiss();
            }
        };

        //初始化加载popupwindow
        View popupView = getLayoutInflater().inflate(R.layout.popupwindow_loading_anim, null);

        mLoadingPopupWindow = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT, true);
        mLoadingPopupWindow.setTouchable(true);
        mLoadingPopupWindow.setOutsideTouchable(true);
        mLoadingPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //设置焦点
        mLoadingPopupWindow.getContentView().setFocusableInTouchMode(true);
        mLoadingPopupWindow.getContentView().setFocusable(true);

        mLoadingPopupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("well","well");
                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mLoadingPopupWindow != null && mLoadingPopupWindow.isShowing()) {
                        mLoadingPopupWindow.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void setRouteLinePopupWindow() {
        View popupView = null;
        popupView = getLayoutInflater().inflate(R.layout.popupwindow_route, null);

        mPopupRouteLine = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT, true);
        mPopupRouteLine.setTouchable(true);
        mPopupRouteLine.setFocusable(false);
        mPopupRouteLine.setOutsideTouchable(false);
        mPopupRouteLine.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //设置焦点
        mPopupRouteLine.getContentView().setFocusableInTouchMode(false);
        mPopupRouteLine.getContentView().setFocusable(false);
    }


    //构建view参数，构造RouteLineController控制路径显示
    private void onRouteLineDisplay(final SearchResult searchResult){
        //关闭路况图
        mBaiduMap.setTrafficEnabled(false);

        if(routeLineController != null)
            routeLineController.recycle();

        //给导航按钮设置listener
        View v = mPopupRouteLine.getContentView().findViewById(R.id.route_start_navi);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mLoadingPopupWindow.isShowing() == false)
                    mLoadingPopupWindow.showAtLocation(findViewById(R.id.layout_main),Gravity.NO_GRAVITY,0,0);

                navigator.requestNavigation(searchResult, controller);
            }
        });

        //实时位置listener
        v = mPopupRouteLine.getContentView().findViewById(R.id.route_current_location);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapStatus mapStatus = new MapStatus.Builder().zoom(18).build();
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
                locationController.setLocationMode(MyLocationConfiguration.LocationMode.COMPASS);

                //开启路况图
                mBaiduMap.setTrafficEnabled(true);
                mPopupRouteLine.dismiss();

                //启动地理围栏线程
                geofenceClient.onStart();
            }
        });

        routeLineController = new RouteLineController(searchResult, mBaiduMap, mPopupRouteLine.getContentView(), handler);
    }

    //搜索出结果后，显示poi相关结果
    private void onPoiDataDisplay(String key, String placeAddr, String distance){
        TextView textView = (TextView)(mPopupPoi.getContentView().findViewById(R.id.locate_title));
        textView.setText(key);

        textView = (TextView)(mPopupPoi.getContentView().findViewById(R.id.locate_address));
        textView.setText(placeAddr);

        textView = (TextView)(mPopupPoi.getContentView().findViewById(R.id.locate_length));
        textView.setText(distance);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (View.GONE == search_title.getVisibility()) {
            search_title.setVisibility(View.VISIBLE);
            search_title.startAnimation(
                    AnimationUtils.loadAnimation(MainActivity.this, R.anim.top_appear_marker));
        } else {
            search_title.setVisibility(View.GONE);
            search_title.startAnimation(
                    AnimationUtils.loadAnimation(MainActivity.this, R.anim.top_disappear_marker));

        }
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }


    //初始化围栏弹窗的view
    private void setRecommendPopupWindow(){
        //弹窗布局
        View popupView = getLayoutInflater().inflate(R.layout.popupwindow_recommend, null);
        mRecommendPopupWindow = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT, true);
        mRecommendPopupWindow.setTouchable(true);
        mRecommendPopupWindow.setOutsideTouchable(true);
        mRecommendPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //设置焦点
        mRecommendPopupWindow.getContentView().setFocusableInTouchMode(true);
        mRecommendPopupWindow.getContentView().setFocusable(true);
        mRecommendPopupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mRecommendPopupWindow != null && mRecommendPopupWindow.isShowing()) {
                        mRecommendPopupWindow.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });

        markerInfo = (TextView) mRecommendPopupWindow.getContentView().findViewById(R.id.recommend_marker_info);
        markerAddr = (TextView) mRecommendPopupWindow.getContentView().findViewById(R.id.recommend_marker_addr);
        markerTrueNum = (TextView) mRecommendPopupWindow.getContentView().findViewById(R.id.recommend_marker_true_num);
        denyBtn = mRecommendPopupWindow.getContentView().findViewById(R.id.recommend_deny);
        verifyBtn = mRecommendPopupWindow.getContentView().findViewById(R.id.recommend_verify);
        imageView = (ImageView) mRecommendPopupWindow.getContentView().findViewById(R.id.recommend_image);

    }


    private void showRecommendPopupWindow(final Markers marker, int distance){
        if(mRecommendPopupWindow.isShowing())
            mRecommendPopupWindow.dismiss();

        mRecommendPopupWindow.showAtLocation(this.findViewById(R.id.layout_main), Gravity.CENTER, 0, 0);

        if(denyBtn == null){
            Log.i("denyBtn","null");
            return;
        }
        if(verifyBtn == null){
            Log.i("verifyBtn","null");
            return;
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecommendPopupWindow.dismiss();

                if(view.getId() == R.id.recommend_deny){
                    marker.setFalse_num(marker.getFalse_num() + 1);
                }else{
                    marker.setTrue_num(marker.getTrue_num() + 1);
                }
                marker.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if(e == null){
                            Toast.makeText(MainActivity.this,"更新成功", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this,"更新失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        denyBtn.setOnClickListener(listener);
        verifyBtn.setOnClickListener(listener);

        Log.i("enterMarkerTrue", markerTrueNum == null ? "null":"notNull");

        String info = "老司机提醒您前方" + distance + "米路段";
        int imageResId = 0;
        switch (marker.getM_type()){
            case Markers.MARKER_ACCIDENT:
                info += "发生交通事故";
                imageResId = R.mipmap.traffic_accident;
                break;

            case Markers.MARKER_POLICE:
                imageResId = R.mipmap.traffic_police;
                info += "交警正在查车";
                break;

            case Markers.MARKER_SPEED_TEST:
                imageResId = R.mipmap.traffic_photo;
                info += "有监控摄像头";
                break;

            case Markers.MARKER_WORK:
                imageResId = R.mipmap.traffic_work;
                info += "存在路障作业";
                break;

            default:
                break;
        }

        if(countDownTimer != null)
            countDownTimer.cancel();

        //设置倒计时自动关闭mPopupWindow
        countDownTimer = new CountDownTimer(8000,1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mRecommendPopupWindow.isShowing())
                            mRecommendPopupWindow.dismiss();
                    }
                });
            }
        };

        countDownTimer.start();

        markerInfo.setText(info);
        markerAddr.setText(marker.getM_location());
        markerTrueNum.setText(marker.getTrue_num() + "");
        imageView.setBackgroundResource(imageResId);

        TextView tv_time = (TextView) mRecommendPopupWindow.getContentView().findViewById(R.id.recommend_marker_time_num);

        try {
            tv_time.setText(DataFormater.formatTime(marker.getCreatedAt()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        MyTTS.speakText(info);
    }



    //判断一下有没有GPS权限,若没有，引导用户设置
    private void checkGPS(){
        LocationManager locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //判断GPS模块是否开启，如果没有则开启
        if(locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == false){

            View popupView = null;
            popupView = getLayoutInflater().inflate(R.layout.popupwindow_gps_no_work, null);

            final PopupWindow mPopupGPS = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT, true);
            mPopupGPS.setTouchable(true);
            mPopupGPS.setFocusable(false);
            mPopupGPS.setOutsideTouchable(false);
            mPopupGPS.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
            //设置焦点
            mPopupGPS.getContentView().setFocusableInTouchMode(false);
            mPopupGPS.getContentView().setFocusable(false);

            start_gps = (LinearLayout) popupView.findViewById(R.id.btn_start_GPS);
            no_gps= (LinearLayout) popupView.findViewById(R.id.btn_no_GPS);

            start_gps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //转到手机设置界面，用户设置GPS
                    if(mPopupGPS.isShowing())
                        mPopupGPS.dismiss();
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent,0); //设置完成后返回到原来的界面
                }
            });
            no_gps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPopupGPS.dismiss();
                }
            });

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPopupGPS.showAtLocation(MainActivity.this.findViewById(R.id.layout_main), Gravity.CENTER, 0, 0);
                }
            },200);
        }
    }
}
