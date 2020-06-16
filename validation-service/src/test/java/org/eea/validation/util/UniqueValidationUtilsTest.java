package org.eea.validation.util;

import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DataSetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.RecordSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.RulesService;
import org.junit.Assert;
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
public class UniqueValidationUtilsTest {

  @InjectMocks
  private UniqueValidationUtils uniqueValidationUtils;

  @Mock
  private DataSetSchemaControllerZuul dataSetSchemaControllerZuul;

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private RulesRepository rulesRepository;

  @Mock
  private RecordRepository recordRepository;

  @Mock
  private TableRepository tableRepository;

  @Mock
  private RulesService rulesService;

  @Mock
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  @Before
  public void initMocks() {

    ReflectionTestUtils.setField(uniqueValidationUtils, "dataSetSchemaControllerZuul",
        dataSetSchemaControllerZuul);
    ReflectionTestUtils.setField(uniqueValidationUtils, "schemasRepository", schemasRepository);
    ReflectionTestUtils.setField(uniqueValidationUtils, "rulesRepository", rulesRepository);
    ReflectionTestUtils.setField(uniqueValidationUtils, "recordRepository", recordRepository);
    ReflectionTestUtils.setField(uniqueValidationUtils, "tableRepository", tableRepository);
    ReflectionTestUtils.setField(uniqueValidationUtils, "rulesService", rulesService);
    ReflectionTestUtils.setField(uniqueValidationUtils, "dataSetMetabaseControllerZuul",
        dataSetMetabaseControllerZuul);

    UniqueConstraintVO uniqueConstraintVO = new UniqueConstraintVO();
    uniqueConstraintVO.setDatasetSchemaId("5ece3de73fd71093b81eb4e6");
    uniqueConstraintVO.setUniqueId("uniqueId");
    uniqueConstraintVO.setTableSchemaId("5ece3de73fd71093b81eb4e6");
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setIdFieldSchema(new ObjectId("5ece3de73fd71093b81eb4e6"));
    recordSchema.setFieldSchema(Arrays.asList(fieldSchema));
    DataSetSchema dataSetSchema = new DataSetSchema();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5ece3de73fd71093b81eb4e6"));
    tableSchema.setRecordSchema(recordSchema);
    TableSchema tableSchema2 = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5ece3de73fd71093b81eb4e6"));
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);
    tableSchemas.add(tableSchema2);
    dataSetSchema.setTableSchemas(tableSchemas);
    dataSetSchema.setIdDataSetSchema(new ObjectId("5ece3de73fd71093b81eb4e6"));
    Rule rule = new Rule();
    rule.setRuleId(new ObjectId("5ece3de73fd71093b81eb4e6"));
    List<String> thenConditions = new ArrayList<>();
    thenConditions.add("message");
    thenConditions.add("WARNING");
    rule.setThenCondition(thenConditions);
    List<String> fieldSchemaIds = new ArrayList<>();
    fieldSchemaIds.add("5ece3de73fd71093b81eb4e6");
    fieldSchemaIds.add("5ece3de73fd71093b81eb4e6");
    uniqueConstraintVO.setFieldSchemaIds(fieldSchemaIds);
    List<RecordValue> duplicatedRecords = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setRecordValidations(new ArrayList<>());
    duplicatedRecords.add(record);

    DatasetValue datasetValue = new DatasetValue();
    datasetValue.setId(1L);
    IntegrityVO integrityVO = new IntegrityVO();
    integrityVO.setId("5e44110d6a9e3a270ce13fac");
    integrityVO.setOriginDatasetSchemaId("5e44110d6a9e3a270ce13fac");
    integrityVO.setReferencedDatasetSchemaId("5e44110d6a9e3a270ce13fac");
    integrityVO
        .setOriginFields(Arrays.asList("5ece3de73fd71093b81eb4e6", "5e44110d6a9e3a270ce13fac"));
    integrityVO
        .setReferencedFields(Arrays.asList("5ece3de73fd71093b81eb4e6", "5e44110d6a9e3a270ce13fac"));
    integrityVO.setIsDoubleReferenced(true);

    when(dataSetSchemaControllerZuul.getUniqueConstraint(Mockito.any()))
        .thenReturn(uniqueConstraintVO);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(dataSetSchema);
    when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    when(recordRepository.queryExecutionRecord(Mockito.any())).thenReturn(duplicatedRecords);
    when(recordRepository.saveAll(Mockito.any())).thenReturn(null);
    when(rulesService.getIntegrityConstraint(Mockito.any())).thenReturn(integrityVO);
    when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(new TableValue());
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void uniqueConstraintTest() {
    Assert.assertFalse(uniqueValidationUtils.uniqueConstraint("5ece3de73fd71093b81eb4e6",
        "5ece3de73fd71093b81eb4e6"));

  }

  @Test
  public void integrityConstraintTest() {
    DatasetValue datasetValue = new DatasetValue();
    datasetValue.setId(1L);
    Assert.assertTrue(uniqueValidationUtils.isIntegrityConstraint(datasetValue,
        "5ece3de73fd71093b81eb4e6", "5ece3de73fd71093b81eb4e6"));

  }

}
