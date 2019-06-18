package org.eea.document.service;

import javax.jcr.RepositoryException;
import org.eea.exception.EEAException;

/**
 * The interface Dataset service.
 */
public interface DocumentService {


  /**
   * Test logging.
   *
   * @throws EEAException the EEA exception
   * @throws RepositoryException the repository exception
   */
  void testLogging() throws EEAException, RepositoryException;

  /**
   * Upload a document.
   *
   * @throws EEAException the EEA exception
   */
  void uploadDocument() throws EEAException;

  /**
   * Gets the document.
   *
   * @return the document
   * @throws EEAException the EEA exception
   */
  void getDocument() throws EEAException;
}
