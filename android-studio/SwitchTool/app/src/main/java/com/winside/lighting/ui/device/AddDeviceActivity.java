package com.winside.lighting.ui.device;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.winside.lighting.GlideApp;
import com.winside.lighting.R;
import com.winside.lighting.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddDeviceActivity extends BaseActivity {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.iv_background)
    ImageView iv_background;

    private AddDeviceViewModel mAddDeviceViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ButterKnife.bind(this);

        mAddDeviceViewModel = ViewModelProviders.of(this).get(AddDeviceViewModel.class);

        tv_navigation_title.setText(R.string.add_device);
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(v -> onBackPressed());
        GlideApp.with(this)
                .load(R.drawable.background)
                .centerCrop()
                .into(iv_background);
        getSupportFragmentManager().beginTransaction().add(R.id.fl_fragment_container, new PreSearchingFragment()).commit();

        setupContent();
    }

    private void setupContent() {
        mAddDeviceViewModel.getSearching().observe(this, aBoolean -> {
            if (aBoolean) {
                {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("SearchingDialogFragment");
                    if (fragment == null) {
                        new SearchingDialogFragment().show(getSupportFragmentManager(), "SearchingDialogFragment");
                    }
                }
                {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("SearchResultFragment");
                    if (fragment == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fl_fragment_container, new SearchResultFragment(), "SearchResultFragment")
                                .commit();
                    }
                }
            } else {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("SearchingDialogFragment");
                if (fragment != null) {
                    ((SearchingDialogFragment) fragment).dismiss();
                }
            }
        });
    }
}
