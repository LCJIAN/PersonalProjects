package com.org.firefighting.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.lib.util.common.PackageUtils2;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.SignInRequest;
import com.org.firefighting.data.network.entity.SignInResponse;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.android.api.JPushInterface;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SignInActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.tv_app_version)
    TextView tv_app_version;
    @BindView(R.id.et_account)
    EditText et_phone;
    @BindView(R.id.et_pwd)
    EditText et_pwd;
    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        tv_app_version.setText(new Spans()
                .append("v", new AbsoluteSizeSpan(DimenUtils.spToPixels(12, this)))
                .append(PackageUtils2.getVersionName(this)));
        et_phone.addTextChangedListener(this);
        et_pwd.addTextChangedListener(this);

        btn_sign_in.setOnClickListener(v -> signIn());
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
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
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
                    SignInResponse signInResponseL = SharedPreferencesDataSource.getSignInResponse();
                    if (signInResponseL == null) {
                        SharedPreferencesDataSource.putSignInResponse(signInResponse);
                    } else {
                        signInResponseL.token = signInResponse.token;
                        signInResponseL.user = signInResponse.user;
                        SharedPreferencesDataSource.putSignInResponse(signInResponseL);
                    }
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
