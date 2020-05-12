package com.winside.lighting.ui.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.db.entity.DeviceSwitchItemGroup;
import com.winside.lighting.ui.base.BaseDialogFragment;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AddGroupFragment extends BaseDialogFragment {

    @BindView(R.id.et_group_name)
    EditText et_group_name;
    @BindView(R.id.et_group_address)
    EditText et_group_address;
    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    private Unbinder unbinder;

    private DeviceSwitchItemGroup mGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_group, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (mGroup != null) {
            et_group_name.setText(mGroup.name);
            et_group_address.setText(mGroup.address);
        }

        btn_confirm.setOnClickListener(v -> {
            String groupName = et_group_name.getEditableText().toString();
            String groupAddress = et_group_address.getEditableText().toString();
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(App.getInstance(), "分组名字不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(groupAddress)) {
                Toast.makeText(App.getInstance(), "分组地址不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mGroup == null) {
                DeviceSwitchItemGroup group = new DeviceSwitchItemGroup();
                group.name = groupName;
                group.address = groupAddress;
                group.timeAdded = new Date();
                ViewModelProviders.of(this).get(GroupsViewModel.class).addGroup(group);
            } else {
                mGroup.name = groupName;
                mGroup.address = groupAddress;
                ViewModelProviders.of(this).get(GroupsViewModel.class).updateGroup(mGroup);
            }
            dismiss();
        });
    }

    AddGroupFragment setGroup(DeviceSwitchItemGroup group) {
        mGroup = group;
        return this;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}