package org.eea.ums.controller;

import java.util.ArrayList;
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

@RunWith(MockitoJUnitRunner.class)
public class ResourceManagementControllerImplTest {

  @InjectMocks
  private ResourceManagementControllerImpl resourceManagementControllerImpl;
  @Mock
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getGroupDetail() {
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    Mockito
        .when(securityProviderInterfaceService
            .getResourceDetails(ResourceGroupEnum.DATAFLOW_PROVIDER.getGroupName(1l)))
        .thenReturn(resourceInfoVO);
    ResourceInfoVO result =
        resourceManagementControllerImpl.getResourceDetail(1L, ResourceGroupEnum.DATAFLOW_PROVIDER);
    Assert.assertNotNull(result);
  }

  @Test
  public void createResource() {
    resourceManagementControllerImpl.createResource(new ResourceInfoVO());
  }

  @Test
  public void deleteResource() {
    resourceManagementControllerImpl.deleteResource(new ArrayList<>());
  }

  @Test
  public void deleteResourceByName() {
    resourceManagementControllerImpl.deleteResourceByName(new ArrayList<>());
  }

  @Test
  public void createResources() {
    resourceManagementControllerImpl.createResources(new ArrayList<ResourceInfoVO>());
  }
}
