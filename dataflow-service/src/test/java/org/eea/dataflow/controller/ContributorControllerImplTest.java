package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import java.util.Collection;
import java.util.HashSet;
import org.eea.dataflow.service.ContributorService;
import org.eea.dataflow.service.impl.DataflowServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class ContributorControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContributorControllerImplTest {

  /**
   * The data flow controller impl.
   */
  @InjectMocks
  ContributorControllerImpl contributorControllerImpl;

  /** The contributor service. */
  @Mock
  private ContributorService contributorService;

  /** The user management controller zull. */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /** The dataflow service. */
  @Mock
  private DataflowServiceImpl dataflowService;

  /** The contributor VO write. */
  private ContributorVO contributorVOWrite;

  /** The contributor VO read. */
  private ContributorVO contributorVORead;

  /** The user representation VO. */
  private UserRepresentationVO userRepresentationVO;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    contributorVOWrite = new ContributorVO();
    contributorVOWrite.setAccount("write@reportnet.net");
    contributorVOWrite.setRole("EDITOR");

    contributorVORead = new ContributorVO();
    contributorVORead.setAccount("read@reportnet.net");
    contributorVORead.setRole("EDITOR");

    userRepresentationVO = new UserRepresentationVO();
    userRepresentationVO.setEmail("write@reportnet.net");
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Update contributor.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateContributor() throws EEAException {
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    contributorControllerImpl.updateRequester(1L, contributorVOWrite);
    Mockito.verify(contributorService, times(1)).updateContributor(1L, contributorVOWrite, null);
  }

  /**
   * Update contributo throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateContributoThrow() throws EEAException {
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    Mockito.doThrow(EEAException.class).when(contributorService).updateContributor(1L,
        contributorVOWrite, null);
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    ResponseEntity<?> value = contributorControllerImpl.updateRequester(1L, contributorVOWrite);
    assertEquals(null, value.getBody());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, value.getStatusCode());
  }

  @Test(expected = ResponseStatusException.class)
  public void updateContributorEmailNullTest() throws EEAException {
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);

    try {
      contributorControllerImpl.updateRequester(1L, contributorVOWrite);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
      assertEquals("The email write@reportnet.net doesn't exist in repornet", ex.getReason());
      throw ex;
    }
  }

  @Test
  public void updateRequesterEEAExceptionTest() throws EEAException {

    Mockito.doThrow(new EEAException("404 NOT_FOUND")).when(contributorService)
        .updateContributor(1L, contributorVOWrite, null);
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    ResponseEntity<?> value = contributorControllerImpl.updateRequester(1L, contributorVOWrite);
    assertEquals("The email write@reportnet.net doesn't exist in repornet", value.getBody());
    assertEquals(HttpStatus.NOT_FOUND, value.getStatusCode());
  }

  @Test
  public void updateReporterEEAExceptionTest() throws EEAException {
    userRepresentationVO.setEmail("read@reportnet.net");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    Mockito.doThrow(new EEAException("404 NOT_FOUND")).when(contributorService)
        .updateContributor(1L, contributorVORead, 1L);
    contributorVORead.setRole("REPORTER");
    ResponseEntity<?> value = contributorControllerImpl.updateReporter(1L, 1L, contributorVORead);
    assertEquals("The email read@reportnet.net doesn't exist in repornet", value.getBody());
    assertEquals(HttpStatus.NOT_FOUND, value.getStatusCode());
  }

  @Test
  public void updateReporterFindTempUsersTest() throws EEAException {

    ContributorVO contributorVO = new ContributorVO();
    contributorVO.setAccount("account@reportnet.net");
    contributorVO.setRole("role");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    Mockito
        .when(contributorService.findTempUserByAccountAndDataflow("account@reportnet.net", 1L, 1L))
        .thenReturn(contributorVO);
    contributorControllerImpl.updateReporter(1L, 1L, contributorVO);
    Mockito.verify(contributorService, times(1)).updateTemporaryUser(1L, contributorVO, 1L);
  }

  @Test
  public void updateReporterFindTempUsersNullTest() throws EEAException {
    ContributorVO contributorVO = new ContributorVO();
    contributorVO.setAccount("account@reportnet.net");
    contributorVO.setRole("role");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    Mockito
        .when(contributorService.findTempUserByAccountAndDataflow("account@reportnet.net", 1L, 1L))
        .thenReturn(null);
    contributorControllerImpl.updateReporter(1L, 1L, contributorVO);
    Mockito.verify(contributorService, times(1)).createTempUser(1L, contributorVO, 1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void updateReporterResponseStatusExceptionTest() throws EEAException {
    ContributorVO contributorVO = new ContributorVO();
    contributorVO.setAccount("account@reportnet.net");
    contributorVO.setRole("role");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    Mockito
        .when(contributorService.findTempUserByAccountAndDataflow("account@reportnet.net", 1L, 1L))
        .thenReturn(null).thenReturn(contributorVO);
    try {
      contributorControllerImpl.updateReporter(1L, 1L, contributorVO);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      assertEquals("There's already a user assigned with the same e-mail to this dataflow",
          ex.getReason());
      throw ex;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void checkIsBussinesCustodianTest() throws EEAException {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();

    ContributorVO contributorVO = new ContributorVO();
    contributorVO.setAccount("account@reportnet.net");
    contributorVO.setRole("role");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    Mockito.when(dataflowService.isDataflowType(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);

    try {
      contributorControllerImpl.updateRequester(1L, contributorVO);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
      throw ex;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void checkIsBussinesCustodianStewardTest() throws EEAException {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_DATAFLOW-1-DATA_STEWARD"));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();

    ContributorVO contributorVO = new ContributorVO();
    contributorVO.setAccount("account@reportnet.net");
    contributorVO.setRole("role");
    Mockito.when(dataflowService.isDataflowType(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);

    try {
      contributorControllerImpl.updateRequester(1L, contributorVO);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
      throw ex;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void checkIsBussinesCustodianObserverTest() throws EEAException {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_OBSERVER"));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();

    ContributorVO contributorVO = new ContributorVO();
    contributorVO.setAccount("account@reportnet.net");
    contributorVO.setRole("role");
    Mockito.when(dataflowService.isDataflowType(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);

    try {
      contributorControllerImpl.updateRequester(1L, contributorVO);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Find contributors by group.
   */
  @Test
  public void findContributorsByGroup() {
    contributorControllerImpl.findRequestersByGroup(1L);
    Mockito.verify(contributorService, times(1)).findContributorsByResourceId(1L, null,
        "REQUESTER");
  }

  /**
   * Delete.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void delete() throws EEAException {
    UserRepresentationVO user = new UserRepresentationVO();
    user.setEmail("write@reportnet.net");

    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    contributorControllerImpl.deleteRequester(1L, contributorVOWrite);
    Mockito.verify(contributorService, times(1)).deleteContributor(1L, "write@reportnet.net",
        "EDITOR", null);
  }

  /**
   * Delete throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteThrow() throws EEAException {
    Mockito.doThrow(EEAException.class).when(contributorService)
        .deleteContributor(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    try {
      contributorControllerImpl.deleteRequester(1L, contributorVOWrite);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }

  }

  /**
   * Delete editor email null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteEditorEmailNullTest() throws EEAException {

    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    try {
      contributorControllerImpl.deleteRequester(1L, contributorVOWrite);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
      assertEquals("The email write@reportnet.net doesn't exist in repornet", ex.getReason());
      throw ex;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteRequesterEEAExceptionTest() throws EEAException {

    UserRepresentationVO user = new UserRepresentationVO();
    user.setEmail("write@reportnet.net");

    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);

    Mockito.doThrow(new EEAException("404 NOT_FOUND")).when(contributorService)
        .deleteContributor(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any());

    try {
      contributorControllerImpl.deleteRequester(1L, contributorVOWrite);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
      assertEquals("The email write@reportnet.net doesn't exist in repornet", ex.getReason());
      throw ex;
    }
  }

  /**
   * Delete reporter email null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteReporterEmailNullTest() throws EEAException {

    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    try {
      contributorControllerImpl.deleteReporter(1L, 1L, contributorVOWrite);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
      assertEquals("The email write@reportnet.net doesn't exist in repornet", ex.getReason());
      throw ex;
    }
  }

  /**
   * Creates the associated permissions.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void createAssociatedPermissions() throws EEAException {
    contributorControllerImpl.createAssociatedPermissions(1L, 1L);
    Mockito.verify(contributorService, times(1)).createAssociatedPermissions(1L, 1L);
  }


  /**
   * Creates the associated permissions throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createAssociatedPermissionsThrow() throws EEAException {
    Mockito.doThrow(EEAException.class).when(contributorService).createAssociatedPermissions(1L,
        1L);
    try {
      contributorControllerImpl.createAssociatedPermissions(1L, 1L);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }
  }



  /**
   * Delete reporter.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void deleteReporter() throws EEAException {
    userRepresentationVO.setEmail("read@reportnet.net");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    contributorControllerImpl.deleteReporter(1L, 1L, contributorVORead);
    Mockito.verify(contributorService, times(1)).deleteContributor(1L, "read@reportnet.net",
        "EDITOR", 1L);
  }

  /**
   * Delete reporter throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteReporterThrow() throws EEAException {
    userRepresentationVO.setEmail("read@reportnet.net");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    Mockito.doThrow(EEAException.class).when(contributorService).deleteContributor(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    try {
      contributorControllerImpl.deleteReporter(1L, 1L, contributorVORead);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteReporterEEAExceptionTest() throws EEAException {

    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);

    Mockito.doThrow(new EEAException("404 NOT_FOUND")).when(contributorService)
        .deleteContributor(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any());

    try {
      contributorControllerImpl.deleteReporter(1L, 1L, contributorVORead);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
      throw ex;
    }
  }

  @Test
  public void deleteReporterFindTempUserTest() throws EEAException {
    ContributorVO contributorVO = new ContributorVO();
    contributorVO.setAccount("account@reportnet.net");
    contributorVO.setRole("role");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    Mockito
        .when(contributorService.findTempUserByAccountAndDataflow("account@reportnet.net", 1L, 1L))
        .thenReturn(contributorVO);
    contributorControllerImpl.deleteReporter(1L, 1L, contributorVO);
    Mockito.verify(contributorService, times(1)).deleteTemporaryUser(1L, "account@reportnet.net",
        "role", 1L);
  }

  /**
   * Find reporters by group.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void findReportersByGroup() throws EEAException {
    contributorControllerImpl.findReportersByGroup(1L, 1L);
    Mockito.verify(contributorService, times(1)).findContributorsByResourceId(1L, 1L, "REPORTER");
  }


  /**
   * Update reporter.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void updateReporter() throws EEAException {
    userRepresentationVO.setEmail("read@reportnet.net");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    contributorVORead.setRole("REPORTER");
    contributorControllerImpl.updateReporter(1L, 1L, contributorVORead);
    Mockito.verify(contributorService, times(1)).updateContributor(1L, contributorVORead, 1L);
  }


  /**
   * Update reporter throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void updateReporterThrow() throws EEAException {
    userRepresentationVO.setEmail("read@reportnet.net");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    Mockito.doThrow(EEAException.class).when(contributorService).updateContributor(1L,
        contributorVORead, 1L);
    contributorVORead.setRole("REPORTER");
    ResponseEntity<?> value = contributorControllerImpl.updateReporter(1L, 1L, contributorVORead);
    assertEquals(null, value.getBody());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, value.getStatusCode());

  }

  /**
   * Update editor email null test.
   *
   * @throws EEAException the EEA exception
   */
  // @Test
  public void updateEditorEmailNullTest() throws EEAException {
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    ResponseEntity<?> value = contributorControllerImpl.updateRequester(1L, contributorVOWrite);
    assertEquals("The email write@reportnet.net doesn't exist in repornet", value.getBody());
    assertEquals(HttpStatus.NOT_FOUND, value.getStatusCode());
  }

  /**
   * Update reporter email null test.
   *
   * @throws EEAException the EEA exception
   */
  // @Test
  public void updateReporterEmailNullTest() throws EEAException {
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    ResponseEntity<?> value = contributorControllerImpl.updateReporter(1L, 1L, contributorVOWrite);
    assertEquals("The email write@reportnet.net doesn't exist in repornet", value.getBody());
    assertEquals(HttpStatus.NOT_FOUND, value.getStatusCode());
  }

  /**
   * Update reporter email not correct test.
   *
   * @throws EEAException the EEA exception
   */
  // @Test
  public void updateReporterEmailNotCorrectTest() throws EEAException {
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    ResponseEntity<?> value = contributorControllerImpl.updateReporter(1L, 1L, contributorVORead);
    assertEquals("The email read@reportnet.net doesn't exist in repornet", value.getBody());
    assertEquals(HttpStatus.NOT_FOUND, value.getStatusCode());
  }

  @Test
  public void validateReportersTest() throws EEAException {
    contributorControllerImpl.validateReporters(1L, 1L);
    Mockito.verify(contributorService, times(1)).validateReporters(Mockito.anyLong(), Mockito.any(),
        Mockito.anyBoolean());
  }

  @Test
  public void validateReportersEEAExceptionTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(contributorService).validateReporters(1L, 1L, true);
    contributorControllerImpl.validateReporters(1L, 1L);
    Mockito.verify(contributorService, times(1)).validateReporters(Mockito.anyLong(), Mockito.any(),
        Mockito.anyBoolean());
  }
}
