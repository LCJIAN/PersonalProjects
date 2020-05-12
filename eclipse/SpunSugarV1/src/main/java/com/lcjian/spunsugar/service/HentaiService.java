package com.lcjian.spunsugar.service;

import com.googlecode.genericdao.search.ISearch;
import com.googlecode.genericdao.search.SearchResult;
import com.lcjian.spunsugar.entity.HentaiAnimeEpisode;

public interface HentaiService {

    public SearchResult<HentaiAnimeEpisode> searchAndCountEpisode(ISearch search);
    
}
