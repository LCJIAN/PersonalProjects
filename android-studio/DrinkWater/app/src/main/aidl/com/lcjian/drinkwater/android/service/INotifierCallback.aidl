// INotifierCallback.aidl
package com.lcjian.drinkwater.android.service;

// Declare any non-default types here with import statements

interface INotifierCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onNextNotifyTimeChanged(String nextNotifyTime);
}
