package com.lcjian.spunsugar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieVideo;

public interface MovieVideoRepository extends JpaRepository<MovieVideo, Integer> {

    MovieVideo findOneByUrl(String url);
    
    List<MovieVideo> findAllByMovie(Movie movie);

}
