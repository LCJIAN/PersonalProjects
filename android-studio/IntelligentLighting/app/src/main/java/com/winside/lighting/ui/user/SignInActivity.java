package com.winside.lighting.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.ui.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.et_user_name)
    EditText et_user_name;
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

        tv_navigation_title.setText(R.string.sign_in);
        btn_sign_in.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_sign_in) {
            showProgress();
            mDisposable = RestAPI.getInstance().signInService().signInRx(et_user_name.getEditableText().toString(),
                    et_pwd.getEditableText().toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(signInInfoResponseData -> {
                                hideProgress();
                                if (signInInfoResponseData.code == 1000) {
                                    RestAPI.getInstance().refreshToken(signInInfoResponseData.data.token);
                                    SharedPreferencesDataSource.setToken(signInInfoResponseData.data.token);
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(App.getInstance(), signInInfoResponseData.message, Toast.LENGTH_LONG).show();
                                }
                            },
                            throwable -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                            });
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }
}
