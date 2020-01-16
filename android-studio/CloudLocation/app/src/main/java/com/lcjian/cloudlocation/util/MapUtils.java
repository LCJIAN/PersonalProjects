package com.lcjian.cloudlocation.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MapUtils {

    private static final String BAIDU_PACKAGE_NAME = "com.baidu.BaiduMap";
    private static final String GAODE_PACKAGE_NAME = "com.autonavi.minimap";
    private static final String TENCENT_PACKAGE_NAME = "com.tencent.map";
    private static final String GOOGLE_PACKAGE_NAME = "com.google.android.apps.maps";

    /**
     * 是否安装百度地图
     */
    public static boolean haveBaiduMap(Context context) {
        return exist(context, BAIDU_PACKAGE_NAME);
    }

    public static boolean haveGaodeMap(Context context) {
        return exist(context, GAODE_PACKAGE_NAME);
    }

    public static boolean haveTencentMap(Context context) {
        return exist(context, TENCENT_PACKAGE_NAME);
    }

    public static boolean haveGoogleMap(Context context) {
        return exist(context, GOOGLE_PACKAGE_NAME);
    }

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param packageName：应用包名
     * @return true 存在
     */
    public static boolean exist(Context context, String packageName) {
        //获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有true，没有false
        return packageNames.contains(packageName);
    }

    /**
     * 调用百度地图
     *
     * @param destination        目的地经纬度
     * @param destinationAddress 目的地地址
     *                           百度参考网址：http://lbsyun.baidu.com/index.php?title=uri/api/android
     */
    public static void openBaiduMap(Context context, LatLng destination,
                                    String destinationAddress) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("baidumap://map/direction?" +
                "destination=latlng:" + destination.latitude + "," + destination.longitude +
                "|name:" + destinationAddress +
                "&mode=driving"));
        context.startActivity(intent);
    }

    /**
     * 调用高德地图app,导航
     *
     * @param context            上下文
     * @param destination        目标经纬度
     * @param destinationAddress 目标地址
     *                           高德地图：http://lbs.amap.com/api/amap-mobile/guide/android/route
     */
    public static void openGaodeMap(Context context, LatLng destination,
                                    String destinationAddress) {
        double[] da = bdToGaoDe(destination.latitude, destination.longitude);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("amapuri://route/plan/?" +
                "dlat=" + da[0] +
                "&dlon=" + da[1] +
                "&dname=" + destinationAddress +
                "&dev=0" +
                "&t=0"));
        context.startActivity(intent);
    }

    /**
     * 调用腾讯地图
     *
     * @param context
     * @param destination        目的地经纬度
     * @param destinationAddress 目的地地址
     *                           腾讯地图参考网址：http://lbs.qq.com/uri_v1/guide-route.html
     */
    public static void openTentcentMap(Context context, LatLng destination, String destinationAddress) {
        double[] da = bdToGaoDe(destination.latitude, destination.longitude);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse("qqmap://map/routeplan?" +
                "type=drive" +
                "&from=" +
                "&fromcoord=" +
                "&to=" + destinationAddress +
                "&tocoord=" + da[0] + "," + da[1] +
                "&policy=0" +
                "&referer=appName"));
        context.startActivity(intent);
    }

    /**
     * 打开网页版 导航
     * 不填我的位置，则通过浏览器定未当前位置
     *
     * @param myLatLng           起点经纬度
     * @param myAddress          起点地址名展示
     * @param destination        终点经纬度
     * @param destinationAddress 终点地址名展示
     */
    public static void openBrowserMap(Context context, LatLng myLatLng, String myAddress, LatLng destination,
                                      String destinationAddress) {

        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse("http://uri.amap.com/navigation?" +
                "from=" + myLatLng.longitude + "," + myLatLng.latitude + "," + myAddress +
                "to=" + destination.longitude + "," + destination.latitude + "," + destinationAddress +
                "&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=0"));
        context.startActivity(intent);
    }

    public static void openGoogleMap(Context context, com.google.android.gms.maps.model.LatLng ll) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + ll.latitude + "," + ll.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(mapIntent);
    }

    public static double[] bdToGaoDe(double bd_lat, double bd_lon) {
        double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta);
        double tempLat = z * Math.sin(theta);
        return new double[]{tempLat, tempLon};
    }
}