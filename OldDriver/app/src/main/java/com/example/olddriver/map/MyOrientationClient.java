package com.example.olddriver.map;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by lzp on 2016/11/23.
 * 方向传感器监听器
 * 当手机方向改变的时候，回调OnOrientationListener()
 * 使用： new 一个MyOrientationListener
 * set一个 OnOrientationListener()回调函数
 * 在ui onStart/onStop的时候同时调用MyOrientationListener的这两个方法，降低资源消耗
 */

public class MyOrientationClient implements SensorEventListener {

    private SensorManager mSensorManager;
    private Context mContext;
    private Sensor mSensor;

    private float lastX;

    public MyOrientationClient(Context context){
        this.mContext = context;
    }

    @SuppressWarnings("deprecation")
    public void start(){
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        if(mSensorManager != null){
            //获得方向传感器
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }

        if(mSensor != null){
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }

    }

    public void stop(){
        mSensorManager.unregisterListener(this);

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSensorChanged(SensorEvent event) {//方向发生变化
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            float x = event.values[SensorManager.DATA_X];

            if(Math.abs(x-lastX) > 1.0){
                if(mOnOrientationListener != null){
                    mOnOrientationListener.onOrientationChanged(x);
                }


            }
            lastX = x;
        }
    }

    private OnOrientationListener mOnOrientationListener;
    public void setOnOrientationListener(
            OnOrientationListener mOnOrientationListener) {
        this.mOnOrientationListener = mOnOrientationListener;
    }

    public interface OnOrientationListener{
        void onOrientationChanged(float x);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        //精度的改变暂时不用管

    }

}

