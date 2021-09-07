package org.eea.dataflow.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.document.DocumentVO;

/**
 * The Interface DataflowService.
 */
public interface DataflowDocumentService {

  /**
   * Insert document.
   *
   * @param documentVO the document VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long insertDocument(DocumentVO documentVO) throws EEAException;


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

  /**
   * Update document.
   *
   * @param documentVO the document VO
   * @throws EEAException the EEA exception
   */
  void updateDocument(DocumentVO documentVO) throws EEAException;


  /**
   * Gets the all documents by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the all documents by dataflow id
   * @throws EEAException the EEA exception
   */
  List<DocumentVO> getAllDocumentsByDataflowId(Long dataflowId) throws EEAException;
}
