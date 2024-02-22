package org.eea.ums.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.UserNationalCoordinatorVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.UserNationalCoordinatorService;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The Class UserNationalCoordinatorServiceImpl.
 */
@Service("UserNationalCoordinatorService")

public class UserNationalCoordinatorServiceImpl implements UserNationalCoordinatorService {

  /** The Constant EMAIL_REGEX: {@value}. */
  private static final String EMAIL_REGEX =
      "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"; // NOSONAR

  /** The keycloak connector service. */
  @Autowired
  private KeycloakConnectorService keycloakConnectorService;

  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The dataflow controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;


  /** The security provider interface service. */
  @Autowired
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Gets the national coordinators.
   *
   * @return the national coordinators
   */
  @Override
  public List<UserNationalCoordinatorVO> getNationalCoordinators() {
    GroupInfo[] groupInfo =
        keycloakConnectorService.getGroupsWithSearch("Provider-%-NATIONAL_COORDINATOR");
    List<UserNationalCoordinatorVO> usersNC = new ArrayList<>();
    if (groupInfo != null && groupInfo.length != 0) {
      for (int i = 0; i < groupInfo.length; i++) {
        List<UserRepresentation> users =
            Arrays.asList(keycloakConnectorService.getUsersByGroupId(groupInfo[i].getId()));
        for (UserRepresentation userRepresentation : users) {
          UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
          userNC.setEmail(userRepresentation.getEmail());
          userNC.setCountryCode(getCountry(groupInfo[i].getName()));
          usersNC.add(userNC);
        }
      }
    }
    return usersNC;
  }


