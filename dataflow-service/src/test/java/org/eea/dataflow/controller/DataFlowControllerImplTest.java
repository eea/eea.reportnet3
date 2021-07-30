package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  /** The representative service. */
  @Mock
  private RepresentativeService representativeService;


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
      dataFlowControllerImpl.findById(null, null);
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
    when(dataflowService.getByIdWithRepresentativesFilteredByUserEmail(Mockito.any()))
        .thenThrow(EEAException.class);
    dataFlowControllerImpl.findById(1L, null);
    assertEquals("fail", null, dataFlowControllerImpl.findById(1L, null));
  }

  /**
   * Test find by id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindById() throws EEAException {
    when(dataflowService.getByIdWithRepresentativesFilteredByUserEmail(Mockito.any()))
        .thenReturn(dataflowVO);
    dataFlowControllerImpl.findById(1L, null);
    assertEquals("fail", dataflowVO, dataFlowControllerImpl.findById(1L, null));
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
   * Find dataflows throws.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findDataflowsThrows() throws EEAException {
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    when(dataflowService.getDataflows(Mockito.any())).thenThrow(EEAException.class);
    dataFlowControllerImpl.findDataflows();
    Mockito.verify(dataflowService, times(1)).getDataflows(Mockito.any());
  }

  /**
   * Find dataflows.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findDataflows() throws EEAException {
    when(dataflowService.getDataflows(Mockito.any())).thenReturn(new ArrayList<>());
    dataFlowControllerImpl.findDataflows();
    assertEquals("fail", new ArrayList<>(), dataflowService.getDataflows(Mockito.any()));
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
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);
    when(dataflowService.getCompleted(Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
    dataFlowControllerImpl.findCompleted(1, 1);
    assertEquals("fail", new ArrayList<>(),
        dataflowService.getCompleted(Mockito.any(), Mockito.any()));
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
  public void createDataFlowDateThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setDeadlineDate(new Date(-1));
    ResponseEntity<?> value = dataFlowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATE_AFTER_INCORRECT, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
  }

  /**
   * Creates the data flow null throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createDataFlowNameThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    ResponseEntity<?> value = dataFlowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
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
    ResponseEntity<?> value = dataFlowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATE_AFTER_INCORRECT, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
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
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    Date date = sdf.parse("2914-09-15");
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    Mockito.when(dataflowService.createDataFlow(dataflowVO)).thenReturn(1L);
    ResponseEntity<?> value = dataFlowControllerImpl.createDataFlow(dataflowVO);
    assertEquals("1", value.getBody());
    assertEquals(HttpStatus.OK, value.getStatusCode());
  }

  /**
   * Creates the data flow throw.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void createDataFlowThrow() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    Date date = sdf.parse("2914-09-15");
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    doThrow(EEAException.class).when(dataflowService).createDataFlow(dataflowVO);
    ResponseEntity<?> value = dataFlowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, value.getStatusCode());
  }

  /**
   * Creates the data flow obligation null.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void createDataFlowObligationNull() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    ResponseEntity<?> value = dataFlowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATAFLOW_OBLIGATION, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
  }

  /**
   * Creates the data flow obligation id null.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void createDataFlowObligationIdNull() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(new ObligationVO());
    ResponseEntity<?> value = dataFlowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATAFLOW_OBLIGATION, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
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
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    doNothing().when(dataflowService).updateDataFlow(dataflowVO);
    dataFlowControllerImpl.updateDataFlow(dataflowVO);
    Mockito.verify(dataflowService, times(1)).updateDataFlow(dataflowVO);
  }


  /**
   * Update flow throw.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void updateFlowThrow() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    doThrow(EEAException.class).when(dataflowService).updateDataFlow(dataflowVO);
    ResponseEntity<?> value = dataFlowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, value.getStatusCode());
  }

  /**
   * Update flow throw.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void updateFlowObligationNull() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    ResponseEntity<?> value = dataFlowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
    assertEquals(EEAErrorMessage.DATAFLOW_OBLIGATION, value.getBody());
  }

  /**
   * Update flow obligation id null.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void updateFlowObligationIdNull() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    ObligationVO obligation = new ObligationVO();
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    ResponseEntity<?> value = dataFlowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
    assertEquals(EEAErrorMessage.DATAFLOW_OBLIGATION, value.getBody());
  }

  /**
   * Creates the data flow null throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDataFlowNullThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    ResponseEntity<?> value = dataFlowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
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
    ResponseEntity<?> result = dataFlowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATE_AFTER_INCORRECT, result.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
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
   * @throws Exception the exception
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
   * @throws Exception the exception
   */
  @Test
  public void deleteDataflow() throws Exception {
    dataFlowControllerImpl.deleteDataFlow(Mockito.anyLong());
    Mockito.verify(dataflowService, times(1)).deleteDataFlow(Mockito.anyLong());
  }

  /**
   * Test update status.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testUpdateStatus() throws EEAException {
    dataFlowControllerImpl.updateDataFlowStatus(Mockito.anyLong(), Mockito.any(), Mockito.any());
    Mockito.verify(dataflowService, times(1)).updateDataFlowStatus(Mockito.anyLong(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Test update status exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateStatusException() throws EEAException {
    try {
      doThrow(new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND)).when(dataflowService)
          .updateDataFlowStatus(Mockito.anyLong(), Mockito.any(), Mockito.any());
      dataFlowControllerImpl.updateDataFlowStatus(Mockito.anyLong(), Mockito.any(), Mockito.any());
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getReason());
      throw e;
    }
  }

  /**
   * Gets the public dataflows test.
   *
   * @return the public dataflows test
   */
  @Test
  public void getPublicDataflowsTest() {
    dataFlowControllerImpl.getPublicDataflows();
    Mockito.verify(dataflowService, times(1)).getPublicDataflows();
  }

  /**
   * Gets the public dataflow by id test.
   *
   * @return the public dataflow by id test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getPublicDataflowByIdTest() throws EEAException {
    dataFlowControllerImpl.getPublicDataflow(1L);
    Mockito.verify(dataflowService, times(1)).getPublicDataflowById(Mockito.any());
  }

  /**
   * Gets the public dataflow by id exception test.
   *
   * @return the public dataflow by id exception test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getPublicDataflowByIdExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(dataflowService).getPublicDataflowById(Mockito.anyLong());
    try {
      dataFlowControllerImpl.getPublicDataflow(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  @Test
  public void updateDataFlowPublicStatusTest() throws EEAException {
    dataFlowControllerImpl.updateDataFlowPublicStatus(1L, true);
    Mockito.verify(dataflowService, times(1)).updateDataFlowPublicStatus(Mockito.any(),
        Mockito.anyBoolean());
  }

  @Test
  public void getUserRolesAllDataflowsTest() {
    assertNotNull("is null", dataFlowControllerImpl.getUserRolesAllDataflows());
  }

  @Test
  public void getPublicDataflowsByCountry() {
    assertNull("assertion error",
        dataFlowControllerImpl.getPublicDataflowsByCountry("FR", 0, 10, "name", true));
  }

  @Test
  public void accessReferenceEntityTest() {
    assertFalse("reference not allowed",
        dataFlowControllerImpl.accessReferenceEntity(EntityClassEnum.DATASET, 1L));
  }

  @Test
  public void findReferenceDataflowsTest() throws EEAException {
    when(dataflowService.getReferenceDataflows(Mockito.any())).thenReturn(new ArrayList<>());
    dataFlowControllerImpl.findReferenceDataflows();
    assertEquals("fail", new ArrayList<>(), dataflowService.getReferenceDataflows(Mockito.any()));
  }

}
