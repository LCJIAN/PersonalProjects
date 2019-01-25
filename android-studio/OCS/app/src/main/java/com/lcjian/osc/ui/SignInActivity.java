package com.lcjian.osc.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcjian.osc.App;
import com.lcjian.osc.Global;
import com.lcjian.osc.R;
import com.lcjian.osc.data.network.entity.ResponseData;
import com.lcjian.osc.data.network.entity.SignInRequestData;
import com.lcjian.osc.ui.base.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class SignInActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    @BindView(R.id.et_api_url)
    EditText et_api_url;
    @BindView(R.id.et_sign_in_account)
    EditText et_sign_in_account;
    @BindView(R.id.et_sign_in_pwd)
    EditText et_sign_in_pwd;

    @BindView(R.id.chb_remember_sign_in)
    CheckBox chb_remember_sign_in;
    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    private Disposable mDisposableP;
    private Disposable mDisposableSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        boolean autoSignIn = mSettingSp.getBoolean("auto_sign_in", true);
        boolean rememberMe = mSettingSp.getBoolean("remember_me", false);
        String apiUrl = mSettingSp.getString("api_url", "");

        String signInAccount = mUserInfoSp.getString("sign_in_account", "");
        String signInPwd = mUserInfoSp.getString("sign_in_pwd", "");

        btn_sign_in.setOnClickListener(this);
        et_api_url.addTextChangedListener(this);
        et_sign_in_account.addTextChangedListener(this);
        et_sign_in_pwd.addTextChangedListener(this);

        et_api_url.setText(apiUrl);
        et_sign_in_account.setText(signInAccount);
        et_sign_in_pwd.setText(signInPwd);

        chb_remember_sign_in.setChecked(rememberMe);

        validate();

        if (rememberMe && !TextUtils.isEmpty(apiUrl)) {
            if (autoSignIn) {
                if (!TextUtils.isEmpty(signInAccount)
                        && !TextUtils.isEmpty(signInPwd)) {
                    signIn(apiUrl, signInAccount, signInPwd);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposableSignIn != null) {
            mDisposableSignIn.dispose();
        }
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                RxPermissions rxPermissions = new RxPermissions(this);
                mDisposableP = rxPermissions.request(
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                signIn();
                            } else {
                                finish();
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
        btn_sign_in.setEnabled(!TextUtils.isEmpty(et_api_url.getText())
                && !TextUtils.isEmpty(et_sign_in_account.getEditableText())
                && !TextUtils.isEmpty(et_sign_in_pwd.getEditableText()));
    }

    private void signIn(String apiUrl, String sign, String pwd) {
        showProgress();
        Global.API_URL = "http://" + apiUrl + "/api/";

        SignInRequestData signInRequestData = new SignInRequestData();
        signInRequestData.tenancyName = apiUrl;
        signInRequestData.usernameOrEmailAddress = sign;
        signInRequestData.password = pwd;

        mRestAPI.reset();
        if (mDisposableSignIn != null) {
            mDisposableSignIn.dispose();
        }
        mDisposableSignIn = mRestAPI.cloudService().signIn(signInRequestData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            if (stringResponseData.success) {
                                mUserInfoSp.edit().putString("token", stringResponseData.result).apply();
                                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(App.getInstance(), stringResponseData.error.details, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            if (throwable instanceof HttpException) {
                                ResponseBody errorBody = ((HttpException) throwable).response().errorBody();
                                if (errorBody != null) {
                                    ResponseData<Object> r = new Gson().fromJson(errorBody.charStream(), new TypeToken<ResponseData<Object>>() {
                                    }.getType());
                                    Toast.makeText(App.getInstance(), r.error.details, Toast.LENGTH_SHORT).show();
                                    errorBody.close();
                                }
                            } else {
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void signIn() {
        String apiUrl = et_api_url.getEditableText().toString();
        String signInAccount = et_sign_in_account.getEditableText().toString();
        String signInPwd = et_sign_in_pwd.getEditableText().toString();
        boolean rememberMe = chb_remember_sign_in.isChecked();

        mSettingSp.edit()
                .putString("api_url", apiUrl)
                .putBoolean("remember_me", rememberMe)
                .putBoolean("auto_sign_in", true)
                .apply();
        if (rememberMe) {
            mUserInfoSp.edit()
                    .putString("sign_in_account", signInAccount)
                    .putString("sign_in_pwd", signInPwd)
                    .apply();
        }
        signIn(apiUrl, signInAccount, signInPwd);
    }
}