  /**
   * Creates the national coordinator.
   *
   * @param userNationalCoordinatorVO the user national coordinator VO
   * @throws EEAException
   */
  @Override
  @Async
  public void createNationalCoordinator(UserNationalCoordinatorVO userNationalCoordinatorVO)
      throws EEAException {
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName()).build();

    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.NATIONAL_COORDINATOR_ADDING_PROCESS_STARTED_EVENT, null, notificationVO);

    checkUser(userNationalCoordinatorVO);

    // check Country
    List<DataProviderVO> providers = representativeControllerZuul
        .findDataProvidersByCode(userNationalCoordinatorVO.getCountryCode());
    if (CollectionUtils.isEmpty(providers)) {
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.ADDING_NATIONAL_COORDINATOR_FAILED_EVENT, null, notificationVO);
      throw new EEAException(EEAErrorMessage.COUNTRY_CODE_NOTFOUND);
    }

    try {

      // create country group
      keycloakConnectorService
          .createGroupDetail(getNationalCoordinatorGroup(userNationalCoordinatorVO));
      securityProviderInterfaceService.addContributorToUserGroup(Optional.empty(),
          userNationalCoordinatorVO.getEmail(), ResourceGroupEnum.PROVIDER_NATIONAL_COORDINATOR
              .getGroupName(userNationalCoordinatorVO.getCountryCode()));
      // datasets in this country
      List<ResourceAssignationVO> resourcesForNC =
          getResourcesForNCAndCreate(userNationalCoordinatorVO, providers, Boolean.TRUE);

      // finally add all permissions
      securityProviderInterfaceService.addContributorsToUserGroup(resourcesForNC);

      // sent notification
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.ADDING_NATIONAL_COORDINATOR_FINISHED_EVENT, null, notificationVO);

    } catch (Exception e) {
      // sent notification
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.ADDING_NATIONAL_COORDINATOR_FAILED_EVENT, null, notificationVO);
      throw new EEAException(EEAErrorMessage.PERMISSION_NOT_CREATED);
    }
  }

  /**
   * Delete national coordinator.
   *
   * @param userNationalCoordinatorVO the user national coordinator VO
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void deleteNationalCoordinator(UserNationalCoordinatorVO userNationalCoordinatorVO)
      throws EEAException {
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName()).build();

    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.NATIONAL_COORDINATOR_DELETING_PROCESS_STARTED_EVENT, null, notificationVO);

    checkUser(userNationalCoordinatorVO);

    // check Country
    List<DataProviderVO> providers = representativeControllerZuul
        .findDataProvidersByCode(userNationalCoordinatorVO.getCountryCode());
    if (CollectionUtils.isEmpty(providers)) {
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETING_NATIONAL_COORDINATOR_FAILED_EVENT, null, notificationVO);
      throw new EEAException(EEAErrorMessage.COUNTRY_CODE_NOTFOUND);
    }

    try {

      // remove country group permission
      securityProviderInterfaceService.removeContributorFromUserGroup(Optional.empty(),
          userNationalCoordinatorVO.getEmail(), ResourceGroupEnum.PROVIDER_NATIONAL_COORDINATOR
              .getGroupName(userNationalCoordinatorVO.getCountryCode()));
      // datasets in this country
      List<ResourceAssignationVO> resourcesForNC =
          getResourcesForNCAndCreate(userNationalCoordinatorVO, providers, Boolean.FALSE);

      // finally add all permissions
      securityProviderInterfaceService.removeContributorsFromUserGroup(resourcesForNC);

      // sent notification
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETING_NATIONAL_COORDINATOR_FINISHED_EVENT, null, notificationVO);

    } catch (Exception e) {
      // sent notification
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETING_NATIONAL_COORDINATOR_FAILED_EVENT, null, notificationVO);
      throw new EEAException(EEAErrorMessage.PERMISSION_NOT_REMOVED);
    }
  }



  /**
   * Gets the resources for NC or create.
   *
   * @param userNationalCoordinatorVO the user national coordinator VO
   * @param providers the providers
   * @param create the create
   * @return the resources for NC or create
   * @throws EEAException the EEA exception
   */
  private List<ResourceAssignationVO> getResourcesForNCAndCreate(
      UserNationalCoordinatorVO userNationalCoordinatorVO, List<DataProviderVO> providers,
      boolean create) throws EEAException {
    List<Long> providerIds =
        providers.stream().map(DataProviderVO::getId).collect(Collectors.toList());
    List<DataSetMetabaseVO> reportings =
        datasetMetabaseControllerZuul.findReportingDataSetByProviderIds(providerIds);
    List<ResourceAssignationVO> resourcesForDataProvider = new ArrayList<>();
    HashSet<Long> dataflowIds = new HashSet<Long>();
    for (DataSetMetabaseVO reportingDatasetVO : reportings) {
      if (create) {
        securityProviderInterfaceService
            .createResourceInstance(createGroup(reportingDatasetVO.getId(),
                ResourceTypeEnum.DATASET, SecurityRoleEnum.NATIONAL_COORDINATOR));
      }
      resourcesForDataProvider.add(fillResourceAssignation(reportingDatasetVO.getId(),
          userNationalCoordinatorVO.getEmail(), ResourceGroupEnum.DATASET_NATIONAL_COORDINATOR));
      dataflowIds.add(reportingDatasetVO.getDataflowId());
    }
    // dataflow permissions
    for (Long id : dataflowIds) {
      if (create) {
        securityProviderInterfaceService.createResourceInstance(
            createGroup(id, ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.NATIONAL_COORDINATOR));
      }
      resourcesForDataProvider.add(fillResourceAssignation(id, userNationalCoordinatorVO.getEmail(),
          ResourceGroupEnum.DATAFLOW_NATIONAL_COORDINATOR));
    }
    return resourcesForDataProvider;
  }


  /**
   * Check user.
   *
   * @param userNationalCoordinatorVO the user national coordinator VO
   * @throws EEAException the EEA exception
   */
  private void checkUser(UserNationalCoordinatorVO userNationalCoordinatorVO) throws EEAException {
    if (null == userNationalCoordinatorVO || null == userNationalCoordinatorVO.getEmail()) {
      throw new EEAException(EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
    userNationalCoordinatorVO.setEmail(userNationalCoordinatorVO.getEmail().toLowerCase());
    Pattern p = Pattern.compile(EMAIL_REGEX);
    Matcher m = p.matcher(userNationalCoordinatorVO.getEmail().toLowerCase());
    boolean result = m.matches();
    if (!result) {
      throw new EEAException(
          String.format(EEAErrorMessage.NOT_EMAIL, userNationalCoordinatorVO.getEmail()));
    }

    UserRepresentation[] user =
        keycloakConnectorService.getUsersByEmail(userNationalCoordinatorVO.getEmail());
    if (user == null || user.length < 1
        || StringUtils.isBlank(userNationalCoordinatorVO.getEmail())) {
      throw new EEAException(
          String.format(EEAErrorMessage.USER_NOTFOUND, userNationalCoordinatorVO.getEmail()));
    }
  }


  /**
   * Gets the national coordinator group.
   *
   * @param userNationalCoordinatorVO the user national coordinator VO
   * @return the national coordinator group
   */
  private GroupInfo getNationalCoordinatorGroup(
      UserNationalCoordinatorVO userNationalCoordinatorVO) {
    GroupInfo groupInfo = new GroupInfo();
    String groupName = ResourceGroupEnum.PROVIDER_NATIONAL_COORDINATOR
        .getGroupName(userNationalCoordinatorVO.getCountryCode());
    groupInfo.setName(groupName);
    groupInfo.setPath("/" + groupName);
    return groupInfo;
  }

  /**
   * Gets the country.
   *
   * @param groupName the group name
   * @return the country
   */
  private String getCountry(String groupName) {
    String country = null;
    if (StringUtils.isNotBlank(groupName)) {
      List<String> groupList = Arrays.asList(groupName.split("-"));
      if (CollectionUtils.isNotEmpty(groupList)) {
        country = groupList.get(1);
      }
    }
    return country;
  }

  /**
   * Creates the group.
   *
   * @param datasetId the dataset id
   * @param type the type
   * @param role the role
   * @return the resource info VO
   */
  private ResourceInfoVO createGroup(Long datasetId, ResourceTypeEnum type, SecurityRoleEnum role) {
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(datasetId);
    resourceInfoVO.setResourceTypeEnum(type);
    resourceInfoVO.setSecurityRoleEnum(role);
    resourceInfoVO.setName(type + "-" + datasetId + "-" + role);
    return resourceInfoVO;
  }

  /**
   * Fill resource assignation.
   *
   * @param id the id
   * @param email the email
   * @param group the group
   * @return the resource assignation VO
   */
  private ResourceAssignationVO fillResourceAssignation(Long id, String email,
      ResourceGroupEnum group) {
    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setResourceId(id);
    resource.setEmail(email);
    resource.setResourceGroup(group);
    return resource;
  }

}


