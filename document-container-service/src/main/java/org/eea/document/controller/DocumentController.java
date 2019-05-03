package org.eea.document.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The interface Document controller.
 */
public interface DocumentController {

  /**
   * The interface Document controller zuul.
   */
  @FeignClient(value = "document", path = "/document")
  interface DocumentControllerZuul extends DocumentController {

  }

  /**
   * log into jackrabbit.
 * @throws Exception 
   *
   */
  @RequestMapping(value = "/testLog", method = RequestMethod.GET)
  void testLogging() throws Exception;
  
   /**
   * Upload document .
   *
   * @param dataset the dataset
   *
   * @return the data set vo
   * @throws Exception 
   */
  @RequestMapping(value = "/create", method = RequestMethod.GET)
  void uploadDocument() throws Exception;



}
