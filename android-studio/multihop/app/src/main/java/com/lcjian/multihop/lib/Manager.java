package com.lcjian.multihop.lib;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.lcjian.multihop.lib.connect.WifiP2pGC;
import com.lcjian.multihop.lib.connect.WifiP2pGO;
import com.lcjian.multihop.lib.receive.HttpServer;
import com.lcjian.multihop.lib.send.PostAudioMessageTask;
import com.lcjian.multihop.lib.send.PostTextMessageTask;
import com.lcjian.multihop.lib.send.Task;
import com.lcjian.multihop.lib.send.TaskRunner;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;


public class Manager {

    private final int STATUS_IDLE = 0;
    private final int STATUS_GO = 1;
    private final int STATUS_GC = 2;
    private int status = STATUS_IDLE;

    private Logger logger;

    private Role role;
    private int port;
    private File directory;

    private Context context;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private boolean retryChannel;

    private TaskRunner taskRunner;
    private HttpServer httpServer;

    private WifiP2pGO wifiP2pGO;
    private WifiP2pGC wifiP2pGC;

    private TaskRunner.OnNoTaskListener onNoTaskListener = () -> {
        if (role == Role.FORWARDER) {
            new Handler(Looper.getMainLooper()).post(this::go);
        }
    };

    private HttpServer.OnReceivedListener onReceivedListener = new HttpServer.OnReceivedListener() {

        @Override
        public void onTextMessageReceived(String text) {
            if (role == Role.FORWARDER) {
                addTask(new PostTextMessageTask(text));
            }
        }

        @Override
        public void onAudioMessageReceived(File audio) {
            if (role == Role.FORWARDER) {
                addTask(new PostAudioMessageTask(audio));
            }
        }
    };

    private WifiP2pGC.GCListenerAdapter gcListenerAdapter = new WifiP2pGC.GCListenerAdapter() {

        @Override
        public void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {
            WifiP2pDevice device = null;
            for (WifiP2pDevice d : wifiP2pDeviceList) {
                if (TextUtils.equals(d.deviceName, role.getNextDeviceName())) {
                    device = d;
                    break;
                }
            }
            if (device != null) {
                wifiP2pGC.connect(device);
            }
        }

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            taskRunner.resume(wifiP2pInfo.groupOwnerAddress.getHostAddress(), port);
        }

        @Override
        public void onConnectionInfoUnavailable() {
            taskRunner.pause();
        }
    };

    private WifiP2pGO.GOListenerAdapter goListenerAdapter = new WifiP2pGO.GOListenerAdapter() {

        @Override
        public void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {
            if (role == Role.FORWARDER) {
                boolean needGC = true;
                for (WifiP2pDevice d : wifiP2pDeviceList) {
                    if (d.status == WifiP2pDevice.CONNECTED) {
                        needGC = false;
                        break;
                    }
                }
                if (needGC) {
                    gc();
                }
            }
        }
    };

    private Manager() {
    }

    public static Manager getInstance() {
        return ManagerHolder.INSTANCE;
    }

    public void init(Context context, Role role, int port, File directory) {
        this.context = context;
        this.role = role;
        this.port = port;
        this.directory = directory;
        this.logger = Logger.getLogger("multi-hop");
    }

    public void start() {
        if (role == Role.SENDER || role == Role.FORWARDER) {
            taskRunner = new TaskRunner();
            taskRunner.addOnNoTaskListener(onNoTaskListener);
            taskRunner.start();
        }
        if (role == Role.FORWARDER || role == Role.RECEIVER) {
            httpServer = new HttpServer(directory, port);
            try {
                httpServer.addOnReceivedListener(onReceivedListener);
                httpServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                if (!retryChannel) {
                    retryChannel = true;
                    channel = wifiP2pManager.initialize(context, context.getMainLooper(), this);
                } else {
                    logger.warning("Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.");
                }
            }
        });
        wifiP2pGO = new WifiP2pGO(context, wifiP2pManager, channel);
        wifiP2pGC = new WifiP2pGC(context, wifiP2pManager, channel);

        if (role == Role.SENDER) {
            gc();
        } else if (role == Role.FORWARDER
                || role == Role.RECEIVER) {
            go();
        }
    }

    public void addTask(Task task) {
        taskRunner.addTask(task);
    }

    public void stop() {
        if (taskRunner != null) {
            taskRunner.removeOnNoTaskListener(onNoTaskListener);
            taskRunner.stop();
        }
        if (httpServer != null) {
            httpServer.removeOnReceivedListener(onReceivedListener);
            httpServer.stop();
        }
        if (status == STATUS_GC) {
            wifiP2pGC.stopDiscover();
            wifiP2pGC.disconnect();
            wifiP2pGC.stop();
            wifiP2pGC.removeListener(gcListenerAdapter);
        }
        if (status == STATUS_GO) {
            wifiP2pGO.removeGroup();
            wifiP2pGO.stop();
            wifiP2pGO.removeListener(goListenerAdapter);
        }
        status = STATUS_IDLE;
    }

    private void go() {
        if (status == STATUS_GC) {
            wifiP2pGC.stopDiscover();
            wifiP2pGC.disconnect();
            wifiP2pGC.stop();
            wifiP2pGC.removeListener(gcListenerAdapter);
        }
        if (status != STATUS_GO) {
            wifiP2pGO.addListener(goListenerAdapter);
            wifiP2pGO.start();
            wifiP2pGO.createGroup();

            status = STATUS_GO;
        }
    }

    private void gc() {
        if (status == STATUS_GO) {
            wifiP2pGO.removeGroup();
            wifiP2pGO.stop();
            wifiP2pGO.removeListener(goListenerAdapter);
        }
        if (status != STATUS_GC) {
            wifiP2pGC.addListener(gcListenerAdapter);
            wifiP2pGC.start();
            wifiP2pGC.discover();

            status = STATUS_GC;
        }
    }

    public WifiP2pGC getWifiP2pGC() {
        return wifiP2pGC;
    }

    public WifiP2pGO getWifiP2pGO() {
        return wifiP2pGO;
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }

    public TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public Role getRole() {
        return role;
    }

    private static class ManagerHolder {
        private static final Manager INSTANCE = new Manager();
    }
}
