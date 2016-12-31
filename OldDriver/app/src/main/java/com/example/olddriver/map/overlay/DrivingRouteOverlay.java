package com.example.olddriver.map.overlay;


import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRouteLine;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lzp on 2016/11/24.
 * 驾车路线覆盖物类，然而相当基础，对样式有需求就修改这个类
 */

/**
 * 用于显示一条驾车路线的overlay，自3.4.0版本起可实例化多个添加在地图中显示，当数据中包含路况数据时，则默认使用路况纹理分段绘制
 */
public class DrivingRouteOverlay extends OverlayManager {

    public DrivingRouteLine getmRouteLine() {
        return mRouteLine;
    }

    private DrivingRouteLine mRouteLine = null;
    boolean focus = false;
    boolean selected = false;
    private int id;

    private static ArrayList<BitmapDescriptor> focusList = null;
    private static ArrayList<BitmapDescriptor> noFocusList = null;
    
    static {
        focusList = new ArrayList<BitmapDescriptor>();
        focusList.add(BitmapDescriptorFactory.fromAsset("icon_road_blue_arrow.png"));
        focusList.add(BitmapDescriptorFactory.fromAsset("icon_road_green_arrow.png"));
        focusList.add(BitmapDescriptorFactory.fromAsset("icon_road_yellow_arrow.png"));
        focusList.add(BitmapDescriptorFactory.fromAsset("icon_road_red_arrow.png"));
        focusList.add(BitmapDescriptorFactory.fromAsset("icon_road_nofocus_arrow.png"));

        noFocusList = new ArrayList<BitmapDescriptor>();
        noFocusList.add(BitmapDescriptorFactory.fromAsset("low_blue.png"));
        noFocusList.add(BitmapDescriptorFactory.fromAsset("low_green.png"));
        noFocusList.add(BitmapDescriptorFactory.fromAsset("low_yellow.png"));
        noFocusList.add(BitmapDescriptorFactory.fromAsset("low_red.png"));
        noFocusList.add(BitmapDescriptorFactory.fromAsset("icon_road_blue_arrow.png"));
    }
    
    
    public void setListener(OnClickListener listener) {
        this.listener = listener;
        mBaiduMap.setOnPolylineClickListener(this);
        mBaiduMap.setOnMarkerClickListener(this);
    }

    private OnClickListener listener = null;

    public void setSelected(boolean selected) {
        this.removeFromMap();
        this.selected = selected;
        this.addToMap();
    }

    public int getId() {
        return id;
    }


