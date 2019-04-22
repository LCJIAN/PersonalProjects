package com.lcjian.drinkwater.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.android.material.navigation.NavigationView;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.ui.DrinkReportActivity;
import com.lcjian.drinkwater.ui.base.BaseActivity;
import com.lcjian.drinkwater.ui.setting.SettingActivity;

import org.jetbrains.annotations.NotNull;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import q.rorbin.badgeview.QBadgeView;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (getSupportFragmentManager().findFragmentByTag("MainFragment") == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_fragment_container, new MainFragment(), "MainFragment").commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("drunk_water", false)) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("MainFragment");
            if (fragment != null) {
                ((MainFragment) fragment).performDrink();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_remove_ads) {
            mSettingSp.edit().putBoolean("ad_clicked", true).apply();
        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(this, SettingActivity.class));
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(this, DrinkReportActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupAdBadge();
    }

    private void setupAdBadge() {
        if (!mSettingSp.getBoolean("ad_clicked", false)) {
            navigationView.post(() -> new QBadgeView(this)
                    .bindTarget(((ViewGroup) ((ViewGroup) navigationView
                            .getChildAt(0))
                            .getChildAt(1))
                            .getChildAt(0))
                    .setBadgeText("NEW"));
        }
    }
}
