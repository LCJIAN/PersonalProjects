package com.lcjian.spunsugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lcjian.spunsugar.entity.MovieProductionCountry;

public interface MovieProductionCountryRepository extends JpaRepository<MovieProductionCountry, Integer> {

    MovieProductionCountry findOneByName(String name);

}
