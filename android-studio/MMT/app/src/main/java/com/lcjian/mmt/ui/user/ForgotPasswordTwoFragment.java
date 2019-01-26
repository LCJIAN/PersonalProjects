package com.lcjian.mmt.ui.user;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ForgotPasswordTwoFragment extends BaseFragment implements TextWatcher, View.OnClickListener {

    @BindView(R.id.et_password_new)
    TextInputEditText et_password_new;
    @BindView(R.id.et_password_confirm)
    TextInputEditText et_password_confirm;
    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    private Unbinder mUnBinder;

    private Disposable mDisposableSignUp;

    private String mPhone;
    private String mVerificationCode;

    public static ForgotPasswordTwoFragment newInstance(String phone, String verificationCode) {
        ForgotPasswordTwoFragment fragment = new ForgotPasswordTwoFragment();
        Bundle args = new Bundle();
        args.putString("phone", phone);
        args.putString("verification_code", verificationCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPhone = getArguments().getString("phone");
            mVerificationCode = getArguments().getString("verification_code");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password_two, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_confirm.setOnClickListener(this);
        et_password_new.addTextChangedListener(this);
        et_password_confirm.addTextChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                if (TextUtils.equals(et_password_new.getEditableText(), et_password_confirm.getEditableText())) {
                    setPassword();
                } else {
                    Toast.makeText(App.getInstance(), R.string.different_pwd, Toast.LENGTH_SHORT).show();
                }
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
        btn_confirm.setEnabled(!TextUtils.isEmpty(et_password_new.getEditableText())
                && !TextUtils.isEmpty(et_password_confirm.getEditableText()));
    }

    private void setPassword() {
        if (mDisposableSignUp != null) {
            mDisposableSignUp.dispose();
        }
        showProgress();
        mDisposableSignUp = mRestAPI.cloudService()
                .setPassword(
                        mPhone,
                        mVerificationCode,
                        et_password_new.getEditableText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            if (stringResponseData.code == 1) {
                                getActivity().finish();
                            } else {
                                hideProgress();
                                Toast.makeText(App.getInstance(), stringResponseData.message, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> hideProgress());
    }
}
