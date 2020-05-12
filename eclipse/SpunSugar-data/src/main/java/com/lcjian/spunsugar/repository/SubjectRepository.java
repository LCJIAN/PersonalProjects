package com.lcjian.spunsugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lcjian.spunsugar.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Integer>, JpaSpecificationExecutor<Subject> {

    Subject findOneByCrawlerId(String crawlerId);

}
