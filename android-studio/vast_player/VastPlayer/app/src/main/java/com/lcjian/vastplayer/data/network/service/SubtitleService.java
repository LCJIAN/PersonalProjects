package com.lcjian.vastplayer.data.network.service;

import com.lcjian.vastplayer.data.network.entity.Sub;
import com.lcjian.vastplayer.data.network.entity.SubResponse;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SubtitleService {

    @GET("sub/search")
    Observable<SubResponse<List<Sub>>> subSearch(@Query("q") String keyword,
                                                 @Query("pos") int pos,
                                                 @Query("cnt") int cnt);

    @GET("sub/detail")
    Observable<SubResponse<List<Sub>>> subDetail(@Query("id") int subId);
}
