package com.org.firefighting.ui.resource;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.lcjian.lib.content.SimpleFragmentPagerAdapter;
import com.org.firefighting.R;
import com.org.firefighting.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResourceDetailActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tab_resource_detail)
    TabLayout tab_resource_detail;
    @BindView(R.id.vp_resource_detail)
    ViewPager vp_resource_detail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_detail);
        ButterKnife.bind(this);
        String resourceId = getIntent().getStringExtra("resource_id");

        btn_back.setOnClickListener(v -> onBackPressed());
        tv_title.setText(getIntent().getStringExtra("resource_table_comment"));

        vp_resource_detail.setOffscreenPageLimit(3);
        vp_resource_detail.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager())
                .addFragment(ResourceBasicInfoFragment.newInstance(resourceId), "基本信息")
                .addFragment(DataFieldFragment.newInstance(resourceId), "数据项")
                .addFragment(DataQueryFragment.newInstance(resourceId), "数据预览"));
        tab_resource_detail.setupWithViewPager(vp_resource_detail);
    }

}
