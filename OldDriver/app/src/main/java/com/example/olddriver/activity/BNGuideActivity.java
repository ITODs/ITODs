package com.example.olddriver.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviBaseCallbackModel;
import com.baidu.navisdk.adapter.BaiduNaviCommonModule;
import com.baidu.navisdk.adapter.NaviModuleFactory;
import com.baidu.navisdk.adapter.NaviModuleImpl;
import com.example.olddriver.R;
import com.example.olddriver.bean.Markers;
import com.example.olddriver.map.LocationService;
import com.example.olddriver.map.MarkersManager;
import com.example.olddriver.map.MarkersManagerForNavi;
import com.example.olddriver.map.MyGeofenceClient;
import com.example.olddriver.map.Navigator;
import com.example.olddriver.util.MyTTS;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;


/**
 * 诱导界面
 *
 * @author sunhao04
 *
 */

public class BNGuideActivity extends Activity {

    private final String TAG = BNGuideActivity.class.getName();
    private BNRoutePlanNode mBNRoutePlanNode = null;
    private BaiduNaviCommonModule mBaiduNaviCommonModule = null;

    private int screenWidth;
    private int screenHeight;
    private int lastX;
    private int lastY;
    private FloatingActionButton marker;

    //提示popupwindow及其相关组件
    private PopupWindow mPopupWindow;
    private TextView markerInfo;
    private TextView markerAddr;
    private TextView markerTrueNum;
    private View denyBtn;
    private View verifyBtn;
    private ImageView imageView;

    private PopupWindow mMarkerPopupWindow;
    private CountDownTimer countDownTimer;

    private MarkersManagerForNavi markersManager;

    //地理围栏
    private MyGeofenceClient geofenceClient = null;
    /*
     * 对于导航模块有两种方式来实现发起导航。 1：使用通用接口来实现 2：使用传统接口来实现
     */
    // 是否使用通用接口
    private boolean useCommonInterface = true;
    private View baiduView = null;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //取消状态栏
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        super.onCreate(savedInstanceState);

