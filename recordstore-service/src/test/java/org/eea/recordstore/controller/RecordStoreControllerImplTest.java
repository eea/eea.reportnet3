package org.eea.recordstore.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.exception.DockerAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.recordstore.service.impl.RecordStoreServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class RecordStoreControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class RecordStoreControllerImplTest {


  /** The record store service impl. */
  @InjectMocks
  private RecordStoreServiceImpl recordStoreServiceImpl;

  /** The record store controller impl. */
  @InjectMocks
  private RecordStoreControllerImpl recordStoreControllerImpl;

  /** The record store service. */
  @Mock
  private RecordStoreService recordStoreService;


  /** The Constant TEST. */
  private static final String TEST = "test";

  /** The Constant FAILED. */
  private static final String FAILED = "failed";

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(recordStoreServiceImpl, "containerName", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "ipPostgreDb", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "userPostgreDb", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "passPostgreDb", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "connStringPostgre",
        "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "connStringPostgre",
        "select * from pg_namespace where nspname like 'dataset%'");
  }



  /**
   * Test create empty data set.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void testCreateEmptyDataSet() throws DockerAccessException {
    recordStoreControllerImpl.createEmptyDataset(TEST);
    Mockito.verify(recordStoreService, times(1)).createEmptyDataSet(Mockito.any());
  }

  /**
   * Test create empty data set exception.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void testCreateEmptyDataSetException() throws DockerAccessException {
    doThrow(new DockerAccessException()).when(recordStoreService).createEmptyDataSet(TEST);
    recordStoreControllerImpl.createEmptyDataset(TEST);
    Mockito.verify(recordStoreService, times(1)).createEmptyDataSet(Mockito.any());
  }

  /**
   * Reste data set data base test.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void resteDataSetDataBaseTest() throws DockerAccessException {
    recordStoreControllerImpl.resteDataSetDataBase();
    Mockito.verify(recordStoreService, times(1)).resetDatasetDatabase();
  }

  /**
   * Reste data set data base test exception.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void resteDataSetDataBaseTestException() throws DockerAccessException {
    doThrow(new DockerAccessException()).when(recordStoreService).resetDatasetDatabase();
    recordStoreControllerImpl.resteDataSetDataBase();
    Mockito.verify(recordStoreService, times(1)).resetDatasetDatabase();
  }

  /**
   * Gets the connection to dataset test.
   *
   * @return the connection to dataset test
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void getConnectionToDatasetTest() throws DockerAccessException {
    ConnectionDataVO expectedResult = new ConnectionDataVO();
    Mockito.when(recordStoreService.getConnectionDataForDataset(TEST)).thenReturn(expectedResult);
    ConnectionDataVO result = recordStoreControllerImpl.getConnectionToDataset(TEST);
    assertEquals(FAILED, expectedResult, result);
  }

  /**
   * Gets the connection to dataset test exception.
   *
   * @return the connection to dataset test exception
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void getConnectionToDatasetTestException() throws DockerAccessException {
    doThrow(new DockerAccessException()).when(recordStoreService).getConnectionDataForDataset(TEST);
    ConnectionDataVO result = recordStoreControllerImpl.getConnectionToDataset(TEST);
    assertNull(FAILED, result);
  }


  /**
   * Gets the data set connections test.
   *
   * @return the data set connections test
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void getDataSetConnectionsTest() throws DockerAccessException {
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
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void getDataSetConnectionsTestException() throws DockerAccessException {
    doThrow(new DockerAccessException()).when(recordStoreService).getConnectionDataForDataset();
    List<ConnectionDataVO> result = recordStoreControllerImpl.getDataSetConnections();
    assertNull(FAILED, result);
  }



}
