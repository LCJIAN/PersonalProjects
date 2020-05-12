package com.winside.lighting.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.user.ModifyPwdActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MineFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.tv_go_to_modify_pwd)
    TextView tv_go_to_modify_pwd;
    @BindView(R.id.tv_go_to_message_notification)
    TextView tv_go_to_message_notification;
    @BindView(R.id.tv_go_to_check_app_version)
    TextView tv_go_to_check_app_version;
    @BindView(R.id.tv_go_to_about_us)
    TextView tv_go_to_about_us;
    @BindView(R.id.tv_go_to_sign_out)
    TextView tv_go_to_sign_out;

    private Unbinder unbinder;

    private Disposable mDisposableSignOut;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_navigation_title.setText(R.string.action_mine);
        tv_go_to_message_notification.setOnClickListener(this);
        tv_go_to_modify_pwd.setOnClickListener(this);
        tv_go_to_check_app_version.setOnClickListener(this);
        tv_go_to_about_us.setOnClickListener(this);
        tv_go_to_sign_out.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (mDisposableSignOut != null) {
            mDisposableSignOut.dispose();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_go_to_message_notification:
                Toast.makeText(App.getInstance(), R.string.message_notification, Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_go_to_modify_pwd:
                startActivity(new Intent(v.getContext(), ModifyPwdActivity.class));
                break;
            case R.id.tv_go_to_check_app_version:
                Toast.makeText(App.getInstance(), R.string.latest_version_already, Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_go_to_about_us:
                Toast.makeText(App.getInstance(), R.string.about_us, Toast.LENGTH_SHORT).show();
                break;
            default:
                new AlertDialog.Builder(v.getContext())
                        .setTitle("是否确定退出？")
                        .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("确定", (dialog, which) -> {
                            dialog.dismiss();
                            signOut();
                        })
                        .create()
                        .show();
                break;
        }
    }

    private void signOut() {
        showProgress();
        if (mDisposableSignOut != null) {
            mDisposableSignOut.dispose();
        }
        mDisposableSignOut = RestAPI.getInstance().lightingService().signOut()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                    hideProgress();
                    if (objectResponseData.code == 1000) {
                        SharedPreferencesDataSource.clear();
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    } else {
                        Toast.makeText(App.getInstance(), objectResponseData.message, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    hideProgress();
                    Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}
