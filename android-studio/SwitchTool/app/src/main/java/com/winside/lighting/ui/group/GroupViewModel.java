package com.winside.lighting.ui.group;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.winside.lighting.App;
import com.winside.lighting.data.db.entity.DeviceSwitchItem;
import com.winside.lighting.data.db.entity.ItemAndGroup;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GroupViewModel extends ViewModel {

    private final MutableLiveData<List<ItemAndGroup>> mDeviceSwitchItems;

    private Disposable mDisposable;

    public GroupViewModel(Long groupId) {
        mDeviceSwitchItems = new MutableLiveData<>();
        mDisposable = App.getInstance().getAppDatabase().deviceSwitchItemDao()
                .getAllByGroupIdRx(groupId)
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

    void removeItemFromGroup(DeviceSwitchItem item) {
        item.groupId = null;
        App.getInstance().getAppDatabase().deviceSwitchItemDao().update(item);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final Long mGroupId;

        public Factory(Long groupId) {
            mGroupId = groupId;
        }

        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
            return (T) new GroupViewModel(mGroupId);
        }
    }
}
