package com.lcjian.spunsugar.service;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.googlecode.genericdao.search.ISearch;
import com.googlecode.genericdao.search.SearchResult;
import com.googlecode.genericdao.search.Sort;
import com.lcjian.spunsugar.dao.MovieDAOImpl;
import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieVideo;
import com.lcjian.spunsugar.util.HibernateUtil;

public class MovieServiceImpl implements MovieService {

    private MovieDAOImpl dao;

    private SessionFactory mSessionFactory;

    public MovieServiceImpl() {
        mSessionFactory = HibernateUtil.getSessionFactory();
        dao = new MovieDAOImpl();
        dao.setSessionFactory(mSessionFactory);
    }

    @Override
    public SearchResult<Movie> searchAndCount(ISearch search) {
        Session session = null;
        Transaction transaction = null;
        SearchResult<Movie> result = null;
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();
            result = dao.searchAndCount(search);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return result;
    }

    @Override
    public SearchResult<Movie> search(ISearch search) {
        Session session = null;
        Transaction transaction = null;
        SearchResult<Movie> result = null;
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();
            {
                Sort sort = search.getSorts().get(0);
                Query query = session.createQuery("select distinct m from Movie as m, MovieVideo as mv"
                        + " where mv.movie = m and (mv.type = 'bilibili' or mv.type = 'letv' or mv.type = 'qianmo') order by m."
                        + sort.getProperty() + (sort.isDesc() ? " desc" : ""));
                query.setFirstResult(search.getFirstResult());
                query.setMaxResults(search.getMaxResults());
                List<Movie> cats = query.list();
                result = new SearchResult<Movie>();
                result.setResult(cats);
            }
            if (result != null) {
                Query query = session.createQuery("select count(distinct m) from Movie as m, MovieVideo as mv"
                        + " where mv.movie = m and (mv.type = 'bilibili' or mv.type = 'letv' or mv.type = 'qianmo') ");
                result.setTotalCount(Integer.valueOf(query.uniqueResult().toString()));
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return result;
    }

    @Override
    public List<MovieVideo> getMovieVideos(Integer id) {
        Session session = null;
        Transaction transaction = null;
        List<MovieVideo> result = new ArrayList<MovieVideo>();
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();
            Movie movie = dao.find(id);
            result.addAll(movie.getMovieVideos());
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return result;
    }
}
