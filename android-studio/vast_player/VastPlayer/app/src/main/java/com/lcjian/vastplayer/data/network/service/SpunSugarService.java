package com.lcjian.vastplayer.data.network.service;

import com.lcjian.vastplayer.data.network.entity.Country;
import com.lcjian.vastplayer.data.network.entity.Genre;
import com.lcjian.vastplayer.data.entity.PageResult;
import com.lcjian.vastplayer.data.network.entity.Recommend;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.data.network.entity.TvStation;
import com.lcjian.vastplayer.data.network.entity.VideoUrl;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpunSugarService {

    @GET("configs")
    Observable<Map<String, String>> configs();

    @GET("recommends")
    Observable<List<Recommend>> recommends();

    @GET("subjects/search")
    Observable<PageResult<Subject>> subjects(@Query("type") String type,
                                             @Query("keyword") String keyword,
                                             @Query("genre_id") Integer genreId,
                                             @Query("country_id") Integer countryId,
                                             @Query("start_release_date") String startReleaseDate,
                                             @Query("end_release_date") String endReleaseDate,
                                             @Query("start_vote_average") Float startVoteAverage,
                                             @Query("end_vote_average") Float endVoteAverage,
                                             @Query("sort_type") String sortType,
                                             @Query("sort_direction") String sortDirection,
                                             @Query("page_number") Integer pageNumber,
                                             @Query("page_size") Integer pageSize);

    @GET("subjects")
    Observable<List<Subject>> subjects(@Query("ids") String ids);

    @GET("subjects/{id}")
    Observable<Subject> subjectDetail(@Path("id") Long id);

    @GET("subjects/{id}/sources")
    Observable<List<VideoUrl>> subjectSources(@Path("id") Long id);

    @GET("subjects/genres")
    Observable<List<Genre>> subjectGenres(@Query("type") String type);

    @GET("subjects/production_countries")
    Observable<List<Country>> subjectCountries(@Query("type") String type);

    @GET("parser/{file_name}")
    Observable<ResponseBody> parser(@Path("file_name") String fileName);

    @GET("tv_stations")
    Observable<List<TvStation>> tvStations(@Query("ids") String ids);

    @GET("tv_stations/{channel}/sources")
    Observable<List<VideoUrl>> tvSources(@Path("channel") String channel);
}
