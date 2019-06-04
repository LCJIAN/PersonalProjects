package com.lcjian.multihop.lib;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class Manager {

    private final int STATUS_IDLE = 0;
    private final int STATUS_GO = 1;
    private final int STATUS_GC = 2;
    private int status = STATUS_IDLE;

    private Logger logger;
    private Disposable disposable1;
    private Disposable disposable2;

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

    private boolean hasTask;

    private TaskRunner.OnTaskListener onTaskListener = new TaskRunner.OnTaskListener() {

        @Override
        public void onNoTask() {
            RxBus.getInstance().send("on_no_task");
        }

        @Override
        public void onAddTask() {
            RxBus.getInstance().send("on_add_task");
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
            if (wifiP2pInfo.groupOwnerAddress != null) {
                taskRunner.resume(wifiP2pInfo.groupOwnerAddress.getHostAddress(), port);
            }
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
                if (hasTask) {
                    boolean needGC = true;
                    for (WifiP2pDevice d : wifiP2pDeviceList) {
                        if (d.status == WifiP2pDevice.CONNECTED) {
                            needGC = false;
                            break;
                        }
                    }
                    if (needGC) {
                        switchToGC();
                    }
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
        disposable1 = RxBus.getInstance().asFlowable()
                .filter(o -> o instanceof String)
                .debounce(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                            if (TextUtils.equals(o.toString(), "on_no_task")) {
                                hasTask = false;
                                logger.info("onNoTask");
                                if (role == Role.FORWARDER) {
                                    switchToGO();
                                } else if (role == Role.SENDER) {
                                    stopGC();
                                }
                            }
                        },
                        throwable -> {
                        });
        disposable2 = RxBus.getInstance().asFlowable()
                .filter(o -> o instanceof String)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                            if (TextUtils.equals(o.toString(), "on_add_task")) {
                                hasTask = true;
                                logger.info("onAddTask");
                                if (role == Role.SENDER) {
                                    startGC();
                                }
                            }
                        },
                        throwable -> {
                        });


        if (role == Role.SENDER || role == Role.FORWARDER) {
            taskRunner = new TaskRunner();
            taskRunner.addOnNoTaskListener(onTaskListener);
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
            switchToGC();
        } else if (role == Role.FORWARDER
                || role == Role.RECEIVER) {
            switchToGO();
        }
    }

    public void addTask(Task task) {
        taskRunner.addTask(task);
    }

    public void stop() {
        if (taskRunner != null) {
            taskRunner.removeOnNoTaskListener(onTaskListener);
            taskRunner.stop();
        }
        if (httpServer != null) {
            httpServer.removeOnReceivedListener(onReceivedListener);
            httpServer.stop();
        }
        stopGC();
        stopGO();
        if (disposable1 != null) {
            disposable1.dispose();
        }
        if (disposable2 != null) {
            disposable2.dispose();
        }
    }

    private void switchToGO() {
        stopGC();
        startGO();
        logger.info("switchToGO");
    }

    private void switchToGC() {
        stopGO();
        startGC();
        logger.info("switchToGC");
    }

    private void startGC() {
        if (status != STATUS_GC) {
            wifiP2pGC.addListener(gcListenerAdapter);
            wifiP2pGC.start();
            wifiP2pGC.discover();
            status = STATUS_GC;
            logger.info("startGC");
        }
    }

    private void stopGC() {
        if (status == STATUS_GC) {
            wifiP2pGC.stopDiscover();
            wifiP2pGC.disconnect();
            wifiP2pGC.stop();
            wifiP2pGC.removeListener(gcListenerAdapter);
            status = STATUS_IDLE;
            logger.info("stopGC");
        }
    }

    private void startGO() {
        if (status != STATUS_GO) {
            wifiP2pGO.addListener(goListenerAdapter);
            wifiP2pGO.start();
            wifiP2pGO.createGroup();
            status = STATUS_GO;
            logger.info("startGO");
        }
    }

    private void stopGO() {
        if (status == STATUS_GO) {
            wifiP2pGO.removeGroup();
            wifiP2pGO.stop();
            wifiP2pGO.removeListener(goListenerAdapter);
            status = STATUS_IDLE;
            logger.info("stopGO");
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
