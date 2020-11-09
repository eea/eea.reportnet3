package org.eea.collaboration.service.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
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
    collaborationServiceHelper.notifyNewMessages(1L, 1L, EventType.RECEIVED_MESSAGE.toString());
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
    collaborationServiceHelper.notifyNewMessages(1L, 1L, EventType.RECEIVED_MESSAGE.toString());
    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }
}
