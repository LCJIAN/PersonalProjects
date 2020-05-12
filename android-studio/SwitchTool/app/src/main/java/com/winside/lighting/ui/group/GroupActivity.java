package com.winside.lighting.ui.group;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.db.entity.ItemAndGroup;
import com.winside.lighting.mesh.WTaskSubGroupAddress;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.ui.base.SlimAdapter;
import com.winside.lighting.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupActivity extends BaseActivity {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.rv_device_switch_item)
    RecyclerView rv_device_switch_item;
    @BindView(R.id.btn_sub_group_address)
    Button btn_sub_group_address;

    private GroupViewModel mGroupViewModel;

    private SlimAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.bind(this);

        mGroupViewModel = ViewModelProviders
                .of(this,
                        new GroupViewModel.Factory(getIntent().getLongExtra("group_id", 0)))
                .get(GroupViewModel.class);

        tv_navigation_title.setText(getIntent().getStringExtra("group_name"));
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(v -> onBackPressed());
        btn_sub_group_address.setOnClickListener(v -> subscribeGroupAddress());

        rv_device_switch_item.setHasFixedSize(true);
        rv_device_switch_item.setLayoutManager(new LinearLayoutManager(rv_device_switch_item.getContext()));
        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<ItemAndGroup>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.group_device_switch_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<ItemAndGroup> viewHolder) {
                viewHolder.clicked(R.id.tv_remove_from_group, v ->
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("是否确定删除？")
                                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                                .setPositiveButton("确定", (dialog, which) -> {
                                    dialog.dismiss();
                                    mGroupViewModel.removeItemFromGroup(viewHolder.itemData.item);
                                })
                                .create()
                                .show());
            }

            @Override
            public void onBind(ItemAndGroup data, SlimAdapter.SlimViewHolder<ItemAndGroup> viewHolder) {
                viewHolder.text(R.id.tv_device_switch_item_name, data.deviceName + " " + data.item.name);
            }
        });
        rv_device_switch_item.setAdapter(mAdapter);

        setupContent();
    }

    private void setupContent() {
        mGroupViewModel.getDeviceSwitchItems().observe(this, deviceSwitchItems -> mAdapter.updateData(new ArrayList<>(deviceSwitchItems)));
    }

    private boolean mAllSuccess;
    private List<WTaskSubGroupAddress> mTasks;

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("unchecked")
    private void subscribeGroupAddress() {
        List<ItemAndGroup> data = (List<ItemAndGroup>) mAdapter.getData();
        if (data != null && !data.isEmpty()) {

            Map<String, List<ItemAndGroup>> map = new HashMap<>();
            for (ItemAndGroup ig : data) {
                if (map.containsKey(ig.deviceAddress)) {
                    map.get(ig.deviceAddress).add(ig);
                } else {
                    List<ItemAndGroup> list = new ArrayList<>();
                    list.add(ig);
                    map.put(ig.deviceAddress, list);
                }
            }

            mAllSuccess = true;
            CountDownLatch countDownLatch = new CountDownLatch(map.size());
            mTasks = new ArrayList<>();

            showProgress();
            mProgressDialog.setCancellable(dialog -> {
                for (WTaskSubGroupAddress wt : mTasks) {
                    wt.cancel(true);
                }
            });
            for (List<ItemAndGroup> l : map.values()) {
                List<Byte> indexes = new ArrayList<>();
                List<byte[]> groupAddresses = new ArrayList<>();
                for (ItemAndGroup ig : l) {
                    indexes.add(ig.item.index.byteValue());
                    groupAddresses.add(Utils.short_to_bb_le((short) Integer.parseInt(ig.groupAddress, 16)));
                }

                WTaskSubGroupAddress task = new WTaskSubGroupAddress(this, l.get(0).deviceAddress, groupAddresses.get(0), indexes, groupAddresses) {

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        if (!aBoolean) {
                            mAllSuccess = false;
                        }
                        countDownLatch.countDown();
                    }
                };
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                mTasks.add(task);
            }

            new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(() -> {
                    hideProgress();
                    if (mAllSuccess) {
                        Toast.makeText(App.getInstance(), "配置成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(App.getInstance(), "配置失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        }
    }
}
