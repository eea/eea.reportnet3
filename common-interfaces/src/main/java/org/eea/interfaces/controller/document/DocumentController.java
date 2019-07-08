package org.eea.interfaces.controller.document;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
   */
  @PostMapping(value = "/upload/{dataFlowId}")
  void uploadDocument(final MultipartFile file, final Long dataFlowId);

  /**
   * Download document .
   *
   * @param documentName the document name
   * @param dataFlowId the data flow id
   * @return the document
   */
  @GetMapping
  ResponseEntity<Resource> getDocument(final String documentName, final Long dataFlowId);

}
