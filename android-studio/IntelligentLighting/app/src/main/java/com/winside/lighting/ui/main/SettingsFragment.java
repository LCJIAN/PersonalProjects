package com.winside.lighting.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.user.ModifyPwdActivity;
import com.winside.lighting.ui.user.SignInActivity;
import com.winside.lighting.ui.user.SwitchServerFragment;
import com.winside.lighting.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.tv_go_to_switch_server)
    TextView tv_go_to_switch_server;
    @BindView(R.id.tv_go_to_modify_pwd)
    TextView tv_go_to_modify_pwd;
    @BindView(R.id.tv_go_to_sign_out)
    TextView tv_go_to_sign_out;

    private Unbinder unbinder;

    private Disposable mDisposableSignOut;
    private Disposable mDisposableSwitchServer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_navigation_title.setText(R.string.action_setting);
        tv_go_to_switch_server.setOnClickListener(this);
        tv_go_to_modify_pwd.setOnClickListener(this);
        tv_go_to_sign_out.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (mDisposableSignOut != null) {
            mDisposableSignOut.dispose();
        }
        if (mDisposableSwitchServer != null) {
            mDisposableSwitchServer.dispose();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_go_to_switch_server:
                new SwitchServerFragment()
                        .setOnServerAddressChangeListener(this::switchServer)
                        .show(getChildFragmentManager(), "SwitchServerFragment");
                break;
            case R.id.tv_go_to_modify_pwd:
                startActivity(new Intent(v.getContext(), ModifyPwdActivity.class));
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

    private void switchServer(String serverAddress) {
        if (TextUtils.isEmpty(serverAddress)) {
            return;
        }
        if (!serverAddress.startsWith("http://") && !serverAddress.startsWith("https://")) {
            serverAddress = "http://" + serverAddress;
        }
        showProgress();
        if (mDisposableSwitchServer != null) {
            mDisposableSwitchServer.dispose();
        }
        mDisposableSwitchServer = Single.just(serverAddress)
                .map(s -> Pair.create(s, Utils.testUrlWithTimeOut(s, 10 * 1000)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aPair -> {
                            hideProgress();
                            if (aPair.second != null && aPair.second) {
                                SharedPreferencesDataSource.clear();
                                RestAPI.getInstance().resetApiUrl(aPair.first);
                                SharedPreferencesDataSource.setServerAddress(aPair.first);
                                if (getActivity() != null) {
                                    startActivity(new Intent(getActivity(), SignInActivity.class));
                                    getActivity().finish();
                                }
                            } else {
                                Toast.makeText(App.getInstance(), R.string.can_not_connect_server, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }
}
