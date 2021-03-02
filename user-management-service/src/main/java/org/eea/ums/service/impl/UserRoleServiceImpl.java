package org.eea.ums.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.ums.UserRoleVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.ums.service.UserRoleService;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
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

      Map<String, UserRoleVO> usersMap = new HashMap<>();
      // CUSTODIAN
      getUsersRolesByGroup(groupInfoMap, usersMap, SecurityRoleEnum.DATA_CUSTODIAN.toString());
      // LEAD REPORTER
      getUsersRolesByGroup(groupInfoMap, usersMap, SecurityRoleEnum.LEAD_REPORTER.toString());
      // NATIONAL COORDINATOR
      getUsersRolesByGroup(groupInfoMap, usersMap,
          SecurityRoleEnum.NATIONAL_COORDINATOR.toString());
      // REPORTER READ
      getUsersRolesByGroup(groupInfoMap, usersMap, SecurityRoleEnum.REPORTER_READ.toString());
      // REPORTER WRITE
      getUsersRolesByGroup(groupInfoMap, usersMap, SecurityRoleEnum.REPORTER_WRITE.toString());
      // REQUESTER
      getUsersRolesByGroup(groupInfoMap, usersMap, SecurityRoleEnum.DATA_REQUESTER.toString());
      // STEWARD
      getUsersRolesByGroup(groupInfoMap, usersMap, SecurityRoleEnum.DATA_STEWARD.toString());

      usersMap.forEach((k, v) -> finalList.add(v));
    }
    return finalList;
  }


  /**
   * Gets the group info map.
   *
   * @param groupInfoMap the group info map
   * @param long1 the long 1
   * @return the group info map
   */
  private void getGroupInfoMap(Map<String, List<GroupInfo>> groupInfoMap, Long long1) {
    setGroupsIntoMap(groupInfoMap,
        new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
            .getGroupsWithSearch(ResourceGroupEnum.DATASET_CUSTODIAN.getGroupName(long1)))),
        SecurityRoleEnum.DATA_CUSTODIAN.toString());
    setGroupsIntoMap(groupInfoMap,
        new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
            .getGroupsWithSearch(ResourceGroupEnum.DATASET_LEAD_REPORTER.getGroupName(long1)))),
        SecurityRoleEnum.LEAD_REPORTER.toString());
    setGroupsIntoMap(groupInfoMap,
        new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService.getGroupsWithSearch(
            ResourceGroupEnum.DATASET_NATIONAL_COORDINATOR.getGroupName(long1)))),
        SecurityRoleEnum.NATIONAL_COORDINATOR.toString());
    setGroupsIntoMap(groupInfoMap,
        new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
            .getGroupsWithSearch(ResourceGroupEnum.DATASET_REPORTER_READ.getGroupName(long1)))),
        SecurityRoleEnum.REPORTER_READ.toString());
    setGroupsIntoMap(groupInfoMap,
        new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
            .getGroupsWithSearch(ResourceGroupEnum.DATASET_REPORTER_WRITE.getGroupName(long1)))),
        SecurityRoleEnum.REPORTER_WRITE.toString());
    setGroupsIntoMap(groupInfoMap,
        new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
            .getGroupsWithSearch(ResourceGroupEnum.DATASET_REQUESTER.getGroupName(long1)))),
        SecurityRoleEnum.DATA_REQUESTER.toString());
    setGroupsIntoMap(groupInfoMap,
        new ArrayList<GroupInfo>(Arrays.asList(keycloakConnectorService
            .getGroupsWithSearch(ResourceGroupEnum.DATASET_STEWARD.getGroupName(long1)))),
        SecurityRoleEnum.DATA_STEWARD.toString());
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
   * @param usersMap the users map
   * @param group the group
   * @return the users roles by group
   */
  private void getUsersRolesByGroup(Map<String, List<GroupInfo>> groupInfoMap,
      Map<String, UserRoleVO> usersMap, String group) {
    if (null != groupInfoMap.get(group) && !groupInfoMap.get(group).isEmpty()) {
      for (GroupInfo groupInfo : groupInfoMap.get(group)) {
        groupInfo.getId();
        UserRepresentation[] users = keycloakConnectorService.getUsersByGroupId(groupInfo.getId());
        for (int i = 0; i < users.length; i++) {
          UserRoleVO userRol = new UserRoleVO();
          if (null != users[i] && null == usersMap.get(users[i].getEmail())) {
            List<String> roles = new ArrayList<>();
            roles.add(group);
            userRol.setEmail(users[i].getEmail());
            userRol.setRoles(roles);
            usersMap.put(userRol.getEmail(), userRol);
          } else if (null != users[i]
              && !usersMap.get(users[i].getEmail()).getRoles().contains(group)) {
            usersMap.get(users[i].getEmail()).getRoles().add(group);
          }
        }
      }
    }
  }



}


