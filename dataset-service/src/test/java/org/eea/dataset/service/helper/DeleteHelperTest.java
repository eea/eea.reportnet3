package org.eea.dataset.service.helper;

import static org.mockito.Mockito.times;
import java.io.IOException;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class DeleteHelperTest {

  @InjectMocks
  private DeleteHelper deleteHelper;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private DatasetService datasetService;

  @Mock
  private LockService lockService;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("user", "password"));
    ThreadPropertiesManager.setVariable("user", "user");
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void executeDeleteProcessTest() throws EEAException, IOException, InterruptedException {
    Mockito.doNothing().when(datasetService).deleteTableBySchema(Mockito.any(), Mockito.any());
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    Mockito.doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    deleteHelper.executeDeleteTableProcess(1L, "");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void executeDeleteDatasetProcessTest()
      throws EEAException, IOException, InterruptedException {
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    Mockito.doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    deleteHelper.executeDeleteDatasetProcess(1L, false);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void executeDeleteImportDataAsyncBeforeReplacingTest()
      throws EEAException, IOException, InterruptedException {

    Mockito.doNothing().when(kafkaSenderUtils).releaseKafkaEvent(Mockito.any(), Mockito.any());
    deleteHelper.executeDeleteImportDataAsyncBeforeReplacing(1L, 1L,
        IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }
}
