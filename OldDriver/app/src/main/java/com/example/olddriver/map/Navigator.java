package com.example.olddriver.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.example.olddriver.activity.BNGuideActivity;
import com.example.olddriver.bean.LocationList;
import com.example.olddriver.bean.SearchResult;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType.BD09LL;

/**
 * Created by lzp on 2016/12/6.
 */

public class Navigator {
    private Activity activity = null;
    private Handler ttsHandler = new Handler();
    public static LocationList targetLocation = null;

    public static final String ROUTE_PLAN_NODE = "route_plan_node";

    public Navigator(final Activity activity, Handler ttsHandler){
        this.activity = activity;
        this.ttsHandler = ttsHandler;

        initNavigator(null);
    }

    public void initNavigator(final Runnable r){
        BaiduNaviManager.getInstance().init(activity, Environment.getExternalStorageDirectory().getAbsolutePath(), "OldDriver",
                new BaiduNaviManager.NaviInitListener() {
                    @Override
                    public void onAuthResult(int i, String s) {
                    }

                    @Override
                    public void initStart() {
                    }

                    @Override
                    public void initSuccess() {
                        Log.i("navigator", "初始化成功！");
                        if(r != null)
                            r.run();
                    }

                    @Override
                    public void initFailed() {
                        Log.i("navi", "initFailed！");
                    }
                }, null, ttsHandler,null);
    }

    public void requestNavigation(final SearchResult searchResult, final NavigatorController controller){
        if(BaiduNaviManager.isNaviInited() == false){
            initNavigator(new Runnable() {
                @Override
                public void run() {
                    startNavigation(searchResult, controller);
                }
            });
        }else{
            startNavigation(searchResult, controller);
        }

    }

    private void startNavigation(SearchResult searchResult, final NavigatorController controller) {
        NavigatorSettings.initNaviSettings();

        this.targetLocation = new LocationList();

        targetLocation.setTargetLatLng(searchResult.getTargetLatLng());
        targetLocation.setLocation_title(searchResult.getKey());
        targetLocation.setLocation_address(searchResult.getAddr());

        List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
        final BNRoutePlanNode sNode =
                new BNRoutePlanNode(LocationService.lastLocation.getLongitude(),
                        LocationService.lastLocation.getLatitude(),
                        "",null, BD09LL);

        BNRoutePlanNode eNode = new BNRoutePlanNode(targetLocation.getTargetLatLng().longitude,
                targetLocation.getTargetLatLng().latitude,
                targetLocation.getLocation_title(), null, BD09LL);

        //final BNRoutePlanNode sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", null, BD09LL);
        //BNRoutePlanNode eNode = new BNRoutePlanNode(116.40386525193937, 39.915160800132085, "北京天安门", null, BD09LL);

        Log.i("naviSnode",sNode.getLatitude()+ "   " +sNode.getLongitude());
        Log.i("naviSnode",eNode.getLatitude()+ "   " +eNode.getLongitude());

        list.add(sNode);
        list.add(eNode);

        BaiduNaviManager
                .getInstance()
                .launchNavigator(
                        activity,                            //建议是应用的主Activity
                        list,                            //传入的算路节点，顺序是起点、途经点、终点，其中途经点最多三个
                        NavigatorSettings.ROUTE_PLAN_MOD,                                //算路偏好 1:推荐 8:少收费 2:高速优先 4:少走高速 16:躲避拥堵
                        true,                            //true表示真实GPS导航，false表示模拟导航
                        new BaiduNaviManager.RoutePlanListener() {
                            @Override
                            public void onJumpToNavigator() {
                                if(controller.isCancel == true)
                                    return;

                                Log.i("navi", "算路成功！");
                                controller.getRoutePlanResult(true);

                                Intent intent = new Intent(activity, BNGuideActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(ROUTE_PLAN_NODE, sNode);
                                intent.putExtras(bundle);
                                activity.startActivity(intent);
                            }

                            @Override
                            public void onRoutePlanFailed() {
                                if(controller.isCancel == true)
                                    return;

                                Log.i("navi", "算路失败！");
                                controller.getRoutePlanResult(true);
                                Toast.makeText(activity, "getRouteFailed",Toast.LENGTH_SHORT).show();
                            }
                        }
                        //开始导航回调监听器，在该监听器里一般是进入导航过程页面
                );
    }

    public static abstract class NavigatorController{
        private boolean isCancel = false;

        public abstract void getRoutePlanResult(boolean isSuccess);

        void cancel(){
            isCancel = true;
        }
    }
}
