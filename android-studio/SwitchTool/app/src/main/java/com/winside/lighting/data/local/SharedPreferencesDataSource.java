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

}
