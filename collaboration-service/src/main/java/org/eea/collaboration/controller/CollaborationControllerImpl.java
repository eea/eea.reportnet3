package org.eea.collaboration.controller;

import org.eea.collaboration.service.CollaborationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.collaboration.CollaborationController;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/collaboration")
public class CollaborationControllerImpl implements CollaborationController {

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Autowired
  private CollaborationService collaborationService;

  @Override
  @PostMapping("/{dataflowId}/createMessage")
  @PreAuthorize("secondLevelAuthorize(#dataflowId, 'DATAFLOW_STEWARD', 'DATAFLOW_CUSTODIAN','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_REPORTER_READ', 'DATAFLOW_REPORTER_WRITE')")
  public MessageVO createMessage(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody MessageVO messageVO) {
    try {
      return collaborationService.createMessage(dataflowId, messageVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
    }
  }
}
