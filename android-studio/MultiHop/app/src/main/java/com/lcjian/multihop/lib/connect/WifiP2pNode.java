package com.lcjian.multihop.lib.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;

public abstract class WifiP2pNode<L extends WifiP2pNode.Listener> {

    protected WifiP2pManager wifiP2pManager;
    protected WifiP2pManager.Channel channel;
    private Context context;
    private WifiP2pBroadcastReceiver wifiP2pBroadcastReceiver;
    private CopyOnWriteArrayList<L> listeners;

    private boolean started;

    public WifiP2pNode(@NonNull Context context,
                       @NonNull WifiP2pManager wifiP2pManager,
                       @NonNull WifiP2pManager.Channel channel) {
        this.context = context;
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.listeners = new CopyOnWriteArrayList<>();
        this.wifiP2pBroadcastReceiver = new WifiP2pBroadcastReceiver(wifiP2pManager, channel, listeners);
    }

    public void start() {
        if (!started) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
            context.registerReceiver(wifiP2pBroadcastReceiver, intentFilter);
            started = true;
        }
    }

    public void stop() {
        if (started) {
            context.unregisterReceiver(wifiP2pBroadcastReceiver);
            started = false;
        }
    }

    public void addListener(L listener) {
        listeners.add(listener);
    }

    public void removeListener(L listener) {
        listeners.remove(listener);
    }

    public interface Listener {

        void onWifiP2pEnabled(boolean enabled);

        void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo);

        void onConnectionInfoUnavailable();

        void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice);

        void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList);
    }

    public final class WifiP2pBroadcastReceiver extends BroadcastReceiver {

        private final WifiP2pManager mWifiP2pManager;

        private final WifiP2pManager.Channel mChannel;

        private final CopyOnWriteArrayList<? extends Listener> mListeners;

        public WifiP2pBroadcastReceiver(@NonNull WifiP2pManager wifiP2pManager,
                                        @NonNull WifiP2pManager.Channel channel,
                                        @NonNull CopyOnWriteArrayList<? extends Listener> listeners) {
            mWifiP2pManager = wifiP2pManager;
            mChannel = channel;
            mListeners = listeners;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    // 用于指示 Wifi P2P 是否可用
                    case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
                        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                        for (Listener listener : mListeners) {
                            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                                listener.onWifiP2pEnabled(true);
                            } else {
                                listener.onWifiP2pEnabled(false);
                            }
                        }
                        break;
                    }
                    // 对等节点列表发生了变化
                    case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                        mWifiP2pManager.requestPeers(mChannel, peers -> {
                            for (Listener listener : mListeners) {
                                listener.onPeersAvailable(peers.getDeviceList());
                            }
                        });
                        break;
                    }
                    // Wifi P2P 的连接状态发生了改变
                    case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                        if (networkInfo.isConnected() && networkInfo.getTypeName().equals("WIFI_P2P")) {
                            mWifiP2pManager.requestConnectionInfo(mChannel, info -> {
                                for (Listener listener : mListeners) {
                                    listener.onConnectionInfoAvailable(info);
                                }
                            });
                        } else {
                            for (Listener listener : mListeners) {
                                listener.onConnectionInfoUnavailable();
                            }
                        }
                        break;
                    }
                    //本设备的设备信息发生了变化
                    case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: {
                        for (Listener listener : mListeners) {
                            listener.onSelfDeviceAvailable(intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
                        }
                        break;
                    }
                }
            }
        }
    }
}
