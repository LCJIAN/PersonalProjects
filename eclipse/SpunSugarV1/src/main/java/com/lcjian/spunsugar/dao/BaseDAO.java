package com.lcjian.spunsugar.dao;

import java.io.Serializable;

import com.googlecode.genericdao.dao.hibernate.GenericDAOImpl;

public class BaseDAO<T, ID extends Serializable> extends GenericDAOImpl<T, ID> {

}
