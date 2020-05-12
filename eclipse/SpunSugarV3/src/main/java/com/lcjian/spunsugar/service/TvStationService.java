package com.lcjian.spunsugar.service;

import java.util.List;

import com.lcjian.spunsugar.entity.TvLiveSource;
import com.lcjian.spunsugar.entity.TvStation;

public interface TvStationService {
    
    TvStation create(TvStation tvStation);
    
    List<TvStation> getTvStations();
    
    List<TvLiveSource> getTvLiveSourcesByChannel(String channel);

    List<TvStation> getTvStationsByIds(String ids);
    
}
