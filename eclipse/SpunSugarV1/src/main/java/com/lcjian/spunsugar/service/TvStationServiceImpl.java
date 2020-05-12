package com.lcjian.spunsugar.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.googlecode.genericdao.search.ISearch;
import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.SearchResult;
import com.lcjian.spunsugar.dao.TvLiveSourceDAOImpl;
import com.lcjian.spunsugar.dao.TvStationDAOImpl;
import com.lcjian.spunsugar.entity.TvLiveSource;
import com.lcjian.spunsugar.entity.TvStation;
import com.lcjian.spunsugar.util.HibernateUtil;

public class TvStationServiceImpl implements TvStationService {

    private TvStationDAOImpl dao;

    private TvLiveSourceDAOImpl tvLiveSourceDao;

    private SessionFactory mSessionFactory;

    public TvStationServiceImpl() {
        mSessionFactory = HibernateUtil.getSessionFactory();
        dao = new TvStationDAOImpl();
        dao.setSessionFactory(mSessionFactory);
        tvLiveSourceDao = new TvLiveSourceDAOImpl();
        tvLiveSourceDao.setSessionFactory(mSessionFactory);
    }

    @Override
    public SearchResult<TvStation> searchAndCount(ISearch search) {
        Session session = null;
        Transaction transaction = null;
        SearchResult<TvStation> result = null;
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
    public List<TvLiveSource> findTvStationSource(String channel) {
        Session session = null;
        Transaction transaction = null;
        List<TvLiveSource> result = null;
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();
            Search search = new Search();
            search.addFilterEqual("channel", channel);
            SearchResult<TvStation> searchResult = dao.searchAndCount(search);
            List<TvStation> tvStations = searchResult.getResult();
            result = new ArrayList<>();
            if (!tvStations.isEmpty()) {
                TvStation tvStation = tvStations.get(0);
                Set<TvLiveSource> tvLiveSources = tvStation.getTvLiveSources();
                if (!tvLiveSources.isEmpty()) {
                    result.addAll(tvLiveSources);
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return result;
    }
}
