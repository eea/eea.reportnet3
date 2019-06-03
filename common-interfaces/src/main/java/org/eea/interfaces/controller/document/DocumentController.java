package org.eea.interfaces.controller.document;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

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
   *
   * @throws Exception the exception
   */
  @GetMapping(value = "/testLog")
  void testLogging() throws Exception;

  /**
   * Upload document .
   *
   * @throws Exception the exception
   */
  @GetMapping(value = "/create")
  void uploadDocument() throws Exception;

  /**
   * Download document .
   *
   * @return the document
   * @throws Exception the exception
   */
  @GetMapping
  void getDocument() throws Exception;

}
