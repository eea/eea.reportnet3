package org.eea.dataflow.integration.executor.fme;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.integration.utils.IntegrationParams;
import org.eea.dataflow.persistence.repository.FMEJobRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
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

@RunWith(MockitoJUnitRunner.class)
public class FMEIntegrationExecutorServiceTest {

  @InjectMocks
  private FMEIntegrationExecutorService fmeIntegrationExecutorService;

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

  IntegrationVO integration = new IntegrationVO();

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    integration.setTool(IntegrationToolTypeEnum.FME);
    Map<String, String> internalParameters = new HashMap<String, String>();
    internalParameters.put(IntegrationParams.REPOSITORY, "test");
    integration.setInternalParameters(internalParameters);
    Map<String, String> externalParameters = new HashMap<String, String>();
    externalParameters.put(IntegrationParams.FILE_IS, "test");
    integration.setExternalParameters(externalParameters);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.initMocks(this);

  }


  @Test
  public void fmeExecutionExportTest() throws EEAException {
    integration.setOperation(IntegrationOperationTypeEnum.EXPORT);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dataset);

    when(fmeCommunicationService.createDirectory(Mockito.any(), Mockito.any()))
        .thenReturn(HttpStatus.OK);

    fmeIntegrationExecutorService.execute(IntegrationOperationTypeEnum.EXPORT, "test", 1L,
        integration);

    Mockito.verify(fmeJobRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void fmeExecutionImportTest() throws EEAException {
    integration.setOperation(IntegrationOperationTypeEnum.IMPORT);

    DataSetMetabaseVO dataset = new DataSetMetabaseVO();

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dataset);


    fmeIntegrationExecutorService.execute(IntegrationOperationTypeEnum.IMPORT, "test", 1L,
        integration);

    Mockito.verify(fmeJobRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void fmeExecutionExportEUDatasetTest() throws EEAException {
    integration.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);

    DataSetMetabaseVO dataset = new DataSetMetabaseVO();

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dataset);


    fmeIntegrationExecutorService.execute(IntegrationOperationTypeEnum.EXPORT_EU_DATASET, "test",
        1L, integration);

    Mockito.verify(fmeJobRepository, times(1)).save(Mockito.any());
  }
}
