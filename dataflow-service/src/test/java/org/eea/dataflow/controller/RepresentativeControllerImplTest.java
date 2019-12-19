package org.eea.dataflow.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.junit.Assert;
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
 * The Class RepresentativeControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class RepresentativeControllerImplTest {

  /** The representative controller impl. */
  @InjectMocks
  private RepresentativeControllerImpl representativeControllerImpl;

  /** The user management controller zull. */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /** The representative service. */
  @Mock
  private RepresentativeService representativeService;

  /** The representative VO. */
  @Mock
  private RepresentativeVO representativeVO;

  /** The users. */
  private List<UserRepresentationVO> users;

  /** The user. */
  private UserRepresentationVO user;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    user = new UserRepresentationVO();
    user.setEmail("email@host.com");
    users = new ArrayList<>();
    users.add(user);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Insert representative test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRepresentativeTest1() throws EEAException {
    Mockito.when(userManagementControllerZull.getUsers()).thenReturn(users);
    Mockito.when(representativeVO.getProviderAccount()).thenReturn("email@host.com");
    Mockito.when(representativeService.insertRepresentative(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Assert.assertEquals((Long) 1L,
        representativeControllerImpl.insertRepresentative(1L, representativeVO));
  }

  /**
   * Insert representative test 2.
   */
  @Test
  public void insertRepresentativeTest2() {
    Mockito.when(userManagementControllerZull.getUsers()).thenReturn(users);
    Mockito.when(representativeVO.getProviderAccount()).thenReturn("otro@host.com");
    try {
      representativeControllerImpl.insertRepresentative(1L, representativeVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.USER_REQUEST_NOTFOUND, e.getReason());
    }
  }

  /**
   * Insert representative test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRepresentativeTest3() throws EEAException {
    Mockito.when(userManagementControllerZull.getUsers()).thenReturn(users);
    Mockito.when(representativeVO.getProviderAccount()).thenReturn("email@host.com");
    Mockito.when(representativeService.insertRepresentative(Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    try {
      representativeControllerImpl.insertRepresentative(1L, representativeVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.DOCUMENT_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Find all data provider by group id test 1.
   */
  @Test
  public void findAllDataProviderByGroupIdTest1() {
    Mockito.when(representativeService.getAllDataProviderByGroupId(Mockito.any()))
        .thenReturn(new ArrayList<DataProviderVO>());
    Assert.assertEquals(0, representativeControllerImpl.findAllDataProviderByGroupId(1L).size());
  }

  /**
   * Find all data provider by group id test 2.
   */
  @Test
  public void findAllDataProviderByGroupIdTest2() {
    try {
      representativeControllerImpl.findAllDataProviderByGroupId(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.REPRESENTATIVE_TYPE_INCORRECT, e.getReason());
    }
  }

  /**
   * Find all data provider types test.
   */
  @Test
  public void findAllDataProviderTypesTest() {
    Mockito.when(representativeService.getAllDataProviderTypes())
        .thenReturn(new ArrayList<DataProviderCodeVO>());
    Assert.assertEquals(0, representativeControllerImpl.findAllDataProviderTypes().size());
  }
}
