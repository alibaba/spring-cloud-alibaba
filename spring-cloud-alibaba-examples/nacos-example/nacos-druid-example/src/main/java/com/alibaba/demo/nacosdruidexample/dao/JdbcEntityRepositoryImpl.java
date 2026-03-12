package com.alibaba.demo.nacosdruidexample.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcEntityRepositoryImpl implements JdbcEntityRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public String findNameById(Long id) {
		try {
			return jdbcTemplate.queryForObject("SELECT name FROM demo_entity WHERE id = ?", String.class, id);
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
		catch (Throwable e) {
			throw e;
		}
	}

	@Override
	public void insertNameById(String name, String content) {
		jdbcTemplate.update("insert into  demo_entity(name,content) values(?,?) ", name, content);
	}

	@Override
	public List<String> getByContent(String content) {
		return jdbcTemplate.queryForList("select content from demo_entity WHERE content like ?  limit 400", new Object[] {"%" + content + "%"}, String.class);
	}

}
