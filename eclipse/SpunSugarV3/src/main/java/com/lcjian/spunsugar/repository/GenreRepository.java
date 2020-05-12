package com.lcjian.spunsugar.repository;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.lcjian.spunsugar.entity.Genre;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

    List<Genre> findAllByType(String type);
    
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Genre findOneByTypeAndName(String type, String name);
}
