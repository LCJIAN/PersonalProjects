// IMediaScannerCallback.aidl
package com.lcjian.vastplayer.android.service;

// Declare any non-default types here with import statements

interface IMediaScannerCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onScanStateChanged(int newState);
}
