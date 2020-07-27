package com.org.firefighting.ui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.google.gson.Gson;
import com.lcjian.lib.util.common.PackageUtils2;
import com.org.chat.SmackClient;
import com.org.chat.SmackClientService;
import com.org.firefighting.App;
import com.org.firefighting.GlideApp;
import com.org.firefighting.R;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.entity.User;
import com.org.firefighting.data.network.entity.VersionInfo;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.chat.ChatActivity;
import com.org.firefighting.ui.chat.DepartmentsActivity;
import com.org.firefighting.ui.resource.ResourcesActivity;
import com.org.firefighting.ui.service.ServiceListActivity;
import com.org.firefighting.ui.user.SignInActivity;

import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.jpush.android.api.JPushInterface;

public class MineFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.iv_avatar)
    ImageView iv_avatar;
    @BindView(R.id.tv_real_name)
    TextView tv_real_name;
    @BindView(R.id.tv_user_department)
    TextView tv_user_department;
    @BindView(R.id.tv_user_role)
    TextView tv_user_role;
    @BindView(R.id.rl_my_task)
    RelativeLayout rl_my_task;
    @BindView(R.id.rl_arc)
    RelativeLayout rl_arc;
    @BindView(R.id.rl_organisation)
    RelativeLayout rl_organisation;
    @BindView(R.id.rl_announcement)
    RelativeLayout rl_announcement;
    @BindView(R.id.rl_version)
    ConstraintLayout rl_version;
    @BindView(R.id.rl_feed_back)
    RelativeLayout rl_feed_back;
    @BindView(R.id.rl_sign_out)
    RelativeLayout rl_sign_out;

    @BindView(R.id.tv_version_name)
    TextView tv_version_name;

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
        User user = SharedPreferencesDataSource.getSignInResponse().user;
        GlideApp.with(this)
                .load("http://124.162.30.39:9528/admin-ht/" + user.avatar)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(iv_avatar);
        tv_real_name.setText(user.realName);
        tv_user_department.setText(user.dept);
        tv_user_role.setText(user.roleName);
        tv_version_name.setText(PackageUtils2.getVersionName(view.getContext()));

        rl_my_task.setOnClickListener(this);
        rl_version.setOnClickListener(this);
        rl_feed_back.setOnClickListener(this);
        rl_sign_out.setOnClickListener(this);
        rl_arc.setOnClickListener(this);
        rl_organisation.setOnClickListener(this);
        rl_announcement.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_my_task:
                ((MainActivity) Objects.requireNonNull(getActivity())).checkTask();
                break;
            case R.id.rl_arc:
                startActivity(new Intent(v.getContext(), DepartmentsActivity.class));
                break;
            case R.id.rl_organisation:
                startActivity(new Intent(v.getContext(), ResourcesActivity.class));
                break;
            case R.id.rl_announcement:
                startActivity(new Intent(v.getContext(), ServiceListActivity.class));
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

    private void checkVersion() {
        AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl("http://124.162.30.39:9000/app/checkversion.html")
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

                    }
                })
                .executeMission(getContext());
    }
}
