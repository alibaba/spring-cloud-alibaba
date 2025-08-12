package com.alibaba.demo.nacosdruidexample.dao;

import java.util.List;

public interface JdbcEntityRepository {

    String findNameById(Long id);

    void insertNameById(String name,String content);

    List<String>  getByContent(String content);
}
