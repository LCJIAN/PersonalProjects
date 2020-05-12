package com.lcjian.vastplayer.ui.mine;

import android.os.Bundle;
import android.view.MenuItem;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.base.BaseActivity;

import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
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
