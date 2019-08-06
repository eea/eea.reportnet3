package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.eea.dataflow.service.DataflowDocumentService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.document.DocumentVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataFlowDocumentControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataFlowDocumentControllerImplTest {

  /** The data flow document controller impl. */
  @InjectMocks
  DataFlowDocumentControllerImpl dataFlowDocumentControllerImpl;

  /** The dataflow service. */
  @Mock
  private DataflowDocumentService dataflowService;

  /**
   * Gets the document by id exception test.
   *
   * @return the document by id exception test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getDocumentByIdExceptionTest() throws EEAException {
    dataFlowDocumentControllerImpl.getDocumentInfoById(null);
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
    dataFlowDocumentControllerImpl.getDocumentInfoById(1L);
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
    assertEquals("fail", document, dataFlowDocumentControllerImpl.getDocumentInfoById(1L));
  }
}
