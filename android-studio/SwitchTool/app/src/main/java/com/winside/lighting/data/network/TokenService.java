package com.winside.lighting.data.network;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TokenService {

    @POST("account/valid")
    Single<ResponseData<Object>> validate(@Body TokenData tokenData);
}
