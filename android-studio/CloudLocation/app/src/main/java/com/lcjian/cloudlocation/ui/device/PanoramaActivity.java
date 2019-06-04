package com.lcjian.cloudlocation.ui.device;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PanoramaActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.panorama)
    PanoramaView panorama;

    private BMapManager mBMapManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBMapManager = new BMapManager(App.getInstance());
        mBMapManager.init(i -> {

        });
        setContentView(R.layout.activity_panorama);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.panorama));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        panorama.setPanoramaViewListener(new PanoramaViewListener() {
            @Override
            public void onDescriptionLoadEnd(String s) {

            }

            @Override
            public void onLoadPanoramaBegin() {

            }

            @Override
            public void onLoadPanoramaEnd(String s) {

            }

            @Override
            public void onLoadPanoramaError(String s) {

            }

            @Override
            public void onMessage(String s, int i) {

            }

            @Override
            public void onCustomMarkerClick(String s) {

            }

            @Override
            public void onMoveStart() {

            }

            @Override
            public void onMoveEnd() {

            }
        });
        panorama.setPanorama(getIntent().getDoubleExtra("longitude", 0d),
                getIntent().getDoubleExtra("latitude", 0d));
    }

    @Override
    protected void onPause() {
        super.onPause();
        panorama.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panorama.onResume();
    }

    @Override
    protected void onDestroy() {
        panorama.destroy();
        mBMapManager = null;
        super.onDestroy();
    }
}
