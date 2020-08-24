package com.org.firefighting.ui.resource;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.R;
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.ui.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DataFieldFragment extends BaseFragment {

    @BindView(R.id.tl_dat_field)
    TableLayout tl_dat_field;

    private Unbinder mUnBinder;

    private ResourceEntity mResourceEntity;

    public static DataFieldFragment newInstance(ResourceEntity entity) {
        DataFieldFragment fragment = new DataFieldFragment();
        Bundle args = new Bundle();
        args.putSerializable("resource_entity", entity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResourceEntity = (ResourceEntity) getArguments().getSerializable("resource_entity");
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
        for (ResourceEntity.Field field : mResourceEntity.fields) {
            TableRow row = (TableRow) inflater.inflate(R.layout.data_field_item, tl_dat_field, false);
            TextView tv_english_name = row.findViewById(R.id.tv_english_name);
            TextView tv_chinese_name = row.findViewById(R.id.tv_chinese_name);
            TextView tv_data_format = row.findViewById(R.id.tv_data_format);
            tv_english_name.setText(field.name);
            tv_chinese_name.setText(field.chineseName);
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
