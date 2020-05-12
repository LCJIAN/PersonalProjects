package com.lcjian.spunsugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lcjian.spunsugar.entity.TvShow;

public interface TvShowRepository extends JpaRepository<TvShow, Integer>, JpaSpecificationExecutor<TvShow> {

    TvShow findOneByCrawlerId(String crawlerId);

}
