package org.eea.dataflow.integration.manager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eea.dataflow.integration.crud.factory.manager.FMEIntegrationManager;
import org.eea.dataflow.mapper.IntegrationMapper;
import org.eea.dataflow.persistence.domain.ExternalOperationParameters;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.domain.InternalOperationParameters;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.dataflow.persistence.repository.OperationParametersRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
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

  /** The Constant DATASETSCHEMAID: {@value}. */
  private static final String DATASET_SCHEMA_ID = "datasetSchemaId";

  /** The Constant DATAFLOW_ID: {@value}. */
  private static final String DATAFLOW_ID = "dataflowId";

  /** The Constant DATASET_ID: {@value}. */
  private static final String DATASET_ID = "datasetId";

  /** The Constant PROCESS_NAME: {@value}. */
  private static final String PROCESS_NAME = "processName";

  /** The Constant REPOSITORY: {@value}. */
  private static final String REPOSITORY = "repository";

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
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
  @Test(expected = ResponseStatusException.class)
  public void testGetIntegrationException() throws EEAException {
    try {
      IntegrationVO integrationVO = new IntegrationVO();
      integrationManager.get(integrationVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
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
    integrationManager.create(integrationVO);
    Mockito.verify(integrationRepository, times(1)).save(Mockito.any());
  }

  @Test(expected = EEAException.class)
  public void createIntegrationDuplicatedNameTest() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.getInternalParameters().put("datasetSchemaId", "test1");
    integrationVO.getInternalParameters().put("dataflowId", "1");
    Integration integration = new Integration();
    integration.setName("");
    List<InternalOperationParameters> internalParameters = new ArrayList<>();
    InternalOperationParameters internalParameter = new InternalOperationParameters();
    internalParameter.setParameter("datasetSchemaId");
    internalParameter.setValue("test1");
    internalParameter.setIntegration(integration);
    internalParameters.add(internalParameter);
    integration.setInternalParameters(internalParameters);
    Mockito.when(integrationMapper.classToEntity(Mockito.any())).thenReturn(integration);
    Mockito
        .when(integrationRepository.findByInternalOperationParameter(Mockito.any(), Mockito.any()))
        .thenReturn(Arrays.asList(integration));
    try {
      integrationManager.create(integrationVO);
    } catch (EEAException e) {
      throw e;
    }
  }

  /**
   * Test create exception.
   * 
   * @throws EEAException
   */
  @Test(expected = ResponseStatusException.class)
  public void testCreateException() throws EEAException {
    try {
      IntegrationVO integrationVO = new IntegrationVO();
      integrationManager.create(integrationVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
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
  @Test(expected = ResponseStatusException.class)
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
      throw e;
    }
  }

  /**
   * Test update integration exception 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
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
    integrationManager.delete(1L);
    Mockito.verify(integrationRepository, times(1)).deleteById(Mockito.any());
  }

  /**
   * Test delete exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteException() throws EEAException {
    try {
      integrationManager.delete(null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
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

  @Test
  public void updateExportEUDatasetTest() throws EEAException {
    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put(DATAFLOW_ID, "1");
    internalParameters.put(DATASET_ID, "1");
    internalParameters.put(DATASET_SCHEMA_ID, "5eb426a506390651aced7c95");
    internalParameters.put(REPOSITORY, "ReportNetTesting");
    internalParameters.put(PROCESS_NAME, "Export_EU_dataset.fmw");

    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    integrationVO.setId(1L);
    integrationVO.setName("name");
    integrationVO.setDescription("description");
    integrationVO.setInternalParameters(internalParameters);

    Integration integration = new Integration();
    integration.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    integration.setId(1L);

    Mockito.when(integrationRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(integration));
    Mockito.when(integrationMapper.classToEntity(Mockito.any())).thenReturn(integration);
    Mockito.when(integrationRepository.save(Mockito.any())).thenReturn(null);
    integrationManager.update(integrationVO);
    Mockito.verify(integrationRepository, times(1)).save(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void updateExportEUDatasetExceptionTest() throws EEAException {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    integrationVO.setId(1L);

    Integration integration = new Integration();
    integration.setOperation(IntegrationOperationTypeEnum.EXPORT);

    Mockito.when(integrationRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(integration));

    try {
      integrationManager.update(integrationVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.OPERATION_TYPE_NOT_EDITABLE, e.getReason());
      throw e;
    }
  }
}
