package org.eea.collaboration.service.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * The Class CollaborationServiceHelper.
 */
@Service
public class CollaborationServiceHelper {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The data set metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Notify new messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param eventType the event type
   */
  @Async
  public void notifyNewMessages(Long dataflowId, Long providerId, EventType eventType) {
    Collection<? extends GrantedAuthority> authorities =
        SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    Set<String> set = new HashSet<>();
    boolean direction = authorities
        .contains(new SimpleGrantedAuthority(
            ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER.getAccessRole(dataflowId)))
        || authorities.contains(new SimpleGrantedAuthority(
            ObjectAccessRoleEnum.DATAFLOW_REPORTER_READ.getAccessRole(dataflowId)))
        || authorities.contains(new SimpleGrantedAuthority(
            ObjectAccessRoleEnum.DATAFLOW_REPORTER_WRITE.getAccessRole(dataflowId)));

    if (direction) {
      String custodian = ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(dataflowId);
      String steward = ResourceGroupEnum.DATAFLOW_STEWARD.getGroupName(dataflowId);
      addUsers(set, userManagementControllerZull.getUsersByGroup(custodian));
      addUsers(set, userManagementControllerZull.getUsersByGroup(steward));
    } else {
      List<Long> datasetIds = dataSetMetabaseControllerZuul
          .getDatasetIdsByDataflowIdAndDataProviderId(dataflowId, providerId);
      for (Long datasetId : datasetIds) {
        String leadReporter = ResourceGroupEnum.DATASET_LEAD_REPORTER.getGroupName(datasetId);
        String reporterRead = ResourceGroupEnum.DATASET_REPORTER_READ.getGroupName(datasetId);
        String reporterWrite = ResourceGroupEnum.DATASET_REPORTER_WRITE.getGroupName(datasetId);
        addUsers(set, userManagementControllerZull.getUsersByGroup(leadReporter));
        addUsers(set, userManagementControllerZull.getUsersByGroup(reporterRead));
        addUsers(set, userManagementControllerZull.getUsersByGroup(reporterWrite));
      }
    }

    try {
      for (String user : set) {
        NotificationVO notificationVO = NotificationVO.builder().user(user).dataflowId(dataflowId)
            .providerId(providerId).build();
        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
      }
    } catch (EEAException e) {
      LOG_ERROR.error("Unexpected exception realasing new message notifications", e);
    }
  }

  /**
   * Adds the users.
   *
   * @param set the set
   * @param users the users
   */
  private void addUsers(Set<String> set, List<UserRepresentationVO> users) {
    if (null != users) {
      for (UserRepresentationVO user : users) {
        set.add(user.getUsername());
      }
    }
  }
}
