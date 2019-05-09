package org.eea.document.service;

import java.io.File;

/**
 * The interface Dataset service.
 */
public interface DocumentService {

  /**
   * testLogging.
   * @throws Exception 
   *
   */
  void testLogging() throws Exception;

  /**
   * Upload a document.
   *
   * @param fileName the file name
 * @throws Exception 
   */
  void uploadDocument() throws Exception;

void getDocument() throws Exception;
}
