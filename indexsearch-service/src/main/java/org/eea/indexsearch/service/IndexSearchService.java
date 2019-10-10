package org.eea.indexsearch.service;

import java.util.List;
import org.eea.indexsearch.io.kafka.domain.ElasticSearchData;

public interface IndexSearchService {

  // String createProfile(EventHandler eventHandler) throws Exception;

  // Employee findById(String id) throws Exception;

  // String updateProfile(Employee employee) throws Exception;

  List<ElasticSearchData> findAll() throws Exception;

  // List<Employee> findProfileByName(String name) throws Exception;

  String deleteProfileDocument(String id) throws Exception;

  // List<Employee> searchByTechnology(String technology) throws Exception;

  void executeMacros();

}
