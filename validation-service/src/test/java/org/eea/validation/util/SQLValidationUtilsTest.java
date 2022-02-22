package org.eea.validation.util;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.RecordSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.SqlRulesService;
import org.eea.validation.util.model.QueryVO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SQLValidationUtilsTest {

  @Mock
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  @Mock
  private DatasetRepository datasetRepository;

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private SqlRulesService sqlRulesService;

  @Mock
  private TableRepository tableRepository;

  @Mock
  private RecordRepository recordRepository;

  @InjectMocks
  private SQLValidationUtils sqlValidationUtils;

  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  private DatasetValue datasetValue;

  private String ruleId;

  private Rule rule;

  private TableValue table;

  private DataSetSchema schema;

  private TableSchema tableS;

  private ObjectId referenceId;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
    datasetValue = new DatasetValue();
    datasetValue.setId(1L);
    ruleId = new ObjectId().toString();

    referenceId = new ObjectId();

    rule = new Rule();
    rule.setSqlSentence("SELECT * from dataset_1.table_value;");
    rule.setReferenceId(referenceId);
    rule.setRuleId(new ObjectId());
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("RULE ERROR");
    thenCondition.add("ERROR");
    rule.setThenCondition(thenCondition);

    schema = new DataSetSchema();

    table = new TableValue();
    table.setId(1L);
    table.setIdTableSchema("5ce524fad31fc52540abae73");
    List<RecordValue> records = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setFields(new ArrayList<>());
    records.add(record);
    table.setRecords(records);
    tableS = new TableSchema();
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    recordSchema.setFieldSchema(List.of(fieldSchema, fieldSchema, fieldSchema));
    tableS.setRecordSchema(recordSchema);
    tableS.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
  }

  @Test
  public void testExecuteValidationSQLRuleTableNoValidations() throws Exception {

    rule.setType(EntityTypeEnum.TABLE);

    List<TableValidation> tableValidations = new ArrayList<>();
    table.setTableValidations(tableValidations);

    List<RecordValue> records = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setId("id");
    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1L);
    recordValidations.add(recordValidation);
    record.setRecordValidations(recordValidations);
    records.add(record);
    table.setRecords(records);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);
    Mockito.when(sqlRulesService.queryTable(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));
    QueryVO queryVO = new QueryVO(rule.getSqlSentence(), rule, EntityTypeEnum.TABLE.toString(),
        datasetMetabase, 1L, Boolean.FALSE);
    Mockito
        .when(sqlRulesService.retrieveTableData(Mockito.anyString(), Mockito.any(), Mockito.any()))
        .thenReturn(queryVO);
    Mockito.when(datasetRepository.evaluateSqlRule(Mockito.any(), Mockito.any())).thenReturn("2");
    sqlValidationUtils.executeValidationSQLRule(datasetValue.getId(), ruleId, null);

    Mockito.verify(tableRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void testExecuteValidationSQLRuleTableValidations() throws Exception {

    rule.setType(EntityTypeEnum.TABLE);

    List<TableValidation> tableValidations = new ArrayList<>();
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidations.add(tableValidation);
    table.setTableValidations(tableValidations);

    List<RecordValue> records = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setId("id");
    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1L);
    recordValidations.add(recordValidation);
    record.setRecordValidations(recordValidations);
    records.add(record);
    table.setRecords(records);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());
    Mockito.when(sqlRulesService.queryTable(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));
    QueryVO queryVO = new QueryVO(rule.getSqlSentence(), rule, EntityTypeEnum.TABLE.toString(),
        datasetMetabase, 1L, Boolean.FALSE);
    Mockito
        .when(sqlRulesService.retrieveTableData(Mockito.anyString(), Mockito.any(), Mockito.any()))
        .thenReturn(queryVO);
    Mockito.when(datasetRepository.evaluateSqlRule(Mockito.any(), Mockito.any())).thenReturn("2");
    DataProviderVO providerCode = new DataProviderVO();
    providerCode.setCode("DE");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(providerCode);
    sqlValidationUtils.executeValidationSQLRule(datasetValue.getId(), ruleId, "27");

    Mockito.verify(tableRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void testExecuteValidationSQLRuleRecordValidations() throws Exception {

    rule.setType(EntityTypeEnum.RECORD);

    List<TableValidation> tableValidations = new ArrayList<>();
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidations.add(tableValidation);
    table.setTableValidations(tableValidations);

    List<RecordValue> records = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setId("id");
    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1L);
    recordValidations.add(recordValidation);
    record.setRecordValidations(recordValidations);
    records.add(record);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());
    Mockito.when(sqlRulesService.queryTable(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    QueryVO queryVO = new QueryVO(rule.getSqlSentence(), rule, EntityTypeEnum.RECORD.toString(),
        datasetMetabase, 1L, Boolean.FALSE);
    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(queryVO);
    Mockito.when(recordRepository.findByIds(Mockito.any())).thenReturn(records);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));
    Mockito.when(datasetRepository.evaluateSqlRule(Mockito.any(), Mockito.any())).thenReturn("2");
    DataProviderVO providerCode = new DataProviderVO();
    providerCode.setCode("DE");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(providerCode);
    sqlValidationUtils.executeValidationSQLRule(datasetValue.getId(), ruleId, "27");

    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());
  }

  @Test
  public void testExecuteValidationSQLRuleRecordNoValidations() throws Exception {

    rule.setType(EntityTypeEnum.RECORD);

    List<TableValidation> tableValidations = new ArrayList<>();
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidations.add(tableValidation);
    table.setTableValidations(tableValidations);

    List<RecordValue> records = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setId("id");
    List<RecordValidation> recordValidations = new ArrayList<>();
    record.setRecordValidations(recordValidations);
    records.add(record);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());
    QueryVO queryVO = new QueryVO(rule.getSqlSentence(), rule, EntityTypeEnum.RECORD.toString(),
        datasetMetabase, 1L, Boolean.FALSE);
    Mockito.when(sqlRulesService.queryTable(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(queryVO);
    Mockito.when(recordRepository.findByIds(Mockito.any())).thenReturn(records);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));
    Mockito.when(datasetRepository.evaluateSqlRule(Mockito.any(), Mockito.any())).thenReturn("2");
    DataProviderVO providerCode = new DataProviderVO();
    providerCode.setCode("DE");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(providerCode);
    sqlValidationUtils.executeValidationSQLRule(datasetValue.getId(), ruleId, "27");

    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());

  }

  @Test
  public void testExecuteValidationSQLRuleFieldNoValidations() throws Exception {

    rule.setType(EntityTypeEnum.FIELD);

    List<TableValidation> tableValidations = new ArrayList<>();
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidations.add(tableValidation);
    table.setTableValidations(tableValidations);

    List<RecordValue> records = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setId("id");
    List<FieldValue> fields = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setIdFieldSchema(referenceId.toString());
    List<FieldValidation> fieldValidations = new ArrayList<>();
    field.setFieldValidations(fieldValidations);
    fields.add(field);
    record.setFields(fields);
    List<RecordValidation> recordValidations = new ArrayList<>();
    record.setRecordValidations(recordValidations);
    records.add(record);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());
    QueryVO queryVO = new QueryVO(rule.getSqlSentence(), rule, EntityTypeEnum.FIELD.toString(),
        datasetMetabase, 1L, Boolean.FALSE);
    Mockito.when(sqlRulesService.queryTable(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(queryVO);
    Mockito.when(recordRepository.findByIds(Mockito.any())).thenReturn(records);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));
    Mockito.when(datasetRepository.evaluateSqlRule(Mockito.any(), Mockito.any())).thenReturn("2");
    DataProviderVO providerCode = new DataProviderVO();
    providerCode.setCode("DE");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(providerCode);
    sqlValidationUtils.executeValidationSQLRule(datasetValue.getId(), ruleId, "27");

    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());
  }

  @Test
  public void testExecuteValidationSQLRuleFieldValidations() throws Exception {

    rule.setType(EntityTypeEnum.FIELD);

    List<TableValidation> tableValidations = new ArrayList<>();
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidations.add(tableValidation);
    table.setTableValidations(tableValidations);

    List<RecordValue> records = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setId("id");
    List<FieldValue> fields = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setIdFieldSchema(referenceId.toString());
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setId(1L);
    fieldValidations.add(fieldValidation);
    field.setFieldValidations(fieldValidations);
    fields.add(field);
    record.setFields(fields);
    List<RecordValidation> recordValidations = new ArrayList<>();
    record.setRecordValidations(recordValidations);
    records.add(record);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());
    Mockito.when(sqlRulesService.queryTable(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    QueryVO queryVO = new QueryVO(rule.getSqlSentence(), rule, EntityTypeEnum.FIELD.toString(),
        datasetMetabase, 1L, Boolean.FALSE);
    Mockito.when(recordRepository.findByIds(Mockito.any())).thenReturn(records);
    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(queryVO);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));
    Mockito.when(datasetRepository.evaluateSqlRule(Mockito.any(), Mockito.any())).thenReturn("2");
    DataProviderVO providerCode = new DataProviderVO();
    providerCode.setCode("DE");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(providerCode);
    sqlValidationUtils.executeValidationSQLRule(datasetValue.getId(), ruleId, "27");

    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());
  }

  @Test
  public void testExecuteValidationSQLRuleDatasetValidations() throws Exception {

    rule.setType(EntityTypeEnum.DATASET);

    List<TableValidation> tableValidations = new ArrayList<>();
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidations.add(tableValidation);
    table.setTableValidations(tableValidations);

    List<RecordValue> records = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setId("id");
    List<FieldValue> fields = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setIdFieldSchema(referenceId.toString());
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setId(1L);
    fieldValidations.add(fieldValidation);
    field.setFieldValidations(fieldValidations);
    fields.add(field);
    record.setFields(fields);
    List<RecordValidation> recordValidations = new ArrayList<>();
    record.setRecordValidations(recordValidations);
    records.add(record);
    table.setRecords(records);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);


    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    DatasetValue dataset = new DatasetValue();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setId(1L);
    datasetValidations.add(datasetValidation);
    dataset.setDatasetValidations(datasetValidations);

    Mockito.when(sqlRulesService.queryTable(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(datasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataset));
    QueryVO queryVO = new QueryVO(rule.getSqlSentence(), rule, EntityTypeEnum.DATASET.toString(),
        datasetMetabase, 1L, Boolean.FALSE);
    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(queryVO);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));
    Mockito.when(recordRepository.findByIds(Mockito.any())).thenReturn(records);
    Mockito.when(datasetRepository.evaluateSqlRule(Mockito.any(), Mockito.any())).thenReturn("2");
    DataProviderVO providerCode = new DataProviderVO();
    providerCode.setCode("DE");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(providerCode);
    sqlValidationUtils.executeValidationSQLRule(datasetValue.getId(), ruleId, "27");
    Mockito.verify(datasetRepository, times(1)).save(Mockito.any());
  }


  @Test
  public void testExecuteValidationSQLRuleDatasetNoValidations() throws Exception {

    rule.setType(EntityTypeEnum.DATASET);

    List<TableValidation> tableValidations = new ArrayList<>();
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidations.add(tableValidation);
    table.setTableValidations(tableValidations);

    List<RecordValue> records = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setId("id");
    List<FieldValue> fields = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setIdFieldSchema(referenceId.toString());
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setId(1L);
    fieldValidations.add(fieldValidation);
    field.setFieldValidations(fieldValidations);
    fields.add(field);
    record.setFields(fields);
    List<RecordValidation> recordValidations = new ArrayList<>();
    record.setRecordValidations(recordValidations);
    records.add(record);
    table.setRecords(records);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);
    schema.setIdDataSetSchema(new ObjectId("5ce524fad31fc52540abae73"));


    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    DatasetValue dataset = new DatasetValue();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());

    List<DatasetValidation> datasetValidations = new ArrayList<>();
    dataset.setDatasetValidations(datasetValidations);
    dataset.setIdDatasetSchema("5ce524fad31fc52540abae73");

    Mockito.when(sqlRulesService.queryTable(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(datasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataset));
    QueryVO queryVO = new QueryVO(rule.getSqlSentence(), rule, EntityTypeEnum.DATASET.toString(),
        datasetMetabase, 1L, Boolean.FALSE);
    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(queryVO);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));
    Mockito.when(datasetRepository.evaluateSqlRule(Mockito.any(), Mockito.any())).thenReturn("2");
    DataProviderVO providerCode = new DataProviderVO();
    providerCode.setCode("DE");
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(providerCode);
    sqlValidationUtils.executeValidationSQLRule(datasetValue.getId(), ruleId, "27");
    Mockito.verify(datasetRepository, times(1)).save(Mockito.any());
  }
}
