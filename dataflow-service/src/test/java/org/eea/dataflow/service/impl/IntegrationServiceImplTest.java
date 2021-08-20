package org.eea.dataflow.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eea.dataflow.integration.crud.factory.CrudManager;
import org.eea.dataflow.integration.crud.factory.CrudManagerFactory;
import org.eea.dataflow.integration.executor.IntegrationExecutorFactory;
import org.eea.dataflow.integration.executor.service.IntegrationExecutorService;
import org.eea.dataflow.mapper.IntegrationMapper;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.domain.InternalOperationParameters;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

  /** The dataset controller zuul. */
  @Mock
  private DataSetControllerZuul datasetControllerZuul;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The lock service. */
  @Mock
  private LockService lockService;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
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
   * Delete integration exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void deleteIntegrationExceptionTest() throws EEAException {
    Mockito.when(integrationRepository.findOperationById(Mockito.any()))
        .thenReturn(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    try {
      integrationService.deleteIntegration(1L);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.FORBIDDEN_EXPORT_EU_DATASET_INTEGRATION_DELETION,
          e.getMessage());
      throw e;
    }
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
  public void createDefaultIntegrationTest() throws EEAException {
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
    parameter.setValue(FileTypeEnum.CSV.getValue());
    Integration integration = new Integration();
    integration.setId(1L);
    integration.setInternalParameters(Arrays.asList(parameter));
    List<Integration> integrations = new ArrayList<>();
    integrations.add(integration);

    Mockito.when(integrationRepository.findByOperationAndParameterAndValue(Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(integrations);
    Mockito.when(integrationMapper.entityToClass(Mockito.any())).thenReturn(integrationVO);
    Assert.assertEquals(integrationVO,
        integrationService.getExportIntegration("5ce524fad31fc52540abae73", 1L));
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
    Assert.assertNull(integrationService.getExportIntegration("5ce524fad31fc52540abae73", 1L));
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

  /**
   * Execute external integration test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeExternalIntegrationTest() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setId(1L);
    IntegrationExecutorService executor = Mockito.mock(IntegrationExecutorService.class);
    Map<String, Object> executionResultParams = new HashMap<>();
    executionResultParams.put("id", 1);
    ExecutionResultVO executionResultVO = new ExecutionResultVO();
    executionResultVO.setExecutionResultParams(executionResultParams);

    Mockito.when(integrationRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new Integration()));
    Mockito.when(integrationMapper.entityToClass(Mockito.any())).thenReturn(integrationVO);

    Mockito.when(integrationExecutorFactory.getExecutor(Mockito.any())).thenReturn(executor);

    Mockito.when(executor.execute(IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM, null, 1L,
        integrationVO)).thenReturn(executionResultVO);

    integrationService.executeExternalIntegration(1L, 1L,
        IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM, false);
    Mockito.verify(integrationExecutorFactory, times(1)).getExecutor(Mockito.any());
  }

  /**
   * Execute external integration replacing data test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeExternalIntegrationReplacingDataTest() throws EEAException {

    Mockito.doNothing().when(datasetControllerZuul).deleteDataBeforeReplacing(Mockito.anyLong(),
        Mockito.any(), Mockito.any());
    integrationService.executeExternalIntegration(1L, 1L,
        IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM, true);
    Mockito.verify(datasetControllerZuul, times(1)).deleteDataBeforeReplacing(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Copy integrations test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void copyIntegrationsTest() throws EEAException {
    List<String> originDatasetSchemaIds = new ArrayList<>();
    originDatasetSchemaIds.add("5ce524fad31fc52540abae73");
    Map<String, String> dictionaryOriginTargetObjectId = new HashMap<>();
    dictionaryOriginTargetObjectId.put("5ce524fad31fc52540abae73", "5ce524fad31fc52540abae73");
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setId(1L);
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    Mockito.when(crudManager.get(Mockito.any())).thenReturn(Arrays.asList(integrationVO));
    integrationService.copyIntegrations(1L, originDatasetSchemaIds, dictionaryOriginTargetObjectId);
    Mockito.verify(crudManager, times(1)).get(Mockito.any());
  }

  /**
   * Adds the populate EU dataset lock test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addPopulateEUDatasetLockTest() throws EEAException {

    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    integrationService.addPopulateEUDatasetLock(1L);
    Mockito.verify(lockService, times(1)).createLock(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
  }


  /**
   * Release populate EU dataset lock test.
   */
  @Test
  public void releasePopulateEUDatasetLockTest() {
    integrationService.releasePopulateEUDatasetLock(1L);
    Mockito.verify(lockService, times(1)).removeLockByCriteria(Mockito.any());
  }

  /**
   * Creates the integrations test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createIntegrationsTest() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.getInternalParameters().put("datasetSchemaId", "test1");
    integrationVO.getInternalParameters().put("dataflowId", "1");
    Mockito.doNothing().when(crudManager).create(integrationVO);
    Mockito.when(crudManagerFactory.getManager(Mockito.any())).thenReturn(crudManager);
    integrationService.createIntegrations(Arrays.asList(integrationVO));
    Mockito.verify(crudManager, times(1)).create(Mockito.any());
  }

  /**
   * Release locks test.
   */
  @Test
  public void releaseLocksTest() {
    integrationService.releaseLocks(0L);
    Mockito.verify(lockService, times(7)).removeLockByCriteria(Mockito.any());
  }

  /**
   * Adds the locks test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addLocksTest() throws EEAException {
    integrationService.addLocks(0L);
    Mockito.verify(lockService, times(6)).createLock(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
  }


  @Test
  public void getIntegrationById() {
    IntegrationVO integrationVO = new IntegrationVO();
    InternalOperationParameters parameter = new InternalOperationParameters();
    parameter.setParameter(IntegrationParams.FILE_EXTENSION);
    parameter.setValue(FileTypeEnum.CSV.getValue());
    Integration integration = new Integration();
    integration.setId(1L);
    integration.setInternalParameters(Arrays.asList(parameter));


    Mockito.when(integrationRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(integration));
    Mockito.when(integrationMapper.entityToClass(Mockito.any())).thenReturn(integrationVO);
    Assert.assertEquals(integrationVO, integrationService.getIntegration(1L));
  }
}
