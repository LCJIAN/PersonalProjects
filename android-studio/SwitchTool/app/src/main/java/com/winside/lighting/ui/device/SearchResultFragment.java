package com.winside.lighting.ui.device;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winside.lighting.R;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.base.SlimAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SearchResultFragment extends BaseFragment {

    @BindView(R.id.tv_search_result)
    TextView tv_search_result;
    @BindView(R.id.rv_search_result)
    RecyclerView rv_search_result;
    @BindView(R.id.btn_add_device)
    Button btn_add_device;

    private Unbinder unbinder;

    private SlimAdapter mAdapter;

    private AddDeviceViewModel mAddDeviceViewModel;

    private List<BluetoothDevice> mChecked;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAddDeviceViewModel = ViewModelProviders.of(getActivity()).get(AddDeviceViewModel.class);

        rv_search_result.setHasFixedSize(true);
        rv_search_result.setLayoutManager(new GridLayoutManager(rv_search_result.getContext(), 2));
        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<BluetoothDevice>() {

            @Override
            public int onGetLayoutResource() {
                return R.layout.search_result_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<BluetoothDevice> viewHolder) {
                viewHolder.clicked(v -> mAddDeviceViewModel.toggle(viewHolder.itemData));
            }

            @Override
            public void onBind(BluetoothDevice data, SlimAdapter.SlimViewHolder<BluetoothDevice> viewHolder) {
                viewHolder.text(R.id.tv_device_name, data.getName())
                        .background(R.id.ll_search_result, mChecked == null || !mChecked.contains(data) ? R.drawable.shape_device_unchecked_bg : R.drawable.shape_device_checked_bg);
            }
        });
        rv_search_result.setAdapter(mAdapter);
        btn_add_device.setOnClickListener(v -> {
            mAddDeviceViewModel.saveDevices();
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        setupContent();
    }

    private void setupContent() {
        mAddDeviceViewModel.getBluetoothDevices().observe(this, bluetoothDevices -> {
            if (bluetoothDevices.isEmpty()) {
                tv_search_result.setText(R.string.no_search_result);
            } else {
                tv_search_result.setText(R.string.search_result);
            }
            mAdapter.updateData(new ArrayList<>(bluetoothDevices));
        });
        mAddDeviceViewModel.getCheckedBluetoothDevices().observe(this, bluetoothDevices -> {
            mChecked = bluetoothDevices;
            mAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}