package org.eea.document.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
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
 */
@Service("documentService")
public class DocumentServiceImpl implements DocumentService {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant PATH_DELIMITER.
   */
  private static final String PATH_DELIMITER = "/";

  /**
   * The Constant PATH_DELIMITER_SNAPSHOT.
   */
  private static final String PATH_DELIMITER_SNAPSHOT = "/snapshotSchema/";

  /**
   * The Constant PATH_DELIMITER_SNAPSHOT_DELETE.
   */
  private static final String PATH_DELIMITER_SNAPSHOT_DELETE = "snapshotSchema/";

  /** The Constant PATH_DELIMITER_COLLABORATION_DATAFLOW. */
  private static final String PATH_DELIMITER_COLLABORATION_DATAFLOW = "/collaboration/dataflow/";

  /** The Constant PATH_DELIMITER_COLLABORATION_DATAFLOW_DELETE. */
  private static final String PATH_DELIMITER_COLLABORATION_DATAFLOW_DELETE =
      "collaboration/dataflow/";

  /**
   * The oak repository utils.
   */
  @Autowired
  private OakRepositoryUtils oakRepositoryUtils;

  /**
   * The dataflow controller.
   */
  @Autowired
  private DataFlowDocumentControllerZuul dataflowController;

  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;


  /**
   * upload a document to the jackrabbit content repository.
   *
   * @param inputStream the input stream
   * @param contentType the content type
   * @param fileName the file name
   * @param documentVO the document VO
   * @param size the size
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Async
  public void uploadDocument(final InputStream inputStream, final String contentType,
      final String fileName, DocumentVO documentVO, final Long size)
      throws EEAException, IOException {
    Session session = null;
    DocumentNodeStore ns = null;
    try {
      if (fileName == null || contentType == null
          || StringUtils.isBlank(FilenameUtils.getExtension(fileName))) {
        throw new EEAException(EEAErrorMessage.FILE_FORMAT);
      }
      // save to metabase
      documentVO.setSize(size);
      documentVO.setDate(new Date());
      documentVO.setName(fileName);
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

        // Release finish event
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.UPLOAD_DOCUMENT_COMPLETED_EVENT,
            null,
            NotificationVO.builder()
                .user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
                .dataflowId(documentVO.getDataflowId()).fileName(fileName).build());
      } else {
        throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR);
      }
    } catch (RepositoryException | EEAException e) {
      // Release fail event
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.UPLOAD_DOCUMENT_FAILED_EVENT, null,
          NotificationVO.builder().user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
              .dataflowId((documentVO != null) ? documentVO.getDataflowId() : null)
              .fileName(fileName).error(e.getMessage()).build());
      LOG_ERROR.error("Error in uploadDocument due to", e);
      throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e);
    } finally {
      inputStream.close();
      oakRepositoryUtils.cleanUp(session, ns);
    }
  }

  /**
   * Update document.
   *
   * @param documentVO the document VO
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateDocument(DocumentVO documentVO) throws EEAException {
    try {
      // save to metabase
      if (documentVO == null) {
        throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR);
      }
      dataflowController.updateDocument(documentVO);
      // Send message to the frontend to notify about the updated document
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.UPDATED_DOCUMENT_COMPLETED_EVENT,
          null,
          NotificationVO.builder().user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
              .dataflowId(documentVO.getDataflowId()).fileName(documentVO.getName()).build());
    } catch (EEAException e) {
      LOG_ERROR.error("Error in uploadDocument due to", e);
      throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e);
    }
  }

  /**
   * Gets the document.
   *
   * @param documentId the document id
   * @param dataFlowId the data flow id
   *
   * @return the document
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public FileResponse getDocument(final Long documentId, final Long dataFlowId)
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
      fileResponse = oakRepositoryUtils.getFileContents(session, PATH_DELIMITER + dataFlowId,
          Long.toString(documentId));
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
   * @param dataFlowId the data flow id
   * @param deleteMetabase the delete metabase
   * @throws EEAException the EEA exception
   */
  @Override
  @Modified
  @Async
  public void deleteDocument(Long documentId, Long dataFlowId, Boolean deleteMetabase)
      throws EEAException {
    Session session = null;
    DocumentNodeStore ns = null;
    try {
      // Initialize the session
      ns = oakRepositoryUtils.initializeNodeStore();
      Repository repository = oakRepositoryUtils.initializeRepository(ns);
      session = oakRepositoryUtils.initializeSession(repository);

      // Delete a file node with the document
      oakRepositoryUtils.deleteFileNode(session, dataFlowId.toString(), Long.toString(documentId));
      LOG.info("File deleted...");

      if (Boolean.TRUE.equals(deleteMetabase)) {
        dataflowController.deleteDocument(documentId);
      }

      // Release finish event
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETE_DOCUMENT_COMPLETED_EVENT, null,
          NotificationVO.builder().user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
              .dataflowId(dataFlowId).build());

      // Physical delete. This won't be notified
      oakRepositoryUtils.deleteBlobsFromRepository(ns);
    } catch (Exception e) {
      LOG_ERROR.error("Error in deleteDocument due to", e);
      // Release finish event
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETE_DOCUMENT_FAILED_EVENT, null,
          NotificationVO.builder().user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
              .dataflowId(dataFlowId).error(e.getMessage()).build());
    } finally {
      oakRepositoryUtils.cleanUp(session, ns);
    }
  }

  /**
   * Upload schema snapshot.
   *
   * @param inputStream the input stream
   * @param contentType the content type
   * @param filename the filename
   * @param designDataset the design dataset
   *
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

      LOG.info("Adding the file... {}", filename);
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
      LOG.info("File snapshot added... {}", filename);

    } catch (RepositoryException | EEAException e) {
      LOG_ERROR.error("Error in uploadSnapshotSchema, document {} due to {}", filename,
          e.getMessage(), e);
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
   *
   * @return the snapshot document
   *
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
      LOG.info("Fething the file... {}", documentName);
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
   *
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
  @Async
  @Override
  public void uploadCollaborationDocument(InputStream inputStream, String contentType,
      String filename, Long dataflowId, Long messageId) throws EEAException, IOException {

    Session session = null;
    DocumentNodeStore ns = null;
    try {
      if (filename == null || contentType == null
          || StringUtils.isBlank(FilenameUtils.getExtension(filename))) {
        throw new EEAException(EEAErrorMessage.FILE_FORMAT);
      }

      LOG.info("Adding the file... {}", filename);
      // Initialize the session
      ns = oakRepositoryUtils.initializeNodeStore();
      Repository repository = oakRepositoryUtils.initializeRepository(ns);
      session = oakRepositoryUtils.initializeSession(repository);

      // Add a file node with the document
      String modifiedFilename = oakRepositoryUtils.addFileNode(session,
          PATH_DELIMITER_COLLABORATION_DATAFLOW + dataflowId, inputStream,
          messageId + "_" + filename, contentType);
      if (StringUtils.isBlank(modifiedFilename)) {
        throw new EEAException(EEAErrorMessage.FILE_NAME);
      }
      LOG.info("File collaboration attachment added... {}", filename);

    } catch (RepositoryException | EEAException e) {
      LOG_ERROR.error("Error in uploadCollaborationDocument, document {} due to {}", filename,
          e.getMessage(), e);
      throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e);
    } finally {
      inputStream.close();
      oakRepositoryUtils.cleanUp(session, ns);
    }

  }


  /**
   * Delete collaboration document.
   *
   * @param documentName the document name
   * @param dataflowId the dataflow id
   * @param messageId the message id
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void deleteCollaborationDocument(String documentName, Long dataflowId, Long messageId)
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
          PATH_DELIMITER_COLLABORATION_DATAFLOW_DELETE + dataflowId,
          messageId + "_" + documentName);
      LOG.info("File {} deleted...", documentName);
      oakRepositoryUtils.deleteBlobsFromRepository(ns);
    } catch (Exception e) {
      LOG_ERROR.error("Error deleting file {} in deleteCollaborationDocument due to", documentName,
          e);
      if (e.getClass().equals(PathNotFoundException.class)) {
        throw new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND, e);
      }
      throw new EEAException(EEAErrorMessage.EXECUTION_ERROR, e);
    } finally {
      oakRepositoryUtils.cleanUp(session, ns);
    }
  }


  /**
   * Gets the collaboration document.
   *
   * @param documentName the document name
   * @param dataflowId the dataflow id
   * @param messageId the message id
   * @return the collaboration document
   * @throws EEAException the EEA exception
   */
  @Override
  public FileResponse getCollaborationDocument(final String documentName, final Long dataflowId,
      final Long messageId) throws EEAException {
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
          PATH_DELIMITER_COLLABORATION_DATAFLOW + dataflowId, messageId + "_" + documentName);
      LOG.info("Fething the file... {}", documentName);
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

}
