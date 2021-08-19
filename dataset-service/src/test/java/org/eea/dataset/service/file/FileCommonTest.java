package org.eea.dataset.service.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/*** The Class ParseCommonTest. */

@RunWith(MockitoJUnitRunner.class)
public class FileCommonTest {

  /**
   * The parse common.
   */
  @InjectMocks
  private FileCommonUtils fileCommon;

  /**
   * The data set schema service.
   */
  @Mock
  private DatasetSchemaService dataSetSchemaService;

  /**
   * The record repository.
   */
  @Mock
  private RecordRepository recordRepository;

  /**
   * The Constant ID.
   */
  private static final String ID = "5ce524fad31fc52540abae73";

  /**
   * The field schema.
   */
  private static FieldSchemaVO fieldSchemaVO;

  /**
   * The field schema.
   */
  private static FieldSchema fieldSchema;

  /**
   * The dataset VO.
   */
  private static DataSetSchemaVO datasetVO;

  /** The dataset. */
  private static DataSetSchema dataset;

  /** The field schema VOs. */
  private static List<FieldSchemaVO> fieldSchemasVO;

  /** The field schemas . */
  private static List<FieldSchema> fieldSchemas;

  /** The design dataset repository. */
  @Mock
  private DesignDatasetRepository designDatasetRepository;

  /** The data set metabase repository. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The validation controller. */
  @Mock
  private ValidationControllerZuul validationController;

  /** The dataflow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /** The table repository. */
  @Mock
  private TableRepository tableRepository;

  /** The table schema. */
  private static TableSchema tableSchema;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    List<TableSchemaVO> tableSchemasVO = new ArrayList<>();
    fieldSchemasVO = new ArrayList<>();
    RecordSchemaVO recordSchemaVO = new RecordSchemaVO();
    fieldSchemaVO = new FieldSchemaVO();
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    datasetVO = new DataSetSchemaVO();
    fieldSchemaVO.setId(ID);
    fieldSchemaVO.setName(ID);
    fieldSchemasVO.add(fieldSchemaVO);
    recordSchemaVO.setIdRecordSchema(ID);
    recordSchemaVO.setFieldSchema(fieldSchemasVO);
    tableSchemaVO.setNameTableSchema(ID);
    tableSchemaVO.setIdTableSchema(ID);
    tableSchemaVO.setRecordSchema(recordSchemaVO);
    tableSchemasVO.add(tableSchemaVO);
    datasetVO.setTableSchemas(tableSchemasVO);

