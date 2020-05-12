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

import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieGenre;
import com.lcjian.spunsugar.entity.MovieProductionCountry;
import com.lcjian.spunsugar.entity.MovieVideo;
import com.lcjian.spunsugar.repository.MovieGenreRepository;
import com.lcjian.spunsugar.repository.MovieProductionCountryRepository;
import com.lcjian.spunsugar.repository.MovieRepository;
import com.lcjian.spunsugar.repository.MovieVideoRepository;

@Service
public class RepositoryMovieService implements MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieVideoRepository movieVideoRepository;

    @Autowired
    private MovieGenreRepository movieGenreRepository;

    @Autowired
    private MovieProductionCountryRepository movieProductionCountryRepository;

    @Transactional
    @Override
    public Movie create(Movie outterMovie) {
        Set<MovieVideo> outterMvs = outterMovie.getMovieVideos();
        Movie innerMovie = movieRepository.findOneByCrawlerId(outterMovie.getCrawlerId());
        if (innerMovie == null) {
            if (outterMovie.getMovieGenres() != null) {
                HashSet<MovieGenre> tempMovieGenres = new HashSet<>();
                for (MovieGenre movieGenre : outterMovie.getMovieGenres()) {
                    MovieGenre mg = movieGenreRepository.findOneByName(movieGenre.getName());
                    if (mg == null) {
                        mg = movieGenreRepository.save(movieGenre);
                    }
                    tempMovieGenres.add(mg);
                }
                outterMovie.setMovieGenres(tempMovieGenres);
            }
            if (outterMovie.getMovieProductionCountries() != null) {
                HashSet<MovieProductionCountry> tempMovieProductionCountries = new HashSet<>();
                for (MovieProductionCountry movieProductionCountry : outterMovie.getMovieProductionCountries()) {
                    MovieProductionCountry mpc = movieProductionCountryRepository
                            .findOneByName(movieProductionCountry.getName());
                    if (mpc == null) {
                        mpc = movieProductionCountryRepository.save(movieProductionCountry);
                    }
                    tempMovieProductionCountries.add(mpc);
                }
                outterMovie.setMovieProductionCountries(tempMovieProductionCountries);
            }
            outterMovie.setMovieVideos(null);
            innerMovie = movieRepository.save(outterMovie);
            
            if (outterMvs != null) {
                for (MovieVideo movieVideo : outterMvs) {
                    movieVideo.setMovie(innerMovie);
                    movieVideoRepository.save(movieVideo);
                }
            }
        } else {
            if (!StringUtils.equals(innerMovie.getDoubanId(), outterMovie.getDoubanId())) {
                innerMovie.setDoubanId(outterMovie.getDoubanId());
                innerMovie.setTmdbId(outterMovie.getTmdbId());
                innerMovie.setImdbId(outterMovie.getImdbId());
                innerMovie.setPopularity(outterMovie.getPopularity());
                innerMovie.setVoteAverage(outterMovie.getVoteAverage());
                innerMovie.setCreateTime(outterMovie.getCreateTime());
                innerMovie = movieRepository.save(innerMovie);
            }
            if (outterMvs != null) {
                final Movie finalInnerMovie = innerMovie;
                Set<MovieVideo> innerMvs = finalInnerMovie.getMovieVideos();
                
                Set<MovieVideo> deleteMvs = innerMvs.stream()
                        .filter(v -> outterMvs.stream()
                                .allMatch(t -> !StringUtils.equals(t.getUrl(), v.getUrl())))
                        .collect(Collectors.toSet());
                
                Set<MovieVideo> updateMvs = innerMvs.stream()
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
                
                Set<MovieVideo> insertMvs = outterMvs.stream()
                        .filter(v -> innerMvs.stream()
                                .allMatch(t -> !StringUtils.equals(t.getUrl(), v.getUrl())))
                        .map(v -> {v.setMovie(finalInnerMovie);return v;})
                        .collect(Collectors.toSet());
                
                if ((deleteMvs != null && deleteMvs.isEmpty())
                        || (updateMvs != null && updateMvs.isEmpty())
                        || (insertMvs != null && insertMvs.isEmpty())) {
                    innerMovie.setCreateTime(outterMovie.getCreateTime());
                    innerMovie = movieRepository.save(innerMovie);
                }
                movieVideoRepository.delete(deleteMvs);
                movieVideoRepository.save(updateMvs);
                movieVideoRepository.save(insertMvs);
            }
        }
        return innerMovie;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieVideo> getMovieVideos(Integer movieId) {
        return movieVideoRepository.findAllByMovie(movieRepository.findOne(movieId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Movie> getMovies(String keyword, Integer genreId, Integer countryId, String startReleaseDate,
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

        Specifications<Movie> spec = null;
        Specification<Movie> specGenre = null;
        Specification<Movie> specCountry = null;
        Specification<Movie> specKeyword = null;
        Specification<Movie> specReleaseDate = null;
        Specification<Movie> specVoteAverage = null;
        if (genreId != null) {
            specGenre = (Specification<Movie>) Specifications.where(new Specification<Movie>() {

                @Override
                public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.equal(root.join(root.getModel().getSet("movieGenres", MovieGenre.class), JoinType.INNER)
                            .get("id").as(Integer.class), genreId);
                }
            });
        }
        if (countryId != null) {
            specCountry = (Specification<Movie>) Specifications.where(new Specification<Movie>() {

                @Override
                public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.equal(root.join(root.getModel().getSet("movieProductionCountries", MovieProductionCountry.class), JoinType.INNER)
                                    .get("id").as(Integer.class), countryId);
                }
            });
        }
        if (!StringUtils.isEmpty(keyword)) {
            specKeyword = Specifications.where(new Specification<Movie>() {

                @Override
                public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.like(root.get("title").as(String.class), "%" + keyword + "%");
                }
            });
        }
        if (!StringUtils.isEmpty(startReleaseDate) && !StringUtils.isEmpty(endReleaseDate)) {
            specReleaseDate = Specifications.where(new Specification<Movie>() {

                @Override
                public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    return cb.between(root.get("releaseDate").as(LocalDate.class),
                            LocalDate.of(Integer.parseInt(startReleaseDate), 1, 2),
                            LocalDate.of(Integer.parseInt(endReleaseDate), 1, 1));
                }
            });
        }
        if (startVoteAverage != null && endVoteAverage != null) {
            specVoteAverage = new Specification<Movie>() {

                @Override
                public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
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
            return movieRepository.findAll(pageable);
        } else {
            return movieRepository.findAll(spec, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Movie get(Integer movieId) {
        return movieRepository.getOne(movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movie> getMovies(String ids) {
        return movieRepository.findAll(
                Arrays.asList(ids.split(","))
                .stream().mapToInt(s -> Integer.parseInt(s))
                .boxed()
                .collect(Collectors.toList()));
    }
}
