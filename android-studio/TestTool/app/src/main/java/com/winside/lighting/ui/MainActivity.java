package com.winside.lighting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.winside.lighting.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_curtain)
    Button btn_curtain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        tv_navigation_title.setText(R.string.home);
        btn_curtain.setOnClickListener(v -> startActivity(new Intent(v.getContext(), CurtainActivity.class)));
    }
}
