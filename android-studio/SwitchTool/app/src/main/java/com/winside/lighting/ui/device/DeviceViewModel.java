package com.winside.lighting.ui.device;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.winside.lighting.App;
import com.winside.lighting.data.db.entity.ItemAndGroup;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DeviceViewModel extends ViewModel {

    private final MutableLiveData<List<ItemAndGroup>> mDeviceSwitchItems;

    private Disposable mDisposable;

    public DeviceViewModel(Long deviceId) {
        mDeviceSwitchItems = new MutableLiveData<>();
        mDisposable = App.getInstance().getAppDatabase().deviceSwitchItemDao()
                .getAllByDeviceIdRx(deviceId)
                .subscribeOn(Schedulers.io())
                .subscribe(mDeviceSwitchItems::postValue);
    }

    @Override
    protected void onCleared() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    LiveData<List<ItemAndGroup>> getDeviceSwitchItems() {
        return mDeviceSwitchItems;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final Long mDeviceId;

        public Factory(Long deviceId) {
            mDeviceId = deviceId;
        }

        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
            return (T) new DeviceViewModel(mDeviceId);
        }
    }
}
