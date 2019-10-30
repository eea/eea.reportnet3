package org.eea.indexsearch.controller;

import java.util.List;
import org.eea.indexsearch.io.kafka.domain.ElasticSearchData;

/**
 * The Interface IndexSearchControler.
 */
public interface IndexSearchController {

  /**
   * Search by name.
   *
   * @return the list
   * @throws Exception the exception
   */
  // List<Employee> searchByName(String name) throws Exception;
  //
  // /**
  // * Search.
  // *
  // * @param technology the technology
  // * @return the list
  // * @throws Exception the exception
  // */
  // List<Employee> search(String technology) throws Exception;
  //
  // /**
  // * Find all.
  // *
  // * @return the list
  // * @throws Exception the exception
  // */
  List<ElasticSearchData> findAll() throws Exception;
  //
  // /**
  // * Find by id.
  // *
  // * @param id the id
  // * @return the employee
  // * @throws Exception the exception
  // */
  // Employee findById(String id) throws Exception;
  //
  // /**
  // * Update profile.
  // *
  // * @param employee the employee
  // * @return the response entity
  // * @throws Exception the exception
  // */
  // ResponseEntity<String> updateProfile(Employee employee) throws Exception;
  //
  // /**
  // * Creates the profile.
  // *
  // * @param employee the employee
  // * @return the response entity
  // * @throws Exception the exception
  // */
  // ResponseEntity<String> createProfile(Employee employee) throws Exception;

  /**
   * Execute macros.
   *
   * @throws Exception the exception
   */
  void executeMacros() throws Exception;

}
