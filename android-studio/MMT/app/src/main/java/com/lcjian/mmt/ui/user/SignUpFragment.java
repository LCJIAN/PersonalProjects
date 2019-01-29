package com.lcjian.mmt.ui.user;

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
import android.widget.Toast;

import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.ui.base.BaseFragment;
import com.lcjian.mmt.ui.main.MainActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SignUpFragment extends BaseFragment implements View.OnClickListener, TextWatcher {

    @BindView(R.id.et_phone_u)
    TextInputEditText et_phone_u;
    @BindView(R.id.et_password_u)
    TextInputEditText et_password_u;
    @BindView(R.id.et_verification_code)
    TextInputEditText et_verification_code;
    @BindView(R.id.btn_sign_up)
    Button btn_sign_up;
    @BindView(R.id.btn_send_verification_code)
    Button btn_send_verification_code;

    private Unbinder mUnBinder;

    private Disposable mDisposableSignIn;
    private Disposable mDisposableSignUp;
    private Disposable mDisposableCheck;
    private Disposable mDisposableCode;
    private Disposable mDisposableCountdown;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_sign_up.setOnClickListener(this);
        btn_send_verification_code.setOnClickListener(this);

        et_phone_u.addTextChangedListener(this);
        et_password_u.addTextChangedListener(this);
        et_verification_code.addTextChangedListener(this);

        validate();
    }

    @Override
    public void onDestroyView() {
        if (mDisposableSignIn != null) {
            mDisposableSignIn.dispose();
        }
        if (mDisposableSignUp != null) {
            mDisposableSignUp.dispose();
        }
        if (mDisposableCheck != null) {
            mDisposableCheck.dispose();
        }
        if (mDisposableCode != null) {
            mDisposableCode.dispose();
        }
        if (mDisposableCountdown != null) {
            mDisposableCountdown.dispose();
        }
        super.onDestroyView();
        mUnBinder.unbind();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_up:
                checkVerificationCode();
                break;
            case R.id.btn_send_verification_code:
                sendVerificationCode();
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
        btn_sign_up.setEnabled(!TextUtils.isEmpty(et_phone_u.getEditableText())
                && !TextUtils.isEmpty(et_password_u.getEditableText())
                && !TextUtils.isEmpty(et_verification_code.getEditableText()));
        btn_send_verification_code.setEnabled(
                TextUtils.equals(getString(R.string.send_verification_code),
                        et_phone_u.getEditableText().toString())
                        && !TextUtils.isEmpty(et_verification_code.getEditableText()));
    }

    private void checkVerificationCode() {
        if (mDisposableCheck != null) {
            mDisposableCheck.dispose();
        }
        showProgress();
        mDisposableCheck = mRestAPI.cloudService()
                .checkVerificationCode(
                        et_phone_u.getEditableText().toString(),
                        et_verification_code.getEditableText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            if (stringResponseData.code == 1) {
                                signUp();
                            } else {
                                hideProgress();
                                Toast.makeText(App.getInstance(), stringResponseData.message, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> hideProgress());
    }

    private void signUp() {
        if (mDisposableSignUp != null) {
            mDisposableSignUp.dispose();
        }
        mDisposableSignUp = mRestAPI.cloudService()
                .signUp(
                        et_phone_u.getEditableText().toString(),
                        et_verification_code.getEditableText().toString(),
                        et_password_u.getEditableText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            if (stringResponseData.code == 1) {
                                signIn();
                            } else {
                                hideProgress();
                                Toast.makeText(App.getInstance(), stringResponseData.message, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> hideProgress());
    }

    private void signIn(String phone, String pwd) {
        if (mDisposableSignIn != null) {
            mDisposableSignIn.dispose();
        }
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

    private void signIn() {
        String signInPhone = et_phone_u.getEditableText().toString();
        String signInPwd = et_password_u.getEditableText().toString();
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

    private void sendVerificationCode() {
        if (mDisposableCode != null) {
            mDisposableCode.dispose();
        }
        showProgress();
        mDisposableCode = mRestAPI.cloudService().sendVerificationCode(et_phone_u.getEditableText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            if (stringResponseData.code == 1) {
                                countdown();
                            } else {
                                Toast.makeText(App.getInstance(), stringResponseData.message, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> hideProgress());
    }


    private void countdown() {
        if (mDisposableCountdown != null) {
            mDisposableCountdown.dispose();
        }
        btn_send_verification_code.setEnabled(false);
        mDisposableCountdown = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                            long left = 60 - aLong;
                            if (left < 0) {
                                btn_send_verification_code.setText(R.string.send_verification_code);
                                btn_send_verification_code.setEnabled(true);
                                mDisposableCountdown.dispose();
                            } else {
                                String ls = left + "s";
                                btn_send_verification_code.setText(ls);
                            }
                        },
                        throwable -> {
                        });
    }
}
