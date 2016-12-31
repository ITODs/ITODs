package com.example.olddriver.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by lzp on 2016/12/6.
 */

public class DataStore {
    public static String APP_PATH;
    public static String USER_PATH;
    static{
        APP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "OldDriver";
        File file = new File(APP_PATH);
        if(file.exists() == false){
            file.mkdir();
        }
        USER_PATH=APP_PATH + File.separator + "user";
        file = new File(APP_PATH + File.separator + "user");
        if(file.exists() == false){
            file.mkdir();
        }

        String NAVI_DATA_PATH = APP_PATH + File.separator + "navi";
        file = new File(NAVI_DATA_PATH);
        if(file.exists() == false){
            file.mkdir();
        }
    }

}
