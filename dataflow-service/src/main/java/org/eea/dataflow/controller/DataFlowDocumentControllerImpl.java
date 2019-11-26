package org.eea.dataflow.controller;

import org.eea.dataflow.service.DataflowDocumentService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowDocumentController;
import org.eea.interfaces.vo.document.DocumentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


/**
 * The Class DataFlowDocumentControllerImpl.
 */
@RestController
@RequestMapping(value = "/dataflowDocument")
public class DataFlowDocumentControllerImpl implements DataFlowDocumentController {

  /**
   * The dataflow service.
   */
  @Autowired
  private DataflowDocumentService dataflowService;


  /**
   * Gets the document info by id.
   *
   * @param documentId the document id
   *
   * @return the document info by id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/document/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public DocumentVO getDocumentInfoById(Long documentId) {
    if (documentId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DOCUMENT_NOT_FOUND);
    }
    DocumentVO document = null;
    try {
      document = dataflowService.getDocumentInfoById(documentId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DOCUMENT_NOT_FOUND,
          e);
    }
    return document;
  }

  /**
   * Update user request.
   *
   * @param document the document
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/update")
  public void updateDocument(@RequestBody DocumentVO document) {
    try {
      dataflowService.updateDocument(document);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DOCUMENT_NOT_FOUND,
          e);
    }
  }

  /**
   * Insert document.
   *
   * @param document the document
   * @return the long
   */
  @Override
  @HystrixCommand
  @PostMapping
  public Long insertDocument(@RequestBody DocumentVO document) {
    try {
      return dataflowService.insertDocument(document);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DOCUMENT_NOT_FOUND,
          e);
    }
  }

  /**
   * Delete document.
   *
   * @param documentId the document id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{documentId}")
  public void deleteDocument(@PathVariable("documentId") Long documentId) {
    try {
      dataflowService.deleteDocument(documentId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DOCUMENT_NOT_FOUND,
          e);
    }
  }
}
