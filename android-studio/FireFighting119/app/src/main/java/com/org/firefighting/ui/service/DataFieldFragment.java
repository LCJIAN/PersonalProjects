package com.org.firefighting.ui.service;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.org.firefighting.R;
import com.org.firefighting.data.network.entity.ServiceEntity;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DataFieldFragment extends BaseFragment {

    @BindView(R.id.tl_dat_field)
    TableLayout tl_dat_field;

    private Unbinder mUnBinder;

    private ServiceEntity mServiceEntity;

    public static DataFieldFragment newInstance(ServiceEntity entity) {
        DataFieldFragment fragment = new DataFieldFragment();
        Bundle args = new Bundle();
        args.putSerializable("service_entity", entity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mServiceEntity = (ServiceEntity) getArguments().getSerializable("service_entity");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resource_data_field, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(tl_dat_field.getContext());
        mServiceEntity.fields = new Gson().fromJson(mServiceEntity.params, new TypeToken<List<ServiceEntity.Field>>() {}.getType());
        for (ServiceEntity.Field field : mServiceEntity.fields) {
            TableRow row = (TableRow) inflater.inflate(R.layout.data_field_item, tl_dat_field, false);
            TextView tv_english_name = row.findViewById(R.id.tv_english_name);
            TextView tv_chinese_name = row.findViewById(R.id.tv_chinese_name);
            TextView tv_data_format = row.findViewById(R.id.tv_data_format);
            tv_english_name.setText(field.name);
            tv_chinese_name.setText(field.remarks);
            tv_data_format.setText(field.dataType);

            tl_dat_field.addView(row);
        }
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }
}
