package com.org.firefighting.ui.user;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.SignInRequest;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.main.MainActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.android.api.JPushInterface;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SignInActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.et_account)
    EditText et_phone;
    @BindView(R.id.et_pwd)
    EditText et_pwd;
    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    private Disposable mDisposable;
    private Disposable mDisposableP;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        et_phone.addTextChangedListener(this);
        et_pwd.addTextChangedListener(this);

        et_phone.setText(R.string.demo_user_name);
        et_pwd.setText(R.string.demo_user_pwd);
        btn_sign_in.setOnClickListener(v -> checkPermission());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        btn_sign_in.setEnabled(!TextUtils.isEmpty(et_phone.getEditableText())
                && !TextUtils.isEmpty(et_pwd.getEditableText()));
    }

    @Override
    protected void onDestroy() {
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    private void checkPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        mDisposableP = rxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.READ_PHONE_STATE)
                .subscribe(granted -> {
                    if (granted) {
                        signIn();
                    } else {
                        finish();
                    }
                });
    }

    private void signIn() {
        showProgress();
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.username = et_phone.getEditableText().toString();
        signInRequest.password = et_pwd.getEditableText().toString();
        SharedPreferencesDataSource.putSignInRequest(signInRequest);
        mDisposable = RestAPI.getInstance().apiServiceSignIn().signIn(signInRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(signInResponse -> {
                    hideProgress();
                    SharedPreferencesDataSource.putSignInResponse(signInResponse);
                    JPushInterface.resumePush(this);
                    JPushInterface.setAlias(this, signInResponse.user.id.intValue(),
                            String.valueOf(signInResponse.user.id));

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }, throwable -> {
                    hideProgress();
                    ThrowableConsumerAdapter.accept(throwable);
                });
    }
}
