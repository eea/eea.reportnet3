package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eea.dataflow.integration.crud.factory.CrudManager;
import org.eea.dataflow.integration.crud.factory.CrudManagerFactory;
import org.eea.dataflow.mapper.IntegrationMapper;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

  /** The integration repository. */
  @Mock
  private IntegrationRepository integrationRepository;

  /** The integration mapper. */
  @Mock
  private IntegrationMapper integrationMapper;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the all integrations by criteria test.
   *
   * @return the all integrations by criteria test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllIntegrationsByCriteriaTest() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setId(1L);
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    Mockito.when(crudManager.get(integrationVO)).thenReturn(Arrays.asList(integrationVO));
    integrationService.getAllIntegrationsByCriteria(integrationVO);
    Mockito.verify(crudManager, times(1)).get(Mockito.any());
  }

  /**
   * Creates the integration test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createIntegrationTest() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.getInternalParameters().put("datasetSchemaId", "test1");
    integrationVO.getInternalParameters().put("dataflowId", "1");
    Mockito.doNothing().when(crudManager).create(integrationVO);
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    integrationService.createIntegration(integrationVO);
    Mockito.verify(crudManager, times(1)).create(Mockito.any());
  }

  /**
   * Update integration test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateIntegrationTest() throws EEAException {
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
   * Delete integration test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteIntegrationTest() throws EEAException {
    Mockito.doNothing().when(crudManager).delete(Mockito.anyLong());
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    integrationService.deleteIntegration(1L);
    Mockito.verify(crudManager, times(1)).delete(Mockito.any());
  }

  /**
   * Gets the only extensions and operations test.
   *
   * @return the only extensions and operations test
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

  /**
   * Creates the default integration test.
   */
  @Test
  public void createDefaultIntegrationTest() {
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    Mockito.doNothing().when(crudManager).create(Mockito.any());
    integrationService.createDefaultIntegration(1L, 1L, "5ce524fad31fc52540abae73");
    Mockito.verify(crudManager, times(1)).create(Mockito.any());
  }

  /**
   * Gets the expor EU dataset integration by dataset id test.
   *
   * @return the expor EU dataset integration by dataset id test
   */
  @Test
  public void getExporEUDatasetIntegrationByDatasetIdTest() {
    IntegrationVO expected = new IntegrationVO();
    Mockito.when(integrationRepository.findFirstByOperationAndParameterAndValue(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(new Integration());
    Mockito.when(integrationMapper.entityToClass(Mockito.any())).thenReturn(expected);
    IntegrationVO response = integrationService.getExporEUDatasetIntegrationByDatasetId(1L);
    Assert.assertEquals(expected, response);
  }

  /**
   * Creates the integration exception test.
   * 
   * @throws EEAException
   */
  @Test(expected = ResponseStatusException.class)
  public void createIntegrationExceptionTest() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);

    try {
      integrationService.createIntegration(integrationVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.FORBIDDEN_EXPORT_EU_DATASET_INTEGRATION_CREATION,
          e.getReason());
      throw e;
    }
  }
}
