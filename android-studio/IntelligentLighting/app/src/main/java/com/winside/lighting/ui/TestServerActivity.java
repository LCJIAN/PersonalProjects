package com.winside.lighting.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.winside.lighting.App;
import com.winside.lighting.BuildConfig;
import com.winside.lighting.R;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.util.Utils;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TestServerActivity extends BaseActivity {

    @BindView(R.id.et_server_address)
    EditText et_server_address;
    @BindView(R.id.btn_next)
    Button btn_next;

    private Disposable mDisposable;
    private Disposable mDisposableP;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_server);
        ButterKnife.bind(this);

        et_server_address.setText(BuildConfig.API_URL);
        btn_next.setOnClickListener(v -> {

            RxPermissions rxPermissions = new RxPermissions(this);
            mDisposableP = rxPermissions.request(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(granted -> {
                        if (granted) {
                            testUrl();
                        } else {
                            finish();
                        }
                    });


        });
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        super.onDestroy();
    }

    private void testUrl() {
        String serverUrl = et_server_address.getEditableText().toString();
        if (TextUtils.isEmpty(serverUrl)) {
            return;
        }
        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            serverUrl = "http://" + serverUrl;
        }
        showProgress();
        mDisposable = Single.just(serverUrl)
                .map(s -> Pair.create(s, Utils.testUrl(s)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aPair -> {
                            hideProgress();
                            if (aPair.second != null && aPair.second) {
                                RestAPI.getInstance().resetApiUrl(aPair.first);
                                startActivity(new Intent(this, SignInActivity.class));
                                finish();
                            } else {
                                Toast.makeText(App.getInstance(), "服务器地址不可用", Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

}
