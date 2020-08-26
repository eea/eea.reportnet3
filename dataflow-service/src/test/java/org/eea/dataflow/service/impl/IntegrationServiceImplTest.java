package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataflow.integration.crud.factory.CrudManager;
import org.eea.dataflow.integration.crud.factory.CrudManagerFactory;
import org.eea.dataflow.integration.executor.IntegrationExecutorFactory;
import org.eea.dataflow.integration.executor.service.IntegrationExecutorService;
import org.eea.dataflow.integration.utils.IntegrationParams;
import org.eea.dataflow.mapper.IntegrationMapper;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.domain.InternalOperationParameters;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
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

  /** The eu dataset controller zuul. */
  @Mock
  private EUDatasetControllerZuul euDatasetControllerZuul;

  /** The integration executor factory. */
  @Mock
  private IntegrationExecutorFactory integrationExecutorFactory;

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
    assertNotNull(
        integrationService.getOnlyExtensionsAndOperations(integrationVOList).get(0).getName());
  }

  /**
   * Creates the default integration test.
   */
  @Test
  public void createDefaultIntegrationTest() {
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    Mockito.doNothing().when(crudManager).create(Mockito.any());
    integrationService.createDefaultIntegration(1L, "5ce524fad31fc52540abae73");
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
    IntegrationVO response =
        integrationService.getExportEUDatasetIntegration("5ce524fad31fc52540abae73");
    Assert.assertEquals(expected, response);
  }

  /**
   * Creates the integration exception test.
   *
   * @throws EEAException the EEA exception
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

  /**
   * Execute EU dataset export test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeEUDatasetExportTest() throws EEAException {
    IntegrationExecutorService executor = Mockito.mock(IntegrationExecutorService.class);
    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put(IntegrationParams.DATASET_SCHEMA_ID, "5ce524fad31fc52540abae73");
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setInternalParameters(internalParameters);
    EUDatasetVO euDatasetVO = new EUDatasetVO();
    euDatasetVO.setId(1L);
    euDatasetVO.setDatasetSchema("5ce524fad31fc52540abae73");
    List<EUDatasetVO> euDatasetVOs = new ArrayList<>();
    List<IntegrationVO> integrationVOs = new ArrayList<>();
    euDatasetVOs.add(euDatasetVO);
    integrationVOs.add(integrationVO);
    Mockito.when(euDatasetControllerZuul.findEUDatasetByDataflowId(Mockito.anyLong()))
        .thenReturn(euDatasetVOs);
    Mockito.when(integrationRepository.findByOperationAndParameterAndValue(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
    Mockito.when(integrationMapper.entityListToClass(Mockito.any())).thenReturn(integrationVOs);
    Mockito.when(integrationExecutorFactory.getExecutor(Mockito.any())).thenReturn(executor);
    Mockito.when(executor.execute(Mockito.any(), Mockito.any()))
        .thenReturn(new ExecutionResultVO());
    List<ExecutionResultVO> result = integrationService.executeEUDatasetExport(1L);
    Assert.assertEquals(1, result.size());
  }

  /**
   * Execute EU dataset expor exceptiont test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void executeEUDatasetExportExceptionTest() throws EEAException {
    EUDatasetVO euDatasetVO = new EUDatasetVO();
    euDatasetVO.setId(1L);
    euDatasetVO.setDatasetSchema("5ce524fad31fc52540abae73");
    List<EUDatasetVO> euDatasetVOs = new ArrayList<>();
    List<IntegrationVO> integrationVOs = new ArrayList<>();
    euDatasetVOs.add(euDatasetVO);
    Mockito.when(euDatasetControllerZuul.findEUDatasetByDataflowId(Mockito.anyLong()))
        .thenReturn(euDatasetVOs);
    Mockito.when(integrationRepository.findByOperationAndParameterAndValue(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
    Mockito.when(integrationMapper.entityListToClass(Mockito.any())).thenReturn(integrationVOs);
    try {
      integrationService.executeEUDatasetExport(1L);
    } catch (EEAException e) {
      Assert.assertEquals("Mismatching number of IntegrationVOs and EUDatasetVOs", e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the export integration test.
   *
   * @return the export integration test
   */
  @Test
  public void getExportIntegrationTest() {
    IntegrationVO integrationVO = new IntegrationVO();
    InternalOperationParameters parameter = new InternalOperationParameters();
    parameter.setParameter(IntegrationParams.FILE_EXTENSION);
    parameter.setValue("csv");
    Integration integration = new Integration();
    integration.setInternalParameters(Arrays.asList(parameter));
    List<Integration> integrations = new ArrayList<>();
    integrations.add(integration);

    Mockito.when(integrationRepository.findByOperationAndParameterAndValue(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(integrations);
    Mockito.when(integrationMapper.entityToClass(Mockito.any())).thenReturn(integrationVO);
    Assert.assertEquals(integrationVO,
        integrationService.getExportIntegration("5ce524fad31fc52540abae73", "csv"));
  }

  /**
   * Gets the export integration null test.
   *
   * @return the export integration null test
   */
  @Test
  public void getExportIntegrationNullTest() {
    Mockito.when(integrationRepository.findByOperationAndParameterAndValue(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(null);
    Assert.assertNull(integrationService.getExportIntegration("5ce524fad31fc52540abae73", "csv"));
  }

  /**
   * Delete schema integrations test.
   */
  @Test
  public void deleteSchemaIntegrationsTest() {
    Mockito.doNothing().when(integrationRepository).deleteByParameterAndValue(Mockito.anyString(),
        Mockito.anyString());
    integrationService.deleteSchemaIntegrations("5ce524fad31fc52540abae73");
    Mockito.verify(integrationRepository, times(1)).deleteByParameterAndValue(Mockito.anyString(),
        Mockito.anyString());
  }

  @Test
  public void executeExternalIntegrationTest() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setId(1L);
    IntegrationExecutorService executor = Mockito.mock(IntegrationExecutorService.class);
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    Mockito.when(crudManager.get(Mockito.any())).thenReturn(Arrays.asList(integrationVO));
    Mockito.when(integrationExecutorFactory.getExecutor(Mockito.any())).thenReturn(executor);
    Mockito.when(executor.execute(Mockito.any(), Mockito.any()))
        .thenReturn(new ExecutionResultVO());
    ExecutionResultVO result = integrationService.executeExternalIntegration(1L, 1L,
        IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM);
    Assert.assertNotNull(result);
  }
}
