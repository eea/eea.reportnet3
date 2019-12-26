package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
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
  private RepresentativeVO representativeVO;

  /** The users. */
  private List<UserRepresentationVO> users;

  /** The user. */
  private UserRepresentationVO user;

  /** The representative V os. */
  private List<RepresentativeVO> representativeVOs;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    user = new UserRepresentationVO();
    user.setEmail("email@host.com");
    users = new ArrayList<>();
    users.add(user);
    representativeVO = new RepresentativeVO();
    representativeVO.setProviderAccount("email@host.com");
    representativeVOs = new ArrayList<>();
    representativeVOs.add(representativeVO);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Insert representative success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRepresentativeSuccessTest() throws EEAException {
    when(userManagementControllerZull.getUsers()).thenReturn(users);
    when(representativeService.insertRepresentative(Mockito.any(), Mockito.any())).thenReturn(1L);
    assertEquals((Long) 1L,
        representativeControllerImpl.insertRepresentative(1L, representativeVO));
  }

  /**
   * Insert representative exception 1 test.
   */
  @Test
  public void insertRepresentativeException1Test() {
    representativeVO.setProviderAccount("otro@host.com");
    when(userManagementControllerZull.getUsers()).thenReturn(users);
    try {
      representativeControllerImpl.insertRepresentative(1L, representativeVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.USER_REQUEST_NOTFOUND, e.getReason());
    }
  }

  /**
   * Insert representative exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRepresentativeException2Test() throws EEAException {
    when(userManagementControllerZull.getUsers()).thenReturn(users);
    when(representativeService.insertRepresentative(Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    try {
      representativeControllerImpl.insertRepresentative(1L, representativeVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Find all data provider by group id success test.
   */
  @Test
  public void findAllDataProviderByGroupIdSuccessTest() {
    Mockito.when(representativeService.getAllDataProviderByGroupId(Mockito.any()))
        .thenReturn(new ArrayList<DataProviderVO>());
    assertEquals(0, representativeControllerImpl.findAllDataProviderByGroupId(1L).size());
  }

  /**
   * Find all data provider by group id exception test.
   */
  @Test
  public void findAllDataProviderByGroupIdExceptionTest() {
    try {
      representativeControllerImpl.findAllDataProviderByGroupId(null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.REPRESENTATIVE_TYPE_INCORRECT, e.getReason());
    }
  }

  /**
   * Find all data provider types success test.
   */
  @Test
  public void findAllDataProviderTypesSuccessTest() {
    when(representativeService.getAllDataProviderTypes())
        .thenReturn(new ArrayList<DataProviderCodeVO>());
    assertEquals(0, representativeControllerImpl.findAllDataProviderTypes().size());
  }

  /**
   * Find represetatives by id data flow exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findRepresetativesByIdDataFlowException1Test() throws EEAException {
    try {
      representativeControllerImpl.findRepresentativesByIdDataFlow(null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATAFLOW_NOTFOUND, e.getReason());
    }
  }

  /**
   * Find represetatives by id data flow exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findRepresetativesByIdDataFlowException2Test() throws EEAException {
    doThrow(new EEAException()).when(representativeService)
        .getRepresetativesByIdDataFlow(Mockito.anyLong());
    try {
      representativeControllerImpl.findRepresentativesByIdDataFlow(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Find represetatives by id data flow success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findRepresetativesByIdDataFlowSuccessTest() throws EEAException {
    when(representativeService.getRepresetativesByIdDataFlow(Mockito.anyLong()))
        .thenReturn(representativeVOs);
    assertEquals("error in the message", representativeVOs,
        representativeControllerImpl.findRepresentativesByIdDataFlow(1L));
  }

  /**
   * Update representative success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRepresentativeSuccessTest() throws EEAException {
    when(userManagementControllerZull.getUsers()).thenReturn(users);
    representativeControllerImpl.updateRepresentative(representativeVO);
    Mockito.verify(representativeService, times(1)).updateDataflowRepresentative(Mockito.any());
  }

  /**
   * Update representative success no account test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRepresentativeSuccessNoAccountTest() throws EEAException {
    representativeVO.setProviderAccount(null);
    representativeControllerImpl.updateRepresentative(representativeVO);
    Mockito.verify(representativeService, times(1)).updateDataflowRepresentative(Mockito.any());
  }

  /**
   * Update representative exception 1 test.
   */
  @Test
  public void updateRepresentativeException1Test() {
    representativeVO.setProviderAccount("otro@host.com");
    when(userManagementControllerZull.getUsers()).thenReturn(users);
    try {
      representativeControllerImpl.updateRepresentative(representativeVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.USER_REQUEST_NOTFOUND, e.getReason());
    }
  }

  /**
   * Update representative exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRepresentativeException2Test() throws EEAException {
    when(userManagementControllerZull.getUsers()).thenReturn(users);
    when(representativeService.updateDataflowRepresentative(Mockito.any()))
        .thenThrow(EEAException.class);
    try {
      representativeControllerImpl.updateRepresentative(representativeVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Delete representative exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRepresentativeExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(representativeService)
        .deleteDataflowRepresentative(Mockito.any());
    try {
      representativeControllerImpl.deleteRepresentative(null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      assertEquals(EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Delete representative success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRepresentativeSuccessTest() throws EEAException {
    representativeControllerImpl.deleteRepresentative(1L);
    Mockito.verify(representativeService, times(1)).deleteDataflowRepresentative(Mockito.any());
  }
}
