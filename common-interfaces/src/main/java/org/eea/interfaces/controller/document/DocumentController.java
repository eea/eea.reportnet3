package org.eea.interfaces.controller.document;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

/**
 * The interface Document controller.
 */
public interface DocumentController {

  /**
   * The interface Document controller zuul.
   */
  @FeignClient(value = "document", path = "/document")
  interface DocumentControllerZuul extends DocumentController {

  }

  /**
   * Upload document .
   *
   * @param file the file
   * @param dataFlowId the data flow id
   * @param description the description
   * @param language the language
   */
  @PostMapping(value = "/upload/{dataFlowId}")
  void uploadDocument(final MultipartFile file, final Long dataFlowId, final String description,
      final String language);

  /**
   * Download document .
   *
   * @param documentId the document id
   * @return the document
   */
  @GetMapping(value = "/{documentId}")
  ResponseEntity<Resource> getDocument(@PathVariable("documentId") final Long documentId);

  /**
   * Delete document.
   *
   * @param documentId the document id
   * @throws Exception the exception
   */
  @DeleteMapping(value = "/{documentId}")
  void deleteDocument(@PathVariable("documentId") final Long documentId) throws Exception;

}
