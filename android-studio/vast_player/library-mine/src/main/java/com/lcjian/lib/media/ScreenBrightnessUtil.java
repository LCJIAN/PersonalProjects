package com.lcjian.lib.media;

import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Window;
import android.view.WindowManager;

public class ScreenBrightnessUtil {

    /**
     * 获取当前Activity亮度
     *
     * @return A value of less than 0, the default, means to use the preferred screen brightness. 0 to 1 adjusts the brightness from dark to full bright.
     */
    public static float getBrightness(Activity activity) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        return lp.screenBrightness;
    }

    /**
     * 设置当前Activity亮度
     *
     * @param activity   当前activity
     * @param brightness A value of less than 0, the default, means to use the preferred screen brightness. 0 to 1 adjusts the brightness from dark to full bright.
     */
    public static void setBrightness(Activity activity, float brightness) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness;
        window.setAttributes(lp);
    }

    /**
     * 设置当前Activity亮度
     *
     * @param activity   当前activity
     * @param percentage 百分比（0~100）
     */
    public static void setBrightness(Activity activity, int percentage) {
        float brightness = percentage / 100f;
        setBrightness(activity, brightness);
    }

    /**
     * 获取屏幕的亮度
     *
     * @return The screen backlight brightness between 0 and 255.
     */
    public static int getSystemBrightness(ContentResolver contentResolver) {
        int nowBrightnessValue = 0;
        try {
            nowBrightnessValue = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    /**
     * 设置系统亮度,如果开启了自动调节功能了，将没有效果
     *
     * @param brightness The screen backlight brightness between 0 and 255.
     */
    public static void setSystemBrightness(ContentResolver contentResolver, int brightness) {
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    /**
     * 判断是否开启了自动亮度调节
     */
    public static boolean isAutoBrightness(ContentResolver contentResolver) {
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }

    /**
     * 停止亮度自动调节
     */
    public static void stopAutoBrightness(ContentResolver contentResolver) {
        Settings.System.putInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * 开启亮度自动调节
     */
    public static void startAutoBrightness(ContentResolver contentResolver) {
        Settings.System.putInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }
}
