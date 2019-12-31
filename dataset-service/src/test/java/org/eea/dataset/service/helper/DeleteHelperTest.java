package org.eea.dataset.service.helper;

import static org.mockito.Mockito.times;
import java.io.IOException;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
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
    ThreadPropertiesManager.setVariable("user", "user");
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeDeleteProcessTest() throws EEAException, IOException, InterruptedException {
    Mockito.doNothing().when(datasetService).deleteTableBySchema(Mockito.any(), Mockito.any());
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    Mockito.doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    deleteHelper.executeDeleteProcess(1L, "");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }
}
