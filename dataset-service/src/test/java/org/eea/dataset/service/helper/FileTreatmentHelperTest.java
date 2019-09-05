package org.eea.dataset.service.helper;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
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

  @Mock
  private DataSetMapper dataSetMapper;

  @Mock
  private TableRepository tableRepository;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeFileProcessTest() throws EEAException, IOException, InterruptedException {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    Mockito
        .when(
            datasetService.processFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
    final DatasetValue entityValue = new DatasetValue();
    final ArrayList<TableValue> tableValues = new ArrayList<>();
    ArrayList<RecordValue> records = new ArrayList<>();
    TableValue table = new TableValue();
    RecordValue record = new RecordValue();
    records.add(record);
    table.setRecords(records);
    tableValues.add(table);
    entityValue.setId(1L);
    entityValue.setTableValues(tableValues);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(entityValue);
    when(datasetService.findTableIdByTableSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(), Mockito.any());
    fileTreatmentHelper.executeFileProcess(1L, "file", file.getInputStream(), null);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void executeFileProcessTestOldTable()
      throws EEAException, IOException, InterruptedException {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    Mockito
        .when(
            datasetService.processFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
    final DatasetValue entityValue = new DatasetValue();
    final ArrayList<TableValue> tableValues = new ArrayList<>();
    ArrayList<RecordValue> records = new ArrayList<>();
    TableValue table = new TableValue();
    RecordValue record = new RecordValue();
    records.add(record);
    table.setRecords(records);
    table.setIdTableSchema("");
    tableValues.add(table);
    entityValue.setId(1L);
    entityValue.setTableValues(tableValues);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(entityValue);
    when(datasetService.findTableIdByTableSchema(Mockito.any(), Mockito.any())).thenReturn(1L);
    doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(), Mockito.any());
    fileTreatmentHelper.executeFileProcess(1L, "file", file.getInputStream(), null);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
  }

  @Test(expected = IOException.class)
  public void executeFileProcessMapperErrorTest()
      throws EEAException, IOException, InterruptedException {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    Mockito
        .when(
            datasetService.processFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(null);
    fileTreatmentHelper.executeFileProcess(1L, "file", file.getInputStream(), null);
  }


}
