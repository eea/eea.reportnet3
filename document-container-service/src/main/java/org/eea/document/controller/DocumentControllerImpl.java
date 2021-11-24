package org.eea.document.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Produces;
import org.apache.commons.lang3.StringUtils;
import org.eea.document.service.DocumentService;
import org.eea.document.type.FileResponse;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowDocumentController.DataFlowDocumentControllerZuul;
import org.eea.interfaces.controller.document.DocumentController;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import feign.FeignException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;

/**
 * The type Document controller.
 */
@RestController
@RequestMapping("/document")
@Api(tags = "Document: Document Manager")
public class DocumentControllerImpl implements DocumentController {

  /**
   * The document service.
   */
  @Autowired
  private DocumentService documentService;

  /**
   * The dataflow controller.
   */
  @Autowired
  private DataFlowDocumentControllerZuul dataflowController;

  /** The notification controller zuul. */
  @Autowired
  private NotificationControllerZuul notificationControllerZuul;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DocumentControllerImpl.class);

  /**
   * Upload document.
   *
   * @param file the file
   * @param dataflowId the dataflow id
   * @param description the description
   * @param language the language
   * @param isPublic the is public
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  @HystrixCommand
  @PostMapping(value = "/v1/upload/{dataflowId}")
  @ApiOperation(value = "Upload document to dataflow help", hidden = false,
      notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  public void uploadDocument(@ApiParam(value = "File to upload") @RequestPart("file") final MultipartFile file,
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable("dataflowId") final Long dataflowId,
      @ApiParam(value = "Document description",
          example = "description") @RequestParam("description") final String description,
      @ApiParam(value = "Document language",
          example = "English") @RequestParam("language") final String language,
      @ApiParam(value = "If document is public or not",
          example = "true") @RequestParam("isPublic") final Boolean isPublic) {

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataflowId);
    notificationControllerZuul.createUserNotificationPrivate("DOCUMENT_UPLOADING_INIT_INFO",
        userNotificationContentVO);

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    LOG.info("uploadDocument");
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
    }
    if (dataflowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    try {
      DocumentVO documentVO = new DocumentVO();
      documentVO.setDataflowId(dataflowId);
      documentVO.setDescription(description);
      documentVO.setLanguage(language);
      documentVO.setIsPublic(isPublic);
      documentService.uploadDocument(file.getInputStream(), file.getContentType(),
          file.getOriginalFilename(), documentVO, file.getSize());
    } catch (EEAException | IOException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Upload document legacy.
   *
   * @param file the file
   * @param dataflowId the dataflow id
   * @param description the description
   * @param language the language
   * @param isPublic the is public
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  @HystrixCommand
  @PostMapping(value = "/upload/{dataflowId}")
  @ApiOperation(value = "Upload dataflow document by dataflow Id", hidden = true,
      notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  public void uploadDocumentLegacy(@RequestPart("file") final MultipartFile file,
      @PathVariable("dataflowId") final Long dataflowId,
      @RequestParam("description") final String description,
      @RequestParam("language") final String language,
      @RequestParam("isPublic") final Boolean isPublic) {
    this.uploadDocument(file, dataflowId, description, language, isPublic);
  }


  /**
   * Gets the document.
   *
   * @param documentId the document id
   * @param dataflowId the dataflow id
   * @return the document
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('ADMIN')")
  @GetMapping(value = "/v1/{documentId}/dataflow/{dataflowId}",
      produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @ApiOperation(value = "Download document from dataflow help by document id", hidden = false,
      notes = "Allowed roles: CUSTODIAN, STEWARD, OBSERVER, LEAD REPORTER, REPORTER WRITE, REPORTER READ, EDITOR READ, EDITOR WRITE, NATIONAL COORDINATOR, ADMIN")
  @HystrixCommand
  public Resource getDocument(
      @ApiParam(value = "Document id",
          example = "0") @PathVariable("documentId") final Long documentId,
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable("dataflowId") final Long dataflowId) {
    try {
      DocumentVO document = dataflowController.getDocumentInfoById(documentId);
      if (document == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DOCUMENT_NOT_FOUND);
      }
      FileResponse file = documentService.getDocument(documentId, document.getDataflowId());
      return new ByteArrayResource(file.getBytes());
    } catch (final EEAException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Gets the document legacy.
   *
   * @param documentId the document id
   * @param dataflowId the dataflow id
   * @return the document legacy
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('ADMIN')")
  @GetMapping(value = "/{documentId}/dataflow/{dataflowId}",
      produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @ApiOperation(value = "Download document from dataflow help by document id", hidden = true,
      notes = "Allowed roles: CUSTODIAN, STEWARD, OBSERVER, LEAD REPORTER, REPORTER WRITE, REPORTER READ, EDITOR READ, EDITOR WRITE, NATIONAL COORDINATOR, ADMIN")
  @HystrixCommand
  public Resource getDocumentLegacy(
      @ApiParam(value = "Document Id",
          example = "0") @PathVariable("documentId") final Long documentId,
      @ApiParam(value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") final Long dataflowId) {
    return this.getDocument(documentId, dataflowId);
  }

  /**
   * Gets the document.
   *
   * @param documentId the document id
   *
   * @return the document
   */
  @Override
  @GetMapping(value = "/public/{documentId}", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @HystrixCommand
  @ApiOperation(value = "Get Public Document", hidden = true)
  public Resource getPublicDocument(@ApiParam(value = "Document Id",
      example = "0") @PathVariable("documentId") final Long documentId) {
    try {
      DocumentVO document = dataflowController.getDocumentInfoById(documentId);
      if (document == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DOCUMENT_NOT_FOUND);
      }
      if (document.getIsPublic()) {
        FileResponse file = documentService.getDocument(documentId, document.getDataflowId());
        return new ByteArrayResource(file.getBytes());
      } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.DOCUMENT_NOT_PUBLIC);
      }
    } catch (final EEAException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Delete document. You can delete metabase if you want , the boolean is to delete metabase by
   * your own
   *
   * @param documentId the document id
   * @param dataflowId the dataflow id
   * @param deleteMetabase the delete metabase
   * @throws Exception the exception
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  @DeleteMapping(value = "/v1/{documentId}/dataflow/{dataflowId}")
  @ApiOperation(value = "Delete document from dataflow help by document id", hidden = false,
      notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  public void deleteDocument(
      @ApiParam(value = "Document id",
          example = "0") @PathVariable("documentId") final Long documentId,
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable("dataflowId") final Long dataflowId,
      @ApiParam(value = "Delete document in metabase", example = "true") @RequestParam(
          value = "deleteMetabase", required = false,
          defaultValue = "true") final Boolean deleteMetabase)
      throws Exception {

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataflowId);
    notificationControllerZuul.createUserNotificationPrivate("DELETE_DOCUMENT_INIT_INFO",
        userNotificationContentVO);

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    try {
      DocumentVO document = dataflowController.getDocumentInfoById(documentId);
      if (document == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DOCUMENT_NOT_FOUND);
      }
      documentService.deleteDocument(documentId, document.getDataflowId(), deleteMetabase);
    } catch (final FeignException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    } catch (final EEAException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Delete document legacy.
   *
   * @param documentId the document id
   * @param dataflowId the dataflow id
   * @param deleteMetabase the delete metabase
   * @throws Exception the exception
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  @DeleteMapping(value = "/{documentId}/dataflow/{dataflowId}")
  @ApiOperation(value = "Delete document from dataflow help by document id", hidden = true,
      notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  public void deleteDocumentLegacy(
      @ApiParam(value = "Document id",
          example = "0") @PathVariable("documentId") final Long documentId,
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable("dataflowId") final Long dataflowId,
      @ApiParam(value = "Delete document in metabase", example = "true") @RequestParam(
          value = "deleteMetabase", required = false,
          defaultValue = "true") final Boolean deleteMetabase)
      throws Exception {
    this.deleteDocument(documentId, dataflowId, deleteMetabase);
  }

  /**
   * Update document.
   *
   * @param file the file
   * @param dataflowId the data flow id
   * @param description the description
   * @param language the language
   * @param idDocument the id document
   * @param isPublic the is public
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  @PutMapping(value = "/v1/update/{idDocument}/dataflow/{dataflowId}")
  @ApiOperation(value = "Update dataflow document by document id", hidden = false,
      notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  public void updateDocument(
      @ApiParam(value = "File to upload") @RequestPart(name = "file",
          required = false) final MultipartFile file,
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable("dataflowId") final Long dataflowId,
      @ApiParam(value = "Document description", example = "description") @RequestParam(name = "description",
          required = false) final String description,
      @ApiParam(value = "Document language", example = "English") @RequestParam(name = "language",
          required = false) final String language,
      @ApiParam(value = "Document id",
          example = "0") @PathVariable("idDocument") final Long idDocument,
      @ApiParam(value = "If the document is public or not",
          example = "true") @RequestParam("isPublic") final Boolean isPublic) {

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataflowId);
    notificationControllerZuul.createUserNotificationPrivate("DOCUMENT_UPLOADING_INIT_INFO",
        userNotificationContentVO);

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    LOG.info("updateDocument");
    if (dataflowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    try {
      DocumentVO documentVO = dataflowController.getDocumentInfoById(idDocument);
      documentVO.setDataflowId(dataflowId);
      if (StringUtils.isNotBlank(description)) {
        documentVO.setDescription(description);
      }
      if (StringUtils.isNotBlank(language)) {
        documentVO.setLanguage(language);
      }
      documentVO.setId(idDocument);
      if (isPublic != null) {
        documentVO.setIsPublic(isPublic);
      }
      if (file == null || file.isEmpty()) {
        documentService.updateDocument(documentVO);
      } else {
        documentService.uploadDocument(file.getInputStream(), file.getContentType(),
            file.getOriginalFilename(), documentVO, file.getSize());
      }
    } catch (EEAException | IOException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Update document legacy.
   *
   * @param file the file
   * @param dataflowId the dataflow id
   * @param description the description
   * @param language the language
   * @param idDocument the id document
   * @param isPublic the is public
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  @PutMapping(value = "/update/{idDocument}/dataflow/{dataflowId}")
  @ApiOperation(value = "Update dataflow document", hidden = true,
      notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  public void updateDocumentLegacy(
      @ApiParam(value = "File to upload") @RequestPart(name = "file",
          required = false) final MultipartFile file,
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable("dataflowId") final Long dataflowId,
      @ApiParam(value = "Document description", example = "abc") @RequestParam(name = "description",
          required = false) final String description,
      @ApiParam(value = "Document language", example = "English") @RequestParam(name = "language",
          required = false) final String language,
      @ApiParam(value = "Document id",
          example = "0") @PathVariable("idDocument") final Long idDocument,
      @ApiParam(value = "Document is public or not",
          example = "true") @RequestParam("isPublic") final Boolean isPublic) {
    this.updateDocument(file, dataflowId, description, language, idDocument, isPublic);
  }

  /**
   * Gets the all documents by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the all documents by dataflow
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('ADMIN')")
  @GetMapping(value = "/v1/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get list of dataflow documents from dataflow help",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DocumentVO.class,
      responseContainer = "List", hidden = false,
      notes = "Allowed roles: CUSTODIAN, STEWARD, OBSERVER, LEAD REPORTER, REPORTER WRITE, REPORTER READ, EDITOR READ, EDITOR WRITE, NATIONAL COORDINATOR, ADMIN")
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public List<DocumentVO> getAllDocumentsByDataflow(@ApiParam(value = "Dataflow id",
          example = "0") @PathVariable("dataflowId") Long dataflowId) {
    List<DocumentVO> documents = new ArrayList<>();
    documents = dataflowController.getAllDocumentsByDataflowId(dataflowId);
    return documents;
  }

  /**
   * Gets the all documents by dataflow legacy.
   *
   * @param dataflowId the dataflow id
   * @return the all documents by dataflow legacy
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('ADMIN')")
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get list of dataflow documents from dataflow help",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DocumentVO.class,
      responseContainer = "List", hidden = true,
      notes = "Allowed roles: CUSTODIAN, STEWARD, OBSERVER, LEAD REPORTER, REPORTER WRITE, REPORTER READ, EDITOR READ, EDITOR WRITE, NATIONAL COORDINATOR, ADMIN")
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public List<DocumentVO> getAllDocumentsByDataflowLegacy(
      @PathVariable("dataflowId") Long dataflowId) {
    return this.getAllDocumentsByDataflow(dataflowId);
  }



  /**
   * Upload schema snapshot document.
   *
   * @param file the file
   * @param designDatasetId the design dataset id
   * @param fileName the file name
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/private/upload/{designDatasetId}/snapshot")
  @ApiOperation(value = "upload Schema snapshot Document", hidden = true)
  public void uploadSchemaSnapshotDocument(@RequestBody final byte[] file,
      @PathVariable("designDatasetId") final Long designDatasetId,
      @RequestParam("fileName") final String fileName) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    if (file == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
    }
    if (designDatasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      ByteArrayInputStream inStream = new ByteArrayInputStream(file);
      documentService.uploadSchemaSnapshot(inStream, "json", fileName, designDatasetId);
    } catch (EEAException | IOException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Gets the snapshot document.
   *
   * @param idDesignDataset the id design dataset
   * @param fileName the file name
   *
   * @return the snapshot document
   */
  @Override
  @GetMapping(value = "/private/{idDesignDataset}/snapshot")
  @HystrixCommand
  @Produces(value = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @ApiOperation(value = "Get Snapshot Document", hidden = true)
  public byte[] getSnapshotDocument(@PathVariable("idDesignDataset") final Long idDesignDataset,
      @RequestParam("fileName") final String fileName) {
    try {

      if (idDesignDataset == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DOCUMENT_NOT_FOUND);
      }
      FileResponse file = documentService.getSnapshotDocument(fileName, idDesignDataset);

      ByteArrayResource resource = new ByteArrayResource(file.getBytes());
      return resource.getByteArray();
    } catch (final EEAException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Delete snapshot schema document.
   *
   * @param idDesignDataset the id design dataset
   * @param fileName the file name
   *
   * @throws Exception the exception
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/private/{idDesignDataset}/snapshot")
  @ApiOperation(value = "Delete Snapshot Document", hidden = true)
  public void deleteSnapshotSchemaDocument(
      @PathVariable("idDesignDataset") final Long idDesignDataset,
      @RequestParam("fileName") final String fileName) throws Exception {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    try {
      if (idDesignDataset == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DOCUMENT_NOT_FOUND);
      }
      documentService.deleteSnapshotDocument(fileName, idDesignDataset);
    } catch (final EEAException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Upload collaboration document.
   *
   * @param file the file
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @param extension the extension
   * @param messageId the message id
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/private/upload/{dataflowId}/collaborationattachment")
  @ApiOperation(value = "upload Collaboration Document", hidden = true)
  public void uploadCollaborationDocument(@RequestBody final byte[] file,
      @PathVariable("dataflowId") final Long dataflowId,
      @RequestParam("fileName") final String fileName,
      @RequestParam("extension") final String extension,
      @RequestParam("messageId") final Long messageId) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    if (file == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
    }
    if (dataflowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    try {
      ByteArrayInputStream inStream = new ByteArrayInputStream(file);
      documentService.uploadCollaborationDocument(inStream, extension, fileName, dataflowId,
          messageId);
    } catch (EEAException | IOException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Delete collaboration document.
   *
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @param messageId the message id
   * @throws Exception the exception
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/private/{dataflowId}/collaborationattachment")
  @ApiOperation(value = "Delete Collaboration Document", hidden = true)
  public void deleteCollaborationDocument(@PathVariable("dataflowId") final Long dataflowId,
      @RequestParam("fileName") final String fileName,
      @RequestParam("messageId") final Long messageId) throws Exception {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    try {
      if (dataflowId == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            EEAErrorMessage.DATAFLOW_INCORRECT_ID);
      }
      documentService.deleteCollaborationDocument(fileName, dataflowId, messageId);
    } catch (final EEAException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Gets the collaboration document.
   *
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @param messageId the message id
   * @return the collaboration document
   */
  @Override
  @GetMapping(value = "/private/{dataflowId}/collaborationattachment")
  @HystrixCommand
  @Produces(value = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @ApiOperation(value = "Get Collaboration Document", hidden = true)
  public byte[] getCollaborationDocument(@PathVariable("dataflowId") final Long dataflowId,
      @RequestParam("fileName") final String fileName,
      @RequestParam("messageId") final Long messageId) {
    try {

      if (dataflowId == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DOCUMENT_NOT_FOUND);
      }
      FileResponse file = documentService.getCollaborationDocument(fileName, dataflowId, messageId);

      ByteArrayResource resource = new ByteArrayResource(file.getBytes());
      return resource.getByteArray();
    } catch (final EEAException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


}
