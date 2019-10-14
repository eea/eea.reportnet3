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
 * The Class DeleteDocumentCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteDocumentCommandTest {

  /** The delete document command. */
  @InjectMocks
  private DeleteDocumentCommand deleteDocumentCommand;

  /**
   * The validation helper.
   */
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
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.DELETE_DOCUMENT_COMPLETED_EVENT);
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
    assertEquals(EventType.DELETE_DOCUMENT_COMPLETED_EVENT, deleteDocumentCommand.getEventType());
  }

  /**
   * Execute self test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTest() throws EEAException {
    doNothing().when(dataflowDocumentService).deleteDocument(Mockito.any());
    deleteDocumentCommand.execute(eeaEventVO);
    Mockito.verify(dataflowDocumentService, times(1)).deleteDocument(Mockito.any());
  }

  /**
   * Execute throw test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeThrowTest() throws EEAException {
    doThrow(new EEAException("error")).when(dataflowDocumentService).deleteDocument(Mockito.any());
    deleteDocumentCommand.execute(eeaEventVO);
    Mockito.verify(dataflowDocumentService, times(1)).deleteDocument(Mockito.any());
  }
}
