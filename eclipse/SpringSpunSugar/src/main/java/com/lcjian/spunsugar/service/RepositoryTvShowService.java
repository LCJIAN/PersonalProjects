package com.lcjian.spunsugar.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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

import com.lcjian.spunsugar.entity.TvShow;
import com.lcjian.spunsugar.entity.TvShowGenre;
import com.lcjian.spunsugar.entity.TvShowProductionCountry;
import com.lcjian.spunsugar.entity.TvShowVideo;
import com.lcjian.spunsugar.repository.TvShowGenreRepository;
import com.lcjian.spunsugar.repository.TvShowProductionCountryRepository;
import com.lcjian.spunsugar.repository.TvShowRepository;
import com.lcjian.spunsugar.repository.TvShowVideoRepository;

@Service
public class RepositoryTvShowService implements TvShowService {

    @Autowired
    private TvShowRepository tvShowRepository;

    @Autowired
    private TvShowVideoRepository tvShowVideoRepository;

    @Autowired
    private TvShowGenreRepository tvShowGenreRepository;

    @Autowired
    private TvShowProductionCountryRepository tvShowProductionCountryRepository;

    @Transactional
    @Override
    public TvShow create(TvShow outterTvShow) {
        Set<TvShowVideo> outterMvs = outterTvShow.getTvShowVideos();
        TvShow innerTvShow = tvShowRepository.findOneByCrawlerId(outterTvShow.getCrawlerId());
        if (innerTvShow == null) {
            if (outterTvShow.getTvShowGenres() != null) {
                HashSet<TvShowGenre> tempTvShowGenres = new HashSet<>();
                for (TvShowGenre tvShowGenre : outterTvShow.getTvShowGenres()) {
                    TvShowGenre mg = tvShowGenreRepository.findOneByName(tvShowGenre.getName());
                    if (mg == null) {
                        mg = tvShowGenreRepository.save(tvShowGenre);
                    }
                    tempTvShowGenres.add(mg);
                }
                outterTvShow.setTvShowGenres(tempTvShowGenres);
            }
            if (outterTvShow.getTvShowProductionCountries() != null) {
                HashSet<TvShowProductionCountry> tempTvShowProductionCountries = new HashSet<>();
                for (TvShowProductionCountry tvShowProductionCountry : outterTvShow.getTvShowProductionCountries()) {
                    TvShowProductionCountry mpc = tvShowProductionCountryRepository
                            .findOneByName(tvShowProductionCountry.getName());
                    if (mpc == null) {
                        mpc = tvShowProductionCountryRepository.save(tvShowProductionCountry);
                    }
                    tempTvShowProductionCountries.add(mpc);
                }
                outterTvShow.setTvShowProductionCountries(tempTvShowProductionCountries);
            }
            outterTvShow.setTvShowVideos(null);
            innerTvShow = tvShowRepository.save(outterTvShow);
            
            if (outterMvs != null) {
                for (TvShowVideo tvShowVideo : outterMvs) {
                    tvShowVideo.setTvShow(innerTvShow);
                    tvShowVideoRepository.save(tvShowVideo);
                }
            }
        } else {
            if (!StringUtils.equals(innerTvShow.getDoubanId(), outterTvShow.getDoubanId())) {
                innerTvShow.setDoubanId(outterTvShow.getDoubanId());
                innerTvShow.setTmdbId(outterTvShow.getTmdbId());
                innerTvShow.setImdbId(outterTvShow.getImdbId());
                innerTvShow.setPopularity(outterTvShow.getPopularity());
                innerTvShow.setVoteAverage(outterTvShow.getVoteAverage());
                innerTvShow.setCreateTime(outterTvShow.getCreateTime());
                innerTvShow = tvShowRepository.save(innerTvShow);
            }
            if (outterMvs != null) {
                final TvShow finalInnerTvShow = innerTvShow;
                Set<TvShowVideo> innerMvs = finalInnerTvShow.getTvShowVideos();
                
                Set<TvShowVideo> deleteMvs = innerMvs.stream()
                        .filter(v -> outterMvs.stream()
                                .allMatch(t -> !StringUtils.equals(t.getUrl(), v.getUrl())))
                        .collect(Collectors.toSet());
                
                Set<TvShowVideo> updateMvs = innerMvs.stream()
                        .filter(v -> outterMvs.stream()
                                .anyMatch(t -> {
                                    boolean update = StringUtils.equals(t.getUrl(), v.getUrl())
                                            && !StringUtils.equals(v.getName(), t.getName());
                                    if (update) {
                                        v.setName(t.getName());
                                    }
                                    return update;
                                }))
                        .collect(Collectors.toSet());
                
                Set<TvShowVideo> insertMvs = outterMvs.stream()
                        .filter(v -> innerMvs.stream()
                                .allMatch(t -> !StringUtils.equals(t.getUrl(), v.getUrl())))
                        .map(v -> {v.setTvShow(finalInnerTvShow);return v;})
                        .collect(Collectors.toSet());
                
                if ((deleteMvs != null && deleteMvs.isEmpty())
                        || (updateMvs != null && updateMvs.isEmpty())
                        || (insertMvs != null && insertMvs.isEmpty())) {
                    innerTvShow.setCreateTime(outterTvShow.getCreateTime());
                    innerTvShow = tvShowRepository.save(innerTvShow);
                }
                tvShowVideoRepository.delete(deleteMvs);
                tvShowVideoRepository.save(updateMvs);
                tvShowVideoRepository.save(insertMvs);
            }
        }
        return innerTvShow;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TvShowVideo> getTvShowVideos(Integer tvShowId) {
        return tvShowVideoRepository.findAllByTvShow(tvShowRepository.findOne(tvShowId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TvShow> getTvShows(String keyword, Integer genreId, Integer countryId, String startReleaseDate,
            String endReleaseDate, Float startVoteAverage, Float endVoteAverage, String sortType, String sortDirection,
            Integer pageNumber, Integer pageSize) {
        Order order;
        if (StringUtils.equals("asc", sortDirection)) {
            if (StringUtils.equals("release_date", sortType)) {
                order = new Order(Direction.ASC, "releaseDate");
            } else if (StringUtils.equals("popularity", sortType)) {
                order = new Order(Direction.ASC, "popularity");
            } else if (StringUtils.equals("vote_average", sortType)) {
                order = new Order(Direction.ASC, "voteAverage");
            } else {
                order = new Order(Direction.ASC, "createTime");
            }
        } else {
            if (StringUtils.equals("release_date", sortType)) {
                order = new Order(Direction.DESC, "releaseDate");
            } else if (StringUtils.equals("popularity", sortType)) {
                order = new Order(Direction.DESC, "popularity");
            } else if (StringUtils.equals("vote_average", sortType)) {
                order = new Order(Direction.DESC, "voteAverage");
            } else {
                order = new Order(Direction.DESC, "createTime");
            }
        }
        Sort sort = new Sort(order);
        Pageable pageable = new PageRequest(pageNumber, pageSize, sort);

        Specifications<TvShow> spec = null;
        Specification<TvShow> specGenre = null;
        Specification<TvShow> specCountry = null;
        Specification<TvShow> specKeyword = null;
        Specification<TvShow> specReleaseDate = null;
        Specification<TvShow> specVoteAverage = null;
        if (genreId != null) {
            specGenre = (Specification<TvShow>) Specifications.where(new Specification<TvShow>() {

                @Override
                public Predicate toPredicate(Root<TvShow> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.equal(root.join(root.getModel().getSet("tvShowGenres", TvShowGenre.class), JoinType.INNER)
                            .get("id").as(Integer.class), genreId);
                }
            });
        }
        if (countryId != null) {
            specCountry = (Specification<TvShow>) Specifications.where(new Specification<TvShow>() {

                @Override
                public Predicate toPredicate(Root<TvShow> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.equal(root.join(root.getModel().getSet("tvShowProductionCountries", TvShowProductionCountry.class), JoinType.INNER)
                                    .get("id").as(Integer.class), countryId);
                }
            });
        }
        if (!StringUtils.isEmpty(keyword)) {
            specKeyword = Specifications.where(new Specification<TvShow>() {

                @Override
                public Predicate toPredicate(Root<TvShow> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.like(root.get("title").as(String.class), "%" + keyword + "%");
                }
            });
        }
        if (!StringUtils.isEmpty(startReleaseDate) && !StringUtils.isEmpty(endReleaseDate)) {
            specReleaseDate = Specifications.where(new Specification<TvShow>() {

                @Override
                public Predicate toPredicate(Root<TvShow> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.between(root.get("releaseDate").as(LocalDate.class),
                            LocalDate.of(Integer.parseInt(startReleaseDate), 1, 2),
                            LocalDate.of(Integer.parseInt(endReleaseDate), 1, 1));
                }
            });
        }
        if (startVoteAverage != null && endVoteAverage != null) {
            specVoteAverage = new Specification<TvShow>() {

                @Override
                public Predicate toPredicate(Root<TvShow> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
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
        if (spec == null) {
            return tvShowRepository.findAll(pageable);
        } else {
            return tvShowRepository.findAll(spec, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TvShow get(Integer tvShowId) {
        return tvShowRepository.getOne(tvShowId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TvShow> getTvShows(String ids) {
        return tvShowRepository.findAll(
                Arrays.asList(ids.split(","))
                .stream().mapToInt(s -> Integer.parseInt(s))
                .boxed()
                .collect(Collectors.toList()));
    }
}
