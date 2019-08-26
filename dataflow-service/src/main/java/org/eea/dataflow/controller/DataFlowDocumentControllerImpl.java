package org.eea.dataflow.controller;

import org.eea.dataflow.service.DataflowDocumentService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowDocumentController;
import org.eea.interfaces.vo.document.DocumentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


/**
 * The Class DataFlowDocumentControllerImpl.
 */
@RestController
@RequestMapping(value = "/dataflowDocument")
public class DataFlowDocumentControllerImpl implements DataFlowDocumentController {

  /** The dataflow service. */
  @Autowired
  private DataflowDocumentService dataflowService;


  /**
   * Gets the document info by id.
   *
   * @param documentId the document id
   * @return the document info by id
   */
  @Override
  public DocumentVO getDocumentInfoById(Long documentId) {
    if (documentId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DOCUMENT_NOT_FOUND);
    }
    DocumentVO document = null;
    try {
      document = dataflowService.getDocumentInfoById(documentId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DOCUMENT_NOT_FOUND);
    }
    return document;
  }

}
