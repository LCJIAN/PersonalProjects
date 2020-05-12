package com.winside.lighting.ui.device;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.winside.lighting.R;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.ui.main.DevicesFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DevicesActivity extends BaseActivity {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_back)
    ImageButton btn_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        ButterKnife.bind(this);

        tv_navigation_title.setText("办公区");
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(v -> onBackPressed());

        getSupportFragmentManager().beginTransaction().add(R.id.fl_fragment_container, new DevicesFragment()).commit();
    }
}
