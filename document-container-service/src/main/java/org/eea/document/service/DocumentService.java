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
   * @param filename the filename
   * @param documentVO the document VO
   * @param size the size
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void uploadDocument(final InputStream inputStream, final String contentType,
      final String filename, DocumentVO documentVO, final Long size)
      throws EEAException, IOException;

  /**
   * Gets the document.
   *
   * @param documentId the document id
   * @param dataFlowId the data flow id
   * @return the document
   * @throws EEAException the EEA exception
   */
  FileResponse getDocument(final Long documentId, final Long dataFlowId) throws EEAException;


  /**
   * Delete document.
   *
   * @param documentId the document id
   * @param dataFlowId the data flow id
   * @throws EEAException the EEA exception
   */
  void deleteDocument(final Long documentId, final Long dataFlowId) throws EEAException;

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
  void uploadSchemaSnapshot(final InputStream inputStream, final String contentType,
      final String filename, final Long designDataset) throws EEAException, IOException;

  /**
   * Gets the snapshot document.
   *
   * @param documentName the document name
   * @param idDesignDataset the id design dataset
   * @return the snapshot document
   * @throws EEAException the EEA exception
   */
  FileResponse getSnapshotDocument(final String documentName, final Long idDesignDataset)
      throws EEAException;

  /**
   * Delete snapshot document.
   *
   * @param documentName the document name
   * @param idDesignDataset the id design dataset
   * @throws EEAException the EEA exception
   */
  void deleteSnapshotDocument(final String documentName, final Long idDesignDataset)
      throws EEAException;

}
