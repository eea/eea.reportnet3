package org.eea.document.service;

import javax.jcr.RepositoryException;
import org.eea.exception.EEAException;
/**
 * The interface Dataset service.
 */
public interface DocumentService {

  /**
   * testLogging.
   * 
   * @throws RepositoryException
   * 
   * @throws Exception
   *
   * @throws Exception the exception
   */
  void testLogging() throws EEAException, RepositoryException;

  /**
   * Upload a document.
   *
   * @throws Exception the exception
   */
  void uploadDocument() throws EEAException;

  /**
   * Gets the document.
   *
   * @return the document
   * @throws Exception the exception
   */
  void getDocument() throws Exception;
}
