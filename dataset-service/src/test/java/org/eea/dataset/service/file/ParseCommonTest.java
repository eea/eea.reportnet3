package org.eea.dataset.service.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ParseCommonTest {

  @InjectMocks
  private ParseCommon parseCommon;

  @Mock
  DatasetSchemaService dataSetSchemaService;

  private static final String ID = "1";
  private static FieldSchemaVO fieldSchema;

  @Before
  public void initMocks() throws IOException {
    List<TableSchemaVO> tableSchemas = new ArrayList<>();
    List<FieldSchemaVO> fieldSchemas = new ArrayList<>();
    RecordSchemaVO recordSchema = new RecordSchemaVO();
    fieldSchema = new FieldSchemaVO();
    TableSchemaVO tableSchema = new TableSchemaVO();
    fieldSchema.setId(ID);
    fieldSchema.setName(ID);
    fieldSchemas.add(fieldSchema);
    recordSchema.setIdRecordSchema(ID);
    recordSchema.setFieldSchema(fieldSchemas);
    tableSchema.setNameTableSchema(ID);
    tableSchema.setIdTableSchema(ID);
    tableSchema.setRecordSchema(recordSchema);
    tableSchemas.add(tableSchema);
    ReflectionTestUtils.setField(parseCommon, "tablesSchema", tableSchemas);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testFindIdTable() {
    assertEquals(ID, parseCommon.findIdTable(ID));
  }

  @Test
  public void testFindIdRecord() {
    assertEquals(ID, parseCommon.findIdRecord(ID));
  }

  @Test
  public void testFindIdFieldSchema() {
    assertEquals(fieldSchema, parseCommon.findIdFieldSchema(ID, ID));
  }

  @Test
  public void testGetDataSetSchema() {
    // Mockito.when(dataSetSchemaService.get)
    // parseCommon.getDataSetSchema(1L,);
  }

  @Test
  public void testIsHeaderTrue() {
    assertTrue(parseCommon.isHeader("_table"));
  }

  @Test
  public void testIsHeaderFalse() {
    assertFalse(parseCommon.isHeader(ID));
  }

}
