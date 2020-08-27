package com.org.firefighting.ui.resource;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.R;
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.ui.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ResourceBasicInfoFragment extends BaseFragment {

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

    private ResourceEntity mResourceEntity;

    public static ResourceBasicInfoFragment newInstance(ResourceEntity entity) {
        ResourceBasicInfoFragment fragment = new ResourceBasicInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_resource_basic_info, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_apply_status.setOnClickListener(v -> ApplyStatusFragment.newInstance(mResourceEntity)
                .show(getChildFragmentManager(), "ApplyStatusFragment"));
        setupContent();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }

    private void setupContent() {
        tv_resource_name.setText(mResourceEntity.shareXxzymc);
        tv_resource_identity.setText(TextUtils.isEmpty(mResourceEntity.shareXxzybh) ? "暂无" : mResourceEntity.shareXxzybh);
        tv_share_type.setText(mResourceEntity.permission);
        tv_supplier.setText(TextUtils.isEmpty(mResourceEntity.unitName) ? "暂无" : mResourceEntity.unitName);
        tv_publish_time.setText(mResourceEntity.createDate);
        tv_des.setText(mResourceEntity.shareXxzyzy);
        tv_tag.setText(mResourceEntity.tableLabel);

        if (TextUtils.equals("1", mResourceEntity.applyStatus)) {
            tv_apply_status.setText("已申请");
            tv_apply_status.setTextColor(0xff9a9a9a);
        } else if (TextUtils.equals("2", mResourceEntity.applyStatus)) {
            tv_apply_status.setText("审核通过");
            tv_apply_status.setTextColor(0xff1dab56);
        } else if (TextUtils.equals("3", mResourceEntity.applyStatus)) {
            tv_apply_status.setText("审核未通过!");
            tv_apply_status.setTextColor(0xff9a9a9a);
        } else {
            tv_apply_status.setText("暂无使用权限");
            tv_apply_status.setTextColor(0xff9a9a9a);
        }
    }
}
