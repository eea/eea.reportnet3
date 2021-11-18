package org.eea.ums.service.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BackupManagmentServiceImplTest {

  @InjectMocks
  private KeycloakSecurityProviderInterfaceService keycloakSecurityProviderInterfaceService;

  @InjectMocks
  private BackupManagmentServiceImpl backupManagmentControlerServiceImpl;

  private UserRepresentation newUser;

  private ByteArrayInputStream fileIn;

  private GroupInfo[] allGroups = new GroupInfo[2];

  private UserRepresentation[] allUsers = new UserRepresentation[1];

  private RoleRepresentation[] allRoles = new RoleRepresentation[1];

  @Mock
  private KeycloakConnectorService keycloakConnectorService;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    List<CredentialRepresentation> credentials = new ArrayList<>();
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType("password");
    credential.setTemporary(false);
    credential.setValue("1234");
    credentials.add(credential);

    Map<String, Boolean> access = new HashMap<>();
    access.put("manageGroupMembership", true);
    access.put("view", true);
    access.put("mapRoles", true);
    access.put("impersonate", false);
    access.put("manage", true);

    List<String> realmRoles = new ArrayList<>();
    realmRoles.add("DATA_PROVIDER");

    newUser = new UserRepresentation();
    newUser.setUsername("test");
    newUser.setEnabled(true);
    newUser.setFirstName("test");
    newUser.setLastName("test");
    newUser.setEmail("test@test.test");
    newUser.setEmailVerified(false);
    newUser.setCredentials(credentials);
    newUser.setAccess(access);
    newUser.setRealmRoles(realmRoles);

    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("Users");

    XSSFRow rowhead = sheet.createRow(0);
    rowhead.createCell(0).setCellValue("username");
    rowhead.createCell(1).setCellValue("firstName");
    rowhead.createCell(2).setCellValue("lastName");
    rowhead.createCell(3).setCellValue("email");
    rowhead.createCell(4).setCellValue("groups");
    rowhead.createCell(5).setCellValue("realmRoles");

    XSSFRow row1 = sheet.createRow(1);
    row1.createCell(0).setCellValue("ES511M270688");
    row1.createCell(1).setCellValue("na");
    row1.createCell(2).setCellValue("16/07/2018");
    row1.createCell(3).setCellValue("test1.test1@gmail.com");
    row1.createCell(4).setCellValue("Dataset-211-DATA_PROVIDER,Dataset-212-DATA_PROVIDER");
    row1.createCell(5).setCellValue("DATA_PROVIDER");


    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    workbook.write(outStream);
    workbook.close();
    outStream.close();


    fileIn = new ByteArrayInputStream(outStream.toByteArray());



    GroupInfo groupInfo1 = new GroupInfo();
    groupInfo1.setId(UUID.randomUUID().toString());
    groupInfo1.setName("test1");
    GroupInfo groupInfo2 = new GroupInfo();
    groupInfo2.setId(UUID.randomUUID().toString());
    groupInfo2.setName("test2");
    allGroups[0] = groupInfo1;
    allGroups[1] = groupInfo2;

    UserRepresentation newUser1 = newUser;
    // UserRepresentation newUser2 = newUser;
    allUsers[0] = newUser1;
    // allUsers[1] = newUser2;

    RoleRepresentation role = new RoleRepresentation();
    role.setName("DATA_PROVIDER");
    role.setId(UUID.randomUUID().toString());
    allRoles[0] = role;

  }


  @Test
  public void readExcelDatatoKeyCloakTest() throws IOException {

    ObjectMapper mapper = new ObjectMapper();
    MockMultipartFile file = new MockMultipartFile("files", "filename.xlsx",
        "application/vnd.ms-excel", "hello".getBytes(StandardCharsets.UTF_8));
    when(keycloakConnectorService.getGroups()).thenReturn(allGroups);
    when(keycloakConnectorService.getUsers()).thenReturn(allUsers);
    when(keycloakConnectorService.getRoles()).thenReturn(allRoles);

    backupManagmentControlerServiceImpl.readAndSaveUsers(fileIn);
    Mockito.verify(keycloakConnectorService, times(1)).addUser(Mockito.any());

  }

}
