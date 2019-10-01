package org.eea.dataflow.io.kafka;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class EventHandlerTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class EventHandlerTest {

  // /** The event handler. */
  // @InjectMocks
  // private EventHandler eventHandler;
  //
  // /** The dataflow service. */
  // @Mock
  // private DataflowDocumentService dataflowService;
  //
  // /**
  // * Inits the mocks.
  // */
  // @Before
  // public void initMocks() {
  // MockitoAnnotations.initMocks(this);
  // }
  //
  // /**
  // * Gets the type.
  // *
  // * @return the type
  // */
  // @Test
  // public void getType() {
  // assertEquals("failed", EEAEventVO.class, eventHandler.getType());
  // }
  //
  // /**
  // * Process message load event exception test.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test
  // public void processMessageLoadEventExceptionTest() throws EEAException {
  // doThrow(new EEAException()).when(dataflowService).insertDocument(Mockito.any(), Mockito.any(),
  // Mockito.any(), Mockito.any());
  // EEAEventVO event = new EEAEventVO();
  // Map<String, Object> map = new HashMap<>();
  // event.setData(map);
  // event.setEventType(EventType.LOAD_DOCUMENT_COMPLETED_EVENT);
  // eventHandler.processMessage(event);
  //
  // Mockito.verify(dataflowService, times(1)).insertDocument(Mockito.any(), Mockito.any(),
  // Mockito.any(), Mockito.any());
  // }
  //
  // /**
  // * Process message load event test.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test
  // public void processMessageLoadEventTest() throws EEAException {
  // doNothing().when(dataflowService).insertDocument(Mockito.any(), Mockito.any(), Mockito.any(),
  // Mockito.any());
  // EEAEventVO event = new EEAEventVO();
  // Map<String, Object> map = new HashMap<>();
  // event.setData(map);
  // event.setEventType(EventType.LOAD_DOCUMENT_COMPLETED_EVENT);
  // eventHandler.processMessage(event);
  // Mockito.verify(dataflowService, times(1)).insertDocument(Mockito.any(), Mockito.any(),
  // Mockito.any(), Mockito.any());
  // }
  //
  // /**
  // * Process message delete test.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test
  // public void processMessageDeleteTest() throws EEAException {
  // doNothing().when(dataflowService).deleteDocument(Mockito.any());
  // EEAEventVO event = new EEAEventVO();
  // Map<String, Object> map = new HashMap<>();
  // event.setData(map);
  // event.setEventType(EventType.DELETE_DOCUMENT_COMPLETED_EVENT);
  // eventHandler.processMessage(event);
  // Mockito.verify(dataflowService, times(1)).deleteDocument(Mockito.any());
  // }
  //
  // /**
  // * Process message deleteexception test.
  // *
  // * @throws EEAException the EEA exception
  // */
  // @Test
  // public void processMessageDeleteexceptionTest() throws EEAException {
  // doThrow(new EEAException()).when(dataflowService).deleteDocument(Mockito.any());
  // EEAEventVO event = new EEAEventVO();
  // Map<String, Object> map = new HashMap<>();
  // event.setData(map);
  // event.setEventType(EventType.DELETE_DOCUMENT_COMPLETED_EVENT);
  // eventHandler.processMessage(event);
  // Mockito.verify(dataflowService, times(1)).deleteDocument(Mockito.any());
  // }
}
