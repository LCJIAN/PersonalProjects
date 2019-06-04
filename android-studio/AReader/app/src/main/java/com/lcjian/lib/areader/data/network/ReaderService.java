package com.lcjian.lib.areader.data.network;

import com.lcjian.lib.areader.data.entity.Book;
import com.lcjian.lib.areader.data.entity.BookCategoryChild;
import com.lcjian.lib.areader.data.entity.BookCategoryGroup;
import com.lcjian.lib.areader.data.entity.BookDetailResult;
import com.lcjian.lib.areader.data.entity.BookGroup;
import com.lcjian.lib.areader.data.entity.BooksReadInfo;
import com.lcjian.lib.areader.data.entity.Chapter;
import com.lcjian.lib.areader.data.entity.RankType;
import com.lcjian.lib.areader.data.entity.RequestData;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.entity.SearchKeyword;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ReaderService {

    @GET("rank")
    Observable<ResponseData<BookGroup>> rank(@Query("version") Integer version);

    @GET("rank/youLike")
    Observable<ResponseData<List<Book>>> guessYouLike(@Query("page") Integer pageNumber,
                                                      @Query("length") Integer pageSize);

    /**
     * 小说分类
     */
    @GET("category")
    Observable<ResponseData<Map<String, BookCategoryGroup>>> categories();

    /**
     * 小说子分类
     */
    @GET("category/catelist")
    Observable<ResponseData<List<BookCategoryChild>>> childCategories(@Query("cate_id") Long cid);

    /**
     * 书籍一览
     *
     * @param cid    子分类类别ID
     * @param status 状态（-1:全部 0:连载 1：完结）
     */
    @GET("category/bookData")
    Observable<ResponseData<List<Book>>> getBooksByCategory(@Query("cate_id") Long cid,
                                                            @Query("st") Integer status,
                                                            @Query("page") Integer pageNumber,
                                                            @Query("length") Integer pageSize);

    /**
     * 排行榜分类
     */
    @GET("rank/cateleft")
    Observable<ResponseData<List<RankType>>> rankTypes();

    /**
     * 书籍一览
     *
     * @param gender 性别（1:男 2:女）
     */
    @GET("rank/rightbook")
    Observable<ResponseData<List<Book>>> getBooksByRank(@Query("type") Integer type,
                                                        @Query("gender") Integer gender,
                                                        @Query("page") Integer pageNumber);

    /**
     * 热门搜索
     */
    @POST("search")
    Observable<ResponseData<List<SearchKeyword>>> hotSearch(@Body RequestBody body);

    /**
     * 搜索
     */
    @POST("search")
    Observable<ResponseData<List<Book>>> search(@Body RequestBody body);

    /**
     * 详情内容
     */
    @GET("detail")
    Observable<ResponseData<BookDetailResult>> getBookDetail(@Query("book_id") Long bookId);

    /**
     * 章节列表
     */
    @GET("read/catalog")
    Observable<ResponseData<List<Chapter>>> getBookChapters(@Query("book_id") Long bookId);

    /**
     * 章节内容
     */
    @GET("read/content")
    Observable<ResponseData<String>> getBookChapterContent(@Query("book_id") Long bookId,
                                                           @Query("index") Long index);

    /**
     * Bookshelf
     */
    @POST("read/bookshelf")
    Observable<ResponseData<List<Book>>> getBookshelf(@Body RequestData<BooksReadInfo> body);

}
