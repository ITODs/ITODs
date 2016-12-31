package com.example.olddriver.map;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.example.olddriver.R;
import com.example.olddriver.bean.SearchResult;
import com.example.olddriver.map.overlay.DrivingRouteOverlay;
import com.example.olddriver.util.App;
import com.example.olddriver.util.DataFormater;

import java.util.Map;

/**
 * Created by 苏颂贤 on 2016/12/4.
 * routeLineController封装了路线和ui的一些联动操作。
 */

public class RouteLineController {
    private Map<DrivingRoutePlanOption.DrivingPolicy, DrivingRouteLine> routeLinesMap;
    private DrivingRouteOverlay[] routeOverlays;
    private TextView[][] routeLineInfos;
    private TextView lightNum;
    private TextView jamLength;
    private LinearLayout[] bgLayout;
    private int currentLine = 0;
    private boolean visiable = true;
    private Handler handler;
    private static long timeStamp = 0;

    //扔个父布局过来findView
    public RouteLineController(SearchResult searchResult,
                               BaiduMap baiduMap,
                               View parent,
                               Handler handler){
        this.handler = handler;

       /* //设置打车费用
        TextView taxiPriceTV = (TextView) parent.findViewById(R.id.route_cost);
        taxiPriceTV.setText(searchResult.getTaxiPrice());*/

        bgLayout = new LinearLayout[3];
        bgLayout[0] = (LinearLayout) parent.findViewById(R.id.plan_time_first);
        bgLayout[1] = (LinearLayout) parent.findViewById(R.id.plan_dis_first);
        bgLayout[2] = (LinearLayout) parent.findViewById(R.id.plan_fee_first);

        routeLineInfos = new TextView[3][3];
        routeLineInfos[0][0] = (TextView) parent.findViewById(R.id.route_time_first_title);
        routeLineInfos[0][1] = (TextView) parent.findViewById(R.id.route_time_first_cost);
        routeLineInfos[0][2] = (TextView) parent.findViewById(R.id.route_time_first_distance);

        routeLineInfos[1][0] = (TextView) parent.findViewById(R.id.route_dis_first_title);
        routeLineInfos[1][1] = (TextView) parent.findViewById(R.id.route_dis_first_cost);
        routeLineInfos[1][2] = (TextView) parent.findViewById(R.id.route_dis_first_distance);

        routeLineInfos[2][0] = (TextView) parent.findViewById(R.id.route_fee_first_title);
        routeLineInfos[2][1] = (TextView) parent.findViewById(R.id.route_fee_first_cost);
        routeLineInfos[2][2] = (TextView) parent.findViewById(R.id.route_fee_first_distance);

        lightNum = (TextView) parent.findViewById(R.id.route_num_light);
        jamLength = (TextView) parent.findViewById(R.id.route_num_jam);


        for (int i = 0; i < 3; i++)
            bgLayout[i].setOnClickListener(listener);

        this.routeLinesMap = searchResult.getRouteLineDataMap();
        routeOverlays = new DrivingRouteOverlay[3];

        int i = 0;
        DrivingRouteLine line = null;
        line = routeLinesMap.get(DrivingRoutePlanOption.DrivingPolicy.ECAR_TIME_FIRST);

        if (line != null) {
            routeOverlays[i] = new DrivingRouteOverlay(baiduMap, i);
            routeOverlays[i].setData(line);

            routeLineInfos[i][1].setText(DataFormater.formatTime(line));
            routeLineInfos[i][2].setText(DataFormater.formatDistance(line.getDistance()));

            routeOverlays[i].setListener(new RouteOverlayClickListener(i));

        } else {
            bgLayout[0].setVisibility(View.GONE);
        }

        i++;
        line = routeLinesMap.get(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST);
        if (line != null) {
            routeOverlays[i] = new DrivingRouteOverlay(baiduMap, i);
            routeOverlays[i].setData(line);

            routeLineInfos[i][1].setText(DataFormater.formatTime(line));
            routeLineInfos[i][2].setText(DataFormater.formatDistance(line.getDistance()));

            routeOverlays[i].setListener(new RouteOverlayClickListener(i));
        } else {
            bgLayout[1].setVisibility(View.GONE);
        }

        i++;
        line = routeLinesMap.get(DrivingRoutePlanOption.DrivingPolicy.ECAR_FEE_FIRST);
        if (line != null) {
            routeOverlays[i] = new DrivingRouteOverlay(baiduMap, i);
            routeOverlays[i].setData(line);

            routeLineInfos[i][1].setText(DataFormater.formatTime(line));
            routeLineInfos[i][2].setText(DataFormater.formatDistance(line.getDistance()));

            routeOverlays[i].setListener(new RouteOverlayClickListener(i));
        } else {
            bgLayout[2].setVisibility(View.GONE);
            Log.i("gone", "2");
        }

        for (i = 0; i < 3; i++) {
            if (routeOverlays[i] != null) {
                currentLine = i;
                routeOverlays[i].setSelected(true);
                changeView(currentLine);
                break;
            }
        }

        for (i = 0; i < 3; i++) {
            if (routeOverlays[i] != null && i != currentLine) {
                routeOverlays[i].addToMap();
            }
        }

        routeOverlays[currentLine].addToMap();
        routeOverlays[currentLine].zoomToSpan();
    }

