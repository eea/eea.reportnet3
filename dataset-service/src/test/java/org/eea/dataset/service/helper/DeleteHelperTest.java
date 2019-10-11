package org.eea.dataset.service.helper;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import java.io.IOException;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.utils.KafkaSenderUtils;
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

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeDeleteProcessTest() throws EEAException, IOException, InterruptedException {
    doNothing().when(datasetService).deleteTableBySchema(Mockito.any(), Mockito.any());
    deleteHelper.executeDeleteProcess(1L, "");
    Mockito.verify(kafkaSenderUtils, times(2)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());

  }

}
