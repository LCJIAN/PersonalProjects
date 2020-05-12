package com.winside.lighting.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.data.network.SignInData;
import com.winside.lighting.data.network.TokenData;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.ui.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.et_user_name)
    EditText et_user_name;
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

        tv_navigation_title.setText(R.string.sign_in);
        btn_sign_in.setOnClickListener(this);

        tryToAutoSignIn();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_sign_in) {
            RxPermissions rxPermissions = new RxPermissions(this);
            mDisposableP = rxPermissions
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(granted -> {
                        if (granted) {
                            signIn();
                        } else {
                            finish();
                        }
                    });
        }
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

    private void tryToAutoSignIn() {
        String token = SharedPreferencesDataSource.getToken();
        if (!TextUtils.isEmpty(token)) {
            autoSignIn(token);
        }
    }

    private void signIn() {
        showProgress();
        mDisposable = RestAPI.getInstance().signInService()
                .signInRx(new SignInData(
                        et_user_name.getEditableText().toString(), et_pwd.getEditableText().toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(signInInfoResponseData -> {
                            hideProgress();
                            if (signInInfoResponseData.code == 0) {
                                SharedPreferencesDataSource.setToken(signInInfoResponseData.data);
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(App.getInstance(), signInInfoResponseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

    private void autoSignIn(String token) {
        showProgress();
        mDisposable = RestAPI.getInstance().tokenService().validate(new TokenData(token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(signInInfoResponseData -> {
                            hideProgress();
                            if (signInInfoResponseData.code == 0) {
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(App.getInstance(), signInInfoResponseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }
}
