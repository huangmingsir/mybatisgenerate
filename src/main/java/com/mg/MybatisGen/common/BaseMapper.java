package com.mg.MybatisGen.common;

import java.util.List;

public interface BaseMapper<T> {
	
	public T selectByPrimaryKey(Integer id);

	public int deleteByPrimaryKey(Integer id);
	
	public int insert(T t);

	public int insertSelective(T t);
	
	public int updateByPrimaryKey(T t);

	public int updateByPrimaryKeySelective(T t);
	
	public List<T> getList(T t);

	public List<T> getListByPage(T t);

	public Long getCount(T t);

}