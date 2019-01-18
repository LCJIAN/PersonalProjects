package com.lcjian.osc.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.osc.App;
import com.lcjian.osc.Constants;
import com.lcjian.osc.R;
import com.lcjian.osc.data.network.entity.SignInInfo;
import com.lcjian.osc.ui.base.BaseActivity;
import com.lcjian.osc.util.Spans;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class UserSignInActivity extends BaseActivity implements View.OnClickListener, TextWatcher {

    @BindView(R.id.tv_sign_in_by_id)
    TextView tv_sign_in_by_id;
    @BindView(R.id.tv_sign_in_by_name)
    TextView tv_sign_in_by_name;
    @BindView(R.id.et_get_api_url_url)
    EditText et_get_api_url_url;
    @BindView(R.id.et_sign_in_id)
    EditText et_sign_in_id;
    @BindView(R.id.et_sign_in_id_pwd)
    EditText et_sign_in_pwd;
    @BindView(R.id.et_sign_in_name)
    EditText et_sign_in_name;
    @BindView(R.id.et_sign_in_name_pwd)
    EditText et_sign_in_name_pwd;

    @BindView(R.id.chb_remember_sign_in)
    CheckBox chb_remember_sign_in;
    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    private int mSignInType = 1;

    private Disposable mDisposableP;
    private Disposable mDisposableSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_in);
        ButterKnife.bind(this);

        boolean autoSignIn = mSettingSp.getBoolean("auto_sign_in", true);
        boolean rememberMe = mSettingSp.getBoolean("remember_me", false);
        String getApiUrlHost = mSettingSp.getString("get_api_url_host", "");

        int signInType = mUserInfoSp.getInt("sign_in_type", 0);
        String signInId = mUserInfoSp.getString("sign_in_id", "");
        String signInIdPwd = mUserInfoSp.getString("sign_in_id_pwd", "");
        String signInName = mUserInfoSp.getString("sign_in_name", "");
        String signInNamePwd = mUserInfoSp.getString("sign_in_name_pwd", "");

        tv_sign_in_by_id.setOnClickListener(this);
        tv_sign_in_by_name.setOnClickListener(this);
        btn_sign_in.setOnClickListener(this);
        et_sign_in_id.addTextChangedListener(this);
        et_sign_in_pwd.addTextChangedListener(this);
        et_sign_in_name.addTextChangedListener(this);
        et_sign_in_name_pwd.addTextChangedListener(this);

        et_sign_in_id.setText(signInId);
        et_sign_in_pwd.setText(signInIdPwd);
        et_sign_in_name.setText(signInName);
        et_sign_in_name_pwd.setText(signInNamePwd);
        mSignInType = signInType;

        chb_remember_sign_in.setChecked(rememberMe);
        et_get_api_url_url.setText(getApiUrlHost);

        switchSignInType();
        validate();

        if (rememberMe && !TextUtils.isEmpty(getApiUrlHost)) {
            if (autoSignIn) {
                if (signInType == 0) {
                    if (!TextUtils.isEmpty(signInName)
                            && !TextUtils.isEmpty(signInNamePwd)) {
                        signIn(getApiUrlHost, signInName, signInNamePwd, signInType);
                    }
                } else {
                    if (!TextUtils.isEmpty(signInId)
                            && !TextUtils.isEmpty(signInIdPwd)) {
                        signIn(getApiUrlHost, signInId, signInIdPwd, signInType);
                    }
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
            case R.id.tv_sign_in_by_id:
                mSignInType = 1;
                switchSignInType();
                validate();
                break;
            case R.id.tv_sign_in_by_name:
                mSignInType = 0;
                switchSignInType();
                validate();
                break;
            case R.id.btn_sign_in:
                RxPermissions rxPermissions = new RxPermissions(this);
                mDisposableP = rxPermissions.request(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
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

    private void switchSignInType() {
        if (mSignInType == 1) {
            tv_sign_in_by_id.setText(new Spans()
                    .append(getString(R.string.sign_in_by_id),
                            new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)),
                            new StyleSpan(Typeface.BOLD)));
            tv_sign_in_by_id.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_indicator);
            tv_sign_in_by_name.setText(R.string.sign_in_by_name);
            tv_sign_in_by_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            et_sign_in_id.setVisibility(View.VISIBLE);
            et_sign_in_pwd.setVisibility(View.VISIBLE);
            et_sign_in_name.setVisibility(View.GONE);
            et_sign_in_name_pwd.setVisibility(View.GONE);
        } else {
            tv_sign_in_by_id.setText(R.string.sign_in_by_id);
            tv_sign_in_by_id.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            tv_sign_in_by_name.setText(new Spans()
                    .append(getString(R.string.sign_in_by_name),
                            new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)),
                            new StyleSpan(Typeface.BOLD)));
            tv_sign_in_by_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_indicator);
            et_sign_in_id.setVisibility(View.GONE);
            et_sign_in_pwd.setVisibility(View.GONE);
            et_sign_in_name.setVisibility(View.VISIBLE);
            et_sign_in_name_pwd.setVisibility(View.VISIBLE);
        }
    }

    private void validate() {
        btn_sign_in.setEnabled(!TextUtils.isEmpty(et_get_api_url_url.getText())
                && ((mSignInType == 0 && !TextUtils.isEmpty(et_sign_in_name.getEditableText()) && !TextUtils.isEmpty(et_sign_in_name_pwd.getEditableText()))
                || (mSignInType == 1 && !TextUtils.isEmpty(et_sign_in_id.getEditableText()) && !TextUtils.isEmpty(et_sign_in_pwd.getEditableText()))));
    }

    private void signIn(String getApiUrlHost, String sign, String pwd, int type) {
        showProgress();
        Global.GET_API_URL_URL = "http://" + getApiUrlHost + "/";
        mDisposableSignIn = mRestAPI.urlService()
                .getApiUrl()
                .map(responseBody -> {
                    Global.API_URL = responseBody.string() + "/";
                    return Global.API_URL;
                })
                .flatMap((Function<String, SingleSource<SignInInfo>>) s -> mRestAPI.cloudService().signIn(sign, pwd, type))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                            hideProgress();
                            if (objectResponseData.deviceInfo != null
                                    || objectResponseData.userInfo != null) {
                                putSignInInfo(objectResponseData);
                                startActivity(new Intent(UserSignInActivity.this, MainActivity.class));
                                PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, Constants.BAIDU_PUSH_KEY);
                                PushManager.setTags(UserSignInActivity.this,
                                        Collections.singletonList(objectResponseData.deviceInfo != null
                                                ? "D" + objectResponseData.deviceInfo.deviceID
                                                : "U" + objectResponseData.userInfo.userID));
                                finish();
                            } else {
                                Toast.makeText(App.getInstance(), R.string.error_id_or_pwd, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void signIn() {
        String getApiUrlHost = et_get_api_url_url.getEditableText().toString();
        String signInId = et_sign_in_id.getEditableText().toString();
        String signInPwd = et_sign_in_pwd.getEditableText().toString();
        String signInName = et_sign_in_name.getEditableText().toString();
        String signInNamePwd = et_sign_in_name_pwd.getEditableText().toString();
        int signInType = mSignInType;
        boolean rememberMe = chb_remember_sign_in.isChecked();

        mSettingSp.edit()
                .putString("get_api_url_host", getApiUrlHost)
                .putBoolean("remember_me", rememberMe)
                .putBoolean("auto_sign_in", true)
                .apply();
        if (rememberMe) {
            if (signInType == 0) {
                mUserInfoSp.edit()
                        .putString("sign_in_name", signInName)
                        .putString("sign_in_name_pwd", signInNamePwd)
                        .putInt("sign_in_type", signInType)
                        .apply();
            } else {
                mUserInfoSp.edit()
                        .putString("sign_in_id", signInId)
                        .putString("sign_in_id_pwd", signInPwd)
                        .putInt("sign_in_type", signInType)
                        .apply();
            }
        }
        signIn(getApiUrlHost,
                signInType == 0 ? signInName : signInId,
                signInType == 0 ? signInNamePwd : signInPwd,
                signInType);
    }
}
