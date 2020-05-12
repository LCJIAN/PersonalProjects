package com.lcjian.spunsugar.crawler;

import java.util.HashSet;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.SearchResult;
import com.lcjian.spunsugar.dao.MovieDAOImpl;
import com.lcjian.spunsugar.dao.MovieGenreDAOImpl;
import com.lcjian.spunsugar.dao.MovieProductionCountryDAOImpl;
import com.lcjian.spunsugar.entity.MovieGenre;
import com.lcjian.spunsugar.entity.MovieProductionCountry;
import com.lcjian.spunsugar.util.HibernateUtil;
import com.lcjian.spunsugar.util.StringUtils;
import com.uwetrottmann.tmdb.Tmdb;

public class TmdbInfoSetter {

    private Tmdb mTmdb;

    private MovieDAOImpl movieDAO;
    
    private MovieGenreDAOImpl movieGenreDAO;
    
    private MovieProductionCountryDAOImpl mMovieProductionCountryDAO;

    private SessionFactory mSessionFactory;

    public TmdbInfoSetter() {
        mSessionFactory = HibernateUtil.getSessionFactory();
        movieDAO = new MovieDAOImpl();
        movieDAO.setSessionFactory(mSessionFactory);
        movieGenreDAO = new MovieGenreDAOImpl();
        movieGenreDAO.setSessionFactory(mSessionFactory);
        mMovieProductionCountryDAO = new MovieProductionCountryDAOImpl();
        mMovieProductionCountryDAO.setSessionFactory(mSessionFactory);
        mTmdb = new Tmdb();
        mTmdb.setApiKey("f4fe5dbf051114ba829c94e8b0c47b6a");
    }

    public String getTmdbId() {
        Session session = null;
        Transaction transaction = null;
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();

            List<com.lcjian.spunsugar.entity.Movie> movies = movieDAO.findAll();
            if (movies != null && !movies.isEmpty()) {
                for (com.lcjian.spunsugar.entity.Movie movie : movies) {
                    if (!StringUtils.isEmpty(movie.getTmdbId()) && movie.getPopularity() == null) {
                        com.uwetrottmann.tmdb.entities.Movie tmdbMovie = mTmdb.moviesService()
                                .summary(Integer.parseInt(movie.getTmdbId()), null, null);
                        if (movie.getMovieGenres() == null) {
                            movie.setMovieGenres(new HashSet<>());
                        }
                        for (com.uwetrottmann.tmdb.entities.Genre genre :tmdbMovie.genres) {
                            Search searchMovieGenre = new Search();
                            searchMovieGenre.addFilterEqual("name", genre.name);
                            SearchResult<MovieGenre> searchResultMovieGenre = movieGenreDAO.searchAndCount(searchMovieGenre);
                            List<MovieGenre> resultMovieGenre = searchResultMovieGenre.getResult();
                            if (resultMovieGenre == null || resultMovieGenre.isEmpty()) {
                                MovieGenre movieGenre = new MovieGenre(genre.name);
                                movieGenreDAO.save(movieGenre);
                                movie.getMovieGenres().add(movieGenre);
                            } else {
                                movie.getMovieGenres().add(resultMovieGenre.get(0));
                            }
                        }
                        movie.setReleaseDate(tmdbMovie.release_date);
                        movie.setVoteAverage(tmdbMovie.vote_average.floatValue());
                        movie.setPopularity(tmdbMovie.popularity.floatValue());
                        
                        if (movie.getMovieProductionCountries() == null) {
                            movie.setMovieProductionCountries(new HashSet<>());
                        }
                        for (com.uwetrottmann.tmdb.entities.ProductionCountry productionCountry :tmdbMovie.production_countries) {
                            Search searchMovieProductionCountry = new Search();
                            searchMovieProductionCountry.addFilterEqual("iso_3166_1", productionCountry.iso_3166_1);
                            SearchResult<MovieProductionCountry> searchResultMovieProductionCountry
                            = mMovieProductionCountryDAO.searchAndCount(searchMovieProductionCountry);
                            List<MovieProductionCountry> resultMovieProductionCountry = searchResultMovieProductionCountry.getResult();
                            if (resultMovieProductionCountry == null || resultMovieProductionCountry.isEmpty()) {
                                MovieProductionCountry movieProductionCountry = new MovieProductionCountry();
                                movieProductionCountry.setIso_3166_1(productionCountry.iso_3166_1);
                                movieProductionCountry.setName(productionCountry.name);
                                mMovieProductionCountryDAO.save(movieProductionCountry);
                                movie.getMovieProductionCountries().add(movieProductionCountry);
                            } else {
                                movie.getMovieProductionCountries().add(resultMovieProductionCountry.get(0));
                            }
                        }
                        System.out.println(movie.getTitle());
                        movieDAO.save(movie);
                    }
                }
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return null;
    }
    
    public static void main(String[] args) {
        new TmdbInfoSetter().getTmdbId();
    }

}
