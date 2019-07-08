package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataFlowControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataFlowControllerImplTest {

  /** The data flow controller impl. */
  @InjectMocks
  DataFlowControllerImpl dataFlowControllerImpl;

  /** The dataflow VO. */
  private DataFlowVO dataflowVO;

  /** The dataflow service. */
  @Mock
  private DataflowService dataflowService;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    dataflowVO = new DataFlowVO();
    dataflowVO.setId(1L);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test find by id data flow incorrect.
   */
  @Test(expected = ResponseStatusException.class)
  public void testFindByIdDataFlowIncorrect() {
    dataFlowControllerImpl.findById(null);
    Mockito.verify(dataFlowControllerImpl, times(1)).findById(Mockito.any());
  }


  /**
   * Test find by id EEA excep.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindByIdEEAExcep() throws EEAException {
    when(dataflowService.getById(Mockito.any())).thenThrow(EEAException.class);
    dataFlowControllerImpl.findById(1L);
    assertEquals("fail", null, dataFlowControllerImpl.findById(1L));
  }

  /**
   * Test find by id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindById() throws EEAException {
    when(dataflowService.getById(Mockito.any())).thenReturn(dataflowVO);
    dataFlowControllerImpl.findById(1L);
    assertEquals("fail", dataflowVO, dataFlowControllerImpl.findById(1L));
  }

  /**
   * Test error handler.
   */
  @Test
  public void testErrorHandler() {
    dataflowVO.setId(-1L);
    assertEquals("fail", dataflowVO, DataFlowControllerImpl.errorHandler(1L));
  }

  /**
   * Test error handler list.
   */
  @Test
  public void testErrorHandlerList() {
    dataflowVO.setId(-1L);
    List<DataFlowVO> dataflowVOs = new ArrayList<>();
    assertEquals("fail", dataflowVOs, DataFlowControllerImpl.errorHandlerList(1L));
  }

  /**
   * Test error handler list completed.
   */
  @Test
  public void testErrorHandlerListCompleted() {
    dataflowVO.setId(-1L);
    List<DataFlowVO> dataflowVOs = new ArrayList<>();
    assertEquals("fail", dataflowVOs, DataFlowControllerImpl.errorHandlerListCompleted(1L, 0, 10));
  }


  /**
   * Testfind by status throws.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testfindByStatusThrows() throws EEAException {
    when(dataflowService.getByStatus(Mockito.any())).thenThrow(EEAException.class);
    dataFlowControllerImpl.findByStatus(Mockito.any());
    Mockito.verify(dataflowService, times(1)).getByStatus(Mockito.any());
  }

  /**
   * Testfind by status.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testfindByStatus() throws EEAException {
    when(dataflowService.getByStatus(Mockito.any())).thenReturn(new ArrayList<>());
    dataFlowControllerImpl.findByStatus(Mockito.any());
    assertEquals("fail", new ArrayList<>(), dataflowService.getByStatus(Mockito.any()));
  }

  /**
   * Find pending accepted throws.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findPendingAcceptedThrows() throws EEAException {
    when(dataflowService.getPendingAccepted(Mockito.any())).thenThrow(EEAException.class);
    dataFlowControllerImpl.findPendingAccepted(Mockito.any());
    Mockito.verify(dataflowService, times(1)).getPendingAccepted(Mockito.any());
  }

  /**
   * Find pending accepted.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findPendingAccepted() throws EEAException {
    when(dataflowService.getPendingAccepted(Mockito.any())).thenReturn(new ArrayList<>());
    dataFlowControllerImpl.findPendingAccepted(Mockito.any());
    assertEquals("fail", new ArrayList<>(), dataflowService.getPendingAccepted(Mockito.any()));
  }

  /**
   * Find completed throws.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findCompletedThrows() throws EEAException {
    when(dataflowService.getCompleted(Mockito.any(), Mockito.any())).thenThrow(EEAException.class);
    dataFlowControllerImpl.findCompleted(1L, 1, 1);
    Mockito.verify(dataflowService, times(1)).getCompleted(Mockito.any(), Mockito.any());
  }

  /**
   * Find completed.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findCompleted() throws EEAException {
    when(dataflowService.getCompleted(Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
    dataFlowControllerImpl.findCompleted(1L, 1, 1);
    assertEquals("fail", new ArrayList<>(),
        dataflowService.getCompleted(Mockito.any(), Mockito.any()));
  }

  /**
   * Find user dataflows by status throws.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findUserDataflowsByStatusThrows() throws EEAException {
    when(dataflowService.getPendingByUser(Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    dataFlowControllerImpl.findUserDataflowsByStatus(Mockito.any(), Mockito.any());
    Mockito.verify(dataflowService, times(1)).getPendingByUser(Mockito.any(), Mockito.any());
  }

  /**
   * Find user dataflows by status.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findUserDataflowsByStatus() throws EEAException {
    when(dataflowService.getPendingByUser(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<>());
    dataFlowControllerImpl.findUserDataflowsByStatus(Mockito.any(), Mockito.any());
    assertEquals("fail", new ArrayList<>(), dataflowService.getPendingAccepted(Mockito.any()));
  }


  /**
   * Update user request.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateUserRequest() throws EEAException {
    Mockito.doNothing().when(dataflowService).updateUserRequestStatus(Mockito.any(), Mockito.any());

    dataFlowControllerImpl.updateUserRequest(Mockito.any(), Mockito.any());
    Mockito.verify(dataflowService, times(1)).updateUserRequestStatus(Mockito.any(), Mockito.any());
  }

  /**
   * Update user request throws.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateUserRequestThrows() throws EEAException {
    doThrow(new EEAException()).when(dataflowService).updateUserRequestStatus(Mockito.any(),
        Mockito.any());

    dataFlowControllerImpl.updateUserRequest(Mockito.any(), Mockito.any());
    Mockito.verify(dataflowService, times(1)).updateUserRequestStatus(Mockito.any(), Mockito.any());
  }

}
