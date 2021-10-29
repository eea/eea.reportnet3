package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataflow.integration.executor.IntegrationExecutorFactory;
import org.eea.dataflow.integration.executor.service.IntegrationExecutorService;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
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
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class IntegrationControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationControllerImplTest {

  /** The integration controller impl. */
  @InjectMocks
  private IntegrationControllerImpl integrationControllerImpl;

  /** The integration service. */
  @Mock
  private IntegrationService integrationService;

  /** The Lock service. */
  @Mock
  private LockService LockService;

  /** The integration executor factory. */
  @Mock
  private IntegrationExecutorFactory integrationExecutorFactory;

  /** The integration executor service. */
  @Mock
  private IntegrationExecutorService integrationExecutorService;



  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Find all integrations by criteria test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findAllIntegrationsByCriteriaTest() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setId(1L);
    integrationControllerImpl.findAllIntegrationsByCriteria(integrationVO);
    Mockito.verify(integrationService, times(1)).getAllIntegrationsByCriteria(Mockito.any());
  }

  /**
   * Find all integrations by criteria exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void findAllIntegrationsByCriteriaExceptionTest() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService)
          .getAllIntegrationsByCriteria(Mockito.any());
      integrationControllerImpl.findAllIntegrationsByCriteria(new IntegrationVO());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
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
    integrationControllerImpl.createIntegration(integrationVO);
    Mockito.verify(integrationService, times(1)).createIntegration(Mockito.any());
  }

  /**
   * Creates the integration exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createIntegrationExceptionTest() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService).createIntegration(Mockito.any());
      integrationControllerImpl.createIntegration(new IntegrationVO());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
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
    integrationControllerImpl.updateIntegration(integrationVO);
    Mockito.verify(integrationService, times(1)).updateIntegration(Mockito.any());
  }

  /**
   * Update integration exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateIntegrationExceptionTest() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService).updateIntegration(Mockito.any());
      integrationControllerImpl.updateIntegration(new IntegrationVO());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Delete integration test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteIntegrationTest() throws EEAException {
    integrationControllerImpl.deleteIntegration(1L, 1L);
    Mockito.verify(integrationService, times(1)).deleteIntegration(Mockito.any());
  }

  /**
   * Delete integration exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteIntegrationExceptionTest() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService).deleteIntegration(Mockito.any());
      integrationControllerImpl.deleteIntegration(null, null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Find extensions and operations test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findExtensionsAndOperationsTest() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put("datasetSchemaId", "datasetSchemaId");
    integrationControllerImpl.findExtensionsAndOperations(integrationVO);
    Mockito.verify(integrationService, times(1)).getOnlyExtensionsAndOperations(Mockito.any());
  }

  /**
   * Find extensions and operations exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void findExtensionsAndOperationsExceptionTest() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService)
          .getAllIntegrationsByCriteria(Mockito.any());
      integrationControllerImpl.findExtensionsAndOperations(new IntegrationVO());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Copy integrations test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void copyIntegrationsTest() throws EEAException {
    Map<String, String> dictionaryOriginTargetObjectId = new HashMap<>();
    dictionaryOriginTargetObjectId.put("5ce524fad31fc52540abae73", "5ce524fad31fc52540abae73");
    CopySchemaVO copy = new CopySchemaVO();
    copy.setDataflowIdDestination(1L);
    copy.setDictionaryOriginTargetObjectId(dictionaryOriginTargetObjectId);
    copy.setOriginDatasetSchemaIds(Arrays.asList("5ce524fad31fc52540abae73"));
    integrationControllerImpl.copyIntegrations(copy);
    Mockito.verify(integrationService, times(1)).copyIntegrations(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Copy integrations exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void copyIntegrationsExceptionTest() throws EEAException {
    try {
      Map<String, String> dictionaryOriginTargetObjectId = new HashMap<>();
      dictionaryOriginTargetObjectId.put("5ce524fad31fc52540abae73", "5ce524fad31fc52540abae73");
      CopySchemaVO copy = new CopySchemaVO();
      copy.setDataflowIdDestination(1L);
      copy.setDictionaryOriginTargetObjectId(dictionaryOriginTargetObjectId);
      copy.setOriginDatasetSchemaIds(Arrays.asList("5ce524fad31fc52540abae73"));
      Mockito.doThrow(EEAException.class).when(integrationService).copyIntegrations(Mockito.any(),
          Mockito.any(), Mockito.any());
      integrationControllerImpl.copyIntegrations(copy);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Creates the default integration test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createDefaultIntegrationTest() throws EEAException {
    Mockito.doNothing().when(integrationService).createDefaultIntegration(Mockito.any(),
        Mockito.any());
    integrationControllerImpl.createDefaultIntegration(1L, "5ce524fad31fc52540abae73");
    Mockito.verify(integrationService, times(1)).createDefaultIntegration(Mockito.any(),
        Mockito.any());
  }

  /**
   * Creates the default integration test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createDefaultIntegrationExceptionTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(integrationService)
        .createDefaultIntegration(Mockito.any(), Mockito.any());
    try {
      integrationControllerImpl.createDefaultIntegration(1L, "5ce524fad31fc52540abae73");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Find expor EU dataset integration by dataset id test.
   */
  @Test
  public void findExporEUDatasetIntegrationByDatasetIdTest() {
    Mockito.when(integrationService.getExportEUDatasetIntegration(Mockito.anyString()))
        .thenReturn(null);
    Assert.assertNull(
        integrationControllerImpl.findExportEUDatasetIntegration("5ce524fad31fc52540abae73", 0L));
  }

  /**
   * Execute EU dataset export test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeEUDatasetExportTest() throws EEAException {
    Mockito.doNothing().when(integrationService).addPopulateEUDatasetLock(Mockito.anyLong());
    Mockito.when(integrationService.executeEUDatasetExport(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    Mockito.doNothing().when(integrationService).releasePopulateEUDatasetLock(Mockito.anyLong());
    List<ExecutionResultVO> response = integrationControllerImpl.executeEUDatasetExport(1L);
    Assert.assertEquals(0, response.size());
  }

  /**
   * Execute EU dataset export exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void executeEUDatasetExportExceptionTest() throws EEAException {
    Mockito.doNothing().when(integrationService).addPopulateEUDatasetLock(Mockito.anyLong());
    Mockito.when(integrationService.executeEUDatasetExport(Mockito.anyLong()))
        .thenThrow(EEAException.class);
    Mockito.doNothing().when(integrationService).releasePopulateEUDatasetLock(Mockito.anyLong());
    try {
      integrationControllerImpl.executeEUDatasetExport(1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Find export integration test.
   */
  @Test
  public void findExportIntegrationTest() {
    IntegrationVO integrationVO = new IntegrationVO();
    Mockito.when(integrationService.getExportIntegration(Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(integrationVO);
    Assert.assertEquals(integrationVO,
        integrationControllerImpl.findExportIntegration("5ce524fad31fc52540abae73", 1L));
  }

  /**
   * Delete schema integrations test.
   */
  @Test
  public void deleteSchemaIntegrationsTest() {
    Mockito.doNothing().when(integrationService).deleteSchemaIntegrations(Mockito.anyString());
    integrationControllerImpl.deleteSchemaIntegrations("5ce524fad31fc52540abae73");
    Mockito.verify(integrationService, times(1)).deleteSchemaIntegrations(Mockito.anyString());
  }

  /**
   * Execute external integration test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeExternalIntegrationTest() throws EEAException {

    integrationControllerImpl.executeExternalIntegration(1L, 1L, false);
    Mockito.verify(integrationService, times(1)).executeExternalIntegration(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Execute external integration exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void executeExternalIntegrationExceptionTest() throws EEAException {

    Mockito.doThrow(EEAException.class).when(integrationService).executeExternalIntegration(
        Mockito.anyLong(), Mockito.anyLong(), Mockito.any(), Mockito.any());
    try {
      integrationControllerImpl.executeExternalIntegration(1L, 1L, false);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
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
    integrationControllerImpl.createIntegrations(Arrays.asList(integrationVO));
    Mockito.verify(integrationService, times(1)).createIntegrations(Mockito.any());
  }

  /**
   * Creates the integrations exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createIntegrationsExceptionTest() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService)
          .createIntegrations(Mockito.any());
      integrationControllerImpl.createIntegrations(Arrays.asList(new IntegrationVO()));
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }


  @Test
  public void findIntegrationByIdTest() throws EEAException {
    IntegrationVO integration = new IntegrationVO();
    integration.setId(1L);
    Mockito.when(integrationService.getIntegration(Mockito.anyLong())).thenReturn(integration);
    assertEquals(integration, integrationControllerImpl.findIntegrationById(1L));
  }

  @Test
  public void executeIntegrationProcessTest() {
    Mockito.when(integrationExecutorFactory.getExecutor(Mockito.any()))
        .thenReturn(integrationExecutorService);
    assertNull("assertion error",
        integrationControllerImpl.executeIntegrationProcess(null, null, null, null, null));
  }


  @Test
  public void testDeleteExportEuDatasetIntegration() throws EEAException {
    Mockito.doNothing().when(integrationService).deleteExportEuDataset(Mockito.anyString());
    integrationControllerImpl.deleteExportEuDatasetIntegration("5ce524fad31fc52540abae73");
    Mockito.verify(integrationService, times(1)).deleteExportEuDataset(Mockito.anyString());
  }

  @Test
  public void testDeleteExportEuDatasetIntegrationException() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService)
          .deleteExportEuDataset(Mockito.anyString());
      integrationControllerImpl.deleteExportEuDatasetIntegration("5ce524fad31fc52540abae73");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

}
