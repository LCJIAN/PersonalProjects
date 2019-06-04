package com.example.newbiechen.ireader.model.remote;

import com.example.newbiechen.ireader.model.bean.packages.BookChapterPackage;
import com.example.newbiechen.ireader.model.bean.packages.ChapterInfoPackage;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by newbiechen on 17-4-20.
 */

public interface BookApi {

    /**
     * 获取书籍的章节总列表
     *
     * @param bookId
     * @param view   默认参数为:chapters
     * @return
     */
    @GET("/mix-atoc/{bookId}")
    Single<BookChapterPackage> getBookChapterPackage(@Path("bookId") String bookId, @Query("view") String view);

    /**
     * 章节的内容
     * 这里采用的是同步请求。
     *
     * @param url
     * @return
     */
    @GET("http://chapter2.zhuishushenqi.com/chapter/{url}")
    Single<ChapterInfoPackage> getChapterInfoPackage(@Path("url") String url);


}
