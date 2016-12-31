package com.example.olddriver.util;

import android.content.Context;

/**
 * 类似于屏幕适配器
 */

public class CompatUtils {
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
