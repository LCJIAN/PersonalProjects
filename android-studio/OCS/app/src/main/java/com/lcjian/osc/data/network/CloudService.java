package com.lcjian.osc.data.network;

import com.lcjian.osc.data.network.entity.GEOFences;
import com.lcjian.osc.data.network.entity.QueryModel;
import com.lcjian.osc.data.network.entity.SignInInfo;
import com.lcjian.osc.data.network.entity.SignInModel;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CloudService {

    /**
     * 登陆
     */
    @POST("Account")
    Single<SignInInfo> signIn(@Body SignInModel signInModel);

    /**
     * 预检查询
     */
    @POST("services/ocs/detection/GetPreview")
    Single<GEOFences> getPreview(@Body QueryModel queryModel);

}
