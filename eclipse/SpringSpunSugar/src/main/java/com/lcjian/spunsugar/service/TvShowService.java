package com.lcjian.spunsugar.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.lcjian.spunsugar.entity.TvShow;
import com.lcjian.spunsugar.entity.TvShowVideo;

public interface TvShowService {
    
    TvShow create(TvShow movie);
    
    TvShow get(Integer movieId);
    
    List<TvShowVideo> getTvShowVideos(Integer tvShowId);
    
    Page<TvShow> getTvShows(
            String keyword,
            Integer genreId,
            Integer countryId,
            String startReleaseDate,
            String endReleaseDate,
            Float startVoteAverage,
            Float endVoteAverage,
            String sortType,
            String sortDirection,
            Integer pageNumber,
            Integer pageSize);

    List<TvShow> getTvShows(String ids);
}
