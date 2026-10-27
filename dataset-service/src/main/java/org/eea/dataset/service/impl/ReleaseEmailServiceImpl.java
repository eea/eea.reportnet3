package org.eea.dataset.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eea.dataset.service.ReleaseEmailService;
import org.eea.interfaces.controller.communication.EmailController.EmailControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.communication.EmailVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
  public void sendReleaseFinishedEmail(String dateRelease, DataSetMetabaseVO dataset,
                                       DataFlowVO dataflowVO) {
    try {
      // get custodian and stewards emails
      List<UserRepresentationVO> custodians = userManagementControllerZuul.getUsersByGroup(
          ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(dataflowVO.getId()));
      List<UserRepresentationVO> stewards = userManagementControllerZuul.getUsersByGroup(
          ResourceGroupEnum.DATAFLOW_STEWARD.getGroupName(dataflowVO.getId()));
      List<UserRepresentationVO> observers = userManagementControllerZuul.getUsersByGroup(
          ResourceGroupEnum.DATAFLOW_OBSERVER.getGroupName(dataflowVO.getId()));
      List<UserRepresentationVO> custodianSupport = userManagementControllerZuul.getUsersByGroup(
          ResourceGroupEnum.DATAFLOW_STEWARD_SUPPORT.getGroupName(dataflowVO.getId()));
      List<String> emails = new ArrayList<>();

      if (null != custodians) {
        custodians.forEach(custodian -> emails.add(custodian.getEmail()));
      }

      if (null != stewards) {
        stewards.forEach(steward -> emails.add(steward.getEmail()));
      }

      if (null != observers) {
        observers.forEach(observer -> emails.add(observer.getEmail()));
      }

      if (null != custodianSupport) {
        custodianSupport.forEach(support -> emails.add(support.getEmail()));
      }

      EmailVO emailVO = new EmailVO();
      emailVO.setBbc(emails);
      emailVO.setSubject(String.format(LiteralConstants.RELEASESUBJECT, dataset.getDataSetName(), dataflowVO.getName()));

      // force date description to CET
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      LocalDateTime utcDateTime = LocalDateTime.parse(dateRelease, formatter);
      ZonedDateTime utcZoned = utcDateTime.atZone(ZoneOffset.UTC);
      ZonedDateTime cetZoned = utcZoned.withZoneSameInstant(ZoneId.of(LiteralConstants.EUROPE_ZONE_ID));
      String cetDate = cetZoned.format(formatter);
      emailVO.setText(String.format(LiteralConstants.RELEASEMESSAGE, dataset.getDataSetName(), dataflowVO.getName(), cetDate));
      emailControllerZuul.sendMessage(emailVO);
    } catch (Exception e) {
      Long dataflowId = dataflowVO != null ? dataflowVO.getId() : null;
      Long datasetId = dataset != null ? dataset.getId() : null;
      LOG.error("Unexpected error! Error sending release finished mail for dataflowId {} and datasetId {}. Message: {}",
          dataflowId, datasetId, e.getMessage());
    }
  }

  @Override
  public void sendReleaseStartedEmail(Long dataflowId, Long providerId) {
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
          ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(dataflowVO.getId()));
      List<String> emails = new ArrayList<>();

      if (custodians != null) {
        custodians.forEach(custodian -> emails.add(custodian.getEmail()));
      }

      EmailVO emailVO = new EmailVO();
      emailVO.setBbc(emails);
      emailVO.setSubject(String.format(LiteralConstants.RELEASE_STARTED_SUBJECT, providerName, dataflowVO.getName()));
      emailVO.setText(String.format(LiteralConstants.RELEASE_STARTED_MESSAGE, providerName, dataflowVO.getName(), date));
      emailControllerZuul.sendMessage(emailVO);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error sending release started mail for dataflowId {}. Message: {}",
          dataflowId, e.getMessage());
    }
  }

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
