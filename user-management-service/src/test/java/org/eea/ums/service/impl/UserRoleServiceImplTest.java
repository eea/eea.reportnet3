package org.eea.ums.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class UserRoleServiceImplTest.
 */
public class UserRoleServiceImplTest {

  /** The user role service. */
  @InjectMocks
  private UserRoleServiceImpl userRoleService;

  /** The keycloak connector service. */
  @Mock
  private KeycloakConnectorService keycloakConnectorService;

  /** The representative controller zuul. */
  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities
        .add(new SimpleGrantedAuthority(ObjectAccessRoleEnum.DATASET_CUSTODIAN.getAccessRole(1L)));
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("user", "password", authorities));
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test get user roles by dataflow country.
   */
  @Test
  public void testGetUserRolesByDataflowCountry() {
    GroupInfo group = new GroupInfo();
    UserRepresentation user = new UserRepresentation();
    group.setId("1");
    user.setEmail("");
    List<Long> datasetIds = new ArrayList<>();
    datasetIds.add(1L);
    datasetIds.add(2L);
    Mockito
        .when(datasetMetabaseControllerZuul
            .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.any(), Mockito.any()))
        .thenReturn(datasetIds);
    Mockito.when(keycloakConnectorService.getGroupsWithSearch(Mockito.any()))
        .thenReturn(new GroupInfo[] {group, group});
    Mockito.when(keycloakConnectorService.getUsersByGroupId(Mockito.any()))
        .thenReturn(new UserRepresentation[] {user, user});
    assertNotNull(userRoleService.getUserRolesByDataflowCountry(1L, 1L));
  }

  @Test
  public void getUserRolesByDataflowTest() {
    RepresentativeVO rep = new RepresentativeVO();
    rep.setLeadReporters(Arrays.asList(new LeadReporterVO()));
    GroupInfo[] group = {new GroupInfo()};
    UserRepresentation[] users = {new UserRepresentation()};
    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(rep));
    Mockito.when(representativeControllerZuul.findDataProvidersByIds(Mockito.any()))
        .thenReturn(Arrays.asList(new DataProviderVO()));
    Mockito.when(datasetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(Arrays.asList(new ReportingDatasetVO()));
    Mockito.when(keycloakConnectorService.getGroupsWithSearch(Mockito.any())).thenReturn(group);
    Mockito.when(keycloakConnectorService.getUsersByGroupId(Mockito.any())).thenReturn(users);
    assertNotNull(userRoleService.getUserRolesByDataflow(0L));
  }

  @Test(expected = ResponseStatusException.class)
  public void downloadUsersByCountryTest() {
    try {
      userRoleService.downloadUsersByCountry(1L, "fileName");
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.NOT_FOUND);
      throw e;
    }
  }

  @Test
  public void exportUsersByCountryTest() throws IOException, EEAException {
    NotificationVO notification = new NotificationVO();
    notification.setDataflowId(1L);
    notification.setUser("user");
    notification.setFileName("dataflow-" + 1L + "-UsersByCountry.csv");
    notification.setError("Failed generating CSV file with name dataflow-" + 1L
        + "-UsersByCountry.csv, using dataflowId " + 1L);
    RepresentativeVO rep = new RepresentativeVO();
    rep.setLeadReporters(Arrays.asList(new LeadReporterVO()));
    GroupInfo[] group = {new GroupInfo()};
    UserRepresentation[] users = {new UserRepresentation()};

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(rep));
    Mockito.when(representativeControllerZuul.findDataProvidersByIds(Mockito.any()))
        .thenReturn(Arrays.asList(new DataProviderVO()));
    Mockito.when(datasetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(Arrays.asList(new ReportingDatasetVO()));
    Mockito.when(keycloakConnectorService.getGroupsWithSearch(Mockito.any())).thenReturn(group);
    Mockito.when(keycloakConnectorService.getUsersByGroupId(Mockito.any())).thenReturn(users);
    userRoleService.exportUsersByCountry(1L);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(
        EventType.EXPORT_USERS_BY_COUNTRY_COMPLETED_EVENT, null, notification);
  }

  @After
  public void afterTests() {
    File file = new File("./dataflow-1-UsersByCountry/dataflow-1-UsersByCountry.csv");
    file.delete();
  }
}
