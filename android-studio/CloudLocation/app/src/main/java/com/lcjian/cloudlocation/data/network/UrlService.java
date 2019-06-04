package com.lcjian.cloudlocation.data.network;

import com.lcjian.cloudlocation.data.network.entity.ApiUrl;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface UrlService {

    @GET("api/GpsApp.ashx?action=getapp&Key=7DU2DJFDR8321")
    Single<ApiUrl> getApiUrl();

}
