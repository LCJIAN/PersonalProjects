package com.lcjian.vastplayer;

import android.content.Context;
import android.text.TextUtils;

import com.lcjian.lib.VideoSniffer;
import com.lcjian.lib.util.common.FileUtils;
import com.lcjian.vastplayer.data.network.RestAPI;
import com.lcjian.vastplayer.data.network.entity.VideoUrl;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dalvik.system.DexClassLoader;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class Utils {

    public static Observable<List<String>> createParseObservable(final Context context, final VideoUrl videoUrl, final RestAPI restAPI) {

        Observable<DexClassLoader> dexClassLoaderObservable = Observable
                .defer(() -> {
                    File file = new File(context.getExternalFilesDir("parser"), "parser.dex");
                    if (file.exists()) {
                        return Observable.just(file);
                    } else {
                        return restAPI.spunSugarService()
                                .parser("parser.dex")
                                .flatMap(responseBody -> {
                                    File aFile = new File(context.getExternalFilesDir("parser"), "parser.dex");
                                    FileUtils.writeFile(aFile.getAbsolutePath(), responseBody.byteStream());
                                    return Observable.just(aFile);
                                });
                    }
                })
                .flatMap(file -> {
                    File optimizedDirectory = context.getDir("parser", Context.MODE_PRIVATE);
                    DexClassLoader dexClassLoader = new DexClassLoader(
                            file.getAbsolutePath(),
                            optimizedDirectory.getAbsolutePath(),
                            null,
                            context.getClassLoader());
                    return Observable.just(dexClassLoader);
                });
        return Observable.zip(
                Observable.just(videoUrl),
                dexClassLoaderObservable,
                Tuple::new)
                .flatMap(tuple -> {
                    List<String> urls = new ArrayList<>();
                    try {
                        Class<?> parserClass;
                        if ("tv".equals(tuple.x.type)) {
                            parserClass = tuple.y.loadClass("com.lcjian.parser.tv.TVParser");
                        } else {
                            parserClass = tuple.y.loadClass("com.lcjian.parser.tv.VideoParser");
                        }
                        Method method = parserClass.getMethod("parse", String.class, String.class);
                        String url = (String) method.invoke(parserClass.newInstance(), tuple.x.site, tuple.x.url);
                        if (!TextUtils.isEmpty(url)) {
                            urls.add(url);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (urls.isEmpty()) {
                        throw new RuntimeException("sequence no elements");
                    }
                    return Observable.just(urls);
                })
                .retryWhen(observable -> observable
                        .zipWith(Observable.range(1, 2), Tuple::new)
                        .flatMap(throwableIntegerTuple -> {
                            if (throwableIntegerTuple.y == 2) {
                                return Observable.<Boolean>error(throwableIntegerTuple.x);
                            } else {
                                return Observable.just(new File(context.getExternalFilesDir("parser"), "parser.dex").delete()
                                        && FileUtils.deleteFile(context.getDir("parser", Context.MODE_PRIVATE).getAbsolutePath()));
                            }
                        })
                );
    }

    public static Observable<List<String>> createSnifferObservable(Context context, String url, boolean mobile) {
        return Observable.<List<String>>create(emitter -> {
            final VideoSniffer videoSniffer = new VideoSniffer(context, url, mobile, new VideoSniffer.Listener() {

                @Override
                public void onSniffStarted() {

                }

                @Override
                public void onSniffFinished() {
                    emitter.onComplete();
                }

                @Override
                public void onSniffCanceled() {
                }

                @Override
                public void onSuccess(String url) {
                    emitter.onNext(Collections.singletonList(url));
                }
            });
            videoSniffer.start();
            emitter.setCancellable(videoSniffer::cancel);
        }).firstOrError().toObservable().subscribeOn(AndroidSchedulers.mainThread());
    }

    private static class Tuple<X, Y> {
        private final X x;
        private final Y y;

        private Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }
}
