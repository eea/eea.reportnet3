package org.eea.communication.service;

import org.eea.interfaces.vo.communication.EmailVO;

/**
 * The Interface EmailService.
 */
public interface EmailService {


  /**
   * Send simple message.
   *
   * @param emailVO the email VO
   */
  void sendMessage(EmailVO emailVO);


}
