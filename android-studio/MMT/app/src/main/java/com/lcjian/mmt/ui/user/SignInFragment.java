package com.lcjian.mmt.ui.user;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.network.entity.SignInInfo;
import com.lcjian.mmt.ui.base.BaseFragment;
import com.lcjian.mmt.ui.main.MainActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SignInFragment extends BaseFragment implements TextWatcher, View.OnClickListener {

    @BindView(R.id.et_phone)
    TextInputEditText et_phone;
    @BindView(R.id.et_password)
    TextInputEditText et_password;
    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;
    @BindView(R.id.tv_forgot_password)
    TextView tv_forgot_password;

    private Unbinder mUnBinder;

    private Disposable mDisposableSignIn;
    private Disposable mDisposableP;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        boolean autoSignIn = mSettingSp.getBoolean("auto_sign_in", true);
        boolean rememberMe = mSettingSp.getBoolean("remember_me", true);

        String signInPhone = mUserInfoSp.getString("sign_in_phone", "");
        String signInPwd = mUserInfoSp.getString("sign_in_pwd", "");

        btn_sign_in.setOnClickListener(this);
        tv_forgot_password.setOnClickListener(this);
        et_phone.addTextChangedListener(this);
        et_password.addTextChangedListener(this);

        et_phone.setText(signInPhone);
        et_password.setText(signInPwd);

        validate();

        if (rememberMe) {
            if (autoSignIn) {
                SignInInfo signInInfo = getSignInInfo();
                if (signInInfo != null) {
                    signIn(signInInfo.token);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_forgot_password:
                startActivity(new Intent(v.getContext(), ForgotPasswordActivity.class));
                break;
            case R.id.btn_sign_in:
                RxPermissions rxPermissions = new RxPermissions(getActivity());
                mDisposableP = rxPermissions.request(
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                signIn();
                            } else {
                                Toast.makeText(App.getInstance(), "no permissions", Toast.LENGTH_LONG).show();
                            }
                        });
                break;
            default:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        validate();
    }

    private void validate() {
        btn_sign_in.setEnabled(!TextUtils.isEmpty(et_phone.getEditableText())
                && !TextUtils.isEmpty(et_password.getEditableText()));
    }

    private void signIn(String phone, String pwd) {
        if (mDisposableSignIn != null) {
            mDisposableSignIn.dispose();
        }
        showProgress();
        mDisposableSignIn = mRestAPI.cloudService().signIn(phone, pwd)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(signInInfoResponseData -> {
                            hideProgress();
                            if (signInInfoResponseData.code == 1) {
                                putSignInInfo(signInInfoResponseData.data);
                                Activity activity = getActivity();
                                if (activity != null) {
                                    startActivity(new Intent(activity, MainActivity.class));
                                    activity.finish();
                                }
                            } else {
                                Toast.makeText(App.getInstance(), signInInfoResponseData.message, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void signIn(String token) {
        if (mDisposableSignIn != null) {
            mDisposableSignIn.dispose();
        }
        showProgress();
        mDisposableSignIn = mRestAPI.cloudService().signIn(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(signInInfoResponseData -> {
                            hideProgress();
                            if (signInInfoResponseData.code == 1) {
                                putSignInInfo(signInInfoResponseData.data);
                                Activity activity = getActivity();
                                if (activity != null) {
                                    startActivity(new Intent(activity, MainActivity.class));
                                    activity.finish();
                                }
                            } else {
                                Toast.makeText(App.getInstance(), signInInfoResponseData.message, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void signIn() {
        String signInPhone = et_phone.getEditableText().toString();
        String signInPwd = et_password.getEditableText().toString();
        boolean rememberMe = mSettingSp.getBoolean("remember_me", true);
        boolean autoSignIn = mSettingSp.getBoolean("auto_sign_in", true);

        mSettingSp.edit()
                .putBoolean("remember_me", rememberMe)
                .putBoolean("auto_sign_in", autoSignIn)
                .apply();
        if (rememberMe) {
            mUserInfoSp.edit()
                    .putString("sign_in_phone", signInPhone)
                    .putString("sign_in_pwd", signInPwd)
                    .apply();
        }
        signIn(signInPhone, signInPwd);
    }

    @Override
    public void onDestroyView() {
        if (mDisposableSignIn != null) {
            mDisposableSignIn.dispose();
        }
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        super.onDestroyView();
        mUnBinder.unbind();
    }

}
