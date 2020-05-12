package com.lcjian.spunsugar.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.lcjian.spunsugar.entity.Subject;

public class SubjectDTO {
    
    private Integer id;
    
    private String type;

    private String title;

    private String overview;

    private Float voteAverage;

    private Float popularity;

    private LocalDate releaseDate;

    private LocalDateTime createTime;
    
    private List<PosterDTO> posters;
    
    private List<ThumbnailDTO> thumbnails;
    
    private List<GenreDTO> genres;
    
    private List<ProductionCountryDTO> productionCountries;
    
    private Map<String, String> properties;

    public SubjectDTO() {
    }
    
    public SubjectDTO(Subject subject) {
        this.id = subject.getId();
        this.type = subject.getType();
        this.title = subject.getTitle();
        this.voteAverage = subject.getVoteAverage();
        this.releaseDate = subject.getReleaseDate();
        if (StringUtils.equals("video", this.type)) {
            this.thumbnails = subject.getThumbnails().stream().map(t -> new ThumbnailDTO(t)).collect(Collectors.toList());
        } else {
            this.posters = subject.getPosters().stream().map(p -> new PosterDTO(p)).collect(Collectors.toList());
        }
    }
    
    public static SubjectDTO getSubjectDTODetail(Subject subject) {
        SubjectDTO dto = new SubjectDTO(subject);
        dto.setOverview(subject.getOverview());
        dto.setGenres(subject.getGenres().stream().map(g -> new GenreDTO(g)).collect(Collectors.toList()));
        dto.setProductionCountries(subject.getProductionCountries().stream().map(p -> new ProductionCountryDTO(p)).collect(Collectors.toList()));
        Map<String, String> properties = new HashMap<>();
        subject.getProperties().stream().forEach(p -> properties.put(p.getId().getKey(), p.getValue()));
        dto.setProperties(properties);
        return dto;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Float getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public Float getPopularity() {
        return popularity;
    }

    public void setPopularity(Float popularity) {
        this.popularity = popularity;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<PosterDTO> getPosters() {
        return posters;
    }

    public void setPosters(List<PosterDTO> posters) {
        this.posters = posters;
    }

    public List<ThumbnailDTO> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<ThumbnailDTO> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public List<GenreDTO> getGenres() {
        return genres;
    }

    public void setGenres(List<GenreDTO> genres) {
        this.genres = genres;
    }

    public List<ProductionCountryDTO> getProductionCountries() {
        return productionCountries;
    }

    public void setProductionCountries(List<ProductionCountryDTO> productionCountries) {
        this.productionCountries = productionCountries;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
