package com.lcjian.spunsugar.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lcjian.spunsugar.entity.Genre;
import com.lcjian.spunsugar.entity.Poster;
import com.lcjian.spunsugar.entity.ProductionCountry;
import com.lcjian.spunsugar.entity.Property;
import com.lcjian.spunsugar.entity.PropertyPK;
import com.lcjian.spunsugar.entity.Subject;
import com.lcjian.spunsugar.entity.Thumbnail;
import com.lcjian.spunsugar.entity.Video;
import com.lcjian.spunsugar.repository.GenreRepository;
import com.lcjian.spunsugar.repository.PosterRepository;
import com.lcjian.spunsugar.repository.ProductionCountryRepository;
import com.lcjian.spunsugar.repository.PropertyRepository;
import com.lcjian.spunsugar.repository.SubjectRepository;
import com.lcjian.spunsugar.repository.ThumbnailRepository;
import com.lcjian.spunsugar.repository.VideoRepository;

@Service
public class RepositorySubjectService implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private PosterRepository posterRepository;
    
    @Autowired
    private ThumbnailRepository thumbnailRepository;
    
    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ProductionCountryRepository productionCountryRepository;

    @Transactional
    @Override
    public Subject create(Subject outterSubject) {
        Set<Video> outterVideos = outterSubject.getVideos();
        Set<Poster> outterPosters = outterSubject.getPosters();
        Set<Thumbnail> outterThumbnails = outterSubject.getThumbnails();
        Set<Property> outterProperties = outterSubject.getProperties();
        Subject innerSubject = subjectRepository.findOneByCrawlerId(outterSubject.getCrawlerId());
        if (innerSubject == null) {
            if (outterSubject.getGenres() != null) {
                HashSet<Genre> tempGenres = new HashSet<>();
                for (Genre genre : outterSubject.getGenres()) {
                    tempGenres.add(createGenre(genre));
                }
                outterSubject.setGenres(tempGenres);
            }
            if (outterSubject.getProductionCountries() != null) {
                HashSet<ProductionCountry> tempProductionCountries = new HashSet<>();
                for (ProductionCountry productionCountry : outterSubject.getProductionCountries()) {
                    tempProductionCountries.add(createProductionCountry(productionCountry));
                }
                outterSubject.setProductionCountries(tempProductionCountries);
            }
            outterSubject.setVideos(null);
            outterSubject.setPosters(null);
            outterSubject.setThumbnails(null);
            outterSubject.setProperties(null);
            innerSubject = subjectRepository.save(outterSubject);
            
            if (outterVideos != null) {
                for (Video video : outterVideos) {
                    video.setSubject(innerSubject);
                    videoRepository.save(video);
                }
            }
            if (outterPosters != null) {
                for (Poster poster : outterPosters) {
                    poster.setSubject(innerSubject);
                    posterRepository.save(poster);
                }
            }
            if (outterThumbnails != null) {
                for (Thumbnail thumbnail : outterThumbnails) {
                    thumbnail.setSubject(innerSubject);
                    thumbnailRepository.save(thumbnail);
                }
            }
            if (outterProperties != null) {
                for (Property property : outterProperties) {
                    property.setSubject(innerSubject);
                    propertyRepository.save(property);
                }
            }
        } else {
            boolean updateSubject = false;
            if (outterProperties != null) {
                final Subject finalInnerSubject = innerSubject;
                Set<Property> innerProperties = finalInnerSubject.getProperties();
                
                Set<Property> deleteProperties = innerProperties.stream()
                        .filter(i -> outterProperties.stream()
                                .allMatch(o -> {
                                    PropertyPK oPK = o.getId();
                                    oPK.setSubjectId(finalInnerSubject.getId());
                                    return !Objects.equals(oPK, i.getId());
                                }))
                        .filter(p -> !StringUtils.equals("updated_episodes", p.getId().getKey()))
                        .filter(p -> !StringUtils.equals("total_episodes", p.getId().getKey()))
                        .filter(p -> !StringUtils.equals("completed", p.getId().getKey()))
                        .collect(Collectors.toSet());
                
                Set<Property> updateProperties = innerProperties.stream()
                        .filter(i -> outterProperties.stream()
                                .anyMatch(o -> {
                                    PropertyPK oPK = o.getId();
                                    oPK.setSubjectId(finalInnerSubject.getId());
                                    boolean update = Objects.equals(oPK, i.getId())
                                            && !StringUtils.equals(o.getValue(), i.getValue());
                                    if (update) {
                                        i.setValue(o.getValue());
                                    }
                                    return update;
                                }))
                        .collect(Collectors.toSet());
                
                Set<Property> insertProperties = outterProperties.stream()
                        .filter(o -> innerProperties.stream()
                                .allMatch(i -> {
                                    PropertyPK oPK = o.getId();
                                    oPK.setSubjectId(finalInnerSubject.getId());
                                    return !Objects.equals(i.getId(), o.getId());
                                }))
                        .map(v -> {v.setSubject(finalInnerSubject);return v;})
                        .collect(Collectors.toSet());
                
                // http://blog.csdn.net/yuzhenyuan1/article/details/45078243
                finalInnerSubject.getProperties().clear();
                
                if ((deleteProperties != null && !deleteProperties.isEmpty())
                        || (updateProperties != null && !updateProperties.isEmpty())
                        || (insertProperties != null && !insertProperties.isEmpty())) {
                    innerSubject.setCreateTime(outterSubject.getCreateTime());
                    updateSubject = true;
                }
                propertyRepository.delete(deleteProperties);
                propertyRepository.save(updateProperties);
                propertyRepository.save(insertProperties);
            }
            if (outterVideos != null) {
                final Subject finalInnerSubject = innerSubject;
                Set<Video> innerVideos = finalInnerSubject.getVideos();
                
                Set<Video> deleteVideos = innerVideos.stream()
                        .filter(v -> outterVideos.stream()
                                .allMatch(t -> !StringUtils.equals(t.getUrl(), v.getUrl())))
                        .collect(Collectors.toSet());
                
                Set<Video> updateVideos = innerVideos.stream()
                        .filter(v -> outterVideos.stream()
                                .anyMatch(t -> {
                                    boolean update = StringUtils.equals(t.getUrl(), v.getUrl())
                                            && !StringUtils.equals(v.getName(), t.getName());
                                    if (update) {
                                        v.setName(t.getName());
                                    }
                                    return update;
                                }))
                        .collect(Collectors.toSet());
                
                Set<Video> insertMvs = outterVideos.stream()
                        .filter(v -> innerVideos.stream()
                                .allMatch(t -> !StringUtils.equals(t.getUrl(), v.getUrl())))
                        .map(v -> {v.setSubject(finalInnerSubject);return v;})
                        .collect(Collectors.toSet());
                
                // http://blog.csdn.net/yuzhenyuan1/article/details/45078243
                finalInnerSubject.getVideos().clear();
                
                if ((deleteVideos != null && !deleteVideos.isEmpty())
                        || (updateVideos != null && !updateVideos.isEmpty())
                        || (insertMvs != null && !insertMvs.isEmpty())) {
                    innerSubject.setCreateTime(outterSubject.getCreateTime());
                    updateSubject = true;
                }
                videoRepository.delete(deleteVideos);
                videoRepository.save(updateVideos);
                videoRepository.save(insertMvs);
            }
            if (!Objects.equals(innerSubject.getVoteAverage(), outterSubject.getVoteAverage())) {
                innerSubject.setVoteAverage(outterSubject.getVoteAverage());
                updateSubject = true;
            }
            if (!Objects.equals(innerSubject.getPopularity(), outterSubject.getPopularity())) {
                innerSubject.setPopularity(outterSubject.getPopularity());
                updateSubject = true;
            }
            if (updateSubject) {
                innerSubject = subjectRepository.save(innerSubject);
            }
        }
        return innerSubject;
    }
    
    @Transactional
    private Genre createGenre(Genre genre) {
        Genre mg = genreRepository.findOneByTypeAndName(genre.getType(), genre.getName());
        if (mg == null) {
            mg = genreRepository.save(genre);
        }
        return mg;
    }
    
    @Transactional
    private ProductionCountry createProductionCountry(ProductionCountry productionCountry) {
        ProductionCountry mp = productionCountryRepository.findOneByTypeAndName(productionCountry.getType(),
                productionCountry.getName());
        if (mp == null) {
            mp = productionCountryRepository.save(productionCountry);
        }
        return mp;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> getVideos(Integer subjectId) {
        return new ArrayList<>(subjectRepository.findOne(subjectId).getVideos());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Subject> getSubjects(String type, String keyword, Integer genreId, Integer countryId, String startReleaseDate,
            String endReleaseDate, Float startVoteAverage, Float endVoteAverage, String sortType, String sortDirection,
            Integer pageNumber, Integer pageSize) {
        Order order;
        if (StringUtils.equalsIgnoreCase("asc", sortDirection)) {
            if (StringUtils.equalsIgnoreCase("release_date", sortType)) {
                order = new Order(Direction.ASC, "releaseDate");
            } else if (StringUtils.equalsIgnoreCase("popularity", sortType)) {
                order = new Order(Direction.ASC, "popularity");
            } else if (StringUtils.equalsIgnoreCase("vote_average", sortType)) {
                order = new Order(Direction.ASC, "voteAverage");
            } else {
                order = new Order(Direction.ASC, "createTime");
            }
        } else {
            if (StringUtils.equalsIgnoreCase("release_date", sortType)) {
                order = new Order(Direction.DESC, "releaseDate");
            } else if (StringUtils.equalsIgnoreCase("popularity", sortType)) {
                order = new Order(Direction.DESC, "popularity");
            } else if (StringUtils.equalsIgnoreCase("vote_average", sortType)) {
                order = new Order(Direction.DESC, "voteAverage");
            } else {
                order = new Order(Direction.DESC, "createTime");
            }
        }
        Sort sort = new Sort(order);
        Pageable pageable = new PageRequest(pageNumber, pageSize, sort);

        Specifications<Subject> spec = null;
        Specification<Subject> specGenre = null;
        Specification<Subject> specCountry = null;
        Specification<Subject> specKeyword = null;
        Specification<Subject> specType = null;
        Specification<Subject> specReleaseDate = null;
        Specification<Subject> specVoteAverage = null;
        if (genreId != null && !genreId.equals(0)) {
            specGenre = (Specification<Subject>) Specifications.where(new Specification<Subject>() {

                @Override
                public Predicate toPredicate(Root<Subject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.equal(root.join(root.getModel().getSet("genres", Genre.class), JoinType.INNER)
                            .get("id").as(Integer.class), genreId);
                }
            });
        }
        if (countryId != null && !countryId.equals(0)) {
            specCountry = (Specification<Subject>) Specifications.where(new Specification<Subject>() {

                @Override
                public Predicate toPredicate(Root<Subject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.equal(root.join(root.getModel().getSet("productionCountries", ProductionCountry.class), JoinType.INNER)
                                    .get("id").as(Integer.class), countryId);
                }
            });
        }
        if (!StringUtils.isEmpty(type)) {
            specType = Specifications.where(new Specification<Subject>() {

                @Override
                public Predicate toPredicate(Root<Subject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.equal(root.get("type").as(String.class), type);
                }
            });
        }
        if (!StringUtils.isEmpty(keyword)) {
            specKeyword = Specifications.where(new Specification<Subject>() {

                @Override
                public Predicate toPredicate(Root<Subject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.like(root.get("title").as(String.class), "%" + keyword + "%");
                }
            });
        }
        if (!StringUtils.isEmpty(startReleaseDate) && !StringUtils.isEmpty(endReleaseDate)) {
            specReleaseDate = Specifications.where(new Specification<Subject>() {

                @Override
                public Predicate toPredicate(Root<Subject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.between(root.get("releaseDate").as(LocalDate.class),
                            LocalDate.of(Integer.parseInt(startReleaseDate), 1, 2),
                            LocalDate.of(Integer.parseInt(endReleaseDate), 1, 1));
                }
            });
        }
        if (startVoteAverage != null && endVoteAverage != null) {
            specVoteAverage = new Specification<Subject>() {

                @Override
                public Predicate toPredicate(Root<Subject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.between(root.get("voteAverage").as(Float.class), startVoteAverage, endVoteAverage);
                }
            };
        }
        if (specGenre != null) {
            spec = Specifications.where(specGenre);
        }
        if (specCountry != null) {
            if (spec == null) {
                spec = Specifications.where(specCountry);
            } else {
                spec = spec.and(specCountry);
            }
        }
        if (specType != null) {
            if (spec == null) {
                spec = Specifications.where(specType);
            } else {
                spec = spec.and(specType);
            }
        }
        if (specKeyword != null) {
            if (spec == null) {
                spec = Specifications.where(specKeyword);
            } else {
                spec = spec.and(specKeyword);
            }
        }
        if (specReleaseDate != null) {
            if (spec == null) {
                spec = Specifications.where(specReleaseDate);
            } else {
                spec = spec.and(specReleaseDate);
            }
        }
        if (specVoteAverage != null) {
            if (spec == null) {
                spec = Specifications.where(specVoteAverage);
            } else {
                spec = spec.and(specVoteAverage);
            }
        }
        Page<Subject> result;
        if (spec == null) {
            result = subjectRepository.findAll(pageable);
        } else {
            result = subjectRepository.findAll(spec, pageable);
        }
        if (StringUtils.equals("video", type)) {
            pageable = new PageRequest(new Random().nextInt(result.getTotalPages()), pageSize, sort);
            if (spec == null) {
                result = subjectRepository.findAll(pageable);
            } else {
                result = subjectRepository.findAll(spec, pageable);
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Subject get(Integer subjectId) {
        return subjectRepository.getOne(subjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getSubjects(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return subjectRepository.findAll(
                Arrays.asList(ids.split(","))
                .stream().mapToInt(s -> Integer.parseInt(s))
                .boxed()
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Genre> getGenres(String type) {
        return genreRepository.findAllByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionCountry> getProductionCountries(String type) {
        return productionCountryRepository.findAllByType(type);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subject> getUncompletedSubjects() {
        return subjectRepository.findAll(new Specification<Subject>() {

            @Override
            public Predicate toPredicate(Root<Subject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Join<Subject, Property> join = root.join(root.getModel().getSet("properties", Property.class), JoinType.INNER);
                Predicate p1 = cb.equal(join.get("id").get("key").as(String.class), "completed");
                Predicate p2 = cb.equal(join.get("value").as(String.class), "false");
                return cb.and(p1, p2);
            }
        });
    }
}
