package org.eea.document.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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
import org.springframework.web.multipart.MultipartFile;

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

  /** The Constant PATH_DELIMITER. */
  private static final String PATH_DELIMITER = "/";

  /** The oak repository utils. */
  @Autowired
  private OakRepositoryUtils oakRepositoryUtils;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * upload a document to the jackrabbit content repository.
   *
   * @param file the file
   * @param dataFlowId the data flow id
   * @param language the language
   * @param description the description
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void uploadDocument(final MultipartFile file, final Long dataFlowId, final String language,
      final String description) throws EEAException {
    Session session = null;
    DocumentNodeStore ns = null;
    try {
      if (file == null) {
        throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND);
      }

      if (file.getOriginalFilename() == null || file.getContentType() == null) {
        throw new EEAException(EEAErrorMessage.FILE_FORMAT);
      }

      LOG.info("Adding the file...");
      // Initialize the session
      ns = oakRepositoryUtils.initializeNodeStore();
      Repository repository = oakRepositoryUtils.initializeRepository(ns);
      session = oakRepositoryUtils.initializeSession(repository);

      // Add a file node with the document
      String nameWithLanguage =
          oakRepositoryUtils.insertStringBeforePoint(file.getOriginalFilename(), "-" + language);

      String modifiedFilename = oakRepositoryUtils.addFileNode(session, PATH_DELIMITER + dataFlowId,
          file.getInputStream(), nameWithLanguage, file.getContentType());
      if (StringUtils.isBlank(modifiedFilename)) {
        throw new EEAException(EEAErrorMessage.FILE_NAME);
      }
      LOG.info("File added...");
      sendKafkaNotification(modifiedFilename.replace("-" + language, ""), dataFlowId, language,
          description, EventType.LOAD_DOCUMENT_COMPLETED_EVENT);
    } catch (RepositoryException | IOException | EEAException e) {
      throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e);
    } finally {
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
    try (FileOutputStream fos = new FileOutputStream(documentName)) {
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
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @param language the language
   * @throws EEAException the EEA exception
   */
  @Override
  @Modified
  @Async
  public void deleteDocument(String documentName, Long dataFlowId, final String language)
      throws EEAException {
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

      oakRepositoryUtils.runGC(ns);

      sendKafkaNotification(documentName, dataFlowId, language, null,
          EventType.DELETE_DOCUMENT_COMPLETED_EVENT);

    } catch (Exception e) {
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
      final String language, final String description, final EventType eventType) {
    Map<String, Object> result = new HashMap<>();
    result.put("dataflow_id", dataFlowId);
    result.put("filename", filename);
    result.put("language", language);
    result.put("description", description);
    kafkaSenderUtils.releaseKafkaEvent(eventType, result);
  }

}
