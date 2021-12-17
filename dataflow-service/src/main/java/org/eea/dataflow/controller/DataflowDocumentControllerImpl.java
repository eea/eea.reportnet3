package org.eea.dataflow.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.DataflowDocumentService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowDocumentController;
import org.eea.interfaces.vo.document.DocumentVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;


/**
 * The Class DataflowDocumentControllerImpl.
 */
@RestController
@RequestMapping(value = "/dataflowDocument")
@ApiIgnore
@Api(tags = "Documents : Documents Manager")
public class DataflowDocumentControllerImpl implements DataFlowDocumentController {

  /**
   * The dataflow service.
   */
  @Autowired
  private DataflowDocumentService dataflowService;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowDocumentControllerImpl.class);

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
  @ApiOperation(value = "Find a Document based on its Id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DocumentVO.class, hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 400, message = EEAErrorMessage.DOCUMENT_NOT_FOUND),
      @ApiResponse(code = 404, message = EEAErrorMessage.DOCUMENT_NOT_FOUND)})
  public DocumentVO getDocumentInfoById(
      @ApiParam(value = "Document id", example = "0") Long documentId) {
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
  @ApiOperation(value = "Update a Document", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.DOCUMENT_NOT_FOUND)
  public void updateDocument(
      @ApiParam(type = "Object", value = "Document") @RequestBody DocumentVO document) {
    LOG.info("updating document in controller");
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
   *
   * @return the long
   */
  @Override
  @HystrixCommand
  @PostMapping
  @ApiOperation(value = "Insert a Document", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.DOCUMENT_NOT_FOUND)
  public Long insertDocument(
      @ApiParam(type = "Object", value = "Document object") @RequestBody DocumentVO document) {
    LOG.info("inserting document in controller");
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
  @ApiOperation(value = "Delete a Document based on its Id", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.DOCUMENT_NOT_FOUND)
  public void deleteDocument(
      @ApiParam(value = "Document id", example = "0") @PathVariable("documentId") Long documentId) {
    try {
      dataflowService.deleteDocument(documentId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DOCUMENT_NOT_FOUND,
          e);
    }
  }

  /**
   * Gets the all documents by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the all documents by dataflow id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/{dataflowId}")
  @ApiOperation(value = "Get all documents for a specific dataflow Id", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.DOCUMENT_NOT_FOUND)
  public List<DocumentVO> getAllDocumentsByDataflowId(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    List<DocumentVO> documents = new ArrayList<>();
    try {
      documents = dataflowService.getAllDocumentsByDataflowId(dataflowId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DOCUMENT_NOT_FOUND,
          e);
    }
    return documents;
  }
}
