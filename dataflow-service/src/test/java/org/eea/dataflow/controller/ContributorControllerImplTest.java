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

  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /** The contributor VO write. */
  private ContributorVO contributorVOWrite;

  /** The contributor VO read. */
  private ContributorVO contributorVORead;


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
    ResponseEntity<?> value = contributorControllerImpl.updateEditor(1L, contributorVOWrite);
    assertEquals(null, value.getBody());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, value.getStatusCode());
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
    Mockito.when(userManagementControllerZull.getUserByEmail(Mockito.any()))
        .thenReturn(new UserRepresentationVO());
    contributorControllerImpl.deleteEditor(1L, contributorVOWrite);
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
    try {
      contributorControllerImpl.deleteEditor(1L, contributorVOWrite);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }
  }


}
