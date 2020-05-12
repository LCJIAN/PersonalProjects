package com.lcjian.spunsugar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lcjian.spunsugar.entity.TvStation;

public interface TvStationRepository extends JpaRepository<TvStation, Integer>, JpaSpecificationExecutor<TvStation> {

    List<TvStation> findAllByType(String type);
    
    TvStation findOneByChannel(String channel);
}
