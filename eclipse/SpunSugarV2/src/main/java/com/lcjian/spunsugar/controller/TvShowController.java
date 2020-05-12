package com.lcjian.spunsugar.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lcjian.spunsugar.entity.PageResult;
import com.lcjian.spunsugar.entity.TvShow;
import com.lcjian.spunsugar.entity.TvShowGenre;
import com.lcjian.spunsugar.entity.TvShowProductionCountry;
import com.lcjian.spunsugar.entity.TvShowVideo;
import com.lcjian.spunsugar.service.TvShowService;

@RestController
@RequestMapping("/api/tv_shows")
public class TvShowController {

    @Autowired
    private TvShowService tvShowService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public PageResult<TvShow> tvShows(
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
        Page<TvShow> pageTvShow = tvShowService.getTvShows(keyword, genreId, countryId, startReleaseDate, endReleaseDate, startVoteAverage,
                endVoteAverage, sortType, sortDirection, pageNumber - 1, pageSize);
        PageResult<TvShow> pageResult = new PageResult<>();
        pageResult.setElements(pageTvShow.getContent());
        pageResult.setPageNumber(pageNumber);
        pageResult.setPageSize(pageSize);
        pageResult.setTotalPages(pageTvShow.getTotalPages());
        pageResult.setTotalElements(pageTvShow.getTotalElements());
        return pageResult;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public List<TvShow> tvShows(@RequestParam("ids") String ids) {
        if (!StringUtils.isEmpty(ids)) {
            return tvShowService.getTvShows(ids);
        } else {
            return new ArrayList<>();
        }
    }

    @RequestMapping(path = "/{id}/sources", method = RequestMethod.GET)
    public List<TvShowVideo> sources(@PathVariable("id") Integer id) {
        List<TvShowVideo> result = tvShowService.getTvShowVideos(id);
        Collections.sort(result, new Comparator<TvShowVideo>() {
            public int compare(TvShowVideo a, TvShowVideo b) {
                Pattern p = Pattern.compile("[^\\d]([0-9]*)");
                Matcher m = p.matcher(a.getName());
                int aNum = 0;
                if (m.find()) {
                    aNum = Integer.parseInt(m.group(1));
                }
                int bNum = 0;
                m = p.matcher(b.getName());
                if (m.find()) {
                    bNum = Integer.parseInt(m.group(1));
                }
                return aNum - bNum;
            }
        });
        return result;
    }
    
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public TvShow tvShow(@PathVariable("id") Integer id) {
        return tvShowService.get(id);
    }
    
    @RequestMapping(path = "/genres", method = RequestMethod.GET)
    public List<TvShowGenre> genres() {
        return tvShowService.getTvShowGenres();
    }

    @RequestMapping(path = "/production_countries", method = RequestMethod.GET)
    public List<TvShowProductionCountry> productionCountry() {
        return tvShowService.getTvShowProductionCountries();
    }
}
