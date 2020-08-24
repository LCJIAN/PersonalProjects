package com.org.firefighting.ui.base;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.kaopiz.kprogresshud.KProgressHUD;

public class BaseDialogFragment extends AppCompatDialogFragment {

    private KProgressHUD mProgressDialog;

    public void showProgress() {
        if (mProgressDialog == null && getActivity() != null) {
            mProgressDialog = KProgressHUD.create(getActivity());
        }
        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void hideProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
