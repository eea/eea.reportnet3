package org.eea.dataset.io.kafka.commands;

import static org.mockito.Mockito.times;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.junit.Assert;
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

  /** The dataset repository. */
  @Mock
  private DatasetRepository datasetRepository;

  /** The schema repository. */
  @Mock
  private SchemasRepository schemaRepository;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    eeaEventVO = new EEAEventVO();
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.CONNECTION_CREATED_EVENT, createConnectionCommand.getEventType());
  }

  /**
   * Execute test 1.
   */
  @Test
  public void executeTest1() {
    eeaEventVO.setEventType(EventType.COMMAND_CLEAN_KYEBASE);
    createConnectionCommand.execute(eeaEventVO);
    Mockito.verifyNoMoreInteractions(datasetService);
  }

  /**
   * Execute test 1.
   */
  @Test
  public void executeTest2() {
    eeaEventVO.setEventType(EventType.CONNECTION_CREATED_EVENT);
    data = new HashMap<>();
    data.put("dataset_id", "dataset_1");
    data.put("idDatasetSchema", "");
    eeaEventVO.setData(data);
    createConnectionCommand.execute(eeaEventVO);
    Mockito.verifyNoMoreInteractions(datasetService);
  }

  /**
   * Execute test 2.
   */
  @Test
  public void executeTest3() {
    eeaEventVO.setEventType(EventType.CONNECTION_CREATED_EVENT);
    data = new HashMap<>();
    data.put("dataset_id", "");
    data.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    eeaEventVO.setData(data);
    createConnectionCommand.execute(eeaEventVO);
    Mockito.verifyNoMoreInteractions(datasetService);
  }

  /**
   * Execute test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTest4() throws EEAException {
    eeaEventVO.setEventType(EventType.CONNECTION_CREATED_EVENT);
    data = new HashMap<>();
    data.put("dataset_id", "dataset_1");
    data.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    eeaEventVO.setData(data);
    Mockito.doNothing().when(datasetService).insertSchema(Mockito.any(), Mockito.any());
    createConnectionCommand.execute(eeaEventVO);
    Mockito.verify(datasetService, times(1)).insertSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Execute test 5.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTest5() throws EEAException {
    eeaEventVO.setEventType(EventType.CONNECTION_CREATED_EVENT);
    data = new HashMap<>();
    data.put("dataset_id", "dataset_1");
    data.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    eeaEventVO.setData(data);
    Mockito.doThrow(EEAException.class).when(datasetService).insertSchema(Mockito.any(),
        Mockito.any());
    createConnectionCommand.execute(eeaEventVO);
    Mockito.verify(datasetService, times(1)).insertSchema(Mockito.any(), Mockito.any());
  }
}
