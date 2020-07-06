package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import org.eea.dataflow.service.ContributorService;
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

  /** The contributor VO write. */
  private ContributorVO contributorVOWrite;

  /** The contributor VO read. */
  private ContributorVO contributorVORead;

  /** The user representation VO. */
  private UserRepresentationVO userRepresentationVO;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    contributorVOWrite = new ContributorVO();
    contributorVOWrite.setAccount("write@reportnet.net");
    contributorVOWrite.setRole("EDITOR");
    contributorVOWrite.setWritePermission(true);

    contributorVORead = new ContributorVO();
    contributorVORead.setAccount("read@reportnet.net");
    contributorVORead.setRole("EDITOR");
    contributorVORead.setWritePermission(false);

    userRepresentationVO = new UserRepresentationVO();
    userRepresentationVO.setEmail("write@reportnet.net");
    MockitoAnnotations.initMocks(this);
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
    contributorControllerImpl.updateEditor(1L, contributorVOWrite);
    Mockito.verify(contributorService, times(1)).updateContributor(1L, contributorVOWrite, "EDITOR",
        null);
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
        contributorVOWrite, "EDITOR", null);
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    ResponseEntity<?> value = contributorControllerImpl.updateEditor(1L, contributorVOWrite);
    assertEquals(null, value.getBody());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, value.getStatusCode());
  }

  @Test
  public void updateContributorNonEditor() throws EEAException {
    contributorVOWrite.setRole("REPO");
    ResponseEntity<?> value = contributorControllerImpl.updateEditor(1L, contributorVOWrite);
    assertEquals("Role REPO doesn't exist", value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());
  }

  /**
   * Find contributors by group.
   */
  @Test
  public void findContributorsByGroup() {
    contributorControllerImpl.findEditorsByGroup(1L);
    Mockito.verify(contributorService, times(1)).findContributorsByResourceId(1L, null, "EDITOR");
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
    contributorControllerImpl.deleteEditor(1L, contributorVOWrite);
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
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    Mockito.doThrow(EEAException.class).when(contributorService)
        .deleteContributor(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    try {
      contributorControllerImpl.deleteEditor(1L, contributorVOWrite);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }

  }

  /**
   * Email null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void emailNullTest() throws EEAException {

    userRepresentationVO.setEmail("write@reportnet.net");
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any())).thenReturn(null);
    try {
      contributorControllerImpl.deleteEditor(1L, contributorVOWrite);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
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
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    contributorControllerImpl.deleteReporter(1L, 1L, contributorVORead);
  }

  /**
   * Delete reporter throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteReporterThrow() throws EEAException {
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    Mockito.doThrow(EEAException.class).when(contributorService).deleteContributor(1L,
        "read@reportnet.net", "REPORTER", 1L);
    try {
      contributorControllerImpl.deleteReporter(1L, 1L, contributorVORead);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }
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
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    contributorVORead.setRole("REPORTER");
    contributorControllerImpl.updateReporter(1L, 1L, contributorVORead);
    Mockito.verify(contributorService, times(1)).updateContributor(1L, contributorVORead,
        "REPORTER", 1L);
  }


  /**
   * Update reporter throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void updateReporterThrow() throws EEAException {
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(userRepresentationVO);
    Mockito.doThrow(EEAException.class).when(contributorService).updateContributor(1L,
        contributorVORead, "REPORTER", 1L);
    contributorVORead.setRole("REPORTER");
    ResponseEntity<?> value = contributorControllerImpl.updateReporter(1L, 1L, contributorVORead);
    assertEquals(null, value.getBody());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, value.getStatusCode());

  }

  /**
   * Update reporter non valid rol throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void updateReporterNonValidRolThrow() throws EEAException {
    ResponseEntity<?> value = contributorControllerImpl.updateReporter(1L, 1L, contributorVORead);
    assertEquals("Role EDITOR doesn't exist", value.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, value.getStatusCode());

  }
}
