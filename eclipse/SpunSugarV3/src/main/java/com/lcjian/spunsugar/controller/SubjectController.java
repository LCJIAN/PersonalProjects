package com.lcjian.spunsugar.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lcjian.spunsugar.dto.GenreDTO;
import com.lcjian.spunsugar.dto.ProductionCountryDTO;
import com.lcjian.spunsugar.dto.SubjectDTO;
import com.lcjian.spunsugar.dto.VideoDTO;
import com.lcjian.spunsugar.entity.PageResult;
import com.lcjian.spunsugar.entity.Subject;
import com.lcjian.spunsugar.entity.Video;
import com.lcjian.spunsugar.service.SubjectService;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public PageResult<SubjectDTO> subjects(
            @RequestParam(value = "type", required = false) String type,
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
        Page<Subject> pageSubject = subjectService.getSubjects(type, keyword, genreId, countryId, startReleaseDate, endReleaseDate, startVoteAverage,
                endVoteAverage, sortType, sortDirection, pageNumber - 1, pageSize);
        PageResult<SubjectDTO> pageResult = new PageResult<>();
        pageResult.setElements(pageSubject.getContent().stream().map(s -> StringUtils.isEmpty(keyword) ? new SubjectDTO(s) : SubjectDTO.getSubjectDTODetail(s))
                .collect(Collectors.toList()));
        pageResult.setPageNumber(pageNumber);
        pageResult.setPageSize(pageSize);
        pageResult.setTotalPages(pageSubject.getTotalPages());
        pageResult.setTotalElements(pageSubject.getTotalElements());
        return pageResult;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public List<SubjectDTO> subjects(@RequestParam("ids") String ids) {
        if (!StringUtils.isEmpty(ids)) {
            return subjectService.getSubjects(ids).stream().map(s -> new SubjectDTO(s)).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @RequestMapping(path = "/{id}/sources", method = RequestMethod.GET)
    public List<VideoDTO> sources(@PathVariable("id") Integer id) {
        List<Video> videos = subjectService.getVideos(id);
        List<VideoDTO> result = videos.stream().map(v -> new VideoDTO(v)).collect(Collectors.toList());
        if (videos != null && !videos.isEmpty()) {
            Collections.sort(result, new Comparator<VideoDTO>() {
                public int compare(VideoDTO a, VideoDTO b) {
                    int aNum = 0;
                    int bNum = 0;
                    Pattern p = Pattern.compile("([0-9]+)");
                    Matcher m = null;
                    if (StringUtils.contains(a.getName(), "备份")) {
                        aNum = Integer.MAX_VALUE;
                    } else if (StringUtils.contains(a.getName(), "备用")) {
                        aNum = Integer.MAX_VALUE -1;
                    } else {
                        m = p.matcher(a.getName());
                        if (m.find()) {
                            aNum = Integer.parseInt(m.group(1));
                        }
                    }
                    if (StringUtils.contains(b.getName(), "备份")) {
                        bNum = Integer.MAX_VALUE;
                    } else if (StringUtils.contains(b.getName(), "备用")) {
                        bNum = Integer.MAX_VALUE -1;
                    } else {
                        m = p.matcher(b.getName());
                        if (m.find()) {
                            bNum = Integer.parseInt(m.group(1));
                        }
                    }
                    return aNum - bNum;
                }
            });
        }
        return result;
    }
    
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public SubjectDTO subject(@PathVariable("id") Integer id) {
        Subject subject = subjectService.get(id);
        SubjectDTO movieDTO = SubjectDTO.getSubjectDTODetail(subject);
        return movieDTO;
    }
    
    @RequestMapping(path = "/genres", method = RequestMethod.GET)
    public List<GenreDTO> subjectGenres(@RequestParam(value = "type", defaultValue = "movie", required = false) String type) {
        return subjectService.getGenres(type).stream().map(g -> new GenreDTO(g)).collect(Collectors.toList());
    }

    @RequestMapping(path = "/production_countries", method = RequestMethod.GET)
    public List<ProductionCountryDTO> productionCountry(@RequestParam(value = "type", defaultValue = "movie", required = false) String type) {
        return subjectService.getProductionCountries(type).stream().map(p -> new ProductionCountryDTO(p)).collect(Collectors.toList());
    }
}
