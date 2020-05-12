package com.lcjian.spunsugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lcjian.spunsugar.entity.TvShowGenre;

public interface TvShowGenreRepository extends JpaRepository<TvShowGenre, Integer> {

    TvShowGenre findOneByName(String name);

}
