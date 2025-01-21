package org.eea.collaboration.service.helper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.utils.LiteralConstants;
import java.util.Set;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.EmailController.EmailControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;

import org.eea.interfaces.vo.communication.EmailVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.security.authorization.ObjectAccessRoleEnum;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


@RunWith(MockitoJUnitRunner.class)
public class CollaborationServiceHelperTest {
  @InjectMocks
  private CollaborationServiceHelper collaborationServiceHelper;

  @Mock
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Mock
  private EmailControllerZuul emailControllerZuul;

  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Before
  public void initMocks() {
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  public void notifyNewMessagesCustodianTest() throws EEAException {
    UserRepresentationVO user = new UserRepresentationVO();
    user.setUsername("provider");
    List<UserRepresentationVO> users = new ArrayList<>();
    users.add(user);
    List<Long> datasetIds = new ArrayList<>();
    datasetIds.add(1L);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito
        .when(dataSetMetabaseControllerZuul
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(datasetIds);
    Mockito.when(userManagementControllerZull.getUsersByGroup(Mockito.anyString()))
        .thenReturn(users);

    DataFlowVO mockDataflow = new DataFlowVO();
    mockDataflow.setName("Test Dataflow Name"); // Set a valid name
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong()))
            .thenReturn(mockDataflow);

    collaborationServiceHelper.notifyNewMessages(1L, 1L,  null,null, null, null,
        EventType.RECEIVED_MESSAGE.toString());
    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void notifyNewMessagesLeadReporterTest() throws EEAException {
    UserRepresentationVO user = new UserRepresentationVO();
    user.setUsername("custodian");
    List<UserRepresentationVO> users = new ArrayList<>();
    users.add(user);
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(
        new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_LEAD_REPORTER.getAccessRole(1L)));
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(userManagementControllerZull.getUsersByGroup(Mockito.anyString()))
        .thenReturn(users);

    DataFlowVO mockDataflow = new DataFlowVO();
    mockDataflow.setName("Test Dataflow Name"); // Set a valid name
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong()))
            .thenReturn(mockDataflow);

    collaborationServiceHelper.notifyNewMessages(1L, 1L,null, null, null, null,
        EventType.RECEIVED_MESSAGE.toString());
    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void emailNewMessagesCustodianTest() throws Exception {
    // Given
    String messageContent = "New message text";

    Date messageCreateDate = new Date();
    ZonedDateTime cetTime = messageCreateDate.toInstant().atZone(ZoneId.of("CET"));
    DateTimeFormatter cetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String cetFormattedDate = cetTime.format(cetFormatter);

    // Mock the user for the group
    UserRepresentationVO user = new UserRepresentationVO();
    user.setUsername("provider");
    user.setEmail("user1@example.com");
    List<UserRepresentationVO> users = new ArrayList<>();
    users.add(user);

    // Mock the datasetIds for the dataflow
    List<Long> datasetIds = new ArrayList<>();
    datasetIds.add(1L);

    //Mock Provider label
    String providerLabel = "Austria";

    // Mock authorities
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATAFLOW_CUSTODIAN.getAccessRole(1L)));

    // Mock the authentication and user group fetching
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.doReturn(authorities).when(authentication).getAuthorities();
    Mockito.when(dataSetMetabaseControllerZuul.getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(datasetIds);
    Mockito.when(userManagementControllerZull.getUsersByGroup(Mockito.anyString()))
            .thenReturn(users); // Return mocked users

    // Mock buildUserAndEmailSets to return the expected emailSet
    Set<String> emailSet = new HashSet<>();
    emailSet.add("user1@example.com"); // Mock email addresses
    Map<String, Set<String>> mockNotificationSets = new HashMap<>();
    mockNotificationSets.put("emailSet", emailSet);  // emailSet is returned here

    // Mock the DataFlowController
    DataFlowVO mockDataflow = new DataFlowVO();
    mockDataflow.setName("Test Dataflow Name");
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong()))
            .thenReturn(mockDataflow);


    // Expected email text format
    String emailText = String.format(
            LiteralConstants.TECH_ACCEPT_MESSAGE , mockDataflow.getName(), cetFormattedDate, messageContent
    );

    String emailSubject = String.format(
            LiteralConstants.TECH_ACCEPT_MESSAGE_SUBJECT  , providerLabel, mockDataflow.getName()
    );

    // Mock the EmailController's sendMessage method (no actual sending)
    Mockito.doNothing().when(emailControllerZuul).sendMessage(Mockito.any(EmailVO.class));

    DataProviderVO mockDataProvider = new DataProviderVO();
    mockDataProvider.setLabel("Austria");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong())).thenReturn(mockDataProvider);

    // When
    collaborationServiceHelper.emailNewMessages(1L, 1L, null, EventType.RECEIVED_MESSAGE.toString(), messageContent, messageCreateDate);

    // Then
    Mockito.verify(emailControllerZuul, Mockito.times(1)).sendMessage(Mockito.argThat(email ->
            // Verifying that Bbc (emailSet) contains the correct email address
            email.getBbc().contains("user1@example.com") &&  // Check that the emailSet is correctly included
                    email.getSubject().equals(emailSubject) &&
                    email.getText().equals(emailText)
    ));
  }
}
