package com.winside.lighting.ui.base;

import com.kaopiz.kprogresshud.KProgressHUD;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private KProgressHUD mProgressDialog;

    public void showProgress() {
        if (mProgressDialog == null)
            mProgressDialog = KProgressHUD.create(this);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
