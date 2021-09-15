package org.eea.ums.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.ums.UserRoleVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.ums.service.UserRoleService;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * The Class UserRoleServiceImpl.
 */
@Service("UserRoleService")
public class UserRoleServiceImpl implements UserRoleService {


  /** The keycloak connector service. */
  @Autowired
  private KeycloakConnectorService keycloakConnectorService;

  /** The dataflow controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /**
   * Gets the user roles by dataflow country.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the user roles by dataflow country
   */
  @Override
  public List<UserRoleVO> getUserRolesByDataflowCountry(Long dataflowId, Long dataProviderId) {

    Map<String, List<GroupInfo>> groupInfoMap = new HashMap<>();
    List<UserRoleVO> finalList = new ArrayList<>();

    List<Long> datasetIds = datasetMetabaseControllerZuul
        .getDatasetIdsByDataflowIdAndDataProviderId(dataflowId, dataProviderId);
    if (null != datasetIds && !datasetIds.isEmpty()) {
      getGroupInfoMap(groupInfoMap, datasetIds.get(0));

      // CUSTODIAN
      getUsersRolesByGroup(groupInfoMap, finalList, SecurityRoleEnum.DATA_CUSTODIAN.toString());
      // LEAD REPORTER
      getUsersRolesByGroup(groupInfoMap, finalList, SecurityRoleEnum.LEAD_REPORTER.toString());
      // NATIONAL COORDINATOR
      getUsersRolesByGroup(groupInfoMap, finalList,
          SecurityRoleEnum.NATIONAL_COORDINATOR.toString());
      // REPORTER READ
      getUsersRolesByGroup(groupInfoMap, finalList, SecurityRoleEnum.REPORTER_READ.toString());
      // REPORTER WRITE
      getUsersRolesByGroup(groupInfoMap, finalList, SecurityRoleEnum.REPORTER_WRITE.toString());
      // STEWARD
      getUsersRolesByGroup(groupInfoMap, finalList, SecurityRoleEnum.DATA_STEWARD.toString());
      // OBSERVER
      getUsersRolesByGroup(groupInfoMap, finalList, SecurityRoleEnum.DATA_OBSERVER.toString());

    }
    return finalList;
  }


