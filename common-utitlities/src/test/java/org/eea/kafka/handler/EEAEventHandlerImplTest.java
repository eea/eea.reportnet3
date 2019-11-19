package org.eea.kafka.handler;

import static org.junit.Assert.*;

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

  @InjectMocks
  private EEAEventHandlerImpl eeaEventHandler;
  @Mock
  private EEAEventCommandFactory eeaEentCommandFactory;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets type.
   */
  @Test
  public void getType() {
    Assert.assertEquals(eeaEventHandler.getType(), EEAEventVO.class);
  }

  /**
   * Process message.
   */
  @Test
  public void processMessage() throws EEAException {
    EEAEventHandlerCommand eeaEventHandlerCommand = Mockito.mock(EEAEventHandlerCommand.class);
    Mockito.when(eeaEentCommandFactory.getEventCommand(Mockito.any(EEAEventVO.class)))
        .thenReturn(eeaEventHandlerCommand);
    EEAEventVO vo = new EEAEventVO();
    eeaEventHandler.processMessage(vo);
    Mockito.verify(eeaEentCommandFactory, Mockito.times(1)).getEventCommand(vo);
    Mockito.verify(eeaEventHandlerCommand, Mockito.times(1)).execute(vo);
  }

  /**
   * Process message.
   */
  @Test(expected = EEAException.class)
  public void processError() throws EEAException {
    EEAEventHandlerCommand eeaEventHandlerCommand = Mockito.mock(EEAEventHandlerCommand.class);
    Mockito.doThrow(new EEAException("test")).when(eeaEventHandlerCommand)
        .execute(Mockito.any(EEAEventVO.class));
    Mockito.when(eeaEentCommandFactory.getEventCommand(Mockito.any(EEAEventVO.class)))
        .thenReturn(eeaEventHandlerCommand);
    EEAEventVO vo = new EEAEventVO();
    try {
      eeaEventHandler.processMessage(vo);
    } catch (EEAException e) {
      Mockito.verify(eeaEentCommandFactory, Mockito.times(1)).getEventCommand(vo);
      Mockito.verify(eeaEventHandlerCommand, Mockito.times(1)).execute(vo);
      Assert.assertEquals("Wrong exception caught", e.getMessage(), "test");
      throw e;
    }

  }
}