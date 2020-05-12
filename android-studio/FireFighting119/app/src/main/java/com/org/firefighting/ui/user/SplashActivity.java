package com.org.firefighting.ui.user;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.util.Pair;

import com.org.firefighting.R;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.main.MainActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SplashActivity extends BaseActivity {

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_splash);

        mDisposable = Single.zip(
                Single.fromCallable(SharedPreferencesDataSource::getGuided),
                Single.fromCallable(() -> SharedPreferencesDataSource.getSignInResponse() != null),
                (Pair::create))
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                            assert pair.first != null;
                            assert pair.second != null;
                            if (pair.second) {
                                startActivity(new Intent(this, MainActivity.class));
                            } else {
                                if (pair.first) {
                                    startActivity(new Intent(this, SignInActivity.class));
                                } else {
                                    SharedPreferencesDataSource.putGuided(true);
                                    startActivity(new Intent(this, GuideActivity.class));
                                }
                            }
                            finish();
                        },
                        throwable -> {
                        });
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }
}