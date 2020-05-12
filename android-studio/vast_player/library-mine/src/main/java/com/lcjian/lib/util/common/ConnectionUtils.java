package com.lcjian.lib.util.common;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionUtils {
    /**
     * 检查网络是否可用
     *
     * @param context 应用程序的上下文对象
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); // 获取系统网络连接管理器
        if (connectivity == null) { // 如果网络管理器为null
            return false; // 返回false表明网络无法连接
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo(); // 获取所有的网络连接对象
            if (info != null) { // 网络信息不为null时
                for (int i = 0; i < info.length; i++) { // 遍历网路连接对象
                    if (info[i].isConnected()) { // 当有一个网络连接对象连接上网络时
                        return true; // 返回true表明网络连接正常
                    }
                }
            }
        }
        return false;
    }

    /**
     * 打开设置网络界面
     */
    public static void setNetworkMethod(final Context context) {
        // 提示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("网络设置提示")
                .setMessage("网络连接不可用,是否进行设置?")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = null;
                        // 判断手机系统的版本 即API大于10 就是3.0或以上版本
                        if (android.os.Build.VERSION.SDK_INT > 10) {
                            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                        } else {
                            intent = new Intent();
                            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                            intent.setComponent(component);
                            intent.setAction("android.intent.action.VIEW");
                        }
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public static void httpTest(final Context context, String title, String msg) {
        if (!isNetworkAvailable(context)) {
            AlertDialog.Builder builders = new AlertDialog.Builder(context);
            builders.setTitle(title);
            builders.setMessage(msg);
            final AlertDialog alert = builders.create();
            builders.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alert.dismiss();
                        }
                    });
            alert.show();
        }
    }
}