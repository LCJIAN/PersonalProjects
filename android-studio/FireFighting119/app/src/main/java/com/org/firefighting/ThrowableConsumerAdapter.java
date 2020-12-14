package com.org.firefighting;

import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.org.firefighting.data.network.entity.ErrorMsg;

import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class ThrowableConsumerAdapter {

    public static void accept(Throwable throwable) {
        if (throwable instanceof HttpException) {
            if (((HttpException) throwable).code() == 401) {
                Toast.makeText(App.getInstance(), "请退出重新登录", Toast.LENGTH_SHORT).show();
                return;
            }
            ResponseBody errorBody = ((HttpException) throwable).response().errorBody();
            if (errorBody != null) {
                ErrorMsg r = new Gson().fromJson(errorBody.charStream(), new TypeToken<ErrorMsg>() {}.getType());
                Toast.makeText(App.getInstance(), r.message, Toast.LENGTH_SHORT).show();
                errorBody.close();
            }
        } else {
            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
