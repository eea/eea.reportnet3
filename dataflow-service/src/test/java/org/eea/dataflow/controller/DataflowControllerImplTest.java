package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.eea.dataflow.service.DataflowService;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.dataflow.service.file.DataflowHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataflowCountVO;
import org.eea.interfaces.vo.dataflow.DataflowPrivateVO;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataflow.PaginatedDataflowVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.lock.service.LockService;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataflowControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataflowControllerImplTest {

  /**
   * The data flow controller impl.
   */
  @InjectMocks
  DataflowControllerImpl dataflowControllerImpl;

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

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

  /** The details. */
  private Map<String, String> details;

  /** The lock service. */
  @Mock
  private LockService lockService;

  @Mock
  private DataflowHelper dataflowHelper;

  @Mock
  HttpServletResponse httpServletResponse;

  /** The notification controller zuul. */
  @Mock
  private NotificationControllerZuul notificationControllerZuul;

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    dataflowVO = new DataFlowVO();
    dataflowVO.setId(1L);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);


    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test find by id data flow incorrect.
   */
  @Test(expected = ResponseStatusException.class)
  public void testFindByIdDataFlowIncorrect() {
    try {
      dataflowControllerImpl.findById(null, null);
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
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    dataflowControllerImpl.findById(1L, null);
    assertEquals("fail", null, dataflowControllerImpl.findById(1L, null));
  }

  /**
   * Test find by id EEA excep 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindByIdEEAExcep2() throws EEAException {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    Mockito.when(dataflowService.getByIdWithRepresentativesFilteredByUserEmail(Mockito.anyLong(),
        Mockito.anyLong())).thenThrow(EEAException.class);

    dataflowControllerImpl.findById(1L, 1L);
    assertEquals("fail", null, dataflowControllerImpl.findById(1L, 1L));
  }

  /**
   * Test find by id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindById() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    doReturn(authorities).when(authentication).getAuthorities();
    when(dataflowService.getById(Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(dataflowVO);
    assertEquals("fail", dataflowVO, dataflowControllerImpl.findById(1L, null));
  }

  @Test
  public void testFindByIdObserver() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_OBSERVER.getAccessRole(1L)));
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    doReturn(authorities).when(authentication).getAuthorities();
    when(dataflowService.getById(Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(dataflowVO);
    assertEquals("fail", dataflowVO, dataflowControllerImpl.findById(1L, null));
  }

  @Test
  public void testFindByIdSteward() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_STEWARD.getAccessRole(1L)));
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    doReturn(authorities).when(authentication).getAuthorities();
    when(dataflowService.getById(Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(dataflowVO);
    assertEquals("fail", dataflowVO, dataflowControllerImpl.findById(1L, null));
  }

  @Test
  public void testFindByIdAdmin() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    doReturn(authorities).when(authentication).getAuthorities();
    when(dataflowService.getById(Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(dataflowVO);
    assertEquals("fail", dataflowVO, dataflowControllerImpl.findById(1L, null));
  }

  @Test
  public void testFindByIdIsNotUserRequester() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE"));
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    doReturn(authorities).when(authentication).getAuthorities();
    assertEquals("fail", null, dataflowControllerImpl.findById(1L, null));
  }

  /**
   * Test find by id legacy.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindByIdLegacy() throws EEAException {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    doReturn(authorities).when(authentication).getAuthorities();
    when(dataflowService.getById(Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(dataflowVO);
    assertEquals("fail", dataflowVO, dataflowControllerImpl.findByIdLegacy(1L, null));
  }

  /**
   * Testfind by status throws.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testfindByStatusThrows() throws EEAException {
    when(dataflowService.getByStatus(Mockito.any())).thenThrow(EEAException.class);
    dataflowControllerImpl.findByStatus(Mockito.any());
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
    dataflowControllerImpl.findByStatus(Mockito.any());
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

    when(dataflowService.getDataflows(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any(), Mockito.any())).thenThrow(EEAException.class);
    dataflowControllerImpl.findDataflows(null, null, false, null, null);
    Mockito.verify(dataflowService, times(1)).getDataflows(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any());
  }

  /**
   * Find dataflows.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findDataflows() throws EEAException {
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);
    PaginatedDataflowVO result = new PaginatedDataflowVO();
    when(dataflowService.getDataflows(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any(), Mockito.any())).thenReturn(result);
    assertEquals("fail", result,
        dataflowControllerImpl.findDataflows(null, null, false, null, null));
  }

  /**
   * Find completed throws.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findCompletedThrows() throws EEAException {
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    when(dataflowService.getCompleted(Mockito.any(), Mockito.any())).thenThrow(EEAException.class);
    dataflowControllerImpl.findCompleted(1, 1);
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
    dataflowControllerImpl.findCompleted(1, 1);
    assertEquals("fail", new ArrayList<>(),
        dataflowService.getCompleted(Mockito.any(), Mockito.any()));
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
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
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
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
  }

  @Test
  public void createDataFlowNotAdminThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setType(TypeDataflowEnum.BUSINESS);
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.UNAUTHORIZED, value.getBody());
    assertEquals(HttpStatus.UNAUTHORIZED, value.getStatusCode());
  }

  @Test
  public void createDataFlowAdminThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setType(TypeDataflowEnum.BUSINESS);
    Mockito.when(dataflowService.isAdmin()).thenReturn(true);
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
  }

  @Test
  public void createDataFlowReferenceThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setType(TypeDataflowEnum.REFERENCE);
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
  }

  @Test
  public void createDataFlowEqualDateThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setType(TypeDataflowEnum.REPORTING);
    Date date = new Date();
    date.setTime(date.getTime());
    dataflowVO.setDeadlineDate(date);
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATE_AFTER_INCORRECT, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
  }

  @Test
  public void createDataFlowDescriptionEmptyThrow() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setType(TypeDataflowEnum.REPORTING);
    dataflowVO.setName("name");
    // dataflowVO.setDescription("desc");
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
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
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
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
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
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
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
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
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
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
    ResponseEntity<?> value = dataflowControllerImpl.createDataFlow(dataflowVO);
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
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowService.isAdmin()).thenReturn(false);
    Mockito.when(dataflowService.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    doNothing().when(dataflowService).updateDataFlow(dataflowVO);
    dataflowControllerImpl.updateDataFlow(dataflowVO);
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
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowService.isAdmin()).thenReturn(false);
    Mockito.when(dataflowService.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    doThrow(EEAException.class).when(dataflowService).updateDataFlow(dataflowVO);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, value.getStatusCode());
  }

  /**
   * Update flow throw EEA exception.
   *
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  @Test
  public void updateFlowThrowEEAException() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowService.isAdmin()).thenReturn(false);
    Mockito.when(dataflowService.getMetabaseById(Mockito.any())).thenThrow(EEAException.class);
    doThrow(EEAException.class).when(dataflowService).updateDataFlow(dataflowVO);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
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
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowService.isAdmin()).thenReturn(false);
    Mockito.when(dataflowService.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
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
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowService.isAdmin()).thenReturn(false);
    Mockito.when(dataflowService.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
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
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
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
    ResponseEntity<?> result = dataflowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATE_AFTER_INCORRECT, result.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void testUpdateDataflowBusinessException() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setType(TypeDataflowEnum.BUSINESS);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    dataflowVO.setDataProviderGroupId(1L);
    dataflowVO.setId(1L);
    DataFlowVO dataFlowVO2 = new DataFlowVO();
    dataFlowVO2.setId(1L);
    dataFlowVO2.setDataProviderGroupId(2L);
    List<RepresentativeVO> representatives = new ArrayList<>();
    representatives.add(new RepresentativeVO());

    Mockito.when(dataflowService.getMetabaseById(Mockito.anyLong())).thenReturn(dataFlowVO2);
    Mockito.when(representativeService.getRepresetativesByIdDataFlow(Mockito.anyLong()))
        .thenReturn(representatives);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
    assertEquals(EEAErrorMessage.EXISTING_REPRESENTATIVES, value.getBody());
  }

  @Test
  public void updateDataFlowReferenceTest() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    dataflowVO.setType(TypeDataflowEnum.REFERENCE);
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowService.isAdmin()).thenReturn(false);
    Mockito.when(dataflowService.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    doNothing().when(dataflowService).updateDataFlow(dataflowVO);
    dataflowControllerImpl.updateDataFlow(dataflowVO);
    Mockito.verify(dataflowService, times(1)).updateDataFlow(dataflowVO);
  }

  @Test
  public void updateFlowEqualDateTest() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setType(TypeDataflowEnum.BUSINESS);
    Date date = new Date();
    date.setTime(date.getTime());
    dataflowVO.setDeadlineDate(date);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
    assertEquals(EEAErrorMessage.DATE_AFTER_INCORRECT, value.getBody());
  }

  @Test
  public void updateDataFlowDescriptionIsBlankTest() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    dataflowVO.setType(TypeDataflowEnum.BUSINESS);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
    assertEquals(EEAErrorMessage.DATAFLOW_DESCRIPTION_NAME, value.getBody());
  }

  @Test
  public void updateDataFlowSameGroupIdTest() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setType(TypeDataflowEnum.BUSINESS);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    dataflowVO.setDataProviderGroupId(1L);
    dataflowVO.setId(1L);
    DataFlowVO dataFlowVO2 = new DataFlowVO();
    dataFlowVO2.setId(1L);
    dataFlowVO2.setDataProviderGroupId(1L);
    List<RepresentativeVO> representatives = new ArrayList<>();
    representatives.add(new RepresentativeVO());
    Mockito.when(dataflowService.isAdmin()).thenReturn(true);
    Mockito.when(dataflowService.getMetabaseById(Mockito.anyLong())).thenReturn(dataFlowVO2);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.OK, value.getStatusCode());
    assertEquals("", value.getBody());
  }

  @Test
  public void updateDataFlowEmptyRepresentativesTest() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setType(TypeDataflowEnum.BUSINESS);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    dataflowVO.setDataProviderGroupId(1L);
    dataflowVO.setId(1L);
    DataFlowVO dataFlowVO2 = new DataFlowVO();
    dataFlowVO2.setId(1L);
    dataFlowVO2.setDataProviderGroupId(2L);
    List<RepresentativeVO> representatives = new ArrayList<>();
    Mockito.when(dataflowService.isAdmin()).thenReturn(true);
    Mockito.when(dataflowService.getMetabaseById(Mockito.anyLong())).thenReturn(dataFlowVO2);
    Mockito.when(representativeService.getRepresetativesByIdDataFlow(Mockito.anyLong()))
        .thenReturn(representatives);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.OK, value.getStatusCode());
    assertEquals("", value.getBody());
  }

  @Test
  public void updateDataFlowEEAExceptionTest() throws EEAException, ParseException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setType(TypeDataflowEnum.BUSINESS);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    dataflowVO.setDataProviderGroupId(1L);
    dataflowVO.setId(1L);
    DataFlowVO dataFlowVO2 = new DataFlowVO();
    dataFlowVO2.setId(1L);
    dataFlowVO2.setDataProviderGroupId(2L);
    List<RepresentativeVO> representatives = new ArrayList<>();
    Mockito.when(dataflowService.isAdmin()).thenReturn(true);
    Mockito.when(dataflowService.getMetabaseById(Mockito.anyLong())).thenReturn(dataFlowVO2);
    Mockito.when(representativeService.getRepresetativesByIdDataFlow(Mockito.anyLong()))
        .thenThrow(EEAException.class);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.OK, value.getStatusCode());
    assertEquals("", value.getBody());
  }

  /**
   * Test get metabase by id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetMetabaseById() throws EEAException {
    when(dataflowService.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    dataflowControllerImpl.getMetabaseById(1L);
    Mockito.verify(dataflowService, times(1)).getMetabaseById(Mockito.anyLong());
  }

  /**
   * Test get metabase by id legacy.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetMetabaseByIdLegacy() throws EEAException {
    when(dataflowService.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    dataflowControllerImpl.getMetabaseByIdLegacy(1L);
    Mockito.verify(dataflowService, times(1)).getMetabaseById(Mockito.anyLong());
  }


  /**
   * Test get metabase by id exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetMetabaseByIdException() throws EEAException {
    when(dataflowService.getMetabaseById(Mockito.any())).thenThrow(EEAException.class);
    dataflowControllerImpl.getMetabaseById(1L);
    Mockito.verify(dataflowService, times(1)).getMetabaseById(Mockito.anyLong());
  }

  /**
   * Test get metabase by id exception null.
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetMetabaseByIdExceptionNull() {
    try {
      dataflowControllerImpl.getMetabaseById(null);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATAFLOW_INCORRECT_ID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }


  /**
   * Delete dataflow.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteDataflow() {

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
    dataflowControllerImpl.deleteDataFlow(1L);
    Mockito.verify(dataflowService, times(1)).deleteDataFlow(Mockito.anyLong());
  }


  /**
   * Test delete dataflow exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteDataflowException() throws EEAException {
    try {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("name");
      doThrow(new ResponseStatusException(HttpStatus.LOCKED)).when(dataflowService)
          .deleteDataFlow(Mockito.anyLong());
      dataflowControllerImpl.deleteDataFlow(1L);

    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.LOCKED, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void testDeleteDataflowEEAExceptionTest() throws EEAException {
    try {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("name");
      doThrow(new ResponseStatusException(HttpStatus.LOCKED)).when(dataflowService)
          .deleteDataFlow(Mockito.anyLong());
      Mockito.when(dataflowService.getMetabaseById(1L)).thenThrow(EEAException.class);
      dataflowControllerImpl.deleteDataFlow(1L);

    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.LOCKED, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteDataflowImportLockNullTest() {
    LockVO importLockVO = new LockVO();
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
    Mockito.when(lockService.findByCriteria(Mockito.anyMap())).thenReturn(importLockVO);
    try {
      dataflowControllerImpl.deleteDataFlow(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.LOCKED, e.getStatus());
      assertEquals("Dataflow is locked because import is in progress.", e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteDataflowCloneLockNullTest() {
    LockVO cloneLockVO = new LockVO();
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
    Mockito.when(lockService.findByCriteria(Mockito.anyMap())).thenReturn(null)
        .thenReturn(cloneLockVO);
    try {
      dataflowControllerImpl.deleteDataFlow(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.LOCKED, e.getStatus());
      assertEquals("Dataflow is locked because clone is in progress.", e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteDataflowForbiddenTest() throws EEAException {
    DataFlowVO dfVO = new DataFlowVO();
    dfVO.setType(TypeDataflowEnum.BUSINESS);
    Mockito.when(dataflowService.getMetabaseById(Mockito.anyLong())).thenReturn(dfVO);
    Mockito.when(dataflowService.isAdmin()).thenReturn(false);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");
    try {
      dataflowControllerImpl.deleteDataFlow(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      assertEquals("Can't delete a Business Dataflow without being an admin user.", e.getReason());
      throw e;
    }
  }

  @Test
  public void deleteDataflowNotForbiddenTest() throws EEAException {
    DataFlowVO dfVO = new DataFlowVO();
    dfVO.setType(TypeDataflowEnum.REPORTING);
    Mockito.when(dataflowService.getMetabaseById(Mockito.anyLong())).thenReturn(dfVO);
    Mockito.when(dataflowService.isAdmin()).thenReturn(false);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");

    dataflowControllerImpl.deleteDataFlow(1L);
    Mockito.verify(dataflowService, times(1)).deleteDataFlow(Mockito.anyLong());
  }

  @Test
  public void deleteDataflowIsAdminTrueTest() throws EEAException {
    Mockito.when(dataflowService.isAdmin()).thenReturn(true);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("name");

    dataflowControllerImpl.deleteDataFlow(1L);
    Mockito.verify(dataflowService, times(1)).deleteDataFlow(Mockito.anyLong());
  }

  /**
   * Test update status.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testUpdateStatus() throws EEAException {
    dataflowControllerImpl.updateDataFlowStatus(Mockito.anyLong(), Mockito.any(), Mockito.any());
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
      dataflowControllerImpl.updateDataFlowStatus(Mockito.anyLong(), Mockito.any(), Mockito.any());
    } catch (ResponseStatusException e) {
      assertEquals("Couldn't update the dataflow status. An unknown error happenned.",
          e.getReason());
      throw e;
    }
  }

  /**
   * Gets the public dataflows test.
   *
   * @return the public dataflows test
   * @throws EEAException
   */
  @Test
  public void getPublicDataflowsTest() throws EEAException {
    dataflowControllerImpl.getPublicDataflows(null, null, false, null, null);
    Mockito.verify(dataflowService, times(1)).getPublicDataflows(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any(), Mockito.any());
  }

  /**
   * Gets the public dataflow by id test.
   *
   * @return the public dataflow by id test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getPublicDataflowByIdTest() throws EEAException {
    dataflowControllerImpl.getPublicDataflow(1L);
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
      dataflowControllerImpl.getPublicDataflow(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  @Test
  public void updateDataFlowPublicStatusTest() throws EEAException {
    dataflowControllerImpl.updateDataFlowPublicStatus(1L, true);
    Mockito.verify(dataflowService, times(1)).updateDataFlowPublicStatus(Mockito.any(),
        Mockito.anyBoolean());
  }

  @Test
  public void getUserRolesAllDataflowsTest() throws EEAException {
    List<Long> dataProviderIds = new ArrayList<>();
    dataProviderIds.add(1L);
    dataProviderIds.add(2L);
    Mockito.when(representativeService.getProviderIds()).thenReturn(dataProviderIds);
    assertNotNull("is null", dataflowControllerImpl.getUserRolesAllDataflows());
  }

  @Test(expected = ResponseStatusException.class)
  public void getUserRolesAllDataflowsEEAExceptionTest() throws EEAException {
    Mockito.when(representativeService.getProviderIds()).thenThrow(EEAException.class);
    try {
      dataflowControllerImpl.getUserRolesAllDataflows();
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
      assertEquals(EEAErrorMessage.UNAUTHORIZED, e.getReason());
      throw e;
    }
  }

  @Test
  public void getPublicDataflowsByCountry() {
    assertNull("assertion error",
        dataflowControllerImpl.getPublicDataflowsByCountry("FR", 0, 10, "name", true, null));
  }

  /**
   * Gets the public dataflows by country EEA exception.
   *
   * @return the public dataflows by country EEA exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getPublicDataflowsByCountryEEAException() throws EEAException {
    Mockito.when(dataflowService.getPublicDataflowsByCountry("FR", "name", true, 0, 10, null))
        .thenThrow(EEAException.class);
    try {
      dataflowControllerImpl.getPublicDataflowsByCountry("FR", 0, 10, "name", true, null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.DATAFLOW_GET_ERROR, e.getReason());
      throw e;
    }
  }

  @Test
  public void accessReferenceEntityTest() {
    assertFalse("reference not allowed",
        dataflowControllerImpl.accessReferenceEntity(EntityClassEnum.DATASET, 1L));
  }

  @Test
  public void findReferenceDataflowsTest() throws EEAException {
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);
    PaginatedDataflowVO result = new PaginatedDataflowVO();
    when(dataflowService.getDataflows(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any(), Mockito.any())).thenReturn(result);
    when(authentication.getDetails()).thenReturn(details);
    assertEquals("fail", result,
        dataflowControllerImpl.findReferenceDataflows(null, null, false, null, null));

  }

  @Test
  public void findBusinessDataflowsTest() throws EEAException {

    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);
    PaginatedDataflowVO result = new PaginatedDataflowVO();
    when(authentication.getDetails()).thenReturn(details);
    when(dataflowService.getDataflows(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any(), Mockito.any())).thenReturn(result);
    assertEquals("fail", result,
        dataflowControllerImpl.findBusinessDataflows(null, null, false, null, null));
  }

  @Test
  public void findBusinessDataflowsExceptionTest() throws EEAException {

    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    when(authentication.getDetails()).thenReturn(details);

    doThrow(new EEAException()).when(dataflowService).getDataflows(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any());
    assertNotNull("fail",
        dataflowControllerImpl.findBusinessDataflows(null, null, false, null, null));
  }


  @Test
  public void findCitizenScienceDataflowsTest() throws EEAException {

    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);
    PaginatedDataflowVO result = new PaginatedDataflowVO();
    when(authentication.getDetails()).thenReturn(details);
    when(dataflowService.getDataflows(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any(), Mockito.any())).thenReturn(result);
    assertEquals("fail", result,
        dataflowControllerImpl.findCitizenScienceDataflows(null, null, false, null, null));
  }

  @Test
  public void findCitizenScienceDataflowsExceptionTest() throws EEAException {

    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    when(authentication.getDetails()).thenReturn(details);

    doThrow(new EEAException()).when(dataflowService).getDataflows(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any());
    assertNotNull("fail",
        dataflowControllerImpl.findCitizenScienceDataflows(details, null, false, null, null));
  }

  @Test
  public void findDataflowsForCloneTest() throws EEAException {

    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    when(authentication.getDetails()).thenReturn(details);
    when(dataflowService.getCloneableDataflows(Mockito.any())).thenReturn(new ArrayList<>());
    assertEquals("fail", new ArrayList<>(), dataflowControllerImpl.findCloneableDataflows());
  }

  @Test
  public void findDataflowsForCloneExceptionTest() throws EEAException {

    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    when(authentication.getDetails()).thenReturn(details);

    doThrow(new EEAException()).when(dataflowService).getCloneableDataflows(Mockito.anyString());
    assertEquals("fail", new ArrayList<>(), dataflowControllerImpl.findCloneableDataflows());
  }

  @Test
  public void accessEntityTest() {
    assertFalse("reference not allowed", dataflowControllerImpl
        .accessEntity(TypeDataflowEnum.BUSINESS, EntityClassEnum.DATASET, 1L));
  }

  @Test
  public void getDatasetSummaryByDataflowIdTest() throws EEAException {
    List<DatasetsSummaryVO> datasetsSummary = new ArrayList<>();
    Mockito.when(dataflowService.getDatasetSummary(Mockito.any())).thenReturn(datasetsSummary);
    assertEquals(new ArrayList<>(), dataflowControllerImpl.getDatasetSummaryByDataflowId(1L));
  }

  @Test
  public void getDatasetSummaryByDataflowIdErrorTest() throws EEAException {
    doThrow(new EEAException()).when(dataflowService).getDatasetSummary(Mockito.anyLong());
    dataflowControllerImpl.getDatasetSummaryByDataflowId(1L);
    Mockito.verify(dataflowService, times(1)).getDatasetSummary(Mockito.anyLong());
  }

  @Test
  public void exportSchemaInformationTest() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());
    Mockito.doNothing().when(dataflowHelper).exportSchemaInformation(1L);
    dataflowControllerImpl.exportSchemaInformation(1L);
    Mockito.verify(dataflowHelper, times(1)).exportSchemaInformation(Mockito.anyLong());
  }

  @Test
  public void exportSchemaInformationEEAExceptionTest() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());
    Mockito.doThrow(EEAException.class).when(dataflowHelper).exportSchemaInformation(1L);
    dataflowControllerImpl.exportSchemaInformation(1L);
    Mockito.verify(dataflowHelper, times(1)).exportSchemaInformation(Mockito.anyLong());
  }

  @Test(expected = ResponseStatusException.class)
  public void downloadSchemaInformationIOExceptionTest() throws EEAException, IOException {
    Mockito.when(dataflowHelper.downloadSchemaInformation(Mockito.any(), Mockito.any()))
        .thenReturn(new File(""));
    try {
      dataflowControllerImpl.downloadSchemaInformation(0L, "", httpServletResponse);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  @Test
  public void downloadSchemaInformationTest() throws EEAException, IOException {
    File file = folder.newFile("filename.txt");

    ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);

    Mockito.when(dataflowHelper.downloadSchemaInformation(Mockito.any(), Mockito.any()))
        .thenReturn(file);
    Mockito.when(httpServletResponse.getOutputStream()).thenReturn(outputStream);
    Mockito.doNothing().when(outputStream).close();

    dataflowControllerImpl.downloadSchemaInformation(0L, "", httpServletResponse);
    Mockito.verify(outputStream, times(1)).close();
  }

  @Test
  public void downloadPublicSchemaInformationTest() throws EEAException, IOException {
    dataflowControllerImpl.downloadPublicSchemaInformation(1L);
    Mockito.verify(dataflowHelper, times(1)).downloadPublicSchemaInformation(Mockito.anyLong());
  }

  @Test(expected = ResponseStatusException.class)
  public void downloadPublicSchemaInformationEEAExceptionTest() throws EEAException, IOException {
    Mockito.when(dataflowService.getPublicDataflowById(1L)).thenThrow(EEAException.class);
    try {
      dataflowControllerImpl.downloadPublicSchemaInformation(1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void downloadPublicSchemaInformationIOExceptionTest() throws EEAException, IOException {
    Mockito.when(dataflowHelper.downloadPublicSchemaInformation(1L)).thenThrow(IOException.class);
    try {
      dataflowControllerImpl.downloadPublicSchemaInformation(1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  @Test
  public void getPrivateDataflowByIdTest() throws EEAException {
    DataflowPrivateVO dataflowPrivateVO = new DataflowPrivateVO();
    dataflowPrivateVO.setId(1L);
    Mockito.when(dataflowService.getPrivateDataflowById(Mockito.anyLong()))
        .thenReturn(dataflowPrivateVO);
    assertEquals(dataflowPrivateVO, dataflowControllerImpl.getPrivateDataflowById(1L));
  }

  @Test
  public void getPrivateDataflowByIdExceptionTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(dataflowService)
        .getPrivateDataflowById(Mockito.anyLong());
    dataflowControllerImpl.getPrivateDataflowById(1L);
    Mockito.verify(dataflowService, times(1)).getPrivateDataflowById(Mockito.anyLong());
  }

  @Test
  public void getDataflowsCountTest() throws EEAException {
    List<DataflowCountVO> dataflows = new ArrayList<>();
    DataflowCountVO dataflowCountVO = new DataflowCountVO();
    dataflowCountVO.setAmount(1L);
    dataflowCountVO.setType(TypeDataflowEnum.REFERENCE);
    dataflows.add(dataflowCountVO);
    Mockito.when(dataflowService.getDataflowsCount()).thenReturn(dataflows);
    assertEquals(dataflows, dataflowControllerImpl.getDataflowsCount());
  }

  @Test
  public void getDataflowsCountExceptionTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(dataflowService).getDataflowsCount();
    dataflowControllerImpl.getDataflowsCount();
    Mockito.verify(dataflowService, times(1)).getDataflowsCount();
  }

  @Test(expected = ResponseStatusException.class)
  public void getPublicDataflowNotFoundExceptionTest() throws EEAException {
    try {
      Mockito.doThrow(new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND)).when(dataflowService)
          .getPublicDataflowById(Mockito.anyLong());
      dataflowControllerImpl.getPublicDataflow(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void getPublicDataflowsException() throws EEAException {
    try {
      Mockito.doThrow(new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND)).when(dataflowService)
          .getPublicDataflows(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(),
              Mockito.any());
      dataflowControllerImpl.getPublicDataflows(null, null, false, null, null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  @Test
  public void findReferenceDataflowsExceptionTest() throws EEAException {
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "1");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);
    Mockito.doThrow(EEAException.class).when(dataflowService).getDataflows(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(),
        Mockito.any());
    dataflowControllerImpl.findReferenceDataflows(null, null, false, null, null);
    Mockito.verify(dataflowService, times(1)).getDataflows(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any());
  }

  @Test
  public void validateAllReportersTest() throws EEAException {
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    dataflowControllerImpl.validateAllReporters();
    Mockito.verify(dataflowService, times(1)).validateAllReporters("userId");
  }

  @Test
  public void validateAllReportersEEAExceptionTest() throws EEAException {
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId");
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(details);
    SecurityContextHolder.setContext(securityContext);

    Mockito.doThrow(EEAException.class).when(dataflowService).validateAllReporters("userId");

    ResponseEntity<?> value = dataflowControllerImpl.validateAllReporters();
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
    assertEquals(
        "Couldn't validate all reporters and lead reporters, an error was produced during the process.",
        value.getBody());
  }

  @Test
  public void updateDataflowDataflowNameEmptyTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    Mockito.when(dataflowService.isAdmin()).thenReturn(true);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(EEAErrorMessage.DATAFLOW_NAME_EMPTY, value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
  }

  @Test
  public void updatDataflowCitizenScienceUpdateErrorTest() throws ParseException, EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse("2914-09-15");
    ObligationVO obligation = new ObligationVO();
    obligation.setObligationId(1);
    dataflowVO.setDeadlineDate(date);
    dataflowVO.setDescription("description");
    dataflowVO.setName("name");
    dataflowVO.setObligation(obligation);
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataflowVO.setType(TypeDataflowEnum.CITIZEN_SCIENCE);
    dataflowVO.setReleasable(false);
    dataflowVO.setShowPublicInfo(false);
    Mockito.when(dataflowService.isAdmin()).thenReturn(false);
    Mockito.when(dataflowService.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    ResponseEntity<?> value = dataflowControllerImpl.updateDataFlow(dataflowVO);
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
    assertEquals(EEAErrorMessage.DATAFLOW_UPDATE_ERROR, value.getBody());
  }

  /**
   * Update data flow automatic reporting deletion test.
   */
  @Test
  public void updateDataFlowAutomaticReportingDeletionTest() {
    dataflowControllerImpl.updateDataFlowAutomaticReportingDeletion(1L, true);
  }

  /**
   * Update data flow automatic reporting deletion throw response status exception test.
   */
  @Test(expected = ResponseStatusException.class)
  public void updateDataFlowAutomaticReportingDeletionThrowResponseStatusExceptionTest() {
    Mockito.doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
        .when(dataflowService).updateDataFlowAutomaticReportingDeletion(1L, true);
    try {
      dataflowControllerImpl.updateDataFlowAutomaticReportingDeletion(1L, true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals(
          "Couldn't update the dataflow automatic delete data and snapshots. An unknown error happenned.",
          e.getReason());
      throw e;
    }
  }

  /**
   * Gets the dataflows metabase by id test.
   *
   * @return the dataflows metabase by id test
   */
  @Test
  public void getDataflowsMetabaseByIdTest() {
    dataflowControllerImpl.getDataflowsMetabaseById(Mockito.anyList());

    Mockito.verify(dataflowService, times(1)).getDataflowsMetabaseById(Mockito.anyList());
  }

  /**
   * Gets the dataflows metabase by id throw response status exception test.
   *
   * @return the dataflows metabase by id throw response status exception test
   */
  @Test(expected = ResponseStatusException.class)
  public void getDataflowsMetabaseByIdThrowResponseStatusExceptionTest() {
    try {
      dataflowControllerImpl.getDataflowsMetabaseById(null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATAFLOW_INCORRECT_ID, e.getReason());
      throw e;
    }
  }
}
