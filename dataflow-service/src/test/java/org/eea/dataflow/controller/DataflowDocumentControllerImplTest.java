package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.DataflowDocumentService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.document.DocumentVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataflowDocumentControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataflowDocumentControllerImplTest {

  /** The data flow document controller impl. */
  @InjectMocks
  DataflowDocumentControllerImpl dataflowDocumentControllerImpl;

  /** The dataflow service. */
  @Mock
  private DataflowDocumentService dataflowService;

  /** The document. */
  DocumentVO document;

  /** The dataflow VO. */
  DataFlowVO dataflowVO;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    document = new DocumentVO();
    dataflowVO = new DataFlowVO();
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Gets the document by id exception test.
   *
   * @return the document by id exception test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getDocumentByIdExceptionTest() throws EEAException {
    dataflowDocumentControllerImpl.getDocumentInfoById(null);
  }

  /**
   * Gets the document by id exception 2 test.
   *
   * @return the document by id exception 2 test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getDocumentByIdException2Test() throws EEAException {
    doThrow(new EEAException()).when(dataflowService).getDocumentInfoById(Mockito.any());
    dataflowDocumentControllerImpl.getDocumentInfoById(1L);
  }

  /**
   * Gets the document by id success test.
   *
   * @return the document by id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDocumentByIdSuccessTest() throws EEAException {
    DocumentVO document = new DocumentVO();
    document.setId(1L);
    when(dataflowService.getDocumentInfoById(Mockito.any())).thenReturn(document);
    assertEquals("fail", document, dataflowDocumentControllerImpl.getDocumentInfoById(1L));
  }

  /**
   * Update documentd exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDocumentExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(dataflowService).updateDocument(Mockito.any());
    try {
      dataflowDocumentControllerImpl.updateDocument(document);
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.DOCUMENT_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Update document success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDocumentSuccessTest() throws EEAException {
    dataflowDocumentControllerImpl.updateDocument(document);
    verify(dataflowService, times(1)).updateDocument(Mockito.any());
  }

  /**
   * Insert document exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertDocumentExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(dataflowService).insertDocument(Mockito.any());
    try {
      dataflowDocumentControllerImpl.insertDocument(document);
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.DOCUMENT_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Insert document success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertDocumentSuccessTest() throws EEAException {
    dataflowDocumentControllerImpl.insertDocument(document);
    verify(dataflowService, times(1)).insertDocument(Mockito.any());
  }

  /**
   * Delete document exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteDocumentExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(dataflowService).deleteDocument(Mockito.any());
    try {
      dataflowDocumentControllerImpl.deleteDocument(document.getId());
    } catch (ResponseStatusException e) {
      assertEquals("bad status", HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals("bad message", EEAErrorMessage.DOCUMENT_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Delete document success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteDocumentSuccessTest() throws EEAException {
    dataflowDocumentControllerImpl.deleteDocument(document.getId());
    verify(dataflowService, times(1)).deleteDocument(Mockito.any());
  }

  /**
   * Gets the all documents by dataflow id exception test.
   *
   * @return the all documents by dataflow id exception test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllDocumentsByDataflowIdExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(dataflowService).getAllDocumentsByDataflowId(Mockito.any());
    try {
      dataflowDocumentControllerImpl.getAllDocumentsByDataflowId(null);
    } catch (ResponseStatusException exception) {
      assertEquals("bad status", HttpStatus.BAD_REQUEST, exception.getStatus());
      assertEquals("bad message", EEAErrorMessage.DOCUMENT_NOT_FOUND, exception.getReason());
    }
  }

  /**
   * Gets the all documents by dataflow id success test.
   *
   * @return the all documents by dataflow id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllDocumentsByDataflowIdSuccessTest() throws EEAException {
    List<DocumentVO> documentsExpected = new ArrayList<>();
    DocumentVO documentVO = new DocumentVO();
    documentVO.setId(1L);
    documentsExpected.add(documentVO);
    dataflowVO.setDocuments(documentsExpected);
    when(dataflowService.getAllDocumentsByDataflowId(Mockito.anyLong()))
        .thenReturn(documentsExpected);
    assertEquals(documentsExpected, dataflowDocumentControllerImpl.getAllDocumentsByDataflowId(1L));
  }

}
