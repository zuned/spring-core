package com.earldouglas.dbencryption;

public interface Repository {

	public Object retrieve(Class<?> entityClass, String identifier);

	public void store(Object entity);
}
