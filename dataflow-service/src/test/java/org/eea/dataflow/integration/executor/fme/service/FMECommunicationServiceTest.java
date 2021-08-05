package org.eea.dataflow.integration.executor.fme.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.FMECollection;
import org.eea.dataflow.integration.executor.fme.domain.FileSubmitResult;
import org.eea.dataflow.integration.executor.fme.domain.SubmitResult;
import org.eea.dataflow.integration.executor.fme.mapper.FMECollectionMapper;
import org.eea.dataflow.integration.executor.fme.service.impl.FMECommunicationServiceImpl;
import org.eea.dataflow.persistence.domain.FMEJob;
import org.eea.dataflow.persistence.repository.FMEJobRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAUnauthorizedException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class FMECommunicationServiceTest {

  @InjectMocks
  private FMECommunicationServiceImpl fmeCommunicationService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private FMECollectionMapper fmeCollectionMapper;

  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  @Mock
  private FMEJobRepository fmeJobRepository;

  @Mock
  private LockService lockService;

  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testSubmitAsyncJob() throws EEAException {
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeHost", "localhost:8080");
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeScheme", "https");

    SubmitResult preResult = new SubmitResult();
    preResult.setId(1);
    FMEAsyncJob fmeAsyncJob = new FMEAsyncJob();

    ResponseEntity<SubmitResult> checkResult = new ResponseEntity<>(preResult, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(checkResult);

    Integer result = fmeCommunicationService.submitAsyncJob("test", "test", fmeAsyncJob, null);
    Assert.assertEquals(preResult.getId(), result);
  }

  @Test
  public void testSendFile() throws EEAException {
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeHost", "localhost:8080");
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeScheme", "https");
    byte[] file = "e04fd020ea3a6910a2d808002b30309d".getBytes();
    FileSubmitResult fileSubmitResult = new FileSubmitResult();
    fileSubmitResult.setName("test");

    ResponseEntity<FileSubmitResult> checkResult =
        new ResponseEntity<>(fileSubmitResult, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(checkResult);

    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());

    FileSubmitResult result = fmeCommunicationService.sendFile(file, 1L, "1", "test");
    Assert.assertEquals(fileSubmitResult.getName(), result.getName());

  }



  @Test
  public void testReceiveFile() throws EEAException, IOException {
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeHost", "localhost:8080");
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeScheme", "https");
    byte[] file = "e04fd020ea3a6910a2d808002b30309d".getBytes();
    FileSubmitResult fileSubmitResult = new FileSubmitResult();
    fileSubmitResult.setName("test");

    ResponseEntity<byte[]> checkResult = new ResponseEntity<>(file, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(checkResult);

    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());


    byte[] lacastania = new byte[50];
    InputStream result = fmeCommunicationService.receiveFile(1L, 1L, "test");
    Assert.assertEquals(file.length, result.read(lacastania));
  }



  @Test
  public void testFindRepository() throws EEAException {
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeHost", "localhost:8080");
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeScheme", "https");

    FMECollectionVO fmeCollectionVO = new FMECollectionVO();
    FMECollection fmeCollection = new FMECollection();

    ResponseEntity<FMECollection> checkResult = new ResponseEntity<>(fmeCollection, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(checkResult);

    when(fmeCollectionMapper.entityToClass(Mockito.any())).thenReturn(fmeCollectionVO);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());

    FMECollectionVO result = fmeCommunicationService.findRepository(1L);
    Assert.assertEquals(fmeCollectionVO, result);
  }

  @Test
  public void testFindItems() throws EEAException {
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeHost", "localhost:8080");
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeScheme", "https");

    FMECollectionVO fmeCollectionVO = new FMECollectionVO();
    FMECollection fmeCollection = new FMECollection();

    ResponseEntity<FMECollection> checkResult = new ResponseEntity<>(fmeCollection, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(checkResult);

    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());

    when(fmeCollectionMapper.entityToClass(Mockito.any())).thenReturn(fmeCollectionVO);

    FMECollectionVO result = fmeCommunicationService.findItems("test", 1L);
    Assert.assertEquals(fmeCollectionVO, result);
  }

  @Test
  public void testcreateDirectory() throws EEAException {
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeHost", "localhost:8080");
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeScheme", "https");

    byte[] file = "e04fd020ea3a6910a2d808002b30309d".getBytes();
    ResponseEntity<byte[]> checkResult = new ResponseEntity<>(file, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(checkResult);

    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());

    HttpStatus result = fmeCommunicationService.createDirectory(1L, "test");
    Assert.assertEquals(HttpStatus.OK, result);
  }

  @Test
  public void authenticateAndAuthorizeTest() throws EEAException {
    TokenVO tokenVO = new TokenVO();
    Set<String> groups = new HashSet<>();
    groups.add("/group");
    tokenVO.setPreferredUsername("userName");
    tokenVO.setRoles(new HashSet<>());
    tokenVO.setGroups(groups);
    FMEJob fmeJob = new FMEJob();
    fmeJob.setUserName("userName");

    Mockito.when(userManagementControllerZull.authenticateUserByApiKey(Mockito.any()))
        .thenReturn(tokenVO);
    Mockito.when(fmeJobRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(fmeJob));
    Assert.assertEquals(fmeJob,
        fmeCommunicationService.authenticateAndAuthorize("sampleApiKey", 1L));
  }

  @Test(expected = EEAException.class)
  public void authenticateAndAuthorizeUnauthorizedTest() throws EEAException {
    try {
      fmeCommunicationService.authenticateAndAuthorize(null, 1L);
    } catch (EEAUnauthorizedException e) {
      Assert.assertEquals(EEAErrorMessage.UNAUTHORIZED, e.getMessage());
      throw e;
    }
  }

  @Test(expected = EEAException.class)
  public void authenticateAndAuthorizeForbiddenTest() throws EEAException {
    TokenVO tokenVO = new TokenVO();
    Set<String> groups = new HashSet<>();
    groups.add("/group");
    tokenVO.setPreferredUsername("userName");
    tokenVO.setRoles(new HashSet<>());
    tokenVO.setGroups(groups);
    FMEJob fmeJob = new FMEJob();
    fmeJob.setUserName("otherUserName");

    Mockito.when(userManagementControllerZull.authenticateUserByApiKey(Mockito.any()))
        .thenReturn(tokenVO);
    Mockito.when(fmeJobRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(fmeJob));
    try {
      fmeCommunicationService.authenticateAndAuthorize("sampleApiKey", 1L);
    } catch (EEAForbiddenException e) {
      Assert.assertEquals(EEAErrorMessage.FORBIDDEN, e.getMessage());
      throw e;
    }
  }

  @Test
  public void releaseNotificationsImportReportingCompletedTest() throws EEAException {
    File file1 = new File(this.getClass().getClassLoader().getResource("").getPath(), "1");
    File file2 = new File(file1, "Test.csv");
    file2.mkdirs();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setDatasetId(1L);
    fmeJob.setProviderId(1L);
    fmeJob.setOperation(IntegrationOperationTypeEnum.IMPORT);
    fmeJob.setFileName("Test.csv");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    fmeCommunicationService.releaseNotifications(fmeJob, 0L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void releaseNotificationsImportReportingFailedTest() throws EEAException {
    File file1 = new File(this.getClass().getClassLoader().getResource("").getPath(), "1");
    File file2 = new File(file1, "Test.csv");
    file2.mkdirs();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setDatasetId(1L);
    fmeJob.setProviderId(1L);
    fmeJob.setOperation(IntegrationOperationTypeEnum.IMPORT);
    fmeJob.setFileName("Test.csv");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    fmeCommunicationService.releaseNotifications(fmeJob, 1L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void releaseNotificationsImportDesignCompletedTest() throws EEAException {
    File file1 = new File(this.getClass().getClassLoader().getResource("").getPath(), "1");
    File file2 = new File(file1, "Test.csv");
    file2.mkdirs();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setDatasetId(1L);
    fmeJob.setProviderId(null);
    fmeJob.setOperation(IntegrationOperationTypeEnum.IMPORT);
    fmeJob.setFileName("Test.csv");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    fmeCommunicationService.releaseNotifications(fmeJob, 0L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  @Test
  public void releaseNotificationsImportDesignFailedTest() throws EEAException {
    File file1 = new File(this.getClass().getClassLoader().getResource("").getPath(), "1");
    File file2 = new File(file1, "Test.csv");
    file2.mkdirs();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setDatasetId(1L);
    fmeJob.setProviderId(null);
    fmeJob.setOperation(IntegrationOperationTypeEnum.IMPORT);
    fmeJob.setFileName("Test.csv");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    fmeCommunicationService.releaseNotifications(fmeJob, 1L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  @Test
  public void releaseNotificationsImportFromOtherSystemDesignTest() throws EEAException {
    File file1 = new File(this.getClass().getClassLoader().getResource("").getPath(), "1");
    File file2 = new File(file1, "Test.csv");
    file2.mkdirs();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setDatasetId(1L);
    fmeJob.setProviderId(null);
    fmeJob.setOperation(IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM);
    fmeJob.setFileName("Test.csv");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    fmeCommunicationService.releaseNotifications(fmeJob, 0L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void releaseNotificationsImportFromOtherSystemDesignFailedTest() throws EEAException {
    File file1 = new File(this.getClass().getClassLoader().getResource("").getPath(), "1");
    File file2 = new File(file1, "Test.csv");
    file2.mkdirs();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setDatasetId(1L);
    fmeJob.setProviderId(null);
    fmeJob.setOperation(IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM);
    fmeJob.setFileName("Test.csv");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    fmeCommunicationService.releaseNotifications(fmeJob, 1L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void releaseNotificationsImportFromOtherSystemReportingTest() throws EEAException {
    File file1 = new File(this.getClass().getClassLoader().getResource("").getPath(), "1");
    File file2 = new File(file1, "Test.csv");
    file2.mkdirs();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setDatasetId(1L);
    fmeJob.setProviderId(1L);
    fmeJob.setOperation(IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM);
    fmeJob.setFileName("Test.csv");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    fmeCommunicationService.releaseNotifications(fmeJob, 0L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  @Test
  public void releaseNotificationsImportFromOtherSystemReportingFailedTest() throws EEAException {
    File file1 = new File(this.getClass().getClassLoader().getResource("").getPath(), "1");
    File file2 = new File(file1, "Test.csv");
    file2.mkdirs();
    FMEJob fmeJob = new FMEJob();
    fmeJob.setDatasetId(1L);
    fmeJob.setProviderId(1L);
    fmeJob.setOperation(IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM);
    fmeJob.setFileName("Test.csv");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    fmeCommunicationService.releaseNotifications(fmeJob, 1L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void releaseNotificationsExportReportingCompletedTest() throws EEAException {
    FMEJob fmeJob = new FMEJob();
    fmeJob.setProviderId(1L);
    fmeJob.setOperation(IntegrationOperationTypeEnum.EXPORT);
    fmeCommunicationService.releaseNotifications(fmeJob, 0L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void releaseNotificationsExportReportingFailedTest() throws EEAException {
    FMEJob fmeJob = new FMEJob();
    fmeJob.setProviderId(null);
    fmeJob.setOperation(IntegrationOperationTypeEnum.EXPORT);
    fmeCommunicationService.releaseNotifications(fmeJob, 0L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void releaseNotificationsExportDesignCompletedTest() throws EEAException {
    FMEJob fmeJob = new FMEJob();
    fmeJob.setProviderId(1L);
    fmeJob.setOperation(IntegrationOperationTypeEnum.EXPORT);
    fmeCommunicationService.releaseNotifications(fmeJob, 1L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void releaseNotificationsExportDesignFailedTest() throws EEAException {
    FMEJob fmeJob = new FMEJob();
    fmeJob.setProviderId(null);
    fmeJob.setOperation(IntegrationOperationTypeEnum.EXPORT);
    fmeCommunicationService.releaseNotifications(fmeJob, 1L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void releaseNotificationsExportEUDatasetCompletedTest() throws EEAException {
    FMEJob fmeJob = new FMEJob();
    fmeJob.setProviderId(1L);
    fmeJob.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    fmeCommunicationService.releaseNotifications(fmeJob, 0L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void releaseNotificationsExportEUDatasetFailedTest() throws EEAException {
    FMEJob fmeJob = new FMEJob();
    fmeJob.setProviderId(null);
    fmeJob.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    fmeCommunicationService.releaseNotifications(fmeJob, 1L, true);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void udpateJobStatusSuccessTest() {
    Mockito.when(fmeJobRepository.save(Mockito.any())).thenReturn(new FMEJob());
    fmeCommunicationService.updateJobStatus(new FMEJob(), 0L);
    Mockito.verify(fmeJobRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void udpateJobStatusFailTest() {
    Mockito.when(fmeJobRepository.save(Mockito.any())).thenReturn(new FMEJob());
    fmeCommunicationService.updateJobStatus(new FMEJob(), 1L);
    Mockito.verify(fmeJobRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void updateJobStatusByIdTest() {
    Mockito.when(fmeJobRepository.findById(Mockito.any())).thenReturn(Optional.of(new FMEJob()));
    fmeCommunicationService.updateJobStatusById(0L, 0L);
    Mockito.verify(fmeJobRepository, times(1)).findById(Mockito.any());
  }

}
