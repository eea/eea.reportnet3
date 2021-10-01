package org.eea.document.service;

import java.io.IOException;
import java.io.InputStream;
import org.eea.document.type.FileResponse;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.document.DocumentVO;

/**
 * The interface Dataset service.
 */
public interface DocumentService {

  /**
   * Upload a document.
   *
   * @param inputStream the file
   * @param contentType the content type
   * @param fileName the file name
   * @param documentVO the document VO
   * @param size the size
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void uploadDocument(InputStream inputStream, String contentType, String fileName,
      DocumentVO documentVO, Long size) throws EEAException, IOException;

  /**
   * Gets the document.
   *
   * @param documentId the document id
   * @param dataFlowId the data flow id
   * @return the document
   * @throws EEAException the EEA exception
   */
  FileResponse getDocument(Long documentId, Long dataFlowId) throws EEAException;


  /**
   * Delete document.
   *
   * @param documentId the document id
   * @param dataFlowId the data flow id
   * @param deleteMetabase the delete metabase
   * @throws EEAException the EEA exception
   */
  void deleteDocument(Long documentId, Long dataFlowId, Boolean deleteMetabase) throws EEAException;

  /**
   * Upload schema snapshot.
   *
   * @param inputStream the input stream
   * @param contentType the content type
   * @param filename the filename
   * @param designDataset the design dataset
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void uploadSchemaSnapshot(InputStream inputStream, String contentType, String filename,
      Long designDataset) throws EEAException, IOException;

  /**
   * Gets the snapshot document.
   *
   * @param documentName the document name
   * @param idDesignDataset the id design dataset
   * @return the snapshot document
   * @throws EEAException the EEA exception
   */
  FileResponse getSnapshotDocument(String documentName, Long idDesignDataset) throws EEAException;

  /**
   * Delete snapshot document.
   *
   * @param documentName the document name
   * @param idDesignDataset the id design dataset
   * @throws EEAException the EEA exception
   */
  void deleteSnapshotDocument(String documentName, Long idDesignDataset) throws EEAException;

  /**
   * Update document.
   *
   * @param documentVO the document VO
   * @throws EEAException the EEA exception
   */
  void updateDocument(DocumentVO documentVO) throws EEAException;

  /**
   * Upload collaboration document.
   *
   * @param inputStream the input stream
   * @param contentType the content type
   * @param filename the filename
   * @param dataflowId the dataflow id
   * @param messageId the message id
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void uploadCollaborationDocument(InputStream inputStream, String contentType, String filename,
      Long dataflowId, Long messageId) throws EEAException, IOException;


  /**
   * Delete collaboration document.
   *
   * @param documentName the document name
   * @param dataflowId the dataflow id
   * @param messageId the message id
   * @throws EEAException the EEA exception
   */
  void deleteCollaborationDocument(String documentName, Long dataflowId, Long messageId)
      throws EEAException;

  /**
   * Gets the collaboration document.
   *
   * @param documentName the document name
   * @param dataflowId the dataflow id
   * @param messageId the message id
   * @return the collaboration document
   * @throws EEAException the EEA exception
   */
  FileResponse getCollaborationDocument(final String documentName, final Long dataflowId,
      Long messageId) throws EEAException;

}
