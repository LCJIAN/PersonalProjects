package com.org.firefighting.ui.service;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.ServiceEntity;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.common.ApplyFragment;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ServiceDataQueryActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;

    @BindView(R.id.fl_apply)
    CardView fl_apply;
    @BindView(R.id.tv_apply)
    TextView tv_apply;

    private ServiceEntity mServiceEntity;
    private Disposable mDisposableA;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_data_query);
        ButterKnife.bind(this);
        mServiceEntity = (ServiceEntity) getIntent().getSerializableExtra("service");
        assert mServiceEntity != null;

        tv_title.setText(mServiceEntity.serviceName);
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        fl_apply.setOnClickListener(v -> {
            if (mServiceEntity != null) {
                if (1 != mServiceEntity.applyStatus
                        && 2 != mServiceEntity.applyStatus) {
                    new ApplyFragment()
                            .setListener(this::applyService)
                            .show(getSupportFragmentManager(), "ApplyFragment");
                }
            }
        });

        setupButtons();
        if (getSupportFragmentManager().findFragmentByTag("DataQueryFragment") == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_fragment_container, DataQueryFragment.newInstance(mServiceEntity.invokeName), "DataQueryFragment")
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposableA != null) {
            mDisposableA.dispose();
        }
        super.onDestroy();
    }

    private void applyService(String name, String reason) {
        if (mServiceEntity == null) {
            return;
        }
        showProgress();
        if (mDisposableA != null) {
            mDisposableA.dispose();
        }
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("category", "service");
        map.put("applyReason", reason);
        map.put("relationId", mServiceEntity.id);
        mDisposableA = RestAPI.getInstance().apiServiceSB3()
                .applyService(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_SHORT).show();
                            if (responseData.code == 0) {
                                mServiceEntity.applyStatus = 1;
                                setupButtons();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void setupButtons() {
        if (1 == mServiceEntity.applyStatus) {
            tv_apply.setText("已申请");
            tv_apply.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_security_white_48dp, 0, 0, 0);
        } else if (2 == mServiceEntity.applyStatus) {
            tv_apply.setText("审核通过");
            tv_apply.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_security_check_white_48dp, 0, 0, 0);
        } else {
            tv_apply.setText("申请");
            tv_apply.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_security_check_white_48dp, 0, 0, 0);
        }
    }
}
