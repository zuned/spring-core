package com.earldouglas.dbencryption;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class HibernateRepository implements Repository {

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Object retrieve(Class<?> entityClass, String identifier) {
		DetachedCriteria criteria = DetachedCriteria.forClass(entityClass).add(
				Restrictions.idEq(identifier));
		return criteria.getExecutableCriteria(
				sessionFactory.getCurrentSession()).uniqueResult();
	}

	@Override
	public void store(Object entity) {
		sessionFactory.getCurrentSession().save(entity);
	}
}
