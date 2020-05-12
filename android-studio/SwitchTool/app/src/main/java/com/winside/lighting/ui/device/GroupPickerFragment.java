package com.winside.lighting.ui.device;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.db.entity.DeviceSwitchItem;
import com.winside.lighting.data.db.entity.DeviceSwitchItemGroup;
import com.winside.lighting.ui.base.BaseDialogFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class GroupPickerFragment extends BaseDialogFragment {

    @BindView(R.id.pv_group)
    NumberPickerView pv_group;
    @BindView(R.id.tv_confirm)
    TextView tv_confirm;

    private Unbinder unbinder;

    private GroupPickerViewModel mGroupPickerViewModel;

    private DeviceSwitchItem mItem;
    private DeviceSwitchItemGroup mGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_picker, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mGroupPickerViewModel = ViewModelProviders.of(this).get(GroupPickerViewModel.class);

        pv_group.setOnValueChangedListener((picker, oldVal, newVal) -> mGroupPickerViewModel.setCurrentIndex(newVal));
        tv_confirm.setOnClickListener(v -> {
            mGroupPickerViewModel.updateItem(mItem);
            dismiss();
        });
        setupContent();
    }

    public GroupPickerFragment setData(DeviceSwitchItem item, DeviceSwitchItemGroup group) {
        mItem = item;
        mGroup = group;
        return this;
    }

    private void setupContent() {
        mGroupPickerViewModel.getGroups().observe(this, groups -> {
            if (groups.size() == 0) {
                Toast.makeText(App.getInstance(), "请先新增分组", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }
            List<String> strings = new ArrayList<>();
            for (DeviceSwitchItemGroup group : groups) {
                strings.add(group.name);
            }
            String[] a = new String[groups.size()];

            int index = 0;
            if (mGroup != null) {
                for (DeviceSwitchItemGroup group : groups) {
                    if (group.id.equals(mGroup.id)) {
                        break;
                    }
                    index++;
                }
            }
            pv_group.setDisplayedValuesAndPickedIndex(strings.toArray(a), index, true);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}