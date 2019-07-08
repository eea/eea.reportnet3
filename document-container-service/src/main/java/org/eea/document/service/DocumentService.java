package org.eea.document.service;

import org.eea.document.type.FileResponse;
import org.eea.exception.EEAException;
import org.springframework.web.multipart.MultipartFile;

/**
 * The interface Dataset service.
 */
public interface DocumentService {

  /**
   * Upload a document.
   *
   * @param file the file
   * @param dataFlowId the data flow id
   * @throws EEAException the EEA exception
   */
  void uploadDocument(final MultipartFile file, final Long dataFlowId) throws EEAException;

  /**
   * Gets the document.
   *
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @return the document
   * @throws EEAException the EEA exception
   */
  FileResponse getDocument(final String documentName, final Long dataFlowId) throws EEAException;
}
