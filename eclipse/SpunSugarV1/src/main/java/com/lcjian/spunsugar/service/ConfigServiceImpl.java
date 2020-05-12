package com.lcjian.spunsugar.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl;
import com.lcjian.spunsugar.entity.Config;
import com.lcjian.spunsugar.util.HibernateUtil;

public class ConfigServiceImpl implements ConfigService {

    private GeneralDAOImpl dao;

    private SessionFactory mSessionFactory;

    public ConfigServiceImpl() {
        mSessionFactory = HibernateUtil.getSessionFactory();
        dao = new GeneralDAOImpl();
        dao.setSessionFactory(mSessionFactory);
    }

    @Override
    public List<Config> findAll() {
        Session session = null;
        Transaction transaction = null;
        List<Config> result = null;
        try {
            session = mSessionFactory.getCurrentSession();
            transaction = session.beginTransaction();
            result = dao.findAll(Config.class);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return result;
    }
}
