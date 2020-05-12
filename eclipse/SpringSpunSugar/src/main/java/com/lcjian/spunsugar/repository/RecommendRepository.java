package com.lcjian.spunsugar.repository;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

import com.lcjian.spunsugar.entity.Recommend;

public interface RecommendRepository extends JpaRepository<Recommend, Integer>, JpaSpecificationExecutor<Recommend> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    List<Recommend> findAllBySubjectId(Integer subjectId);
 
    List<Recommend> findAllByOrderByCreateTimeDesc();
}
