package org.eea.document.controller;

import java.io.IOException;
import javax.ws.rs.Produces;
import org.eea.document.service.DocumentService;
import org.eea.document.type.FileResponse;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.document.DocumentController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * The type Document controller.
 */
@RestController
@RequestMapping("/document")
public class DocumentControllerImpl implements DocumentController {

  /** The document service. */
  @Autowired
  private DocumentService documentService;

  /**
   * Upload document.
   *
   * @param file the file
   * @param dataFlowId the data flow id
   * @param description the description
   * @param language the language
   */
  @Override
  @PostMapping(value = "/upload/{dataFlowId}")
  public void uploadDocument(@RequestPart("file") final MultipartFile file,
      @PathVariable("dataFlowId") final Long dataFlowId, @RequestParam final String description,
      @RequestParam final String language) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
    }
    if (dataFlowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    }
    try {
      documentService.uploadDocument(file.getInputStream(), file.getContentType(),
          file.getOriginalFilename(), dataFlowId, language, description);
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
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @return the document
   */
  @Override
  @GetMapping
  @Produces(value = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
  public ResponseEntity<Resource> getDocument(
      @RequestParam("documentName") final String documentName,
      @RequestParam("dataFlowId") final Long dataFlowId,
      @RequestParam("language") final String language) {
    try {
      FileResponse file = documentService.getDocument(documentName, dataFlowId, language);
      HttpHeaders header = new HttpHeaders();
      header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + documentName);
      header.add("Cache-Control", "no-cache, no-store, must-revalidate");
      header.add("Pragma", "no-cache");
      header.add("Expires", "0");
      ByteArrayResource resource = new ByteArrayResource(file.getBytes());
      return ResponseEntity.ok().headers(header).contentLength(file.getBytes().length)
          .contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
    } catch (final EEAException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Delete a document.
   *
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @param language the language
   * @return the document
   * @throws Exception the exception
   */
  @Override
  @DeleteMapping
  public void deleteDocument(@RequestParam("documentName") final String documentName,
      @RequestParam("dataFlowId") final Long dataFlowId,
      @RequestParam("language") final String language) throws Exception {
    try {
      documentService.deleteDocument(documentName, dataFlowId, language);
    } catch (final EEAException e) {
      if (EEAErrorMessage.DOCUMENT_NOT_FOUND.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
