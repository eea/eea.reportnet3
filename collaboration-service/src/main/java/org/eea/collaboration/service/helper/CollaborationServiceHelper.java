package org.eea.collaboration.service.helper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.EmailController.EmailControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.communication.EmailVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.utils.LiteralConstants;
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

  /** The data flow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  /** The email controller zuul. */
  @Autowired
  private EmailControllerZuul emailControllerZuul;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  private static final Logger LOG = LoggerFactory.getLogger(CollaborationServiceHelper.class);

  /**
   * Notify new messages.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param custodianUserName the custodian username
   * @param modifiedDatasetId the modified dataset id
   * @param datasetStatus the dataset status
   * @param datasetName the dataset name
   * @param eventType the event type
   */
  @Async
  public void notifyNewMessages(Long dataflowId, Long providerId, String custodianUserName,
                                Long modifiedDatasetId, DatasetStatusEnum datasetStatus,
                                String datasetName, String eventType) {
    if (dataflowId == null || eventType == null) {
      LOG_ERROR.warn("Missing required parameters for notifyNewMessages: dataflowId or eventType");
      return;
    }

    EventType event = EventType.valueOf(eventType);
    String dataflowName = dataFlowControllerZuul.getMetabaseById(dataflowId).getName();
    Map<String, Set<String>> notificationSets = buildUserAndEmailSets(dataflowId, providerId, custodianUserName);
    Set<String> usernamesSet = notificationSets.get("usernamesSet");

    for (String user : usernamesSet) {
      try {
        NotificationVO notificationVO = NotificationVO.builder()
                .user(user)
                .dataflowId(dataflowId)
                .datasetStatus(datasetStatus)
                .datasetId(modifiedDatasetId)
                .dataflowName(dataflowName)
                .datasetName(datasetName)
                .providerId(providerId)
                .build();
        kafkaSenderUtils.releaseNotificableKafkaEvent(event, null, notificationVO);
      } catch (EEAException e) {
        LOG_ERROR.error("Failed to notify user {} for dataflowId {} and eventType {}. Message: {}",
                user, dataflowId, eventType, e.getMessage(), e);
      }
    }
  }

  /**
   * Builds the user set for notification.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param custodianUserName the custodian username
   */
  @Async
  public void emailNewMessages(Long dataflowId, Long providerId, String custodianUserName, String eventType, String messageContent, Date messageCreateDate) {
    try {
      Map<String, Set<String>> notificationSets = buildUserAndEmailSets(dataflowId, providerId, custodianUserName);
      Set<String> emailSet = notificationSets.get("emailSet");
      DataProviderVO dataProviderVO = representativeControllerZuul.findDataProviderById(providerId);
      String providerLabel = dataProviderVO.getLabel();
      String dataflowName = dataFlowControllerZuul.getMetabaseById(dataflowId).getName();
      sendMail(emailSet, providerLabel, dataflowId, dataflowName, messageContent, messageCreateDate);
    } catch (Exception e) {
      LOG_ERROR.error("Error in emailNewMessages for dataflowId {} and eventType {}: {}", dataflowId, eventType, e.getMessage(), e);
    }
  }

  /**
   * Builds the user set for notification.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param custodianUserName the custodian username
   * @return the set of user names and emails for notifications
   */
  private Map<String, Set<String>> buildUserAndEmailSets(Long dataflowId, Long providerId, String custodianUserName) {
    Set<String> usernamesSet = new HashSet<>();
    Set<String> emailSet = new HashSet<>();

    Collection<? extends GrantedAuthority> authorities =
            SecurityContextHolder.getContext().getAuthentication().getAuthorities();
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
      addUsers(usernamesSet, emailSet, userManagementControllerZull.getUsersByGroup(custodian));
      addUsers(usernamesSet, emailSet, userManagementControllerZull.getUsersByGroup(steward));
    } else {
      List<Long> datasetIds = dataSetMetabaseControllerZuul
              .getDatasetIdsByDataflowIdAndDataProviderId(dataflowId, providerId);
      for (Long datasetId : datasetIds) {
        String leadReporter = ResourceGroupEnum.DATASET_LEAD_REPORTER.getGroupName(datasetId);
        String reporterRead = ResourceGroupEnum.DATASET_REPORTER_READ.getGroupName(datasetId);
        String reporterWrite = ResourceGroupEnum.DATASET_REPORTER_WRITE.getGroupName(datasetId);
        addUsers(usernamesSet, emailSet, userManagementControllerZull.getUsersByGroup(leadReporter));
        addUsers(usernamesSet, emailSet, userManagementControllerZull.getUsersByGroup(reporterRead));
        addUsers(usernamesSet, emailSet, userManagementControllerZull.getUsersByGroup(reporterWrite));
      }
    }

    if (custodianUserName != null) {
      usernamesSet.add(custodianUserName);
    }

    Map<String, Set<String>> notificationSets = new HashMap<>();
    notificationSets.put("usernamesSet", usernamesSet);
    notificationSets.put("emailSet", emailSet);
    return notificationSets;
  }

  /**
   * Helper method to send the email.
   *
   * @param emailSet the set of email addresses to notify
   * @param dataflowId the dataflow ID
   * @param dataflowName the dataflow name
   */
  private void sendMail(Set<String> emailSet, String providerLabel, Long dataflowId, String dataflowName, String messageContent, Date messageCreateDate) {
    try {
      ZonedDateTime cetTime = messageCreateDate.toInstant().atZone(ZoneId.of(LiteralConstants.EUROPE_ZONE_ID));
      DateTimeFormatter cetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      String cetFormattedDate = cetTime.format(cetFormatter);
      String emailText = String.format(
              LiteralConstants.TECH_ACCEPT_MESSAGE , dataflowName, cetFormattedDate, messageContent);

      EmailVO emailVO = new EmailVO();
      emailVO.setBbc(new ArrayList<>(emailSet));
      emailVO.setSubject(String.format(LiteralConstants.TECH_ACCEPT_MESSAGE_SUBJECT , providerLabel, dataflowName));
      emailVO.setText(emailText);
      emailControllerZuul.sendMessage(emailVO);
    } catch (Exception e) {
      LOG_ERROR.error("Failed to send email notifications for dataflowId {}: {}", dataflowId, e.getMessage(), e);
    }
  }

  /**
   * Helper method to add both usernames and emails to their respective sets.
   *
   * @param usernamesSet the set of usernames to notify
   * @param emailSet the set of email addresses to notify
   * @param users the list of UserRepresentationVO objects
   */
  private void addUsers(Set<String> usernamesSet, Set<String> emailSet, List<UserRepresentationVO> users) {
    if (users != null) {
      for (UserRepresentationVO user : users) {
        usernamesSet.add(user.getUsername());
        emailSet.add(user.getEmail());
      }
    }
  }
}