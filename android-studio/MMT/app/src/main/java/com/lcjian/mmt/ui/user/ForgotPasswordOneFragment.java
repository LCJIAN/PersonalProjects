package com.lcjian.mmt.ui.user;

import android.os.Bundle;
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
import com.lcjian.mmt.ThrowableConsumerAdapter;
import com.lcjian.mmt.ui.base.BaseFragment;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ForgotPasswordOneFragment extends BaseFragment implements TextWatcher, View.OnClickListener {


    @BindView(R.id.et_phone_f)
    AppCompatEditText et_phone_f;
    @BindView(R.id.et_verification_code_f)
    AppCompatEditText et_verification_code_f;
    @BindView(R.id.btn_next_step)
    Button btn_next_step;
    @BindView(R.id.btn_verification_code_f)
    Button btn_verification_code_f;

    private Unbinder mUnBinder;

    private Disposable mDisposableCheck;
    private Disposable mDisposableCode;
    private Disposable mDisposableCountdown;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password_one, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_next_step.setOnClickListener(this);
        btn_verification_code_f.setOnClickListener(this);
        et_phone_f.addTextChangedListener(this);
        et_verification_code_f.addTextChangedListener(this);
    }

    @Override
    public void onDestroyView() {
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
            case R.id.btn_verification_code_f:
                sendVerificationCode();
                break;
            case R.id.btn_next_step:
                checkVerificationCode();
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
        btn_next_step.setEnabled(!TextUtils.isEmpty(et_phone_f.getEditableText())
                && !TextUtils.isEmpty(et_verification_code_f.getEditableText()));
        btn_verification_code_f.setEnabled(
                TextUtils.equals(getString(R.string.send_verification_code),
                        et_phone_f.getEditableText().toString())
                        && !TextUtils.isEmpty(et_verification_code_f.getEditableText()));
    }

    private void checkVerificationCode() {
        if (mDisposableCheck != null) {
            mDisposableCheck.dispose();
        }
        showProgress();
        mDisposableCheck = mRestAPI.cloudService()
                .checkVerificationCode(
                        et_phone_f.getEditableText().toString(),
                        et_verification_code_f.getEditableText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            if (stringResponseData.code == 1) {
                                ((ForgotPasswordActivity) getActivity()).nextStep(
                                        et_phone_f.getEditableText().toString(),
                                        et_verification_code_f.getEditableText().toString());
                            } else {
                                Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void sendVerificationCode() {
        if (mDisposableCode != null) {
            mDisposableCode.dispose();
        }
        showProgress();
        mDisposableCode = mRestAPI.cloudService().sendVerificationCode(et_phone_f.getEditableText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            if (stringResponseData.code == 1) {
                                countdown();
                            } else {
                                Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }


    private void countdown() {
        if (mDisposableCountdown != null) {
            mDisposableCountdown.dispose();
        }
        btn_verification_code_f.setEnabled(false);
        mDisposableCountdown = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                            long left = 60 - aLong;
                            if (left < 0) {
                                btn_verification_code_f.setText(R.string.send_verification_code);
                                btn_verification_code_f.setEnabled(true);
                                mDisposableCountdown.dispose();
                            } else {
                                String ls = left + "s";
                                btn_verification_code_f.setText(ls);
                            }
                        },
                        throwable -> {
                        });
    }
}
