package org.eea.document.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.eea.document.type.FileResponse;
import org.eea.exception.EEAException;

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
   * @param dataFlowId the data flow id
   * @param language the language
   * @param description the description
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void uploadDocument(final InputStream inputStream, final String contentType,
      final String filename, final Long dataFlowId, final String language, final String description)
      throws EEAException, IOException;

  /**
   * Gets the document.
   *
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @param language the language
   * @return the document
   * @throws EEAException the EEA exception
   */
  FileResponse getDocument(final String documentName, final Long dataFlowId, final String language)
      throws EEAException;


  /**
   * Delete document.
   *
   * @param documentId the document id
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @param language the language
   * @throws EEAException the EEA exception
   */
  void deleteDocument(final Long documentId, final String documentName, final Long dataFlowId,
      final String language) throws EEAException;

  InputStream readFromFile(final String fileName) throws FileNotFoundException;

  void writeToFile(final String fileName, final OutputStream content) throws IOException;

  void uploadSchemaSnapshot(final InputStream inputStream, final String contentType,
      final String filename, final Long designDataset) throws EEAException, IOException;

  FileResponse getSnapshotDocument(final String documentName, final Long idDesignDataset)
      throws EEAException;

  void deleteSnapshotDocument(final String documentName, final Long idDesignDataset)
      throws EEAException;
}
