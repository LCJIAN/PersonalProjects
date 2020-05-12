package com.lcjian.vastplayer.ui.mine;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.base.BaseActivity;

import io.reactivex.disposables.CompositeDisposable;

public class VideoLibActivity extends BaseActivity {

    private CompositeDisposable mDisposables;

    private boolean mHasContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            TypedArray a = obtainStyledAttributes(new int[]{android.R.attr.colorPrimaryDark});
            getWindow().setStatusBarColor(a.getColor(0, 0x00ffffff));
            a.recycle();
        }
        mHasContent = getIntent().getBooleanExtra("has_content", false);
        mDisposables = new CompositeDisposable();
        mDisposables.add(mRxBus.asFlowable()
                .subscribe(event -> {
                    if (event instanceof Toolbar) {
                        setSupportActionBar((Toolbar) event);
                        if (mHasContent) {
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            }
                        }
                    }
                }));

        if (getSupportFragmentManager().findFragmentByTag("VideoLibFragment") == null) {
            getSupportFragmentManager().beginTransaction().add(
                    R.id.fl_fragment_container, new VideoLibFragment(), "VideoLibFragment").commit();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mHasContent) {
            menu.findItem(R.id.action_downloads).setVisible(false);
            menu.findItem(R.id.action_settings).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (mDisposables != null) {
            mDisposables.dispose();
        }
        super.onDestroy();
    }
}
