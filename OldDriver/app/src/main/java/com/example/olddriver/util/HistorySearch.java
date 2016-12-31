package com.example.olddriver.util;

import android.util.Log;

import com.example.olddriver.bean.LocationList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import cn.bmob.v3.BmobUser;

/**
 * Created by lzp on 2016/12/12.
 */

public class HistorySearch {

    private final static int MaxSize =20;
    public static String USER_PATH = null;
    private static File file = null;
    private static boolean flag=true;

    static {
        Log.i("datalist","historySearchRecord is onInit");

        if(BmobUser.getCurrentUser()!=null) {
            USER_PATH =DataStore.APP_PATH + File.separator + "user" + File.separator + BmobUser.getCurrentUser().getUsername();
            file =new File(USER_PATH);
            if(file.exists()==false)
                file.mkdir();
            file =new File(USER_PATH + File.separator + "historySearch.ser");
            if(file.exists()==false)
                try {
                    file.createNewFile();
                    dataList = new LinkedList<>();
                    Output();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Input();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    private static List<LocationList> dataList = null;

    private static void Input() throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        dataList = (List<LocationList>) ois.readObject();

        if(dataList == null)
            dataList = new LinkedList<>();

        Log.i("datalist.size","" + dataList.size());

        for(LocationList data: dataList){
            Log.i("dataitem",""+data.getLocation_title());
        }

        ois.close();
        fis.close();
    }

    private static void Output() throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(dataList);
        oos.flush();
        oos.close();
        fos.close();
    }

    public static boolean saveHistorySearchData(final LocationList location) {
        flag = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(int i = 0; i < dataList.size(); i++)
                        if(location.getLocation_title().equals(dataList.get(i).getLocation_title())){
                            dataList.remove(i);
                            break;
                            //最多只会有一个相同的纪录
                        }
                    dataList.add(0,location);
                    if (dataList.size() == MaxSize + 1)
                        dataList.remove(MaxSize);
                    Output();
                } catch (Exception e) {
                    e.printStackTrace();
                    flag =false;
                }
            }
        }).start();
        return flag;
    }


    public static boolean remove(final int index){
        flag = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dataList.remove(index);
                    Output();
                } catch (Exception e) {
                    flag =false;
                    e.printStackTrace();
                }
            }
        }).start();
        return flag;
    }

    public static boolean removeAll(){
        flag = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dataList.clear();
                    Output();
                } catch (Exception e) {
                    flag =false;
                    e.printStackTrace();
                }
            }
        }).start();
        return flag;
    }

    public static List<LocationList> getHistory(){
        return dataList;
    }

    public static void getHistory(final OnGetHistoryDataListener listener){
        if(BmobUser.getCurrentUser()!=null) {
            USER_PATH =DataStore.APP_PATH + File.separator + "user" + File.separator + BmobUser.getCurrentUser().getUsername();
            file =new File(USER_PATH);
            if(file.exists()==false)
                file.mkdir();
            file =new File(USER_PATH + File.separator + "historySearch.ser");
            if(file.exists()==false)
                try {
                    file.createNewFile();
                    dataList = new LinkedList<>();
                    Output();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Input();
                        //input后数据会在dataList中
                        listener.onGetHistoryData(dataList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public interface OnGetHistoryDataListener{
        void onGetHistoryData(List<LocationList> dataList);
    }
}
