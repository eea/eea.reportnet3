package org.eea.interfaces.controller.collaboration;

import org.eea.interfaces.vo.dataflow.MessageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface CollaborationController {

  @FeignClient(value = "collaboration", path = "/collaboration")
  interface CollaborationControllerZuul extends CollaborationController {

  }

  @PostMapping("/{dataflowId}/createMessage")
  MessageVO createMessage(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody MessageVO messageVO);
}
