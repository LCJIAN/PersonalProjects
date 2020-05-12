package com.lcjian.spunsugar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lcjian.spunsugar.entity.TvShow;
import com.lcjian.spunsugar.entity.TvShowVideo;

public interface TvShowVideoRepository extends JpaRepository<TvShowVideo, Integer> {

    TvShowVideo findOneByUrl(String url);
    
    List<TvShowVideo> findAllByTvShow(TvShow tvShow);

}
