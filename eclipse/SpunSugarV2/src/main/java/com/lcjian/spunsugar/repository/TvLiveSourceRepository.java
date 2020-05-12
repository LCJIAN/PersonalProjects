package com.lcjian.spunsugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lcjian.spunsugar.entity.TvLiveSource;

public interface TvLiveSourceRepository extends JpaRepository<TvLiveSource, Integer>, JpaSpecificationExecutor<TvLiveSource> {


}
