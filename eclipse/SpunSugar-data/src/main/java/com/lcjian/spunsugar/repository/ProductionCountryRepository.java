package com.lcjian.spunsugar.repository;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.lcjian.spunsugar.entity.ProductionCountry;

public interface ProductionCountryRepository extends JpaRepository<ProductionCountry, Integer> {

    List<ProductionCountry> findAllByType(String type);
    
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    ProductionCountry findOneByTypeAndName(String type, String name);
}
