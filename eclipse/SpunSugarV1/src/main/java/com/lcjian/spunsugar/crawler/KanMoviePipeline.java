package com.lcjian.spunsugar.crawler;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.SearchResult;
import com.lcjian.spunsugar.dao.MovieDAOImpl;
import com.lcjian.spunsugar.dao.MovieVideoDAOImpl;
import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieVideo;
import com.lcjian.spunsugar.util.HibernateUtil;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class KanMoviePipeline implements Pipeline {

    private MovieDAOImpl movieDAO;

    private MovieVideoDAOImpl movieVideoDAO;

    private SessionFactory mSessionFactory;

    public KanMoviePipeline() {
        mSessionFactory = HibernateUtil.getSessionFactory();
        movieDAO = new MovieDAOImpl();
        movieDAO.setSessionFactory(mSessionFactory);
        movieVideoDAO = new MovieVideoDAOImpl();
        movieVideoDAO.setSessionFactory(mSessionFactory);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        Movie movie = resultItems.get("movie");
        Session session = null;
        Transaction transaction = null;
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();

            Search search = new Search();
            search.addFilterEqual("doubanId", movie.getDoubanId());
            SearchResult<Movie> searchResult = movieDAO
                    .searchAndCount(search);
            List<Movie> result = searchResult.getResult();
            if (result == null || result.isEmpty()) {
                movieDAO.save(movie);
                
                Set<MovieVideo> videos = movie.getMovieVideos();
                for (MovieVideo video : videos) {
                    video.setMovie(movie);
                    movieVideoDAO.save(video);
                }
            } else {
                Movie newMovie = result.get(0);
                Set<MovieVideo> videos = movie.getMovieVideos();
                for (MovieVideo video : videos) {
                    Search searchVideo = new Search();
                    searchVideo.addFilterEqual("url", video.getUrl());
                    SearchResult<MovieVideo> searchResultVideo = movieVideoDAO.searchAndCount(searchVideo);
                    List<MovieVideo> resultVideo = searchResultVideo.getResult();
                    if (resultVideo == null || resultVideo.isEmpty()) {
                        video.setMovie(newMovie);
                        movieVideoDAO.save(video);
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
}
