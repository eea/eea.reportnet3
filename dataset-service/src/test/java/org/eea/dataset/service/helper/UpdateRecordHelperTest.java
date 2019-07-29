package org.eea.dataset.service.helper;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.kafka.io.KafkaSender;
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
public class UpdateRecordHelperTest {

  @InjectMocks
  private UpdateRecordHelper updateRecordHelper;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private DatasetService datasetService;

  @Mock
  private KafkaSender kafkaSender;

  /** The records. */
  private List<RecordVO> records;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    records = new ArrayList<>();
  }

  @Test
  public void executeUpdateProcessTest() throws EEAException, IOException, InterruptedException {
    doNothing().when(datasetService).updateRecords(Mockito.any(), Mockito.any());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    updateRecordHelper.executeUpdateProcess(1L, records);
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }

  @Test
  public void executeCreateProcessTest() throws EEAException, IOException, InterruptedException {
    doNothing().when(datasetService).updateRecords(Mockito.any(), Mockito.any());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    updateRecordHelper.executeCreateProcess(1L, records, "");
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }

  @Test
  public void executeDeleteProcessTest() throws EEAException, IOException, InterruptedException {
    List<Long> recordIds = new ArrayList<>();
    doNothing().when(datasetService).deleteRecords(Mockito.any(), Mockito.any());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    updateRecordHelper.executeDeleteProcess(1L, recordIds);
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }

}
