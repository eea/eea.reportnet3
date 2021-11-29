package org.eea.communication.controller;

import static org.mockito.Mockito.times;
import org.eea.communication.service.EmailService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class EmailControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailControllerImplTest {

  /** The email controller impl. */
  @InjectMocks
  private EmailControllerImpl emailControllerImpl;

  /** The email service. */
  @Mock
  private EmailService emailService;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void sendMessageTest() {
    Mockito.doNothing().when(emailService).sendMessage(Mockito.any());
    emailControllerImpl.sendMessage(Mockito.any());
    Mockito.verify(emailService, times(1)).sendMessage(Mockito.any());
  }
}
