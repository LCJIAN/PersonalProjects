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

    private CopyOnWriteArrayList<OnTaskListener> mOnTaskListeners;

    public TaskRunner() {
        this.mAtomicIP = new AtomicReference<>();
        this.mAtomicPort = new AtomicInteger();
        this.mTasksQueue = new LinkedBlockingDeque<>();
        this.mOnTaskListeners = new CopyOnWriteArrayList<>();
        this.mTaskConsumerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    mCurrentTask = null;
                    if (mTasksQueue.isEmpty()) {
                        for (OnTaskListener listener : mOnTaskListeners) {
                            listener.onNoTask();
                        }
                    }
                    mCurrentTask = mTasksQueue.take();

                    String ip = mAtomicIP.get();
                    int port = mAtomicPort.get();

                    if (TextUtils.isEmpty(mAtomicIP.get()) || port < 0 || port > 65535) {
                        Thread.sleep(1000 * 2);
                        continue;
                    }

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
            for (OnTaskListener listener : mOnTaskListeners) {
                listener.onAddTask();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addOnNoTaskListener(OnTaskListener onTaskListener) {
        this.mOnTaskListeners.add(onTaskListener);
    }

    public void removeOnNoTaskListener(OnTaskListener onTaskListener) {
        this.mOnTaskListeners.remove(onTaskListener);
    }

    public interface OnTaskListener {
        void onNoTask();

        void onAddTask();
    }
}
