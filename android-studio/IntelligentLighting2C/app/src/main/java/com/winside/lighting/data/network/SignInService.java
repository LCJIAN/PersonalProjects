package com.winside.lighting.data.network;

import com.winside.lighting.data.network.entity.ResponseData;
import com.winside.lighting.data.network.entity.SignInInfo;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface SignInService {

    @FormUrlEncoded
    @POST("user/signInAccount")
    Call<ResponseData<SignInInfo>> signIn(@Field("phone") String userName,
                                          @Field("password") String password);

    @FormUrlEncoded
    @POST("user/signInAccount")
    Single<ResponseData<SignInInfo>> signInRx(@Field("phone") String userName,
                                              @Field("password") String password);
}
