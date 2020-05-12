package com.lcjian.spunsugar.dto;

import com.lcjian.spunsugar.entity.ProductionCountry;

public class ProductionCountryDTO {

    private Integer id;
    
    private String type;

    private String name;

    public ProductionCountryDTO() {
    }

    public ProductionCountryDTO(ProductionCountry productionCountry) {
        this.id = productionCountry.getId();
        this.type = productionCountry.getType();
        this.name = productionCountry.getName();
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
