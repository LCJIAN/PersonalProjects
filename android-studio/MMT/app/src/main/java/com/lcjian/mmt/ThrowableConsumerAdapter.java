package com.lcjian.mmt;

import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcjian.mmt.data.network.entity.ResponseData;

import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class ThrowableConsumerAdapter {

    public static void accept(Throwable throwable) {
        if (throwable instanceof HttpException) {
            ResponseBody errorBody = ((HttpException) throwable).response().errorBody();
            if (errorBody != null) {
                ResponseData<String> r = new Gson().fromJson(errorBody.charStream(), new TypeToken<ResponseData<String>>() {
                }.getType());
                Toast.makeText(App.getInstance(), r.data, Toast.LENGTH_SHORT).show();
                errorBody.close();
            }
        } else {
            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