    /**
     * 构造函数
     *
     * @param baiduMap
     *            该DrivingRouteOvelray引用的 BaiduMap
     */
    public DrivingRouteOverlay(BaiduMap baiduMap, int id) {
        super(baiduMap);
        this.id = id;

    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        if (mRouteLine == null) {
            return null;
        }

        List<OverlayOptions> overlayOptionses = new ArrayList<OverlayOptions>();
        // step node
        if (mRouteLine.getAllStep() != null
                && mRouteLine.getAllStep().size() > 0) {

            for (DrivingRouteLine.DrivingStep step : mRouteLine.getAllStep()) {
                Bundle b = new Bundle();

                b.putInt("index", mRouteLine.getAllStep().indexOf(step));

                // 最后路段绘制出口点
                if (mRouteLine.getAllStep().indexOf(step) == (mRouteLine
                        .getAllStep().size() - 1) && step.getExit() != null) {
                    overlayOptionses.add((new MarkerOptions())
                            .position(step.getExit().getLocation())
                            .anchor(0.5f, 0.5f)
                            .zIndex(10)
                            .icon(BitmapDescriptorFactory
                                    .fromAssetWithDpi("Icon_line_node.png")));

                }
            }
        }

        if (mRouteLine.getStarting() != null) {
            overlayOptionses.add((new MarkerOptions())
                    .position(mRouteLine.getStarting().getLocation())
                    .icon(getStartMarker() != null ? getStartMarker() :
                            BitmapDescriptorFactory
                                    .fromAssetWithDpi("Icon_start.png")).zIndex(10));
        }
        if (mRouteLine.getTerminal() != null) {
            overlayOptionses
                    .add((new MarkerOptions())
                            .position(mRouteLine.getTerminal().getLocation())
                            .icon(getTerminalMarker() != null ? getTerminalMarker() :
                                    BitmapDescriptorFactory
                                            .fromAssetWithDpi("Icon_end.png"))
                            .zIndex(10));
        }
        // poly line
        if (mRouteLine.getAllStep() != null
                && mRouteLine.getAllStep().size() > 0) {

            List<DrivingRouteLine.DrivingStep> steps = mRouteLine.getAllStep();
            int stepNum = steps.size();


            List<LatLng> points = new ArrayList<LatLng>();
            ArrayList<Integer> traffics = new ArrayList<Integer>();
            int totalTraffic = 0;

            for (int i = 0; i < stepNum ; i++) {
                if (i == stepNum - 1) {
                    points.addAll(steps.get(i).getWayPoints());
                } else {
                    points.addAll(steps.get(i).getWayPoints().subList(0, steps.get(i).getWayPoints().size() - 1));
                }

                totalTraffic += steps.get(i).getWayPoints().size() - 1;


                if (steps.get(i).getTrafficList() != null && steps.get(i).getTrafficList().length > 0) {
                    for (int j = 0;j < steps.get(i).getTrafficList().length;j++) {
                        traffics.add(steps.get(i).getTrafficList()[j]);
                    }
                }
            }

//            Bundle indexList = new Bundle();
//            if (traffics.size() > 0) {
//                int raffic[] = new int[traffics.size()];
//                int index = 0;
//                for (Integer tempTraff : traffics) {
//                    raffic[index] = tempTraff.intValue();
//                    index++;
//                }
//                indexList.putIntArray("indexs", raffic);
//            }
            boolean isDotLine = false;

            if (traffics != null && traffics.size() > 0) {
                isDotLine = true;
            }
            PolylineOptions option = new PolylineOptions().points(points).textureIndex(traffics)
                    .width(9).dottedLine(isDotLine).focus(true)
                    .color(getLineColor(selected) != 0 ? getLineColor(selected) : Color.argb(178, 0, 78, 255)).zIndex(0);
            if (isDotLine) {
                option.customTextureList(getCustomTextureList());
            }

            overlayOptionses.add(option);
        }
        return overlayOptionses;
    }

    /**
     * 设置路线数据
     *
     * @param routeLine
     *            路线数据
     */
    public void setData(DrivingRouteLine routeLine) {
        this.mRouteLine = routeLine;
    }

    /**
     * 覆写此方法以改变默认起点图标
     *
     * @return 起点图标
     */
    public BitmapDescriptor getStartMarker() {
        return null;
    }

    /**
     * 覆写此方法以改变默认绘制颜色
     * @return 线颜色
     */
    public int getLineColor(boolean selected) {
        if(selected)
            return Color.RED;
        else
            return Color.argb(255,0,255,172);
    }

    public List<BitmapDescriptor> getCustomTextureList() {
        if(this.selected){
            return focusList;
        }else{
            return noFocusList;
        }
    }
    /**
     * 覆写此方法以改变默认终点图标
     *
     * @return 终点图标
     */
    public BitmapDescriptor getTerminalMarker() {
        return null;
    }

    /**
     * 覆写此方法以改变默认点击处理
     *
     * @param i
     *            线路节点的 index
     * @return 是否处理了该点击事件
     */
    public boolean onRouteNodeClick(int i) {
        if (mRouteLine.getAllStep() != null
                && mRouteLine.getAllStep().get(i) != null) {
            Log.i("baidumapsdk", "DrivingRouteOverlay onRouteNodeClick");
        }
        return false;
    }

    public interface OnClickListener{
        boolean onMarkerClick(Marker marker);
        boolean onPolylineClick(Polyline polyline);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for (Overlay mMarker : mOverlayList) {
            if (mMarker instanceof Marker && mMarker.equals(marker)) {
                if (marker.getExtraInfo() != null) {
                    onRouteNodeClick(marker.getExtraInfo().getInt("index"));
                }
                listener.onMarkerClick(marker);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        boolean flag = false;
        for (Overlay mPolyline : mOverlayList) {
            if (mPolyline instanceof Polyline && mPolyline.equals(polyline)) {
                listener.onPolylineClick(polyline);
                // 选中
                flag = true;
                setFocus(flag);
                return true;
            }
        }
        return false;
    }

    public void setFocus(boolean flag) {
        focus = flag;
        for (Overlay mPolyline : mOverlayList) {
            if (mPolyline instanceof Polyline) {
                // 选中
                ((Polyline) mPolyline).setFocus(flag);
                break;
            }
        }

    }

}
