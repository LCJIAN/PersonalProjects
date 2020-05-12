package com.lcjian.vastplayer.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.WebViewActivity;
import com.lcjian.vastplayer.ui.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class WebsiteFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.tv_tx_video)
    TextView tv_tx_video;
    @BindView(R.id.tv_yk_video)
    TextView tv_yk_video;
    @BindView(R.id.tv_aqy_video)
    TextView tv_aqy_video;
    @BindView(R.id.tv_mg_video)
    TextView tv_mg_video;
    @BindView(R.id.tv_sh_video)
    TextView tv_sh_video;
    @BindView(R.id.tv_pp_video)
    TextView tv_pp_video;
    @BindView(R.id.tv_ls_video)
    TextView tv_ls_video;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_website, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_tx_video.setOnClickListener(this);
        tv_yk_video.setOnClickListener(this);
        tv_aqy_video.setOnClickListener(this);
        tv_mg_video.setOnClickListener(this);
        tv_sh_video.setOnClickListener(this);
        tv_pp_video.setOnClickListener(this);
        tv_ls_video.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onClick(View v) {
        String url;
        switch (v.getId()) {
            case R.id.tv_tx_video:
                url = "http://m.v.qq.com/";
                break;
            case R.id.tv_yk_video:
                url = "http://www.youku.com/ ";
                break;
            case R.id.tv_aqy_video:
                url = "http://m.iqiyi.com/";
                break;
            case R.id.tv_mg_video:
                url = "https://m.mgtv.com/";
                break;
            case R.id.tv_sh_video:
                url = "http://m.tv.sohu.com/";
                break;
            case R.id.tv_pp_video:
                url = "http://m.pptv.com/";
                break;
            case R.id.tv_ls_video:
                url = "http://m.le.com/";
                break;
            default:
                url = "http://m.v.qq.com";
                break;
        }
        startActivity(new Intent(v.getContext(), WebViewActivity.class)
                .putExtra("url", url));
    }
}
