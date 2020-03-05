package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataFlowControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataFlowControllerImplTest {

  /**
   * The data flow controller impl.
   */
  @InjectMocks
  DataFlowControllerImpl dataFlowControllerImpl;

  /**
   * The dataflow VO.
   */
  private DataFlowVO dataflowVO;

  /**
   * The dataflow service.
   */
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
    try {
      dataFlowControllerImpl.findById(null);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_INCORRECT_ID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
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
    Map<String, String> details = new HashMap<>();
    details.put("userId", "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    when(dataflowService.getPendingAccepted(Mockito.any())).thenThrow(EEAException.class);
    dataFlowControllerImpl.findPendingAccepted();
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
    dataFlowControllerImpl.findPendingAccepted();
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
    dataFlowControllerImpl.findCompleted(1, 1);
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
    dataFlowControllerImpl.findCompleted(1, 1);
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
    Map<String, String> details = new HashMap<>();
    details.put("userId", "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    when(dataflowService.getPendingByUser(Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    dataFlowControllerImpl.findUserDataflowsByStatus(TypeRequestEnum.PENDING);
    Mockito.verify(dataflowService, times(1)).getPendingByUser(Mockito.any(), Mockito.any());
  }

  /**
   * Find user dataflows by status.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findUserDataflowsByStatus() throws EEAException {
    Map<String, String> details = new HashMap<>();
    details.put("userId", "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    when(dataflowService.getPendingByUser(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<>());
    dataFlowControllerImpl.findUserDataflowsByStatus(TypeRequestEnum.PENDING);
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
    try {
      dataFlowControllerImpl.updateUserRequest(Mockito.any(), Mockito.any());
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.USER_REQUEST_NOTFOUND, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Adds the contributor.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addContributor() throws EEAException {
    Mockito.doNothing().when(dataflowService).addContributorToDataflow(Mockito.any(),
        Mockito.any());

    dataFlowControllerImpl.addContributor(Mockito.any(), Mockito.any());
    Mockito.verify(dataflowService, times(1)).addContributorToDataflow(Mockito.any(),
        Mockito.any());
  }

  /**
   * Adds the contributor throws.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void addContributorThrows() throws EEAException {

    doThrow(new EEAException()).when(dataflowService).addContributorToDataflow(Mockito.any(),
        Mockito.any());


    try {
      dataFlowControllerImpl.addContributor(Mockito.any(), Mockito.any());
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.USER_REQUEST_NOTFOUND, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Removes the contributor.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void removeContributor() throws EEAException {
    Mockito.doNothing().when(dataflowService).removeContributorFromDataflow(Mockito.any(),
        Mockito.any());

    dataFlowControllerImpl.removeContributor(Mockito.any(), Mockito.any());
    Mockito.verify(dataflowService, times(1)).removeContributorFromDataflow(Mockito.any(),
        Mockito.any());
  }

  /**
   * Removes the contributor throws.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void removeContributorThrows() throws EEAException {
    doThrow(new EEAException()).when(dataflowService).removeContributorFromDataflow(Mockito.any(),
        Mockito.any());

    try {
      dataFlowControllerImpl.removeContributor(Mockito.any(), Mockito.any());
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.USER_REQUEST_NOTFOUND, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Creates the data flow throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createDataFlowThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setDeadlineDate(new Date(-1));
    dataFlowControllerImpl.createDataFlow(dataflowVO);
    Mockito.verify(dataflowService, times(1)).createDataFlow(dataflowVO);
  }

  /**
   * Creates the data flow null throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createDataFlowNullThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    try {
      dataFlowControllerImpl.createDataFlow(dataflowVO);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
  }

  /**
   * Creates the data flow date today throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createDataFlowDateTodayThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    Date date = new Date();
    date.setTime(date.getTime() - 1000L);
    dataflowVO.setDeadlineDate(date);
    try {
      dataFlowControllerImpl.createDataFlow(dataflowVO);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATE_AFTER_INCORRECT, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
  }

  /**
   * Creates the data throw repeat name.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void createDataThrowRepeatName() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    EEAException EEAException = new EEAException(EEAErrorMessage.DATAFLOW_EXISTS_NAME);
    doThrow(EEAException).when(dataflowService).createDataFlow(dataflowVO);
    try {
      dataFlowControllerImpl.createDataFlow(dataflowVO);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_EXISTS_NAME, ex.getReason());
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }
  }

  /**
   * Creates the data flow.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void createDataFlow() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    doNothing().when(dataflowService).createDataFlow(dataflowVO);
    dataFlowControllerImpl.createDataFlow(dataflowVO);
    Mockito.verify(dataflowService, times(1)).createDataFlow(dataflowVO);
  }

  /**
   * Creates the data flow.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void updateFlow() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    doNothing().when(dataflowService).updateDataFlow(dataflowVO);
    dataFlowControllerImpl.updateDataFlow(dataflowVO);
    Mockito.verify(dataflowService, times(1)).updateDataFlow(dataflowVO);
  }


  /**
   * Update flow throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFlowThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setDeadlineDate(new Date(-1));
    dataFlowControllerImpl.updateDataFlow(dataflowVO);
    Mockito.verify(dataflowService, times(1)).updateDataFlow(dataflowVO);
  }

  /**
   * Creates the data flow null throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataFlowNullThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    try {
      dataFlowControllerImpl.updateDataFlow(dataflowVO);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
  }

  /**
   * Creates the data flow date today throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataFlowDateTodayThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    Date date = new Date();
    date.setTime(date.getTime() - 1000L);
    dataflowVO.setDeadlineDate(date);
    try {
      dataFlowControllerImpl.updateDataFlow(dataflowVO);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATE_AFTER_INCORRECT, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
  }

  /**
   * Update data throw repeat name.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void updateDataThrowRepeatName() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    EEAException EEAException = new EEAException(EEAErrorMessage.DATAFLOW_EXISTS_NAME);
    doThrow(EEAException).when(dataflowService).updateDataFlow(dataflowVO);
    try {
      dataFlowControllerImpl.updateDataFlow(dataflowVO);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_EXISTS_NAME, ex.getReason());
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }
  }


  /**
   * Test get metabase by id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetMetabaseById() throws EEAException {
    when(dataflowService.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    dataFlowControllerImpl.getMetabaseById(1L);
    Mockito.verify(dataflowService, times(1)).getMetabaseById(1L);
  }

  /**
   * Test get metabase by id exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetMetabaseByIdException() throws EEAException {
    when(dataflowService.getMetabaseById(Mockito.any())).thenThrow(EEAException.class);
    dataFlowControllerImpl.getMetabaseById(1L);
    Mockito.verify(dataflowService, times(1)).getMetabaseById(1L);
  }

  /**
   * Test get metabase by id exception null.
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetMetabaseByIdExceptionNull() {
    try {
      dataFlowControllerImpl.getMetabaseById(null);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_INCORRECT_ID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Delete dataflow throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDataflowThrow() throws Exception {
    EEAException Exception = new EEAException(EEAErrorMessage.DATAFLOW_INCORRECT_ID);
    doThrow(Exception).when(dataflowService).deleteDataFlow(Mockito.anyLong());
    try {
      dataFlowControllerImpl.deleteDataFlow(Mockito.anyLong());
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_INCORRECT_ID, ex.getReason());
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Delete dataflow.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteDataflow() throws Exception {
    dataFlowControllerImpl.deleteDataFlow(Mockito.anyLong());
    Mockito.verify(dataflowService, times(1)).deleteDataFlow(Mockito.anyLong());
  }

  @Test
  public void testUpdateStatus() throws EEAException {
    dataFlowControllerImpl.updateDataFlowStatus(Mockito.anyLong(), Mockito.any());
    Mockito.verify(dataflowService, times(1)).updateDataFlowStatus(Mockito.anyLong(),
        Mockito.any());
  }


  @Test
  public void testUpdateStatusException() throws EEAException {
    try {
      doThrow(new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND)).when(dataflowService)
          .updateDataFlowStatus(Mockito.anyLong(), Mockito.any());
      dataFlowControllerImpl.updateDataFlowStatus(Mockito.anyLong(), Mockito.any());
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getReason());
    }

  }

}

