package org.eea.dataset.io.kafka.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class CreateConnectionCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateConnectionCommandTest {


  /** The create connection command. */
  @InjectMocks
  private CreateConnectionCommand createConnectionCommand;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test execute create connection.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteCreateConnection() throws EEAException {

    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.CONNECTION_CREATED_EVENT);
    data = new HashMap<>();
    data.put("dataset_id", "dataset_1");
    data.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    eeaEventVO.setData(data);

    doNothing().when(datasetService).insertSchema(Mockito.any(), Mockito.any());
    doNothing().when(datasetService).saveStatistics(Mockito.any());

    createConnectionCommand.execute(eeaEventVO);
  }

  /**
   * Test execute create connection 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteCreateConnection2() throws EEAException {

    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATE_TABLE);
    data = new HashMap<>();
    data.put("dataset_id", "dataset_1");
    data.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    eeaEventVO.setData(data);

    createConnectionCommand.execute(eeaEventVO);
  }

  /**
   * Test execute create connection 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteCreateConnection3() throws EEAException {

    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.CONNECTION_CREATED_EVENT);
    data = new HashMap<>();
    data.put("dataset_id", "dataset_1");
    eeaEventVO.setData(data);

    createConnectionCommand.execute(eeaEventVO);
  }

  /**
   * Test execute create connection 4.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteCreateConnection4() throws EEAException {

    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.CONNECTION_CREATED_EVENT);
    data = new HashMap<>();
    data.put("dataset_id", "dataset_1");
    data.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    eeaEventVO.setData(data);

    doThrow(EEAException.class).when(datasetService).insertSchema(Mockito.any(), Mockito.any());

    createConnectionCommand.execute(eeaEventVO);
  }

  /**
   * Test execute create connection 5.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteCreateConnection5() throws EEAException {

    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.CONNECTION_CREATED_EVENT);
    data = new HashMap<>();
    data.put("dataset_id", "dataset_1");
    data.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    eeaEventVO.setData(data);

    // doThrow(EEAException.class).when(datasetService).saveStatistics(Mockito.any());

    createConnectionCommand.execute(eeaEventVO);
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.CONNECTION_CREATED_EVENT, createConnectionCommand.getEventType());
  }

}
