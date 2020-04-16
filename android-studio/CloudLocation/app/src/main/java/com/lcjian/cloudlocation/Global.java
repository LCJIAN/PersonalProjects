package com.lcjian.cloudlocation;

import android.content.Context;

public class Global {

    public static final String GET_API_URL_URL = BuildConfig.GET_API_URL_URL;
    public static String CURRENT_USER_ID = "";
    public static String CURRENT_USER_NAME = "";
    public static boolean RE_CREATED = false;

    public static void setApiUrl(String apiUrl) {
        App.getInstance().getSharedPreferences("global_info", Context.MODE_PRIVATE)
                .edit()
                .putString("api_url", apiUrl)
                .apply();
    }

    public static String getApiUrl() {
        return App.getInstance().getSharedPreferences("global_info", Context.MODE_PRIVATE).getString("api_url", "");
    }

    public static void setServerUrl(String serverUrl) {
        App.getInstance().getSharedPreferences("global_info", Context.MODE_PRIVATE)
                .edit()
                .putString("server_url", serverUrl)
                .apply();
    }

    public static String getServerUrl() {
        return App.getInstance().getSharedPreferences("global_info", Context.MODE_PRIVATE).getString("server_url", "");
    }


}
