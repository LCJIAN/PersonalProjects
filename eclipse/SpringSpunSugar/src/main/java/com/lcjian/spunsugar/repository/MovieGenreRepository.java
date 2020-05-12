package com.lcjian.spunsugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lcjian.spunsugar.entity.MovieGenre;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Integer> {

    MovieGenre findOneByName(String name);

}
