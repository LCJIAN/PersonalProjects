package com.winside.lighting.data.network;

import com.winside.lighting.data.network.entity.Building;
import com.winside.lighting.data.network.entity.Device;
import com.winside.lighting.data.network.entity.Floor;
import com.winside.lighting.data.network.entity.Project;
import com.winside.lighting.data.network.entity.Region;
import com.winside.lighting.data.network.entity.RegionFloorPlanData;
import com.winside.lighting.data.network.entity.ResponseData;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface LightingService {

    @Headers("Content-Type:application/json")
    @POST("user/signDownAccount")
    Single<ResponseData<Object>> signOut();

    @FormUrlEncoded
    @POST("user/modifyPassword")
    Single<ResponseData<Object>> modifyPassword(@Field("oldPassword") String oldPassword,
                                                @Field("newPassword") String newPassword);

    @Headers("Content-Type:application/json")
    @POST("project/getProjectList")
    Single<ResponseData<List<Project>>> getProjects();

    @FormUrlEncoded
    @POST("project/getBuildList")
    Single<ResponseData<List<Building>>> getBuildings(@Field("projectId") Long projectId);

    @FormUrlEncoded
    @POST("project/getFloorList")
    Single<ResponseData<List<Floor>>> getFloors(@Field("buildId") Long buildingId);

    @FormUrlEncoded
    @POST("project/getRegionList")
    Single<ResponseData<List<Region>>> getRegions(@Field("floorId") Long floorId);

    @FormUrlEncoded
    @POST("project/floorData")
    Single<ResponseData<Object>> getFloorData(@Field("floorId") Long floorId);

    @FormUrlEncoded
    @POST("project/regionData")
    Single<ResponseData<RegionFloorPlanData>> getRegionData(@Field("regionId") Long regionId);

    @FormUrlEncoded
    @POST("device/bindPosition")
    Single<ResponseData<Object>> bindPosition(@Field("coordinateId") Long coordinateId,
                                              @Field("deviceSN") String deviceSN);

    @FormUrlEncoded
    @POST("device/getDeviceDetail")
    Single<ResponseData<Device>> getDeviceDetail(@Field("deviceId") Long deviceId);

    @FormUrlEncoded
    @POST("device/noticeConfigResult")
    Single<ResponseData<Object>> noticeConfigResult(@Field("deviceIds") String deviceIds);

    @FormUrlEncoded
    @POST("device/unbindPositionDevice")
    Single<ResponseData<Object>> deleteDevice(@Field("coordinateId") Long coordinateId);

    @FormUrlEncoded
    @POST("device/getAllDeviceDetail")
    Single<ResponseData<List<Device>>> getDevices(@Field("gwDeviceId") Long gwDeviceId);

    @FormUrlEncoded
    @POST("device/sendToDevice")
    Single<ResponseData<Object>> sendToDevice(@Field("deviceId") Long deviceId,
                                              @Field("message") String message);
}
