package org.eea.dataset.io.kafka.commands;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * The Class SpreadDataCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class SpreadDataCommandTest {

  /** The create connection command. */
  @InjectMocks
  private SpreadDataCommand spreadDataCommand;

  /** The data set metabase repository. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The design dataset repository. */
  @Mock
  private DesignDatasetRepository designDatasetRepository;

  /** The record repository. */
  @Mock
  private RecordRepository recordRepository;

  /** The field repository. */
  @Mock
  private FieldRepository fieldRepository;

  /** The partition data set metabase repository. */
  @Mock
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The schemas repository. */
  @Mock
  private SchemasRepository schemasRepository;


  /** The table repository. */
  @Mock
  private TableRepository tableRepository;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The result. */
  private Map<String, Object> result = new HashMap<>();

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    result.put("dataset_id", "1");
    result.put("idDatasetSchema", new ObjectId().toString());
    eeaEventVO = new EEAEventVO();
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.SPREAD_DATA_EVENT, spreadDataCommand.getEventType());
  }


  /**
   * Execute test.
   */
  @Test
  public void executeTest() {
    eeaEventVO.setData(result);
    eeaEventVO.setEventType(EventType.SPREAD_DATA_EVENT);
    DesignDataset desingDataset = new DesignDataset();
    desingDataset.setId(1L);
    List<DesignDataset> desingDatasetList = new ArrayList<>();
    desingDatasetList.add(desingDataset);
    when(designDatasetRepository.findByDataflowId(Mockito.anyLong())).thenReturn(desingDatasetList);
    spreadDataCommand.execute(eeaEventVO);
    Mockito.verify(designDatasetRepository, times(1)).findByDataflowId(Mockito.anyLong());
  }

  /**
   * Execute test no desing.
   */
  @Test
  public void executeTestToPrefill() {
    eeaEventVO.setData(result);
    eeaEventVO.setEventType(EventType.SPREAD_DATA_EVENT);
    DesignDataset desingDataset = new DesignDataset();
    desingDataset.setId(2L);
    List<DesignDataset> desingDatasetList = new ArrayList<>();
    desingDatasetList.add(desingDataset);
    when(designDatasetRepository.findByDataflowId(Mockito.anyLong())).thenReturn(desingDatasetList);
    DataSetSchema schema = new DataSetSchema();
    TableSchema desingTableSchema = new TableSchema();
    desingTableSchema.setToPrefill(Boolean.TRUE);
    desingTableSchema.setIdTableSchema(new ObjectId());
    List<TableSchema> desingTableSchemas = new ArrayList<>();
    desingTableSchemas.add(desingTableSchema);
    schema.setTableSchemas(desingTableSchemas);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    List<RecordValue> recordDesignValues = new ArrayList<>();
    RecordValue record = new RecordValue();
    TableValue table = new TableValue();
    table.setId(1L);
    record.setTableValue(table);
    recordDesignValues.add(record);
    when(recordRepository.findByTableValueAllRecords(Mockito.any())).thenReturn(recordDesignValues);
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue field = new FieldValue();
    fieldValues.add(field);
    when(fieldRepository.findByRecord(Mockito.any())).thenReturn(fieldValues);
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_id(Mockito.any()))
        .thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(tableRepository.findIdByIdTableSchema(Mockito.any())).thenReturn(1L);
    spreadDataCommand.execute(eeaEventVO);
    Mockito.verify(designDatasetRepository, times(1)).findByDataflowId(Mockito.anyLong());
  }


  /**
   * Execute test not to prefill.
   */
  @Test
  public void executeTestNotToPrefill() {
    eeaEventVO.setData(result);
    eeaEventVO.setEventType(EventType.SPREAD_DATA_EVENT);
    DesignDataset desingDataset = new DesignDataset();
    desingDataset.setId(2L);
    List<DesignDataset> desingDatasetList = new ArrayList<>();
    desingDatasetList.add(desingDataset);
    when(designDatasetRepository.findByDataflowId(Mockito.anyLong())).thenReturn(desingDatasetList);
    DataSetSchema schema = new DataSetSchema();
    TableSchema desingTableSchema = new TableSchema();
    desingTableSchema.setToPrefill(Boolean.FALSE);
    desingTableSchema.setIdTableSchema(new ObjectId());
    List<TableSchema> desingTableSchemas = new ArrayList<>();
    desingTableSchemas.add(desingTableSchema);
    schema.setTableSchemas(desingTableSchemas);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    spreadDataCommand.execute(eeaEventVO);
    Mockito.verify(designDatasetRepository, times(1)).findByDataflowId(Mockito.anyLong());
  }


  /**
   * Execute test no desing datasets.
   */
  @Test
  public void executeTestNoDesingDatasets() {
    eeaEventVO.setData(result);
    eeaEventVO.setEventType(EventType.SPREAD_DATA_EVENT);
    List<DesignDataset> desingDatasetList = new ArrayList<>();
    when(designDatasetRepository.findByDataflowId(Mockito.anyLong())).thenReturn(desingDatasetList);
    spreadDataCommand.execute(eeaEventVO);
    Mockito.verify(designDatasetRepository, times(1)).findByDataflowId(Mockito.anyLong());
  }


  /**
   * Execute test desing datasets are null.
   */
  @Test
  public void executeTestNoDesingDatasetsNull() {
    eeaEventVO.setData(result);
    eeaEventVO.setEventType(EventType.SPREAD_DATA_EVENT);
    when(dataSetMetabaseRepository.findDataflowIdById(Mockito.anyLong())).thenReturn(null);
    spreadDataCommand.execute(eeaEventVO);
    Mockito.verify(designDatasetRepository, times(1)).findByDataflowId(null);
  }



}
