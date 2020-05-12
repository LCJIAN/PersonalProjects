package com.lcjian.spunsugar.service;

import java.util.List;

import com.lcjian.spunsugar.entity.Recommend;

public interface RecommendService {
    
    Recommend create(Recommend recommend);

    List<Recommend> findAll();
    
}
