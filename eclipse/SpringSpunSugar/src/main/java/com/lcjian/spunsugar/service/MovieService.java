package com.lcjian.spunsugar.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieVideo;

public interface MovieService {
    
    Movie create(Movie movie);
    
    Movie get(Integer movieId);
    
    List<MovieVideo> getMovieVideos(Integer movieId);
    
    Page<Movie> getMovies(
            String keyword,
            Integer genreId,
            Integer countryId,
            String startReleaseDate,
            String endReleaseDate,
            Float startVoteAverage,
            Float endVoteAverage,
            String sortType,
            String sortDirection,
            Integer pageNumber,
            Integer pageSize);

    List<Movie> getMovies(String ids);
}
