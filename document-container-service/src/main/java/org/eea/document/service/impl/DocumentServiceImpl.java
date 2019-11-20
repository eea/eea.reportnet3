package org.eea.document.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
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
import org.eea.interfaces.controller.dataflow.DataFlowDocumentController.DataFlowDocumentControllerZuul;
import org.eea.interfaces.vo.document.DocumentVO;
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

  /** The Constant PATH_DELIMITER_SNAPSHOT. */
  private static final String PATH_DELIMITER_SNAPSHOT = "/snapshotSchema/";

  /** The Constant PATH_DELIMITER_SNAPSHOT_DELETE. */
  private static final String PATH_DELIMITER_SNAPSHOT_DELETE = "snapshotSchema/";

  /** The oak repository utils. */
  @Autowired
  private OakRepositoryUtils oakRepositoryUtils;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The dataflow controller.
   */
  @Autowired
  private DataFlowDocumentControllerZuul dataflowController;

  /**
   * upload a document to the jackrabbit content repository.
   *
   * @param inputStream the input stream
   * @param contentType the content type
   * @param filename the filename
   * @param documentVO the document VO
   * @param size the size
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Async
  public void uploadDocument(final InputStream inputStream, final String contentType,
      final String filename, DocumentVO documentVO, final Long size)
      throws EEAException, IOException {
    Session session = null;
    DocumentNodeStore ns = null;
    try {
      if (filename == null || contentType == null
          || StringUtils.isBlank(FilenameUtils.getExtension(filename))) {
        throw new EEAException(EEAErrorMessage.FILE_FORMAT);
      }
      // save to metabase
      Long idDocument = dataflowController.insertDocument(documentVO);
      if (idDocument != null) {
        LOG.info("Adding the file...");
        // Initialize the session
        ns = oakRepositoryUtils.initializeNodeStore();
        Repository repository = oakRepositoryUtils.initializeRepository(ns);
        session = oakRepositoryUtils.initializeSession(repository);

        // Add a file node with the document
        oakRepositoryUtils.addFileNode(session, PATH_DELIMITER + documentVO.getDataflowId(),
            inputStream, Long.toString(idDocument), contentType);
        LOG.info("File added...");
      }
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
      final String language, final String description, final Long size, final EventType eventType) {
    Map<String, Object> result = new HashMap<>();
    result.put("dataflow_id", dataFlowId);
    result.put("filename", filename);
    result.put("language", language);
    result.put("description", description);
    result.put("size", humanReadableByteCount(size, true));
    result.put("date", Instant.now());
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

  /**
   * Human readable byte count.
   *
   * @param bytes the bytes
   * @param si the si
   * @return the string
   */
  private String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

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


  /**
   * Gets the snapshot document.
   *
   * @param documentName the document name
   * @param idDesignDataset the id design dataset
   * @return the snapshot document
   * @throws EEAException the EEA exception
   */
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


  /**
   * Delete snapshot document.
   *
   * @param documentName the document name
   * @param designDatasetId the design dataset id
   * @throws EEAException the EEA exception
   */
  @Override
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
      oakRepositoryUtils.deleteFileNode(session,
          PATH_DELIMITER_SNAPSHOT_DELETE + designDatasetId.toString(), documentName);
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
