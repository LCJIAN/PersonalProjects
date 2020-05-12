package com.lcjian.spunsugar.service;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lcjian.spunsugar.entity.Recommend;
import com.lcjian.spunsugar.repository.RecommendRepository;

@Service
public class RepositoryRecommendService implements RecommendService {

    @Autowired
    private RecommendRepository recommendRepository;

    @Transactional
    @Override
    public Recommend create(Recommend recommend) {
        List<Recommend> recommends = recommendRepository.findAllBySubjectId(recommend.getSubjectId());
        Specification<Recommend> spec = new Specification<Recommend>() {
            
            @Override
            public Predicate toPredicate(Root<Recommend> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.and(cb.equal(root.get("title").as(String.class), recommend.getTitle()),
                        cb.equal(root.get("type").as(String.class), recommend.getType()));
            }
        };
        int limit = StringUtils.equals(recommend.getTitle(), "banner") ? 6 : 12;
        if (recommendRepository.count(spec) > limit) {
            recommendRepository.delete(recommendRepository.findAll(spec,
                    new Sort(new Order(Direction.ASC, "createTime"))).get(0));
        }
        if (recommends.isEmpty()) {
            return recommendRepository.save(recommend);
        } else {
            return recommends.get(0);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Recommend> findAll() {
        return recommendRepository.findAllByOrderByCreateTimeDesc();
    }
}
