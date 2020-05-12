package com.lcjian.spunsugar.crawler;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.SearchResult;
import com.lcjian.spunsugar.dao.MovieDAOImpl;
import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.util.DateUtils;
import com.lcjian.spunsugar.util.HibernateUtil;
import com.lcjian.spunsugar.util.StringUtils;

public class MdbIdSetter {

    private MovieDAOImpl movieDAO;

    private SessionFactory mSessionFactory;

    public MdbIdSetter() {
        mSessionFactory = HibernateUtil.getSessionFactory();
        movieDAO = new MovieDAOImpl();
        movieDAO.setSessionFactory(mSessionFactory);
    }

    public void setDoubanId() {
        Session session = null;
        Transaction transaction = null;
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();

            Search search = new Search();
            search.addFilterEmpty("doubanId");
            SearchResult<Movie> searchResult = movieDAO.searchAndCount(search);
            List<Movie> movies = searchResult.getResult();
            if (movies != null && !movies.isEmpty()) {
                for (Movie movie : movies) {
                    if (movie.getReleaseDate() != null) {
                        String doubanId = MdbIdGetter.getDoubanId(
                                DateUtils.convertDateToStr(movie.getReleaseDate(), "yyyy"), movie.getTitle());
                        System.out.println(movie.getTitle() + " doubanId:" + doubanId);
                        if (!StringUtils.isEmpty(doubanId)) {
                            movie.setDoubanId(doubanId);
                            movieDAO.save(movie);
                        }
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
    }

    public void setImdbId() {
        Session session = null;
        Transaction transaction = null;
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();

            Search search = new Search();
            search.addFilterEmpty("imdbId");
            SearchResult<Movie> searchResult = movieDAO.searchAndCount(search);
            List<Movie> movies = searchResult.getResult();
            if (movies != null && !movies.isEmpty()) {
                for (Movie movie : movies) {
                    if (!StringUtils.isEmpty(movie.getDoubanId())) {
                        String imdbId = MdbIdGetter.getImdbId(movie.getDoubanId());
                        System.out.println(movie.getTitle() + " imdbId:" + imdbId);
                        if (!StringUtils.isEmpty(imdbId)) {
                            movie.setImdbId(imdbId);
                            movieDAO.save(movie);
                        }
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
    }

    public void setTmdbId() {
        Session session = null;
        Transaction transaction = null;
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();

            Search search = new Search();
            search.addFilterEmpty("tmdbId");
            SearchResult<Movie> searchResult = movieDAO.searchAndCount(search);
            List<Movie> movies = searchResult.getResult();
            if (movies != null && !movies.isEmpty()) {
                for (Movie movie : movies) {
                    if (!StringUtils.isEmpty(movie.getImdbId())) {
                        String tmdbId = MdbIdGetter.getTmdbId(movie.getImdbId());
                        System.out.println(movie.getTitle() + " tmdbId:" + tmdbId);
                        if (!StringUtils.isEmpty(tmdbId)) {
                            movie.setTmdbId(tmdbId);
                            movieDAO.save(movie);
                        }
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
    }
    
    public static void main(String[] args) {
        MdbIdSetter mdbIdSetter = new MdbIdSetter();
        mdbIdSetter.setDoubanId();
        mdbIdSetter.setImdbId();
        mdbIdSetter.setTmdbId();
    }
}
