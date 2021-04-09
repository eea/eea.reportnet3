package org.eea.communication.configuration;


import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * The Class EmailConfiguration.
 */
@Configuration
public class EmailConfiguration {

  /** The mail server host. */
  @Value("${spring.mail.host}")
  private String mailServerHost;

  /** The mail server port. */
  @Value("${spring.mail.port}")
  private Integer mailServerPort;

  /** The mail server username. */
  @Value("${spring.mail.username}")
  private String mailServerUsername;

  /** The mail server password. */
  @Value("${spring.mail.password:}")
  private String mailServerPassword;

  /** The mail server auth. */
  @Value("${spring.mail.properties.mail.smtp.auth}")
  private String mailServerAuth;

  /** The mail server start tls. */
  @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
  private String mailServerStartTls;

  /**
   * Gets the java mail sender.
   *
   * @return the java mail sender
   */
  @Bean
  public JavaMailSender getJavaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    mailSender.setHost(mailServerHost);
    mailSender.setPort(mailServerPort);

    mailSender.setUsername(mailServerUsername);
    mailSender.setPassword(mailServerPassword);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", mailServerAuth);
    props.put("mail.smtp.starttls.enable", mailServerStartTls);
    props.put("mail.debug", "true");

    return mailSender;
  }

  /**
   * Email message source.
   *
   * @return the resource bundle message source
   */
  @Bean
  public ResourceBundleMessageSource emailMessageSource() {
    final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("mailMessages");
    return messageSource;
  }

}
