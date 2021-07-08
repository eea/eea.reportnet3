package org.eea.validation.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DataType;
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

/**
 * The Class FKValidationUtilsTest.
 */
public class FKValidationUtilsTest {

  /** The k validation utils. */
  @InjectMocks
  private FKValidationUtils fKValidationUtils;

  /** The dataset metabase controller zuul. */
  @Mock
  private static DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The schemas repository. */
  @Mock
  private static SchemasRepository schemasRepository;

  /** The rules repository. */
  @Mock
  private static RulesRepository rulesRepository;

  /** The field repository. */
  @Mock
  private static FieldRepository fieldRepository;

  /** The data set controller zuul. */
  @Mock
  private static DataSetControllerZuul dataSetControllerZuul;

  /** The dataset. */
  private DatasetValue dataset;

  /** The dataset schema. */
  private DataSetSchema datasetSchema;

  /** The id. */
  private ObjectId id;

  /** The referenced field schema. */
  private ReferencedFieldSchema referencedFieldSchema;


  /**
   * Inits the mocks.
   */
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
    referencedFieldSchema = new ReferencedFieldSchema();
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

  /**
   * Test isfield FK warning.
   */
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

  /**
   * Test isfield FK error.
   */
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

  /**
   * Test isfield FK blocker.
   */
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

  /**
   * Test isfield FK info.
   */
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

  /**
   * Test isfield FK default.
   */
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

  /**
   * Test isfield FK error false.
   */
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

  /**
   * Test isfield FK error true.
   */
  @Test
  public void testIsfieldFKErrorTrue() {
    Rule rule = new Rule();
    rule.setRuleId(id);
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    List<FieldValue> fields = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setValue("1");
    fields.add(field);
    datasetSchema.getTableSchemas().get(0).getRecordSchema().getFieldSchema().get(0)
        .setPkHasMultipleValues(true);
    datasetSchema.getTableSchemas().get(0).getRecordSchema().getFieldSchema().get(0)
        .setPkMustBeUsed(false);
    rule.setThenCondition(thenCondition);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(id.toString());
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    Mockito.when(fieldRepository.findByIdFieldSchema(id.toString())).thenReturn(fields);
    assertTrue(fKValidationUtils.isfieldFK(dataset, id.toString(), id.toString(), Boolean.FALSE));
  }

  /**
   * Test isfield FK error true.
   */
  @Test
  public void testIsfieldFKErrorNotAllValue() {
    Rule rule = new Rule();
    rule.setRuleId(id);
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    List<FieldValue> fields = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setValue("1");
    fields.add(field);
    datasetSchema.getTableSchemas().get(0).getRecordSchema().getFieldSchema().get(0)
        .setPkHasMultipleValues(true);
    rule.setThenCondition(thenCondition);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(id.toString());
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    Mockito.when(fieldRepository.findByIdFieldSchema(id.toString())).thenReturn(fields);
    assertTrue(fKValidationUtils.isfieldFK(dataset, id.toString(), id.toString(), Boolean.TRUE));
  }

  @Test
  public void testIsfieldMultiConstrainFK() {
    referencedFieldSchema.setLinkedConditionalFieldId(id);
    referencedFieldSchema.setMasterConditionalFieldId(id);
    Rule rule = new Rule();
    rule.setRuleId(id);
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    List<FieldValue> fields = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setValue("1");
    fields.add(field);
    datasetSchema.getTableSchemas().get(0).getRecordSchema().getFieldSchema().get(0)
        .setPkHasMultipleValues(true);
    datasetSchema.getTableSchemas().get(0).getRecordSchema().getFieldSchema().get(0)
        .setPkMustBeUsed(false);
    rule.setThenCondition(thenCondition);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(id.toString());
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    Mockito.when(fieldRepository.findByIdFieldSchema(id.toString())).thenReturn(fields);
    assertTrue(fKValidationUtils.isfieldFK(dataset, id.toString(), id.toString(), Boolean.FALSE));
  }

  @Test
  public void isGeometryFalseTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.EMAIL);
    assertFalse("not true", fKValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryPointTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.POINT);
    fieldValue.setValue("");
    assertTrue("not true", fKValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryLinestringTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.LINESTRING);
    fieldValue.setValue("");
    assertTrue("not true", fKValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryPolygonTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.POLYGON);
    fieldValue.setValue("");
    assertTrue("not true", fKValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryMultiPointTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.MULTIPOINT);
    fieldValue.setValue("");
    assertTrue("not true", fKValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryMultilinestringTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.MULTILINESTRING);
    fieldValue.setValue("");
    assertTrue("not true", fKValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryMultipolygongTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.MULTIPOLYGON);
    fieldValue.setValue("");
    assertTrue("not true", fKValidationUtils.isGeometry(fieldValue));
  }

  @Test
  public void isGeometryCollectionTest() {
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.GEOMETRYCOLLECTION);
    fieldValue.setValue("");
    assertTrue("not true", fKValidationUtils.isGeometry(fieldValue));
  }
}
