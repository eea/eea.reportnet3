package org.eea.dataset.service.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.service.DatasetSchemaService;
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
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class ParseCommonTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParseCommonTest {

  /** The parse common. */
  @InjectMocks
  private ParseCommon parseCommon;

  /** The data set schema service. */
  @Mock
  private DatasetSchemaService dataSetSchemaService;

  /** The Constant ID. */
  private static final String ID = "1";

  /** The field schema. */
  private static FieldSchemaVO fieldSchema;

  private static DataSetSchemaVO dataset;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    List<TableSchemaVO> tableSchemas = new ArrayList<>();
    List<FieldSchemaVO> fieldSchemas = new ArrayList<>();
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
    ReflectionTestUtils.setField(parseCommon, "tablesSchema", tableSchemas);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test find id table.
   */
  @Test
  public void testFindIdTable() {
    assertEquals(ID, parseCommon.findIdTable(ID));
  }

  /**
   * Test find id record.
   */
  @Test
  public void testFindIdRecord() {
    assertEquals(ID, parseCommon.findIdRecord(ID));
  }

  @Test
  public void testFindIdRecordNull() {
    assertNull(parseCommon.findIdRecord(null));
  }

  /**
   * Test find id field schema.
   */
  @Test
  public void testFindIdFieldSchema() {
    assertEquals(fieldSchema, parseCommon.findIdFieldSchema(ID, ID));
  }

  @Test
  public void testFindIdFieldSchemaNull() {
    assertNull(parseCommon.findIdFieldSchema(null, null));
  }

  /**
   * Test get data set schema.
   */
  @Test
  public void testGetDataSetSchema() {
    when(dataSetSchemaService.getDataSchemaByIdFlow(Mockito.any())).thenReturn(dataset);
    assertEquals(dataset, parseCommon.getDataSetSchema(1L, dataSetSchemaService));
  }

  /**
   * Test is header true.
   */
  @Test
  public void testIsHeaderTrue() {
    assertTrue(parseCommon.isHeader("_table"));
  }

  /**
   * Test is header false.
   */
  @Test
  public void testIsHeaderFalse() {
    assertFalse(parseCommon.isHeader(ID));
  }

}
