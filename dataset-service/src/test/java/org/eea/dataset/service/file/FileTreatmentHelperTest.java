package org.eea.dataset.service.file;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import java.io.IOException;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.helper.FileTreatmentHelper;
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
import org.springframework.mock.web.MockMultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class FileTreatmentHelperTest {

  @InjectMocks
  private FileTreatmentHelper fileTreatmentHelper;

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
  public void executeFileProcessTest() throws EEAException, IOException, InterruptedException {
    final MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    doNothing().when(datasetService).processFile(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
    doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(), Mockito.any());
    fileTreatmentHelper.executeFileProcess(1L, "file", fileNoExtension.getInputStream(), null);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
  }

}
