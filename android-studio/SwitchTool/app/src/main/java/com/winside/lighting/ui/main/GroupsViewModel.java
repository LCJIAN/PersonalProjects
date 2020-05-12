package com.winside.lighting.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.winside.lighting.App;
import com.winside.lighting.data.db.entity.DeviceSwitchItemGroup;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GroupsViewModel extends ViewModel {

    private final MutableLiveData<List<DeviceSwitchItemGroup>> mGroups;

    private Disposable mDisposable;

    public GroupsViewModel() {
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

    void deleteGroup(DeviceSwitchItemGroup group) {
        App.getInstance().getAppDatabase().deviceSwitchItemGroupDao().delete(group);
    }

    void addGroup(DeviceSwitchItemGroup group) {
        App.getInstance().getAppDatabase().deviceSwitchItemGroupDao().insert(group);
    }

    void updateGroup(DeviceSwitchItemGroup group) {
        App.getInstance().getAppDatabase().deviceSwitchItemGroupDao().update(group);
    }
}
