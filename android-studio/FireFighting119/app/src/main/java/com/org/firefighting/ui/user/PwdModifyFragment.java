package com.org.firefighting.ui.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.ModifyPwdRequest;
import com.org.firefighting.data.network.entity.ResponseData;
import com.org.firefighting.data.network.entity.SignInRequest;
import com.org.firefighting.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PwdModifyFragment extends BaseDialogFragment implements TextWatcher {

    @BindView(R.id.et_old_pwd)
    EditText et_old_pwd;
    @BindView(R.id.et_new_pwd)
    EditText et_new_pwd;
    @BindView(R.id.et_new_pwd_confirm)
    EditText et_new_pwd_confirm;
    @BindView(R.id.btn_modify)
    AppCompatButton btn_modify;
    @BindView(R.id.btn_close)
    ImageButton btn_close;

    private Disposable mDisposable;

    private Unbinder mUnBinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pwd_modify, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_close.setOnClickListener(v -> dismiss());
        btn_modify.setOnClickListener(v -> {
            if (!TextUtils.equals(et_new_pwd.getEditableText(), et_new_pwd_confirm.getEditableText())) {
                Toast.makeText(App.getInstance(), R.string.error_confirm_pwd, Toast.LENGTH_SHORT).show();
                return;
            }
            showProgress();
            final ModifyPwdRequest modifyPwdRequest = new ModifyPwdRequest();
            modifyPwdRequest.oldPass = et_old_pwd.getEditableText().toString();
            modifyPwdRequest.newPass = et_new_pwd.getEditableText().toString();
            mDisposable = RestAPI.getInstance().apiServiceSB()
                    .modifyPwd(modifyPwdRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(responseBody -> {
                                hideProgress();
                                String s = responseBody.string();
                                if (TextUtils.isEmpty(s)) {
                                    SignInRequest signInRequest = SharedPreferencesDataSource.getSignInRequest();
                                    signInRequest.password = modifyPwdRequest.newPass;
                                    SharedPreferencesDataSource.putSignInRequest(signInRequest);
                                    Toast.makeText(App.getInstance(), R.string.password_modification_succeeded, Toast.LENGTH_SHORT).show();
                                    dismiss();
                                } else {
                                    ResponseData<String> r = new Gson().fromJson(s, new TypeToken<ResponseData<String>>() {}.getType());
                                    Toast.makeText(App.getInstance(), r.message, Toast.LENGTH_SHORT).show();
                                }
                            },
                            throwable -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            });
        });
        et_old_pwd.addTextChangedListener(this);
        et_new_pwd.addTextChangedListener(this);
        et_new_pwd_confirm.addTextChangedListener(this);

        validate();
    }

    @Override
    public void onDestroyView() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mUnBinder.unbind();
        super.onDestroyView();
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
        btn_modify.setEnabled(!TextUtils.isEmpty(et_old_pwd.getEditableText())
                && !TextUtils.isEmpty(et_new_pwd.getEditableText())
                && !TextUtils.isEmpty(et_new_pwd_confirm.getEditableText()));
    }
}