  /**
   * Gets the group info map.
   *
   * @param groupInfoMap the group info map
   * @param datasetId the dataset Id
   * @return the group info map
   */
  private void getGroupInfoMap(Map<String, List<GroupInfo>> groupInfoMap, Long datasetId) {

    Collection<String> authorities = SecurityContextHolder.getContext().getAuthentication()
        .getAuthorities().stream().map(authority -> ((GrantedAuthority) authority).getAuthority())
        .collect(Collectors.toList());
    if (authorities.contains(ObjectAccessRoleEnum.DATASET_CUSTODIAN.getAccessRole(datasetId))
        || authorities.contains(ObjectAccessRoleEnum.DATASET_STEWARD.getAccessRole(datasetId))
        || authorities.contains(ObjectAccessRoleEnum.DATASET_OBSERVER.getAccessRole(datasetId))) {
      // CUSTODIAN
      setGroupsIntoMap(groupInfoMap,
          new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
              .getGroupsWithSearch(ResourceGroupEnum.DATASET_CUSTODIAN.getGroupName(datasetId)))),
          SecurityRoleEnum.DATA_CUSTODIAN.toString());
      // STEWARD
      setGroupsIntoMap(groupInfoMap,
          new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
              .getGroupsWithSearch(ResourceGroupEnum.DATASET_STEWARD.getGroupName(datasetId)))),
          SecurityRoleEnum.DATA_STEWARD.toString());
      // OBSERVER
      setGroupsIntoMap(groupInfoMap,
          new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
              .getGroupsWithSearch(ResourceGroupEnum.DATASET_OBSERVER.getGroupName(datasetId)))),
          SecurityRoleEnum.DATA_OBSERVER.toString());
    }
    if (authorities.contains(ObjectAccessRoleEnum.DATASET_CUSTODIAN.getAccessRole(datasetId))
        || authorities.contains(ObjectAccessRoleEnum.DATASET_STEWARD.getAccessRole(datasetId))
        || authorities
            .contains(ObjectAccessRoleEnum.DATASET_NATIONAL_COORDINATOR.getAccessRole(datasetId))) {
      // NATIONAL COORDINATOR
      setGroupsIntoMap(groupInfoMap,
          new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService.getGroupsWithSearch(
              ResourceGroupEnum.DATASET_NATIONAL_COORDINATOR.getGroupName(datasetId)))),
          SecurityRoleEnum.NATIONAL_COORDINATOR.toString());
    }
    // REPORTERS
    setGroupsIntoMap(groupInfoMap,
        new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
            .getGroupsWithSearch(ResourceGroupEnum.DATASET_LEAD_REPORTER.getGroupName(datasetId)))),
        SecurityRoleEnum.LEAD_REPORTER.toString());
    setGroupsIntoMap(groupInfoMap,
        new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
            .getGroupsWithSearch(ResourceGroupEnum.DATASET_REPORTER_READ.getGroupName(datasetId)))),
        SecurityRoleEnum.REPORTER_READ.toString());
    setGroupsIntoMap(groupInfoMap,
        new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService.getGroupsWithSearch(
            ResourceGroupEnum.DATASET_REPORTER_WRITE.getGroupName(datasetId)))),
        SecurityRoleEnum.REPORTER_WRITE.toString());

  }

  /**
   * Sets the groups into map.
   *
   * @param groupInfoMap the group info map
   * @param groupInfoValue the group info value
   * @param group the group
   */
  private void setGroupsIntoMap(Map<String, List<GroupInfo>> groupInfoMap,
      List<GroupInfo> groupInfoValue, String group) {
    if (null != groupInfoMap.get(group)) {
      groupInfoValue.addAll(groupInfoMap.get(group));
    }
    groupInfoMap.put(group, groupInfoValue);
  }

  /**
   * Gets the users roles by group.
   *
   * @param groupInfoMap the group info map
   * @param usersList the users list
   * @param group the group
   * @return the users roles by group
   */
  private void getUsersRolesByGroup(Map<String, List<GroupInfo>> groupInfoMap,
      List<UserRoleVO> usersList, String group) {
    if (null != groupInfoMap.get(group) && !groupInfoMap.get(group).isEmpty()) {
      for (GroupInfo groupInfo : groupInfoMap.get(group)) {
        groupInfo.getId();
        UserRepresentation[] users = keycloakConnectorService.getUsersByGroupId(groupInfo.getId());
        for (int i = 0; i < users.length; i++) {
          UserRoleVO userRol = new UserRoleVO();
          List<String> roles = new ArrayList<>();
          roles.add(group);
          userRol.setEmail(users[i].getEmail());
          userRol.setRoles(roles);
          usersList.add(userRol);
        }
      }
    }
  }

  /**
   * Gets the user roles by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the user roles by dataflow
   */
  @Override
  public List<UserRoleVO> getUserRolesByDataflow(Long dataflowId) {
    List<UserRoleVO> userRoleList = new ArrayList<>();
    HashMap<Long, String> providerIds = new HashMap<>();

    getLeadReportersWithCountry(dataflowId, userRoleList, providerIds);
    getUsersWithCountry(dataflowId, userRoleList, providerIds);
    getUserRole(ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(dataflowId),
        SecurityRoleEnum.DATA_CUSTODIAN.toString(), userRoleList, null);
    getUserRole(ResourceGroupEnum.DATAFLOW_STEWARD.getGroupName(dataflowId),
        SecurityRoleEnum.DATA_STEWARD.toString(), userRoleList, null);
    getUserRole(ResourceGroupEnum.DATAFLOW_OBSERVER.getGroupName(dataflowId),
        SecurityRoleEnum.DATA_OBSERVER.toString(), userRoleList, null);
    getUserRole(ResourceGroupEnum.DATAFLOW_EDITOR_READ.getGroupName(dataflowId),
        SecurityRoleEnum.EDITOR_READ.toString(), userRoleList, null);
    getUserRole(ResourceGroupEnum.DATAFLOW_EDITOR_WRITE.getGroupName(dataflowId),
        SecurityRoleEnum.EDITOR_WRITE.toString(), userRoleList, null);
    return userRoleList;
  }


  /**
   * Gets the lead reporters with country.
   *
   * @param dataflowId the dataflow id
   * @param userRoleList the user role list
   * @param providerIds the provider ids
   * @return the lead reporters with country
   */
  private void getLeadReportersWithCountry(Long dataflowId, List<UserRoleVO> userRoleList,
      HashMap<Long, String> providerIds) {
    List<RepresentativeVO> representatives =
        representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowId);
    if (!CollectionUtils.isEmpty(representatives)) {
      List<Long> dataproviderIds = representatives.stream()
          .map(representative -> representative.getDataProviderId()).collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(dataproviderIds)) {
        List<DataProviderVO> dataproviders =
            representativeControllerZuul.findDataProvidersByIds(dataproviderIds);
        for (DataProviderVO dataProviderVO : dataproviders) {
          providerIds.put(dataProviderVO.getId(), dataProviderVO.getLabel());
        }
      }

      for (RepresentativeVO representative : representatives) {
        if (null != representative.getLeadReporters()) {
          for (LeadReporterVO leadReporter : representative.getLeadReporters()) {
            UserRoleVO userRoleVO = new UserRoleVO();
            userRoleVO.setEmail(leadReporter.getEmail());
            userRoleVO.setRoles(Arrays.asList(SecurityRoleEnum.LEAD_REPORTER.toString()));
            userRoleVO.setDatasetProviderName(providerIds.get(representative.getDataProviderId()));
            userRoleList.add(userRoleVO);
          }
        }
      }
    }
  }

  /**
   * Gets the users with country.
   *
   * @param dataflowId the dataflow id
   * @param userRoleList the user role list
   * @param providerIds the provider ids
   * @return the users with country
   */
  private void getUsersWithCountry(Long dataflowId, List<UserRoleVO> userRoleList,
      HashMap<Long, String> providerIds) {
    List<ReportingDatasetVO> datasets =
        datasetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId);
    if (null != datasets) {
      for (ReportingDatasetVO reportingDatasetVO : datasets) {
        if (providerIds.containsKey(reportingDatasetVO.getDataProviderId())) {
          getUserRole(
              ResourceGroupEnum.DATASET_REPORTER_READ.getGroupName(reportingDatasetVO.getId()),
              SecurityRoleEnum.REPORTER_READ.toString(), userRoleList,
              providerIds.get(reportingDatasetVO.getDataProviderId()));
          getUserRole(
              ResourceGroupEnum.DATASET_REPORTER_WRITE.getGroupName(reportingDatasetVO.getId()),
              SecurityRoleEnum.REPORTER_WRITE.toString(), userRoleList,
              providerIds.get(reportingDatasetVO.getDataProviderId()));
          getUserRole(
              ResourceGroupEnum.DATASET_NATIONAL_COORDINATOR.getGroupName(
                  reportingDatasetVO.getId()),
              SecurityRoleEnum.NATIONAL_COORDINATOR.toString(), userRoleList,
              providerIds.get(reportingDatasetVO.getDataProviderId()));
          providerIds.remove(reportingDatasetVO.getDataProviderId());
        }
      }
    }
  }



  /**
   * Gets the user role.
   *
   * @param group the group
   * @param role the role
   * @param userRoleList the user role list
   * @param country the country
   * @return the user role
   */
  private void getUserRole(String group, String role, List<UserRoleVO> userRoleList,
      String country) {
    GroupInfo[] groupInfo = keycloakConnectorService.getGroupsWithSearch(group);
    UserRepresentation[] users = null;
    if (groupInfo != null && groupInfo.length != 0) {
      users = keycloakConnectorService.getUsersByGroupId(groupInfo[0].getId());
    }
    if (users != null && users.length != 0) {
      for (UserRepresentation userRepresentation : users) {
        UserRoleVO userRoleVO = new UserRoleVO();
        userRoleVO.setEmail(userRepresentation.getEmail());
        userRoleVO.setRoles(Arrays.asList(role));
        if (null != country) {
          userRoleVO.setDatasetProviderName(country);
        }
        userRoleList.add(userRoleVO);
      } ;
    }
  }


}


