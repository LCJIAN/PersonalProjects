package com.lcjian.vastplayer;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.lcjian.lib.util.common.DimenUtils;

public class Global {

    public static DrawableTransitionOptions crossFade = DrawableTransitionOptions.withCrossFade();

    public static RequestOptions moviePoster = RequestOptions.placeholderOf(R.drawable.placeholder_movie)
            .centerCrop();

    public static RequestOptions roundedPoster = RequestOptions.placeholderOf(R.drawable.color_video_place_holder)
            .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCorners((int) DimenUtils.dipToPixels(8, App.getInstance()))));

    public static RequestOptions centerCrop = RequestOptions.centerCropTransform();

    public static DrawableTransitionOptions dontTransition = new DrawableTransitionOptions().dontTransition();
}
