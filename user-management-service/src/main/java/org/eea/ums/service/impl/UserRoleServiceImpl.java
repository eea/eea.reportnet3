package org.eea.ums.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
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



}


