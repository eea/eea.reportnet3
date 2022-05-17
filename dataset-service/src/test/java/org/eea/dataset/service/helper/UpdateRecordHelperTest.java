package org.eea.dataset.service.helper;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
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
import org.springframework.test.util.ReflectionTestUtils;

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

  @Mock
  private DatasetSchemaService datasetSchemaService;

  @Mock
  private DataSchemaMapper dataSchemaMapper;

  @Mock
  DatasetMetabaseService datasetMetabaseService;

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private FileTreatmentHelper fileTreatmentHelper;

  /** The records. */
  private List<RecordVO> records;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
    records = new ArrayList<>();
    ReflectionTestUtils.setField(updateRecordHelper, "fieldBatchSize", 1);
  }

  @Test
  public void executeUpdateProcessTest() throws EEAException, IOException, InterruptedException {
    doNothing().when(datasetService).updateRecords(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    updateRecordHelper.executeUpdateProcess(1L, records, false);
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }

  @Test
  public void executeCreateProcessTest() throws EEAException, IOException, InterruptedException {
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    Optional<DataSetSchema> datasetSchema = Optional.ofNullable(new DataSetSchema());
    Mockito.when(schemasRepository.findById(new ObjectId("5cf0e9b3b793310e9ceca190")))
        .thenReturn(datasetSchema);
    updateRecordHelper.executeCreateProcess(1L, records, "");
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }

  @Test
  public void executeMultiCreateProcessTest()
      throws EEAException, IOException, InterruptedException {
    List<TableVO> tables = new ArrayList<>();
    tables.add(new TableVO());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    updateRecordHelper.executeMultiCreateProcess(1L, tables);
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }

  @Test
  public void executeDeleteProcessTest() throws EEAException, IOException, InterruptedException {
    doNothing().when(datasetService).deleteRecord(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    updateRecordHelper.executeDeleteProcess(1L, "1L", false);
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }

  @Test
  public void executeUpdateFieldProcessTest()
      throws EEAException, IOException, InterruptedException {
    doNothing().when(datasetService).updateField(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    Optional<DataSetSchema> datasetSchema = Optional.ofNullable(new DataSetSchema());
    Mockito.when(schemasRepository.findById(new ObjectId("5cf0e9b3b793310e9ceca190")))
        .thenReturn(datasetSchema);
    updateRecordHelper.executeFieldUpdateProcess(1L, new FieldVO(), false);
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }

  @Test
  public void propagateNewFieldDesignTest() {
    updateRecordHelper.propagateNewFieldDesign(1L, "5cf0e9b3b793310e9ceca190", 1, 1, "testUuid",
        "5cf0e9b3b793310e9ceca190", DataType.TEXT);
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }


}
