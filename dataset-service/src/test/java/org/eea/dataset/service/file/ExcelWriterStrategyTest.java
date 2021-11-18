package org.eea.dataset.service.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
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
    List<ErrorsValidationVO> errors = new ArrayList<>();
    ErrorsValidationVO error = new ErrorsValidationVO();
    error.setLevelError("ERROR");
    error.setMessage("message");
    error.setIdObject("id1");
    errors.add(error);
    ErrorsValidationVO error2 = new ErrorsValidationVO();
    error2.setLevelError("BLOCKER");
    error2.setMessage("message");
    error2.setIdObject("id1");
    errors.add(error2);
    ErrorsValidationVO error3 = new ErrorsValidationVO();
    error3.setLevelError("WARNING");
    error3.setMessage("message");
    error3.setIdObject("id1");
    errors.add(error3);
    ErrorsValidationVO error4 = new ErrorsValidationVO();
    error4.setLevelError("ERROR");
    error4.setMessage("message");
    error4.setIdObject("id1");
    errors.add(error4);
    ErrorsValidationVO error5 = new ErrorsValidationVO();
    error5.setLevelError("WARNING");
    error5.setMessage("message");
    error5.setIdObject("id1");
    errors.add(error5);
    ErrorsValidationVO error6 = new ErrorsValidationVO();
    error6.setLevelError("INFO");
    error6.setMessage("message");
    error6.setIdObject("id1");
    errors.add(error6);
    ErrorsValidationVO error7 = new ErrorsValidationVO();
    error7.setLevelError("INFO");
    error7.setMessage("message");
    error7.setIdObject("id1");
    errors.add(error7);
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
