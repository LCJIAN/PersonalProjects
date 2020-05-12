package com.lcjian.vastplayer;

import com.lcjian.lib.util.common.StorageUtils;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class Tmdb extends com.uwetrottmann.tmdb2.Tmdb {

    private static final int DISK_CACHE_SIZE = 20 * 1024 * 1024; // 20MB

    public Tmdb(String apiKey) {
        super(apiKey);
    }

    @Override
    protected synchronized OkHttpClient okHttpClient() {
        return super.okHttpClient().newBuilder().cache(getCache()).build();
    }

    private Cache getCache() {
        Cache cache = null;
        // Install an HTTP cache in the application cache directory.
        try {
            File cacheDir = new File(StorageUtils.getCacheDirectory(App.getInstance()), "http");
            cache = new Cache(cacheDir, DISK_CACHE_SIZE);
        } catch (Exception e) {
            Timber.e(e, "Unable to install disk cache.");
        }
        return cache;
    }
}
