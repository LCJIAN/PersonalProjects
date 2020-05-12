package com.lcjian.spunsugar.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieGenre;
import com.lcjian.spunsugar.entity.MovieProductionCountry;
import com.lcjian.spunsugar.entity.MovieVideo;
import com.lcjian.spunsugar.entity.PageResult;
import com.lcjian.spunsugar.service.MovieService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public PageResult<Movie> movies(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "genre_id", required = false) Integer genreId,
            @RequestParam(value = "country_id", required = false) Integer countryId,
            @RequestParam(value = "start_release_date", required = false) String startReleaseDate,
            @RequestParam(value = "end_release_date", required = false) String endReleaseDate,
            @RequestParam(value = "start_vote_average", required = false) Float startVoteAverage,
            @RequestParam(value = "end_vote_average", required = false) Float endVoteAverage,
            @RequestParam(value = "sort_type", defaultValue = "create_time", required = false) String sortType,
            @RequestParam(value = "sort_direction", defaultValue = "desc", required = false) String sortDirection,
            @RequestParam(value = "page_number", defaultValue = "1", required = false) Integer pageNumber,
            @RequestParam(value = "page_size", defaultValue = "10", required = false) Integer pageSize) {
        Page<Movie> pageMovie = movieService.getMovies(keyword, genreId, countryId, startReleaseDate, endReleaseDate, startVoteAverage,
                endVoteAverage, sortType, sortDirection, pageNumber - 1, pageSize);
        PageResult<Movie> pageResult = new PageResult<>();
        pageResult.setElements(pageMovie.getContent());
        pageResult.setPageNumber(pageNumber);
        pageResult.setPageSize(pageSize);
        pageResult.setTotalPages(pageMovie.getTotalPages());
        pageResult.setTotalElements(pageMovie.getTotalElements());
        return pageResult;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public List<Movie> movies(@RequestParam("ids") String ids) {
        if (!StringUtils.isEmpty(ids)) {
            return movieService.getMovies(ids);
        } else {
            return new ArrayList<>();
        }
    }

    @RequestMapping(path = "/{id}/sources", method = RequestMethod.GET)
    public List<MovieVideo> sources(@PathVariable("id") Integer id) {
        return movieService.getMovieVideos(id);
    }
    
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public Movie movie(@PathVariable("id") Integer id) {
        return movieService.get(id);
    }
    
    @RequestMapping(path = "/genres", method = RequestMethod.GET)
    public List<MovieGenre> movieGenres() {
        return movieService.getMovieGenres();
    }

    @RequestMapping(path = "/production_countries", method = RequestMethod.GET)
    public List<MovieProductionCountry> productionCountry() {
        return movieService.getMovieProductionCountries();
    }
}
