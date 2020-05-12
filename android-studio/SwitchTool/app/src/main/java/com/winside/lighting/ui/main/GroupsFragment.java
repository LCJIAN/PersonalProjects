package com.winside.lighting.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winside.lighting.R;
import com.winside.lighting.data.db.entity.DeviceSwitchItemGroup;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.base.SlimAdapter;
import com.winside.lighting.ui.group.GroupActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class GroupsFragment extends BaseFragment {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_right)
    ImageButton btn_right;
    @BindView(R.id.rv_group)
    RecyclerView rv_group;

    private Unbinder unbinder;

    private SlimAdapter mAdapter;

    private GroupsViewModel mGroupsViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mGroupsViewModel = ViewModelProviders.of(this).get(GroupsViewModel.class);

        tv_navigation_title.setText(R.string.action_group);
        btn_right.setVisibility(View.VISIBLE);
        btn_right.setOnClickListener(v -> new AddGroupFragment().show(getChildFragmentManager(), "AddGroupFragment"));

        rv_group.setHasFixedSize(true);
        rv_group.setLayoutManager(new GridLayoutManager(rv_group.getContext(), 2));
        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<DeviceSwitchItemGroup>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.group_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<DeviceSwitchItemGroup> viewHolder) {
                viewHolder
                        .clicked(v -> startActivity(new Intent(v.getContext(), GroupActivity.class)
                                .putExtra("group_id", viewHolder.itemData.id)
                                .putExtra("group_name", viewHolder.itemData.name)
                                .putExtra("group_address", viewHolder.itemData.address)
                        ))
                        .clicked(R.id.btn_delete, v ->
                                new AlertDialog.Builder(v.getContext())
                                        .setTitle("是否确定删除？")
                                        .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                                        .setPositiveButton("确定", (dialog, which) -> {
                                            dialog.dismiss();
                                            mGroupsViewModel.deleteGroup(viewHolder.itemData);
                                        })
                                        .create()
                                        .show())
                        .clicked(R.id.btn_edit, v -> new AddGroupFragment().setGroup(viewHolder.itemData)
                                .show(getChildFragmentManager(), "AddGroupFragment"));
            }

            @Override
            public void onBind(DeviceSwitchItemGroup data, SlimAdapter.SlimViewHolder<DeviceSwitchItemGroup> viewHolder) {
                viewHolder.text(R.id.tv_group_name, data.name)
                        .text(R.id.tv_group_address, "地址:" + data.address);
            }
        });
        rv_group.setAdapter(mAdapter);

        setupContent();
    }

    private void setupContent() {
        mGroupsViewModel.getGroups().observe(this, groups -> mAdapter.updateData(new ArrayList<>(groups)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}