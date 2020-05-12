package com.winside.lighting.ui.device;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.winside.lighting.App;
import com.winside.lighting.data.db.entity.DeviceSwitchItem;
import com.winside.lighting.data.db.entity.DeviceSwitchItemGroup;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GroupPickerViewModel extends ViewModel {

    private final MutableLiveData<List<DeviceSwitchItemGroup>> mGroups;

    private int mCurrentIndex;

    private Disposable mDisposable;

    public GroupPickerViewModel() {
        mGroups = new MutableLiveData<>();
        mDisposable = App.getInstance().getAppDatabase().deviceSwitchItemGroupDao()
                .getAllRx()
                .subscribeOn(Schedulers.io())
                .subscribe(mGroups::postValue);
    }

    @Override
    protected void onCleared() {
        mDisposable.dispose();
        super.onCleared();
    }

    LiveData<List<DeviceSwitchItemGroup>> getGroups() {
        return mGroups;
    }

    void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
    }

    void updateItem(DeviceSwitchItem item) {
        item.groupId = mGroups.getValue().get(mCurrentIndex).id;
        App.getInstance().getAppDatabase().deviceSwitchItemDao().update(item);
    }
}
