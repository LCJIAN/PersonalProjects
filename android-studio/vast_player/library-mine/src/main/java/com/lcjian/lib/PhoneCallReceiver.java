package com.lcjian.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class PhoneCallReceiver extends BroadcastReceiver {

    private static final String TAG = "PhoneCallReceiver";

    private static boolean incomingFlag = false;

    private static String incomingNumber = null;

    private static WindowManager wm;

    private static TextView tv;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 如果是拨打电话
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            incomingFlag = false;
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.i(TAG, "call OUT:" + phoneNumber);
        } else {
            // 如果是来电
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            switch (tm.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING: {
                    incomingFlag = true;// 标识当前是来电
                    incomingNumber = intent.getStringExtra("incoming_number");

                    wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                    params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
                    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.format = PixelFormat.RGBA_8888;
                    tv = new TextView(context);
                    tv.setText("这是悬浮窗口，来电号码：" + incomingNumber);
                    wm.addView(tv, params);

                    Log.i(TAG, "RINGING :" + incomingNumber);
                    break;
                }
                case TelephonyManager.CALL_STATE_OFFHOOK: {
                    if (incomingFlag) {
                        Log.i(TAG, "incoming ACCEPT :" + incomingNumber);
                    }
                    break;
                }
                case TelephonyManager.CALL_STATE_IDLE: {
                    if (incomingFlag) {
                        if (wm != null) {
                            wm.removeView(tv);
                        }
                    }
                    Log.i(TAG, "incoming IDLE");
                    break;
                }
            }
        }
    }
}
