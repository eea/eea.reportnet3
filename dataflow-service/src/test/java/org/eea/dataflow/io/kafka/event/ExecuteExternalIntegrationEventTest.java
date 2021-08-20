package org.eea.dataflow.io.kafka.event;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.integration.crud.factory.CrudManager;
import org.eea.dataflow.integration.crud.factory.CrudManagerFactory;
import org.eea.dataflow.integration.executor.IntegrationExecutorFactory;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.utils.LiteralConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;


/**
 * The Class ExecuteExternalIntegrationEventTest.
 */
public class ExecuteExternalIntegrationEventTest {

  /** The execute external integration event. */
  @InjectMocks
  private ExecuteExternalIntegrationEvent executeExternalIntegrationEvent;

  /** The integration executor factory. */
  @Mock
  private IntegrationExecutorFactory integrationExecutorFactory;

  /** The integration service. */
  @Mock
  private IntegrationService integrationService;

  /** The crud manager. */
  @Mock
  private CrudManager crudManager;

  /** The crud manager factory. */
  @Mock
  private CrudManagerFactory crudManagerFactory;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.DATA_DELETE_TO_REPLACE_COMPLETED_EVENT,
        executeExternalIntegrationEvent.getEventType());
  }

  @Test
  public void executeTest() throws EEAException {

    Mockito.doNothing().when(integrationService).executeExternalIntegration(Mockito.anyLong(),
        Mockito.any(), Mockito.any(), Mockito.any());
    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.DATA_DELETE_TO_REPLACE_COMPLETED_EVENT);
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, 1L);
    value.put(LiteralConstants.INTEGRATION_ID, 1L);
    value.put(LiteralConstants.OPERATION, IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM);
    event.setData(value);
    executeExternalIntegrationEvent.execute(event);
    Mockito.verify(integrationService, Mockito.times(1)).executeExternalIntegration(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void executeTestException() throws EEAException {

    Mockito.doThrow(EEAException.class).when(integrationService)
        .executeExternalIntegration(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.DATA_DELETE_TO_REPLACE_COMPLETED_EVENT);
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, 1L);
    value.put(LiteralConstants.INTEGRATION_ID, 1L);
    value.put(LiteralConstants.OPERATION, IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM);
    event.setData(value);
    try {
      executeExternalIntegrationEvent.execute(event);
    } catch (EEAException e) {
      assertEquals(
          "Error executing an external integration with id 1 on the datasetId 1, with message: null",
          e.getMessage());
      throw e;
    }
  }
}
