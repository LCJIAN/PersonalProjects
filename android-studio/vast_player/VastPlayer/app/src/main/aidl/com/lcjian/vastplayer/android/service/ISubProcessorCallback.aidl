// ISubProcessorCallback.aidl
package com.lcjian.vastplayer.android.service;

// Declare any non-default types here with import statements

interface ISubProcessorCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void onProcessStateChanged(int newState);

     void onSubReady(String subFile);
}
