package org.eea.orchestrator.service.impl;

import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.orchestrator.service.ReleaseEmailService;
import org.eea.interfaces.controller.communication.EmailController.EmailControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.communication.EmailVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReleaseEmailServiceImpl implements ReleaseEmailService {

  private static final Logger LOG = LoggerFactory.getLogger(ReleaseEmailServiceImpl.class);

  @Autowired
  private EmailControllerZuul emailControllerZuul;

  @Autowired
  private UserManagementControllerZull userManagementControllerZuul;

  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  @Override
  public void sendReleaseFailedEmail(Long dataflowId, Long providerId, String reason) {
    try {
      DataFlowVO dataflowVO = dataFlowControllerZuul.getMetabaseById(dataflowId);

      String providerName = "";
      if (providerId != null) {
        DataProviderVO provider = representativeControllerZuul.findDataProviderById(providerId);
        providerName = provider.getLabel();
      }

      String date = ZonedDateTime.now(ZoneId.of(LiteralConstants.EUROPE_ZONE_ID))
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      List<UserRepresentationVO> custodians = userManagementControllerZuul.getUsersByGroup(
          ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(dataflowId));
      List<String> emails = new ArrayList<>();

      if (custodians != null) {
        custodians.forEach(custodian -> emails.add(custodian.getEmail()));
      }

      EmailVO emailVO = new EmailVO();
      emailVO.setBbc(emails);
      emailVO.setSubject(String.format(LiteralConstants.RELEASE_FAILED_SUBJECT, providerName, dataflowVO.getName()));
      emailVO.setText(String.format(LiteralConstants.RELEASE_FAILED_MESSAGE, providerName, dataflowVO.getName(), date, reason));
      emailControllerZuul.sendMessage(emailVO);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error sending release failed mail for dataflowId {}. Message: {}",
          dataflowId, e.getMessage());
    }
  }

}

