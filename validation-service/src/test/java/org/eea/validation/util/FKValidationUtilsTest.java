package org.eea.validation.util;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.repository.FieldRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.RecordSchema;
import org.eea.validation.persistence.schemas.ReferencedFieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class FKValidationUtilsTest {

  @InjectMocks
  private FKValidationUtils fKValidationUtils;

  @Mock
  private static DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  @Mock
  private static SchemasRepository schemasRepository;

  @Mock
  private static RulesRepository rulesRepository;

  @Mock
  private static FieldRepository fieldRepository;

  @Mock
  private static DataSetControllerZuul dataSetControllerZuul;

  private DatasetValue dataset;
  private DataSetSchema datasetSchema;
  private ObjectId id;


  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(fKValidationUtils, "dataSetControllerZuul", dataSetControllerZuul);
    ReflectionTestUtils.setField(fKValidationUtils, "datasetMetabaseControllerZuul",
        datasetMetabaseControllerZuul);
    ReflectionTestUtils.setField(fKValidationUtils, "schemasRepository", schemasRepository);
    ReflectionTestUtils.setField(fKValidationUtils, "rulesRepository", rulesRepository);
    ReflectionTestUtils.setField(fKValidationUtils, "fieldRepository", fieldRepository);
    id = new ObjectId();
    dataset = new DatasetValue();
    datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemas = new ArrayList<>();
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    TableSchema tableSchema = new TableSchema();
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema = new FieldSchema();
    ReferencedFieldSchema referencedFieldSchema = new ReferencedFieldSchema();
    referencedFieldSchema.setIdPk(id);
    fieldSchema.setIdFieldSchema(id);
    fieldSchema.setReferencedField(referencedFieldSchema);
    fieldSchema.setPkMustBeUsed(Boolean.TRUE);
    fieldSchemas.add(fieldSchema);
    recordSchema.setFieldSchema(fieldSchemas);
    tableSchema.setRecordSchema(recordSchema);
    tableSchemas.add(tableSchema);
    datasetSchema.setTableSchemas(tableSchemas);
    dataset.setId(1L);
  }

  @Test
  public void testIsfieldFKWarning() {
    Rule rule = new Rule();
    rule.setRuleId(id);
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add(ErrorTypeEnum.WARNING.toString());
    rule.setThenCondition(thenCondition);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(id.toString());
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    assertTrue(fKValidationUtils.isfieldFK(dataset, id.toString(), id.toString(), Boolean.TRUE));
  }

  @Test
  public void testIsfieldFKError() {
    Rule rule = new Rule();
    rule.setRuleId(id);
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add(ErrorTypeEnum.ERROR.toString());
    rule.setThenCondition(thenCondition);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(id.toString());
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    assertTrue(fKValidationUtils.isfieldFK(dataset, id.toString(), id.toString(), Boolean.TRUE));
  }

  @Test
  public void testIsfieldFKBlocker() {
    Rule rule = new Rule();
    rule.setRuleId(id);
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add(ErrorTypeEnum.BLOCKER.toString());
    rule.setThenCondition(thenCondition);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(id.toString());
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    assertTrue(fKValidationUtils.isfieldFK(dataset, id.toString(), id.toString(), Boolean.TRUE));
  }

  @Test
  public void testIsfieldFKInfo() {
    Rule rule = new Rule();
    rule.setRuleId(id);
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add(ErrorTypeEnum.INFO.toString());
    rule.setThenCondition(thenCondition);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(id.toString());
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    assertTrue(fKValidationUtils.isfieldFK(dataset, id.toString(), id.toString(), Boolean.TRUE));
  }

  @Test
  public void testIsfieldFKDefault() {
    Rule rule = new Rule();
    rule.setRuleId(id);
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    rule.setThenCondition(thenCondition);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(id.toString());
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    assertTrue(fKValidationUtils.isfieldFK(dataset, id.toString(), id.toString(), Boolean.TRUE));
  }

  @Test
  public void testIsfieldFKErrorFalse() {
    Rule rule = new Rule();
    rule.setRuleId(id);
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add(ErrorTypeEnum.ERROR.toString());
    rule.setThenCondition(thenCondition);
    List<FieldValue> fields = new ArrayList<>();
    FieldValue field = new FieldValue();
    fields.add(field);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(id.toString());
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    Mockito.when(fieldRepository.findByIdFieldSchema(id.toString())).thenReturn(fields);
    assertTrue(fKValidationUtils.isfieldFK(dataset, id.toString(), id.toString(), Boolean.FALSE));
  }

}
