package com.org.firefighting.ui.resource;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.ui.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ResourceBasicInfoFragment extends BaseFragment {

    @BindView(R.id.tv_resource_identity)
    TextView tv_resource_identity;
    @BindView(R.id.tv_resource_name)
    TextView tv_resource_name;
    @BindView(R.id.tv_share_method)
    TextView tv_share_method;
    @BindView(R.id.tv_police_category)
    TextView tv_police_category;
    @BindView(R.id.tv_supplier)
    TextView tv_supplier;
    @BindView(R.id.tv_share_range)
    TextView tv_share_range;
    @BindView(R.id.tv_share_type)
    TextView tv_share_type;
    @BindView(R.id.tv_factor)
    TextView tv_factor;
    @BindView(R.id.tv_des)
    TextView tv_des;
    @BindView(R.id.tv_download_count)
    TextView tv_download_count;

    private Unbinder mUnBinder;

    private Disposable mDisposable;

    private String mResourceId;

    public static ResourceBasicInfoFragment newInstance(String taskId) {
        ResourceBasicInfoFragment fragment = new ResourceBasicInfoFragment();
        Bundle args = new Bundle();
        args.putString("resource_id", taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResourceId = getArguments().getString("resource_id");
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
        setupContent();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        mDisposable.dispose();
        super.onDestroyView();
    }

    private void setupContent() {
        showProgress();
        mDisposable = RestAPI.getInstance().apiServiceSB2()
                .getResourceDetail(mResourceId, SharedPreferencesDataSource.getSignInResponse().user.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            tv_resource_identity.setText(responseData.data.shareXxzybh);
                            tv_resource_name.setText(responseData.data.shareXxzymc);
                            tv_share_method.setText(responseData.data.shareMethod);
                            tv_police_category.setText(responseData.data.shareXxzyflSsywjz);
                            tv_supplier.setText(responseData.data.unitName);
                            tv_share_range.setText(responseData.data.permission);
                            tv_share_type.setText(responseData.data.shareGxfwms);
                            tv_factor.setText(responseData.data.shareXxzyflSsys);
                            tv_des.setText(responseData.data.shareXxzyzy);
                            tv_download_count.setText(String.valueOf(responseData.data.download));
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

}
