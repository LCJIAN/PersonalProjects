package com.lcjian.vastplayer.ui.subject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.base.BaseActivity;

public class SubjectsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);
        String type = getIntent().getStringExtra("type");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(
                    TextUtils.equals("movie", type) ? R.string.movies
                            : TextUtils.equals("tv_show", type) ? R.string.tv_shows
                            : TextUtils.equals("variety", type) ? R.string.variety
                            : TextUtils.equals("animation", type) ? R.string.animation : 0);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container,
                SubjectsFragment.newInstance(type)).commit();
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
