package org.eea.interfaces.controller.dataflow;


import org.eea.interfaces.vo.document.DocumentVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * The Interface DataFlowDocumentController.
 */
public interface DataFlowDocumentController {


  /**
   * The Interface DataFlowDocumentControllerZuul.
   */
  @FeignClient(value = "dataflow", contextId = "dataflowDocument", path = "/dataflowDocument")
  interface DataFlowDocumentControllerZuul extends DataFlowDocumentController {

  }

  /**
   * Gets the document info by id.
   *
   * @param documentId the document id
   * @return the document by id
   */
  @GetMapping(value = "/document/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
  DocumentVO getDocumentInfoById(@PathVariable("documentId") Long documentId);

  /**
   * Updatedocument.
   *
   * @param document the document
   */
  @PutMapping(value = "/update")
  void updateDocument(@RequestBody DocumentVO document);

  /**
   * Insert document.
   *
   * @param documentVO the document VO
   * @return the long
   */
  @PostMapping
  Long insertDocument(@RequestBody DocumentVO documentVO);

  /**
   * Delete document.
   *
   * @param documentId the document id
   */
  @DeleteMapping(value = "/{documentId}")
  void deleteDocument(@PathVariable("documentId") Long documentId);


  // @GetMapping(value = "/{dataflowId}")
  // List<DocumentVO> getAllDocumentsByDataflowId(@PathVariable("dataflowId") Long dataflowId);
}
