package com.lcjian.vastplayer.ui.player;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.VideoUrl;
import com.lcjian.vastplayer.ui.base.BaseActivity;

public class VideoPlayerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        if (getSupportFragmentManager().findFragmentByTag("VideoPlayerFragment") == null) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.container,
                    VideoPlayerFragment.newInstance(
                            (VideoUrl) getIntent().getSerializableExtra("video_url"),
                            getIntent().getStringExtra("title")),
                    "VideoPlayerFragment").commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_player, menu);
        return true;
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
            case R.id.action_settings: {
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
