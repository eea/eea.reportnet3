package org.eea.dataflow.integration.manager;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Optional;
import org.eea.dataflow.integration.crud.factory.manager.FMEIntegrationManager;
import org.eea.dataflow.mapper.IntegrationMapper;
import org.eea.dataflow.persistence.domain.ExternalOperationParameters;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.domain.InternalOperationParameters;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.dataflow.persistence.repository.OperationParametersRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
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
 * The Class FMEIntegrationManagerTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FMEIntegrationManagerTest {


  /** The integration manager. */
  @InjectMocks
  private FMEIntegrationManager integrationManager;

  /** The integration repository. */
  @Mock
  private IntegrationRepository integrationRepository;

  /** The operation parameters repository. */
  @Mock
  private OperationParametersRepository operationParametersRepository;

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
   * Test get integration by id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetIntegrationById() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setId(1L);
    Integration integration = new Integration();
    integration.setId(1L);
    Mockito.when(integrationRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(integration));
    integrationManager.get(integrationVO);
    Mockito.verify(integrationRepository, times(1)).findById(Mockito.any());
  }


  /**
   * Test get integration by id dataset schema.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetIntegrationByIdDatasetSchema() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.getInternalParameters().put("datasetSchemaId", "test1");
    Integration integration = new Integration();
    integration.setId(1L);
    integrationManager.get(integrationVO);
    Mockito.verify(integrationRepository, times(1)).findByInternalOperationParameter(Mockito.any(),
        Mockito.any());
  }

  /**
   * Test get integration exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetIntegrationException() throws EEAException {
    try {
      IntegrationVO integrationVO = new IntegrationVO();
      integrationManager.get(integrationVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
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

    integrationManager.create(integrationVO);
    Mockito.verify(integrationRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test create exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testCreateException() throws EEAException {
    try {
      IntegrationVO integrationVO = new IntegrationVO();
      integrationManager.create(integrationVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
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
    Integration integration = new Integration();
    integration.setId(1L);
    integration.setInternalParameters(new ArrayList<>());
    integration.setExternalParameters(new ArrayList<>());
    integration.getInternalParameters().add(new InternalOperationParameters());
    integration.getExternalParameters().add(new ExternalOperationParameters());
    Mockito.when(integrationRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(integration));
    Mockito.when(integrationMapper.classToEntity(Mockito.any())).thenReturn(integration);
    integrationManager.update(integrationVO);
    Mockito.verify(integrationRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test update integration exception 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testUpdateIntegrationException2() throws EEAException {
    try {
      IntegrationVO integrationVO = new IntegrationVO();
      integrationVO.setId(1L);
      Integration integration = new Integration();
      integration.setId(1L);
      Mockito.when(integrationRepository.findById(Mockito.anyLong()))
          .thenReturn(Optional.of(integration));
      integrationManager.update(integrationVO);
    } catch (ResponseStatusException e) {
      assertEquals("Parameters incorrect", e.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Test update integration exception 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testUpdateIntegrationException1() throws EEAException {
    try {
      IntegrationVO integrationVO = new IntegrationVO();
      integrationVO.setId(1L);
      integrationVO.getInternalParameters().put("datasetSchemaId", "test1");
      integrationVO.getInternalParameters().put("dataflowId", "1");
      Integration integration = new Integration();
      integration.setId(1L);
      Mockito.when(integrationRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
      integrationManager.update(integrationVO);
    } catch (EEAException e) {
      assertEquals("Integration not found", e.getMessage());
    }
  }

  /**
   * Test delete integration.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testDeleteIntegration() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setId(1L);
    integrationManager.delete(integrationVO);
    Mockito.verify(integrationRepository, times(1)).deleteById(Mockito.any());
  }

  /**
   * Test delete exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testDeleteException() throws EEAException {
    try {
      IntegrationVO integrationVO = new IntegrationVO();
      integrationManager.delete(integrationVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Test get tool type.
   */
  @Test
  public void testGetToolType() {
    Assert.assertEquals("tool equals", IntegrationToolTypeEnum.FME,
        integrationManager.getToolType());

  }


}
