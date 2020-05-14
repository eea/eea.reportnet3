package org.eea.ums.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eea.exception.EEAException;
import org.eea.ums.service.BackupManagmentService;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class BackupManagmentControlerServiceImpl.
 */
@Service("backupManagmentService")
public class BackupManagmentServiceImpl implements BackupManagmentService {



  /** The Constant ROLE. */
  private static final String ROLE = "DATA_PROVIDER";

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger("logger");

  /** The keycloak connector service. */
  @Autowired
  private KeycloakConnectorService keycloakConnectorService;


  /**
   * Read and safe users.
   *
   * @param is the is
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void readAndSaveUsers(InputStream is) throws IOException {

    GroupInfo[] allGroups;
    UserRepresentation[] allUsers;
    RoleRepresentation[] allRoles;

    // Default credentials
    List<CredentialRepresentation> credentials = new ArrayList<>();
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType("password");
    credential.setTemporary(false);
    credential.setValue("1234");
    credentials.add(credential);

    // Access default configuration
    Map<String, Boolean> access = new HashMap<>();
    access.put("manageGroupMembership", true);
    access.put("view", true);
    access.put("mapRoles", true);
    access.put("impersonate", false);
    access.put("manage", true);

    // Default role
    List<String> realmRoles = new ArrayList<>();
    realmRoles.add(ROLE);

    // Default groups


    LOG.info("Init read Excel");
    List<UserRepresentation> newUsers = generateUserList(is, credentials, access, realmRoles);

    LOG.info("Finish read Excel");

    // Init set groups
    // Get all groups
    LOG.info("Init set groups");
    allGroups = keycloakConnectorService.getGroups();
    allUsers = keycloakConnectorService.getUsers();
    allRoles = keycloakConnectorService.getRoles();

    Map<String, String> groupsMap = new HashMap<>();
    Arrays.asList(allGroups).stream()
        .forEach(group -> groupsMap.put(group.getName(), group.getId()));
    Map<String, String> usersMap = new HashMap<>();
    Arrays.asList(allUsers).stream()
        .forEach(user -> usersMap.put(user.getUsername(), user.getId()));

    LOG.info("Init set roles");
    Map<String, String> rolesMap = new HashMap<>();
    Arrays.asList(allRoles).stream().forEach(role -> rolesMap.put(role.getName(), role.getId()));


    newUsers.stream().forEach(user -> {
      user.getGroups().stream().forEach(group -> {
        try {
          keycloakConnectorService.addUserToGroup(usersMap.get(user.getUsername()),
              groupsMap.get(group));
        } catch (EEAException e) {
          LOG_ERROR.error("Error adding USER to resource. Message: {}", e.getMessage(), e);
        }
        LOG.info("Finish save group: " + group);
      });

      ObjectMapper roleMapper = new ObjectMapper();
      List<RoleRepresentation> roles = new ArrayList<>();
      RoleRepresentation newRole = new RoleRepresentation();
      newRole.setId(rolesMap.get(ROLE));
      newRole.setName(ROLE);
      roles.add(newRole);

      try {
        String json = roleMapper.writeValueAsString(roles);
        keycloakConnectorService.addRole(json, usersMap.get(user.getUsername()));
        LOG.info("Finish save Roles");
      } catch (JsonProcessingException e) {
        LOG_ERROR.error("Role not saved");
      }
    });

    LOG.info("Finish process");

  }



  /**
   * Generate user list.
   *
   * @param is the is
   * @param credentials the credentials
   * @param access the access
   * @param realmRoles the realm roles
   * @return the list
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private List<UserRepresentation> generateUserList(InputStream is,
      List<CredentialRepresentation> credentials, Map<String, Boolean> access,
      List<String> realmRoles) throws IOException {
    List<UserRepresentation> newUsers = new ArrayList<>();
    XSSFWorkbook workbook = new XSSFWorkbook(is);
    XSSFSheet worksheet = workbook.getSheetAt(0);

    for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
      UserRepresentation newUser = new UserRepresentation();
      XSSFRow row = worksheet.getRow(i);
      // ID can't be created
      newUser.setUsername(row.getCell(0).getStringCellValue());
      newUser.setEnabled(true);
      newUser.setFirstName(row.getCell(1).getStringCellValue());
      newUser.setLastName(row.getCell(2).getStringCellValue());
      newUser.setEmail(row.getCell(3).getStringCellValue());
      newUser.setEmailVerified(false);
      newUser.setCredentials(credentials);
      newUser.setAccess(access);
      newUser.setRealmRoles(realmRoles);
      String groups = row.getCell(4).getStringCellValue();
      List<String> groupsList = Arrays.asList(groups.split(","));
      newUser.setGroups(groupsList);
      newUsers.add(newUser);
      createUserOnebyOne(newUser);

    }
    workbook.close();
    return newUsers;
  }

  /**
   * Creates the user oneby one.
   *
   * @param newUser the new user
   */
  private void createUserOnebyOne(UserRepresentation newUser) {
    ObjectMapper userMapper = new ObjectMapper();
    LOG.info("Try to save User");
    try {
      String json = userMapper.writeValueAsString(newUser);
      keycloakConnectorService.addUser(json);
    } catch (JsonProcessingException e) {
      LOG_ERROR.error("User not saved");
      e.printStackTrace();
    }
    LOG.info("User saved");

  }

}


