package com.org.firefighting.ui.base;

import androidx.appcompat.app.AppCompatActivity;

import com.kaopiz.kprogresshud.KProgressHUD;

public class BaseActivity extends AppCompatActivity {

    private KProgressHUD mProgressDialog;

    public void showProgress() {
        if (mProgressDialog == null)
            mProgressDialog = KProgressHUD.create(this);
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    public void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
