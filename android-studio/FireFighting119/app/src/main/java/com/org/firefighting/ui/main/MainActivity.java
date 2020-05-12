package com.org.firefighting.ui.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.lcjian.lib.util.FragmentSwitchHelper;
import com.lcjian.lib.util.common.PackageUtils2;
import com.org.firefighting.R;
import com.org.firefighting.data.network.entity.VersionInfo;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.bnv_main)
    BottomNavigationView bnv_main;

    private FragmentSwitchHelper mFragmentSwitchHelper;

    private int mCheckedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFragmentSwitchHelper = FragmentSwitchHelper.create(R.id.fl_fragment_container,
                getSupportFragmentManager(), true,
                new HomeFragment(), new TasksFragment(), new ConversationsFragment(), new MineFragment());

        bnv_main.setOnNavigationItemSelectedListener(this);
        bnv_main.setSelectedItemId(R.id.action_home);

        checkVersion();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int checkedId = item.getItemId();
        if (mCheckedId == checkedId) {
            return false;
        }
        switch (checkedId) {
            case R.id.action_home:
                mFragmentSwitchHelper.changeFragment(HomeFragment.class);
                break;
            case R.id.action_task:
                mFragmentSwitchHelper.changeFragment(TasksFragment.class);
                break;
            case R.id.action_conversation:
                mFragmentSwitchHelper.changeFragment(ConversationsFragment.class);
                break;
            case R.id.action_mine:
                mFragmentSwitchHelper.changeFragment(MineFragment.class);
                break;
            default:
                break;
        }
        mCheckedId = checkedId;
        return true;
    }

    @Override
    protected void onDestroy() {
        AllenVersionChecker.getInstance().cancelAllMission();
        super.onDestroy();
    }

    void checkTask() {
        bnv_main.setSelectedItemId(R.id.action_task);
    }

    private void checkVersion() {
        AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl("http://58.144.150.104:9528/app/checkversion.html")
                .request(new RequestVersionListener() {
                    @Override
                    public UIData onRequestVersionSuccess(DownloadBuilder downloadBuilder, String result) {
                        VersionInfo info = new Gson().fromJson(result, VersionInfo.class);

                        int newVersion = Integer.parseInt(info.newVersion.replace(".", ""));
                        int minVersion = Integer.parseInt(info.minVersion.replace(".", ""));
                        int version = Integer.parseInt(PackageUtils2.getVersionName(MainActivity.this).replace(".", ""));

                        if (version >= newVersion) {
                            return null;
                        } else {
                            if (version < minVersion) {
                                downloadBuilder.setForceRedownload(true);
                                downloadBuilder.setForceUpdateListener(() -> finish());
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
                .executeMission(this);
    }
}
