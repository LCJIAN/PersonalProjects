package com.winside.lighting.data.network;

import com.winside.lighting.data.network.entity.Building;
import com.winside.lighting.data.network.entity.Floor;
import com.winside.lighting.data.network.entity.Project;
import com.winside.lighting.data.network.entity.Region;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface LightingService {

    @Headers("Content-Type:application/json")
    @POST("project/getProjectList")
    Single<List<Project>> getProjects();

    @FormUrlEncoded
    @POST("project/getBuildList")
    Single<List<Building>> getBuildings(@Field("projectId") Long projectId);

    @FormUrlEncoded
    @POST("project/getFloorList")
    Single<List<Floor>> getFloors(@Field("buildId") Long buildingId);

    @FormUrlEncoded
    @POST("project/getRegionList")
    Single<List<Region>> getRegions(@Field("buildId") Long floorId);
}
