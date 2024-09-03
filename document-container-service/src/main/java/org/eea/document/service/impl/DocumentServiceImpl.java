package org.eea.document.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
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
import org.eea.utils.LiteralConstants;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
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

  @Value("${importPath}")
  private String importPath;

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

  private final S3Helper s3HelperPrivate;

  private final S3Service s3ServicePrivate;

  public DocumentServiceImpl(S3Helper s3HelperPrivate, S3Service s3ServicePrivate) {
    this.s3HelperPrivate = s3HelperPrivate;
    this.s3ServicePrivate = s3ServicePrivate;
  }

  /**
   * upload a document to the jackrabbit content repository.
   *
   * @param inputStream the input stream
   * @param contentType the content type
   * @param fileName the file name
   * @param documentVO the document VO
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
        LOG.info("Inserting document {} with id {}", fileName, idDocument);
        // Initialize the session
        ns = oakRepositoryUtils.initializeNodeStore();
        Repository repository = oakRepositoryUtils.initializeRepository(ns);
        session = oakRepositoryUtils.initializeSession(repository);

        // Add a file node with the document
        oakRepositoryUtils.addFileNode(session, PATH_DELIMITER + documentVO.getDataflowId(),
            inputStream, Long.toString(idDocument), contentType);
        LOG.info("Finished inserting document {} with id {}", fileName, idDocument);

        // Release finish event
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.UPLOAD_DOCUMENT_COMPLETED_EVENT,
            null,
            NotificationVO.builder()
                .user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
                .dataflowId(documentVO.getDataflowId()).fileName(fileName).build());
        LOG.info("Successfully uploaded document {} with id {}", fileName, idDocument);
      } else {
        throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR);
      }
    } catch (RepositoryException | EEAException e) {
      // Release fail event
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.UPLOAD_DOCUMENT_FAILED_EVENT, null,
          NotificationVO.builder().user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
              .dataflowId((documentVO != null) ? documentVO.getDataflowId() : null)
              .fileName(fileName).error(e.getMessage()).build());
      LOG.error("Error in uploadDocument {} due to exception: {}", fileName, e.getMessage(), e);
      throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e);
    } finally {
      inputStream.close();
      oakRepositoryUtils.cleanUp(session, ns);
    }
  }

  @Override
  public void uploadDocumentDL(MultipartFile multipartFile, final String fileName, DocumentVO documentVO, final Long size) throws EEAException, IOException {
    Long dataflowId = documentVO.getDataflowId();

    // save to metabase
    documentVO.setSize(size);
    documentVO.setDate(new Date());
    documentVO.setName(fileName);
    Long idDocument = dataflowController.insertDocument(documentVO);
    String modifiedFileName = "document_" + idDocument + "_" + fileName;
    if (idDocument != null) {
      LOG.info("Inserting document {} to s3 with id {}", fileName, idDocument);

      File folder = new File(importPath + "/" + dataflowId);
      if (!folder.exists()) {
        folder.mkdir();
      }
      String filePathInReportnet = folder.getAbsolutePath() + "/" + multipartFile.getOriginalFilename();
      File file = new File(filePathInReportnet);
      try (FileOutputStream fos = new FileOutputStream(file)) {
        FileCopyUtils.copy(multipartFile.getInputStream(), fos);
      }
      catch (Exception e){
        LOG.error("Could not store supporting file to disk for dataflowId {} fileName {}", dataflowId, file.getName());
        throw e;
      }

      S3PathResolver s3AttachmentsPathResolver = new S3PathResolver(dataflowId, modifiedFileName, LiteralConstants.S3_SUPPORTING_DOCUMENTS_FILE_PATH);
      String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3AttachmentsPathResolver);
      s3HelperPrivate.uploadFileToBucket(attachmentPathInS3, file.getAbsolutePath());
      file.delete();

      // Release finish event
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.UPLOAD_DOCUMENT_COMPLETED_EVENT,
              null,
              NotificationVO.builder()
                      .user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
                      .dataflowId(dataflowId).fileName(fileName).build());
      LOG.info("Successfully uploaded document {} with id {}", fileName, idDocument);
    } else {
      throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR);
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
      LOG.info("Successfully updated document with id {}", documentVO.getId());
    } catch (EEAException e) {
      if(documentVO != null){
        LOG.error("Error in updateDocument for file with id {} and dataflowId {} due to exception {}", documentVO.getId(), documentVO.getDataflowId(), e.getMessage(), e);
      }
      else{
        LOG.error("Error in updateDocument due to exception {}", e.getMessage(), e);
      }
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
      LOG.info("Fecthing the file...");
    } catch (IOException | RepositoryException e) {
      LOG.error("Error in getDocument due to", e);
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
   * Gets the document.
   *
   * @param document document
   * @return the document in bytes
   */
  @Override
  public FileResponse getDocumentDL(DocumentVO document) {

    String modifiedFileName = "document_" + document.getId() + "_" + document.getName();
    S3PathResolver s3AttachmentsPathResolver = new S3PathResolver(document.getDataflowId(), modifiedFileName, LiteralConstants.S3_SUPPORTING_DOCUMENTS_FILE_PATH);
    String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3AttachmentsPathResolver);
    byte[] fileAsBytes = s3HelperPrivate.getBytesFromS3(attachmentPathInS3);
    FileResponse fileResponse = new FileResponse();
    fileResponse.setBytes(fileAsBytes);

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
      LOG.info("Successfully deleted document with id {} for dataflowId {}", documentId, dataFlowId);
    } catch (Exception e) {
      LOG.error("Error in deleteDocument with id {} for dataflowId {} due to", documentId, dataFlowId, e);
      // Release finish event
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETE_DOCUMENT_FAILED_EVENT, null,
          NotificationVO.builder().user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
              .dataflowId(dataFlowId).error(e.getMessage()).build());
    } finally {
      oakRepositoryUtils.cleanUp(session, ns);
    }
  }

  /**
   * Delete document.
   *
   * @param documentVO the document
   * @param deleteMetabase the delete metabase
   * @throws EEAException the EEA exception
   */
  @Override
  @Modified
  public void deleteDocumentDL(DocumentVO documentVO, Boolean deleteMetabase) throws EEAException {
    try {

      String modifiedFileName = "document_" + documentVO.getId() + "_" + documentVO.getName();
      S3PathResolver s3AttachmentsPathResolver = new S3PathResolver(documentVO.getDataflowId(), modifiedFileName, LiteralConstants.S3_SUPPORTING_DOCUMENTS_FILE_PATH);
      String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3AttachmentsPathResolver);
      s3HelperPrivate.deleteFileFromS3(attachmentPathInS3);

      if (Boolean.TRUE.equals(deleteMetabase)) {
        dataflowController.deleteDocument(documentVO.getId());
      }

      // Release finish event
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETE_DOCUMENT_COMPLETED_EVENT, null,
              NotificationVO.builder().user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
                      .dataflowId(documentVO.getDataflowId()).build());

      LOG.info("Successfully deleted big data document with id {} for dataflowId {}", documentVO.getId(), documentVO.getDataflowId());
    } catch (Exception e) {
      LOG.error("Error in deleteDocumentDL with id {} for dataflowId {} due to", documentVO.getId(), documentVO.getDataflowId(), e);
      // Release finish event
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETE_DOCUMENT_FAILED_EVENT, null,
              NotificationVO.builder().user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
                      .dataflowId(documentVO.getDataflowId()).error(e.getMessage()).build());
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

      LOG.info("Inserting schema snapshot document {} for designDataset {}", filename, designDataset);
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
      LOG.info("Successfully uploaded schema snapshot document {} for designDatasetId {}", filename, designDataset);

    } catch (RepositoryException | EEAException e) {
      LOG.error("Error in uploadSnapshotSchema, document {}, designDatasetId {} due to {}", filename, designDataset,
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
      LOG.error("Error in getDocument due to", e);
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
      LOG.info("File {} deleted for designDatasetId {}", documentName, designDatasetId);
      oakRepositoryUtils.deleteBlobsFromRepository(ns);
      LOG.info("Successfully deleted schema snapshot document {} for designDatasetId {}", documentName, designDatasetId);
    } catch (Exception e) {
      LOG.error("Error in deleteSnapshotDocument for file {} and designDatasetId {} due to", documentName, designDatasetId, e);
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

      LOG.info("Inserting collaboration document {} for dataflowId {}", filename, dataflowId);
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
      LOG.info("Successfully uploaded collaboration document {} for dataflowId {}", filename, dataflowId);

    } catch (RepositoryException | EEAException e) {
      LOG.error("Error in uploadCollaborationDocument, document {} dataflowId {} due to {}", filename, dataflowId,
          e.getMessage(), e);
      throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR, e);
    } finally {
      inputStream.close();
      oakRepositoryUtils.cleanUp(session, ns);
    }

  }

  /**
   * Upload collaboration document in s3.
   *
   * @param inputStream the input stream
   * @param filename the filename
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param messageId the message id
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void uploadCollaborationDocumentDL(InputStream inputStream, String filename,
                                     Long dataflowId, Long providerId, Long messageId) throws IOException{

    String modifiedFileName = "message_" + messageId + "_" + filename;
    String absolutePath = importPath + "/" + dataflowId + "/" + modifiedFileName;
    Path path = Paths.get(absolutePath);
    try {
      Files.copy(inputStream, path);
      S3PathResolver s3TechnicalAcceptancePathResolver = new S3PathResolver(dataflowId, modifiedFileName, LiteralConstants.S3_TECHNICAL_ACCEPTANCE_FILE_PATH);
      s3TechnicalAcceptancePathResolver.setDataProviderId(providerId);
      String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3TechnicalAcceptancePathResolver);
      s3HelperPrivate.uploadFileToBucket(attachmentPathInS3, absolutePath);
      Files.delete(path);
      } finally {
        inputStream.close();
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
      oakRepositoryUtils.deleteBlobsFromRepository(ns);
      LOG.info("Successfully deleted collaboration document {} for dataflowId {}", documentName, dataflowId);
    } catch (Exception e) {
      LOG.error("Error deleting file {} for dataflowId {} in deleteCollaborationDocument due to", documentName, dataflowId,
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
   * Delete collaboration document from s3.
   *
   * @param documentName the document name
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param messageId the message id
   */
  @Override
  public void deleteCollaborationDocumentDL(String documentName, Long dataflowId, Long providerId, Long messageId){

    String fileName = "message_" + messageId + "_" + documentName;
    S3PathResolver s3TechnicalAcceptancePathResolver = new S3PathResolver(dataflowId, fileName, LiteralConstants.S3_TECHNICAL_ACCEPTANCE_FILE_PATH);
    s3TechnicalAcceptancePathResolver.setDataProviderId(providerId);
    String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3TechnicalAcceptancePathResolver);
    s3HelperPrivate.deleteFileFromS3(attachmentPathInS3);
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
      LOG.error("Error in getDocument due to", e);
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
   * Gets the collaboration document from s3.
   *
   * @param documentName the document name
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param messageId the message id
   */
  @Override
  public FileResponse getCollaborationDocumentDL(final String documentName, final Long dataflowId, Long providerId, Long messageId){
    String fileName = "message_" + messageId + "_" + documentName;
    S3PathResolver s3AttachmentsPathResolver = new S3PathResolver(dataflowId, fileName, LiteralConstants.S3_TECHNICAL_ACCEPTANCE_FILE_PATH);
    s3AttachmentsPathResolver.setDataProviderId(providerId);
    String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3AttachmentsPathResolver);
    byte[] fileAsBytes = s3HelperPrivate.getBytesFromS3(attachmentPathInS3);
    FileResponse fileResponse = new FileResponse();
    fileResponse.setBytes(fileAsBytes);

    return fileResponse;
  }

}
