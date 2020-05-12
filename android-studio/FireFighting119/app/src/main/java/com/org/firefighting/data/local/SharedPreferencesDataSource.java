package com.org.firefighting.data.local;

import android.content.Context;

import com.google.gson.Gson;
import com.org.firefighting.App;
import com.org.firefighting.data.network.entity.SignInRequest;
import com.org.firefighting.data.network.entity.SignInResponse;

public class SharedPreferencesDataSource {

    public static void putGuided(boolean guided) {
        App.getInstance().getSharedPreferences("app_info", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("guided", guided)
                .apply();
    }

    public static boolean getGuided() {
        return App.getInstance()
                .getSharedPreferences("app_info", Context.MODE_PRIVATE)
                .getBoolean("guided", false);
    }

    public static void putSignInRequest(SignInRequest signInRequest) {
        App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .edit()
                .putString("sign_in_request", new Gson().toJson(signInRequest))
                .apply();
    }

    public static SignInRequest getSignInRequest() {
        return new Gson()
                .fromJson(App.getInstance()
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("sign_in_request", ""), SignInRequest.class);
    }

    public static void putSignInResponse(SignInResponse signInResponse) {
        App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .edit()
                .putString("sign_in_response", new Gson().toJson(signInResponse))
                .apply();
    }

    public static SignInResponse getSignInResponse() {
        return new Gson()
                .fromJson(App.getInstance()
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("sign_in_response", ""), SignInResponse.class);
    }

    public static void putContinueAdd(boolean continueAdd) {
        App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("continue_add", continueAdd)
                .apply();
    }

    public static boolean getContinueAdd() {
        return App.getInstance()
                .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getBoolean("continue_add", false);
    }

    public static void putContinueAddRemember(boolean continueAddRemember) {
        App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("continue_add_remember", continueAddRemember)
                .apply();
    }

    public static boolean getContinueAddRemember() {
        return App.getInstance()
                .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getBoolean("continue_add_remember", false);
    }

    public static void clearUserInfo() {
        App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE).edit().clear().apply();
    }
}
