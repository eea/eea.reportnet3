package org.eea.validation.util;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.data.repository.FieldRepository;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.RecordSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class RulesErrorUtilsTest {

  @InjectMocks
  private RulesErrorUtils rulesErrorUtils;

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private RulesRepository rulesRepository;

  @Mock
  private RecordRepository recordRepository;

  @Mock
  private FieldRepository fieldRepository;

  @Mock
  private TableRepository tableRepository;

  @Mock
  private DatasetRepository datasetRepository;

  @Mock
  private DatasetMetabaseController datasetMetabaseController;

  private RuntimeException e;

  private RecordValue recordValue;

  private TableValue tableValue;

  private DatasetValue datasetValue;

  private FieldValue fieldValue;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
    e = new RuntimeException("aaaaaaa ," + new ObjectId());
    fieldValue = new FieldValue();
    recordValue = new RecordValue();
    tableValue = new TableValue();
    datasetValue = new DatasetValue();
    datasetValue.setIdDatasetSchema(new ObjectId().toString());
    tableValue.setDatasetId(datasetValue);
    recordValue.setTableValue(tableValue);
    fieldValue.setRecord(recordValue);
  }

  @Test
  public void testCreateRuleErrorExceptionRecord() {
    DataSetSchema datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemas = new ArrayList<>();
    datasetSchema.setTableSchemas(tableSchemas);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(new Rule());
    rulesErrorUtils.createRuleErrorException(recordValue, e);
    Mockito.verify(recordRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void testCreateRuleErrorExceptionField() {
    DataSetSchema datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemas = new ArrayList<>();
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setFieldSchema(fieldSchemas);
    TableSchema tableSchema = new TableSchema();
    tableSchema.setRecordSchema(recordSchema);
    tableSchemas.add(tableSchema);
    datasetSchema.setTableSchemas(tableSchemas);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(new Rule());
    rulesErrorUtils.createRuleErrorException(fieldValue, e);
    Mockito.verify(fieldRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void testCreateRuleErrorExceptionTable() {
    DataSetSchema datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemas = new ArrayList<>();
    datasetSchema.setTableSchemas(tableSchemas);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(new Rule());
    rulesErrorUtils.createRuleErrorException(tableValue, e);
    Mockito.verify(tableRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void testCreateRuleErrorExceptionDataset() {
    DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(dataSetMetabaseVO);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(new Rule());
    rulesErrorUtils.createRuleErrorException(datasetValue, e);
    Mockito.verify(datasetRepository, times(1)).save(Mockito.any());
  }
}
