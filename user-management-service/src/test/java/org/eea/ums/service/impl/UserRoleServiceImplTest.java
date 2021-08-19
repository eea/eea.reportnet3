package org.eea.ums.service.impl;

import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The Class UserRoleServiceImplTest.
 */
public class UserRoleServiceImplTest {

  /** The user role service. */
  @InjectMocks
  private UserRoleServiceImpl userRoleService;

  /** The dataflow controller zuul. */
  @Mock
  private DataSetMetabaseControllerZuul dataflowControllerZuul;

  /** The keycloak connector service. */
  @Mock
  private KeycloakConnectorService keycloakConnectorService;

  /** The representative controller zuul. */
  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;



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
    Mockito.when(dataflowControllerZuul.getDatasetIdsByDataflowIdAndDataProviderId(Mockito.any(),
        Mockito.any())).thenReturn(datasetIds);
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

}
