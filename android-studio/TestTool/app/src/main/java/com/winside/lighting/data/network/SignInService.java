package com.winside.lighting.data.network;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SignInService {

    @POST("account/login")
    Single<ResponseData<String>> signInRx(@Body SignInData signInData);
}
