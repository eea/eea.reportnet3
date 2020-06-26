package org.eea.ums.service.impl;


import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.ums.service.AccessRightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class AccessRightServiceImpl.
 */
@Service("AccessRightService")
public class AccessRightServiceImpl implements AccessRightService {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AccessRightServiceImpl.class);
  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Autowired
  private DataFlowControllerZuul dataflowControlleZuul;

  @Override
  public void deleteRoleUser(RepresentativeVO representativeVO, Long dataflowId) {
    dataflowControlleZuul.findById(dataflowId);
  }



}
