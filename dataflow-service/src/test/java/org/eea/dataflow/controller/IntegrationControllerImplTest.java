package org.eea.dataflow.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
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
    integrationControllerImpl.findAllIntegrationsByCriteria(integrationVO);
    Mockito.verify(integrationService, times(1)).getAllIntegrationsByCriteria(Mockito.any());
  }


  /**
   * Test get integration exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetIntegrationException() throws EEAException {
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
   * Test create integration.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testCreateIntegration() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.getInternalParameters().put("datasetSchemaId", "test1");
    integrationVO.getInternalParameters().put("dataflowId", "1");
    integrationControllerImpl.createIntegration(integrationVO);
    Mockito.verify(integrationService, times(1)).createIntegration(Mockito.any());
  }



  /**
   * Test create integration exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testCreateIntegrationException() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService).createIntegration(Mockito.any());
      integrationControllerImpl.createIntegration(new IntegrationVO());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
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
    integrationControllerImpl.updateIntegration(integrationVO);
    Mockito.verify(integrationService, times(1)).updateIntegration(Mockito.any());
  }

  /**
   * Test update integration exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateIntegrationException() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService).updateIntegration(Mockito.any());
      integrationControllerImpl.updateIntegration(new IntegrationVO());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Test delete integration.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testDeleteIntegration() throws EEAException {

    integrationControllerImpl.deleteIntegration(1L);
    Mockito.verify(integrationService, times(1)).deleteIntegration(Mockito.any());
  }


  /**
   * Test delete integration exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteIntegrationException() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService).deleteIntegration(Mockito.any());
      integrationControllerImpl.deleteIntegration(null);
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
   * Find extensions and operations test exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void findExtensionsAndOperationsTestException() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(integrationService)
          .getAllIntegrationsByCriteria(Mockito.any());
      integrationControllerImpl.findExtensionsAndOperations(new IntegrationVO());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  @Test
  public void testCopyIntegrations() throws EEAException {
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

  @Test(expected = ResponseStatusException.class)
  public void testCopyIntegrationsException() throws EEAException {
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

}
