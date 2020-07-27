package com.org.firefighting;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.room.Room;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.org.chat.data.db.AppDatabase;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cn.jpush.android.api.JPushInterface;
import io.reactivex.plugins.RxJavaPlugins;
import timber.log.Timber;

public class App extends Application {

    private static App INSTANCE;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static App getInstance() {
        return INSTANCE;
    }

    private AppDatabase mAppDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        initBugLy();

        JPushInterface.init(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            JPushInterface.setDebugMode(true);
        } else {
            Timber.plant(new ErrorTree());
        }

        mAppDatabase = Room.databaseBuilder(this, AppDatabase.class, "smack-client")
                .allowMainThreadQueries()
                .build();
        BigImageViewer.initialize(GlideImageLoader.with(App.getInstance()));
        RxJavaPlugins.setErrorHandler(Timber::e);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public AppDatabase getAppDatabase() {
        return mAppDatabase;
    }

    private void initBugLy() {
        Context context = getApplicationContext();
        // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        // 初始化Bugly
        CrashReport.initCrashReport(context, Constants.BUG_LY_APP_ID, BuildConfig.DEBUG, strategy);
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
