package org.eea.ums.controller;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The Class ResourceManagementControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceManagementControllerImplTest {

  /** The resource management controller impl. */
  @InjectMocks
  private ResourceManagementControllerImpl resourceManagementControllerImpl;

  /** The security provider interface service. */
  @Mock
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {


    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the group detail.
   *
   * @return the group detail
   */
  @Test
  public void getGroupDetail() {
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    Mockito
        .when(securityProviderInterfaceService
            .getResourceDetails(ResourceGroupEnum.DATAFLOW_LEAD_REPORTER.getGroupName(1l)))
        .thenReturn(resourceInfoVO);
    ResourceInfoVO result = resourceManagementControllerImpl.getResourceDetail(1L,
        ResourceGroupEnum.DATAFLOW_LEAD_REPORTER);
    Assert.assertNotNull(result);
  }

  /**
   * Creates the resource.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createResource() throws EEAException {
    ResourceInfoVO resource = new ResourceInfoVO();
    resourceManagementControllerImpl.createResource(resource);
    Mockito.verify(securityProviderInterfaceService, times(1)).createResourceInstance(resource);
  }

  /**
   * Delete resource.
   */
  @Test
  public void deleteResource() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user", null, null);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    resourceManagementControllerImpl.deleteResource(new ArrayList<>());
    Mockito.verify(securityProviderInterfaceService, times(1))
        .deleteResourceInstances(Mockito.any());
  }

  /**
   * Delete resource by name.
   */
  @Test
  public void deleteResourceByName() {
    resourceManagementControllerImpl.deleteResourceByName(new ArrayList<>());
    Mockito.verify(securityProviderInterfaceService, times(1))
        .deleteResourceInstancesByName(Mockito.any());
  }

  /**
   * Creates the resources.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createResources() throws EEAException {
    ArrayList<ResourceInfoVO> resourceList = new ArrayList<>();
    resourceManagementControllerImpl.createResources(resourceList);
    Mockito.verify(securityProviderInterfaceService, times(1)).createResourceInstance(resourceList);
  }
}
