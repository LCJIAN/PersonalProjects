package com.lcjian.vastplayer.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.lcjian.lib.media.design.MediaController;

public class MediaControllerM extends MediaController {

    public MediaControllerM(Context context) {
        super(context);
    }

    public MediaControllerM(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaControllerM(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected String layoutResource() {
        return "media_controller_m";
    }
}
