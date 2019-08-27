package org.eea.dataflow.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.document.DocumentVO;

/**
 * The Interface DataflowService.
 */
public interface DataflowDocumentService {

  /**
   * Insert document.
   *
   * @param dataflowId the dataflow id
   * @param filename the filename
   * @param language the language
   * @param description the description
   * @throws EEAException the EEA exception
   */
  void insertDocument(Long dataflowId, String filename, String language, String description)
      throws EEAException;


  /**
   * Delete document.
   *
   * @param documentId the document id
   * @throws EEAException the EEA exception
   */
  void deleteDocument(Long documentId) throws EEAException;


  /**
   * Gets the document by id.
   *
   * @param documentId the document id
   * @return the document by id
   * @throws EEAException the EEA exception
   */
  DocumentVO getDocumentInfoById(Long documentId) throws EEAException;

}
