package org.eea.document.controller;

import javax.jcr.RepositoryException;
import org.eea.document.service.DocumentService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.document.DocumentController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
   * Test logging.
   *
   * @throws Exception the exception
   */
  @Override
  @GetMapping(value = "/testLog")
  public void testLogging() {
    try {
      documentService.testLogging();
    } catch (EEAException | RepositoryException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Upload document.
   *
   * @throws Exception the exception
   */
  @Override
  @GetMapping(value = "/create")
  public void uploadDocument() {
    try {
      documentService.uploadDocument();
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Gets the document.
   *
   * @return the document
   * @throws Exception the exception
   */
  @Override
  @GetMapping
  public void getDocument() {
    try {
      documentService.getDocument();
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
