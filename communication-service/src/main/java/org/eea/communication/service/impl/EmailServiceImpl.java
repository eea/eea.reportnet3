package org.eea.communication.service.impl;

import org.eea.communication.service.EmailService;
import org.eea.interfaces.vo.communication.EmailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * The Class EmailServiceImpl.
 */
@Service("EmailService")
public class EmailServiceImpl implements EmailService {

  /** The email sender. */
  @Autowired
  private JavaMailSender emailSender;

  @Value("${spring.mail.active}")
  private boolean active;

  @Value("${spring.mail.username}")
  private String mailServerUsername;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

  /**
   * Send simple message.
   *
   * @param emailVO the email VO
   */
  @Override
  public void sendMessage(EmailVO emailVO) {
    try {
      if (active) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setBcc(
            emailVO.getBbc() != null ? emailVO.getBbc().stream().toArray(String[]::new) : null);
        message.setCc(
            emailVO.getCc() != null ? emailVO.getCc().stream().toArray(String[]::new) : null);
        message.setTo(
            emailVO.getTo() != null ? emailVO.getTo().stream().toArray(String[]::new) : null);
        message.setSubject(emailVO.getSubject());
        message.setText(emailVO.getText());
        message.setFrom(mailServerUsername);


        emailSender.send(message);
      } else {
        LOG.error("Mail Service is disabled in this server");
      }
    } catch (MailException exception) {
      exception.printStackTrace();
    }
  }

}
