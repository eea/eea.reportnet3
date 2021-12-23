package org.eea.dataset.service.file;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
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
 * The Class CSVWriterStrategyTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CSVWriterStrategyTest {

  /** The csv writer strategy. */
  @InjectMocks
  private CSVWriterStrategy csvWriterStrategy;

  /** The parse common. */
  @Mock
  private FileCommonUtils fileCommon;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(csvWriterStrategy, "delimiter", ',');
  }

  /**
   * Csv writer strategy test.
   */
  @Test
  public void csvWriterStrategyTest() {
    CSVWriterStrategy test = new CSVWriterStrategy(',', fileCommon);
    assertNotNull("failed assertion", test);
  }

  /**
   * Test write file.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test
  public void testWriteFile() throws IOException, EEAException {
    List<RecordValue> records = new ArrayList<>();
    List<FieldSchemaVO> fieldSchemas = new ArrayList<>();
    List<FieldValue> fields = new ArrayList<>();
    RecordValue record = new RecordValue();
    FieldValue fieldValue = new FieldValue();
    FieldSchemaVO fieldSchema = new FieldSchemaVO();
    fieldSchema.setId("");
    fieldValue.setIdFieldSchema("");
    fieldValue.setValue("value");
    fields.add(fieldValue);
    record.setFields(fields);
    records.add(record);
    record.setDataProviderCode("ES");
    fieldSchemas.add(fieldSchema);
    Mockito.when(fileCommon.getDataSetSchemaVO(Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    Mockito.when(fileCommon.getRecordValuesPaginated(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(records);
    Mockito.when(fileCommon.getFieldSchemas(Mockito.any(), Mockito.any(DataSetSchemaVO.class)))
        .thenReturn(fieldSchemas);
    csvWriterStrategy.writeFile(1L, 1L, "", true, false);
    Mockito.verify(fileCommon, times(1)).getFieldSchemas(Mockito.any(),
        Mockito.any(DataSetSchemaVO.class));
    Mockito.verify(fileCommon, times(1)).getRecordValuesPaginated(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Test write file write.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test
  public void testWriteFileWrite() throws IOException, EEAException {
    List<RecordValue> records = new ArrayList<>();
    List<FieldSchemaVO> fieldSchemas = new ArrayList<>();
    List<FieldValue> fields = new ArrayList<>();
    RecordValue record = new RecordValue();
    RecordValue record2 = new RecordValue();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setValue("");
    FieldSchemaVO fieldSchema = new FieldSchemaVO();
    fieldSchema.setId("");
    fields.add(fieldValue);
    record.setFields(fields);
    record2.setFields(new ArrayList<>());
    records.add(record);
    records.add(record2);
    fieldSchemas.add(fieldSchema);
    DataSetSchemaVO dataSetSchemaVO = new DataSetSchemaVO();
    List<TableSchemaVO> tableSchemas = new ArrayList<>();
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setIdTableSchema("AAAAAAAA");
    tableSchemas.add(tableSchemaVO);
    dataSetSchemaVO.setTableSchemas(tableSchemas);
    Mockito.when(fileCommon.getDataSetSchemaVO(Mockito.any(), Mockito.any()))
        .thenReturn(dataSetSchemaVO);
    Mockito.when(fileCommon.getRecordValuesPaginated(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(records);
    Mockito.when(fileCommon.getFieldSchemas(Mockito.any(), Mockito.any(DataSetSchemaVO.class)))
        .thenReturn(fieldSchemas);
    csvWriterStrategy.writeFileList(1L, 1L, false, false);
    Mockito.verify(fileCommon, times(1)).getFieldSchemas(Mockito.any(),
        Mockito.any(DataSetSchemaVO.class));
    Mockito.verify(fileCommon, times(1)).getRecordValuesPaginated(Mockito.any(), Mockito.any(),
        Mockito.any());
  }
}
