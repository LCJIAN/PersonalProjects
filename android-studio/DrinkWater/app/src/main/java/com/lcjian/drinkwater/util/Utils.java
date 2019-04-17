package com.lcjian.drinkwater.util;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class Utils {

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                /*
                 *  BACKGROUND=400 EMPTY=500 FOREGROUND=100
                 *  GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
                 */
                return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }
}
