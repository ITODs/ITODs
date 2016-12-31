package com.example.olddriver.map;
import android.util.Log;

import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;

import java.util.HashMap;

/**
 * Created by lzp on 2016/12/4.
 * 里面维护3个RoutePlan对象，分别处理3种类型的查询，
 * private boolean isContainRouteLine(DrivingRouteLine line)用来去重
 * 有OnGetRoutePlanResultListener当3个查询都返回时触发
 */

public class RoutePlanSearch {
    private int responseCnt = 0;
    private DrivingRoutePlanOption searchOption;
    private RoutePlan[] routePlans;
    private HashMap<DrivingRoutePlanOption.DrivingPolicy, DrivingRouteLine> dataMap = new HashMap<>();

    public RoutePlanSearch(final DrivingRoutePlanOption searchOption){
        routePlans = new RoutePlan[3];
        routePlans[0] = new RoutePlan(DrivingRoutePlanOption.DrivingPolicy.ECAR_TIME_FIRST);
        routePlans[1] = new RoutePlan(DrivingRoutePlanOption.DrivingPolicy.ECAR_FEE_FIRST);
        routePlans[2] = new RoutePlan(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST);

        this.searchOption = searchOption;
    }

    public interface OnGetRoutePlanResultListener{
        void OnGetResult(HashMap<DrivingRoutePlanOption.DrivingPolicy, DrivingRouteLine> dataMap);
    }


    public void setListener(final OnGetRoutePlanResultListener listener) {
        for(int i = 0; i < 3; i++) {
            routePlans[i].setOnGetRoutePlanListener(new RoutePlan.OnGetRoutePlanListener() {
                @Override
                public void onGetDrivingRoutePlan(DrivingRouteResult drivingRouteResult, DrivingRoutePlanOption.DrivingPolicy routePlanPolicy) {
                    synchronized (dataMap){
                        responseCnt++;
                        Log.i("routePlan", "type" + routePlanPolicy.toString());
                        if(drivingRouteResult.getRouteLines() == null ||
                                drivingRouteResult.getRouteLines().get(0) == null){
                            return;
                        }else if(isContainRouteLine(drivingRouteResult.getRouteLines().get(0)) == false)
                            dataMap.put(routePlanPolicy,
                                    drivingRouteResult.getRouteLines().get(0));
                    }

                    if(responseCnt == 3)
                        listener.OnGetResult(dataMap);
                }
            });
        }
    }

    public void start(){
        routePlans[0].drivingSearch(searchOption);
        routePlans[1].drivingSearch(searchOption);
        routePlans[2].drivingSearch(searchOption);
    }

    private boolean isContainRouteLine(DrivingRouteLine line){
        for(DrivingRouteLine mLine: dataMap.values()){
            if(mLine.getDistance() == line.getDistance() &&
                    mLine.getDuration() == line.getDuration() &&
                    mLine.getLightNum() == line.getLightNum())
                return true;
        }
        return false;
    }
}
