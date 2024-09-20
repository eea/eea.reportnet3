package org.eea.document.service.impl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.document.service.DocumentServiceDL;
import org.eea.document.type.FileResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Service("documentServiceDL")
@ImportDataLakeCommons
public class DocumentServiceDLImpl implements DocumentServiceDL {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceDLImpl.class);

    @Value("${importPath}")
    private String importPath;

    private final KafkaSenderUtils kafkaSenderUtils;

    private final DataFlowDocumentControllerZuul dataflowController;

    private final S3Helper s3Helper;

    private final S3Service s3Service;

    public DocumentServiceDLImpl(DataFlowDocumentControllerZuul dataflowController, S3Helper s3Helper, KafkaSenderUtils kafkaSenderUtils) {
        this.s3Helper = s3Helper;
        this.s3Service = s3Helper.getS3Service();
        this.dataflowController = dataflowController;
        this.kafkaSenderUtils = kafkaSenderUtils;
    }

    @Override
    @Async
    public void uploadDocumentDL(MultipartFile multipartFile, final String fileName, DocumentVO documentVO, final Long size) throws EEAException, IOException {
        if (multipartFile.getOriginalFilename() == null || StringUtils.isBlank(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))) {
            throw new EEAException(EEAErrorMessage.FILE_FORMAT);
        }

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
            String attachmentPathInS3 = s3Service.getS3Path(s3AttachmentsPathResolver);
            s3Helper.uploadFileToBucket(attachmentPathInS3, file.getAbsolutePath());
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
     * Gets the document.
     *
     * @param document document
     * @return the document in bytes
     */
    @Override
    public FileResponse getDocumentDL(DocumentVO document) {

        String modifiedFileName = "document_" + document.getId() + "_" + document.getName();
        S3PathResolver s3AttachmentsPathResolver = new S3PathResolver(document.getDataflowId(), modifiedFileName, LiteralConstants.S3_SUPPORTING_DOCUMENTS_FILE_PATH);
        String attachmentPathInS3 = s3Service.getS3Path(s3AttachmentsPathResolver);
        byte[] fileAsBytes = s3Helper.getBytesFromS3(attachmentPathInS3);
        FileResponse fileResponse = new FileResponse();
        fileResponse.setBytes(fileAsBytes);

        return fileResponse;
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
    @Async
    public void deleteDocumentDL(DocumentVO documentVO, Boolean deleteMetabase) throws EEAException {
        try {

            String modifiedFileName = "document_" + documentVO.getId() + "_" + documentVO.getName();
            S3PathResolver s3AttachmentsPathResolver = new S3PathResolver(documentVO.getDataflowId(), modifiedFileName, LiteralConstants.S3_SUPPORTING_DOCUMENTS_FILE_PATH);
            String attachmentPathInS3 = s3Service.getS3Path(s3AttachmentsPathResolver);
            s3Helper.deleteFileFromS3(attachmentPathInS3);

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
     * Clone all documents
     *
     * @param originDataflowId the originDataflowId
     * @param destinationDataflowId thed estinationDataflowId
     *
     */
    @Override
    public void cloneAllDocumentsInDataflow(final Long originDataflowId, final Long destinationDataflowId) throws EEAException {
        // Copy the dataflow documents
        List<DocumentVO> documents = dataflowController.getAllDocumentsByDataflowId(originDataflowId);
        for (DocumentVO documentVO : documents) {
            documentVO.setDataflowId(destinationDataflowId);
            Long idDocument = dataflowController.insertDocument(documentVO);
            if (idDocument == null) {
                throw new EEAException(EEAErrorMessage.DOCUMENT_UPLOAD_ERROR);
            }
            String modifiedFileName = "document_" + idDocument + "_" + documentVO.getName();
            S3PathResolver s3OriginDocumentPathResolver = new S3PathResolver(originDataflowId, modifiedFileName, LiteralConstants.S3_SUPPORTING_DOCUMENTS_FILE_PATH);
            String originDocumentPathInS3 = s3Service.getS3Path(s3OriginDocumentPathResolver);
            S3PathResolver s3DestDocumentPathResolver = new S3PathResolver(destinationDataflowId, modifiedFileName, LiteralConstants.S3_SUPPORTING_DOCUMENTS_FILE_PATH);
            String destDocumentPathInS3 = s3Service.getS3Path(s3DestDocumentPathResolver);
            s3Helper.copyFileToAnotherDestination(originDocumentPathInS3, destDocumentPathInS3);
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
    @Async
    public void uploadCollaborationDocumentDL(InputStream inputStream, String filename,
                                              Long dataflowId, Long providerId, Long messageId) throws IOException, EEAException {
        if (filename == null || StringUtils.isBlank(FilenameUtils.getExtension(filename))) {
            throw new EEAException(EEAErrorMessage.FILE_FORMAT);
        }
        String attachmentPathInS3 = null;
        String modifiedFileName = "message_" + messageId + "_" + filename;
        String absolutePath = importPath + "/" + dataflowId + "/" + modifiedFileName;
        File file = null;
      try (inputStream) {
        // Ensure that the directory exists
        File folder = new File(importPath + "/" + dataflowId);
        if (!folder.exists()) {
          folder.mkdir();
        }
        file = new File(absolutePath);
        try (FileOutputStream fos = new FileOutputStream(file)) {
          FileCopyUtils.copy(inputStream, fos);
        }
        S3PathResolver s3TechnicalAcceptancePathResolver = new S3PathResolver(dataflowId, modifiedFileName, LiteralConstants.S3_TECHNICAL_ACCEPTANCE_FILE_PATH);
        s3TechnicalAcceptancePathResolver.setDataProviderId(providerId);
        attachmentPathInS3 = s3Service.getS3Path(s3TechnicalAcceptancePathResolver);
        s3Helper.uploadFileToBucket(attachmentPathInS3, absolutePath);
      } catch (Exception e) {
        LOG.error("Error while uploading file {} to s3 in path {} Error: {}", absolutePath, attachmentPathInS3, e.getMessage());
        throw e;
      } finally {
        if (file != null && file.exists()) {
          file.delete();
        }
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
        String attachmentPathInS3 = s3Service.getS3Path(s3TechnicalAcceptancePathResolver);
        s3Helper.deleteFileFromS3(attachmentPathInS3);
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
        String attachmentPathInS3 = s3Service.getS3Path(s3AttachmentsPathResolver);
        byte[] fileAsBytes = s3Helper.getBytesFromS3(attachmentPathInS3);
        FileResponse fileResponse = new FileResponse();
        fileResponse.setBytes(fileAsBytes);

        return fileResponse;
    }


}
