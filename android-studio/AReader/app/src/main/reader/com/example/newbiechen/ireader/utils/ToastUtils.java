package com.example.newbiechen.ireader.utils;

import android.widget.Toast;

import com.lcjian.lib.areader.App;

/**
 * Created by newbiechen on 17-5-11.
 */

public class ToastUtils {

    public static void show(String msg) {
        Toast.makeText(App.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }
}
