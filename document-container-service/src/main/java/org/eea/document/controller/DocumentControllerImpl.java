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
import org.eea.interfaces.controller.dataflow.DataFlowDocumentController.DataFlowDocumentControllerZuul;
import org.eea.interfaces.controller.document.DocumentController;
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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;

/**
 * The type Document controller.
 */
@RestController
@RequestMapping("/document")
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE')")
  @HystrixCommand
  @PostMapping(value = "/upload/{dataflowId}")
  public void uploadDocument(@RequestPart("file") final MultipartFile file,
      @PathVariable("dataflowId") final Long dataflowId,
      @RequestParam("description") final String description,
      @RequestParam("language") final String language,
      @RequestParam("isPublic") final Boolean isPublic) {
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
   * Gets the document.
   *
   * @param documentId the document id
   *
   * @return the document
   */
  @Override
  @GetMapping(value = "/{documentId}", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @HystrixCommand
  public Resource getDocument(@PathVariable("documentId") final Long documentId) {
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
   * Delete document. You can delete metabase if you want , the boolean is to delete metabase by
   * your own
   *
   * @param documentId the document id
   * @param deleteMetabase the delete metabase
   *
   * @throws Exception the exception
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{documentId}")
  public void deleteDocument(@PathVariable("documentId") final Long documentId,
      @RequestParam(value = "deleteMetabase", required = false,
          defaultValue = "true") final Boolean deleteMetabase)
      throws Exception {
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
   * Update document.
   *
   * @param file the file
   * @param dataFlowId the data flow id
   * @param description the description
   * @param language the language
   * @param idDocument the id document
   * @param isPublic the is public
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/update/{idDocument}/dataflow/{dataFlowId}")
  public void updateDocument(@RequestPart(name = "file", required = false) final MultipartFile file,
      @PathVariable("dataFlowId") final Long dataFlowId,
      @RequestParam(name = "description", required = false) final String description,
      @RequestParam(name = "language", required = false) final String language,
      @PathVariable("idDocument") final Long idDocument,
      @RequestParam("isPublic") final Boolean isPublic) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    LOG.info("updateDocument");
    if (dataFlowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    try {
      DocumentVO documentVO = dataflowController.getDocumentInfoById(idDocument);
      documentVO.setDataflowId(dataFlowId);
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
   * Upload schema snapshot document.
   *
   * @param file the file
   * @param designDatasetId the design dataset id
   * @param fileName the file name
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/upload/{designDatasetId}/snapshot")
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
  @GetMapping(value = "/{idDesignDataset}/snapshot")
  @HystrixCommand
  @Produces(value = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
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
  @DeleteMapping(value = "/{idDesignDataset}/snapshot")
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
   * Gets the all documents by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the all documents by dataflow
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_REQUESTER','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('ADMIN')")
  @GetMapping(value = "/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find list of all documents by id of a Dataflow",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DocumentVO.class,
      responseContainer = "List")
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public List<DocumentVO> getAllDocumentsByDataflow(@PathVariable("dataflowId") Long dataflowId) {
    List<DocumentVO> documents = new ArrayList<>();
    documents = dataflowController.getAllDocumentsByDataflowId(dataflowId);
    return documents;
  }

}
