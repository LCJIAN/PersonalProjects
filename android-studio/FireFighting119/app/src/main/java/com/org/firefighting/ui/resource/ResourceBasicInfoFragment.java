package com.org.firefighting.ui.resource;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.common.ApplyFragment;

import java.util.HashMap;
import java.util.Map;

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

    @BindView(R.id.ll_button)
    LinearLayout ll_button;
    @BindView(R.id.fl_favourite)
    FrameLayout fl_favourite;
    @BindView(R.id.tv_favourite)
    TextView tv_favourite;
    @BindView(R.id.fl_apply)
    FrameLayout fl_apply;
    @BindView(R.id.tv_apply)
    TextView tv_apply;

    private Unbinder mUnBinder;

    private Disposable mDisposable;
    private Disposable mDisposableA;

    private String mResourceId;
    private ResourceEntity mResourceEntity;

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
        fl_favourite.setOnClickListener(v -> {
            if (mResourceEntity != null) {
                if (mResourceEntity.collectStatus == 0) {
                    favourite();
                } else {
                    unFavourite();
                }
            }
        });
        fl_apply.setOnClickListener(v -> {
            if (mResourceEntity != null) {
                if (!TextUtils.equals("1", mResourceEntity.applyStatus)
                        && !TextUtils.equals("2", mResourceEntity.applyStatus)) {
                    new ApplyFragment()
                            .setListener(this::applyResource)
                            .show(getChildFragmentManager(), "ApplyFragment");
                }
            }
        });

        setupButtons();
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
                            mResourceEntity = responseData.data;

                            tv_resource_identity.setText(mResourceEntity.shareXxzybh);
                            tv_resource_name.setText(mResourceEntity.shareXxzymc);
                            tv_share_method.setText(mResourceEntity.shareMethod);
                            tv_police_category.setText(mResourceEntity.shareXxzyflSsywjz);
                            tv_supplier.setText(mResourceEntity.unitName);
                            tv_share_range.setText(mResourceEntity.permission);
                            tv_share_type.setText(mResourceEntity.shareGxfwms);
                            tv_factor.setText(mResourceEntity.shareXxzyflSsys);
                            tv_des.setText(mResourceEntity.shareXxzyzy);
                            tv_download_count.setText(String.valueOf(mResourceEntity.download));

                            setupButtons();
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void setupButtons() {
        ll_button.setVisibility(mResourceEntity == null ? View.GONE : View.VISIBLE);
        if (mResourceEntity == null) {
            return;
        }

        if (mResourceEntity.collectStatus == 0) {
            tv_favourite.setText("收藏");
            tv_favourite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_white_48dp, 0, 0, 0);
        } else {
            tv_favourite.setText("已收藏");
            tv_favourite.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_fill_white_48dp, 0, 0, 0);
        }

        if (TextUtils.equals("1", mResourceEntity.applyStatus)) {
            tv_apply.setText("已申请");
            tv_apply.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_security_white_48dp, 0, 0, 0);
        } else if (TextUtils.equals("2", mResourceEntity.applyStatus)) {
            tv_apply.setText("审核通过");
            tv_apply.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_security_check_white_48dp, 0, 0, 0);
        } else {
            tv_apply.setText("申请");
            tv_apply.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_security_check_white_48dp, 0, 0, 0);
        }
    }

    private void favourite() {
        showProgress();
        if (mDisposableA != null) {
            mDisposableA.dispose();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", SharedPreferencesDataSource.getSignInResponse().user.id);
        map.put("category", "resource");
        map.put("userType", "external");
        mDisposableA = RestAPI.getInstance().apiServiceSB3()
                .favourite(mResourceId, map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_SHORT).show();
                            if (responseData.code == 0) {
                                setupContent();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void unFavourite() {
        if (mResourceEntity == null) {
            return;
        }
        showProgress();
        if (mDisposableA != null) {
            mDisposableA.dispose();
        }
        mDisposableA = RestAPI.getInstance().apiServiceSB3()
                .unFavourite(mResourceEntity.collectId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_SHORT).show();
                            if (responseData.code == 0) {
                                setupContent();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void applyResource(String name, String reason) {
        if (mResourceEntity == null) {
            return;
        }
        showProgress();
        if (mDisposableA != null) {
            mDisposableA.dispose();
        }
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("category", "resource");
        map.put("applyReason", reason);
        map.put("relationId", mResourceEntity.id);
        mDisposableA = RestAPI.getInstance().apiServiceSB3()
                .applyResource(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_SHORT).show();
                            if (responseData.code == 0) {
                                setupContent();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

}
