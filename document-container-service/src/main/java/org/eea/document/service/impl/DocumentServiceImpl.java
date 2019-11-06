package org.eea.document.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.eea.document.service.DocumentService;
import org.eea.document.type.FileResponse;
import org.eea.document.utils.OakRepositoryUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * The type Document service.
 *
 * @author ruben.lozano
 *
 */
@Service("documentService")
public class DocumentServiceImpl implements DocumentService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant PATH_DELIMITER. */
  private static final String PATH_DELIMITER = "/";

  private static final String PATH_DELIMITER_SNAPSHOT = "/snapshotSchema/";

  /** The oak repository utils. */
  @Autowired
  private OakRepositoryUtils oakRepositoryUtils;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * upload a document to the jackrabbit content repository.
   *
   * @param inputStream the input stream
   * @param contentType the content type
   * @param filename the filename
   * @param dataFlowId the data flow id
   * @param language the language
   * @param description the description
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Async
  public void uploadDocument(final InputStream inputStream, final String contentType,
      final String filename, final Long dataFlowId, final String language, final String description)
      throws EEAException, IOException {
    Session session = null;
    DocumentNodeStore ns = null;
    try {
      if (filename == null || contentType == null
          || StringUtils.isBlank(FilenameUtils.getExtension(filename))) {
        throw new EEAException(EEAErrorMessage.FILE_FORMAT);
      }

      LOG.info("Adding the file...");
      // Initialize the session
      ns = oakRepositoryUtils.initializeNodeStore();
      Repository repository = oakRepositoryUtils.initializeRepository(ns);
      session = oakRepositoryUtils.initializeSession(repository);

      // Add a file node with the document
      String nameWithLanguage =
          oakRepositoryUtils.insertStringBeforePoint(filename, "-" + language);

      String modifiedFilename = oakRepositoryUtils.addFileNode(session, PATH_DELIMITER + dataFlowId,
          inputStream, nameWithLanguage, contentType);
      if (StringUtils.isBlank(modifiedFilename)) {
        throw new EEAException(EEAErrorMessage.FILE_NAME);
      }
      LOG.info("File added...");
      sendKafkaNotification(modifiedFilename.replace("-" + language, ""), dataFlowId, language,
          description, EventType.LOAD_DOCUMENT_COMPLETED_EVENT);
    } catch (RepositoryException | EEAException e) {
      LOG_ERROR.error("Error in uploadDocument due to", e);
      throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e);
    } finally {
      inputStream.close();
      oakRepositoryUtils.cleanUp(session, ns);
    }
  }


  /**
   * Gets the document.
   *
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @param language the language
   * @return the document
   * @throws EEAException the EEA exception
   */
  @Override
  public FileResponse getDocument(final String documentName, final Long dataFlowId,
      final String language) throws EEAException {
    Session session = null;
    FileResponse fileResponse = null;
    DocumentNodeStore ns = null;
    try {
      // Initialize the session
      ns = oakRepositoryUtils.initializeNodeStore();
      Repository repository = oakRepositoryUtils.initializeRepository(ns);
      session = oakRepositoryUtils.initializeSession(repository);

      // retrieve the file to the controller
      String nameWithLanguage =
          oakRepositoryUtils.insertStringBeforePoint(documentName, "-" + language);

      fileResponse = oakRepositoryUtils.getFileContents(session, PATH_DELIMITER + dataFlowId,
          nameWithLanguage);
      LOG.info("Fething the file...");
    } catch (IOException | RepositoryException e) {
      LOG_ERROR.error("Error in getDocument due to", e);
      if (e.getClass().equals(PathNotFoundException.class)) {
        throw new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND, e);
      }
      throw new EEAException(EEAErrorMessage.DOCUMENT_DOWNLOAD_ERROR, e);
    } finally {
      oakRepositoryUtils.cleanUp(session, ns);
    }
    return fileResponse;
  }

  /**
   * Delete document.
   *
   * @param documentId the document id
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @param language the language
   * @throws EEAException the EEA exception
   */
  @Override
  @Modified
  @Async
  public void deleteDocument(Long documentId, String documentName, Long dataFlowId,
      final String language) throws EEAException {
    Session session = null;
    DocumentNodeStore ns = null;
    try {
      // Initialize the session
      ns = oakRepositoryUtils.initializeNodeStore();
      Repository repository = oakRepositoryUtils.initializeRepository(ns);
      session = oakRepositoryUtils.initializeSession(repository);

      // Delete a file node with the document
      String nameWithLanguage =
          oakRepositoryUtils.insertStringBeforePoint(documentName, "-" + language);
      oakRepositoryUtils.deleteFileNode(session, dataFlowId.toString(), nameWithLanguage);
      LOG.info("File deleted...");

      oakRepositoryUtils.deleteBlobsFromRepository(ns);

      sendKafkaNotification(documentId, EventType.DELETE_DOCUMENT_COMPLETED_EVENT);

    } catch (Exception e) {
      LOG_ERROR.error("Error in deleteDocument due to", e);
      if (e.getClass().equals(PathNotFoundException.class)) {
        throw new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND, e);
      }
      throw new EEAException(EEAErrorMessage.EXECUTION_ERROR, e);
    } finally {
      oakRepositoryUtils.cleanUp(session, ns);
    }
  }

  @Override
  public InputStream readFromFile(final String fileName) throws FileNotFoundException {

    File file = new File(fileName);
    InputStream targetStream = new FileInputStream(file);
    return targetStream;

  }


  @Override
  public void writeToFile(final String fileName, final OutputStream content) throws IOException {

    try (FileOutputStream fos = new FileOutputStream(fileName)) {

      byte[] mybytes = content.toString().getBytes();

      fos.write(mybytes);
    }


  }



  /**
   * Send kafka notification.
   *
   * @param filename the filename
   * @param dataFlowId the data flow id
   * @param language the language
   * @param description the description
   * @param eventType the event type
   */
  public void sendKafkaNotification(final String filename, final Long dataFlowId,
      final String language, final String description, final EventType eventType) {
    Map<String, Object> result = new HashMap<>();
    result.put("dataflow_id", dataFlowId);
    result.put("filename", filename);
    result.put("language", language);
    result.put("description", description);
    kafkaSenderUtils.releaseKafkaEvent(eventType, result);
  }

  /**
   * Send kafka notification.
   *
   * @param documentId the document id
   * @param eventType the event type
   */
  public void sendKafkaNotification(final Long documentId, final EventType eventType) {
    Map<String, Object> result = new HashMap<>();
    result.put("documentId", documentId);
    kafkaSenderUtils.releaseKafkaEvent(eventType, result);
  }


  @Override
  @Async
  public void uploadSchemaSnapshot(InputStream inputStream, String contentType, String filename,
      Long designDataset) throws EEAException, IOException {

    Session session = null;
    DocumentNodeStore ns = null;
    try {
      if (filename == null || contentType == null
          || StringUtils.isBlank(FilenameUtils.getExtension(filename))) {
        throw new EEAException(EEAErrorMessage.FILE_FORMAT);
      }

      LOG.info("Adding the file...");
      // Initialize the session
      ns = oakRepositoryUtils.initializeNodeStore();
      Repository repository = oakRepositoryUtils.initializeRepository(ns);
      session = oakRepositoryUtils.initializeSession(repository);

      // Add a file node with the document
      String modifiedFilename = oakRepositoryUtils.addFileNode(session,
          PATH_DELIMITER_SNAPSHOT + designDataset, inputStream, filename, contentType);
      if (StringUtils.isBlank(modifiedFilename)) {
        throw new EEAException(EEAErrorMessage.FILE_NAME);
      }
      LOG.info("File snapshot added...");

    } catch (RepositoryException | EEAException e) {
      LOG_ERROR.error("Error in uploadSnapshotSchema document due to", e);
      throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e);
    } finally {
      inputStream.close();
      oakRepositoryUtils.cleanUp(session, ns);
    }

  }


  @Override
  public FileResponse getSnapshotDocument(final String documentName, final Long idDesignDataset)
      throws EEAException {
    Session session = null;
    FileResponse fileResponse = null;
    DocumentNodeStore ns = null;
    try {
      // Initialize the session
      ns = oakRepositoryUtils.initializeNodeStore();
      Repository repository = oakRepositoryUtils.initializeRepository(ns);
      session = oakRepositoryUtils.initializeSession(repository);

      // retrieve the file to the controller
      fileResponse = oakRepositoryUtils.getFileContents(session,
          PATH_DELIMITER_SNAPSHOT + idDesignDataset, documentName);
      LOG.info("Fething the file...");
    } catch (IOException | RepositoryException e) {
      LOG_ERROR.error("Error in getDocument due to", e);
      if (e.getClass().equals(PathNotFoundException.class)) {
        throw new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND, e);
      }
      throw new EEAException(EEAErrorMessage.DOCUMENT_DOWNLOAD_ERROR, e);
    } finally {
      oakRepositoryUtils.cleanUp(session, ns);
    }
    return fileResponse;
  }


  @Override
  @Modified
  @Async
  public void deleteSnapshotDocument(String documentName, Long designDatasetId)
      throws EEAException {
    Session session = null;
    DocumentNodeStore ns = null;
    try {
      // Initialize the session
      ns = oakRepositoryUtils.initializeNodeStore();
      Repository repository = oakRepositoryUtils.initializeRepository(ns);
      session = oakRepositoryUtils.initializeSession(repository);

      // Delete a file node with the document
      oakRepositoryUtils.deleteFileNode(session, designDatasetId.toString(), documentName);
      LOG.info("File deleted...");

      oakRepositoryUtils.deleteBlobsFromRepository(ns);


    } catch (Exception e) {
      LOG_ERROR.error("Error in deleteSnapshotDocument due to", e);
      if (e.getClass().equals(PathNotFoundException.class)) {
        throw new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND, e);
      }
      throw new EEAException(EEAErrorMessage.EXECUTION_ERROR, e);
    } finally {
      oakRepositoryUtils.cleanUp(session, ns);
    }
  }


}
