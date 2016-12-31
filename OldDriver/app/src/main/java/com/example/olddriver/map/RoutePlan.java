package com.example.olddriver.map;
import android.util.Log;

import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

/**
 * Created by lzp on 2016/11/23.
 * 封装路径规划请求
 * 有两个构造方法，一个强行指定查询类型，
 * 一个无参数方法使用查询参数指定的查询类型
 * OnGetRoutePlanListener异步接收查询结果
 */


public class RoutePlan {
    private RoutePlanSearch mSearch;
    private DrivingRoutePlanOption.DrivingPolicy routePlanPolicy;



    public interface OnGetRoutePlanListener{
        public void onGetDrivingRoutePlan(DrivingRouteResult drivingRouteResult, DrivingRoutePlanOption.DrivingPolicy routePlanPolicy);
    }


    /*封装监听事件，方便调用*/
    public void setOnGetRoutePlanListener(final OnGetRoutePlanListener listener){
        OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {}

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {}

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {}

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                Log.i("routePlan running", "running");
                listener.onGetDrivingRoutePlan(drivingRouteResult, RoutePlan.this.routePlanPolicy);
            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {}

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {}
        };

        mSearch.setOnGetRoutePlanResultListener(routePlanResultListener);
    }

    public RoutePlan(){
        mSearch = RoutePlanSearch.newInstance();
    }

    public RoutePlan(DrivingRoutePlanOption.DrivingPolicy policy){
        mSearch = RoutePlanSearch.newInstance();
        this.routePlanPolicy = policy;
    }

    public void setPolicy(DrivingRoutePlanOption.DrivingPolicy policy){
        this.routePlanPolicy = policy;
    }

    public void drivingSearch(DrivingRoutePlanOption option){
        if(routePlanPolicy != null)
            option.policy(this.routePlanPolicy);


        option.trafficPolicy(DrivingRoutePlanOption.DrivingTrafficPolicy.ROUTE_PATH_AND_TRAFFIC);
        mSearch.drivingSearch(option);

        Log.i("routePlanStart: ", "start");
    }
}
