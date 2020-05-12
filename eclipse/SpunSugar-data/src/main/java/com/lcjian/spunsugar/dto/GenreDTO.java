package com.lcjian.spunsugar.dto;

import com.lcjian.spunsugar.entity.Genre;

public class GenreDTO {

    private Integer id;
    
    private String type;

    private String name;

    public GenreDTO() {
    }

    public GenreDTO(Genre genre) {
        this.id = genre.getId();
        this.type = genre.getType();
        this.name = genre.getName();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
