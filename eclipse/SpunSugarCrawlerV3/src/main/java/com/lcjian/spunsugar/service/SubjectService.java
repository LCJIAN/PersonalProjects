package com.lcjian.spunsugar.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.lcjian.spunsugar.entity.Genre;
import com.lcjian.spunsugar.entity.ProductionCountry;
import com.lcjian.spunsugar.entity.Subject;
import com.lcjian.spunsugar.entity.Video;

public interface SubjectService {
    
    Subject create(Subject subject);
    
    Subject get(Integer subjectId);
    
    List<Subject> getSubjects(String subjectIds);
    
    List<Video> getVideos(Integer subjectId);
    
    Page<Subject> getSubjects(
            String type,
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

    List<Genre> getGenres(String type);
    
    List<ProductionCountry> getProductionCountries(String type);
    
    public List<Subject> getUncompletedSubjects();
}
