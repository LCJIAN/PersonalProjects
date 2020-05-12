package com.lcjian.spunsugar.service;

import java.util.List;

import com.googlecode.genericdao.search.ISearch;
import com.googlecode.genericdao.search.SearchResult;
import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieVideo;

public interface MovieService {

    public SearchResult<Movie> searchAndCount(ISearch search);
    
    public SearchResult<Movie> search(ISearch search);
    
    public List<MovieVideo> getMovieVideos(Integer id);
}
