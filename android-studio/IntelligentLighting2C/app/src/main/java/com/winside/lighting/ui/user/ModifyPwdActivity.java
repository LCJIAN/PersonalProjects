package com.winside.lighting.ui.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ModifyPwdActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.et_old_pwd)
    EditText et_old_pwd;
    @BindView(R.id.et_new_pwd)
    EditText et_new_pwd;
    @BindView(R.id.et_new_pwd_confirm)
    EditText et_new_pwd_confirm;
    @BindView(R.id.btn_modify)
    Button btn_modify;

    private Disposable mDisposable;

    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_pwd);
        ButterKnife.bind(this);

        tv_navigation_title.setText(R.string.modify_pwd);
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(v -> onBackPressed());
        btn_modify.setOnClickListener(v -> modifyPassword());
        et_old_pwd.addTextChangedListener(this);
        et_new_pwd.addTextChangedListener(this);
        et_new_pwd_confirm.addTextChangedListener(this);

        validate();
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
        btn_modify.removeCallbacks(mRunnable);
        mRunnable = () -> btn_modify.setEnabled(!TextUtils.isEmpty(et_old_pwd.getEditableText())
                && !TextUtils.isEmpty(et_new_pwd.getEditableText())
                && !TextUtils.isEmpty(et_new_pwd_confirm.getEditableText()));
        btn_modify.postDelayed(mRunnable, 500);
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    private void modifyPassword() {
        if (!TextUtils.equals(et_new_pwd.getEditableText(), et_new_pwd_confirm.getEditableText())) {
            Toast.makeText(App.getInstance(), R.string.error_confirm_pwd, Toast.LENGTH_SHORT).show();
            return;
        }
        showProgress();
        mDisposable = RestAPI.getInstance().lightingService()
                .modifyPassword(et_old_pwd.getEditableText().toString(), et_new_pwd.getEditableText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                    hideProgress();
                    if (objectResponseData.code == 1000) {
                        Toast.makeText(App.getInstance(), R.string.password_modification_succeeded, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(App.getInstance(), objectResponseData.message, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    hideProgress();
                    Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}