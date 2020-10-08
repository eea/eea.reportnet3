package org.eea.validation.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
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
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.SqlRulesService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class SQLValidationUtilsTest {
  @Mock
  private static DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;


  @Mock
  private static DatasetRepository datasetRepository;


  @Mock
  private static SchemasRepository schemasRepository;


  @Mock
  private static SqlRulesService sqlRulesService;


  @Mock
  private static TableRepository tableRepository;

  @InjectMocks
  private SQLValidationUtils sqlValidationUtils;

  private Long datasetId;

  private String ruleId;

  private Rule rule;

  private TableValue table;

  private DataSetSchema schema;

  private TableSchema tableS;

  private ObjectId referenceId;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(sqlValidationUtils, "datasetMetabaseControllerZuul",
        dataSetMetabaseControllerZuul);
    ReflectionTestUtils.setField(sqlValidationUtils, "datasetRepository", datasetRepository);
    ReflectionTestUtils.setField(sqlValidationUtils, "schemasRepository", schemasRepository);
    ReflectionTestUtils.setField(sqlValidationUtils, "sqlRulesService", sqlRulesService);
    ReflectionTestUtils.setField(sqlValidationUtils, "tableRepository", tableRepository);


    datasetId = 1L;
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
    table.setIdTableSchema(new ObjectId().toString());

    tableS = new TableSchema();
    tableS.setIdTableSchema(new ObjectId());
  }


  @Test
  public void testExecuteValidationSQLRuleTableNoValidations() throws Exception {

    rule.setType(EntityTypeEnum.TABLE);

    List<TableValidation> tableValidations = new ArrayList<>();
    table.setTableValidations(tableValidations);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);

    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(sqlRulesService.retriveFirstResult(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<Object>());
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));

    sqlValidationUtils.executeValidationSQLRule(datasetId, ruleId);
  }

  @Test
  public void testExecuteValidationSQLRuleTableValidations() throws Exception {

    rule.setType(EntityTypeEnum.TABLE);

    List<TableValidation> tableValidations = new ArrayList<>();
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidations.add(tableValidation);
    table.setTableValidations(tableValidations);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);

    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(sqlRulesService.retriveFirstResult(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<Object>());
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));

    sqlValidationUtils.executeValidationSQLRule(datasetId, ruleId);

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
    table.setRecords(records);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);

    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.anyLong(), Mockito.any()))
        .thenReturn(table);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(sqlRulesService.retriveFirstResult(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<Object>());
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));

    sqlValidationUtils.executeValidationSQLRule(datasetId, ruleId);

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
    table.setRecords(records);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);

    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.anyLong(), Mockito.any()))
        .thenReturn(table);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(sqlRulesService.retriveFirstResult(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<Object>());
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));

    sqlValidationUtils.executeValidationSQLRule(datasetId, ruleId);

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
    table.setRecords(records);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);

    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.anyLong(), Mockito.any()))
        .thenReturn(table);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(sqlRulesService.retriveFirstResult(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<Object>());
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));

    sqlValidationUtils.executeValidationSQLRule(datasetId, ruleId);
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
    table.setRecords(records);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableS);
    schema.setTableSchemas(tableSchemas);

    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.anyLong(), Mockito.any()))
        .thenReturn(table);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(sqlRulesService.retriveFirstResult(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<Object>());
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));

    sqlValidationUtils.executeValidationSQLRule(datasetId, ruleId);
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

    List<DatasetValidation> datasetValidations = new ArrayList<>();
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setId(1L);
    datasetValidations.add(datasetValidation);
    dataset.setDatasetValidations(datasetValidations);


    Mockito.when(datasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataset));
    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.anyLong(), Mockito.any()))
        .thenReturn(table);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(sqlRulesService.retriveFirstResult(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<Object>());
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));

    sqlValidationUtils.executeValidationSQLRule(datasetId, ruleId);
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


    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    DatasetValue dataset = new DatasetValue();

    List<DatasetValidation> datasetValidations = new ArrayList<>();
    dataset.setDatasetValidations(datasetValidations);


    Mockito.when(datasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataset));
    Mockito.when(sqlRulesService.retrieveTableData(Mockito.any(), Mockito.anyLong(), Mockito.any()))
        .thenReturn(table);
    Mockito.when(sqlRulesService.getRule(Mockito.anyLong(), Mockito.any())).thenReturn(rule);
    Mockito.when(sqlRulesService.retriveFirstResult(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<Object>());
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(table);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(table));

    sqlValidationUtils.executeValidationSQLRule(datasetId, ruleId);
  }


}
