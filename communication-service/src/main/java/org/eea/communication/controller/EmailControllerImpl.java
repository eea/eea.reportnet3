package org.eea.communication.controller;

import org.eea.communication.service.EmailService;
import org.eea.interfaces.controller.communication.EmailController;
import org.eea.interfaces.vo.communication.EmailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Class EmailControllerImpl.
 */
@RestController
@RequestMapping("/email")
public class EmailControllerImpl implements EmailController {

  /** The email service. */
  @Autowired
  private EmailService emailService;

  /**
   * Send message.
   *
   * @param emailVO the email VO
   */
  @Override
  @PostMapping(value = "/private/send", produces = MediaType.APPLICATION_JSON_VALUE)
  public void sendMessage(@RequestBody EmailVO emailVO) {
    emailService.sendMessage(emailVO);
  }
}
