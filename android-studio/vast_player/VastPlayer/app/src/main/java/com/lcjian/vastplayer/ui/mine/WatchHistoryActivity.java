package com.lcjian.vastplayer.ui.mine;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.base.BaseActivity;

import io.reactivex.disposables.CompositeDisposable;

public class WatchHistoryActivity extends BaseActivity {

    private CompositeDisposable mDisposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        mDisposables = new CompositeDisposable();
        mDisposables.add(mRxBus.asFlowable().subscribe(event -> {
            if (event instanceof Toolbar) {
                setSupportActionBar((Toolbar) event);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        }));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (getSupportFragmentManager().findFragmentByTag("WatchHistoryFragment") == null) {
            getSupportFragmentManager().beginTransaction().add(
                    R.id.fl_fragment_container, new WatchHistoryFragment(), "WatchHistoryFragment").commit();
        }
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