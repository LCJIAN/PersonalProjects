package com.lcjian.mmt.ui.user;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.network.entity.SignInInfo;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.util.FileUtils;
import com.lcjian.mmt.util.StorageUtils;
import com.lcjian.mmt.util.Utils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.android.api.JPushInterface;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_cache_size)
    TextView tv_cache_size;
    @BindView(R.id.ll_clear_cache)
    LinearLayout ll_clear_cache;
    @BindView(R.id.ll_sign_out)
    LinearLayout ll_sign_out;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        tv_title.setText(R.string.settings);
        setupCacheSize();

        btn_nav_back.setOnClickListener(v -> onBackPressed());
        ll_clear_cache.setOnClickListener(v -> clearCache());
        ll_sign_out.setOnClickListener(v -> {
            mRestAPI.reset();
            mUserInfoSp.edit().clear().apply();
            JPushInterface.deleteAlias(this,
                    new Gson().fromJson(mUserInfoSp.getString("sign_in_info", ""), SignInInfo.class).user.userId.hashCode());
            JPushInterface.stopPush(this);
            startActivity(Intent.makeRestartActivityTask(new ComponentName(this, SignInUpActivity.class)));
        });
    }

    private void clearCache() {
        showProgress();
        mDisposable = Single
                .just(true)
                .map(aBoolean -> {
                    FileUtils.deleteFile(StorageUtils.getCacheDirectory(SettingsActivity.this, false).getAbsolutePath());
                    FileUtils.deleteFile(StorageUtils.getCacheDirectory(SettingsActivity.this, true).getAbsolutePath());
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                            hideProgress();
                            setupCacheSize();
                            Toast.makeText(App.getInstance(), R.string.cache_cleared, Toast.LENGTH_SHORT).show();
                        },
                        throwable -> hideProgress());
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    private void setupCacheSize() {
        File f1 = StorageUtils.getCacheDirectory(SettingsActivity.this, false);
        File f2 = StorageUtils.getCacheDirectory(SettingsActivity.this, true);
        if (TextUtils.equals(f1.getAbsolutePath(), f2.getAbsolutePath())) {
            tv_cache_size.setText(Utils.formatBytes(getFileSize(f1), 2));
        } else {
            tv_cache_size.setText(Utils.formatBytes(getFileSize(f1) + getFileSize(f2), 2));
        }
    }

    private long getFileSize(File file) {
        long size = FileUtils.getFileSize(file.getAbsolutePath());
        return size == -1 ? 0 : size;
    }
}
