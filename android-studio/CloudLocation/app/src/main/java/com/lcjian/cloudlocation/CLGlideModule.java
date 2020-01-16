package com.lcjian.cloudlocation;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.NonNull;

@GlideModule
public class CLGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context))
                .setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888));
    }
}
