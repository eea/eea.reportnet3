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
   */
  void testLogging() throws EEAException, RepositoryException;

  /**
   * Upload a document.
   *
   * @param fileName the file name
   * @throws Exception
   */
  void uploadDocument() throws EEAException;

  void getDocument() throws EEAException;
}
