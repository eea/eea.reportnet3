package org.eea.communication.service;

import org.eea.interfaces.vo.communication.EmailVO;

/**
 * The Interface EmailService.
 */
public interface EmailService {


  /**
   * Send simple message.
   *
   * @param to the to
   * @param subject the subject
   * @param text the text
   */
  void sendMessage(EmailVO emailVO);


}
