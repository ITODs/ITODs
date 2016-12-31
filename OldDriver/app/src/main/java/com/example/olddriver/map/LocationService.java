package com.example.olddriver.map;
import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.Date;

/**
 * Created by lzp on 2016/11/16.
 * 使用方法：
 * 在主线程中new,并且传入一个全局Context和定位时间间隔 作为参数（getApplicationContext()，time）
 * 1.可选项 .setLocationListener(MyLocationListener listener)注册个人需要的监听。
 * //封装了一个默认的MyLocationListener类在类中提供默认的监听
 * 2.startLocationService()启动定位服务
 * 3.stopLocationService()终止定位服务
 *4.类中定义了两个静态对象引用用于存储最近一次定位的位置，和时间
 * 备注：经过测试，在程序没有完全退出之前， 定位服务会一直开启
 * 定位相关的参数可以在initLocation()里面修正
 *
 * 初次使用会询问定位权限，
 * 如果没有获得权限会定位失败并报error code 62
 * 在后期版本，申请定位前最好检查一下定位权限，并动态做出修改，
 * 否则第一次拒绝之后只能用户到手机权限管理中自己修改
 *
 * 飞行模式error code 也是62
 * 只要手机有信号，就算没有开wifi和数据， 一样能够网络定位成功
 */
public class LocationService {
    private LocationClient locationClient = null;
    private BDLocationListener listener = null;
    private MyOrientationClient orientationClient = null;
    private MyLocationListener myLocationListener = null;

    public static BDLocation lastLocation = null;
    public static Date createTime = null;

    /*实时方向*/
    private float orientation = 0.0f;

    public interface MyLocationListener{
        void getLocation(BDLocation location);
    }


    /*初始化定位参数*/
    private void initLocation(int span){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        locationClient.setLocOption(option);
    }

    public LocationService(Context context, int span){
        /*构造定位核心类，
        * 注册定位结果监听
        * 初始化定位参数*/
        locationClient = new LocationClient(context);
        orientationClient = new MyOrientationClient(context);

        orientationClient.setOnOrientationListener(new MyOrientationClient.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                orientation = x;
            }
        });

        initLocation(span);
    }

    public void startLocationService(){
        Log.i("定位服务","start");
        if(locationClient.isStarted() == false){
            orientationClient.start();
            locationClient.start();
        }
    }

    public void stopLocationService(){
        if(locationClient.isStarted()){
            locationClient.stop();
            orientationClient.stop();
        }
    }


    public void setLocationListener(MyLocationListener locationListener){
        this.myLocationListener = locationListener;

        listener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                Log.i("location",bdLocation.getLocType() + "");

                bdLocation.setDirection(orientation);

                lastLocation = bdLocation;
                createTime = new Date();
                //调用传进来的myLocationListener
                myLocationListener.getLocation(bdLocation);
            }
        };

        locationClient.registerLocationListener(listener);
    }

    public void requestCurrentLocation(){
        locationClient.requestLocation();
    }
}
