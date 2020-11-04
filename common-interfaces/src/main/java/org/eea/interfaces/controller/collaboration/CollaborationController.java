package org.eea.interfaces.controller.collaboration;

import org.springframework.cloud.openfeign.FeignClient;

public interface CollaborationController {

  @FeignClient(value = "collaboration", path = "/collaboration")
  interface CollaborationControllerZuul extends CollaborationController {

  }
}
