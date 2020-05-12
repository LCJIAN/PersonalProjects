package com.lcjian.spunsugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lcjian.spunsugar.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Integer>, JpaSpecificationExecutor<Movie> {

    Movie findOneByCrawlerId(String crawlerId);

}
