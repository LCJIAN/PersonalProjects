package com.lcjian.drinkwater.util;

import android.app.ActivityManager;
import android.app.Service;
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

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @return true代表正在运行;false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context context, Class<? extends Service> serviceClass) {
        boolean isWork = false;

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(40);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            String mName = runningServiceInfo.service.getClassName();
            if (mName.equals(serviceClass.getName())) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
