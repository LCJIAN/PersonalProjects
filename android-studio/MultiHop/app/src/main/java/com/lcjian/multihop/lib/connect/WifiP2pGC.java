package com.lcjian.multihop.lib.connect;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import androidx.annotation.NonNull;

public final class WifiP2pGC extends WifiP2pNode<WifiP2pGC.GCListenerAdapter> {

    private CopyOnWriteArrayList<GCListener> gcListeners;

    private boolean connected;
    private WifiP2pDevice connectPendingDevice;

    private GCListenerAdapter gcListenerLog = new GCListenerAdapter() {

        private Logger logger = Logger.getLogger("multi-hop");

        @Override
        public void onDiscoverFailure(int reason) {
            logger.warning("onDiscoverFailure,reason:" + reason);
        }

        @Override
        public void onStopDiscoverFailure(int reason) {
            logger.warning("onStopDiscoverFailure,reason:" + reason);
        }

        @Override
        public void onConnectFailure(int reason) {
            logger.warning("onConnectFailure:" + reason);
        }

        @Override
        public void onCancelConnectFailure(int reason) {
            logger.warning("onCancelConnectFailure,reason:" + reason);
        }

        @Override
        public void onRemoveGroupFailure(int reason) {
            logger.warning("onRemoveGroupFailure,reason:" + reason);
        }

        @Override
        public void onWifiP2pEnabled(boolean enabled) {
            logger.info("onWifiP2pEnabled,enabled:" + enabled);
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

    private GCListenerAdapter gcListenerC = new GCListenerAdapter() {

        @Override
        public void onWifiP2pEnabled(boolean enabled) {
            if (!enabled) {
                connected = false;
            }
        }

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            connected = true;
        }

        @Override
        public void onConnectionInfoUnavailable() {
            connected = false;
        }

    };

    public WifiP2pGC(@NonNull Context context,
                     @NonNull WifiP2pManager wifiP2pManager,
                     @NonNull WifiP2pManager.Channel channel) {
        super(context, wifiP2pManager, channel);
        this.gcListeners = new CopyOnWriteArrayList<>();
    }

    public void discover() {
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                for (GCListener gcListener : gcListeners) {
                    gcListener.onDiscoverFailure(reason);
                }
            }
        });
    }

    public void stopDiscover() {
        wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                for (GCListener gcListener : gcListeners) {
                    gcListener.onStopDiscoverFailure(reason);
                }
            }
        });
    }

    public void connect(@NonNull WifiP2pDevice device) {
        if (connectPendingDevice != null) {
            if (!connectPendingDevice.equals(device)) {
                disconnect();
            }
        }
        if (connectPendingDevice == null) {
            connectPendingDevice = device;
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = connectPendingDevice.deviceAddress;
            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                    connectPendingDevice = null;
                    for (GCListener gcListener : gcListeners) {
                        gcListener.onConnectFailure(reason);
                    }
                }
            });
        }
    }

    public void disconnect() {
        if (connected) {
            wifiP2pManager.requestGroupInfo(channel, group -> wifiP2pManager
                    .removeGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int reason) {
                            for (GCListener gcListener : gcListeners) {
                                gcListener.onRemoveGroupFailure(reason);
                            }
                        }
                    }));
        } else if (connectPendingDevice != null) {
            wifiP2pManager.cancelConnect(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                    for (GCListener gcListener : gcListeners) {
                        gcListener.onCancelConnectFailure(reason);
                    }
                }
            });
        }
        connectPendingDevice = null;
    }

    @Override
    public void start() {
        addListener(gcListenerC);
        addListener(gcListenerLog);
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
//        removeListener(gcListenerC);
//        removeListener(gcListenerLog);
    }

    @Override
    public void addListener(WifiP2pGC.GCListenerAdapter listener) {
        super.addListener(listener);
        gcListeners.add(listener);
    }

    @Override
    public void removeListener(WifiP2pGC.GCListenerAdapter listener) {
        super.removeListener(listener);
        gcListeners.remove(listener);
    }

    public interface GCListener extends WifiP2pNode.Listener {

        void onDiscoverFailure(int reason);

        void onStopDiscoverFailure(int reason);

        void onConnectFailure(int reason);

        void onRemoveGroupFailure(int reason);

        void onCancelConnectFailure(int reason);
    }

    public static class GCListenerAdapter implements GCListener {

        @Override
        public void onDiscoverFailure(int reason) {

        }

        @Override
        public void onStopDiscoverFailure(int reason) {

        }

        @Override
        public void onConnectFailure(int reason) {

        }

        @Override
        public void onCancelConnectFailure(int reason) {

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