    private void changeView(int id) {
        for (int i = 0; i < 3; i++) {
            routeLineInfos[id][i].setTextColor(Color.WHITE);
        }

        for(int i = 0; i < 3; i++){
            if(i != id && routeOverlays[i] != null){
                bgLayout[i].setBackgroundColor(Color.parseColor("#ccFFFFFF"));
                routeLineInfos[i][0].setTextColor(Color.GRAY);
                routeLineInfos[i][1].setTextColor(Color.parseColor("#FF8C00"));
                routeLineInfos[i][2].setTextColor(App.getAppContext().getResources().getColor(R.color.DarkGray));
            }
        }

        lightNum.setText(routeOverlays[id].getmRouteLine().getLightNum() +"");
        jamLength.setText(routeOverlays[id].getmRouteLine().getCongestionDistance()+"");
        bgLayout[id].setBackgroundResource(R.color.DodgerBlue);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final int id = view.getId();
            int selectedId = 0;
            switch (id) {
                case R.id.plan_time_first:
                    selectedId = 0;
                    break;
                case R.id.plan_dis_first:
                    selectedId = 1;
                    break;
                case R.id.plan_fee_first:
                    selectedId = 2;
                    break;
                default:
                    break;
            }

            changeView(selectedId);
            final int lastLineNum = currentLine;
            final int newLineNum = selectedId;
            RouteLineController.timeStamp = System.currentTimeMillis();
            handler.postDelayed(new Runnable() {
                private long timeStamp = RouteLineController.timeStamp;

                @Override
                public void run() {
                    if(timeStamp == RouteLineController.timeStamp){
                        changeSelectedRoute(lastLineNum, newLineNum);
                        currentLine = newLineNum;
                        return;
                    }
                }
            },300);

        }
    };

    private void changeSelectedRoute(int lastLineNum, int newLineNum) {
        routeOverlays[lastLineNum].setSelected(false);
        routeOverlays[newLineNum].setSelected(true);
        routeOverlays[newLineNum].zoomToSpan();
    }

    public void recycle() {
        for (int i = 0; i < 3; i++) {
            if (routeOverlays[i] != null)
                routeOverlays[i].removeFromMap();
        }
    }

    public void setVisiable(boolean visiable) {
        if (this.visiable == visiable)
            return;

        if (!visiable) {
            for (int i = 0; i < 3; i++) {
                if (routeOverlays[i] != null)
                    routeOverlays[i].removeFromMap();
            }
        } else {
            for (int i = 0; i < 3; i++) {
                if (routeOverlays[i] != null)
                    routeOverlays[i].addToMap();
            }
        }
    }

    private class RouteOverlayClickListener implements DrivingRouteOverlay.OnClickListener {

        private int lineId;

        public RouteOverlayClickListener(int lineId) {
            this.lineId = lineId;
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            bgLayout[lineId].performClick();
            return true;
        }

        @Override
        public boolean onPolylineClick(Polyline polyline) {
            bgLayout[lineId].performClick();
            return true;
        }
    }
}
