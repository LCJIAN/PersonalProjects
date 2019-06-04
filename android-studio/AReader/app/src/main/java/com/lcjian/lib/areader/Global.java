package com.lcjian.lib.areader;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.lcjian.lib.areader.util.DimenUtils;

public class Global {

    public static DrawableTransitionOptions crossFade = DrawableTransitionOptions.withCrossFade();

    public static RequestOptions roundedPoster = RequestOptions.placeholderOf(R.drawable.cover_default)
            .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCorners((int) DimenUtils.dipToPixels(3, App.getInstance()))));

    public static RequestOptions roundedPoster2 = RequestOptions.placeholderOf(R.drawable.cover_default)
            .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCorners((int) DimenUtils.dipToPixels(4, App.getInstance()))));
}
