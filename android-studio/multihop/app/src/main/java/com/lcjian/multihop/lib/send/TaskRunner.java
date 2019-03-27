package com.lcjian.multihop.lib.send;

import android.text.TextUtils;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TaskRunner {

    private BlockingDeque<Task> mTasksQueue;
    private Thread mTaskConsumerThread;
    private AtomicReference<String> mAtomicIP;
    private AtomicInteger mAtomicPort;

    private Task mCurrentTask;

    private CopyOnWriteArrayList<OnNoTaskListener> mOnNoTaskListeners;

    public TaskRunner() {
        this.mAtomicIP = new AtomicReference<>();
        this.mAtomicPort = new AtomicInteger();
        this.mTasksQueue = new LinkedBlockingDeque<>();
        this.mOnNoTaskListeners = new CopyOnWriteArrayList<>();
        this.mTaskConsumerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    mCurrentTask = null;

                    String ip = mAtomicIP.get();
                    int port = mAtomicPort.get();

                    if (TextUtils.isEmpty(mAtomicIP.get()) || port < 0 || port > 65535) {
                        Thread.sleep(1000 * 2);
                        continue;
                    }
                    mCurrentTask = mTasksQueue.take();
                    mCurrentTask.run(ip, port);
                } catch (Exception e) {
                    if (mCurrentTask != null) {
                        try {
                            mTasksQueue.putFirst(mCurrentTask);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                    e.printStackTrace();
                } finally {
                    if (mTasksQueue.isEmpty()) {
                        for (OnNoTaskListener listener : mOnNoTaskListeners) {
                            listener.onNoTask();
                        }
                    }
                }
            }
        });
    }

    public void start() {
        mTaskConsumerThread.start();
    }

    public void stop() {
        mTaskConsumerThread.interrupt();
    }

    public void resume(String ip, int port) {
        mAtomicIP.getAndSet(ip);
        mAtomicPort.getAndSet(port);
    }

    public void pause() {
        mAtomicIP.getAndSet("");
        mAtomicPort.getAndSet(-1);
    }

    public void addTask(Task task) {
        try {
            mTasksQueue.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addOnNoTaskListener(OnNoTaskListener onNoTaskListener) {
        this.mOnNoTaskListeners.add(onNoTaskListener);
    }

    public void removeOnNoTaskListener(OnNoTaskListener onNoTaskListener) {
        this.mOnNoTaskListeners.remove(onNoTaskListener);
    }

    public interface OnNoTaskListener {
        void onNoTask();
    }
}
