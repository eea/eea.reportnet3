package org.eea.interfaces.controller.communication;

import org.eea.interfaces.vo.communication.EmailVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The Interface EmailController.
 */
public interface EmailController {


  /**
   * The Interface ContributorControllerZuul.
   */
  @FeignClient(value = "communication", contextId = "email", path = "/email")
  interface EmailControllerZuul extends EmailController {

  }

  /**
   * Send message.
   *
   * @param emailVO the email VO
   */
  @PostMapping(value = "/private/send", produces = MediaType.APPLICATION_JSON_VALUE)
  void sendMessage(@RequestBody EmailVO emailVO);

}
