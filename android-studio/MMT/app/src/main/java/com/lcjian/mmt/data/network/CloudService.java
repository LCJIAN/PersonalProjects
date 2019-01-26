package com.lcjian.mmt.data.network;

import com.lcjian.mmt.data.network.entity.ResponseData;
import com.lcjian.mmt.data.network.entity.User;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface CloudService {

    /**
     * 获取短信验证码
     */
    @FormUrlEncoded
    @POST("mmt/user/getsmscode")
    Single<ResponseData<String>> sendVerificationCode(@Field("mobile") String phone);

    /**
     * 校验短信验证码有效
     */
    @FormUrlEncoded
    @POST("mmt/user/checksmscode")
    Single<ResponseData<String>> checkVerificationCode(@Field("mobile") String phone,
                                                       @Field("smscode") String verificationCode);

    /**
     * 用户注册
     */
    @FormUrlEncoded
    @POST("mmt/user/checksmscode")
    Single<ResponseData<String>> signUp(@Field("mobile") String phone,
                                        @Field("smscode") String verificationCode,
                                        @Field("password") String password);

    /**
     * 登陆
     */
    @FormUrlEncoded
    @POST("mmt/login/mobile")
    Single<ResponseData<User>> signIn(@Field("username") String phone,
                                      @Field("password") String password);

}
