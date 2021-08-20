package org.eea.dataset.service.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
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
  private FileCommonUtils fileCommon;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void excelWriterStrategyTest() {
    assertNotNull("failed assertion",
        new ExcelWriterStrategy(fileCommon, FileTypeEnum.XLS.getValue()));
  }

  @Test
  public void writeFileXLS() throws EEAException {
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
    field.setType(DataType.TEXT);
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
    value.setDataProviderCode("ES");
    values.add(value);
    Mockito.when(fileCommon.getDataSetSchemaVO(Mockito.any(), Mockito.any())).thenReturn(dataset);
    Mockito.when(fileCommon.findTableSchemaVO(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(fileCommon.getRecordValuesPaginated(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(values);
    excelWriterStrategy.setMimeType(FileTypeEnum.XLS.getValue());
    excelWriterStrategy.writeFile(1L, 1L, "", true, false);
    excelWriterStrategy.getMimeType();
    Mockito.verify(fileCommon, times(1)).getRecordValuesPaginated(Mockito.any(), Mockito.any(),
        Mockito.any());
    assertEquals("failed assertion", FileTypeEnum.XLS.getValue(),
        excelWriterStrategy.getMimeType());
  }

  @Test
  public void writeFileXLSX() throws EEAException {
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
    field.setType(DataType.TEXT);
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
    Mockito.when(fileCommon.getDataSetSchemaVO(Mockito.any(), Mockito.any())).thenReturn(dataset);
    Mockito.when(fileCommon.findTableSchemaVO(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(fileCommon.getRecordValuesPaginated(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(values);
    excelWriterStrategy.setMimeType(FileTypeEnum.XLSX.getValue());
    excelWriterStrategy.writeFile(1L, 1L, "", false, false);
    excelWriterStrategy.getMimeType();
    Mockito.verify(fileCommon, times(1)).getRecordValuesPaginated(Mockito.any(), Mockito.any(),
        Mockito.any());
    assertEquals("failed assertion", FileTypeEnum.XLSX.getValue(),
        excelWriterStrategy.getMimeType());
  }

  @Test
  public void writeFileBadExtension() throws EEAException {
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
    field.setType(DataType.TEXT);
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
    Mockito.when(fileCommon.getDataSetSchemaVO(Mockito.any(), Mockito.any())).thenReturn(dataset);
    Mockito.when(fileCommon.findTableSchemaVO(Mockito.any(), Mockito.any())).thenReturn(table);
    excelWriterStrategy.setMimeType("xlxs");
    excelWriterStrategy.writeFile(1L, 1L, "", false, false);
    excelWriterStrategy.getMimeType();
    Mockito.verify(fileCommon, times(1)).getDataSetSchemaVO(Mockito.any(), Mockito.any());
    assertEquals("failed assertion", "xlxs", excelWriterStrategy.getMimeType());

  }

  @Test
  public void writeValidations() throws EEAException {
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
    FailedValidationsDatasetVO failedValidationsByIdDataset = new FailedValidationsDatasetVO();
    List<LinkedHashMap<?, ?>> errors = new ArrayList<>();
    LinkedHashMap<String, String> linkedMap = new LinkedHashMap<>();
    linkedMap.put("levelError", "ERROR");
    linkedMap.put("message", "message");
    linkedMap.put("idObject", "id1");
    errors.add(linkedMap);
    LinkedHashMap<String, String> linkedMap2 = new LinkedHashMap<>();
    linkedMap2.put("levelError", "BLOCKER");
    linkedMap2.put("message", "message");
    linkedMap2.put("idObject", "id1");
    errors.add(linkedMap2);
    LinkedHashMap<String, String> linkedMap3 = new LinkedHashMap<>();
    linkedMap3.put("levelError", "WARNING");
    linkedMap3.put("message", "message");
    linkedMap3.put("idObject", "id1");
    errors.add(linkedMap3);
    LinkedHashMap<String, String> linkedMap4 = new LinkedHashMap<>();
    linkedMap4.put("levelError", "ERROR");
    linkedMap4.put("message", "message");
    linkedMap4.put("idObject", "id1");
    errors.add(linkedMap4);
    LinkedHashMap<String, String> linkedMap5 = new LinkedHashMap<>();
    linkedMap5.put("levelError", "WARNING");
    linkedMap5.put("message", "message");
    linkedMap5.put("idObject", "id1");
    errors.add(linkedMap5);
    LinkedHashMap<String, String> linkedMap6 = new LinkedHashMap<>();
    linkedMap6.put("levelError", "INFO");
    linkedMap6.put("message", "message");
    linkedMap6.put("idObject", "id1");
    errors.add(linkedMap6);
    LinkedHashMap<String, String> linkedMap7 = new LinkedHashMap<>();
    linkedMap7.put("levelError", "INFO");
    linkedMap7.put("message", "message");
    linkedMap7.put("idObject", "id1");
    errors.add(linkedMap7);
    failedValidationsByIdDataset.setErrors(errors);
    field.setId("9012");
    field.setIdRecord("3456");
    field.setName("FieldTest_ExcelWriterStrategy");
    field.setType(DataType.TEXT);
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
    Mockito.when(fileCommon.getDataSetSchemaVO(Mockito.any(), Mockito.any())).thenReturn(dataset);
    Mockito.when(fileCommon.findTableSchemaVO(Mockito.any(), Mockito.any())).thenReturn(table);
    Mockito.when(fileCommon.getRecordValuesPaginated(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(values);
    Mockito.when(fileCommon.getErrors(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(failedValidationsByIdDataset);
    excelWriterStrategy.setMimeType(FileTypeEnum.XLSX.getValue());
    excelWriterStrategy.writeFile(1L, 1L, "", false, true);
    excelWriterStrategy.getMimeType();
    Mockito.verify(fileCommon, times(1)).getRecordValuesPaginated(Mockito.any(), Mockito.any(),
        Mockito.any());
    assertEquals("failed assertion", FileTypeEnum.XLSX.getValue(),
        excelWriterStrategy.getMimeType());

  }
}
