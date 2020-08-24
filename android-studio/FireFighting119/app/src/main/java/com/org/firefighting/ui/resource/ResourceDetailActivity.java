package com.org.firefighting.ui.resource;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.common.ApplyFragment;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ResourceDetailActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.btn_favourite)
    ImageButton btn_favourite;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_apply)
    AppCompatButton btn_apply;

    private Disposable mDisposable;
    private Disposable mDisposableA;
    private Disposable mDisposableR;

    private String mResourceId;
    private ResourceEntity mResourceEntity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_detail);
        ButterKnife.bind(this);
        mResourceId = getIntent().getStringExtra("resource_id");

        tv_title.setText(getIntent().getStringExtra("resource_table_comment"));
        btn_back.setOnClickListener(v -> onBackPressed());
        btn_apply.setOnClickListener(v -> RxBus.getInstance().send(new ResourceDetailActivity.ShowApplyDialogEvent()));
        btn_favourite.setOnClickListener(v -> {
            if (mResourceEntity != null) {
                if (mResourceEntity.collectStatus == 0) {
                    favourite();
                } else {
                    unFavourite();
                }
            }
        });
        mDisposableR = RxBus.getInstance()
                .asFlowable()
                .filter(o -> o instanceof ShowApplyDialogEvent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    ApplyFragment
                            .newInstance("资源名称：" + mResourceEntity.tableComment
                                    + "\n资源描述：" + (TextUtils.isEmpty(mResourceEntity.shareXxzyzy) ? "暂无" : mResourceEntity.shareXxzyzy))
                            .setListener(this::applyResource)
                            .show(getSupportFragmentManager(), "ApplyFragment");
                });


        setupContent();
    }

    @Override
    protected void onDestroy() {
        mDisposableR.dispose();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableA != null) {
            mDisposableA.dispose();
        }
        super.onDestroy();
    }

    private void setupContent() {
        showProgress();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = RestAPI.getInstance().apiServiceSB2()
                .getResourceDetail(mResourceId, SharedPreferencesDataSource.getSignInResponse().user.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            mResourceEntity = responseData.data;

                            if (mResourceEntity.collectStatus == 0) {
                                btn_favourite.setImageResource(R.drawable.favourite_btn);
                            } else {
                                btn_favourite.setImageResource(R.drawable.un_favourite_btn);
                            }

                            if (TextUtils.equals("2", mResourceEntity.applyStatus)) { // 审核通过，有权限
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fl_fragment_container_basic, ResourceBasicInfoFragment.newInstance(mResourceEntity), "ResourceBasicInfoFragment")
                                        .replace(R.id.fl_fragment_container_query, DataQueryFragment.newInstance(mResourceEntity), "DataQueryFragment")
                                        .commit();
                            } else {
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fl_fragment_container_basic, ResourceBasicInfoFragment.newInstance(mResourceEntity), "ResourceBasicInfoFragment")
                                        .replace(R.id.fl_fragment_container_field, DataFieldFragment.newInstance(mResourceEntity), "DataFieldFragment")
                                        .commit();
                            }

                            if (TextUtils.equals("1", mResourceEntity.applyStatus)) {
                                btn_apply.setVisibility(View.GONE);
                            } else if (TextUtils.equals("2", mResourceEntity.applyStatus)) {
                                btn_apply.setVisibility(View.GONE);
                            } else if (TextUtils.equals("3", mResourceEntity.applyStatus)) {
                                btn_apply.setVisibility(View.VISIBLE);
                                btn_apply.setText("重新申请");
                            } else {
                                btn_apply.setVisibility(View.VISIBLE);
                                btn_apply.setText("立即申请");
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
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
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void unFavourite() {
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
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void applyResource(String name, String reason) {
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
                            setupContent();
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    static class ShowApplyDialogEvent {
    }
}
