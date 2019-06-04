package com.lcjian.cloudlocation.ui.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PwdModifyActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.et_old_pwd)
    EditText et_old_pwd;
    @BindView(R.id.et_new_pwd)
    EditText et_new_pwd;
    @BindView(R.id.et_new_pwd_confirm)
    EditText et_new_pwd_confirm;
    @BindView(R.id.btn_modify)
    Button btn_modify;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwd_modify);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.pwd_modify));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_modify.setOnClickListener(v -> {
            if (!TextUtils.equals(et_new_pwd.getEditableText(), et_new_pwd_confirm.getEditableText())) {
                Toast.makeText(App.getInstance(), R.string.error_confirm_pwd, Toast.LENGTH_SHORT).show();
                return;
            }
            showProgress();
            mDisposable = Single.defer(() -> Single.just(getSignInInfo()))
                    .flatMap(t -> {
                        if (t.userInfo == null) {
                            return mRestAPI.cloudService().updateDevicePwd(
                                    Long.parseLong(t.deviceInfo.deviceID),
                                    et_old_pwd.getEditableText().toString(),
                                    et_new_pwd.getEditableText().toString());
                        } else {
                            return mRestAPI.cloudService().updateUserPwd(
                                    Long.parseLong(t.userInfo.userID),
                                    et_old_pwd.getEditableText().toString(),
                                    et_new_pwd.getEditableText().toString());
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(state -> {
                                hideProgress();
                                if (TextUtils.equals("2005", state.state)) {
                                    Toast.makeText(App.getInstance(), R.string.password_modification_succeeded, Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(App.getInstance(), R.string.failed_to_modify_pwd, Toast.LENGTH_SHORT).show();
                                }
                            },
                            throwable -> hideProgress());
        });
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
        btn_modify.setEnabled(!TextUtils.isEmpty(et_old_pwd.getEditableText())
                && !TextUtils.isEmpty(et_new_pwd.getEditableText())
                && !TextUtils.isEmpty(et_new_pwd_confirm.getEditableText()));
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }
}
