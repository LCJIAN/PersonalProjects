package com.lcjian.spunsugar.service;

import java.util.List;

import com.googlecode.genericdao.search.ISearch;
import com.googlecode.genericdao.search.SearchResult;
import com.lcjian.spunsugar.entity.TvLiveSource;
import com.lcjian.spunsugar.entity.TvStation;

public interface TvStationService {

    public SearchResult<TvStation> searchAndCount(ISearch search);
    
    public List<TvLiveSource> findTvStationSource(String channel);
    
}
