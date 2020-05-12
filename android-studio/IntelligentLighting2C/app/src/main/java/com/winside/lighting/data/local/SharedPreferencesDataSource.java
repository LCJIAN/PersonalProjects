package com.winside.lighting.data.local;

import android.content.Context;

import com.winside.lighting.App;

public class SharedPreferencesDataSource {

    public static void setToken(String token) {
        App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .edit()
                .putString("token", token)
                .apply();
    }

    public static String getToken() {
        return App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE).getString("token", "");
    }

    public static void setCurrentProjectId(Long projectId) {
        App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .edit()
                .putLong("project_id", projectId)
                .apply();
    }

    public static Long getCurrentProjectId() {
        return App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE).getLong("project_id", 0);
    }

    public static Boolean getNeedRefresh() {
        return App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE).getBoolean("need_refresh", false);
    }

    public static void putNeedRefresh(Boolean needRefresh) {
        App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("need_refresh", needRefresh)
                .apply();
    }

    public static void clear() {
        App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE).edit().clear().apply();
    }
}
