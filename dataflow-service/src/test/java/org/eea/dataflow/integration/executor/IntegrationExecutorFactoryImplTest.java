package org.eea.dataflow.integration.executor;

import static org.junit.Assert.assertNotNull;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.integration.executor.service.AbstractIntegrationExecutorService;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class IntegrationExecutorFactoryImplTest {

  @InjectMocks
  IntegrationExecutorFactoryImpl integrationExecutorFactory;

  @Mock
  private AbstractIntegrationExecutorService abstractIntegrationExecutorService;

  private Map<IntegrationToolTypeEnum, AbstractIntegrationExecutorService> integrationMap;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
    integrationMap = new HashMap<>();
    integrationMap.put(IntegrationToolTypeEnum.OTHER, abstractIntegrationExecutorService);
    ReflectionTestUtils.setField(integrationExecutorFactory, "integrationMap", integrationMap);
  }

  @Test
  public void testGetExecutor() {
    assertNotNull(integrationExecutorFactory.getExecutor(IntegrationToolTypeEnum.OTHER));
  }


}
