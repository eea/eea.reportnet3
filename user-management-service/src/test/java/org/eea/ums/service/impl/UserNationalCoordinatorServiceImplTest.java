package org.eea.ums.service.impl;

import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.ums.UserNationalCoordinatorVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.redis.RedisLockService;
import org.eea.lock.redis.RedisLockServiceImpl;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;

/**
 * The Class UserNationalCoordinatorServiceImplTest.
 */
public class UserNationalCoordinatorServiceImplTest {

  /** The user national coordinator service impl. */
  @InjectMocks
  private UserNationalCoordinatorServiceImpl userNationalCoordinatorServiceImpl;

  /** The keycloak connector service. */
  @Mock
  private KeycloakConnectorService keycloakConnectorService;

  /** The representative controller zuul. */
  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The dataset metabase controller zuul. */
  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The security provider interface service. */
  @Mock
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private RedisLockService redisLockService = new RedisLockServiceImpl(redisTemplate);

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("userId", "123", new HashSet<>());
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userId");
    authentication.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Mockito.when(redisLockService.checkAndAcquireLock(Mockito.any(), Mockito.any(), Mockito.anyLong())).thenReturn(true);
  }

  /**
   * Gets the national coordinators test.
   *
   * @return the national coordinators test
   */
  @Test
  public void getNationalCoordinatorsTest() {
    GroupInfo group = new GroupInfo();
    group.setName(ResourceGroupEnum.PROVIDER_NATIONAL_COORDINATOR.getGroupName("ES"));
    GroupInfo[] groupInfo = {group};
    UserRepresentation[] userRep = {new UserRepresentation()};
    Mockito.when(keycloakConnectorService.getGroupsWithSearch(Mockito.any())).thenReturn(groupInfo);
    Mockito.when(keycloakConnectorService.getUsersByGroupId(Mockito.any())).thenReturn(userRep);
    assertNotNull(userNationalCoordinatorServiceImpl.getNationalCoordinators());
  }

  /**
   * Creates the national coordinator test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createNationalCoordinatorTest() throws EEAException {

    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    UserRepresentation[] userRep = {new UserRepresentation()};
    List<DataProviderVO> dataProviders = new ArrayList<>();
    List<DataSetMetabaseVO> reportings = new ArrayList<>();
    dataProviders.add(new DataProviderVO());
    reportings.add(new DataSetMetabaseVO());
    Mockito.when(keycloakConnectorService.getUsersByEmail(Mockito.any())).thenReturn(userRep);
    Mockito.when(representativeControllerZuul.findDataProvidersByCode(Mockito.any()))
        .thenReturn(dataProviders);
    Mockito.when(datasetMetabaseControllerZuul.findReportingDataSetByProviderIds(Mockito.any()))
        .thenReturn(reportings);
    userNationalCoordinatorServiceImpl.createNationalCoordinator(userNC);
    Mockito.verify(securityProviderInterfaceService, times(1))
        .addContributorsToUserGroup(Mockito.any());
  }


  /**
   * Creates the national coordinator user not found test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNationalCoordinatorUserNotFoundTest() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    try {
      userNationalCoordinatorServiceImpl.createNationalCoordinator(userNC);
    } catch (EEAException e) {
      assertEquals(String.format(EEAErrorMessage.USER_NOTFOUND, userNC.getEmail()), e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the national coordinator not email test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNationalCoordinatorNotEmailTest() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc");
    try {
      userNationalCoordinatorServiceImpl.createNationalCoordinator(userNC);
    } catch (EEAException e) {
      assertEquals(String.format(EEAErrorMessage.NOT_EMAIL, userNC.getEmail()), e.getMessage());
      throw e;
    }
  }


  /**
   * Creates the national coordinator exception user test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNationalCoordinatorExceptionUserTest() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    try {
      userNationalCoordinatorServiceImpl.createNationalCoordinator(userNC);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.USER_REQUEST_NOTFOUND, e.getMessage());
      throw e;
    }
  }


  /**
   * Creates the national coordinator country not found test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNationalCoordinatorCountryNotFoundTest() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    UserRepresentation[] userRep = {new UserRepresentation()};
    Mockito.when(keycloakConnectorService.getUsersByEmail(Mockito.any())).thenReturn(userRep);
    try {
      userNationalCoordinatorServiceImpl.createNationalCoordinator(userNC);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.COUNTRY_CODE_NOTFOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete national coordinator test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteNationalCoordinatorTest() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    UserRepresentation[] userRep = {new UserRepresentation()};
    List<DataProviderVO> dataProviders = new ArrayList<>();
    List<DataSetMetabaseVO> reportings = new ArrayList<>();
    dataProviders.add(new DataProviderVO());
    reportings.add(new DataSetMetabaseVO());
    Mockito.when(keycloakConnectorService.getUsersByEmail(Mockito.any())).thenReturn(userRep);
    Mockito.when(representativeControllerZuul.findDataProvidersByCode(Mockito.any()))
        .thenReturn(dataProviders);
    Mockito.when(datasetMetabaseControllerZuul.findReportingDataSetByProviderIds(Mockito.any()))
        .thenReturn(reportings);
    userNationalCoordinatorServiceImpl.deleteNationalCoordinator(userNC);
    Mockito.verify(securityProviderInterfaceService, times(1))
        .removeContributorsFromUserGroup(Mockito.any());
  }

  /**
   * Delete national coordinator country not found test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void deleteNationalCoordinatorCountryNotFoundTest() throws EEAException {
    UserNationalCoordinatorVO userNC = new UserNationalCoordinatorVO();
    userNC.setCountryCode("ES");
    userNC.setEmail("abc@abc.com");
    UserRepresentation[] userRep = {new UserRepresentation()};
    Mockito.when(keycloakConnectorService.getUsersByEmail(Mockito.any())).thenReturn(userRep);
    try {
      userNationalCoordinatorServiceImpl.deleteNationalCoordinator(userNC);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.COUNTRY_CODE_NOTFOUND, e.getMessage());
      throw e;
    }
  }
}
