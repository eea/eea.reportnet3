package org.eea.dataflow.integration.factory;


import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.integration.crud.factory.AbstractCrudManager;
import org.eea.dataflow.integration.crud.factory.CrudManager;
import org.eea.dataflow.integration.crud.factory.CrudManagerFactoryImpl;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;


/**
 * The Class CrudManagerFactoryImplTest.
 */
public class CrudManagerFactoryImplTest {

  /** The manager factory. */
  @InjectMocks
  private CrudManagerFactoryImpl managerFactory;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }


  /**
   * Gets the manager.
   *
   * @return the manager
   */
  @Test
  public void getManager() {
    Map<IntegrationToolTypeEnum, AbstractCrudManager> managersMaps = new HashMap<>();
    AbstractCrudManager manager = Mockito.mock(AbstractCrudManager.class);
    Mockito.when(manager.getToolType()).thenReturn(IntegrationToolTypeEnum.FME);
    managersMaps.put(IntegrationToolTypeEnum.FME, manager);

    ReflectionTestUtils.setField(managerFactory, "managersMap", managersMaps);

    CrudManager result = managerFactory.getManager(IntegrationToolTypeEnum.FME);

    Assert.assertNotNull("Retrieved event handler is null", result);
    Assert.assertEquals("The manager is one of the FME kind", IntegrationToolTypeEnum.FME,
        ((AbstractCrudManager) result).getToolType());

  }
}
