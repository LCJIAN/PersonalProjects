package com.lcjian.multihop.lib.connect;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import androidx.annotation.NonNull;

public final class WifiP2pGO extends WifiP2pNode<WifiP2pGO.GOListenerAdapter> {

    private CopyOnWriteArrayList<GOListener> goListeners;

    private GOListenerAdapter goListenerLog = new GOListenerAdapter() {

        private Logger logger = Logger.getLogger("multi-hop");

        @Override
        public void onCreateGroupFailure(int reason) {
            logger.warning("onCreateGroupFailure,reason:" + reason);
        }

        @Override
        public void onRemoveGroupFailure(int reason) {
            logger.warning("onRemoveGroupFailure,reason:" + reason);
        }

        @Override
        public void onWifiP2pEnabled(boolean enabled) {
            logger.info("onWifiP2pEnabled,enabled" + enabled);
        }

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            logger.info("onConnectionInfoAvailable," + wifiP2pInfo.toString());
        }

        @Override
        public void onConnectionInfoUnavailable() {
            logger.info("onConnectionInfoUnavailable");
        }

        @Override
        public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {
            logger.info("onSelfDeviceAvailable");
        }

        @Override
        public void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {
            StringBuilder sb = new StringBuilder("onPeersAvailable,list:");
            if (wifiP2pDeviceList == null || wifiP2pDeviceList.isEmpty()) {
                sb.append("empty");
            } else {
                for (WifiP2pDevice device : wifiP2pDeviceList) {
                    sb.append(device.deviceName).append(",");
                }
            }
            logger.info(sb.toString());
        }
    };

    public WifiP2pGO(@NonNull Context context,
                     @NonNull WifiP2pManager wifiP2pManager,
                     @NonNull WifiP2pManager.Channel channel) {
        super(context, wifiP2pManager, channel);
        this.goListeners = new CopyOnWriteArrayList<>();
    }

    public void createGroup() {
        wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                for (GOListener goListener : goListeners) {
                    goListener.onCreateGroupFailure(reason);
                }
            }
        });
    }

    public void removeGroup() {
        wifiP2pManager.requestGroupInfo(channel, group -> wifiP2pManager
                .removeGroup(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(int reason) {
                        for (GOListener goListener : goListeners) {
                            goListener.onRemoveGroupFailure(reason);
                        }
                    }
                }));
    }

    @Override
    public void start() {
        addListener(goListenerLog);
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
//        removeListener(goListenerLog);
    }

    @Override
    public void addListener(GOListenerAdapter listener) {
        super.addListener(listener);
        goListeners.add(listener);
    }

    @Override
    public void removeListener(GOListenerAdapter listener) {
        super.removeListener(listener);
        goListeners.remove(listener);
    }

    public interface GOListener extends WifiP2pNode.Listener {

        void onCreateGroupFailure(int reason);

        void onRemoveGroupFailure(int reason);

    }

    public static class GOListenerAdapter implements GOListener {

        @Override
        public void onCreateGroupFailure(int reason) {

        }

        @Override
        public void onRemoveGroupFailure(int reason) {

        }

        @Override
        public void onWifiP2pEnabled(boolean enabled) {

        }

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

        }

        @Override
        public void onConnectionInfoUnavailable() {

        }

        @Override
        public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {

        }

        @Override
        public void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {

        }
    }
}
