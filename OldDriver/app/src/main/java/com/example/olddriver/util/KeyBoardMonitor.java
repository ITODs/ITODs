package com.example.olddriver.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.reflect.Field;

/**
 * Created by lzp on 2016/12/15.
 * 键盘监听器，监听键盘的弹出和取消
 * 传进来的view要是窗口的rootView
 * 能够获取键盘高度
 */

public class KeyBoardMonitor {
    // 状态栏的高度
    private int statusBarHeight;
    // 软键盘的高度
    public static int keyboardHeight;
    // 软键盘的显示状态
    private boolean isShowKeyboard;
    private View layout;
    private Activity activity;

    public void setOnKeyBoardStateChangeListener(OnKeyBoardStateChangeListener listener) {
        this.listener = listener;
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new GlobalLayoutListener());
    }

    private OnKeyBoardStateChangeListener listener;

    public interface OnKeyBoardStateChangeListener{
        void onKeyBoardStateChange(boolean isVisiable);
    }

    public KeyBoardMonitor(Activity activity, View rootLayout){
        this.activity = activity;
        this.layout = rootLayout;
        statusBarHeight = getStatusBarHeight(activity.getApplicationContext());

    }

    // 获取状态栏高度
    public static int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    private class GlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            // 应用可以显示的区域。此处包括应用占用的区域，
            // 以及ActionBar和状态栏，但不含设备底部的虚拟按键。
            Rect r = new Rect();
            layout.getWindowVisibleDisplayFrame(r);

            // 屏幕高度。这个高度不含虚拟按键的高度
            int screenHeight = layout.getRootView().getHeight();

            int heightDiff = screenHeight - (r.bottom - r.top);

            // 在不显示软键盘时，heightDiff等于状态栏的高度
            // 在显示软键盘时，heightDiff会变大，等于软键盘加状态栏的高度。
            // 所以heightDiff大于状态栏高度时表示软键盘出现了，
            // 这时可算出软键盘的高度，即heightDiff减去状态栏的高度
            if(keyboardHeight == 0 && heightDiff > statusBarHeight){
                keyboardHeight = heightDiff - statusBarHeight;
            }

            if (isShowKeyboard) {
                // 如果软键盘是弹出的状态，并且heightDiff小于等于状态栏高度，
                // 说明这时软键盘已经收起
                if (heightDiff <= statusBarHeight) {
                    isShowKeyboard = false;
                    listener.onKeyBoardStateChange(false);
                }
            } else {
                // 如果软键盘是收起的状态，并且heightDiff大于状态栏高度，
                // 说明这时软键盘已经弹出
                if (heightDiff > statusBarHeight) {
                    isShowKeyboard = true;
                    listener.onKeyBoardStateChange(true);
                }
            }
        }
    };
}
