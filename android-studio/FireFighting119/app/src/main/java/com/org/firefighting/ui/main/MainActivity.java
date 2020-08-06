package com.org.firefighting.ui.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.lcjian.lib.util.FragmentSwitchHelper;
import com.lcjian.lib.util.common.PackageUtils2;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.data.network.entity.VersionInfo;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.bnv_main)
    BottomNavigationView bnv_main;

    private FragmentSwitchHelper mFragmentSwitchHelper;

    private int mCheckedId;

    private Disposable mDisposableR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFragmentSwitchHelper = FragmentSwitchHelper.create(R.id.fl_fragment_container,
                getSupportFragmentManager(), true,
                new HomeFragment(), new DepartmentsFragment(), new ConversationsFragment(), new MineFragment());

        bnv_main.setOnNavigationItemSelectedListener(this);
        bnv_main.setSelectedItemId(R.id.action_conversation);

        bnv_main.post(() -> bnv_main.setSelectedItemId(R.id.action_home));

        mDisposableR = RxBus.getInstance()
                .asFlowable()
                .filter(o -> o instanceof ConversationsFragment.TotalUnReadCount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    BadgeDrawable badgeDrawable = bnv_main.getOrCreateBadge(R.id.action_conversation);
                    int number = ((ConversationsFragment.TotalUnReadCount) o).number;
                    badgeDrawable.setNumber(number);
                    badgeDrawable.setBackgroundColor(0xFFE84E40);
                    badgeDrawable.setVisible(number != 0);
                });
        checkVersion();
    }

    @Override
    protected void onStop() {
        ShortcutBadger.removeCount(getApplicationContext());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mDisposableR.dispose();
        AllenVersionChecker.getInstance().cancelAllMission();
        super.onDestroy();
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
                mFragmentSwitchHelper.changeFragment(DepartmentsFragment.class);
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

    void checkContacts() {
        bnv_main.setSelectedItemId(R.id.action_task);
    }

    private void checkVersion() {
        AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestUrl("http://124.162.30.39:9000/app/checkversion.html")
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
//
//    /**
//     * BottomNavigationView显示角标
//     *
//     * @param viewIndex  tab索引
//     * @param showNumber 显示的数字，小于等于0是将不显示
//     */
//    private void showBadgeView(int viewIndex, int showNumber) {
//        // 具体child的查找和view的嵌套结构请在源码中查看
//        // 从bottomNavigationView中获得BottomNavigationMenuView
//        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bnv_main.getChildAt(0);
//        // 从BottomNavigationMenuView中获得childView, BottomNavigationItemView
//        if (viewIndex < menuView.getChildCount()) {
//            // 获得viewIndex对应子tab
//            View view = menuView.getChildAt(viewIndex);
//            // 从子tab中获得其中显示图片的ImageView
//            View icon = view.findViewById(android.support.design.R.id.icon);
//            // 获得图标的宽度
//            int iconWidth = icon.getWidth();
//            // 获得tab的宽度/2
//            int tabWidth = view.getWidth() / 2;
//            // 计算badge要距离右边的距离
//            int spaceWidth = tabWidth - iconWidth;
//            // 显示BadgeView
//            new QBadgeView(this).bindTarget(view)
//                    .setGravityOffset(spaceWidth, 3, false)
//                    .setBadgeNumber(showNumber);
//        }
//    }
}
