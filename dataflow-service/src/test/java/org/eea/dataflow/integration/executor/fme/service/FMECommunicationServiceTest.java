package org.eea.dataflow.integration.executor.fme.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.FMECollection;
import org.eea.dataflow.integration.executor.fme.domain.FileSubmitResult;
import org.eea.dataflow.integration.executor.fme.domain.SubmitResult;
import org.eea.dataflow.integration.executor.fme.mapper.FMECollectionMapper;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.integration.enums.FMEOperation;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.eea.kafka.utils.KafkaSenderUtils;
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
  private FMECommunicationService fmeCommunicationService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private FMECollectionMapper fmeCollectionMapper;

  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void operationFinishedImportDesignTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setDatasetId(1L);
    fmeOperationInfoVO.setDataflowId(1L);
    fmeOperationInfoVO.setFileName("fileName");
    fmeOperationInfoVO.setFmeOperation(FMEOperation.IMPORT);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    fmeCommunicationService.operationFinished(fmeOperationInfoVO);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    Mockito.verify(kafkaSenderUtils, times(1)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void operationFinishedImportReportingTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setDatasetId(1L);
    fmeOperationInfoVO.setDataflowId(1L);
    fmeOperationInfoVO.setProviderId(1L);
    fmeOperationInfoVO.setFileName("fileName");
    fmeOperationInfoVO.setFmeOperation(FMEOperation.IMPORT);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    fmeCommunicationService.operationFinished(fmeOperationInfoVO);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    Mockito.verify(kafkaSenderUtils, times(1)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void operationFinishedExporttDesignTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setDatasetId(1L);
    fmeOperationInfoVO.setDataflowId(1L);
    fmeOperationInfoVO.setFileName("fileName");
    fmeOperationInfoVO.setFmeOperation(FMEOperation.EXPORT);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    fmeCommunicationService.operationFinished(fmeOperationInfoVO);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void operationFinishedExportReportingTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setDatasetId(1L);
    fmeOperationInfoVO.setDataflowId(1L);
    fmeOperationInfoVO.setProviderId(1L);
    fmeOperationInfoVO.setFileName("fileName");
    fmeOperationInfoVO.setFmeOperation(FMEOperation.EXPORT);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    fmeCommunicationService.operationFinished(fmeOperationInfoVO);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  @Test
  public void operationFinishedKafkaExceptionTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setDatasetId(1L);
    fmeOperationInfoVO.setDataflowId(1L);
    fmeOperationInfoVO.setProviderId(1L);
    fmeOperationInfoVO.setFileName("fileName");
    fmeOperationInfoVO.setFmeOperation(FMEOperation.IMPORT);
    Mockito.doThrow(EEAException.class).when(kafkaSenderUtils)
        .releaseNotificableKafkaEvent(Mockito.any(), Mockito.any(), Mockito.any());
    fmeCommunicationService.operationFinished(fmeOperationInfoVO);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    Mockito.verify(kafkaSenderUtils, times(0)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
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

    Integer result = fmeCommunicationService.submitAsyncJob("test", "test", fmeAsyncJob);
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

    FileSubmitResult result = fmeCommunicationService.sendFile(file, 1L, "1", "test");
    Assert.assertEquals(fileSubmitResult.getName(), result.getName());

  }

  @Test
  public void testReceiveFile() throws EEAException {
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeHost", "localhost:8080");
    ReflectionTestUtils.setField(fmeCommunicationService, "fmeScheme", "https");
    byte[] file = "e04fd020ea3a6910a2d808002b30309d".getBytes();
    FileSubmitResult fileSubmitResult = new FileSubmitResult();
    fileSubmitResult.setName("test");

    ResponseEntity<FileSubmitResult> checkResult =
        new ResponseEntity<>(fileSubmitResult, HttpStatus.OK);

    Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(checkResult);

    FileSubmitResult result = fmeCommunicationService.receiveFile(file, 1L, "1", "test");
    Assert.assertEquals(fileSubmitResult.getName(), result.getName());
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

    FMECollectionVO result = fmeCommunicationService.findRepository();
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

    when(fmeCollectionMapper.entityToClass(Mockito.any())).thenReturn(fmeCollectionVO);

    FMECollectionVO result = fmeCommunicationService.findItems("test");
    Assert.assertEquals(fmeCollectionVO, result);
  }
}
