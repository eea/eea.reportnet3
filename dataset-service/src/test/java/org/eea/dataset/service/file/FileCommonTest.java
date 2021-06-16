package org.eea.dataset.service.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
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
  private static final String ID = "1";

  /**
   * The field schema.
   */
  private static FieldSchemaVO fieldSchema;

  /**
   * The dataset.
   */
  private static DataSetSchemaVO dataset;

  /** The field schemas. */
  private static List<FieldSchemaVO> fieldSchemas;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    List<TableSchemaVO> tableSchemas = new ArrayList<>();
    fieldSchemas = new ArrayList<>();
    RecordSchemaVO recordSchema = new RecordSchemaVO();
    fieldSchema = new FieldSchemaVO();
    TableSchemaVO tableSchema = new TableSchemaVO();
    dataset = new DataSetSchemaVO();
    fieldSchema.setId(ID);
    fieldSchema.setName(ID);
    fieldSchemas.add(fieldSchema);
    recordSchema.setIdRecordSchema(ID);
    recordSchema.setFieldSchema(fieldSchemas);
    tableSchema.setNameTableSchema(ID);
    tableSchema.setIdTableSchema(ID);
    tableSchema.setRecordSchema(recordSchema);
    tableSchemas.add(tableSchema);
    dataset.setTableSchemas(tableSchemas);
    MockitoAnnotations.initMocks(this);
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
    assertEquals("fail", fieldSchema, fileCommon.findIdFieldSchema(ID, ID, dataset));
  }

  /**
   * Test find id field schema null.
   */
  @Test
  public void testFindIdFieldSchemaNull() {
    assertNull("fail", fileCommon.findIdFieldSchema(null, null, dataset));
  }

  /**
   * Gets the id table schema.
   *
   * @return the id table schema
   */
  @Test
  public void getIdTableSchema() {
    assertEquals("fail", dataset.getTableSchemas().get(0).getIdTableSchema(),
        fileCommon.getIdTableSchema(ID, dataset));
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
    assertNull("fail", fileCommon.getIdTableSchema(ID, (DataSetSchemaVO) null));
  }

  /**
   * Test get data set schema.
   *
   * @throws EEAException
   */
  @Test
  public void testGetDataSetSchema() throws EEAException {
    when(dataSetSchemaService.getDataSchemaByDatasetId(Mockito.any(), Mockito.any()))
        .thenReturn(dataset);
    assertEquals("fail", dataset, fileCommon.getDataSetSchema(1L, 1L));
  }


  /**
   * Gets the table name test.
   *
   * @return the table name test
   */
  @Test
  public void getTableNameTest() {
    assertEquals("fail", ID, fileCommon.getTableName(ID, dataset));
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

  @Test
  public void findFieldSchemasTest() {
    List<FieldSchemaVO> fields = new ArrayList<>();
    fields.add(fieldSchema);
    assertEquals("fail", fields, fileCommon.findFieldSchemas(ID, dataset));
  }

  @Test
  public void findFieldSchemasNullTest() {
    assertEquals("fail", null, fileCommon.findFieldSchemas(ID, (DataSetSchemaVO) null));
  }
}
