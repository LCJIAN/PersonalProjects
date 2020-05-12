package com.lcjian.vastplayer.ui.subject;

import android.os.Bundle;
import android.view.MenuItem;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.base.BaseActivity;

public class TvStationsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.action_live_tv);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container, new TVStationsFragment()).commit();
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
}
