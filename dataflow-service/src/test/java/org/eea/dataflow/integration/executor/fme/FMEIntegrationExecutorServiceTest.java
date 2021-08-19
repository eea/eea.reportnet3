package org.eea.dataflow.integration.executor.fme;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.persistence.domain.FMEJob;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.repository.FMEJobRepository;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.thread.ThreadPropertiesManager;
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

/**
 * The Class FMEIntegrationExecutorServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FMEIntegrationExecutorServiceTest {

  /** The fme integration executor service. */
  @InjectMocks
  private FMEIntegrationExecutorService fmeIntegrationExecutorService;

  /** The fme communication service. */
  @Mock
  private FMECommunicationService fmeCommunicationService;

  /** The data set metabase controller zuul. */
  @Mock
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The data set controller zuul. */
  @Mock
  private DataSetControllerZuul dataSetControllerZuul;

  /** The user management controller. */
  @Mock
  private UserManagementController userManagementController;

  /** The FME job repository. */
  @Mock
  FMEJobRepository fmeJobRepository;

  /** The integration repository. */
  @Mock
  IntegrationRepository integrationRepository;

  /** The integrationVO. */
  IntegrationVO integrationVO = new IntegrationVO();

  /** The integration. */
  Integration integration = new Integration();

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    integrationVO.setTool(IntegrationToolTypeEnum.FME);
    Map<String, String> internalParameters = new HashMap<String, String>();
    internalParameters.put(IntegrationParams.REPOSITORY, "test");
    integrationVO.setInternalParameters(internalParameters);
    Map<String, String> externalParameters = new HashMap<String, String>();
    externalParameters.put(IntegrationParams.FILE_IS, "test");
    integrationVO.setExternalParameters(externalParameters);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Fme execution export test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void fmeExecutionExportTest() throws EEAException {
    integration.setOperation(IntegrationOperationTypeEnum.EXPORT);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setId(1L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dataset);
    when(fmeCommunicationService.createDirectory(Mockito.any(), Mockito.any()))
        .thenReturn(HttpStatus.OK);
    when(fmeJobRepository.save(Mockito.any())).thenReturn(fmeJob);
    when(integrationRepository.findById(Mockito.any())).thenReturn(Optional.of(integration));
    fmeIntegrationExecutorService.execute(IntegrationOperationTypeEnum.EXPORT, "test", 1L,
        integrationVO);
    Mockito.verify(fmeJobRepository, times(2)).save(Mockito.any());
  }

  /**
   * Fme execution import test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void fmeExecutionImportTest() throws EEAException {
    integration.setOperation(IntegrationOperationTypeEnum.IMPORT);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setId(1L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dataset);
    when(integrationRepository.findById(Mockito.any())).thenReturn(Optional.of(integration));
    when(fmeJobRepository.save(Mockito.any())).thenReturn(fmeJob);
    fmeIntegrationExecutorService.execute(IntegrationOperationTypeEnum.IMPORT, "test", 1L,
        integrationVO);
    Mockito.verify(fmeJobRepository, times(2)).save(Mockito.any());
  }

  /**
   * Fme execution importfrom other sistem test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void fmeExecutionImportfromOtherSistemTest() throws EEAException {
    integration.setOperation(IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setId(1L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dataset);
    when(fmeJobRepository.save(Mockito.any())).thenReturn(fmeJob);
    when(integrationRepository.findById(Mockito.any())).thenReturn(Optional.of(integration));
    fmeIntegrationExecutorService.execute(IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM,
        "test", 1L, integrationVO);
    Mockito.verify(fmeJobRepository, times(2)).save(Mockito.any());
  }

  /**
   * Fme execution export EU dataset test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void fmeExecutionExportEUDatasetTest() throws EEAException {
    integration.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setId(1L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dataset);
    when(fmeJobRepository.save(Mockito.any())).thenReturn(fmeJob);
    when(integrationRepository.findById(Mockito.any())).thenReturn(Optional.of(integration));
    fmeIntegrationExecutorService.execute(IntegrationOperationTypeEnum.EXPORT_EU_DATASET, "test",
        1L, integrationVO);
    Mockito.verify(fmeJobRepository, times(2)).save(Mockito.any());
  }
}
