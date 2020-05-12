package com.lcjian.vastplayer.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.base.BaseBottomSheetDialogFragment;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

public class ShareFragment extends BaseBottomSheetDialogFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View v, @Nullable Bundle savedInstanceState) {
        v.findViewById(R.id.rl_share_qq).setOnClickListener(this);
        v.findViewById(R.id.rl_share_qq_zone).setOnClickListener(this);
        v.findViewById(R.id.rl_share_wei_bo).setOnClickListener(this);
        v.findViewById(R.id.rl_share_we_chat).setOnClickListener(this);
        v.findViewById(R.id.rl_share_moment).setOnClickListener(this);

        v.findViewById(R.id.rl_share_wei_bo).setVisibility(View.GONE);
    }

    @Override
    public void onClick(final View view) {
        Activity activity = getActivity();
        UMWeb umWeb = new UMWeb(mUserInfoSp.getString("share_url", ""),
                mUserInfoSp.getString("share_title", ""),
                mUserInfoSp.getString("share_content", ""),
                new UMImage(activity, R.mipmap.ic_launcher));
        SHARE_MEDIA shareMedia;
        switch (view.getId()) {
            case R.id.rl_share_qq:
                shareMedia = SHARE_MEDIA.QQ;
                break;
            case R.id.rl_share_qq_zone:
                shareMedia = SHARE_MEDIA.QZONE;
                break;
            case R.id.rl_share_wei_bo:
                shareMedia = SHARE_MEDIA.SINA;
                break;
            case R.id.rl_share_moment:
                shareMedia = SHARE_MEDIA.WEIXIN_CIRCLE;
                break;
            case R.id.rl_share_we_chat:
                shareMedia = SHARE_MEDIA.WEIXIN;
                break;
            default:
                shareMedia = SHARE_MEDIA.QQ;
                break;
        }
        new ShareAction(activity).withMedia(umWeb).setPlatform(shareMedia).setCallback(new UMShareListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
            }

            @Override
            public void onResult(SHARE_MEDIA share_media) {
                mUserInfoSp.edit().putBoolean("shared", true).apply();
            }

            @Override
            public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                Toast.makeText(view.getContext(), R.string.share_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media) {
            }
        }).share();
        dismiss();
    }
}
