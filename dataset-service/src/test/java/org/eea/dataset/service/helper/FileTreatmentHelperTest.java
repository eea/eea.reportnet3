package org.eea.dataset.service.helper;

import static org.mockito.Mockito.times;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileTreatmentHelperTest {

  @InjectMocks
  private FileTreatmentHelper fileTreatmentHelper;

  @Mock
  private DatasetService datasetService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private LockService lockService;

  @Mock
  private DataSetMapper dataSetMapper;

  @Mock
  private DataSetVO datasetVO;

  @Mock
  private DatasetValue datasetValue;

  @Mock
  private List<TableValue> listTableValue;

  @Mock
  private TableValue tableValue;

  @Mock
  private List<RecordValue> listRecordValue;

  @Mock
  private Stream<TableValue> tableValueStream;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeFileProcessTest1() throws EEAException, IOException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito
        .when(
            datasetService.processFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.doNothing().when(datasetVO).setId(Mockito.any());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetValue.getTableValues()).thenReturn(listTableValue);
    Mockito.when(listTableValue.get(Mockito.anyInt())).thenReturn(tableValue);
    Mockito.when(tableValue.getRecords()).thenReturn(listRecordValue);
    Mockito.doNothing().when(tableValue).setRecords(Mockito.any());
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Mockito.when(listTableValue.stream()).thenReturn(tableValueStream);
    Mockito.when(tableValueStream.filter(Mockito.any())).thenReturn(tableValueStream);
    Mockito.doNothing().when(tableValueStream).forEach(Mockito.any());
    Mockito.when(listRecordValue.size()).thenReturn(2000);
    Mockito.when(listRecordValue.subList(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(new ArrayList<RecordValue>());
    Mockito.doNothing().when(datasetService).saveAllRecords(Mockito.any(), Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseKafkaEvent(Mockito.any(), Mockito.any());
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    fileTreatmentHelper.executeFileProcess(1L, "fileName", new ByteArrayInputStream(new byte[0]),
        "5d4abe555b1c1e0001477410", "user");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

  @Test
  public void executeFileProcessTest2() throws EEAException, IOException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito
        .when(
            datasetService.processFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.doNothing().when(datasetVO).setId(Mockito.any());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetValue.getTableValues()).thenReturn(listTableValue);
    Mockito.when(listTableValue.get(Mockito.anyInt())).thenReturn(tableValue);
    Mockito.when(tableValue.getRecords()).thenReturn(listRecordValue);
    Mockito.doNothing().when(tableValue).setRecords(Mockito.any());
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(null);
    Mockito.doNothing().when(datasetService).saveTable(Mockito.any(), Mockito.any());
    Mockito.when(listRecordValue.size()).thenReturn(1);
    Mockito.when(listRecordValue.subList(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(new ArrayList<RecordValue>());
    Mockito.doNothing().when(datasetService).saveAllRecords(Mockito.any(), Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseKafkaEvent(Mockito.any(), Mockito.any());
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    fileTreatmentHelper.executeFileProcess(1L, "fileName", new ByteArrayInputStream(new byte[0]),
        "5d4abe555b1c1e0001477410", "user");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

  @Test
  public void executeFileProcessTest3() throws EEAException, IOException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito
        .when(
            datasetService.processFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.doNothing().when(datasetVO).setId(Mockito.any());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(null);
    Mockito.doNothing().when(kafkaSenderUtils).releaseKafkaEvent(Mockito.any(), Mockito.any());
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    fileTreatmentHelper.executeFileProcess(1L, "fileName", new ByteArrayInputStream(new byte[0]),
        "5d4abe555b1c1e0001477410", "user");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }
}
