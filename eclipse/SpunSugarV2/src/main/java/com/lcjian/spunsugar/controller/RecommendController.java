package com.lcjian.spunsugar.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcjian.spunsugar.dto.MovieDTO;
import com.lcjian.spunsugar.dto.RecommendDTO;
import com.lcjian.spunsugar.dto.TvShowDTO;
import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.Recommend;
import com.lcjian.spunsugar.entity.TvShow;
import com.lcjian.spunsugar.service.MovieService;
import com.lcjian.spunsugar.service.RecommendService;
import com.lcjian.spunsugar.service.TvShowService;

@RestController
@RequestMapping("/api/recommends")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private TvShowService tvShowService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(method = RequestMethod.GET)
    public List<RecommendDTO> getConfigs() {
        List<Recommend> recommends = recommendService.findAll();
        return recommends.stream().map(r -> {
            RecommendDTO recommendDTO = new RecommendDTO();
            recommendDTO.setTitle(r.getTitle());
            recommendDTO.setType(r.getType());
            recommendDTO.setExtra(r.getExtra());
            recommendDTO.setExtra1(r.getExtra1());
            if (StringUtils.equals("movie", r.getType())) {
                Movie movie = movieService.get(r.getSubjectId());
                String movieStr = "";
                try {
                    movieStr = objectMapper.writeValueAsString(new MovieDTO(movie));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                recommendDTO.setData(movieStr);
            } else if (StringUtils.equals("tv_show", r.getType())) {
                TvShow tvShow = tvShowService.get(r.getSubjectId());
                String tvShowStr = "";
                try {
                    tvShowStr = objectMapper.writeValueAsString(new TvShowDTO(tvShow));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                recommendDTO.setData(tvShowStr);
            }
            return recommendDTO;
        }).collect(Collectors.toList());
    }
}