        createHandler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        }
        View view = null;
        if (useCommonInterface) {
            //使用通用接口
            mBaiduNaviCommonModule = NaviModuleFactory.getNaviModuleManager().getNaviCommonModule(
                    NaviModuleImpl.BNaviCommonModuleConstants.ROUTE_GUIDE_MODULE, this,
                    BNaviBaseCallbackModel.BNaviBaseCallbackConstants.CALLBACK_ROUTEGUIDE_TYPE, mOnNavigationListener);
            if(mBaiduNaviCommonModule != null) {
                mBaiduNaviCommonModule.onCreate();
                view = mBaiduNaviCommonModule.getView();
            }

        } else {
            //使用传统接口
            view = BNRouteGuideManager.getInstance().onCreate(this,mOnNavigationListener);
        }


        if (view != null) {
            setContentView(R.layout.activity_bnguide);
            baiduView = view;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            RelativeLayout contentLayout = (RelativeLayout) this.findViewById(R.id.content_bnguide);
            contentLayout.addView(baiduView,params);
            //经过测试，子view0是百度地图视图
            //class com.baidu.nplatform.comapi.map.MapGLSurfaceView
            //v.setVisibility(View.GONE);
            //子View1是导航诱导页上除了百度地图视图之外的ViewGroup，实际类型是个FrameLayout
            //v.setVisibility(View.GONE);

            //v5文字诱导信息
            //v5.2文字诱导信息区域   FrameLayout{
            // 5.2.0:未知 class android.widget.RelativeLayout
            //5.2.1:未知 class android.widget.FrameLayout
            //5.2.2:信息框右边的信息显示框，里面有检索到的卫星数，是否开启声音等等的信息。class android.widget.FrameLayout
            // 5.2的类说明5.2的区域仅仅搭载了信息框的底色和5.2.2的信息显示框
            // 5.2的灰黑色背景不少BackgColor来的}
            //v6.RelativeLayout各种设置button
            //v6.0声音+刷新路线 LinearLayout
            //v6.1路况 LinearLayout
            //v6.2退出+Reset  LinearLayout
            //v6.3放大缩小按钮+setting LinearLayout
            //v6.4位置 TextView
            //v7右下角缩略图
        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mBNRoutePlanNode = (BNRoutePlanNode) bundle.getSerializable(Navigator.ROUTE_PLAN_NODE);

                Log.i("routePlanNode", mBNRoutePlanNode.getLatitude() +"  " + mBNRoutePlanNode.getLongitude() + "" + mBNRoutePlanNode.getCoordinateType().toString());
            }

            Log.i("routePlanNode", mBNRoutePlanNode.toString());
        }

        setMarkerButton();

        //init弹窗布局和覆盖物相关的东西
        markersManager = new MarkersManagerForNavi(this);
        initPopupWindow();
        setMarkerPopupWindow();

        //初始化异步围栏线程
        if(geofenceClient == null){
            geofenceClient = new MyGeofenceClient();
            geofenceClient.setEnterGeofenceListener(new MyGeofenceClient.EnterGeofenceListener() {
                @Override
                public void enterGeofence(final Markers marker, final int distance) {
                    Log.i("enterGeofence" , marker.getM_type() + "  " + distance);
                    BNGuideActivity.this.runOnUiThread(new Runnable() {
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

    //设置MarkerBtn相关的参数
    private void setMarkerButton() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels - 50;

        marker = (FloatingActionButton) findViewById(R.id.button_marker);
        marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMarkerPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
                mMarkerPopupWindow.showAtLocation(findViewById(R.id.activity_bnguide), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);

                //加载地址数据
                if(LocationService.lastLocation != null && LocationService.lastLocation.getAddrStr() != null)
                    ((TextView)(mMarkerPopupWindow.getContentView().findViewById(R.id.textview_location)))
                            .setText(LocationService.lastLocation.getAddrStr());
            }
        });
        marker.setOnTouchListener(new View.OnTouchListener() {
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
        });
    }

    //初始化围栏弹窗的view
    private void initPopupWindow(){
        //弹窗布局
        View popupView = getLayoutInflater().inflate(R.layout.popupwindow_recommend, null);
        mPopupWindow = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //设置焦点
        mPopupWindow.getContentView().setFocusableInTouchMode(true);
        mPopupWindow.getContentView().setFocusable(true);
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

        markerInfo = (TextView) mPopupWindow.getContentView().findViewById(R.id.recommend_marker_info);
        markerAddr = (TextView) mPopupWindow.getContentView().findViewById(R.id.recommend_marker_addr);
        markerTrueNum = (TextView) mPopupWindow.getContentView().findViewById(R.id.recommend_marker_true_num);
        denyBtn = mPopupWindow.getContentView().findViewById(R.id.recommend_deny);
        verifyBtn = mPopupWindow.getContentView().findViewById(R.id.recommend_verify);
        imageView = (ImageView) mPopupWindow.getContentView().findViewById(R.id.recommend_image);

    }


    private void showRecommendPopupWindow(final Markers marker, int distance){
        if(mPopupWindow.isShowing())
            mPopupWindow.dismiss();

        mPopupWindow.showAtLocation(this.findViewById(R.id.activity_bnguide), Gravity.CENTER, 0, 0);

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
                mPopupWindow.dismiss();

                if(view.getId() == R.id.recommend_deny){
                    marker.setFalse_num(marker.getFalse_num() + 1);
                }else{
                    marker.setTrue_num(marker.getTrue_num() + 1);
                }
                marker.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if(e == null){
                            Toast.makeText(BNGuideActivity.this,"更新成功", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(BNGuideActivity.this,"更新失败", Toast.LENGTH_SHORT).show();
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
                info += "有交警查车";
                break;

            case Markers.MARKER_SPEED_TEST:
                imageResId = R.mipmap.traffic_photo;
                info += "有测速摄像头";
                break;

            case Markers.MARKER_WORK:
                imageResId = R.mipmap.traffic_work;
                info += "有路障作业";
                break;

            default:
                break;
        }

        if(countDownTimer != null)
            countDownTimer.cancel();

        //设置倒计时自动关闭mPopupWindow
        countDownTimer = new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                BNGuideActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mPopupWindow.isShowing())
                            mPopupWindow.dismiss();
                    }
                });
            }
        };

        countDownTimer.start();

        markerInfo.setText(info);
        markerAddr.setText(marker.getM_location());
        markerTrueNum.setText(marker.getTrue_num() + "");
        imageView.setBackgroundResource(imageResId);

        MyTTS.speakText(info);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(useCommonInterface) {
            if(mBaiduNaviCommonModule != null) {
                mBaiduNaviCommonModule.onResume();
            }
        } else {
            BNRouteGuideManager.getInstance().onResume();
        }


        if (hd != null) {
            hd.sendEmptyMessageAtTime(MSG_SHOW, 2000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(geofenceClient != null)
            geofenceClient.onStart();

        /*if(markersManager != null)
            markersManager.onResume();*/
    }

    protected void onPause() {
        super.onPause();

        if(useCommonInterface) {
            if(mBaiduNaviCommonModule != null) {
                mBaiduNaviCommonModule.onPause();
            }
        } else {
            BNRouteGuideManager.getInstance().onPause();
        }


        if(geofenceClient != null)
            geofenceClient.onStop();

        /*if(markersManager != null)
            markersManager.onPause();*/
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(useCommonInterface) {
            if(mBaiduNaviCommonModule != null) {
                mBaiduNaviCommonModule.onDestroy();
            }
        } else {
            BNRouteGuideManager.getInstance().onDestroy();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(useCommonInterface) {
            if(mBaiduNaviCommonModule != null) {
                mBaiduNaviCommonModule.onStop();
            }
        } else {
            BNRouteGuideManager.getInstance().onStop();
        }

    }

    @Override
    public void onBackPressed() {
        if(useCommonInterface) {
            if(mBaiduNaviCommonModule != null) {
                mBaiduNaviCommonModule.onBackPressed(false);
            }
        } else {
            BNRouteGuideManager.getInstance().onBackPressed(false);
        }
    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(useCommonInterface) {
            if(mBaiduNaviCommonModule != null) {
                mBaiduNaviCommonModule.onConfigurationChanged(newConfig);
            }
        } else {
            BNRouteGuideManager.getInstance().onConfigurationChanged(newConfig);
        }

    };

    private void addCustomizedLayerItems() {
        List<BNRouteGuideManager.CustomizedLayerItem> items = new ArrayList<BNRouteGuideManager.CustomizedLayerItem>();
        BNRouteGuideManager.CustomizedLayerItem item1 = null;
        if (mBNRoutePlanNode != null) {
            item1 = new BNRouteGuideManager.CustomizedLayerItem(LocationService.lastLocation.getLongitude(), LocationService.lastLocation.getLatitude(),
                    BNRoutePlanNode.CoordinateType.BD09LL, getResources().getDrawable(R.mipmap.location),
                    BNRouteGuideManager.CustomizedLayerItem.ALIGN_CENTER);

            Toast.makeText(BNGuideActivity.this, item1.isValid()? "true" : "false", Toast.LENGTH_SHORT).show();
            items.add(item1);

            BNRouteGuideManager.getInstance().setCustomizedLayerItems(items);


            Log.i("setItem","well");
        }
        BNRouteGuideManager.getInstance().showCustomizedLayer(true);
    }

    private static final int MSG_SHOW = 1;
    private static final int MSG_HIDE = 2;
    private static final int MSG_RESET_NODE = 3;
    private Handler hd = null;

    private void createHandler() {
        if (hd == null) {
            hd = new Handler(getMainLooper()) {
                public void handleMessage(android.os.Message msg) {
                    if (msg.what == MSG_SHOW) {
                        addCustomizedLayerItems();
                    } else if (msg.what == MSG_HIDE) {
                        //BNRouteGuideManager.getInstance().showCustomizedLayer(false);
                    } else if (msg.what == MSG_RESET_NODE) {
                        LatLng latLng = Navigator.targetLocation.getTargetLatLng();
                        BNRouteGuideManager.getInstance().resetEndNodeInNavi(
                                new BNRoutePlanNode(latLng.latitude,latLng.longitude,Navigator.targetLocation.getLocation_title(), null, BNRoutePlanNode.CoordinateType.BD09LL));
                    }
                };
            };
        }
    }

    private BNRouteGuideManager.OnNavigationListener mOnNavigationListener = new BNRouteGuideManager.OnNavigationListener() {

        @Override
        public void onNaviGuideEnd() {
            finish();
        }

        @Override
        public void notifyOtherAction(int actionType, int arg1, int arg2, Object obj) {
            if (actionType == 0) {
                Log.i(TAG, "notifyOtherAction actionType = " + actionType + ",导航到达目的地！");
            }

            Log.i(TAG, "actionType:" + actionType + "arg1:" + arg1 + "arg2:" + arg2 + "obj:" + obj.toString());
        }

    };


    private void setMarkerPopupWindow() {
        View popupView = null;
        popupView = getLayoutInflater().inflate(R.layout.popupwindow_markers, null);

        View.OnClickListener markerListener = null;

        //设置添加marker按钮的监听
        markerListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(BNGuideActivity.this,"0  " + view.getId() ,Toast.LENGTH_SHORT).show();

                switch (view.getId()){
                    //marker
                    case R.id.traffic_accident:
                        Toast.makeText(BNGuideActivity.this,"1",Toast.LENGTH_SHORT).show();
                        markersManager.addMarker(MarkersManager.MARKER_ACCIDENT);
                        break;
                    case R.id.traffic_photo:
                        Toast.makeText(BNGuideActivity.this,"2",Toast.LENGTH_SHORT).show();
                        markersManager.addMarker(MarkersManager.MARKER_SPEED_TEST);
                        break;
                    case R.id.traffic_police:
                        Toast.makeText(BNGuideActivity.this,"3",Toast.LENGTH_SHORT).show();
                        markersManager.addMarker(MarkersManager.MARKER_POLICE);
                        break;
                    case R.id.traffic_work:
                        Toast.makeText(BNGuideActivity.this,"4",Toast.LENGTH_SHORT).show();
                        markersManager.addMarker(MarkersManager.MARKER_WORK);
                        break;
                }
            }
        };

        popupView.findViewById(R.id.traffic_accident).setOnClickListener(markerListener);
        popupView.findViewById(R.id.traffic_photo).setOnClickListener(markerListener);
        popupView.findViewById(R.id.traffic_police).setOnClickListener(markerListener);
        popupView.findViewById(R.id.traffic_work).setOnClickListener(markerListener);

        if(LocationService.lastLocation != null && LocationService.lastLocation.getAddrStr() != null)
            ((TextView)(popupView.findViewById(R.id.textview_location)))
                    .setText(LocationService.lastLocation.getAddrStr());

        mMarkerPopupWindow = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT, true);
        mMarkerPopupWindow.setTouchable(true);
        mMarkerPopupWindow.setOutsideTouchable(true);
        mMarkerPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //设置焦点
        mMarkerPopupWindow.getContentView().setFocusableInTouchMode(true);
        mMarkerPopupWindow.getContentView().setFocusable(true);

        //设置键盘监听
        mMarkerPopupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mMarkerPopupWindow != null && mMarkerPopupWindow.isShowing()) {
                        mMarkerPopupWindow.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
    }
}