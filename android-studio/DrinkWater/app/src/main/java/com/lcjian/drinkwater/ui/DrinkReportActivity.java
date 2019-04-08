package com.lcjian.drinkwater.ui;

import android.os.Bundle;
import android.view.MenuItem;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.ui.base.BaseActivity;

import androidx.appcompat.app.ActionBar;

public class DrinkReportActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_report);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.ds);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}