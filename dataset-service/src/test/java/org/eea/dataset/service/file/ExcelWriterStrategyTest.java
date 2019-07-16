package org.eea.dataset.service.file;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
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

@RunWith(MockitoJUnitRunner.class)
public class ExcelWriterStrategyTest {

  @InjectMocks
  private ExcelWriterStrategy excelWriterStrategy;

  @Mock
  private ParseCommon parseCommon;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void excelWriterStrategyTest() {
    new ExcelWriterStrategy(parseCommon, "xls");
  }

  @Test
  public void writeFileXLS() {
    DataSetSchemaVO dataset = new DataSetSchemaVO();
    List<TableSchemaVO> tableSchemas = new ArrayList<>();
    List<FieldSchemaVO> fields = new ArrayList<>();
    List<RecordValue> values = new ArrayList<>();
    RecordValue value = new RecordValue();
    FieldSchemaVO field = new FieldSchemaVO();
    RecordSchemaVO records = new RecordSchemaVO();
    TableSchemaVO table = new TableSchemaVO();
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue fieldValue1 = new FieldValue();
    FieldValue fieldValue2 = new FieldValue();
    field.setId("9012");
    field.setIdRecord("3456");
    field.setName("FieldTest_ExcelWriterStrategy");
    field.setType("String");
    fields.add(field);
    records.setIdRecordSchema("5678");
    records.setFieldSchema(fields);
    table.setIdTableSchema("1234");
    table.setNameTableSchema("Testing_ExcelWriterStrategy");
    table.setRecordSchema(records);
    dataset.setTableSchemas(tableSchemas);
    fieldValue1.setValue("test_1");
    fieldValue2.setValue("test_2");
    fieldValues.add(fieldValue1);
    fieldValues.add(fieldValue2);
    value.setFields(fieldValues);
    values.add(value);
    Mockito.when(parseCommon.getDataSetSchema(Mockito.any())).thenReturn(dataset);
    Mockito.when(parseCommon.findTableSchema(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(parseCommon.getRecordValues(1L, table.getIdTableSchema())).thenReturn(values);
    excelWriterStrategy.setMimeType("xls");
    excelWriterStrategy.writeFile(1L, 1L, "");
    excelWriterStrategy.getMimeType();
  }

  @Test
  public void writeFileXLSX() {
    DataSetSchemaVO dataset = new DataSetSchemaVO();
    List<TableSchemaVO> tableSchemas = new ArrayList<>();
    List<FieldSchemaVO> fields = new ArrayList<>();
    List<RecordValue> values = new ArrayList<>();
    RecordValue value = new RecordValue();
    FieldSchemaVO field = new FieldSchemaVO();
    RecordSchemaVO records = new RecordSchemaVO();
    TableSchemaVO table = new TableSchemaVO();
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue fieldValue1 = new FieldValue();
    FieldValue fieldValue2 = new FieldValue();
    field.setId("9012");
    field.setIdRecord("3456");
    field.setName("FieldTest_ExcelWriterStrategy");
    field.setType("String");
    fields.add(field);
    records.setIdRecordSchema("5678");
    records.setFieldSchema(fields);
    table.setIdTableSchema("1234");
    table.setNameTableSchema("Testing_ExcelWriterStrategy");
    table.setRecordSchema(records);
    dataset.setTableSchemas(tableSchemas);
    fieldValue1.setValue("test_1");
    fieldValue2.setValue("test_2");
    fieldValues.add(fieldValue1);
    fieldValues.add(fieldValue2);
    value.setFields(fieldValues);
    values.add(value);
    Mockito.when(parseCommon.getDataSetSchema(Mockito.any())).thenReturn(dataset);
    Mockito.when(parseCommon.findTableSchema(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(parseCommon.getRecordValues(1L, table.getIdTableSchema())).thenReturn(values);
    excelWriterStrategy.setMimeType("xlsx");
    excelWriterStrategy.writeFile(1L, 1L, "");
    excelWriterStrategy.getMimeType();
  }

  @Test
  public void writeFileBadExtension() {
    DataSetSchemaVO dataset = new DataSetSchemaVO();
    List<TableSchemaVO> tableSchemas = new ArrayList<>();
    List<FieldSchemaVO> fields = new ArrayList<>();
    List<RecordValue> values = new ArrayList<>();
    RecordValue value = new RecordValue();
    FieldSchemaVO field = new FieldSchemaVO();
    RecordSchemaVO records = new RecordSchemaVO();
    TableSchemaVO table = new TableSchemaVO();
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue fieldValue1 = new FieldValue();
    FieldValue fieldValue2 = new FieldValue();
    field.setId("9012");
    field.setIdRecord("3456");
    field.setName("FieldTest_ExcelWriterStrategy");
    field.setType("String");
    fields.add(field);
    records.setIdRecordSchema("5678");
    records.setFieldSchema(fields);
    table.setIdTableSchema("1234");
    table.setNameTableSchema("Testing_ExcelWriterStrategy");
    table.setRecordSchema(records);
    dataset.setTableSchemas(tableSchemas);
    fieldValue1.setValue("test_1");
    fieldValue2.setValue("test_2");
    fieldValues.add(fieldValue1);
    fieldValues.add(fieldValue2);
    value.setFields(fieldValues);
    values.add(value);
    Mockito.when(parseCommon.getDataSetSchema(Mockito.any())).thenReturn(dataset);
    Mockito.when(parseCommon.findTableSchema(Mockito.any(), Mockito.any())).thenReturn(table);
    excelWriterStrategy.setMimeType("xlxs");
    excelWriterStrategy.writeFile(1L, 1L, "");
    excelWriterStrategy.getMimeType();
  }
}
