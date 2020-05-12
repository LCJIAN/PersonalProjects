package com.lcjian.spunsugar.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.googlecode.genericdao.search.ISearch;
import com.googlecode.genericdao.search.SearchResult;
import com.lcjian.spunsugar.dao.HentaiAnimeEpisodeDAOImpl;
import com.lcjian.spunsugar.entity.HentaiAnimeEpisode;
import com.lcjian.spunsugar.util.HibernateUtil;

public class HentaiServiceImpl implements HentaiService {

    private HentaiAnimeEpisodeDAOImpl episodeDao;

    private SessionFactory mSessionFactory;

    public HentaiServiceImpl() {
        mSessionFactory = HibernateUtil.getSessionFactory();
        episodeDao = new HentaiAnimeEpisodeDAOImpl();
        episodeDao.setSessionFactory(mSessionFactory);
    }

    @Override
    public SearchResult<HentaiAnimeEpisode> searchAndCountEpisode(ISearch search) {
        Session session = null;
        Transaction transaction = null;
        SearchResult<HentaiAnimeEpisode> result = null;
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();
            result = episodeDao.searchAndCount(search);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return result;
    }
}
