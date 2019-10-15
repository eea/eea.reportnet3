package org.eea.dataflow.io.kafka.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.service.DataflowDocumentService;
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
 * The Class LoadDocumentCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadDocumentCommandTest {

  /** The load document command. */
  @InjectMocks
  private LoadDocumentCommand loadDocumentCommand;

  /** The dataflow document service. */
  @Mock
  private DataflowDocumentService dataflowDocumentService;

  /**
   * The data.
   */
  private Map<String, Object> data;

  /**
   * The eea event VO.
   */
  private EEAEventVO eeaEventVO;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    data = new HashMap<>();
    data.put("uuid", "uuid");
    data.put("datasetId", "1L");
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.LOAD_DOCUMENT_COMPLETED_EVENT);
    eeaEventVO.setData(data);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.LOAD_DOCUMENT_COMPLETED_EVENT, loadDocumentCommand.getEventType());
  }

  /**
   * Execute test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTest() throws EEAException {
    doNothing().when(dataflowDocumentService).insertDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    loadDocumentCommand.execute(eeaEventVO);

    Mockito.verify(dataflowDocumentService, times(1)).insertDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Execute throw test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeThrowTest() throws EEAException {
    doThrow(new EEAException("error")).when(dataflowDocumentService).insertDocument(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    loadDocumentCommand.execute(eeaEventVO);
    Mockito.verify(dataflowDocumentService, times(1)).insertDocument(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }
}
