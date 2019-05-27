package org.eea.document.service;

/**
 * The interface Dataset service.
 */
public interface DocumentService {

  /**
   * testLogging.
   *
   * @throws Exception the exception
   */
  void testLogging() throws Exception;

  /**
   * Upload a document.
   *
   * @throws Exception the exception
   */
  void uploadDocument() throws Exception;

  /**
   * Gets the document.
   *
   * @return the document
   * @throws Exception the exception
   */
  void getDocument() throws Exception;
}
