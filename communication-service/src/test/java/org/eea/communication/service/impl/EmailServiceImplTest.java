package org.eea.communication.service.impl;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.vo.communication.EmailVO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

public class EmailServiceImplTest {

  /** The email service impl. */
  @InjectMocks
  private EmailServiceImpl emailServiceImpl;

  @Mock
  private JavaMailSender emailSender;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void sendMessageActiveFalseTest() {
    EmailVO emailVO = new EmailVO();
    SimpleMailMessage message = new SimpleMailMessage();
    Mockito.doNothing().when(emailSender).send(message);
    emailServiceImpl.sendMessage(emailVO);
    Mockito.verify(emailSender, times(0)).send(message);
  }

  @Test
  public void sendMessageTest() {
    ReflectionTestUtils.setField(emailServiceImpl, "active", true);
    EmailVO emailVO = new EmailVO();
    SimpleMailMessage message = new SimpleMailMessage();
    Mockito.doNothing().when(emailSender).send(message);
    emailServiceImpl.sendMessage(emailVO);
    Mockito.verify(emailSender, times(1)).send(message);
  }

  @Test
  public void sendMessageMailExceptionTest() {
    ReflectionTestUtils.setField(emailServiceImpl, "active", true);
    EmailVO emailVO = new EmailVO();
    SimpleMailMessage message = new SimpleMailMessage();
    Mockito.doThrow(MailSendException.class).when(emailSender).send(message);
    try {
      emailServiceImpl.sendMessage(emailVO);
    } catch (MailException e) {
    }
    Mockito.verify(emailSender, times(1)).send(message);
  }

  @Test
  public void sendMessageNotNullTest() {
    ReflectionTestUtils.setField(emailServiceImpl, "active", true);
    List<String> listString = new ArrayList<>();
    listString.add("item");
    EmailVO emailVO = new EmailVO();
    emailVO.setBbc(listString);
    emailVO.setCc(listString);
    emailVO.setTo(listString);
    SimpleMailMessage message = new SimpleMailMessage();
    Mockito.doNothing().when(emailSender).send(message);
    emailServiceImpl.sendMessage(emailVO);
    message.setBcc("item");
    message.setCc("item");
    message.setTo("item");
    Mockito.verify(emailSender, times(1)).send(message);
  }
}
