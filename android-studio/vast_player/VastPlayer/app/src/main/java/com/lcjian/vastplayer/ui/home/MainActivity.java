package com.lcjian.vastplayer.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lcjian.lib.util.FragmentSwitchHelper;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.ShareFragment;
import com.lcjian.vastplayer.ui.base.BaseActivity;
import com.lcjian.vastplayer.ui.download.DownloadsActivity;
import com.lcjian.vastplayer.ui.search.SearchActivity;
import com.umeng.socialize.UMShareAPI;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bnv_main)
    BottomNavigationView bnv_main;

    private FragmentSwitchHelper mFragmentSwitchHelper;

    private int mCheckedId;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_search: {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            }
            case R.id.action_downloads: {
                startActivity(new Intent(this, DownloadsActivity.class));
                return true;
            }
            case R.id.action_share: {
                new ShareFragment().show(getSupportFragmentManager(), "ShareFragment");
                return true;
            }
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        UMShareAPI.get(this).release();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.mipmap.ic_launcher_middle);
        }

        mFragmentSwitchHelper = FragmentSwitchHelper.create(R.id.fl_main_fragment_container,
                getSupportFragmentManager(), true,
                new RecommendFragment(), new MicroVideoFragment(), new WebsiteFragment(), new MineFragment());

        bnv_main.setOnNavigationItemSelectedListener(this);
        bnv_main.setSelectedItemId(R.id.action_recommend);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int checkedId = item.getItemId();
        if (mCheckedId == checkedId) {
            return false;
        }
        switch (checkedId) {
            case R.id.action_recommend:
                mFragmentSwitchHelper.changeFragment(RecommendFragment.class);
                break;
            case R.id.action_micro_video:
                mFragmentSwitchHelper.changeFragment(MicroVideoFragment.class);
                break;
            case R.id.action_website:
                mFragmentSwitchHelper.changeFragment(WebsiteFragment.class);
                break;
            case R.id.action_account:
                mFragmentSwitchHelper.changeFragment(MineFragment.class);
                break;
            default:
                break;
        }
        mCheckedId = checkedId;
        return true;
    }
}
