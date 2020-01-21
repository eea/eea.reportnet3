package org.eea.interfaces.controller.document;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
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
   * @param dataflowId the dataflow id
   * @param description the description
   * @param language the language
   * @param isPublic the is public
   */
  @PostMapping(value = "/upload/{dataflowId}")
  void uploadDocument(final MultipartFile file, @PathVariable("dataflowId") final Long dataflowId,
      @RequestParam("description") final String description,
      @RequestParam("language") final String language,
      @RequestParam("isPublic") final Boolean isPublic);

  /**
   * Download document .
   *
   * @param documentId the document id
   * @return the document
   */
  @GetMapping(value = "/{documentId}")
  Resource getDocument(@PathVariable("documentId") final Long documentId);

  /**
   * Delete document. You can delete metabase if you want , the boolean is to delete metabase by
   * your own
   *
   * @param documentId the document id
   * @param deleteMetabase the delete metabase
   * @throws Exception the exception
   */
  @DeleteMapping(value = "/{documentId}")
  void deleteDocument(@PathVariable("documentId") final Long documentId,
      @RequestParam("deleteMetabase") final Boolean deleteMetabase) throws Exception;


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
  @PutMapping(value = "/update/{idDocument}/dataflow/{dataFlowId}")
  void updateDocument(@RequestPart(name = "file", required = false) final MultipartFile file,
      @PathVariable("dataFlowId") final Long dataFlowId,
      @RequestParam(name = "description", required = false) final String description,
      @RequestParam(name = "language", required = false) final String language,
      @PathVariable("idDocument") final Long idDocument,
      @RequestParam("isPublic") final Boolean isPublic);

  /**
   * Upload schema snapshot document.
   *
   * @param file the file
   * @param designDatasetId the design dataset id
   * @param fileName the file name
   */
  @PostMapping(value = "/upload/{designDatasetId}/snapshot")
  void uploadSchemaSnapshotDocument(@RequestBody final byte[] file,
      @PathVariable("designDatasetId") final Long designDatasetId,
      @RequestParam("fileName") final String fileName);

  /**
   * Gets the snapshot document.
   *
   * @param idDesignDataset the id design dataset
   * @param fileName the file name
   * @return the snapshot document
   */
  @GetMapping(value = "/{idDesignDataset}/snapshot")
  byte[] getSnapshotDocument(@PathVariable("idDesignDataset") final Long idDesignDataset,
      @RequestParam("fileName") final String fileName);


  /**
   * Delete snapshot schema document.
   *
   * @param idDesignDataset the id design dataset
   * @param fileName the file name
   * @throws Exception the exception
   */
  @DeleteMapping(value = "/{idDesignDataset}/snapshot")
  void deleteSnapshotSchemaDocument(@PathVariable("idDesignDataset") final Long idDesignDataset,
      @RequestParam("fileName") final String fileName) throws Exception;

}
