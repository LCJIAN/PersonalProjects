package com.lcjian.osc.data.network;

import com.lcjian.osc.data.network.entity.DetectionInfo;
import com.lcjian.osc.data.network.entity.DetectionRequestData;
import com.lcjian.osc.data.network.entity.PictureRequestData;
import com.lcjian.osc.data.network.entity.Pictures;
import com.lcjian.osc.data.network.entity.ResponseData;
import com.lcjian.osc.data.network.entity.SignInRequestData;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CloudService {

    /**
     * 登陆
     */
    @POST("Account")
    Single<ResponseData<String>> signIn(@Body SignInRequestData signInRequestData);

    /**
     * 登陆
     */
    @POST("Account")
    Call<ResponseData<String>> signInSync(@Body SignInRequestData signInRequestData);

    /**
     * 预检查询
     */
    @POST("services/ocs/detection/GetPreview")
    Single<ResponseData<DetectionInfo>> getPreview(@Body DetectionRequestData detectionRequestData);

    /**
     * 查看图片
     */
    @POST("services/ocs/detection/ShowPicture")
    Single<ResponseData<Pictures>> getPictures(@Body PictureRequestData pictureRequestData);
}
