package com.org.firefighting.ui.service;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.R;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.entity.ServiceEntity;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ServiceBasicInfoFragment extends BaseFragment {

    @BindView(R.id.tv_resource_name)
    TextView tv_resource_name;
    @BindView(R.id.tv_apply_status)
    TextView tv_apply_status;

    @BindView(R.id.tv_resource_identity)
    TextView tv_resource_identity;
    @BindView(R.id.tv_share_type)
    TextView tv_share_type;
    @BindView(R.id.tv_supplier)
    TextView tv_supplier;
    @BindView(R.id.tv_publish_time)
    TextView tv_publish_time;
    @BindView(R.id.tv_des)
    TextView tv_des;
    @BindView(R.id.tv_tag)
    TextView tv_tag;

    private Unbinder mUnBinder;

    private ServiceEntity mServiceEntity;

    public static ServiceBasicInfoFragment newInstance(ServiceEntity entity) {
        ServiceBasicInfoFragment fragment = new ServiceBasicInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_resource_basic_info, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_apply_status.setOnClickListener(v -> ApplyStatusFragment.newInstance(mServiceEntity)
                .show(getChildFragmentManager(), "ApplyStatusFragment"));
        setupContent();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }

    private void setupContent() {
        Map<String, String> map = SharedPreferencesDataSource.getSignInResponse().setting;
        tv_resource_name.setText(mServiceEntity.serviceName);
        tv_resource_identity.setText(TextUtils.isEmpty(mServiceEntity.dirId) ? "暂无" : mServiceEntity.dirId);
        tv_share_type.setText(map == null ? "开放" : map.get(mServiceEntity.contentType));
        tv_supplier.setText(TextUtils.isEmpty(mServiceEntity.serviceDeveloper) ? "暂无" : mServiceEntity.serviceDeveloper);
        tv_publish_time.setText(mServiceEntity.createDate);
        tv_des.setText(mServiceEntity.description);
        tv_tag.setText(mServiceEntity.remarks);

        if (1 == mServiceEntity.applyStatus) {
            tv_apply_status.setText("已申请");
            tv_apply_status.setTextColor(0xff9a9a9a);
        } else if (2 == mServiceEntity.applyStatus) {
            tv_apply_status.setText("审核通过");
            tv_apply_status.setTextColor(0xff1dab56);
        } else if (3 == mServiceEntity.applyStatus) {
            tv_apply_status.setText("审核未通过!");
            tv_apply_status.setTextColor(0xff9a9a9a);
        } else {
            tv_apply_status.setText("暂无使用权限");
            tv_apply_status.setTextColor(0xff9a9a9a);
        }
    }
}