    List<TableSchema> tableSchemas = new ArrayList<>();
    fieldSchemas = new ArrayList<>();
    RecordSchema recordSchema = new RecordSchema();
    fieldSchema = new FieldSchema();
    tableSchema = new TableSchema();
    dataset = new DataSetSchema();
    fieldSchema.setIdFieldSchema(new ObjectId(ID));
    fieldSchema.setHeaderName("1L");
    fieldSchemas.add(fieldSchema);
    recordSchema.setIdRecordSchema(new ObjectId(ID));
    recordSchema.setFieldSchema(fieldSchemas);
    tableSchema.setNameTableSchema("1L");
    tableSchema.setIdTableSchema(new ObjectId(ID));
    tableSchema.setRecordSchema(recordSchema);
    tableSchema.setFixedNumber(false);
    tableSchemas.add(tableSchema);
    dataset.setTableSchemas(tableSchemas);
    MockitoAnnotations.openMocks(this);
  }


  /**
   * Test find id record.
   */
  @Test
  public void testFindIdRecordVO() {
    assertEquals("fail", ID, fileCommon.findIdRecord(ID, datasetVO));
  }

  /**
   * Test find id record null.
   */
  @Test
  public void testFindIdRecordNullVO() {
    assertNull("fail", fileCommon.findIdRecord(null, datasetVO));
  }

  /**
   * Test find id record.
   */
  @Test
  public void testFindIdRecord() {
    assertEquals("fail", ID, fileCommon.findIdRecord(ID, dataset));
  }

  /**
   * Test find id record null.
   */
  @Test
  public void testFindIdRecordNull() {
    assertNull("fail", fileCommon.findIdRecord(null, dataset));
  }

  /**
   * Test find id field schema.
   */
  @Test
  public void testFindIdFieldSchema() {
    assertEquals("fail", fieldSchema, fileCommon.findIdFieldSchema("1L", ID, dataset));
  }

  /**
   * Test find id field schema null.
   */
  @Test
  public void testFindIdFieldSchemaNull() {
    assertNull("fail", fileCommon.findIdFieldSchema(null, null, dataset));
  }

  /**
   * Test find id field schema VO.
   */
  @Test
  public void testFindIdFieldSchemaVO() {
    assertEquals("fail", fieldSchemaVO, fileCommon.findIdFieldSchema(ID, ID, datasetVO));
  }

  /**
   * Test find id field schema null.
   */
  @Test
  public void testFindIdFieldSchemaVONull() {
    assertNull("fail", fileCommon.findIdFieldSchema(null, null, datasetVO));
  }

  /**
   * Gets the id table schema.
   *
   * @return the id table schema
   */
  @Test
  public void getIdTableSchemaVO() {
    assertEquals("fail", dataset.getTableSchemas().get(0).getIdTableSchema().toString(),
        fileCommon.getIdTableSchema(ID, datasetVO));
  }

  /**
   * Gets the id table schema null.
   *
   * @return the id table schema null
   */
  @Test
  public void getIdTableSchemaNullVO() {
    assertNull("fail", fileCommon.getIdTableSchema(null, datasetVO));
  }

  /**
   * Gets the id table schema null 2.
   *
   * @return the id table schema null 2
   */
  @Test
  public void getIdTableSchemaNullVO2() {
    assertNull("fail", fileCommon.getIdTableSchema("2", datasetVO));
  }

  /**
   * Gets the id table schema.
   *
   * @return the id table schema
   */
  @Test
  public void getIdTableSchema() {
    assertEquals("fail", dataset.getTableSchemas().get(0).getIdTableSchema().toString(),
        fileCommon.getIdTableSchema("1L", dataset));
  }

  /**
   * Gets the id table schema null.
   *
   * @return the id table schema null
   */
  @Test
  public void getIdTableSchemaNull() {
    assertNull("fail", fileCommon.getIdTableSchema(null, dataset));
  }

  /**
   * Gets the id table schema null 2.
   *
   * @return the id table schema null 2
   */
  @Test
  public void getIdTableSchemaNull2() {
    assertNull("fail", fileCommon.getIdTableSchema("2", dataset));
  }

  /**
   * Gets the id table schema null 3.
   *
   * @return the id table schema null 3
   */
  @Test
  public void getIdTableSchemaNull3() {
    assertNull("fail", fileCommon.getIdTableSchema(ID, (DataSetSchema) null));
  }

  /**
   * Test get data set schema.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetDataSetSchemaVO() throws EEAException {
    when(dataSetSchemaService.getDataSchemaByDatasetId(Mockito.any(), Mockito.any()))
        .thenReturn(datasetVO);
    assertEquals("fail", datasetVO, fileCommon.getDataSetSchemaVO(1L, 1L));
  }

  /**
   * Gets the table name test.
   *
   * @return the table name test
   */
  @Test
  public void getTableNameVOTest() {
    assertEquals("fail", ID, fileCommon.getTableName(ID, datasetVO));
  }

  /**
   * Gets the field schemas test.
   *
   * @return the field schemas test
   */
  @Test
  public void getFieldSchemasVOTest() {
    assertEquals("fail", fieldSchemasVO, fileCommon.getFieldSchemas(ID, datasetVO));
  }

  /**
   * Gets the field schemas test.
   *
   * @return the field schemas test
   */
  @Test
  public void getFieldSchemasTest() {
    assertEquals("fail", fieldSchemas, fileCommon.getFieldSchemas(ID, dataset));
  }

  /**
   * Gets the record values test.
   *
   * @return the record values test
   */
  @Test
  public void getRecordValuesTest() {
    List<RecordValue> records = new ArrayList<>();
    List<FieldValue> fields = new ArrayList<>();
    RecordValue record = new RecordValue();
    record.setId("1L");
    FieldValue fieldValue = new FieldValue();
    fields.add(fieldValue);
    record.setFields(fields);
    records.add(record);

    when(recordRepository.findByTableValueIdTableSchema(Mockito.any())).thenReturn(records);
    assertEquals("fail", records, fileCommon.getRecordValues(1L, ID));
  }

  /**
   * Find field schemas test.
   */
  @Test
  public void findFieldSchemasVOTest() {
    List<FieldSchemaVO> fields = new ArrayList<>();
    fields.add(fieldSchemaVO);
    assertEquals("fail", fields, fileCommon.findFieldSchemas(ID, datasetVO));
  }

  /**
   * Find field schemas null test.
   */
  @Test
  public void findFieldSchemasVONullTest() {
    assertEquals("fail", null, fileCommon.findFieldSchemas(ID, (DataSetSchemaVO) null));
  }

  /**
   * Find field schemas test.
   */
  @Test
  public void findFieldSchemasTest() {
    List<FieldSchema> fields = new ArrayList<>();
    fields.add(fieldSchema);
    assertEquals("fail", fields, fileCommon.findFieldSchemas(ID, dataset));
  }

  /**
   * Find field schemas null test.
   */
  @Test
  public void findFieldSchemasNullTest() {
    assertEquals("fail", null, fileCommon.findFieldSchemas(ID, (DataSetSchema) null));
  }

  /**
   * Checks if is design dataset test.
   */
  @Test
  public void isDesignDatasetTest() {
    Mockito.when(designDatasetRepository.existsById(Mockito.anyLong())).thenReturn(true);
    assertEquals("design", true, fileCommon.isDesignDataset(1L));
  }

  /**
   * Gets the table name test.
   *
   * @return the table name test
   */
  @Test
  public void getTableNameTest() {
    assertEquals("fail", "1L", fileCommon.getTableName(ID, dataset));
  }

  /**
   * Gets the errors test.
   *
   * @return the errors test
   */
  @Test
  public void getErrorsTest() {
    Mockito.when(validationController.getFailedValidationsByIdDataset(Mockito.anyLong(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);
    assertNull("fail", fileCommon.getErrors(1L, ID, datasetVO));
  }

  /**
   * Count records by table schema test.
   */
  @Test
  public void countRecordsByTableSchemaTest() {
    Mockito.when(recordRepository.countByTableSchema(Mockito.anyString())).thenReturn(1L);
    assertEquals((Long) 1L, fileCommon.countRecordsByTableSchema(ID));
  }

  /**
   * Schema contains fixed records not found test.
   */
  @Test
  public void schemaContainsFixedRecordsNotFoundTest() {
    Mockito.when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any()))
        .thenReturn(new DataFlowVO());
    assertFalse(fileCommon.schemaContainsFixedRecords(1L, dataset, ID));
  }

  /**
   * Schema contains fixed records found test.
   */
  @Test
  public void schemaContainsFixedRecordsFoundTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    assertFalse(fileCommon.schemaContainsFixedRecords(1L, dataset, null));
  }

  /**
   * Persist imported dataset success test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void persistImportedDatasetSuccessTest() throws IOException, SQLException, EEAException {
    DatasetValue datasetValue = new DatasetValue();
    List<TableValue> tableValues = new ArrayList<>();
    TableValue tableValue = new TableValue();
    tableValue.setIdTableSchema(ID);
    tableValues.add(tableValue);
    datasetValue.setTableValues(tableValues);
    Mockito.when(tableRepository.findIdByIdTableSchema(Mockito.any())).thenReturn(1L);
    Mockito.when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any()))
        .thenReturn(new DataFlowVO());
    fileCommon.persistImportedDataset(ID, 1L, "filename", true, dataset, datasetValue);
    Mockito.verify(datasetService, times(1)).storeRecords(Mockito.any(), Mockito.any());
  }

  /**
   * Persist imported dataset empty test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void persistImportedDatasetEmptyTest() throws IOException, SQLException, EEAException {
    DatasetValue datasetValue = new DatasetValue();
    List<TableValue> tableValues = new ArrayList<>();
    TableValue tableValue = new TableValue();
    tableValue.setIdTableSchema(ID);
    tableValues.add(tableValue);
    datasetValue.setTableValues(tableValues);
    Mockito.when(tableRepository.findIdByIdTableSchema(Mockito.any())).thenReturn(null);
    Mockito.when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any()))
        .thenReturn(new DataFlowVO());
    fileCommon.persistImportedDataset(ID, 1L, "filename", true, dataset, datasetValue);
    Mockito.verify(datasetService, times(1)).storeRecords(Mockito.any(), Mockito.any());
  }

  /**
   * Persist imported dataset fixed records test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void persistImportedDatasetFixedRecordsTest()
      throws IOException, SQLException, EEAException {
    DatasetValue datasetValue = new DatasetValue();
    List<TableValue> tableValues = new ArrayList<>();
    TableValue tableValue = new TableValue();
    tableValue.setIdTableSchema(ID);
    tableValues.add(tableValue);
    datasetValue.setTableValues(tableValues);
    Mockito.when(tableRepository.findIdByIdTableSchema(Mockito.any())).thenReturn(1L);
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataset.getTableSchemas().remove(0);
    tableSchema.setFixedNumber(true);
    dataset.getTableSchemas().add(tableSchema);
    Mockito.when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    fileCommon.persistImportedDataset(ID, 1L, "filename", true, dataset, datasetValue);
    Mockito.verify(datasetService, times(1)).updateRecordsWithConditions(Mockito.any(),
        Mockito.any(), Mockito.any());
  }
}
