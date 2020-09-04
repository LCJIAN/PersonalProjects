package com.org.firefighting.ui.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.google.gson.Gson;
import com.lcjian.lib.util.common.PackageUtils2;
import com.org.chat.SmackClient;
import com.org.chat.SmackClientService;
import com.org.firefighting.App;
import com.org.firefighting.BuildConfig;
import com.org.firefighting.GlideApp;
import com.org.firefighting.R;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.SignInResponse;
import com.org.firefighting.data.network.entity.User;
import com.org.firefighting.data.network.entity.VersionInfo;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.chat.ChatActivity;
import com.org.firefighting.ui.user.PwdModifyFragment;
import com.org.firefighting.ui.user.SignInActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yalantis.ucrop.UCrop;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.net.URLEncoder;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.jpush.android.api.JPushInterface;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import top.zibin.luban.Luban;

public class MineFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipe_refresh_layout;
    @BindView(R.id.btn_edit_avatar)
    ImageButton btn_edit_avatar;
    @BindView(R.id.iv_avatar)
    ImageView iv_avatar;
    @BindView(R.id.tv_real_name)
    TextView tv_real_name;
    @BindView(R.id.tv_user_department)
    TextView tv_user_department;
    @BindView(R.id.tv_user_role)
    TextView tv_user_role;
    @BindView(R.id.tv_phone)
    TextView tv_phone;
    @BindView(R.id.rl_version)
    ConstraintLayout rl_version;
    @BindView(R.id.rl_feed_back)
    RelativeLayout rl_feed_back;
    @BindView(R.id.rl_pwd_modify)
    RelativeLayout rl_pwd_modify;
    @BindView(R.id.rl_sign_out)
    RelativeLayout rl_sign_out;

    @BindView(R.id.tv_version_name)
    TextView tv_version_name;

    private Disposable mDisposable;
    private Disposable mDisposableU;
    private Disposable mDisposableP;

    private Unbinder mUnBinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipe_refresh_layout.setColorSchemeResources(R.color.colorAccent);
        swipe_refresh_layout.setOnRefreshListener(this::getUserInfo);

        btn_edit_avatar.setOnClickListener(this);
        tv_phone.setOnClickListener(this);
        rl_version.setOnClickListener(this);
        rl_feed_back.setOnClickListener(this);
        rl_sign_out.setOnClickListener(this);
        rl_pwd_modify.setOnClickListener(this);

        setupUserInfo();
    }

    @Override
    public void onDestroyView() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        if (mDisposableU != null) {
            mDisposableU.dispose();
        }
        mUnBinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            assert data != null;
            if (requestCode == 1000) {
                Context context = getContext();
                assert context != null;
                File file = new File(context.getExternalFilesDir("Pictures"), "uCrop.jpg");
                Uri sourceUri = Uri.fromFile(new File(Matisse.obtainPathResult(data).get(0)));
                Uri destinationUri = Uri.fromFile(file);
                UCrop.of(sourceUri, destinationUri)
                        .withAspectRatio(1, 1)
                        .start(context, this);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                modifyAvatar(UCrop.getOutput(data));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit_avatar:
                chooseImage();
                break;
            case R.id.tv_phone: {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + SharedPreferencesDataSource.getSignInResponse().user.phone));
                v.getContext().startActivity(intent);
            }
            break;
            case R.id.rl_pwd_modify:
                new PwdModifyFragment().show(getChildFragmentManager(), "PwdModifyFragment");
                break;
            case R.id.rl_feed_back:
                startActivity(new Intent(v.getContext(), ChatActivity.class)
                        .putExtra("owner_jid", SharedPreferencesDataSource.getSignInResponse().user.id + "@" + SmackClient.DOMAIN)
                        .putExtra("opposite_jid", "1645@" + SmackClient.DOMAIN)
                        .putExtra("opposite_name", "意见反馈"));
                break;
            case R.id.rl_version:
                checkVersion();
                break;
            case R.id.rl_sign_out:
                new AlertDialog.Builder(v.getContext())
                        .setTitle(R.string.sign_out_title)
                        .setMessage(R.string.sign_out_msg)
                        .setNegativeButton(R.string.confirm, (dialog, which) -> {
                            JPushInterface.deleteAlias(v.getContext(), SharedPreferencesDataSource.getSignInResponse().user.id.intValue());
                            JPushInterface.stopPush(v.getContext());
                            SharedPreferencesDataSource.clearUserInfo();
                            SmackClientService.stop(v.getContext());
                            startActivity(Intent.makeRestartActivityTask(new Intent(v.getContext(), SignInActivity.class).getComponent()));
                        })
                        .setPositiveButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .create().show();
                break;
        }
    }

    protected void setRefreshing(final boolean refreshing) {
        if (swipe_refresh_layout.isEnabled()) {
            swipe_refresh_layout.post(() -> {
                if (swipe_refresh_layout != null) {
                    swipe_refresh_layout.setRefreshing(refreshing);
                }
            });
        }
    }

    private void setupUserInfo() {
        User user = SharedPreferencesDataSource.getSignInResponse().user;
        GlideApp.with(this)
                .load("http://124.162.30.39:9528/admin-ht/" + user.avatar)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(iv_avatar);
        tv_real_name.setText(user.realName);
        tv_user_department.setText(user.dept);
        tv_user_role.setText(user.roleName);
        tv_phone.setText(user.phone);
        tv_version_name.setText(PackageUtils2.getVersionName(tv_version_name.getContext()));
    }

    private void getUserInfo() {
        setRefreshing(true);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = RestAPI.getInstance().apiServiceSB()
                .getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                            setRefreshing(false);
                            SignInResponse signInResponse = SharedPreferencesDataSource.getSignInResponse();
                            signInResponse.user = user;
                            SharedPreferencesDataSource.putSignInResponse(signInResponse);
                            setupUserInfo();
                        },
                        throwable -> {
                            setRefreshing(false);
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void chooseImage() {
        Activity activity = getActivity();
        assert activity != null;
        RxPermissions rxPermissions = new RxPermissions(activity);
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        mDisposableP = rxPermissions
                .request(Manifest.permission.CAMERA)
                .subscribe(granted -> {
                    if (granted) {
                        Matisse.from(this)
                                .choose(MimeType.ofImage())
                                .capture(true)
                                .captureStrategy(new CaptureStrategy(false, BuildConfig.FILE_PROVIDER_AUTHORITIES, "Matisse"))
                                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                .thumbnailScale(0.85f)
                                .imageEngine(new GlideEngine())
                                .forResult(1000);
                    } else {
                        Toast.makeText(App.getInstance(), "no permissions", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void modifyAvatar(Uri uri) {
        showProgress();
        if (mDisposableU != null) {
            mDisposableU.dispose();
        }
        mDisposableU = Single
                .just(uri)
                .flatMap(u -> {
                    File file = Luban.with(getContext()).load(u).get().get(0);
                    return RestAPI.getInstance().apiServiceSB()
                            .modifyAvatar(MultipartBody.Part.createFormData(
                                    "file",
                                    URLEncoder.encode(file.getName(), "utf-8"),
                                    RequestBody.create(MediaType.parse("image/*"), file)));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseBody -> {
                            hideProgress();
                            getUserInfo();
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void checkVersion() {
        checkVersion("http://47.241.26.39/app/checkversion.html", "http://124.162.30.39:9000/app/checkversion.html");
    }

    private void checkVersion(String url, String fallback) {
        AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl(url)
                .request(new RequestVersionListener() {
                    @Override
                    public UIData onRequestVersionSuccess(DownloadBuilder downloadBuilder, String result) {
                        Activity activity = getActivity();
                        if (activity == null) {
                            return null;
                        }
                        VersionInfo info = new Gson().fromJson(result, VersionInfo.class);

                        int newVersion = Integer.parseInt(info.newVersion.replace(".", ""));
                        int minVersion = Integer.parseInt(info.minVersion.replace(".", ""));
                        int version = Integer.parseInt(PackageUtils2.getVersionName(activity).replace(".", ""));

                        if (version >= newVersion) {
                            Toast.makeText(App.getInstance(), R.string.latest_version, Toast.LENGTH_SHORT).show();
                            return null;
                        } else {
                            if (version < minVersion) {
                                downloadBuilder.setForceRedownload(true);
                                downloadBuilder.setForceUpdateListener(activity::finish);
                            } else {
                                downloadBuilder.setForceRedownload(false);
                                downloadBuilder.setForceUpdateListener(null);
                            }
                            StringBuilder content = new StringBuilder();
                            for (Map.Entry<String, String> entry : info.updateDescription.entrySet()) {
                                content.append(entry.getKey()).append(".").append(entry.getValue()).append("\n");
                            }
                            return UIData
                                    .create()
                                    .setDownloadUrl(info.apkUrl)
                                    .setTitle("发现新版本")
                                    .setContent(content.toString());
                        }
                    }

                    @Override
                    public void onRequestVersionFailure(String message) {
                        if (!TextUtils.isEmpty(fallback)) {
                            checkVersion(fallback, null);
                        }
                    }
                })
                .executeMission(getContext());
    }
}
