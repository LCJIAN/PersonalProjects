package com.org.firefighting.ui.base;

import androidx.fragment.app.Fragment;

import com.kaopiz.kprogresshud.KProgressHUD;

public class BaseFragment extends Fragment {

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
