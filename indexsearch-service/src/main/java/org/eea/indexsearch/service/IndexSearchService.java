package org.eea.indexsearch.service;

import java.util.List;
import org.eea.indexsearch.io.kafka.domain.ElasticSearchData;

/**
 * The Interface IndexSearchService.
 */
public interface IndexSearchService {

  // String createProfile(EventHandler eventHandler) throws Exception;

  // Employee findById(String id) throws Exception;

  // String updateProfile(Employee employee) throws Exception;

  /**
   * Find all.
   *
   * @return the list
   * @throws Exception the exception
   */
  List<ElasticSearchData> findAll() throws Exception;

  // List<Employee> findProfileByName(String name) throws Exception;

  /**
   * Delete profile document.
   *
   * @param id the id
   * @return the string
   * @throws Exception the exception
   */
  String deleteProfileDocument(String id) throws Exception;

  // List<Employee> searchByTechnology(String technology) throws Exception;

  /**
   * Execute macros.
   */
  void executeMacros();

}
