// INotifier.aidl
package com.lcjian.drinkwater.android.service;

import com.lcjian.drinkwater.android.service.INotifierCallback;

interface INotifier {

    String getNextNotifyTime();

    void registerCallback(INotifierCallback cb);

    void unregisterCallback(INotifierCallback cb);

}
