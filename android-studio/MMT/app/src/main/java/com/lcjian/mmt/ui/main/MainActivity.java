package com.lcjian.mmt.ui.main;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.network.entity.SignInInfo;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.util.FragmentSwitchHelper;
import com.lcjian.mmt.util.NotificationUtils;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.bnv_main)
    BottomNavigationView bnv_main;

    private FragmentSwitchHelper mFragmentSwitchHelper;

    private int mCheckedId;

    public LocationClient mLocClient = null;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFragmentSwitchHelper = FragmentSwitchHelper.create(R.id.fl_fragment_container,
                getSupportFragmentManager(), true,
                new QuoteManageFragment(), new LogisticsManageFragment(), new CarManageFragment(), new UserCenterFragment());

        bnv_main.setOnNavigationItemSelectedListener(this);
        bnv_main.setSelectedItemId(R.id.action_quote);

        mDisposable = mRxBus.asFlowable()
                .filter(o -> o instanceof BDLocation)
                .observeOn(Schedulers.io())
                .flatMap(o -> mRestAPI.cloudService()
                        .uploadLocation(new Gson().fromJson(mUserInfoSp.getString("sign_in_info", ""), SignInInfo.class).user.userId,
                                ((BDLocation) o).getLongitude(), ((BDLocation) o).getLatitude())
                        .toFlowable())
                .subscribe(stringResponseData -> {
                        },
                        throwable -> {
                        });
        setupLoc();
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        mLocClient.disableLocInForeground(true);
        mLocClient.stop();
        mLocClient = null;
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int checkedId = item.getItemId();
        if (mCheckedId == checkedId) {
            return false;
        }
        switch (checkedId) {
            case R.id.action_quote: {
                mFragmentSwitchHelper.changeFragment(QuoteManageFragment.class);
            }
            break;
            case R.id.action_logistics: {
                mFragmentSwitchHelper.changeFragment(LogisticsManageFragment.class);
            }
            break;
            case R.id.action_car: {
                mFragmentSwitchHelper.changeFragment(CarManageFragment.class);
            }
            break;
            case R.id.action_user: {
                mFragmentSwitchHelper.changeFragment(UserCenterFragment.class);
            }
            break;
            default:
                break;
        }
        mCheckedId = checkedId;
        return true;
    }

    private void setupLoc() {
        mLocClient = new LocationClient(getApplicationContext());
        mLocClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (location == null) {
                    return;
                }
                mRxBus.send(location);
            }
        });
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(30 * 60 * 1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        Notification notification;
        //设置后台定位
        //android8.0及以上使用NotificationUtils
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationUtils mNotificationUtils = new NotificationUtils(this);
            Notification.Builder builder2 = mNotificationUtils.getAndroidChannelNotification(
                    getString(R.string.app_name), "正在后台定位(30分钟定位一次)");
            notification = builder2.build();
        } else {
            //获取一个Notification构造器
            Notification.Builder builder = new Notification.Builder(MainActivity.this.getApplicationContext());
            Intent nfIntent = new Intent(MainActivity.this.getApplicationContext(), MainActivity.class);

            builder.setContentIntent(PendingIntent.getActivity(MainActivity.this, 0, nfIntent, 0)) // 设置PendingIntent
                    .setContentTitle(getString(R.string.app_name))       // 设置下拉列表里的标题
                    .setSmallIcon(R.mipmap.ic_launcher)                   // 设置状态栏内的小图标
                    .setContentText("正在后台定位(30分钟定位一次)")        // 设置上下文内容
                    .setWhen(System.currentTimeMillis());                // 设置该通知发生的时间

            notification = builder.build(); // 获取构建好的Notification
        }
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

        mLocClient.enableLocInForeground(1001, notification);// 调起前台定位
    }
}
