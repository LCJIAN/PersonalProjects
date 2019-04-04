package com.lcjian.drinkwater.data.network.service;

import com.lcjian.drinkwater.data.network.entity.Recommend;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface SpunSugarService {

    @GET("recommends")
    Observable<List<Recommend>> recommends();
}
