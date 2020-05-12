package com.winside.lighting.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.winside.lighting.App;
import com.winside.lighting.data.db.entity.Device;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DevicesViewModel extends ViewModel {

    private final MutableLiveData<List<Device>> mDevices;

    private Disposable mDisposable;

    public DevicesViewModel() {
        mDevices = new MutableLiveData<>();
        mDisposable = App.getInstance().getAppDatabase().deviceDao()
                .getAllRx()
                .subscribeOn(Schedulers.io())
                .subscribe(mDevices::postValue);
    }

    @Override
    protected void onCleared() {
        mDisposable.dispose();
        super.onCleared();
    }

    LiveData<List<Device>> getDevices() {
        return mDevices;
    }

    void deleteDevice(Device device) {
        App.getInstance().getAppDatabase().deviceDao().delete(device);
    }
}
