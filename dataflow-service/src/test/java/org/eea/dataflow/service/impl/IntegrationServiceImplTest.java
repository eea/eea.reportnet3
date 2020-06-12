package org.eea.dataflow.service.impl;


import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eea.dataflow.integration.crud.factory.CrudManager;
import org.eea.dataflow.integration.crud.factory.CrudManagerFactory;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * The Class IntegrationServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationServiceImplTest {


  /** The integration service. */
  @InjectMocks
  private IntegrationServiceImpl integrationService;

  /** The crud manager. */
  @Mock
  private CrudManager crudManager;

  /** The crud manager factory. */
  @Mock
  private CrudManagerFactory crudManagerFactory;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  /**
   * Test get integration.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetIntegration() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setId(1L);
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    Mockito.when(crudManager.get(integrationVO)).thenReturn(Arrays.asList(integrationVO));
    integrationService.getAllIntegrationsByCriteria(integrationVO);
    Mockito.verify(crudManager, times(1)).get(Mockito.any());
  }

  /**
   * Test create integration.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testCreateIntegration() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.getInternalParameters().put("datasetSchemaId", "test1");
    integrationVO.getInternalParameters().put("dataflowId", "1");
    Mockito.doNothing().when(crudManager).create(integrationVO);
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    integrationService.createIntegration(integrationVO);
    Mockito.verify(crudManager, times(1)).create(Mockito.any());
  }

  /**
   * Test update integration.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testUpdateIntegration() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setId(1L);
    integrationVO.getInternalParameters().put("datasetSchemaId", "test1");
    integrationVO.getInternalParameters().put("dataflowId", "1");
    Mockito.doNothing().when(crudManager).update(integrationVO);
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    integrationService.updateIntegration(integrationVO);
    Mockito.verify(crudManager, times(1)).update(Mockito.any());
  }

  /**
   * Test delete integration.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testDeleteIntegration() throws EEAException {
    Mockito.doNothing().when(crudManager).delete(Mockito.anyLong());
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    integrationService.deleteIntegration(1L);
    Mockito.verify(crudManager, times(1)).delete(Mockito.any());
  }

  /**
   * Gets the only extensions and operations test.
   *
   * @return the only extensions and operations test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getOnlyExtensionsAndOperationsTest() {
    List<IntegrationVO> integrationVOList = new ArrayList<>();
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setName("name");
    integrationVOList.add(integrationVO);
    assertNull(
        integrationService.getOnlyExtensionsAndOperations(integrationVOList).get(0).getName());
  }


}
