package org.eea.recordstore.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.recordstore.service.impl.JdbcRecordStoreServiceImpl;
import org.eea.recordstore.service.impl.SnapshotHelper;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class RecordStoreControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class RecordStoreControllerImplTest {



  /** The record store controller impl. */
  @InjectMocks
  private RecordStoreControllerImpl recordStoreControllerImpl;

  /** The record store service. */
  @Mock
  private RecordStoreService recordStoreService;

  @Mock
  private JdbcRecordStoreServiceImpl jdbcRecordStoreServiceImpl;

  @Mock
  private SnapshotHelper restoreSnapshotHelper;


  /** The Constant TEST. */
  private static final String TEST = "test";

  /** The Constant FAILED. */
  private static final String FAILED = "failed";

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    Set<String> roles = new HashSet<>();
    roles.add(ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER.getAccessRole(1L));
    UserDetails userDetails = EeaUserDetails.create("test", roles);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    Map<String, String> details = new HashMap<>();
    details.put("", "");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    MockitoAnnotations.openMocks(this);
  }



  /**
   * Test create empty data set.
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  @Test
  public void testCreateEmptyDataSet() throws RecordStoreAccessException {
    recordStoreControllerImpl.createEmptyDataset(TEST, TEST);
    Mockito.verify(recordStoreService, times(1)).createEmptyDataSet(Mockito.any(), Mockito.any());
  }

  /**
   * Test create empty data set exception.
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testCreateEmptyDataSetException() throws RecordStoreAccessException {
    doThrow(new RecordStoreAccessException("error")).when(recordStoreService)
        .createEmptyDataSet(TEST, TEST);
    try {
      recordStoreControllerImpl.createEmptyDataset(TEST, TEST);
    } catch (ResponseStatusException e) {
      assertEquals(String.format("error"), e.getReason());
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Gets the connection to dataset test.
   *
   * @return the connection to dataset test
   * @throws RecordStoreAccessException the docker access exception
   */
  @Test
  public void getConnectionToDatasetTest() throws RecordStoreAccessException {
    ConnectionDataVO expectedResult = new ConnectionDataVO();
    Mockito.when(recordStoreService.getConnectionDataForDataset(TEST)).thenReturn(expectedResult);
    ConnectionDataVO result = recordStoreControllerImpl.getConnectionToDataset(TEST);
    assertEquals(FAILED, expectedResult, result);
  }

  /**
   * Gets the connection to dataset test exception.
   *
   * @return the connection to dataset test exception
   * @throws RecordStoreAccessException the docker access exception
   */
  @Test
  public void getConnectionToDatasetTestException() throws RecordStoreAccessException {
    doThrow(new RecordStoreAccessException()).when(recordStoreService)
        .getConnectionDataForDataset(TEST);
    ConnectionDataVO result = recordStoreControllerImpl.getConnectionToDataset(TEST);
    assertNull(FAILED, result);
  }


  /**
   * Gets the data set connections test.
   *
   * @return the data set connections test
   * @throws RecordStoreAccessException the docker access exception
   */
  @Test
  public void getDataSetConnectionsTest() throws RecordStoreAccessException {
    List<ConnectionDataVO> expectedResult = new ArrayList<>();
    expectedResult.add(new ConnectionDataVO());
    Mockito.when(recordStoreService.getConnectionDataForDataset()).thenReturn(expectedResult);
    List<ConnectionDataVO> result = recordStoreControllerImpl.getDataSetConnections();
    assertEquals(FAILED, expectedResult, result);
  }

  /**
   * Gets the data set connections test exception.
   *
   * @return the data set connections test exception
   * @throws RecordStoreAccessException the docker access exception
   */
  @Test
  public void getDataSetConnectionsTestException() throws RecordStoreAccessException {
    doThrow(new RecordStoreAccessException()).when(recordStoreService)
        .getConnectionDataForDataset();
    List<ConnectionDataVO> result = recordStoreControllerImpl.getDataSetConnections();
    assertNull(FAILED, result);
  }

  @Test
  public void testCreateSnapshot()
      throws SQLException, IOException, RecordStoreAccessException, EEAException {
    recordStoreControllerImpl.createSnapshotData(1L, 1L, 1L,
        java.sql.Timestamp.valueOf(LocalDateTime.now()).toString(), Mockito.any());

    Mockito.verify(recordStoreService, times(1)).createDataSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  public void testRestoreSnapshot()
      throws SQLException, IOException, RecordStoreAccessException, EEAException {
    recordStoreControllerImpl.restoreSnapshotData(1L, 1L, 1L, DatasetTypeEnum.DESIGN, true, false,
        false);
    Mockito.verify(restoreSnapshotHelper, times(1)).processRestoration(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  public void testDeleteSnapshot() throws IOException {
    recordStoreControllerImpl.deleteSnapshotData(1L, 1L);

    Mockito.verify(recordStoreService, times(1)).deleteDataSnapshot(Mockito.any(), Mockito.any());
  }

  @Test
  public void testDeleteDataset() throws IOException {
    recordStoreControllerImpl.deleteDataset("schema");

    Mockito.verify(recordStoreService, times(1)).deleteDataset(Mockito.any());
  }
}
