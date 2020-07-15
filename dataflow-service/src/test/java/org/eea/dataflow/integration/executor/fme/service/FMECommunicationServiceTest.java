package org.eea.dataflow.integration.executor.fme.service;

import static org.mockito.Mockito.times;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.integration.enums.FMEOperation;
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

@RunWith(MockitoJUnitRunner.class)
public class FMECommunicationServiceTest {

  @InjectMocks
  private FMECommunicationService fmeCommunicationService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void operationFinishedDesignTest() throws EEAException {
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
  public void operationFinishedReportingTest() throws EEAException {
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

  @Test(expected = UnsupportedOperationException.class)
  public void operationFinishedUnsupportedTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setDatasetId(1L);
    fmeOperationInfoVO.setDataflowId(1L);
    fmeOperationInfoVO.setProviderId(1L);
    fmeOperationInfoVO.setFileName("fileName");
    fmeOperationInfoVO.setFmeOperation(FMEOperation.EXPORT);
    try {
      fmeCommunicationService.operationFinished(fmeOperationInfoVO);
    } catch (UnsupportedOperationException e) {
      Assert.assertEquals("Not yet implemented", e.getMessage());
      throw e;
    }
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
}
