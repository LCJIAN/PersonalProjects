package com.lcjian.lib.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityChangeHelper {

    private boolean mRegistered = false;

    private Context mContext;

    private OnConnectivityChangeListener mChangeListener;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager cm = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        if (mChangeListener != null) {
                            mChangeListener.onMobileAvailable();
                        }
                    } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (mChangeListener != null) {
                            mChangeListener.onWiFiAvailable();
                        }
                    }
                } else {
                    if (mChangeListener != null) {
                        mChangeListener.onNetworkUnAvailable();
                    }
                }
            }
        }
    };

    public ConnectivityChangeHelper(Context context,
                                    OnConnectivityChangeListener changeListener) {
        super();
        this.mContext = context;
        this.mChangeListener = changeListener;
    }

    public void registerReceiver() {
        if (!mRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mContext.registerReceiver(mReceiver, filter);
            mRegistered = true;
        }
    }

    public void unregisterReceiver() {
        if (mRegistered) {
            mContext.unregisterReceiver(mReceiver);
            mRegistered = false;
        }
    }

    public interface OnConnectivityChangeListener {
        public void onNetworkUnAvailable();

        public void onWiFiAvailable();

        public void onMobileAvailable();
    }
}
