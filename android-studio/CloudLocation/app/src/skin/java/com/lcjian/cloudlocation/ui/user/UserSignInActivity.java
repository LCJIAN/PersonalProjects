package com.lcjian.cloudlocation.ui.user;

import android.Manifest;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.franmontiel.localechanger.LocaleChanger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.BuildConfig;
import com.lcjian.cloudlocation.Constants;
import com.lcjian.cloudlocation.Global;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.home.MainActivity;
import com.lcjian.cloudlocation.util.Spans;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Collections;
import java.util.Locale;

import androidx.appcompat.widget.AppCompatSpinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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
    @BindView(R.id.sp_sign_in_map)
    AppCompatSpinner sp_sign_in_map;

    @BindView(R.id.chb_remember_sign_in)
    CheckBox chb_remember_sign_in;
    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    private int mSignInType;
    private String mSignInMap;

    private Disposable mDisposableP;
    private Disposable mDisposableSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_in);
        ButterKnife.bind(this);

        boolean autoSignIn = mSettingSp.getBoolean("auto_sign_in", true);
        boolean rememberMe = mSettingSp.getBoolean("remember_me", false);
        String getApiUrlHost = mSettingSp.getString("get_api_url_host", BuildConfig.GET_API_URL);

        int signInType = mUserInfoSp.getInt("sign_in_type", 1);
        String signInId = mUserInfoSp.getString("sign_in_id", "");
        String signInIdPwd = mUserInfoSp.getString("sign_in_id_pwd", "");
        String signInName = mUserInfoSp.getString("sign_in_name", "");
        String signInNamePwd = mUserInfoSp.getString("sign_in_name_pwd", "");
        String signInMap = mUserInfoSp.getString("sign_in_map", "");

        mSignInType = signInType;

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

        chb_remember_sign_in.setChecked(rememberMe);
        et_get_api_url_url.setText(getApiUrlHost);

        {
            String[] maps = new String[]{getString(R.string.bd_map), getString(R.string.gl_map)};
            ArrayAdapter adapter = new ArrayAdapter<>(sp_sign_in_map.getContext(),
                    R.layout.map_spinner_dropdown_item,
                    maps);
            adapter.setDropDownViewResource(R.layout.map_spinner_dropdown_item);
            sp_sign_in_map.setAdapter(adapter);
            sp_sign_in_map.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 1
                            && GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(parent.getContext()) != ConnectionResult.SUCCESS) {
                        sp_sign_in_map.setSelection(0);
                    } else {
                        mSignInMap = position == 0 ? "Baidu" : "Google";
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (TextUtils.isEmpty(signInMap)) {
                if (Locale.SIMPLIFIED_CHINESE.equals(LocaleChanger.getLocale())) {
                    sp_sign_in_map.setSelection(0);
                } else {
                    sp_sign_in_map.setSelection(1);
                }
            } else {
                if (TextUtils.equals("Google", signInMap)) {
                    sp_sign_in_map.setSelection(1);
                } else {
                    sp_sign_in_map.setSelection(0);
                }
            }
        }

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
                            new ForegroundColorSpan(0xffef7f0f),
                            new StyleSpan(Typeface.BOLD)));
            tv_sign_in_by_id.setCompoundDrawablesWithIntrinsicBounds(R.drawable.load_grxz, 0, 0, 0);
            tv_sign_in_by_name.setText(R.string.sign_in_by_name);
            tv_sign_in_by_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.load_dsbwx, 0, 0, 0);
            et_sign_in_id.setVisibility(View.VISIBLE);
            et_sign_in_pwd.setVisibility(View.VISIBLE);
            et_sign_in_name.setVisibility(View.GONE);
            et_sign_in_name_pwd.setVisibility(View.GONE);
        } else {
            tv_sign_in_by_id.setText(R.string.sign_in_by_id);
            tv_sign_in_by_id.setCompoundDrawablesWithIntrinsicBounds(R.drawable.load_grwx, 0, 0, 0);
            tv_sign_in_by_name.setText(new Spans()
                    .append(getString(R.string.sign_in_by_name),
                            new ForegroundColorSpan(0xffef7f0f),
                            new StyleSpan(Typeface.BOLD)));
            tv_sign_in_by_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.load_dsbyx, 0, 0, 0);
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
        mRestAPI.reset();
        Global.SERVER_URL = getApiUrlHost;
        mDisposableSignIn = mRestAPI.urlService()
                .getApiUrl(getApiUrlHost)
                .map(apiUrl -> {
                    Global.API_URL = apiUrl.url + "/";
                    mRestAPI.reset();
                    return Global.API_URL;
                })
                .flatMap(s -> mRestAPI.cloudService().signIn(sign, pwd, type))
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
                                Toast.makeText(App.getInstance(), R.string.error_id_or_pwd, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

    private void signIn() {
        String getApiUrlHost = et_get_api_url_url.getEditableText().toString();
        String signInId = et_sign_in_id.getEditableText().toString();
        String signInPwd = et_sign_in_pwd.getEditableText().toString();
        String signInName = et_sign_in_name.getEditableText().toString();
        String signInNamePwd = et_sign_in_name_pwd.getEditableText().toString();
        String map = mSignInMap;
        int signInType = mSignInType;
        boolean rememberMe = chb_remember_sign_in.isChecked();

        mSettingSp.edit()
                .putBoolean("remember_me", rememberMe)
                .putBoolean("auto_sign_in", true)
                .apply();
        if (rememberMe) {
            mSettingSp.edit().putString("get_api_url_host", getApiUrlHost).apply();

            if (signInType == 0) {
                mUserInfoSp.edit()
                        .putString("sign_in_name", signInName)
                        .putString("sign_in_name_pwd", signInNamePwd)
                        .putString("sign_in_map", map)
                        .putInt("sign_in_type", signInType)
                        .apply();
            } else {
                mUserInfoSp.edit()
                        .putString("sign_in_id", signInId)
                        .putString("sign_in_id_pwd", signInPwd)
                        .putString("sign_in_map", map)
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
