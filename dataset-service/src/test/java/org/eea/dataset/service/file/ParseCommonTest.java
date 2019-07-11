package org.eea.dataset.service.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

/*** The Class ParseCommonTest. */

@RunWith(MockitoJUnitRunner.class)
public class ParseCommonTest {

  /**
   * The parse common.
   */
  @InjectMocks
  private ParseCommon parseCommon;

  /**
   * The data set schema service.
   */
  @Mock
  private DatasetSchemaService dataSetSchemaService;

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
    MockitoAnnotations.initMocks(this);
  }


  /**
   * Test find id record.
   */
  @Test
  public void testFindIdRecord() {
    assertEquals("fail", ID, parseCommon.findIdRecord(ID, dataset));
  }

  /**
   * Test find id record null.
   */
  @Test
  public void testFindIdRecordNull() {
    assertNull("fail", parseCommon.findIdRecord(null, dataset));
  }

  /**
   * Test find id field schema.
   */
  @Test
  public void testFindIdFieldSchema() {
    assertEquals("fail", fieldSchema, parseCommon.findIdFieldSchema(ID, ID, dataset));
  }

  /**
   * Test find id field schema null.
   */
  @Test
  public void testFindIdFieldSchemaNull() {
    assertNull("fail", parseCommon.findIdFieldSchema(null, null, dataset));
  }

  /**
   * Gets the id table schema.
   *
   * @return the id table schema
   */
  @Test
  public void getIdTableSchema() {
    assertEquals("fail", dataset.getTableSchemas().get(0).getIdTableSchema(),
        parseCommon.getIdTableSchema(ID, dataset));
  }

  /**
   * Gets the id table schema null.
   *
   * @return the id table schema null
   */
  @Test
  public void getIdTableSchemaNull() {
    assertNull("fail", parseCommon.getIdTableSchema(null, dataset));
  }

  /**
   * Gets the id table schema null 2.
   *
   * @return the id table schema null 2
   */
  @Test
  public void getIdTableSchemaNull2() {
    assertNull("fail", parseCommon.getIdTableSchema("2", dataset));
  }

  /**
   * Gets the id table schema null 3.
   *
   * @return the id table schema null 3
   */
  @Test
  public void getIdTableSchemaNull3() {
    assertNull("fail", parseCommon.getIdTableSchema(ID, null));
  }

  /**
   * Test get data set schema.
   */
  @Test
  public void testGetDataSetSchema() {
    when(dataSetSchemaService.getDataSchemaByIdFlow(Mockito.any())).thenReturn(dataset);
    assertEquals("fail", dataset, parseCommon.getDataSetSchema(1L));
  }

}
