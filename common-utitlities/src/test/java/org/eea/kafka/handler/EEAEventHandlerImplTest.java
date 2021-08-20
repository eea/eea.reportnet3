package org.eea.kafka.handler;

import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.EEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.factory.EEAEventCommandFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * The type Eea event handler impl test.
 */
public class EEAEventHandlerImplTest {

  /**
   * The eea event handler.
   */
  @InjectMocks
  private EEAEventHandlerImpl eeaEventHandler;

  /**
   * The eea eent command factory.
   */
  @Mock
  private EEAEventCommandFactory eeaEentCommandFactory;

  @Mock
  private EEAEventHandlerCommand eeaEventHandlerCommand;

  private EEAEventVO event;

  @Before
  public void initMocks() {
    event = new EEAEventVO();
    Map<String, Object> data = new HashMap<>();
    event.setData(data);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Gets type.
   *
   * @return the type
   */
  @Test
  public void getType() {
    Assert.assertEquals(eeaEventHandler.getType(), EEAEventVO.class);
  }

  @Test
  public void processMessageTest1() throws EEAException {
    Mockito.when(eeaEentCommandFactory.getEventCommand(Mockito.any()))
        .thenReturn(eeaEventHandlerCommand);
    event.getData().put("user", "user");
    eeaEventHandler.processMessage(event);
    Mockito.verify(eeaEentCommandFactory, times(1)).getEventCommand(Mockito.any());
  }

  @Test
  public void processMessageTest2() throws EEAException {
    Mockito.when(eeaEentCommandFactory.getEventCommand(Mockito.any()))
        .thenReturn(eeaEventHandlerCommand);
    eeaEventHandler.processMessage(event);
    Mockito.verify(eeaEentCommandFactory, times(1)).getEventCommand(Mockito.any());
  }

  @Test
  public void processMessageTest3() throws EEAException {
    Mockito.when(eeaEentCommandFactory.getEventCommand(Mockito.any())).thenReturn(null);
    eeaEventHandler.processMessage(event);
    Mockito.verify(eeaEentCommandFactory, times(1)).getEventCommand(Mockito.any());
  }
}
