package com.lcjian.spunsugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lcjian.spunsugar.entity.TvShowProductionCountry;

public interface TvShowProductionCountryRepository extends JpaRepository<TvShowProductionCountry, Integer> {

    TvShowProductionCountry findOneByName(String name);

}
