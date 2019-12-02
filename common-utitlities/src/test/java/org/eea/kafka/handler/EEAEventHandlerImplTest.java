package org.eea.kafka.handler;

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

  /** The eea event handler. */
  @InjectMocks
  private EEAEventHandlerImpl eeaEventHandler;

  /** The eea eent command factory. */
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
   *
   * @return the type
   */
  @Test
  public void getType() {
    Assert.assertEquals(eeaEventHandler.getType(), EEAEventVO.class);
  }

  /**
   * Process message.
   *
   * @throws EEAException the EEA exception
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
   *
   * @throws EEAException the EEA exception
   */
  @Test
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
      Assert.assertEquals("Wrong exception caught", "test", e.getMessage());
    }
  }


  /**
   * Process error 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void processError2() throws EEAException {
    EEAEventHandlerCommand eeaEventHandlerCommand = Mockito.mock(EEAEventHandlerCommand.class);
    Mockito.doThrow(new EEAException("test")).when(eeaEventHandlerCommand)
        .execute(Mockito.any(EEAEventVO.class));
    Mockito.when(eeaEentCommandFactory.getEventCommand(Mockito.any(EEAEventVO.class)))
        .thenReturn(null);
    EEAEventVO vo = new EEAEventVO();
    eeaEventHandler.processMessage(vo);
    Mockito.verify(eeaEventHandlerCommand, Mockito.times(0)).execute(vo);
  }
}
