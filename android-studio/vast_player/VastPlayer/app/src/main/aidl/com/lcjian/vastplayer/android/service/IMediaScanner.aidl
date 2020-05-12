// IMediaScanner.aidl
package com.lcjian.vastplayer.android.service;

import com.lcjian.vastplayer.android.service.IMediaScannerCallback;

interface IMediaScanner {

    int getScanState();

    void registerCallback(IMediaScannerCallback cb);

    void unregisterCallback(IMediaScannerCallback cb);

}
