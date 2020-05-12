package com.lcjian.spunsugar.jersey.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.SearchResult;
import com.googlecode.genericdao.search.Sort;
import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieVideo;
import com.lcjian.spunsugar.entity.PageResult;
import com.lcjian.spunsugar.service.MovieServiceImpl;

@Path("movies")
public class MovieResource {
    
    MovieServiceImpl movieService;
    
    public MovieResource() {
        movieService = new MovieServiceImpl();
    }

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public PageResult<Movie> search(
            @DefaultValue("1") @QueryParam("page") Integer page,
            @DefaultValue("10") @QueryParam("page_size") Integer pageSize,
            @DefaultValue("createTime") @QueryParam("sort_type") String sortType) {
        if (page < 1) {
            throw new BadRequestException("页数必须大于0");
        }
        if (pageSize < 1) {
            throw new BadRequestException("每页大小必须大于0");
        }
        if (!sortType.equals("createTime")
                && !sortType.equals("popularity")
                && !sortType.equals("voteAverage")) {
            throw new BadRequestException("sort_type错误");
        }
        
        Search search = new Search();
        search.setFirstResult((page - 1) * pageSize);
        search.setMaxResults(pageSize);
        List<Sort> sorts = new ArrayList<>();
        sorts.add(new Sort(sortType, true));
        search.setSorts(sorts);
        SearchResult<Movie> searchResult = movieService.search(search);
        PageResult<Movie> pageResult = new PageResult<Movie>();
        pageResult.total_results = searchResult.getTotalCount();
        pageResult.total_pages = searchResult.getTotalCount() % pageSize == 0
                ? searchResult.getTotalCount() / pageSize : searchResult.getTotalCount() / pageSize + 1;
        pageResult.results = searchResult.getResult();
        pageResult.page = page;
        return pageResult;
    }
    
    @GET
    @Path("{id}/sources")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MovieVideo> sources(@PathParam("id") Integer id) {
        return movieService.getMovieVideos(id);
    }
}
