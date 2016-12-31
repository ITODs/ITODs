package com.example.olddriver.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.navisdk.adapter.BaiduNaviManager;

import cn.bmob.v3.Bmob;

/**
 * Created by lzp on 2016/11/30.
 */

public class App extends Application {

    //通过UserName 获取
    private static SharedPreferences settingsSharedPreferences = null;


    private static Context appContext = null;

    public static SharedPreferences getSettingsSharedPreferences() {
        return settingsSharedPreferences;
    }

    public static Context getAppContext() {
        return appContext;
    }

    @Override
    public void onCreate() {
        settingsSharedPreferences = this.getSharedPreferences("settings",MODE_PRIVATE);
        appContext = this.getApplicationContext();

        /*初始化百度SDK,Bmob SDK*/
        Bmob.initialize(getApplicationContext(),"fe0e98fd5c52e0e6089e20a259cf2d87");
        SDKInitializer.initialize(this.getApplicationContext());


        /*反初始化百度导航*/
        if(BaiduNaviManager.isNaviInited())
            BaiduNaviManager.getInstance().uninit();

        MyTTS.init(getApplicationContext());

        //在这里为应用设置异常处理程序，然后我们的程序才能捕获未处理的异常
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }


}
