// ISubProcessor.aidl
package com.lcjian.vastplayer.android.service;

import com.lcjian.vastplayer.android.service.ISubProcessorCallback;

interface ISubProcessor {

    int getProcessState();

    void registerCallback(ISubProcessorCallback cb);

    void unregisterCallback(ISubProcessorCallback cb);

}